# Security Starter - 실제 Public API 문서

## 개요

이 문서는 security-starter에서 실제로 구현된 Public API와 인터페이스를 상세히 설명합니다. 헥사고날 아키텍처를 준수하여 Port 인터페이스만 공개하고, 모든 구현체는 package-private로 숨겨져 있습니다.

## 🏛️ 아키텍처 원칙

### Public 요소
- ✅ **Use Case 인터페이스**: Application Layer의 Inbound Port
- ✅ **Repository 인터페이스**: Application Layer의 Outbound Port
- ✅ **Domain 객체**: Command, Result, Event, Value Object
- ✅ **Auto-Configuration**: 라이브러리 조립 로직

### Package-Private 요소
- 🔒 **Use Case 구현체**: `XxxUseCaseImpl`
- 🔒 **Adapter 구현체**: `XxxAdapter`
- 🔒 **Configuration**: `XxxConfig` (Auto-Configuration 제외)

## 📦 Domain Layer Public API

### Value Objects

#### Credentials (인증 자격증명)
```java
package com.ldx.hexacore.security.auth.domain.vo;

public final class Credentials {
    // 정적 팩토리 메서드 (상수 주입 버전)
    public static Credentials of(String username, String password,
                               int minUsernameLength, int maxUsernameLength,
                               int minPasswordLength);

    // 정적 팩토리 메서드 (기본 제약사항, deprecated)
    @Deprecated
    public static Credentials of(String username, String password);

    // Getter 메서드
    public String getUsername();
    public String getPassword();

    // Object 메서드
    public boolean equals(Object o);
    public int hashCode();
    public String toString(); // 비밀번호는 마스킹됨
}
```

**제약사항:**
- Username: 3-50자, 영문/숫자/언더스코어만 허용
- Password: 최소 8자 이상
- 불변 객체 (final class, final fields)

#### Token (JWT 토큰 정보)
```java
package com.ldx.hexacore.security.auth.domain.vo;

public final class Token {
    // 정적 팩토리 메서드 (상수 주입 버전)
    public static Token of(String accessToken, String refreshToken, long expiresIn,
                          long minExpiresIn, long maxExpiresIn);

    // 정적 팩토리 메서드 (기본 제약사항, deprecated)
    @Deprecated
    public static Token of(String accessToken, String refreshToken, long expiresIn);

    // Getter 메서드
    public String getAccessToken();
    public String getRefreshToken();
    public long getExpiresIn();
    public boolean isExpired();

    // 유틸리티 메서드
    public Token expire(long minExpiresIn, long maxExpiresIn);
    @Deprecated
    public Token expire();

    // Object 메서드
    public boolean equals(Object o);
    public int hashCode();
    public String toString(); // 토큰은 마스킹됨
}
```

**제약사항:**
- AccessToken: null/empty 불가
- RefreshToken: null/empty 불가
- ExpiresIn: 1-86400초 (최대 24시간)

#### AuthenticationStatus (인증 상태)
```java
package com.ldx.hexacore.security.auth.domain.vo;

public enum AuthenticationStatus {
    SUCCESS,
    FAILED,
    LOCKED,
    EXPIRED
}
```

### Domain Events

#### AuthenticationSucceeded (인증 성공)
```java
package com.ldx.hexacore.security.auth.domain.event;

public record AuthenticationSucceeded(
    String userId,
    String username,
    Instant authenticatedAt,
    String clientIp,
    String userAgent
) implements DomainEvent {

    @Override
    public Instant occurredOn() {
        return authenticatedAt;
    }
}
```

#### AuthenticationFailed (인증 실패)
```java
package com.ldx.hexacore.security.auth.domain.event;

public record AuthenticationFailed(
    String username,
    String reason,
    Instant failedAt,
    String clientIp,
    String userAgent,
    int attemptCount
) implements DomainEvent {

    @Override
    public Instant occurredOn() {
        return failedAt;
    }
}
```

#### TokenExpired (토큰 만료)
```java
package com.ldx.hexacore.security.auth.domain.event;

public record TokenExpired(
    String userId,
    String tokenType, // "access" | "refresh"
    Instant expiredAt,
    String clientIp
) implements DomainEvent {

    @Override
    public Instant occurredOn() {
        return expiredAt;
    }
}
```

### Aggregate Root

#### Authentication (인증 애그리거트)
```java
package com.ldx.hexacore.security.auth.domain;

public class Authentication extends AggregateRoot {
    // 정적 팩토리 메서드
    public static Authentication create(String userId, Credentials credentials);
    public static Authentication fromSession(String sessionId, String userId);

    // 비즈니스 메서드
    public AuthenticationResult authenticate(Credentials credentials);
    public void recordFailure(String reason);
    public void lockAccount();
    public void unlockAccount();

    // 조회 메서드
    public String getUserId();
    public String getSessionId();
    public AuthenticationStatus getStatus();
    public int getFailedAttempts();
    public Instant getLastAttemptAt();
    public boolean isLocked();

    // 도메인 이벤트 발행
    public List<DomainEvent> getDomainEvents();
    public void clearDomainEvents();
}
```

## 🔄 Application Layer Public API

### Commands & Results

#### AuthenticateCommand (인증 명령)
```java
package com.ldx.hexacore.security.auth.application.command.port.in;

public record AuthenticateCommand(
    String username,
    String password,
    String clientIp,
    String userAgent
) {
    public AuthenticateCommand {
        Objects.requireNonNull(username, "Username is required");
        Objects.requireNonNull(password, "Password is required");
    }
}
```

#### AuthenticationResult (인증 결과)
```java
package com.ldx.hexacore.security.auth.application.command.port.in;

public record AuthenticationResult(
    boolean success,
    String userId,
    String username,
    Token token,
    String errorMessage,
    AuthenticationStatus status
) {
    // 정적 팩토리 메서드
    public static AuthenticationResult success(String userId, String username, Token token);
    public static AuthenticationResult failure(String errorMessage, AuthenticationStatus status);
    public static AuthenticationResult locked(String username);
}
```

#### RefreshTokenCommand (토큰 갱신 명령)
```java
package com.ldx.hexacore.security.auth.application.command.port.in;

public record RefreshTokenCommand(
    String refreshToken,
    String clientIp,
    String userAgent
) {
    public RefreshTokenCommand {
        Objects.requireNonNull(refreshToken, "Refresh token is required");
    }
}
```

#### ValidateTokenCommand (토큰 검증 명령)
```java
package com.ldx.hexacore.security.auth.application.command.port.in;

public record ValidateTokenCommand(
    String accessToken,
    String requestUri,
    String httpMethod,
    String clientIp,
    String userAgent
) {
    public ValidateTokenCommand {
        Objects.requireNonNull(accessToken, "Access token is required");
    }
}
```

#### TokenValidationResult (토큰 검증 결과)
```java
package com.ldx.hexacore.security.auth.application.command.port.in;

public record TokenValidationResult(
    boolean valid,
    String userId,
    String username,
    Map<String, Object> claims,
    Instant expiresAt,
    String errorMessage
) {
    // 정적 팩토리 메서드
    public static TokenValidationResult valid(String userId, String username,
                                            Map<String, Object> claims, Instant expiresAt);
    public static TokenValidationResult invalid(String errorMessage);
}
```

### Use Case Interfaces (Inbound Ports)

#### AuthenticationUseCase (인증 사용 사례)
```java
package com.ldx.hexacore.security.auth.application.command.port.in;

/**
 * 인증 관련 사용 사례를 정의하는 인바운드 포트
 */
public interface AuthenticationUseCase {

    /**
     * 사용자 인증을 수행합니다
     *
     * @param command 인증 명령 (사용자명, 비밀번호 포함)
     * @return 인증 결과 (성공/실패, 토큰 정보 포함)
     * @throws IllegalArgumentException command가 null인 경우
     */
    AuthenticationResult authenticate(AuthenticateCommand command);
}
```

#### TokenManagementUseCase (토큰 관리 사용 사례)
```java
package com.ldx.hexacore.security.auth.application.command.port.in;

/**
 * 토큰 관리 관련 사용 사례를 정의하는 인바운드 포트
 */
public interface TokenManagementUseCase {

    /**
     * 액세스 토큰을 검증합니다
     */
    TokenValidationResult validateToken(ValidateTokenCommand command);

    /**
     * 리프레시 토큰으로 새로운 토큰을 발급합니다
     */
    AuthenticationResult refreshToken(RefreshTokenCommand command);
}
```

### Outbound Port Interfaces

#### TokenProvider (토큰 제공자)
```java
package com.ldx.hexacore.security.auth.application.command.port.out;

/**
 * 토큰 제공자 인터페이스
 * 다양한 토큰 제공자 구현체를 추상화합니다
 */
public interface TokenProvider {

    /**
     * 자격증명으로 토큰을 발급합니다
     */
    Token issueToken(Credentials credentials) throws TokenProviderException;

    /**
     * 액세스 토큰을 검증합니다
     */
    TokenValidationResult validateToken(String accessToken) throws TokenProviderException;

    /**
     * 컨텍스트와 함께 토큰을 검증합니다 (기본 구현 제공)
     */
    default TokenValidationResult validateTokenWithContext(String accessToken,
                                                          TokenValidationContext context)
            throws TokenProviderException {
        return validateToken(accessToken);
    }

    /**
     * 리프레시 토큰으로 새로운 토큰을 발급합니다
     */
    Token refreshToken(String refreshToken) throws TokenProviderException;

    /**
     * 토큰 제공자 타입을 반환합니다
     */
    TokenProviderType getProviderType();
}
```

#### ExternalAuthProvider (외부 인증 제공자)
```java
package com.ldx.hexacore.security.auth.application.command.port.out;

/**
 * 외부 인증 시스템과의 통합을 위한 포트
 */
public interface ExternalAuthProvider {

    /**
     * 외부 시스템에서 사용자를 인증합니다
     */
    AuthenticationResult authenticate(Credentials credentials) throws ExternalAuthException;

    /**
     * 외부 시스템에서 토큰을 검증합니다
     */
    TokenValidationResult validateToken(String token) throws ExternalAuthException;

    /**
     * 외부 인증 제공자 타입을 반환합니다
     */
    String getProviderType();
}
```

#### EventPublisher (이벤트 발행자)
```java
package com.ldx.hexacore.security.auth.application.command.port.out;

/**
 * 도메인 이벤트 발행을 위한 포트
 */
public interface EventPublisher {

    /**
     * 단일 도메인 이벤트를 발행합니다
     */
    void publish(DomainEvent event);

    /**
     * 여러 도메인 이벤트를 발행합니다
     */
    void publishAll(List<DomainEvent> events);
}
```

### Token Provider Support Types

#### TokenProviderType (토큰 제공자 타입)
```java
package com.ldx.hexacore.security.auth.application.command.port.out;

public enum TokenProviderType {
    SPRING_JWT("Spring JWT"),
    KEYCLOAK("Keycloak"),
    NO_OP("No Operation");

    private final String displayName;

    TokenProviderType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
```

#### TokenValidationContext (토큰 검증 컨텍스트)
```java
package com.ldx.hexacore.security.auth.application.command.port.out;

@Builder
public record TokenValidationContext(
    String requestUri,
    String httpMethod,
    String clientIp,
    String userAgent,
    boolean checkResourcePermission
) {
    public static class TokenValidationContextBuilder {
        // Lombok이 생성하는 빌더 클래스
    }
}
```

#### TokenProviderException (토큰 제공자 예외)
```java
package com.ldx.hexacore.security.auth.application.command.port.out;

public class TokenProviderException extends RuntimeException {
    private final TokenProviderErrorCode errorCode;
    private final String providerType;

    public TokenProviderException(String message, TokenProviderErrorCode errorCode, String providerType);
    public TokenProviderException(String message, TokenProviderErrorCode errorCode, String providerType, Throwable cause);

    // 정적 팩토리 메서드
    public static TokenProviderException invalidCredentials(String providerType);
    public static TokenProviderException tokenExpired(String providerType);
    public static TokenProviderException tokenValidationFailed(String providerType, Throwable cause);
    public static TokenProviderException tokenIssueFailed(String providerType, Throwable cause);
    public static TokenProviderException tokenRefreshFailed(String providerType, Throwable cause);

    // Getter 메서드
    public TokenProviderErrorCode getErrorCode();
    public String getProviderType();
}
```

#### TokenProviderErrorCode (토큰 제공자 오류 코드)
```java
package com.ldx.hexacore.security.auth.application.command.port.out;

public enum TokenProviderErrorCode {
    INVALID_CREDENTIALS("Invalid credentials provided"),
    TOKEN_EXPIRED("Token has expired"),
    TOKEN_VALIDATION_FAILED("Token validation failed"),
    TOKEN_ISSUE_FAILED("Token issuance failed"),
    TOKEN_REFRESH_FAILED("Token refresh failed"),
    PROVIDER_UNAVAILABLE("Token provider is unavailable"),
    CONFIGURATION_ERROR("Configuration error");

    private final String defaultMessage;

    TokenProviderErrorCode(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
```

## 🔧 Configuration Layer Public API

### Auto Configuration

#### SecurityStarterAutoConfiguration (메인 자동 설정)
```java
package com.ldx.hexacore.security.config;

@Configuration("securityStarterAutoConfiguration")
@ConditionalOnClass(AuthenticationUseCase.class)
@ConditionalOnProperty(
    prefix = "security-starter",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
@EnableConfigurationProperties({
    SecurityStarterProperties.class,
    SecurityConstants.class
})
@Import({
    TraditionalModeConfiguration.class,
    HexagonalModeConfiguration.class,
    TokenProviderAutoConfiguration.class,
    SecurityFilterAutoConfiguration.class,
    ApplicationLayerAutoConfiguration.class,
    SupportBeansAutoConfiguration.class,
    LoggingBeansAutoConfiguration.class
})
public class SecurityStarterAutoConfiguration {

    public SecurityStarterAutoConfiguration(SecurityStarterProperties securityStarterProperties);

    @PostConstruct
    public void init(); // 초기화 로깅
}
```

#### TokenProviderAutoConfiguration (토큰 제공자 자동 설정)
```java
package com.ldx.hexacore.security.config.autoconfigure;

@Configuration
@EnableConfigurationProperties(SecurityStarterProperties.class)
@ConditionalOnClass(TokenProvider.class)
public class TokenProviderAutoConfiguration {

    // Spring JWT Token Provider Bean
    @Bean(name = "springJwtTokenProvider")
    @ConditionalOnClass(name = "io.jsonwebtoken.JwtBuilder")
    @ConditionalOnProperty(prefix = "security-starter.token-provider.jwt", name = "enabled", havingValue = "true", matchIfMissing = false)
    @ConditionalOnMissingBean(TokenProvider.class)
    public TokenProvider springJwtTokenProvider(SecurityStarterProperties properties);

    // No-Op Token Provider Bean (Fallback)
    @Bean(name = "noOpTokenProvider")
    @ConditionalOnMissingClass("io.jsonwebtoken.JwtBuilder")
    @ConditionalOnMissingBean(TokenProvider.class)
    public TokenProvider noOpTokenProvider();

    // Keycloak Token Provider 중첩 설정
    @Configuration
    @ConditionalOnProperty(prefix = "security-starter.token-provider", name = "provider", havingValue = "keycloak")
    @ConditionalOnClass(name = "com.ldx.hexacore.security.auth.adapter.outbound.token.keycloak.KeycloakTokenProvider")
    public static class KeycloakTokenProviderConfiguration {

        @Bean
        @ConditionalOnProperty(prefix = "security-starter.token-provider.keycloak", name = "enabled", havingValue = "true", matchIfMissing = false)
        public TokenProvider keycloakTokenProvider(SecurityStarterProperties properties);
    }
}
```

### Properties Classes

#### SecurityStarterProperties (메인 설정 프로퍼티)
```java
package com.ldx.hexacore.security.config.properties;

@Data
@ConfigurationProperties(prefix = "security-starter")
@Validated
public class SecurityStarterProperties {

    // 기본 설정
    @NotNull
    private Boolean enabled = true;
    private Mode mode = Mode.TRADITIONAL;

    // Feature Toggles
    @NestedConfigurationProperty
    private FeatureToggle authenticationToggle = new FeatureToggle(true);
    @NestedConfigurationProperty
    private FeatureToggle sessionToggle = new FeatureToggle(true);
    @NestedConfigurationProperty
    private FeatureToggle jwtToggle = new FeatureToggle(true);
    @NestedConfigurationProperty
    private FeatureToggle rateLimitToggle = new FeatureToggle(false);
    @NestedConfigurationProperty
    private FeatureToggle ipRestrictionToggle = new FeatureToggle(false);
    @NestedConfigurationProperty
    private FeatureToggle headersToggle = new FeatureToggle(true);

    // 중첩 설정 객체들
    @Valid @NestedConfigurationProperty
    private JwtIntegration jwt = new JwtIntegration();
    @Valid @NestedConfigurationProperty
    private TokenProvider tokenProvider = new TokenProvider();
    @Valid @NestedConfigurationProperty
    private AuthFilterProperties filter = new AuthFilterProperties();
    @Valid @NestedConfigurationProperty
    private SessionProperties session = new SessionProperties();
    @Valid @NestedConfigurationProperty
    private CacheProperties cache = new CacheProperties();
    @Valid @NestedConfigurationProperty
    private RateLimitProperties rateLimit = new RateLimitProperties();
    @Valid @NestedConfigurationProperty
    private IpRestrictionProperties ipRestriction = new IpRestrictionProperties();
    @Valid @NestedConfigurationProperty
    private HeadersProperties headers = new HeadersProperties();

    // 편의 메서드들 (조건 로직을 Properties 내부로 이동)
    public boolean isAuthenticationEnabled();
    public boolean isSessionEnabled();
    public boolean isJwtEnabled();
    public boolean isRateLimitEnabled();
    public boolean isIpRestrictionEnabled();
    public boolean isHeadersEnabled();
    public boolean isKeycloakEnabled();

    // 내부 클래스들
    public enum Mode { TRADITIONAL, HEXAGONAL }

    @Data
    public static class FeatureToggle {
        private boolean enabled;

        public FeatureToggle() { this.enabled = false; }
        public FeatureToggle(boolean defaultEnabled) { this.enabled = defaultEnabled; }

        public boolean isEnabled() { return enabled; }
        public Boolean getEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    // 기타 중첩 클래스들... (전체 구조는 CONFIGURATION.md 참조)
}
```

#### SecurityConstants (보안 상수 설정)
```java
package com.ldx.hexacore.security.config;

@Data
@ConfigurationProperties(prefix = "security-starter.constants")
public class SecurityConstants {

    private Session session = new Session();
    private Token token = new Token();
    private Validation validation = new Validation();
    private Logging logging = new Logging();

    // 중첩 클래스들
    @Data
    public static class Session {
        private int timeWindowMinutes = 15;
        private int timeoutHours = 24;
        private int maxFailedAttempts = 5;
        private int lockoutDurationMinutes = 30;
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
        private int minJwtSecretLength = 32;
        private int recommendedJwtSecretLength = 64;
    }

    @Data
    public static class Logging {
        private int suspiciousActivityThreshold = 5;
        private int suspiciousActivityTimeWindowMinutes = 5;
        private int maxLogMessageLength = 50;
        private int topStatsLimit = 5;
    }

    // 편의 메서드
    public Duration getSessionTimeWindow();
    public Duration getSuspiciousActivityTimeWindow();
}
```

## 🎯 사용 예제

### 1. 기본 인증 사용
```java
@Service
public class UserService {

    private final AuthenticationUseCase authenticationUseCase;

    public AuthenticationResult login(String username, String password, HttpServletRequest request) {
        AuthenticateCommand command = new AuthenticateCommand(
            username,
            password,
            getClientIp(request),
            request.getHeader("User-Agent")
        );

        return authenticationUseCase.authenticate(command);
    }
}
```

### 2. 토큰 검증 사용
```java
@Service
public class TokenService {

    private final TokenManagementUseCase tokenManagementUseCase;

    public TokenValidationResult validateUserToken(String token, HttpServletRequest request) {
        ValidateTokenCommand command = new ValidateTokenCommand(
            token,
            request.getRequestURI(),
            request.getMethod(),
            getClientIp(request),
            request.getHeader("User-Agent")
        );

        return tokenManagementUseCase.validateToken(command);
    }
}
```

### 3. 커스텀 토큰 제공자 구현
```java
@Component
public class CustomTokenProvider implements TokenProvider {

    @Override
    public Token issueToken(Credentials credentials) throws TokenProviderException {
        // 사용자 정의 토큰 발급 로직
        return Token.of(accessToken, refreshToken, expiresIn, minExpires, maxExpires);
    }

    @Override
    public TokenValidationResult validateToken(String accessToken) throws TokenProviderException {
        // 사용자 정의 토큰 검증 로직
        return TokenValidationResult.valid(userId, username, claims, expiresAt);
    }

    @Override
    public Token refreshToken(String refreshToken) throws TokenProviderException {
        // 사용자 정의 토큰 갱신 로직
        return Token.of(newAccessToken, newRefreshToken, expiresIn, minExpires, maxExpires);
    }

    @Override
    public TokenProviderType getProviderType() {
        return TokenProviderType.SPRING_JWT; // 또는 사용자 정의 타입
    }
}
```

### 4. 도메인 이벤트 처리
```java
@Component
public class SecurityEventHandler {

    @EventListener
    public void handleAuthenticationSuccess(AuthenticationSucceeded event) {
        log.info("User {} authenticated successfully from {}",
                event.username(), event.clientIp());
        // 성공 로깅, 통계 수집 등
    }

    @EventListener
    public void handleAuthenticationFailure(AuthenticationFailed event) {
        log.warn("Authentication failed for user {} from {}: {}",
                event.username(), event.clientIp(), event.reason());
        // 실패 로깅, 보안 알림 등
    }

    @EventListener
    public void handleTokenExpired(TokenExpired event) {
        log.info("Token expired for user {} (type: {})",
                event.userId(), event.tokenType());
        // 토큰 만료 처리
    }
}
```

### 5. 사용자 정의 인증 로직
```java
@Service
public class CustomAuthenticationService {

    private final AuthenticationUseCase authenticationUseCase;
    private final TokenManagementUseCase tokenManagementUseCase;

    public LoginResponse authenticateUser(LoginRequest request, HttpServletRequest httpRequest) {
        try {
            // 1. 인증 수행
            AuthenticateCommand command = new AuthenticateCommand(
                request.getUsername(),
                request.getPassword(),
                getClientIp(httpRequest),
                httpRequest.getHeader("User-Agent")
            );

            AuthenticationResult result = authenticationUseCase.authenticate(command);

            if (result.success()) {
                // 2. 성공 응답 생성
                return LoginResponse.success(
                    result.token().getAccessToken(),
                    result.token().getRefreshToken(),
                    result.token().getExpiresIn()
                );
            } else {
                // 3. 실패 응답 생성
                return LoginResponse.failure(result.errorMessage());
            }

        } catch (Exception e) {
            log.error("Authentication error", e);
            return LoginResponse.failure("Authentication failed");
        }
    }
}
```

## 🔒 보안 고려사항

### 1. 토큰 보안
- 토큰은 toString()에서 자동으로 마스킹됨
- 로그에 토큰이 노출되지 않도록 주의
- HTTPS 환경에서만 토큰 전송

### 2. 예외 처리
- TokenProviderException을 통한 구체적인 오류 정보 제공
- 민감한 정보가 예외 메시지에 포함되지 않도록 주의
- 사용자에게는 일반적인 오류 메시지만 노출

### 3. 설정 검증
- 프로덕션 환경에서 기본 Secret 사용 금지
- HTTPS 강제 검증 (Keycloak 연동 시)
- 토큰 만료시간 관계 검증

이 API 문서는 security-starter의 모든 Public 인터페이스를 포함하며, 헥사고날 아키텍처 원칙을 준수하여 구현체는 숨기고 계약만 공개합니다. 개발자는 이 API를 통해 안전하고 확장 가능한 보안 기능을 구현할 수 있습니다.