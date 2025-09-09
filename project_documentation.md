# Security Starter 프로젝트 상세 문서

## 1. 프로젝트 개요

Security-Starter는 Spring Boot 애플리케이션을 위한 엔터프라이즈급 보안 라이브러리입니다. DDD(Domain-Driven Design) 헥사고날 아키텍처를 기반으로 하여 확장 가능하고 유지보수하기 쉬운 보안 솔루션을 제공합니다.

### 주요 특징

#### 🔐 인증 및 권한 부여
- **JWT/Keycloak 지원**: 유연한 토큰 기반 인증 with 다중 프로바이더
- **자동 토큰 검증**: Spring Security Filter 자동 등록
- **토큰 관리**: 액세스/리프레시 토큰 자동 관리

#### 🛡️ 세션 관리
- **지능형 계정 잠금**: 실패 시도 추적 및 자동 잠금
- **보안 정책 설정**: 최대 시도 횟수, 잠금 시간, 시도 윈도우 설정
- **실시간 모니터링**: 인증 시도 및 보안 이벤트 추적

#### 🏗️ 아키텍처
- **DDD 헥사고날 아키텍처**: 비즈니스 로직과 기술 구현 분리
- **CQRS 패턴**: Command와 Query 분리로 확장성 향상
- **이벤트 기반**: 도메인 이벤트를 통한 느슨한 결합

#### 🔧 개발자 경험
- **자동 설정**: Spring Boot Auto Configuration
- **설정 검증**: 시작 시 설정 자동 검증 및 친화적 에러 메시지
- **타입 안전성**: 완전한 Java 17+ 및 Spring Boot 3.5+ 지원

## 2. 프로젝트 구조

### 패키지 구조

```
com.dx.hexacore.security
├── auth                          # 인증 관련 기능
│   ├── adapter                   # 외부 시스템과의 통신 담당
│   │   ├── inbound               # 외부에서 들어오는 요청 처리
│   │   │   ├── config            # 인바운드 어댑터 설정
│   │   │   ├── event             # 이벤트 리스너
│   │   │   └── filter            # 보안 필터
│   │   └── outbound              # 외부 시스템으로 나가는 요청 처리
│   ├── application               # 애플리케이션 서비스
│   │   ├── command               # 명령 처리
│   │   │   ├── handler           # 명령 핸들러
│   │   │   └── port              # 포트 인터페이스
│   │   │       ├── in            # 인바운드 포트
│   │   │       └── out           # 아웃바운드 포트
│   │   ├── exception             # 예외 처리
│   │   ├── projection            # 프로젝션
│   │   └── query                 # 쿼리 처리
│   └── domain                    # 도메인 모델
│       └── vo                    # 값 객체
├── session                       # 세션 관리 기능
│   ├── adapter                   # 세션 어댑터
│   ├── application               # 세션 애플리케이션 서비스
│   │   ├── command               # 세션 명령
│   │   ├── exception             # 세션 예외
│   │   ├── projection            # 세션 프로젝션
│   │   └── query                 # 세션 쿼리
│   └── domain                    # 세션 도메인 모델
└── config                        # 설정
    ├── autoconfigure             # 자동 설정
    ├── condition                 # 조건부 설정
    ├── properties                # 속성 클래스
    └── support                   # 지원 클래스
```

### 핵심 컴포넌트

#### 인증(Auth) 컴포넌트
- **TokenProvider**: 토큰 생성 및 검증을 담당하는 인터페이스
- **AuthenticationUseCase**: 인증 프로세스를 처리하는 유스케이스
- **JwtAuthenticationFilter**: JWT 토큰을 검증하고 인증 정보를 설정하는 필터
- **SecurityFilterConfig**: 보안 필터 체인을 구성하는 설정 클래스

#### 세션(Session) 컴포넌트
- **SessionManagementUseCase**: 세션 관리를 담당하는 유스케이스
- **SessionRepository**: 세션 정보를 저장하고 조회하는 인터페이스

#### 설정(Config) 컴포넌트
- **HexacoreSecurityAutoConfiguration**: 보안 라이브러리의 자동 설정을 담당하는 클래스
- **TokenProviderAutoConfiguration**: 토큰 제공자의 자동 설정을 담당하는 클래스

## 3. 헥사고날 아키텍처

Security Starter는 헥사고날 아키텍처(포트 및 어댑터 아키텍처)를 기반으로 설계되었습니다. 이 아키텍처는 애플리케이션의 핵심 비즈니스 로직을 외부 시스템과 분리하여 유지보수성과 테스트 용이성을 향상시킵니다.

### 도메인 레이어
- 비즈니스 로직의 핵심을 담당
- 외부 의존성 없이 순수한 도메인 모델 구현
- 예: `Authentication`, `Credentials`, `Token` 등의 도메인 객체

### 애플리케이션 레이어
- 도메인 레이어와 외부 어댑터 사이의 중재자 역할
- 유스케이스 구현 및 포트 인터페이스 정의
- 예: `AuthenticationUseCase`, `TokenProvider` 인터페이스

### 어댑터 레이어
- 외부 시스템과의 통신을 담당
- 인바운드 어댑터: 외부에서 들어오는 요청 처리 (예: REST 컨트롤러, 필터)
- 아웃바운드 어댑터: 외부 시스템으로 나가는 요청 처리 (예: 데이터베이스, 외부 API)

## 4. 주요 기능 흐름

### 인증 프로세스 흐름

1. **인증 시도**:
   - 클라이언트가 사용자 이름과 비밀번호로 인증 요청
   - `AuthenticateCommand` 객체 생성
   - `AuthenticationUseCase.authenticate()` 메서드 호출

2. **토큰 발급**:
   - `TokenProvider.issueToken()` 메서드를 통해 토큰 발급
   - 성공 시 `Token` 객체 반환, 실패 시 `TokenProviderException` 발생

3. **인증 결과 처리**:
   - 성공 시 `Authentication` 객체에 토큰 저장 및 성공 이벤트 발행
   - 실패 시 실패 이벤트 발행 및 실패 정보 저장

4. **결과 반환**:
   - `AuthenticationResult` 객체를 통해 인증 결과 반환

### JWT 인증 필터 흐름

1. **토큰 추출**:
   - HTTP 요청 헤더에서 Bearer 토큰 추출
   - `JwtAuthenticationFilter.extractToken()` 메서드 사용

2. **토큰 검증**:
   - `TokenProvider.validateToken()` 메서드를 통해 토큰 검증
   - 유효한 토큰인지 확인 및 클레임 추출

3. **인증 정보 설정**:
   - 유효한 토큰이면 `SecurityContextHolder`에 인증 정보 설정
   - `JwtAuthenticationToken` 객체 생성 및 권한 설정

4. **예외 처리**:
   - 토큰이 유효하지 않으면 `JwtAuthenticationException` 발생
   - `JwtAuthenticationEntryPoint`에서 인증 실패 응답 생성

## 5. 텍스트 플로우 다이어그램

### 인증 프로세스 다이어그램

```
클라이언트                  AuthenticationUseCase              TokenProvider                  EventPublisher
   |                               |                               |                               |
   | --- 인증 요청 ----------------->|                               |                               |
   |                               |                               |                               |
   |                               | --- 인증 시도 이벤트 발행 --------|-------------------------------->|
   |                               |                               |                               |
   |                               | --- 토큰 발급 요청 ------------->|                               |
   |                               |                               |                               |
   |                               |<-- 토큰 발급 결과 --------------|                               |
   |                               |                               |                               |
   |                               | --- 인증 결과 이벤트 발행 --------|-------------------------------->|
   |                               |                               |                               |
   |<-- 인증 결과 반환 --------------|                               |                               |
```

### JWT 인증 필터 다이어그램

```
HTTP 요청                JwtAuthenticationFilter             TokenProvider              SecurityContextHolder
   |                               |                               |                               |
   | --- 요청 헤더 전달 ------------->|                               |                               |
   |                               |                               |                               |
   |                               | --- 토큰 추출 ---------------->|                               |
   |                               |                               |                               |
   |                               | --- 토큰 검증 요청 ------------->|                               |
   |                               |                               |                               |
   |                               |<-- 토큰 검증 결과 --------------|                               |
   |                               |                               |                               |
   |                               | --- 인증 정보 설정 --------------|-------------------------------->|
   |                               |                               |                               |
   |<-- 필터 체인 계속 ---------------|                               |                               |
```

### 보안 필터 체인 다이어그램

```
HTTP 요청                SecurityFilterChain                JwtAuthenticationFilter        UsernamePasswordAuthFilter
   |                               |                               |                               |
   | --- 요청 전달 ------------------>|                               |                               |
   |                               |                               |                               |
   |                               | --- 요청 전달 ------------------>|                               |
   |                               |                               |                               |
   |                               |<-- 처리 완료 -------------------|                               |
   |                               |                               |                               |
   |                               | --- 요청 전달 ------------------------------------------->|
   |                               |                               |                               |
   |                               |<-- 처리 완료 --------------------------------------------|
   |                               |                               |                               |
   |<-- 응답 반환 -------------------|                               |                               |
```

## 6. 설정 방법 및 사용 예제

### 기본 설정

```yaml
hexacore:
  security:
    enabled: true                    # 보안 기능 활성화 여부
    token-provider:
      provider: jwt                  # 토큰 제공자 유형 (jwt 또는 keycloak)
      jwt:
        secret: ${JWT_SECRET:your-secret-key}  # JWT 서명 비밀키 (환경변수 또는 기본값)
        expiration: 3600             # 액세스 토큰 만료 시간(초) - 1시간
        refresh-expiration: 86400    # 리프레시 토큰 만료 시간(초) - 24시간
        issuer: "hexacore-security"  # 토큰 발급자
        audience: "web-client"       # 토큰 대상자
      keycloak:                      # Keycloak 설정 (provider가 keycloak일 경우)
        server-url: ${KEYCLOAK_URL:http://localhost:8080}
        realm: ${KEYCLOAK_REALM:master}
        client-id: ${KEYCLOAK_CLIENT_ID:admin-cli}
        client-secret: ${KEYCLOAK_CLIENT_SECRET:your-client-secret}
    session:
      enabled: true                  # 세션 관리 기능 활성화 여부
      lockout:
        enabled: true                # 계정 잠금 기능 활성화 여부
        max-attempts: 5              # 최대 실패 시도 횟수
        lockout-duration-minutes: 30 # 계정 잠금 시간(분)
        attempt-window-minutes: 10   # 실패 시도 윈도우 시간(분)
      storage:
        type: in-memory              # 세션 저장소 유형 (in-memory 또는 jpa)
        cleanup-interval-seconds: 300 # 만료된 세션 정리 간격(초)
    filter:
      enabled: true                  # 보안 필터 활성화 여부
      excludePaths:                  # 인증이 필요 없는 경로 패턴
        - "/api/auth/**"             # 인증 API 경로
        - "/actuator/health"         # 헬스 체크 엔드포인트
        - "/swagger-ui/**"           # API 문서 경로
        - "/v3/api-docs/**"          # OpenAPI 스펙 경로
    authentication:
      default-role: "ROLE_USER"      # 기본 권한
      error-response:
        include-timestamp: true      # 에러 응답에 타임스탬프 포함 여부
        include-status: true         # 에러 응답에 상태 코드 포함 여부
        default-message: "인증에 실패했습니다" # 기본 에러 메시지
```

### 설정 옵션 상세 설명

#### 토큰 제공자 설정

- **provider**: 사용할 토큰 제공자 유형을 지정합니다.
  - `jwt`: 내장된 JWT 토큰 제공자를 사용합니다.
  - `keycloak`: Keycloak 서버를 통한 토큰 제공자를 사용합니다.

- **jwt**: JWT 토큰 제공자 설정
  - `secret`: 토큰 서명에 사용할 비밀키입니다. 환경 변수를 통해 설정하는 것이 권장됩니다.
  - `expiration`: 액세스 토큰의 만료 시간(초)입니다.
  - `refresh-expiration`: 리프레시 토큰의 만료 시간(초)입니다.
  - `issuer`: 토큰 발급자 정보입니다.
  - `audience`: 토큰 대상자 정보입니다.

- **keycloak**: Keycloak 토큰 제공자 설정
  - `server-url`: Keycloak 서버 URL입니다.
  - `realm`: 사용할 Keycloak 영역(realm)입니다.
  - `client-id`: Keycloak 클라이언트 ID입니다.
  - `client-secret`: Keycloak 클라이언트 시크릿입니다.

#### 세션 관리 설정

- **enabled**: 세션 관리 기능 활성화 여부입니다.

- **lockout**: 계정 잠금 정책 설정
  - `enabled`: 계정 잠금 기능 활성화 여부입니다.
  - `max-attempts`: 계정이 잠기기 전 허용되는 최대 실패 시도 횟수입니다.
  - `lockout-duration-minutes`: 계정이 잠기는 시간(분)입니다.
  - `attempt-window-minutes`: 실패 시도를 집계하는 시간 윈도우(분)입니다.

- **storage**: 세션 저장소 설정
  - `type`: 세션 정보 저장 방식입니다. (`in-memory` 또는 `jpa`)
  - `cleanup-interval-seconds`: 만료된 세션을 정리하는 간격(초)입니다.

#### 필터 설정

- **enabled**: 보안 필터 활성화 여부입니다.
- **excludePaths**: 인증이 필요 없는 URL 패턴 목록입니다. Ant 스타일 패턴을 지원합니다.

### 코드 사용 예제

#### 1. 인증 컨트롤러 구현

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationUseCase authenticationUseCase;
    private final SessionManagementUseCase sessionManagementUseCase;

    public AuthController(
        AuthenticationUseCase authenticationUseCase,
        SessionManagementUseCase sessionManagementUseCase
    ) {
        this.authenticationUseCase = authenticationUseCase;
        this.sessionManagementUseCase = sessionManagementUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResult> login(@RequestBody LoginRequest request) {
        var command = new AuthenticateCommand(request.username(), request.password());
        var result = authenticationUseCase.authenticate(command);

        if (result.isSuccess()) {
            return ResponseEntity.ok(
                new AuthResult(
                    true,
                    result.getUsername(),
                    result.getToken().getValue(),
                    result.getToken().getExpiresAt()
                )
            );
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AuthResult(false, result.getUsername(), null, null));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResult> refreshToken(@RequestBody RefreshTokenRequest request) {
        var command = new RefreshTokenCommand(request.getRefreshToken());
        var result = authenticationUseCase.refreshToken(command);

        if (result.isSuccess()) {
            return ResponseEntity.ok(
                new AuthResult(
                    true,
                    result.getUsername(),
                    result.getToken().getValue(),
                    result.getToken().getExpiresAt()
                )
            );
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AuthResult(false, null, null, null));
        }
    }

    @GetMapping("/session/status")
    public ResponseEntity<SessionStatusResponse> getSessionStatus(
            @RequestParam String username) {
        var query = new SessionStatusQuery(username);
        var result = sessionManagementUseCase.getSessionStatus(query);

        return ResponseEntity.ok(
            new SessionStatusResponse(
                result.isLocked(),
                result.getRemainingAttempts(),
                result.getLockExpirationTime()
            )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        // Bearer 접두사 제거
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        var command = new LogoutCommand(token);
        authenticationUseCase.logout(command);

        return ResponseEntity.noContent().build();
    }
}
```

#### 2. 보안 설정 커스터마이징

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain customFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers("/api/public/**").permitAll();
                auth.requestMatchers("/api/admin/**").hasRole("ADMIN");
                auth.requestMatchers("/api/user/**").hasRole("USER");
                auth.anyRequest().authenticated();
            })
            .exceptionHandling(exception -> 
                exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

#### 3. 세션 관리 서비스 활용

```java
@Service
public class UserAccountService {

    private final SessionManagementUseCase sessionManagementUseCase;
    private final UserRepository userRepository;

    public UserAccountService(
            SessionManagementUseCase sessionManagementUseCase,
            UserRepository userRepository) {
        this.sessionManagementUseCase = sessionManagementUseCase;
        this.userRepository = userRepository;
    }

    public void unlockAccount(String username) {
        // 사용자 계정 잠금 해제
        var command = new UnlockAccountCommand(username);
        sessionManagementUseCase.unlockAccount(command);

        // 추가적인 비즈니스 로직 수행
        userRepository.findByUsername(username)
            .ifPresent(user -> {
                user.setLastUnlockTime(LocalDateTime.now());
                userRepository.save(user);
            });
    }

    public AccountStatusDTO getAccountStatus(String username) {
        // 세션 상태 조회
        var sessionQuery = new SessionStatusQuery(username);
        var sessionStatus = sessionManagementUseCase.getSessionStatus(sessionQuery);

        // 로그인 시도 이력 조회
        var attemptsQuery = new LoginAttemptsQuery(username);
        var attempts = sessionManagementUseCase.getLoginAttempts(attemptsQuery);

        return new AccountStatusDTO(
            username,
            sessionStatus.isLocked(),
            sessionStatus.getRemainingAttempts(),
            sessionStatus.getLockExpirationTime(),
            attempts.getRecentAttempts()
        );
    }
}
```

#### 4. 토큰 검증 및 사용자 정보 추출

```java
@Component
public class SecurityUtils {

    private final TokenProvider tokenProvider;

    public SecurityUtils(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    /**
     * 현재 인증된 사용자의 사용자명을 반환합니다.
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }

        if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            String token = jwtAuth.getToken();

            TokenValidationResult result = tokenProvider.validateToken(token);
            if (result.valid() && result.claims() != null) {
                return (String) result.claims().get("sub");
            }
        }

        return null;
    }

    /**
     * 토큰에서 사용자 정보를 추출합니다.
     */
    public UserDetails extractUserDetails(String token) {
        TokenValidationResult result = tokenProvider.validateToken(token);

        if (!result.valid() || result.claims() == null) {
            throw new InvalidTokenException("Invalid or expired token");
        }

        String username = (String) result.claims().get("sub");
        List<String> roles = (List<String>) result.claims().get("roles");

        List<SimpleGrantedAuthority> authorities = roles.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

        return new User(username, "", authorities);
    }
}
```

## 7. 결론

Security Starter는 DDD 헥사고날 아키텍처를 기반으로 한 엔터프라이즈급 보안 라이브러리로, Spring Boot 애플리케이션에 쉽게 통합할 수 있습니다. JWT/Keycloak 인증, 세션 관리, 계정 잠금 등의 기능을 제공하며, 확장 가능하고 유지보수하기 쉬운 구조를 가지고 있습니다.

이 라이브러리를 사용하면 보안 관련 코드를 직접 작성할 필요 없이, 설정만으로 엔터프라이즈급 보안 기능을 애플리케이션에 쉽게 추가할 수 있습니다.
