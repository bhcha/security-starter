# Hexacore Security Library 통합 가이드

## 📋 개요

이 문서는 security-starter 라이브러리를 Spring Boot 프로젝트(예: hexa-hr)에 통합하는 방법을 안내합니다.

## 🚀 빠른 시작

### 1. 의존성 추가

`build.gradle`에 다음 의존성을 추가하세요:

```gradle
dependencies {
    implementation 'com.dx:security-starter:1.0.0'
    
    // 필수 의존성 (Spring Boot Starter가 자동으로 포함하지 않는 경우)
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-cache'
    implementation 'com.github.ben-manes.caffeine:caffeine'
}
```

### 2. 기본 설정

`application.yml` 또는 `application.properties`에 다음 설정을 추가하세요:

```yaml
# application.yml
hexacore:
  security:
    enabled: true
    token-provider:
      provider: jwt  # 또는 keycloak
      jwt:
        enabled: true
        secret-key: "your-secret-key-here"
        issuer: "your-app-name"
        audience: 
          - "your-app-name"
        access-token-expire-seconds: 3600
        refresh-token-expire-seconds: 604800
    persistence:
      jpa:
        enabled: true
    cache:
      type: caffeine
      caffeine:
        enabled: true

spring:
  datasource:
    url: jdbc:h2:mem:testdb  # 또는 실제 데이터베이스 URL
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  jpa:
    hibernate:
      ddl-auto: update  # 운영환경에서는 validate 사용 권장
    show-sql: false
```

### 3. 메인 애플리케이션 클래스

Spring Boot 메인 클래스에서 캐싱을 활성화하세요:

```java
@SpringBootApplication
@EnableCaching  // 캐시 기능 활성화
public class HexaHrApplication {
    public static void main(String[] args) {
        SpringApplication.run(HexaHrApplication.class, args);
    }
}
```

## 🔧 API 사용법

### 인증 서비스 사용

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthenticationUseCase authenticationUseCase;
    private final TokenManagementUseCase tokenManagementUseCase;
    
    public AuthController(AuthenticationUseCase authenticationUseCase,
                         TokenManagementUseCase tokenManagementUseCase) {
        this.authenticationUseCase = authenticationUseCase;
        this.tokenManagementUseCase = tokenManagementUseCase;
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthenticateCommand command = new AuthenticateCommand(
                request.getUsername(), 
                request.getPassword()
            );
            
            AuthenticationResult result = authenticationUseCase.authenticate(command);
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "username", result.getUsername(),
                    "token", result.getToken().orElse(null)
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", result.getFailureReason().orElse("Authentication failed")
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Internal server error: " + e.getMessage()
            ));
        }
    }
    
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestBody TokenRequest request) {
        try {
            ValidateTokenCommand command = new ValidateTokenCommand(request.getToken());
            TokenValidationResult result = tokenManagementUseCase.validateToken(command);
            
            if (result.isValid()) {
                return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "token", result.getAccessToken(),
                    "message", "Token is valid"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "message", result.getInvalidReason().orElse("Token is invalid")
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "valid", false,
                "message", "Internal server error: " + e.getMessage()
            ));
        }
    }
}
```

### DTO 클래스

```java
// LoginRequest.java
public class LoginRequest {
    private String username;
    private String password;
    
    // getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

// TokenRequest.java
public class TokenRequest {
    private String token;
    
    // getters and setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
```

## 🔐 보안 설정 (선택사항)

기본 Spring Security와 함께 사용하려면:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()  // 개발환경에서만
                .anyRequest().authenticated()
            )
            .headers(headers -> headers.frameOptions().sameOrigin()); // H2 console용

        return http.build();
    }
}
```

## ⚙️ 고급 설정

### Keycloak 사용

Keycloak을 토큰 공급자로 사용하려면:

```yaml
hexacore:
  security:
    token-provider:
      provider: keycloak
      keycloak:
        enabled: true
        server-url: "http://localhost:8080"
        realm: "your-realm"
        client-id: "your-client-id"
        client-secret: "your-client-secret"
```

### 캐시 설정

#### Caffeine 캐시 (권장)
```yaml
hexacore:
  security:
    cache:
      type: caffeine
      caffeine:
        enabled: true
        max-size: 1000
        expire-after-write: 300  # 5분
```

#### Redis 캐시
```yaml
hexacore:
  security:
    cache:
      type: redis
      redis:
        enabled: true
        host: localhost
        port: 6379
        timeout: 2000
```

### 데이터베이스 설정

#### MySQL
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/hexahr
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: your-username
    password: your-password
  jpa:
    database-platform: org.hibernate.dialect.MySQL8Dialect
```

#### PostgreSQL
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/hexahr
    driver-class-name: org.postgresql.Driver
    username: your-username
    password: your-password
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
```

## 🧪 테스트

### 애플리케이션 시작 후 테스트

#### 1. 로그인 테스트
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "testpass"
  }'
```

#### 2. 토큰 검증 테스트
```bash
curl -X POST http://localhost:8080/api/auth/validate \
  -H "Content-Type: application/json" \
  -d '{
    "token": "your-jwt-token-here"
  }'
```

#### 3. Health Check
```bash
curl -X GET http://localhost:8080/actuator/health
```

## 📊 데이터베이스 스키마

라이브러리가 자동으로 생성하는 테이블들:

- `authentication`: 인증 정보
- `authentication_sessions`: 사용자 세션 정보  
- `authentication_attempts`: 로그인 시도 이력

## 🚨 문제 해결

### 자주 발생하는 문제들

#### 1. Bean을 찾을 수 없음
```
No qualifying bean of type 'AuthenticationUseCase'
```
**해결방법**: `@EnableCaching` 어노테이션을 메인 클래스에 추가하세요.

#### 2. JPA 엔티티를 찾을 수 없음
```
Not a managed type: class com.dx.hexacore.security...
```
**해결방법**: 라이브러리가 최신 버전인지 확인하고, JPA 설정이 올바른지 확인하세요.

#### 3. 토큰 발급 실패
**해결방법**: 
- JWT secret-key가 설정되어 있는지 확인
- Keycloak 사용 시 연결 정보가 올바른지 확인

## 📝 로그 설정

디버깅을 위한 로그 레벨 설정:

```yaml
logging:
  level:
    com.dx.hexacore.security: DEBUG
    org.springframework.security: DEBUG
```

## 🔄 마이그레이션

기존 인증 시스템에서 마이그레이션하는 경우:

1. 기존 인증 로직을 점진적으로 교체
2. 데이터 마이그레이션 스크립트 작성
3. 단계별 배포 계획 수립

## 📞 지원

문제가 발생하면:
1. 이 가이드의 문제 해결 섹션 확인
2. 로그를 DEBUG 레벨로 설정하여 자세한 정보 확인
3. GitHub Issues에 문제 보고

---

**참고**: 이 라이브러리는 헥사고날 아키텍처를 따르며, DDD 패턴을 적용하여 구현되었습니다.