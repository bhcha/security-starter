# AuthenticationSession Aggregate - Phase 6 구현 계획서

## 1. 구현 목표
AuthenticationSession 애그리거트의 Application Layer Query Side를 구현합니다.

## 2. 구현 범위
`/docs/plan/implementation-plan.md`에 명시된 AuthenticationSession 애그리거트의 Application Layer Query Side 컴포넌트들을 구현합니다.

## 3. 구현 컴포넌트 목록

### 3.1 Queries
- **GetSessionStatusQuery**: 세션 상태 조회 쿼리
- **GetFailedAttemptsQuery**: 실패한 인증 시도 목록 조회 쿼리

### 3.2 Query Handlers
- **SessionQueryHandler**: 세션 관련 쿼리 처리 핸들러

### 3.3 Response Objects
- **SessionStatusResponse**: 세션 상태 응답 객체
- **FailedAttemptResponse**: 실패한 인증 시도 응답 객체
- **FailedAttemptsResponse**: 실패한 인증 시도 목록 응답 객체

### 3.4 Projection Classes
- **SessionStatusProjection**: 세션 상태 프로젝션
- **FailedAttemptProjection**: 실패한 인증 시도 프로젝션

### 3.5 Query Ports
- **LoadSessionStatusQueryPort**: 세션 상태 조회 포트
- **LoadFailedAttemptsQueryPort**: 실패한 인증 시도 조회 포트

### 3.6 예외 클래스
- **SessionQueryException**: 세션 쿼리 처리 실패 시 발생하는 예외

## 4. 구현 상세

### 4.1 Queries 구현 계획

#### GetSessionStatusQuery
```java
public record GetSessionStatusQuery(
    String sessionId,           // 조회할 세션 ID
    String userId               // 조회할 사용자 ID (optional)
) {
    // 검증 로직 구현 예정
}
```

#### GetFailedAttemptsQuery
```java
public record GetFailedAttemptsQuery(
    String sessionId,           // 세션 ID
    String userId,              // 사용자 ID (optional)
    LocalDateTime from,         // 조회 시작 시간
    LocalDateTime to,           // 조회 종료 시간
    int limit                   // 조회 제한 개수
) {
    // 검증 로직 구현 예정
}
```

### 4.2 Query Handler 구현 계획

#### SessionQueryHandler
- **책임**: 세션 관련 모든 쿼리 처리
- **메서드**:
  - `handle(GetSessionStatusQuery query)`: 세션 상태 조회
  - `handle(GetFailedAttemptsQuery query)`: 실패한 인증 시도 목록 조회

### 4.3 Response Objects 구현 계획

#### SessionStatusResponse
```java
public record SessionStatusResponse(
    String sessionId,           // 세션 ID
    String primaryUserId,       // 주 사용자 ID
    String primaryClientIp,     // 주 클라이언트 IP
    boolean isLocked,           // 잠금 상태
    LocalDateTime lockedUntil,  // 잠금 해제 시각
    LocalDateTime createdAt,    // 생성 시각
    LocalDateTime lastActivityAt, // 마지막 활동 시각
    int totalAttempts,          // 총 인증 시도 횟수
    int failedAttempts          // 실패한 인증 시도 횟수
) {
    // 정적 팩토리 메서드 구현 예정
}
```

#### FailedAttemptResponse
```java
public record FailedAttemptResponse(
    String userId,              // 사용자 ID
    String clientIp,            // 클라이언트 IP
    int riskScore,              // 위험도 점수
    String riskReason,          // 위험도 이유
    LocalDateTime attemptedAt   // 시도 시각
) {
    // 정적 팩토리 메서드 구현 예정
}
```

#### FailedAttemptsResponse
```java
public record FailedAttemptsResponse(
    String sessionId,           // 세션 ID
    List<FailedAttemptResponse> attempts, // 실패한 시도 목록
    int totalCount,             // 전체 개수
    LocalDateTime queriedAt     // 쿼리 실행 시각
) {
    // 정적 팩토리 메서드 구현 예정
}
```

### 4.4 Projection Classes 구현 계획

#### SessionStatusProjection
- **책임**: 세션 상태 데이터를 읽기 최적화된 형태로 변환
- **매핑**: AuthenticationSession → SessionStatusResponse

#### FailedAttemptProjection
- **책임**: 실패한 인증 시도 데이터를 읽기 최적화된 형태로 변환
- **매핑**: AuthenticationAttempt → FailedAttemptResponse

### 4.5 Query Ports 구현 계획

#### LoadSessionStatusQueryPort
```java
public interface LoadSessionStatusQueryPort {
    Optional<SessionStatusProjection> loadSessionStatus(String sessionId);
    Optional<SessionStatusProjection> loadSessionStatusByUser(String sessionId, String userId);
}
```

#### LoadFailedAttemptsQueryPort
```java
public interface LoadFailedAttemptsQueryPort {
    List<FailedAttemptProjection> loadFailedAttempts(String sessionId, 
                                                    LocalDateTime from, LocalDateTime to, int limit);
    List<FailedAttemptProjection> loadFailedAttemptsByUser(String sessionId, String userId,
                                                          LocalDateTime from, LocalDateTime to, int limit);
    int countFailedAttempts(String sessionId, LocalDateTime from, LocalDateTime to);
}
```

## 5. 패키지 구조
```
com.dx.hexacore.security.application.session/
├── query/
│   ├── GetSessionStatusQuery.java
│   └── GetFailedAttemptsQuery.java
├── handler/
│   └── SessionQueryHandler.java
├── response/
│   ├── SessionStatusResponse.java
│   ├── FailedAttemptResponse.java
│   └── FailedAttemptsResponse.java
├── projection/
│   ├── SessionStatusProjection.java
│   └── FailedAttemptProjection.java
├── port/
│   ├── LoadSessionStatusQueryPort.java
│   └── LoadFailedAttemptsQueryPort.java
└── exception/
    └── SessionQueryException.java
```

## 6. 참조 문서
- `/docs/spec/authenticationsession-domain-layer-spec.md` - 도메인 레이어 명세서
- `/docs/guide/2-coding-standards.md` - 코딩 표준
- `/docs/guide/3-2-application-tdd.md` - Application Layer TDD 가이드
- `/docs/guide/4-2-application-templates.md` - Application Layer 템플릿

## 7. 완료 기준
- [ ] 모든 Query 클래스 구현 완료
- [ ] SessionQueryHandler 구현 완료
- [ ] 모든 Response 클래스 구현 완료
- [ ] 모든 Projection 클래스 구현 완료
- [ ] 모든 Query Port 인터페이스 구현 완료
- [ ] 예외 클래스 구현 완료
- [ ] 단위 테스트 작성 및 통과 (커버리지 80% 이상)
- [ ] 테스트 리뷰 완료
- [ ] 코딩 표준 리뷰 완료

## 8. 제약사항 및 주의사항
- Domain Layer의 AuthenticationSession Aggregate 명세서만 참조 가능
- CQRS 패턴 준수: Query Side는 읽기 전용, 상태 변경 금지
- 읽기 전용 트랜잭션(@Transactional(readOnly = true)) 사용
- 성능 최적화된 프로젝션 데이터 구조 설계
- 페이징 및 필터링 지원

## 9. 테스트 전략
- **단위 테스트**: 각 Query Handler의 개별 동작 검증
- **통합 테스트**: Query Port와의 연동 검증
- **예외 테스트**: 다양한 예외 상황 처리 검증
- **성능 테스트**: 대량 데이터 조회 성능 검증
- **Mock 테스트**: 외부 의존성 격리 테스트

## 10. CQRS 패턴 적용
- **읽기 전용**: 모든 쿼리는 데이터 변경 없이 조회만 수행
- **성능 최적화**: 읽기에 특화된 데이터 구조 사용
- **캐싱 고려**: 자주 조회되는 데이터에 대한 캐싱 전략 고려
- **이벤트 소싱**: 필요시 이벤트 기반 읽기 모델 구축 준비