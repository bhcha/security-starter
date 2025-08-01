# AuthenticationSession 도메인 레이어 명세서

## 1. 개요
AuthenticationSession 애그리거트의 도메인 레이어 구현 명세서입니다. 이 명세서는 Application Layer에서 Domain Layer를 참조할 때 사용됩니다.

## 2. Value Objects

### 2.1 SessionId
**패키지**: `com.dx.hexacore.security.session.domain.vo`

```java
public class SessionId {
    // 생성 메서드
    public static SessionId generate()              // UUID 기반 새 세션 ID 생성
    public static SessionId of(String value)       // 기존 값으로 세션 ID 생성
    
    // 주요 메서드
    public String getValue()                        // 세션 ID 값 반환
    public boolean equals(Object o)                 // 동등성 비교
    public int hashCode()                           // 해시코드
    public String toString()                        // 문자열 표현
}
```

**검증 규칙**:
- 값이 null이거나 빈 문자열일 수 없음
- UUID 형식을 따라야 함 (36자리, 하이픈 포함)

### 2.2 ClientIp
**패키지**: `com.dx.hexacore.security.session.domain.vo`

```java
public class ClientIp {
    // 생성 메서드
    public static ClientIp of(String ip)           // IP 주소로 ClientIp 생성
    
    // 주요 메서드
    public String getValue()                        // IP 주소 값 반환
    public IpType getType()                         // IP 타입 (IPv4/IPv6) 반환
    public boolean isLocalhost()                    // 로컬호스트 여부 확인
    public boolean equals(Object o)                 // 동등성 비교
    public int hashCode()                           // 해시코드
    public String toString()                        // 문자열 표현
}
```

**검증 규칙**:
- 값이 null이거나 빈 문자열일 수 없음
- 유효한 IPv4 또는 IPv6 형식이어야 함

### 2.3 RiskLevel
**패키지**: `com.dx.hexacore.security.session.domain.vo`

```java
public class RiskLevel {
    // 생성 메서드
    public static RiskLevel low(String reason)     // 낮은 위험도 (0-24점)
    public static RiskLevel medium(String reason)  // 중간 위험도 (25-49점)
    public static RiskLevel high(String reason)    // 높은 위험도 (50-74점)
    public static RiskLevel critical(String reason) // 매우 높은 위험도 (75-100점)
    public static RiskLevel of(int score, String reason) // 점수로 위험도 생성
    
    // 주요 메서드
    public int getScore()                           // 위험도 점수 반환
    public RiskCategory getCategory()               // 위험도 카테고리 반환
    public String getReason()                       // 위험도 이유 반환
    public boolean equals(Object o)                 // 동등성 비교
    public int hashCode()                           // 해시코드
    public String toString()                        // 문자열 표현
}
```

**검증 규칙**:
- 점수는 0-100 범위 내여야 함
- 이유(reason)는 null이거나 빈 문자열일 수 없음

### 2.4 IpType (Enum)
**패키지**: `com.dx.hexacore.security.session.domain.vo`

```java
public enum IpType {
    IPV4,    // IPv4 주소
    IPV6     // IPv6 주소
}
```

### 2.5 RiskCategory (Enum)
**패키지**: `com.dx.hexacore.security.session.domain.vo`

```java
public enum RiskCategory {
    LOW,      // 낮은 위험 (0-24점)
    MEDIUM,   // 중간 위험 (25-49점)
    HIGH,     // 높은 위험 (50-74점)
    CRITICAL  // 매우 높은 위험 (75-100점)
}
```

## 3. Entities

### 3.1 AuthenticationAttempt
**패키지**: `com.dx.hexacore.security.session.domain`

```java
public class AuthenticationAttempt {
    // 생성 메서드
    public static AuthenticationAttempt create(String userId, ClientIp clientIp, 
                                             boolean isSuccessful, RiskLevel riskLevel)
    
    // 주요 메서드
    public String getUserId()                       // 사용자 ID 반환
    public ClientIp getClientIp()                   // 클라이언트 IP 반환
    public boolean isSuccessful()                   // 성공 여부 반환
    public RiskLevel getRiskLevel()                 // 위험도 반환
    public LocalDateTime getAttemptedAt()           // 시도 시각 반환
    public boolean isWithinTimeWindow(LocalDateTime from, LocalDateTime to) // 시간 범위 내 여부
    public boolean equals(Object o)                 // 동등성 비교
    public int hashCode()                           // 해시코드
}
```

**비즈니스 규칙**:
- 인증 시도는 생성 후 수정 불가 (불변)
- 시도 시각은 생성 시점으로 자동 설정
- 실패한 시도만 계정 잠금 로직에 사용됨

## 4. Aggregate Root

### 4.1 AuthenticationSession
**패키지**: `com.dx.hexacore.security.session.domain`

```java
public class AuthenticationSession extends AggregateRoot {
    // 생성 메서드
    public static AuthenticationSession create(SessionId sessionId, String primaryUserId, ClientIp primaryClientIp)
    
    // 주요 비즈니스 메서드
    public void recordAttempt(String userId, ClientIp clientIp, boolean isSuccessful, RiskLevel riskLevel)
    public boolean isAccountLocked()                // 계정 잠금 상태 확인
    public boolean isAccountLocked(String userId)   // 특정 사용자 계정 잠금 상태 확인
    public Optional<LocalDateTime> getLockedUntil(String userId) // 잠금 해제 시각 반환
    public void unlockAccount(String userId)        // 계정 잠금 해제
    
    // 조회 메서드
    public SessionId getSessionId()                 // 세션 ID 반환
    public String getPrimaryUserId()                // 주 사용자 ID 반환
    public ClientIp getPrimaryClientIp()            // 주 클라이언트 IP 반환
    public LocalDateTime getCreatedAt()             // 생성 시각 반환
    public LocalDateTime getLastActivityAt()        // 마지막 활동 시각 반환
    public List<AuthenticationAttempt> getAttempts() // 인증 시도 목록 반환
    public Map<String, LocalDateTime> getLockedUsers() // 잠긴 사용자 목록 반환
}
```

**불변 규칙 (Invariants)**:
1. **계정 잠금 정책**: 15분 시간 윈도우 내에 5회 이상 실패 시 30분간 계정 잠금
2. **세션 식별**: 세션 ID는 생성 후 변경 불가
3. **시간 순서**: 모든 타임스탬프는 시간 순서를 따라야 함
4. **사용자 연관**: 하나의 세션은 여러 사용자의 인증 시도를 기록할 수 있음

**상태 전이**:
- `ACTIVE` → 인증 시도 기록 → `ACTIVE` (성공 시)
- `ACTIVE` → 인증 시도 기록 → `LOCKED` (5회 실패 시)
- `LOCKED` → 시간 경과 또는 명시적 해제 → `ACTIVE`

## 5. Domain Events

### 5.1 AccountLocked
**패키지**: `com.dx.hexacore.security.session.domain.event`

```java
public class AccountLocked extends DomainEvent {
    // 생성 메서드
    public static AccountLocked of(String sessionId, String userId, String clientIp,
                                  LocalDateTime lockedUntil, int failedAttemptCount, 
                                  LocalDateTime occurredAt)
    
    // 메타데이터 메서드
    public String eventType()                       // "AccountLocked" 반환
    public String aggregateId()                     // 세션 ID 반환
    
    // 데이터 접근 메서드
    public String sessionId()                       // 세션 ID 반환
    public String userId()                          // 사용자 ID 반환
    public String clientIp()                        // 클라이언트 IP 반환
    public LocalDateTime lockedUntil()              // 잠금 해제 시각 반환
    public int failedAttemptCount()                 // 실패 횟수 반환
    public LocalDateTime occurredAt()               // 발생 시각 반환
}
```

**발생 조건**:
- 15분 시간 윈도우 내에 5회 이상 인증 실패 시
- AuthenticationSession.recordAttempt() 호출 시 자동 발행

**포함 데이터**:
- 세션 정보: sessionId
- 사용자 정보: userId
- 네트워크 정보: clientIp
- 잠금 정보: lockedUntil, failedAttemptCount
- 시간 정보: occurredAt

## 6. Domain Services
이 애그리거트에는 Domain Services가 없습니다. 모든 비즈니스 로직이 Aggregate Root에 구현되어 있습니다.

## 7. 패키지 구조
```
com.dx.hexacore.security.session.domain/
├── AuthenticationSession.java          # Aggregate Root
├── AuthenticationAttempt.java          # Entity
├── vo/
│   ├── SessionId.java                  # Value Object
│   ├── ClientIp.java                   # Value Object
│   ├── RiskLevel.java                  # Value Object
│   ├── IpType.java                     # Enum
│   └── RiskCategory.java               # Enum
└── event/
    └── AccountLocked.java              # Domain Event
```

## 8. 의존성 관계
- **상위 의존성**: `event.com.dx.hexacore.security.auth.domain.DomainEvent`
- **외부 라이브러리**: Jackson (JSON 직렬화), Jakarta Validation
- **내부 의존성**: 없음 (다른 애그리거트에 의존하지 않음)

## 9. 사용 가이드라인

### 9.1 애그리거트 생성
```java
SessionId sessionId = SessionId.generate();
ClientIp clientIp = ClientIp.of("192.168.1.100");
AuthenticationSession session = AuthenticationSession.create(sessionId, "user123", clientIp);
```

### 9.2 인증 시도 기록
```java
// 성공적인 인증
RiskLevel lowRisk = RiskLevel.low("Normal login");
session.recordAttempt("user123", clientIp, true, lowRisk);

// 실패한 인증
RiskLevel mediumRisk = RiskLevel.medium("Wrong password");
session.recordAttempt("user123", clientIp, false, mediumRisk);
```

### 9.3 계정 잠금 상태 확인
```java
boolean isLocked = session.isAccountLocked("user123");
Optional<LocalDateTime> lockedUntil = session.getLockedUntil("user123");
```

### 9.4 도메인 이벤트 처리
```java
List<DomainEvent> events = session.getDomainEvents();
for (DomainEvent event : events) {
    if (event instanceof AccountLocked accountLocked) {
        // 계정 잠금 이벤트 처리
    }
}
```

## 10. 테스트 전략
- **단위 테스트**: 각 Value Object, Entity, Aggregate의 개별 동작 검증
- **통합 테스트**: Aggregate와 Domain Event 간의 상호작용 검증
- **불변 규칙 테스트**: 비즈니스 규칙 위반 시 예외 발생 확인
- **상태 전이 테스트**: 정상적인 상태 변화 시나리오 검증

이 명세서는 AuthenticationSession 애그리거트의 도메인 레이어 완전한 구현을 기술하며, Application Layer 개발 시 이 문서만을 참조하여 개발할 수 있도록 작성되었습니다.