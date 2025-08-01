# Authentication Aggregate Interface Specification

## 1. Aggregate Overview
- **Bounded Context**: Security Context
- **Aggregate Root**: Authentication
- **핵심 책임**: 사용자 인증 및 세션 관리
- **패키지 구조**: com.dx.hexacore.security

## 2. Public Domain Interface

### Aggregate Root
```java
public class Authentication extends AggregateRoot {
    // 생성자/팩토리 메서드
    public static Authentication attemptAuthentication(Credentials credentials);
    
    // 주요 비즈니스 메서드
    public void markAsSuccessful(Token token);
    public void markAsFailed(String reason);
    public void refreshToken(Token newToken);
    public void terminate();
    
    // 조회 메서드
    public AuthenticationId getId();
    public Credentials getCredentials();
    public AuthenticationStatus getStatus();
    public Token getToken();
    public boolean isActive();
}
```

### Value Objects (외부 참조 가능한 것만)
```java
// 인증 ID
public record AuthenticationId(UUID value) {
    public static AuthenticationId generate();
    public static AuthenticationId of(String value);
    public static AuthenticationId of(UUID value);
}

// 자격 증명
public class Credentials {
    public static Credentials of(String username, String password);
    public String getUsername();
    // password는 보안상 getter 제공하지 않음
}

// 토큰
public class Token {
    public static Token of(String accessToken, String refreshToken, Long expiresIn);
    public String getAccessToken();
    public String getRefreshToken();
    public Long getExpiresIn();
    public boolean isExpired();
    public LocalDateTime getExpiresAt();
}

// 인증 상태
public enum AuthenticationStatus {
    ATTEMPTING, SUCCESS, FAILED, TERMINATED;
    public boolean isSuccess();
    public boolean isFailed();
    public boolean isTerminated();
}
```

### Domain Events
```java
// 인증 시도됨
public record AuthenticationAttempted(
    UUID aggregateId,
    String username,
    LocalDateTime attemptTime
) implements DomainEvent { }

// 인증 성공
public record AuthenticationSucceeded(
    UUID aggregateId,
    Token token,
    LocalDateTime successTime
) implements DomainEvent { }

// 인증 실패
public record AuthenticationFailed(
    UUID aggregateId,
    String reason,
    LocalDateTime failTime
) implements DomainEvent { }

// 토큰 갱신됨
public record TokenRefreshed(
    UUID aggregateId,
    Token newToken,
    LocalDateTime refreshTime
) implements DomainEvent { }

// 세션 종료됨
public record SessionTerminated(
    UUID aggregateId,
    LocalDateTime terminateTime
) implements DomainEvent { }
```

## 3. Application Services Interface

### Commands & Handlers
```java
// 로그인 명령
public record LoginCommand(String username, String password) { }

// 로그아웃 명령
public record LogoutCommand(String accessToken) { }

// 토큰 갱신 명령
public record RefreshTokenCommand(String refreshToken) { }

// Command Handler Interface
public interface LoginUseCase {
    LoginResult execute(LoginCommand command);
}

public interface LogoutUseCase {
    void execute(LogoutCommand command);
}

public interface RefreshTokenUseCase {
    RefreshTokenResult execute(RefreshTokenCommand command);
}
```

### Queries & Results
```java
// 토큰 정보 조회
public record GetTokenInfoQuery(String accessToken) { }

// 인증 상태 조회
public record GetAuthenticationStatusQuery(String authenticationId) { }

// Query Results
public record TokenInfo(
    String username,
    boolean isValid,
    Long remainingTime
) { }

public record AuthenticationInfo(
    String id,
    String username,
    String status,
    LocalDateTime lastActivity
) { }

// Query Handler Interface
public interface GetTokenInfoQueryHandler {
    TokenInfo handle(GetTokenInfoQuery query);
}

public interface GetAuthenticationStatusQueryHandler {
    AuthenticationInfo handle(GetAuthenticationStatusQuery query);
}
```

### Port Interfaces
```java
// Inbound Port (Application Service)
public interface AuthenticationService {
    LoginResult login(String username, String password);
    void logout(String token);
    RefreshTokenResult refreshToken(String refreshToken);
}

// Outbound Port (Repository)
public interface AuthenticationRepository {
    Authentication findById(AuthenticationId id);
    Optional<Authentication> findByAccessToken(String accessToken);
    Optional<Authentication> findByRefreshToken(String refreshToken);
    Authentication save(Authentication authentication);
    void delete(Authentication authentication);
}

// Outbound Port (External Auth Provider)
public interface ExternalAuthProvider {
    Token authenticate(Credentials credentials) throws ExternalAuthException;
    Token refreshToken(String refreshToken) throws ExternalAuthException;
    boolean validateToken(String token);
    void revokeToken(String token);
}

// Outbound Port (Event Publisher)
public interface EventPublisher {
    void publish(DomainEvent event);
    void publish(List<DomainEvent> events);
}
```

## 4. Integration Points

### Events Published
- `AuthenticationAttempted`: 로그인 시도 시 발행
- `AuthenticationSucceeded`: 로그인 성공 시 발행
- `AuthenticationFailed`: 로그인 실패 시 발행
- `TokenRefreshed`: 토큰 갱신 시 발행
- `SessionTerminated`: 로그아웃 시 발행

### Events Subscribed
- `UserAccountLocked`: 사용자 계정 잠김 시 해당 세션 종료
- `UserAccountDeleted`: 사용자 계정 삭제 시 해당 세션 종료

### External Dependencies
- `ExternalAuthProvider`: Keycloak 등 외부 인증 시스템 연동
- `EventPublisher`: 도메인 이벤트 발행

## 6. Usage Examples

### Creating Authentication (Login)
```java
// Via Application Service
var command = new LoginCommand("user@example.com", "password123");
LoginResult result = loginUseCase.execute(command);

// Via REST API
POST /api/auth/login
{
    "username": "user@example.com",
    "password": "password123"
}
```

### Refreshing Token
```java
// Via Application Service
var command = new RefreshTokenCommand("refresh-token-value");
RefreshTokenResult result = refreshTokenUseCase.execute(command);

// Via REST API
POST /api/auth/refresh
{
    "refreshToken": "refresh-token-value"
}
```

### Querying Token Info
```java
// Via Query Handler
var query = new GetTokenInfoQuery("access-token-value");
TokenInfo info = queryHandler.handle(query);

// Via Application Service
TokenInfo info = authenticationService.getTokenInfo("access-token-value");
```

## 7. Important Constraints
- **토큰 만료**: Access Token은 설정된 시간(기본 1시간) 후 만료
- **동시 세션**: 동일 사용자의 다중 세션 허용
- **인증 재시도**: 연속된 인증 실패 시 일시적 차단 가능
- **토큰 갱신**: Refresh Token으로만 Access Token 갱신 가능
- **세션 종료**: 로그아웃 시 토큰 즉시 무효화

## 8. Error Codes
- `INVALID_CREDENTIALS`: 잘못된 사용자명 또는 비밀번호
- `TOKEN_EXPIRED`: 토큰 만료
- `INVALID_TOKEN`: 유효하지 않은 토큰
- `SESSION_NOT_FOUND`: 세션을 찾을 수 없음
- `EXTERNAL_AUTH_ERROR`: 외부 인증 시스템 오류