# Security Starter 코드 개선 작업계획서

## 📋 문서 정보
- **작성일**: 2025-09-09
- **대상 프로젝트**: security-starter (DDD 헥사고날 아키텍처)
- **작업 범위**: 코드 품질 개선 (하드코딩, 우회코드, 불필요한 코드 제거)
- **예상 기간**: 즉시 개선 2주, 점진적 개선 6주

---

## 🎯 개선 목표

### 주요 목표
1. **보안 기능 완성도 향상**: 미구현된 보안 로직 완료
2. **유지보수성 개선**: 하드코딩된 상수 및 중복 코드 제거
3. **코드 품질 향상**: 일관된 패턴 적용 및 재사용성 증대
4. **설정 외부화**: 운영 환경별 설정 분리

### 성공 지표
- 하드코딩된 매직넘버 0개
- 중복 검증 로직 80% 감소
- 테스트 커버리지 90% 이상 유지
- 코드 리뷰 통과율 100%

---

## 🚀 즉시 개선 항목 (우선순위: 높음)

### Phase 1: 보안 기능 완성 ⚡ [CRITICAL]
**목표**: 미구현된 보안 로직 완료로 보안 강화

#### 1.1 의심스러운 활동 감지 구현
- **파일**: `SecurityEventLogger.java:183-186`
- **현재 상태**: TODO 주석으로 방치
- **위험도**: 높음 (보안 취약점)

**구현 내용**:
```java
// 추가할 필드
private final Map<String, SuspiciousActivityTracker> ipActivityMap = new ConcurrentHashMap<>();
private final int FAILURE_THRESHOLD = 5; // Properties로 이동 예정
private final Duration TIME_WINDOW = Duration.ofMinutes(5); // Properties로 이동 예정

// 구현할 메서드
private void checkSuspiciousActivity(String clientIp) {
    SuspiciousActivityTracker tracker = ipActivityMap.computeIfAbsent(
        clientIp, 
        ip -> new SuspiciousActivityTracker()
    );
    
    if (tracker.addFailure(LocalDateTime.now()) >= FAILURE_THRESHOLD) {
        eventLogger.warn("🚨 Suspicious activity detected from IP: {} ({}회 연속 실패)", 
                        clientIp, tracker.getFailureCount());
        
        // 필요시 추가 조치: IP 차단, 알림 등
        publishSuspiciousActivityEvent(clientIp, tracker.getFailureCount());
    }
}
```

**작업 단계**:
1. `SuspiciousActivityTracker` 클래스 생성 (1일)
2. `checkSuspiciousActivity()` 메서드 구현 (1일)  
3. 단위 테스트 작성 (1일)
4. 통합 테스트 및 검증 (1일)

**완료 기준**: 
- ✅ IP별 실패 횟수 추적 동작
- ✅ 임계값 초과시 경고 로그 생성
- ✅ 테스트 커버리지 90% 이상

---

### Phase 2: 매직넘버 상수화 🔢
**목표**: 설정 가능한 상수로 변경하여 유연성 확보

#### 2.1 도메인 상수 분리
**대상 파일 및 상수**:

| 파일 | 현재 상수 | 용도 | 설정 위치 |
|------|-----------|------|-----------|
| `AuthenticationSession.java` | `TIME_WINDOW_MINUTES = 15` | 인증 실패 추적 시간 | `SecurityProperties.session.timeWindowMinutes` |
| `SessionPolicy.java` | `SESSION_TIMEOUT_HOURS = 24` | 세션 타임아웃 | `SecurityProperties.session.timeoutHours` |
| `Token.java` | `MIN_EXPIRES_IN = 1L`<br>`MAX_EXPIRES_IN = 86400L` | 토큰 만료시간 범위 | `SecurityProperties.token.minExpiresIn`<br>`SecurityProperties.token.maxExpiresIn` |
| `Credentials.java` | `MIN_USERNAME_LENGTH = 3`<br>`MAX_USERNAME_LENGTH = 50`<br>`MIN_PASSWORD_LENGTH = 8` | 자격증명 검증 규칙 | `SecurityProperties.validation.*` |

#### 2.2 구현 방안

**2.2.1 SecurityConstants 클래스 생성**:
```java
@Component
@ConfigurationProperties(prefix = "hexacore.security")
public class SecurityConstants {
    private Session session = new Session();
    private Token token = new Token();
    private Validation validation = new Validation();
    
    @Data
    public static class Session {
        private int timeWindowMinutes = 15;
        private int timeoutHours = 24;
        private int maxFailedAttempts = 5;
    }
    
    @Data
    public static class Token {
        private long minExpiresIn = 1L;
        private long maxExpiresIn = 86400L;
    }
    
    @Data  
    public static class Validation {
        private int minUsernameLength = 3;
        private int maxUsernameLength = 50;
        private int minPasswordLength = 8;
    }
}
```

**2.2.2 기존 코드 수정 전략**:
1. **생성자 주입 방식**: 도메인 객체 생성시 상수값 주입
2. **팩토리 패턴**: 설정값을 받는 팩토리 메서드 추가
3. **하위 호환성**: 기존 API는 기본값으로 동작

**작업 단계**:
1. `SecurityConstants` 클래스 생성 (0.5일)
2. Properties 파일 설정 추가 (0.5일)
3. 도메인 클래스별 상수 교체 (2일)
4. 테스트 코드 수정 및 검증 (1일)

**완료 기준**:
- ✅ 모든 매직넘버가 Properties로 관리됨
- ✅ 기존 테스트가 모두 통과함  
- ✅ application.yml을 통한 설정 변경이 반영됨

---

### Phase 3: 공통 검증 유틸리티 구현 🛠️
**목표**: 중복된 검증 로직을 통합하여 코드 재사용성 향상

#### 3.1 ValidationUtils 클래스 설계

**구현 내용**:
```java
public final class ValidationUtils {
    private ValidationUtils() {}
    
    // Null/Empty 검증
    public static void requireNonNullOrEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
    }
    
    // 범위 검증  
    public static void requireInRange(int value, int min, int max, String fieldName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                String.format("%s must be between %d and %d", fieldName, min, max)
            );
        }
    }
    
    // UUID 형식 검증
    public static UUID requireValidUUID(String value, String fieldName) {
        requireNonNullOrEmpty(value, fieldName);
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format for " + fieldName + ": " + value);
        }
    }
    
    // IP 주소 검증
    public static void requireValidIpAddress(String ipAddress, String fieldName) {
        requireNonNullOrEmpty(ipAddress, fieldName);
        String trimmed = ipAddress.trim();
        if (!trimmed.equals(ipAddress)) {
            throw new IllegalArgumentException("Invalid IP address format for " + fieldName + ": " + ipAddress);
        }
        
        try {
            InetAddress.getByName(ipAddress);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid IP address format for " + fieldName + ": " + ipAddress);
        }
    }
}
```

#### 3.2 Value Object 리팩토링

**적용 대상**: `ClientIp`, `SessionId`, `Token`, `Credentials` 등 39개 클래스

**적용 전**:
```java
// ClientIp.java
if (ipAddress == null || ipAddress.trim().isEmpty()) {
    throw new IllegalArgumentException("IP address cannot be null or empty");
}
```

**적용 후**:
```java
// ClientIp.java  
ValidationUtils.requireValidIpAddress(ipAddress, "IP address");
```

**작업 단계**:
1. `ValidationUtils` 클래스 구현 (1일)
2. Value Object별 검증 로직 교체 (2일)
3. 메시지 일관성 검토 및 수정 (0.5일)
4. 테스트 코드 작성 및 기존 테스트 수정 (1일)

**완료 기준**:
- ✅ 중복 검증 코드 80% 감소
- ✅ 일관된 에러 메시지 사용
- ✅ 모든 기존 테스트 통과

---

## 🔄 점진적 개선 항목 (우선순위: 중간)

### Phase 4: 에러 메시지 표준화 📝 
**기간**: 1주 | **담당**: 백엔드 개발자

#### 4.1 메시지 상수 클래스 생성

**구현 방안**:
```java
public final class ValidationMessages {
    // Null/Empty 관련
    public static final String CANNOT_BE_NULL_OR_EMPTY = "%s cannot be null or empty";
    public static final String CANNOT_BE_NULL = "%s cannot be null";
    
    // 형식 관련  
    public static final String INVALID_FORMAT = "Invalid %s format: %s";
    public static final String INVALID_UUID_FORMAT = "Invalid UUID format for %s: %s";
    public static final String INVALID_IP_FORMAT = "Invalid IP address format for %s: %s";
    
    // 범위 관련
    public static final String MUST_BE_BETWEEN = "%s must be between %d and %d";
    public static final String MUST_BE_POSITIVE = "%s must be positive";
    
    // 비즈니스 규칙
    public static final String LOCKED_UNTIL_FUTURE = "Locked until time must be in the future";
    public static final String SESSION_NOT_FOUND = "Session not found: %s";
}
```

#### 4.2 기존 메시지 교체
- **적용 범위**: 146개 `IllegalArgumentException` 사용처
- **일관성 규칙**: 필드명은 소문자, 메시지는 동사원형으로 시작

---

### Phase 5: Builder 패턴 도입 🏗️
**기간**: 2주 | **담당**: 백엔드 개발자

#### 5.1 적용 대상 선별
**복잡한 객체 생성이 필요한 클래스**:
1. `AuthenticationSession` - 5개 이상의 파라미터
2. `AuthenticationAttempt` - 복잡한 검증 로직  
3. 각종 Command 객체들

#### 5.2 Builder 구현 전략
```java
// AuthenticationSession.java에 추가
public static class Builder {
    private SessionId sessionId;
    private String userId;
    private ClientIp clientIp;
    private int maxFailedAttempts = 5; // 기본값
    private int lockoutDurationMinutes = 30; // 기본값
    
    public Builder sessionId(SessionId sessionId) {
        this.sessionId = sessionId;
        return this;
    }
    
    public Builder userId(String userId) {
        this.userId = userId;
        return this;
    }
    
    public Builder clientIp(ClientIp clientIp) {
        this.clientIp = clientIp;
        return this;
    }
    
    public Builder maxFailedAttempts(int maxFailedAttempts) {
        this.maxFailedAttempts = maxFailedAttempts;
        return this;
    }
    
    public Builder lockoutDurationMinutes(int lockoutDurationMinutes) {
        this.lockoutDurationMinutes = lockoutDurationMinutes;
        return this;
    }
    
    public AuthenticationSession build() {
        validateParameters(sessionId, userId, clientIp);
        return new AuthenticationSession(sessionId, userId, clientIp, 
                                       maxFailedAttempts, lockoutDurationMinutes);
    }
}
```

#### 5.3 점진적 적용 계획
1. **Week 1**: Core 도메인 객체 (AuthenticationSession, AuthenticationAttempt)
2. **Week 2**: Command 객체들 및 복잡한 DTO

---

### Phase 6: 캐시/외부 설정 분리 ⚙️
**기간**: 1주 | **담당**: 인프라/백엔드 개발자

#### 6.1 캐시 설정 외부화
**현재 하드코딩된 설정**:
- `ttl = Duration.ofMinutes(15)` → `cache.ttl-minutes`
- `maximumSize = 10000` → `cache.maximum-size`
- Order 값들 → `cache.order.*`

**application.yml 설정**:
```yaml
hexacore:
  security:
    cache:
      enabled: true
      ttl-minutes: 15
      maximum-size: 10000
      order:
        memory: 200
        jpa: 100
```

#### 6.2 버전 정보 외부화
- 하드코딩된 `"Version 1.2.0"` → build.gradle에서 자동 생성
- Git 커밋 해시 포함으로 추적성 향상

---

## 📊 작업 의존성 및 일정

### 의존성 다이어그램
```
Phase 1 (보안 기능) ──┐
                   │
Phase 2 (상수화) ────┼──→ Phase 4 (메시지 표준화)
                   │
Phase 3 (검증유틸) ──┘
                           ↓
                     Phase 5 (Builder 패턴)
                           ↓  
                     Phase 6 (설정 외부화)
```

### 전체 일정
| Phase | 작업 내용 | 기간 | 시작일 | 완료일 | 담당자 |
|-------|-----------|------|--------|--------|---------|
| 1 | 보안 기능 구현 | 4일 | Week 1 | Week 1 | Backend Dev |
| 2 | 상수 분리 | 4일 | Week 1 | Week 2 | Backend Dev |  
| 3 | 검증 유틸리티 | 4.5일 | Week 2 | Week 2 | Backend Dev |
| 4 | 메시지 표준화 | 5일 | Week 3 | Week 3 | Backend Dev |
| 5 | Builder 패턴 | 10일 | Week 4 | Week 5 | Backend Dev |
| 6 | 설정 외부화 | 5일 | Week 6 | Week 6 | Backend/Infra Dev |

**총 소요기간**: 6주 (즉시 개선: 2주, 점진적 개선: 4주)

---

## ⚠️ 리스크 분석 및 대응방안

### 기술적 리스크

#### 1. 의존성 주입 복잡성 증가 [중간]
**리스크**: 상수 분리로 인한 DI 복잡도 증가  
**대응**: 
- 팩토리 패턴으로 생성 로직 캡슐화
- 기본값 제공으로 하위 호환성 보장

#### 2. 테스트 코드 수정 범위 확대 [낮음]  
**리스크**: 기존 테스트의 대규모 수정 필요
**대응**:
- 테스트용 Builder 클래스 제공
- 기존 API 유지로 점진적 마이그레이션

### 일정 리스크

#### 1. Phase 1 지연 시 전체 일정 영향 [높음]
**리스크**: 보안 기능 구현 지연으로 후속 작업 연쇄 지연  
**대응**:
- Phase 1을 최우선으로 집중 투입
- 병렬 작업 가능한 Phase 2, 3 우선 착수

#### 2. 테스트 작성 시간 과소산정 [중간]
**리스크**: 복잡한 검증 로직의 테스트 작성 시간 초과  
**대응**:
- 테스트 시간을 20% 추가 확보
- Mock 객체 활용으로 테스트 간소화

### 품질 리스크  

#### 1. 기존 기능 회귀 [중간]
**리스크**: 리팩토링 과정에서 기존 기능 손상  
**대응**:
- 각 Phase별 철저한 테스트 실행
- Code Review 의무화
- Canary 배포로 점진적 출시

#### 2. 성능 저하 [낮음]  
**리스크**: 추가된 검증 로직으로 인한 성능 저하
**대응**:
- 성능 테스트 수행
- 필요시 lazy validation 적용

---

## ✅ 검증 및 완료 기준

### Phase별 완료 기준

#### Phase 1: 보안 기능
- [ ] `checkSuspiciousActivity()` 메서드 완전 구현
- [ ] IP별 실패 추적 정상 동작  
- [ ] 임계값 초과시 경고 로그 생성
- [ ] 단위/통합 테스트 커버리지 90% 이상
- [ ] 보안 테스트 통과

#### Phase 2: 상수 분리  
- [ ] 모든 매직넘버가 Properties로 이전
- [ ] application.yml 설정으로 값 변경 가능
- [ ] 기존 테스트 100% 통과
- [ ] 하위 호환성 보장

#### Phase 3: 검증 유틸리티
- [ ] ValidationUtils 클래스 구현 완료  
- [ ] 기존 중복 코드 80% 감소
- [ ] 일관된 에러 메시지 적용
- [ ] 모든 Value Object 리팩토링 완료

#### Phase 4-6: 점진적 개선
- [ ] 각 Phase별 개별 완료 기준 달성
- [ ] 전체 테스트 통과
- [ ] 성능 테스트 통과
- [ ] 문서 업데이트 완료

### 최종 검증
- [ ] 전체 테스트 실행 시 100% 통과
- [ ] 코드 커버리지 90% 이상 유지  
- [ ] SonarQube 품질 게이트 통과
- [ ] 성능 벤치마크 기준치 달성
- [ ] 보안 스캔 통과

---

## 📚 참고 자료

### 개발 가이드라인  
- [DDD 헥사고날 아키텍처 가이드](./CLAUDE.md)
- [코딩 표준](./docs/guide/2-coding-standards.md) 
- [TDD 워크플로우](./docs/guide/3-tdd-workflow.md)

### 설정 및 프로퍼티
- [HexacoreSecurityProperties](./src/main/java/com/dx/hexacore/security/config/properties/HexacoreSecurityProperties.java)
- [보안 설정 검증기](./src/main/java/com/dx/hexacore/security/config/support/SecurityConfigurationValidator.java)

### 테스트 전략
- [도메인 TDD 가이드](./docs/guide/3-1-domain-tdd.md)
- [애플리케이션 TDD 가이드](./docs/guide/3-2-application-tdd.md)

---

## 📞 연락처 및 승인

**작성자**: Claude Code  
**검토자**: 프로젝트 매니저  
**승인자**: 기술 리더  

**승인 필요 사항**:  
- [ ] 전체 일정 및 리소스 계획 승인
- [ ] Phase 1 (보안 기능) 긴급 착수 승인  
- [ ] 테스트 환경 및 CI/CD 파이프라인 준비

---

*📅 최종 수정일: 2025-09-09*  
*📄 문서 버전: 1.0*