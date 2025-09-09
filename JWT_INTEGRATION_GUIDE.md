# JWT 통합 가이드

## 개요

Hexacore Security Starter는 세 가지 JWT 통합 전략을 제공하여, 부모 프로젝트의 요구사항에 맞게 유연하게 JWT 인증을 적용할 수 있습니다.

## 통합 전략

### 1. Security Integration (기본값)
**특징**: 
- 부모 프로젝트가 SecurityFilterChain을 정의하지 않으면 자동으로 생성
- 부모 프로젝트가 SecurityFilterChain을 정의하면 자동으로 백오프
- JWT 필터를 Bean으로 제공하여 재사용 가능

**설정**:
```yaml
hexacore:
  security:
    jwt:
      enabled: true
      strategy: security-integration  # 기본값, 생략 가능
```

### 2. Servlet Filter 전략
**특징**:
- Spring Security와 완전히 독립적으로 동작
- ServletFilter로 JWT 처리
- 부모 프로젝트의 SecurityFilterChain과 충돌 없음

**설정**:
```yaml
hexacore:
  security:
    jwt:
      enabled: true
      strategy: servlet-filter
      filter-order: 50  # ServletFilter 우선순위
```

### 3. Manual 전략
**특징**:
- JWT 필터만 Bean으로 제공
- SecurityFilterChain 생성하지 않음
- 부모 프로젝트가 완전히 제어

**설정**:
```yaml
hexacore:
  security:
    jwt:
      enabled: true
      strategy: manual
```

## 사용 예시

### 예시 1: 기본 사용 (Zero Configuration)
```yaml
# application.yml
hexacore:
  security:
    tokenProvider:
      jwt:
        secret: "your-secret-key-at-least-32-characters-long"
        access-token-validity: 3600
```

JWT 인증이 자동으로 적용되며, 모든 요청에 JWT가 필요합니다.

### 예시 2: 부모 프로젝트에서 SecurityFilterChain 커스터마이징

**방법 1: JwtSecurityHelper 사용 (권장)**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtSecurityHelper jwtHelper) throws Exception {
        
        // JWT 설정 자동 적용
        jwtHelper.configureJwt(http);
        
        // JWT 제외 경로 적용
        jwtHelper.configureExcludePaths(http);
        
        // 추가 커스터마이징
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/public/**").permitAll()
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
            .anyRequest().authenticated()
        );
        
        return http.build();
    }
}
```

**방법 2: JwtAuthenticationFilter 직접 주입**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtFilter,
            JwtAuthenticationEntryPoint jwtEntryPoint) throws Exception {
        
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(exception -> 
                exception.authenticationEntryPoint(jwtEntryPoint))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

### 예시 3: ServletFilter 전략으로 완전 독립 실행
```yaml
# application.yml
hexacore:
  security:
    jwt:
      strategy: servlet-filter
      filter-order: 10  # Spring Security보다 먼저 실행
    tokenProvider:
      jwt:
        secret: "your-secret-key"
        excluded-paths:
          - /api/public/**
          - /health
```

부모 프로젝트:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // JWT는 ServletFilter에서 처리되므로
        // 여기서는 추가 보안 규칙만 정의
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().permitAll()
            );
        
        return http.build();
    }
}
```

### 예시 4: Manual 전략으로 완전 제어
```yaml
# application.yml
hexacore:
  security:
    jwt:
      strategy: manual
```

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtFilter;
    
    @Bean
    public SecurityFilterChain apiSecurityChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**")
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            );
        
        return http.build();
    }
    
    @Bean
    @Order(1)
    public SecurityFilterChain adminSecurityChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/admin/**")
            .httpBasic(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().hasRole("ADMIN")
            );
        
        return http.build();
    }
}
```

## 제외 경로 설정

JWT 인증을 적용하지 않을 경로를 설정할 수 있습니다:

```yaml
hexacore:
  security:
    filter:
      exclude-paths:
        - /api/public/**
        - /health
        - /swagger-ui/**
    tokenProvider:
      jwt:
        excluded-paths:
          - /api/auth/login
          - /api/auth/refresh
```

## 마이그레이션 가이드

### 기존 코드에서 마이그레이션

**이전 방식**:
```java
@ComponentScan(basePackages = {
    "com.myapp",
    "com.dx.hexacore.security"  // 명시적 스캔 필요
})
```

**새로운 방식**:
```java
// 별도 설정 불필요, Auto-Configuration이 자동 처리
@SpringBootApplication
public class MyApplication {
    // ...
}
```

### 충돌 해결

만약 기존 SecurityFilterChain과 충돌이 발생한다면:

1. **옵션 1**: `servlet-filter` 전략 사용
2. **옵션 2**: `manual` 전략으로 완전 제어
3. **옵션 3**: JwtSecurityHelper 사용하여 통합

## 문제 해결

### JWT 필터가 적용되지 않음
- `hexacore.security.jwt.enabled: true` 확인
- TokenProvider Bean이 생성되었는지 확인
- 로그에서 "JWT Strategy" 메시지 확인

### SecurityFilterChain 충돌
- 부모 프로젝트의 @Order 확인
- `servlet-filter` 전략 고려
- @ConditionalOnMissingBean 조건 확인

### 제외 경로가 작동하지 않음
- 경로 패턴 확인 (Ant 패턴 사용)
- 여러 설정 위치의 제외 경로 모두 확인
- 로그에서 "Applied exclude paths" 메시지 확인

## 모니터링

활성화된 전략과 설정을 확인하려면 애플리케이션 시작 로그를 확인하세요:

```
🚀 JWT Strategy: Security Integration (Fallback SecurityFilterChain 생성)
✅ JWT 설정이 HttpSecurity에 적용됨
✅ JWT 제외 경로 적용: [/api/public/**, /health]
```