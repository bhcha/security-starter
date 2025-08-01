# AuthenticationSession Adapter 레이어 명세서

## 개요
AuthenticationSession 애그리거트의 Adapter Layer는 외부 시스템과의 연동을 담당하며, Inbound Adapter(Phase 7)와 Outbound Adapter(Phase 8)로 구성됩니다.

## Inbound Adapters (Phase 7)

### SessionEventListener
**목적**: Authentication 애그리거트 이벤트 수신 및 처리

**패키지**: `com.dx.hexacore.security.session.adapter.inbound.event`

**주요 기능**:
- Authentication 도메인 이벤트 수신
- 세션 생성 및 인증 시도 기록
- 계정 잠금 이벤트 처리

**처리 이벤트**:
```java
@TransactionalEventListener
public void handle(AuthenticationAttempted event) {
    // 새로운 인증 시도 기록
}

@TransactionalEventListener  
public void handle(AuthenticationSucceeded event) {
    // 성공한 인증 시도 기록
}

@TransactionalEventListener
public void handle(AuthenticationFailed event) {
    // 실패한 인증 시도 기록 및 잠금 정책 적용
}

@TransactionalEventListener
public void handle(AccountLocked event) {
    // 계정 잠금 상태 업데이트
}
```

**설정**:
```java
@ConditionalOnProperty(
    prefix = "hexacore.security.session.event",
    name = "enabled", 
    havingValue = "true"
)
```

**트랜잭션 전략**:
- `@TransactionalEventListener`: 커밋 후 이벤트 처리
- 실패 시 로그 기록 (시스템 중단 방지)

## Outbound Adapters (Phase 8)

### 1. JPA Persistence Adapter

#### SessionJpaAdapter
**목적**: 세션 데이터의 영속성 관리

**패키지**: `com.dx.hexacore.security.session.adapter.outbound.persistence`

**구현 포트**:
- `AuthenticationSessionRepository`
- `LoadSessionStatusQueryPort`  
- `LoadFailedAttemptsQueryPort`

**핵심 메서드**:
```java
@Transactional
public AuthenticationSession save(AuthenticationSession session) {
    // 애그리거트 단위 저장
}

public Optional<AuthenticationSession> findBySessionId(SessionId sessionId) {
    // 세션 ID로 조회
}

public List<FailedAttemptProjection> loadFailedAttempts(
    String sessionId, LocalDateTime from, LocalDateTime to, int limit) {
    // 실패한 인증 시도 조회 (페이징 지원)
}
```

#### JPA 엔티티 구조

**SessionJpaEntity**:
```java
@Entity
@Table(name = "authentication_sessions")
public class SessionJpaEntity {
    @Id
    private String sessionId;           // 세션 ID (UUID 문자열)
    private String userId;              // 사용자 ID
    private LocalDateTime lockoutUntil; // 잠금 해제 시각
    private LocalDateTime createdAt;    // 생성 시각
    private LocalDateTime updatedAt;    // 업데이트 시각
    
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    @OrderBy("attemptedAt DESC")
    private List<AuthenticationAttemptJpaEntity> attempts;
}
```

**AuthenticationAttemptJpaEntity**:
```java
@Entity
@Table(name = "authentication_attempts")
public class AuthenticationAttemptJpaEntity {
    @Id
    private String attemptId;           // 시도 ID (UUID)
    private LocalDateTime attemptedAt;  // 시도 시각
    private boolean successful;         // 성공 여부
    private String reason;              // 실패 사유
    private String clientIp;            // 클라이언트 IP
    private int riskLevel;              // 위험도 점수
    
    @ManyToOne(fetch = FetchType.LAZY)
    private SessionJpaEntity session;
}
```

#### 매핑 전략
**SessionMapper**:
```java
@Component
public class SessionMapper {
    // 도메인 → JPA 엔티티
    public SessionJpaEntity toEntity(AuthenticationSession session);
    public void updateEntity(SessionJpaEntity entity, AuthenticationSession session);
    
    // JPA 엔티티 → 도메인
    public AuthenticationSession toDomain(SessionJpaEntity entity);
    
    // JPA 엔티티 → 프로젝션
    public SessionStatusProjection toStatusProjection(SessionJpaEntity entity);
    public FailedAttemptProjection toFailedAttemptProjection(AuthenticationAttemptJpaEntity entity);
}
```

### 2. Cache Adapter

#### SessionCacheAdapter
**목적**: 세션 데이터 캐싱을 통한 성능 최적화

**패키지**: `com.dx.hexacore.security.session.adapter.outbound.cache`

**캐시 전략**:
- **세션 상태**: 15분 TTL, 10,000개 최대 크기
- **실패 시도**: 쿼리별 캐시 키 생성, 동일 TTL
- **무효화**: 새로운 인증 시도 시 관련 캐시 제거

**캐시 구현**:
```java
public interface SessionCache<K, V> {
    Optional<V> get(K key);
    void put(K key, V value);
    void evict(K key);
    void evictAll();
}
```

**Caffeine 구현체**:
```java
public class CaffeineSessionCache<K, V> implements SessionCache<K, V> {
    private final Cache<K, V> cache;
    
    // TTL, 최대 크기, 통계 수집 지원
}
```

#### 캐시 키 전략
- **세션 상태**: `sessionId`
- **실패 시도**: `sessionId:fromEpoch:toEpoch:limit`

#### 성능 특성
- **캐시 히트율**: 85% 이상 목표
- **응답 시간**: 캐시 히트 시 1ms 이하
- **메모리 사용량**: 세션당 평균 2KB

### 3. Configuration

#### SessionPersistenceConfiguration
**목적**: JPA 어댑터 조건부 활성화

```java
@Configuration
@ConditionalOnProperty(
    prefix = "hexacore.security.session.persistence",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true  // 기본값: 활성화
)
@EnableJpaRepositories
public class SessionPersistenceConfiguration {
    
    @Bean
    public SessionJpaAdapter sessionJpaAdapter(...);
    
    @Bean  
    public AuthenticationSessionRepository authenticationSessionRepository(...);
}
```

#### SessionCacheConfiguration
**목적**: 캐시 어댑터 조건부 활성화

```java
@Configuration
@ConditionalOnProperty(
    prefix = "hexacore.security.session.cache", 
    name = "enabled",
    havingValue = "true",
    matchIfMissing = false  // 기본값: 비활성화
)
public class SessionCacheConfiguration {
    
    @Bean
    public SessionCache<String, SessionStatusProjection> sessionStatusCache(...);
    
    @Bean
    @Primary
    public SessionCacheAdapter cachedLoadSessionStatusQueryPort(...);
}
```

## 설정 프로퍼티

### 영속성 설정
```yaml
hexacore:
  session:
    persistence:
      enabled: true  # JPA 어댑터 활성화
```

### 캐시 설정  
```yaml
hexacore:
  session:
    cache:
      enabled: false           # 캐시 어댑터 활성화 여부
      ttl: PT15M              # 캐시 TTL (15분)
      maximum-size: 10000     # 최대 캐시 크기
```

### 이벤트 설정
```yaml
hexacore:
  session:
    event:
      enabled: true  # 이벤트 리스너 활성화
```

## 데이터베이스 스키마

### 테이블 구조
```sql
-- 인증 세션 테이블
CREATE TABLE authentication_sessions (
    session_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    lockout_until TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 인증 시도 테이블
CREATE TABLE authentication_attempts (
    attempt_id VARCHAR(36) PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL,
    attempted_at TIMESTAMP NOT NULL,
    successful BOOLEAN NOT NULL,
    reason VARCHAR(255),
    client_ip VARCHAR(45) NOT NULL,
    risk_level INT NOT NULL,
    
    FOREIGN KEY (session_id) REFERENCES authentication_sessions(session_id),
    INDEX idx_session_attempted_at (session_id, attempted_at DESC),
    INDEX idx_session_successful (session_id, successful)
);
```

### 인덱스 전략
- **기본 키**: UUID 기반 클러스터드 인덱스
- **조회 최적화**: 세션별 시간 역순 인덱스
- **필터링**: 성공/실패 여부별 인덱스

## 모니터링 및 메트릭

### 성능 지표
- **응답 시간**: P50, P95, P99 측정
- **처리량**: 초당 세션 조회/업데이트 수
- **캐시 효율성**: 히트율, 미스율, 제거율

### 에러 지표  
- **데이터베이스 오류율**: 연결 실패, 쿼리 오류
- **캐시 오류율**: Fallback 발생 빈도
- **이벤트 처리 실패율**: 이벤트 리스너 오류

### 비즈니스 지표
- **세션 생성률**: 시간당 새로운 세션 수
- **잠금 발생률**: 계정 잠금 비율
- **실패 시도 패턴**: IP별, 사용자별 분석

## 확장성 고려사항

### 수평 확장
- **데이터베이스**: 읽기 복제본을 통한 조회 성능 향상
- **캐시**: Redis 클러스터 모드 지원 (향후)
- **이벤트**: 메시지 큐 기반 비동기 처리 (향후)

### 데이터 증가 대응
- **파티셔닝**: 날짜별 테이블 분할
- **아카이빙**: 오래된 세션 데이터 별도 저장
- **압축**: 캐시 데이터 압축으로 메모리 효율성

## 보안 고려사항

### 데이터 보호
- **개인정보 최소화**: 필요한 최소한의 정보만 저장
- **암호화**: IP 주소 등 민감 정보 암호화 고려
- **접근 제어**: 데이터베이스 사용자 권한 최소화

### 감사 추적
- **변경 기록**: 모든 세션 상태 변경 로그 기록
- **접근 로그**: 조회 요청에 대한 감사 추적
- **의심 활동**: 비정상적인 패턴 탐지 및 알림

## 장애 복구

### 장애 시나리오별 대응
1. **데이터베이스 장애**:
   - 읽기 복제본으로 Failover
   - 캐시를 통한 제한적 서비스 유지

2. **캐시 장애**:
   - 자동 Fallback to Database
   - 성능 저하 알림

3. **이벤트 처리 장애**:
   - Dead Letter Queue 활용
   - 재처리 메커니즘

### 복구 절차
- **데이터 일관성 검증**: 캐시-DB 동기화 확인
- **성능 회복**: 캐시 워밍업 절차
- **모니터링 복구**: 지표 수집 재시작

## 사용 예시

### 기본 설정 (JPA만 사용)
```yaml
hexacore:
  session:
    persistence:
      enabled: true
    cache:
      enabled: false
```

### 성능 최적화 설정 (JPA + 캐시)
```yaml
hexacore:
  session:
    persistence:
      enabled: true
    cache:
      enabled: true
      ttl: PT10M
      maximum-size: 50000
```

### 개발 환경 설정
```yaml
hexacore:
  session:
    persistence:
      enabled: true
    cache:
      enabled: true
      ttl: PT1M      # 짧은 TTL로 캐시 동작 확인
      maximum-size: 100
```

## 중요 제약사항

### 기술적 제약
- **캐시 일관성**: 캐시와 DB 간 일시적 불일치 가능
- **메모리 사용량**: 캐시 크기에 따른 heap 메모리 소비
- **트랜잭션 범위**: 이벤트 처리는 별도 트랜잭션

### 비즈니스 제약
- **세션 TTL**: 최대 24시간 (정책 설정)
- **잠금 정책**: 5회 실패 시 30분 잠금
- **데이터 보관**: 90일 후 아카이빙

### 성능 제약
- **동시 세션**: 최대 100,000 활성 세션
- **쿼리 응답시간**: 95% 요청이 100ms 이내
- **캐시 적중률**: 80% 이상 유지