# Hexacore Security Library

Spring Boot 애플리케이션을 위한 헥사고날 아키텍처 기반 **통합 보안 라이브러리**입니다.
인증, 세션 관리, JWT 토큰 처리를 Port 인터페이스로 제공하며, **Spring Boot Auto-Configuration**이 내장되어 있습니다.

> 🔄 **migration notice**: 기존 `security-auth-starter` 사용자는 [마이그레이션 가이드](MIGRATION.md)를 참고하세요.

## 🚀 최신 업데이트 (v1.0.0)

### 통합 라이브러리 완성 ✅
- **단일 의존성**: `security-auth-starter` 기능 완전 통합
- **Auto-Configuration 내장**: 별도 Starter 불필요
- **간소화된 설정**: `hexacore.security.*` prefix로 통합

### TokenProvider 아키텍처 개선 ✅
- **다중 Provider 지원**: Keycloak과 Spring JWT를 설정으로 선택 가능
- **플러그인 방식**: 새로운 TokenProvider 구현체 추가 용이
- **자동 설정**: Spring Boot Auto Configuration으로 자동 구성

### 통합 가이드 문서 완성 ✅
- **[빠른 시작 가이드](QUICK_START.md)**: 3단계로 바로 적용
- **[상세 통합 가이드](INTEGRATION_GUIDE.md)**: 모든 설정 옵션과 사용법
- **[예시 파일들](examples/)**: hexa-hr 프로젝트용 설정 파일들

### 새로운 통합 설정 방식
```yaml
hexacore:
  security:
    enabled: true
    token:
      provider: keycloak  # 또는 'jwt' 선택
      keycloak:
        enabled: true
        server-url: https://keycloak.example.com
        realm: my-realm
        client-id: my-client
        client-secret: my-secret
      jwt:
        enabled: false
        secret: your-256-bit-secret-key
        access-token-expiration: PT1H
        refresh-token-expiration: P7D
    session:
      max-failed-attempts: 5
      lockout-duration: PT30M
    filter:
      enabled: true
      exclude-urls:
        - "/actuator/**"
        - "/public/**"
```

## 🏗️ 아키텍처

헥사고날 아키텍처(Ports and Adapters) 패턴을 적용하여 비즈니스 로직과 외부 의존성을 분리했습니다.

```
┌─────────────────────────────────────────────────────────────┐
│                    Application Layer                        │
│  ┌─────────────────────┐    ┌─────────────────────────────┐ │
│  │   Inbound Ports     │    │      Outbound Ports         │ │
│  │  (Use Cases)        │    │   (Repository, Gateway)     │ │
│  └─────────────────────┘    └─────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
              │                              │
    ┌─────────▼─────────┐            ┌──────▼──────┐
    │  Inbound Adapters │            │ Outbound    │
    │  (REST, Events)   │            │ Adapters    │
    └───────────────────┘            │ (JPA, HTTP) │
                                     └─────────────┘
```

## 📦 설치

### Maven
```xml
<dependency>
    <groupId>com.dx</groupId>
    <artifactId>security-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle
```gradle
implementation 'com.dx:security-starter:1.0.0'
```

## 🚀 시작하기

### 1. 자동 설정

라이브러리를 추가하면 자동으로 설정됩니다:

```java
@SpringBootApplication
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
    // 자동으로 Hexacore Security가 활성화됩니다
}
```

### 2. 설정

`application.yml`에서 설정을 커스터마이징할 수 있습니다:

```yaml
hexacore:
  security:
    enabled: true
    token:
      provider: keycloak
      keycloak:
        enabled: true
        server-url: http://localhost:8080
        realm: my-realm
        client-id: my-client
        client-secret: your-secret
    session:
      max-failed-attempts: 5
      lockout-duration: PT30M
```

> 📝 **참고**: 기존 `security.auth.*` 설정을 사용 중이라면 [마이그레이션 가이드](MIGRATION.md)를 참고하세요.

## 🔌 Port 인터페이스

라이브러리는 다음 Port 인터페이스들을 제공합니다:

### 인증 관련

#### Inbound Ports (Use Cases)
```java
@Autowired
private AuthenticationUseCase authenticationUseCase;

@Autowired 
private TokenManagementUseCase tokenManagementUseCase;

@Autowired
private GetAuthenticationUseCase getAuthenticationUseCase;
```

#### Outbound Ports (구현 가능)
```java
public interface TokenProvider {
    Token issueToken(Credentials credentials);
    TokenValidationResult validateToken(String accessToken);
    Token refreshToken(String refreshToken);
    TokenProviderType getProviderType();
}

public interface AuthenticationRepository {
    Authentication save(Authentication authentication);
    Optional<Authentication> findById(String id);
    Optional<Authentication> findByAccessToken(String accessToken);
```

### 세션 관리 관련

#### Inbound Ports (Use Cases)
```java
@Autowired
private CheckLockoutUseCase checkLockoutUseCase;

@Autowired
private RecordAttemptUseCase recordAttemptUseCase;

@Autowired
private UnlockAccountUseCase unlockAccountUseCase;

// 추가 조회 기능
@Autowired
private GetSessionStatusUseCase getSessionStatusUseCase;

@Autowired
private GetFailedAttemptsUseCase getFailedAttemptsUseCase;
```

#### Outbound Ports (구현 가능)
```java
public interface AuthenticationSessionRepository {
    AuthenticationSession save(AuthenticationSession session);
    Optional<AuthenticationSession> findBySessionId(SessionId sessionId);
    void delete(SessionId sessionId);
}

public interface LoadSessionStatusQueryPort {
    Optional<SessionStatusProjection> loadSessionStatus(String sessionId);
    Optional<SessionStatusProjection> loadSessionStatusByUser(String sessionId, String userId);
}

public interface LoadFailedAttemptsQueryPort {
    List<FailedAttemptProjection> loadFailedAttempts(String sessionId, 
                                                    LocalDateTime from, LocalDateTime to, int limit);
    int countFailedAttempts(String sessionId, LocalDateTime from, LocalDateTime to);
}

public interface SessionEventPublisher {
    void publish(DomainEvent event);
}
```

## 💡 사용 예제

### 기본 인증 (로그인)

```java
@RestController
public class AuthController {
    
    @Autowired
    private AuthenticationUseCase authenticationUseCase;
    
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResult> login(@RequestBody LoginRequest request) {
        AuthenticateCommand command = AuthenticateCommand.builder()
            .username(request.getUsername())
            .password(request.getPassword())
            .build();
            
        AuthenticationResult result = authenticationUseCase.authenticate(command);
        return ResponseEntity.ok(result);
    }
}
```

### Bearer 토큰 인증 (자동 처리)

라이브러리는 **JWT Bearer 토큰 인증을 자동으로 처리**합니다:

```http
# 토큰 발급 후 API 호출
GET /api/protected-endpoint
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### JWT 필터 자동 설정

```java
// 자동으로 다음 기능들이 활성화됩니다:
// 1. Authorization 헤더에서 Bearer 토큰 추출
// 2. JWT 토큰 검증
// 3. Spring Security Context에 인증 정보 설정
// 4. 인증 실패 시 401 Unauthorized 응답

@RestController
public class ProtectedController {
    
    @GetMapping("/api/profile")
    public ResponseEntity<UserProfile> getProfile() {
        // Bearer 토큰이 유효하면 자동으로 인증됨
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String token = ((JwtAuthenticationToken) auth).getToken();
        
        return ResponseEntity.ok(userService.getProfile(token));
    }
}
```

#### JWT 설정

```yaml
hexacore:
  security:
    token:
      provider: jwt
      jwt:
        enabled: true
        secret: your-jwt-secret-key-at-least-256-bits-long
        access-token-expiration: PT1H  # 1시간
        refresh-token-expiration: P7D  # 7일
    filter:
      enabled: true
      exclude-urls:  # 인증이 필요없는 URL 패턴
        - "/api/public/**"
        - "/login"
        - "/actuator/health"
```

### 토큰 관리

```java
@RestController
public class TokenController {
    
    @Autowired
    private TokenManagementUseCase tokenManagementUseCase;
    
    @PostMapping("/token/refresh")
    public ResponseEntity<TokenValidationResult> refreshToken(@RequestBody RefreshTokenRequest request) {
        RefreshTokenCommand command = RefreshTokenCommand.builder()
            .refreshToken(request.getRefreshToken())
            .build();
            
        TokenValidationResult result = tokenManagementUseCase.refreshToken(command);
        return ResponseEntity.ok(result);
    }
}
```

### 세션 관리 (위험도 평가 포함)

```java
@Service
public class SecurityService {
    
    @Autowired
    private CheckLockoutUseCase checkLockoutUseCase;
    
    @Autowired
    private RecordAttemptUseCase recordAttemptUseCase;
    
    @Autowired
    private GetSessionStatusUseCase getSessionStatusUseCase;
    
    public boolean attemptLogin(String userId, String clientIp) {
        // 계정 잠금 상태 확인
        LockoutCheckResult lockoutResult = checkLockoutUseCase.checkLockout(userId);
        if (lockoutResult.isLocked()) {
            return false;
        }
        
        // 위험도 평가 후 로그인 시도 기록
        RecordAuthenticationAttemptCommand command = RecordAuthenticationAttemptCommand.builder()
            .userId(userId)
            .successful(false)
            .clientIp(clientIp)
            .riskScore(calculateRiskScore(clientIp)) // 위험도 평가
            .riskReason(getRiskReason(clientIp))
            .build();
            
        recordAttemptUseCase.execute(command);
        return true;
    }
    
    public SessionStatusResponse getSessionStatus(String sessionId) {
        GetSessionStatusQuery query = new GetSessionStatusQuery(sessionId, null);
        return getSessionStatusUseCase.execute(query);
    }
    
    private int calculateRiskScore(String clientIp) {
        // IP 기반 위험도 계산 로직
        if (isVpnOrProxy(clientIp)) return 75;
        if (isUnknownLocation(clientIp)) return 50;
        return 25; // 정상
    }
}
```

### 도메인 이벤트 구독

라이브러리에서 발행하는 도메인 이벤트를 구독할 수 있습니다:

```java
@Component
public class SecurityEventHandler {
    
    @EventListener
    public void handleAuthenticationSucceeded(AuthenticationSucceeded event) {
        log.info("사용자 인증 성공: {} at {}", event.aggregateId(), event.successTime());
    }
    
    @EventListener
    public void handleAccountLocked(AccountLocked event) {
        // 계정 잠김 알림 발송
        notificationService.sendAlert(
            event.userId(), 
            "계정이 잠겼습니다. 해제 시간: " + event.lockedUntil()
        );
    }
    
    @EventListener
    public void handleAuthenticationFailed(AuthenticationFailed event) {
        // 실패 로그 기록
        securityLogger.logFailedAttempt(event.aggregateId(), event.reason());
    }
}
```

## 🔧 고급 설정

### 커스텀 구현체 제공

원하는 경우 자체 구현체를 제공할 수 있습니다:

```java
@Configuration
public class CustomSecurityConfig {
    
    @Bean
    @Primary
    public TokenProvider customTokenProvider() {
        return new MyCustomTokenProvider();
    }
    
    @Bean
    @Primary  
    public AuthenticationRepository customAuthRepository() {
        return new MyCustomAuthRepository();
    }
}
```

### 이벤트 구독

도메인 이벤트를 구독할 수 있습니다:

```java
@Component
public class SecurityEventListener {
    
    @EventListener
    public void handleAuthenticationSuccess(AuthenticationSucceeded event) {
        log.info("Authentication succeeded for user: {}", event.getCredentials().getUsername());
    }
    
    @EventListener
    public void handleAccountLocked(AccountLocked event) {
        log.warn("Account locked: {}", event.getUserId());
    }
}
```

## 🎯 도메인 모델

### 인증 애그리거트 (Authentication)

```java
// Value Objects
public class Credentials {
    public static Credentials of(String username, String password);
    public String getUsername();
    // password는 보안상 getter 제공하지 않음
}

public class Token {
    public static Token of(String accessToken, String refreshToken, Long expiresIn);
    public String getAccessToken();
    public String getRefreshToken();
    public boolean isExpired();
    public LocalDateTime getExpiresAt();
}

// Aggregate Root
public class Authentication {
    public static Authentication attemptAuthentication(Credentials credentials);
    public void markAsSuccessful(Token token);
    public void markAsFailed(String reason);
    public void refreshToken(Token newToken);
    public void terminate();
    public boolean isActive();
    public AuthenticationStatus getStatus();
}

// 도메인 이벤트
public record AuthenticationAttempted(UUID aggregateId, String username, LocalDateTime attemptTime);
public record AuthenticationSucceeded(UUID aggregateId, Token token, LocalDateTime successTime);
public record AuthenticationFailed(UUID aggregateId, String reason, LocalDateTime failTime);
```

### 세션 애그리거트 (AuthenticationSession)

```java
// Value Objects  
public class ClientIp {
    public static ClientIp of(String ipAddress);
    public String getIpAddress();
    public boolean isLocalhost();
    public IpType getType();
}

public class SessionId {
    public static SessionId generate();
    public static SessionId of(UUID value);
    public UUID getValue();
}

public class RiskLevel {
    public static RiskLevel of(int score, String reason);
    public static RiskLevel high(String reason);
    public int getScore();
    public RiskCategory getCategory();
    public boolean isCritical();
}

// Aggregate Root
public class AuthenticationSession {
    public static AuthenticationSession create(SessionId sessionId, String userId, ClientIp clientIp);
    public void recordAttempt(String userId, ClientIp clientIp, boolean isSuccessful, RiskLevel riskLevel);
    public void lockAccount();
    public void unlockAccount();
    public boolean shouldLockAccount();
    public boolean isCurrentlyLocked();
    public int getFailedAttemptsInWindow();
}

// 도메인 이벤트
public record AccountLocked(String sessionId, String userId, String clientIp, 
                          LocalDateTime lockedUntil, int failedAttempts, LocalDateTime occurredAt);
```

## 🚦 제공되는 기능

### 🔐 인증 관리 (Authentication Aggregate)
- ✅ **JWT Bearer 토큰 인증** (자동 필터 처리)
- ✅ JWT 토큰 발급/갱신/검증
- ✅ Keycloak 연동 (OAuth 2.0)
- ✅ Spring JWT Provider (자체 JWT 발급)
- ✅ 다중 TokenProvider 지원 (설정 기반 선택)
- ✅ Spring Security 통합
- ✅ 토큰 만료 처리
- ✅ 외부 인증 제공자 추상화

### 🛡️ 세션 관리 (AuthenticationSession Aggregate)  
- ✅ 계정 잠금/해제 (정책 기반)
- ✅ 로그인 시도 기록 및 추적
- ✅ IP 기반 접근 제어
- ✅ 위험도 평가 시스템
- ✅ 시간 윈도우 기반 실패 감지
- ✅ 자동 계정 잠금 해제

### 🏗️ 아키텍처 & 인프라
- ✅ 헥사고날 아키텍처 (Port/Adapter)
- ✅ 도메인 이벤트 발행/구독
- ✅ 캐시 지원 (Redis/Caffeine/Simple)
- ✅ 비동기 이벤트 처리
- ✅ JPA 영속성 관리
- ✅ Spring Boot Auto Configuration

### 📊 모니터링 & 보안
- ✅ 인증 시도 추적 및 감사
- ✅ 무차별 대입 공격 방어
- ✅ 클라이언트 IP 추적
- ✅ 커스터마이징 가능한 보안 정책
- ✅ 실시간 계정 상태 모니터링

## 📋 요구사항

- Java 21+
- Spring Boot 3.5.4+
- Spring Security 6+
- Spring Data JPA 3+

### 선택적 의존성
- Keycloak (Keycloak Provider 사용 시)
- JJWT 0.11.5+ (Spring JWT Provider 사용 시)
- Caffeine Cache (로컬 캐싱 사용 시)

## 🤝 라이선스

MIT License

## 📊 품질 현황

- **테스트 커버리지**: 85%+
- **테스트 성공률**: 98% (813개 중 797개 성공)
- **아키텍처 준수도**: 100%
- **코드 품질 평가**: A+ (92.4/100)

## 🔄 버전 히스토리

### v1.0.0 (2025-01-30)
- Authentication 애그리거트 구현 완료
- AuthenticationSession 애그리거트 구현 완료
- TokenProvider 아키텍처 개선
- Keycloak/Spring JWT 다중 Provider 지원
- 헥사고날 아키텍처 100% 준수

## 🔧 프로젝트 통합 가이드

### hexa-hr 프로젝트 적용

hexa-hr 프로젝트에 security-starter를 통합하려면:

1. **[빠른 시작 가이드](QUICK_START.md)** - 3단계로 바로 적용
2. **[상세 통합 가이드](INTEGRATION_GUIDE.md)** - 모든 설정 옵션과 사용법
3. **[예시 파일들](examples/)** - 바로 사용할 수 있는 설정 파일들

### 3단계 빠른 적용

```bash
# 1. 의존성 추가 (build.gradle)
implementation 'com.dx:security-starter:1.0.0'

# 2. 설정 추가 (application.yml)
hexacore.security.enabled: true

# 3. 메인 클래스에서 캐싱 활성화
@EnableCaching
```

**더 자세한 내용은 [INTEGRATION_GUIDE.md](INTEGRATION_GUIDE.md)를 참고하세요.**

## 📚 더 자세한 정보

- 🚀 **[빠른 시작 가이드](QUICK_START.md)** - 3분만에 적용하기
- 🔧 **[통합 가이드](INTEGRATION_GUIDE.md)** - 상세한 적용 방법
- 📁 **[예시 파일들](examples/)** - hexa-hr용 설정 파일들
- 🔄 [마이그레이션 가이드](MIGRATION.md) - security-auth-starter에서 마이그레이션
- 📋 [구현 계획서](docs/plan/implementation-plan.md)
- 📊 [품질 평가 보고서](docs/quality-assessment-report.md)
- 📖 [API JavaDoc](https://javadoc.io/doc/com.dx/security-starter)
- 🐙 [GitHub Repository](https://github.com/dx/security-starter)