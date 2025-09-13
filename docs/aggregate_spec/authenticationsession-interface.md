# AuthenticationSession Aggregate Interface Specification

## 1. Aggregate Overview
- **Bounded Context**: Security Management
- **Aggregate Root**: AuthenticationSession
- **핵심 책임**: 인증 시도 추적, 계정 잠금 정책 적용, 세션 관리
- **패키지 구조**: com.ldx.hexacore.security.domain.session

## 2. Public Domain Interface

### Aggregate Root
```java
public class AuthenticationSession extends AggregateRoot {
    
    // 생성자/팩토리 메서드
    public static AuthenticationSession create(SessionId sessionId, String userId, ClientIp clientIp);
    
    // 주요 비즈니스 메서드
    public void recordAttempt(String userId, ClientIp clientIp, boolean isSuccessful, RiskLevel riskLevel);
    public void recordAttemptAtSpecificTime(String userId, ClientIp clientIp, boolean isSuccessful, 
                                          RiskLevel riskLevel, LocalDateTime attemptTime);
    public void lockAccount();
    public void unlockAccount();
    public boolean shouldLockAccount();
    
    // 조회 메서드
    public SessionId getSessionId();
    public String getUserId();
    public ClientIp getClientIp();
    public List<AuthenticationAttempt> getAttempts();
    public boolean isLocked();
    public boolean isCurrentlyLocked();
    public LocalDateTime getLockedUntil();
    public LocalDateTime getCreatedAt();
    public LocalDateTime getLastActivityAt();
    public int getFailedAttemptsInWindow();
}
```

### Value Objects (외부 참조 가능한 것만)
```java
public record SessionId(UUID value) {
    public static SessionId generate();
    public static SessionId of(UUID value);
    public static SessionId of(String value);
    public UUID getValue();
    public String toString();
}

public class ClientIp {
    public static ClientIp of(String ipAddress);
    public String getIpAddress();
    public boolean isLocalhost();
    public IpType getType();
}

public class RiskLevel {
    public static RiskLevel of(int score, String reason);
    public static RiskLevel low(String reason);
    public static RiskLevel medium(String reason);
    public static RiskLevel high(String reason);
    public static RiskLevel critical(String reason);
    
    public int getScore();
    public String getReason();
    public RiskCategory getCategory();
    public boolean isLow();
    public boolean isMedium();
    public boolean isHigh();
    public boolean isCritical();
}
```

### Domain Events
```java
public record AccountLocked(
    String eventId,          // 이벤트 ID
    String sessionId,        // 세션 ID
    String userId,           // 사용자 ID
    String clientIp,         // 클라이언트 IP
    LocalDateTime lockedUntil, // 잠금 해제 시각
    int failedAttempts,      // 실패 시도 횟수
    LocalDateTime occurredAt // 발생 시각
) implements DomainEvent {
    
    public static AccountLocked of(String sessionId, String userId, String clientIp,
                                 LocalDateTime lockedUntil, int failedAttempts, LocalDateTime occurredAt);
}
```

### Entities
```java
public class AuthenticationAttempt {
    public static AuthenticationAttempt create(String userId, LocalDateTime attemptedAt, 
                                             boolean isSuccessful, ClientIp clientIp, RiskLevel riskLevel);
    
    public Long getAttemptId();
    public String getUserId();
    public LocalDateTime getAttemptedAt();
    public boolean isSuccessful();
    public ClientIp getClientIp();
    public RiskLevel getRiskLevel();
    public boolean isWithinTimeWindow(LocalDateTime windowStart);
    public boolean isFromSameSource(ClientIp otherIp);
    public int calculateRiskScore();
}
```

## 3. Application Services Interface

### Commands & Handlers
```java
// Command - 세션 생성
public record CreateSessionCommand(
    String sessionId,     // 생성할 세션 ID
    String userId,        // 사용자 ID
    String clientIp       // 클라이언트 IP 주소
) { }

// Command - 인증 시도 기록
public record RecordAttemptCommand(
    String sessionId,     // 대상 세션 ID
    String userId,        // 시도한 사용자 ID
    String clientIp,      // 클라이언트 IP
    boolean isSuccessful, // 성공 여부
    int riskScore,        // 위험도 점수 (0-100)
    String riskReason     // 위험도 이유
) { }

// Command - 세션 잠금
public record LockSessionCommand(
    String sessionId,           // 잠글 세션 ID
    String reason,              // 잠금 사유
    LocalDateTime lockUntil     // 잠금 해제 시각 (선택적)
) { }

// Command - 세션 잠금 해제
public record UnlockSessionCommand(
    String sessionId,     // 잠금 해제할 세션 ID
    String reason         // 해제 사유
) { }

// Handler Interface
@Service
@Transactional
public class SessionCommandHandler {
    public void handle(CreateSessionCommand command);
    public void handle(RecordAttemptCommand command);
    public void handle(LockSessionCommand command);
    public void handle(UnlockSessionCommand command);
}
```

### Queries & Results
```java
// Query - 세션 상태 조회
public record GetSessionStatusQuery(
    String sessionId,    // 조회할 세션 ID
    String userId        // 조회할 사용자 ID (optional)
) { }

// Query - 실패한 인증 시도 조회
public record GetFailedAttemptsQuery(
    String sessionId,           // 세션 ID
    String userId,              // 사용자 ID (선택적)
    LocalDateTime from,         // 조회 시작 시간
    LocalDateTime to,           // 조회 종료 시간
    int limit                   // 조회 제한 개수
) { }

// Result - 세션 상태 응답
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
) { }

// Result - 실패한 인증 시도 응답
public record FailedAttemptsResponse(
    String sessionId,                    // 세션 ID
    List<FailedAttemptResponse> attempts, // 실패 시도 목록
    int totalCount,                      // 전체 실패 시도 수
    LocalDateTime queriedAt              // 조회 시각
) { }

public record FailedAttemptResponse(
    String userId,              // 사용자 ID
    String clientIp,            // 클라이언트 IP
    int riskScore,              // 위험도 점수
    String riskReason,          // 위험도 이유
    LocalDateTime attemptedAt   // 시도 시각
) { }

// Handler Interface
@Service
@Transactional(readOnly = true)
public class SessionQueryHandler {
    public SessionStatusResponse handle(GetSessionStatusQuery query);
    public FailedAttemptsResponse handle(GetFailedAttemptsQuery query);
}
```

### Port Interfaces
```java
// Inbound Port (Application Service)
public interface CreateSessionUseCase {
    void execute(CreateSessionCommand command);
}

public interface RecordAttemptUseCase {
    void execute(RecordAttemptCommand command);
}

public interface GetSessionStatusUseCase {
    SessionStatusResponse execute(GetSessionStatusQuery query);
}

public interface GetFailedAttemptsUseCase {
    FailedAttemptsResponse execute(GetFailedAttemptsQuery query);
}

// Outbound Port (Repository/Gateway)
public interface AuthenticationSessionRepository {
    Optional<AuthenticationSession> findBySessionId(SessionId sessionId);
    AuthenticationSession save(AuthenticationSession session);
    void delete(SessionId sessionId);
}

public interface LoadSessionStatusQueryPort {
    Optional<SessionStatusProjection> loadSessionStatus(String sessionId);
    Optional<SessionStatusProjection> loadSessionStatusByUser(String sessionId, String userId);
}

public interface LoadFailedAttemptsQueryPort {
    List<FailedAttemptProjection> loadFailedAttempts(String sessionId, 
                                                    LocalDateTime from, LocalDateTime to, int limit);
    List<FailedAttemptProjection> loadFailedAttemptsByUser(String sessionId, String userId,
                                                          LocalDateTime from, LocalDateTime to, int limit);
    int countFailedAttempts(String sessionId, LocalDateTime from, LocalDateTime to);
    int countFailedAttemptsByUser(String sessionId, String userId, 
                                 LocalDateTime from, LocalDateTime to);
}

public interface SessionEventPublisher {
    void publish(DomainEvent event);
}
```

## 4. Event Handling Interface

### Inbound Events (세션이 수신하는 이벤트)
```java
// Authentication 애그리거트에서 발생하는 이벤트들
public record AuthenticationAttempted(
    String sessionId,
    String userId,
    String clientIp,
    LocalDateTime attemptedAt
) implements DomainEvent { }

public record AuthenticationSucceeded(
    String sessionId,
    String userId,
    String clientIp,
    LocalDateTime succeededAt
) implements DomainEvent { }

public record AuthenticationFailed(
    String sessionId,
    String userId,
    String clientIp,
    String reason,
    int riskScore,
    LocalDateTime failedAt
) implements DomainEvent { }

// Event Listener
@Component
public class SessionEventListener {
    
    @TransactionalEventListener
    public void handle(AuthenticationAttempted event);
    
    @TransactionalEventListener
    public void handle(AuthenticationSucceeded event);
    
    @TransactionalEventListener
    public void handle(AuthenticationFailed event);
}
```

### Outbound Events (세션이 발행하는 이벤트)
```java
// 다른 애그리거트가 구독할 수 있는 이벤트
public record AccountLocked(
    String eventId,
    String sessionId,
    String userId,
    String clientIp,
    LocalDateTime lockedUntil,
    int failedAttempts,
    LocalDateTime occurredAt
) implements DomainEvent { }
```

## 5. Configuration Interface

### Property Configuration
```yaml
# 세션 관리 설정
hexacore:
  session:
    # 영속성 설정
    persistence:
      enabled: true                    # JPA 어댑터 활성화
      
    # 캐시 설정  
    cache:
      enabled: false                   # 캐시 어댑터 활성화
      ttl: PT15M                      # 캐시 TTL (15분)
      maximum-size: 10000             # 최대 캐시 크기
      
    # 이벤트 설정
    event:
      enabled: true                    # 이벤트 리스너 활성화
      
    # 정책 설정
    policy:
      max-failed-attempts: 5           # 최대 실패 시도 횟수
      lockout-duration: PT30M          # 잠금 지속 시간
      time-window: PT15M               # 시간 윈도우
```

### Spring Configuration
```java
@EnableConfigurationProperties({
    SessionPolicyProperties.class,
    SessionCacheProperties.class
})
@Configuration
public class SessionConfiguration {
    
    // JPA 설정 (조건부)
    @ConditionalOnProperty("hexacore.session.persistence.enabled")
    @Import(SessionPersistenceConfiguration.class)
    static class PersistenceConfig { }
    
    // 캐시 설정 (조건부)
    @ConditionalOnProperty("hexacore.session.cache.enabled")
    @Import(SessionCacheConfiguration.class)
    static class CacheConfig { }
    
    // 이벤트 설정 (조건부)
    @ConditionalOnProperty("hexacore.session.event.enabled")
    @Import(SessionEventConfiguration.class)
    static class EventConfig { }
}
```

## 6. Usage Examples

### Creating Session
```java
// Via Application Service
var command = new CreateSessionCommand("session-123", "user456", "192.168.1.100");
sessionCommandHandler.handle(command);

// Via Use Case
@Autowired
private CreateSessionUseCase createSessionUseCase;

createSessionUseCase.execute(command);
```

### Recording Authentication Attempt
```java
// 성공한 인증 시도
var successCommand = new RecordAttemptCommand(
    "session-123", 
    "user456", 
    "192.168.1.100", 
    true,  // successful
    25,    // low risk
    "Normal login"
);
sessionCommandHandler.handle(successCommand);

// 실패한 인증 시도
var failCommand = new RecordAttemptCommand(
    "session-123", 
    "user456", 
    "192.168.1.100", 
    false, // failed
    75,    // high risk
    "Suspicious IP"
);
sessionCommandHandler.handle(failCommand);
```

### Querying Session Status
```java
// Via Query Handler
var query = new GetSessionStatusQuery("session-123", null);
SessionStatusResponse status = sessionQueryHandler.handle(query);

// Check if locked
if (status.isLocked()) {
    System.out.println("Account locked until: " + status.lockedUntil());
}
```

### Querying Failed Attempts
```java
// 최근 15분간 실패 시도 조회
var query = new GetFailedAttemptsQuery(
    "session-123", 
    null,
    LocalDateTime.now().minusMinutes(15), 
    LocalDateTime.now(), 
    10
);
FailedAttemptsResponse attempts = sessionQueryHandler.handle(query);

System.out.println("Failed attempts: " + attempts.totalCount());
```

### Event Handling
```java
// 이벤트 발행 (내부적으로 처리됨)
@EventListener
public void handleAccountLocked(AccountLocked event) {
    // 알림 서비스에 계정 잠금 알림
    notificationService.sendAccountLockedAlert(
        event.userId(), 
        event.lockedUntil()
    );
}
```

## 7. Important Constraints

### 비즈니스 규칙
- 세션 ID는 시스템 내에서 유일해야 함 (UUID 기반)
- 실패 시도 5회 초과 시 자동 잠금 (정책 설정 가능)  
- 잠금 시간은 30분 (정책 설정 가능)
- 시간 윈도우는 15분 (연속 실패 판단 기준, 정책 설정 가능)
- 성공한 인증 시도 시 기존 잠금 상태 자동 해제

### 기술적 제약
- 세션 ID는 UUID 형식 (36자 문자열)
- IP 주소는 IPv4/IPv6 지원
- 위험도 점수는 0-100 범위
- 쿼리 결과는 최대 1000건으로 제한
- 시간 범위 조회는 최대 30일로 제한
- 동시성 처리를 위한 낙관적 잠금 적용

### 성능 고려사항
- 프로젝션을 통한 읽기 성능 최적화
- 캐시를 통한 응답 시간 단축 (옵션)
- 읽기 전용 트랜잭션으로 리소스 사용 최소화
- 인덱스를 통한 쿼리 성능 최적화
- 페이징을 통한 대용량 데이터 처리

### 보안 제약사항
- 개인정보 최소 수집 원칙
- IP 주소 암호화 저장 고려
- 세션 데이터 90일 후 자동 삭제
- 감사 로그를 통한 접근 추적
- 무차별 대입 공격 방어 (계정 잠금)

## 8. Integration Points

### 의존하는 애그리거트
- **Authentication**: 인증 이벤트 수신 (AuthenticationAttempted, AuthenticationSucceeded, AuthenticationFailed)

### 영향을 받는 애그리거트  
- **SecurityAlert**: AccountLocked 이벤트 구독하여 알림 생성
- **RateLimitBucket**: 세션별 API 호출 제한과 연계

### 외부 시스템 연동
- **모니터링 시스템**: 메트릭 및 알림
- **로그 시스템**: 감사 추적
- **알림 시스템**: 계정 잠금 알림

## 9. Testing Interface

### Test Support Classes
```java
// 테스트용 팩토리
public class AuthenticationSessionTestFixture {
    public static AuthenticationSession createNormalSession(String sessionId, String userId);
    public static AuthenticationSession createLockedSession(String sessionId, String userId);
    public static AuthenticationAttempt createFailedAttempt(String userId, String reason);
}

// 테스트용 이벤트 검증
@TestConfiguration
public class SessionTestConfiguration {
    
    @Bean
    @Primary
    public SessionEventPublisher mockEventPublisher() {
        return Mockito.mock(SessionEventPublisher.class);
    }
}
```

### Integration Test Support
```java
@DataJpaTest
@Import(SessionPersistenceConfiguration.class)
public class SessionPersistenceIntegrationTest {
    // 실제 DB 연동 테스트
}

@SpringBootTest
@TestPropertySource(properties = {
    "hexacore.session.cache.enabled=true",
    "hexacore.session.cache.ttl=PT1M"
})
public class SessionCacheIntegrationTest {
    // 캐시 통합 테스트
}
```

이 인터페이스 명세는 AuthenticationSession 애그리거트의 **모든 공개 인터페이스**를 정의하며, 다른 애그리거트나 외부 시스템이 이 문서만으로 AuthenticationSession과 완전히 통합할 수 있도록 작성되었습니다.