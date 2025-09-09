# Security-Starter 테스트 실패 문제 분석 및 수정 방안

## 문제 상황

Jenkins에서 `SPRING_PROFILES_ACTIVE=prod` 환경으로 빌드 시 다음 6개 테스트가 실패:

```
DatabaseSchemaTest > printDatabaseSchema() FAILED
DatabaseSchemaValidationTest > validateEmployeeViewSchema() FAILED  
DatabaseSchemaValidationTest > validateOrganizationViewSchema() FAILED
DatabaseSchemaValidationTest > validatePositionViewSchema() FAILED
PositionSchemaTest > printPositionSchema() FAILED
IntegratedHrControllerRestDocsTest > health() FAILED
```

## 근본 원인 분석

### 1. SecurityConfigurationValidator 오류
- **위치**: `SecurityConfigurationValidator.java:54`
- **원인**: Production 프로파일에서 Keycloak 서버 연결 검증 실패
- **상세**: `hexacore.security.token-provider.keycloak.server-url` 플레이스홀더 해결 불가

### 2. 자동 설정 충돌
- Security-Starter의 자동 설정이 테스트 환경에서도 강제 활성화
- `hexacore.security.enabled=false` 설정이 무시됨
- Bean 의존성 주입 실패: `JwtAuthenticationFilter` Bean 누락

### 3. 프로파일별 설정 문제
- application.yml과 application-prod.yml이 동일한 내용
- 테스트 환경에서 production 데이터베이스/외부 서비스 접근 시도

## Security-Starter에서 수정해야 할 사항

### 1. SecurityConfigurationValidator 개선
**파일**: `SecurityConfigurationValidator.java`

```java
// 현재 문제: 무조건 검증 수행
@Component
public class SecurityConfigurationValidator {
    
    // 수정 방안: 테스트 환경 감지 및 스킵 로직 추가
    @EventListener(ApplicationReadyEvent.class)
    public void validateConfiguration() {
        // 테스트 환경 감지
        if (isTestEnvironment()) {
            log.info("테스트 환경에서는 보안 설정 검증을 스킵합니다.");
            return;
        }
        
        // Keycloak 설정이 없는 경우 graceful 처리
        if (!hasKeycloakConfiguration()) {
            log.warn("Keycloak 설정이 없어 검증을 스킵합니다.");
            return;
        }
        
        // 실제 검증 로직 수행
        performValidation();
    }
    
    private boolean isTestEnvironment() {
        return environment.getActiveProfiles().length == 0 || 
               Arrays.asList(environment.getActiveProfiles()).contains("test") ||
               TestContextManager.getCurrentTestContext() != null;
    }
}
```

### 2. 자동 설정 조건부 활성화 개선
**파일**: `SecurityAutoConfiguration.java`

```java
@Configuration
@ConditionalOnProperty(
    prefix = "hexacore.security", 
    name = "enabled", 
    havingValue = "true", 
    matchIfMissing = false  // 기본값을 false로 변경
)
@ConditionalOnClass({SecurityFilterChain.class, JwtDecoder.class})
@ConditionalOnMissingBean(SecurityFilterChain.class)
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityAutoConfiguration {

    // 추가: 테스트 환경에서는 자동 설정 비활성화
    @ConditionalOnMissingClass("org.springframework.boot.test.context.SpringBootTest")
    @Bean
    public SecurityFilterChain filterChain() {
        // 기존 구현
    }
}
```

### 3. 설정 속성 검증 개선
**파일**: `SecurityProperties.java`

```java
@ConfigurationProperties(prefix = "hexacore.security")
@Validated
public class SecurityProperties {
    
    @NestedConfigurationProperty
    private TokenProvider tokenProvider = new TokenProvider();
    
    public static class TokenProvider {
        @NestedConfigurationProperty  
        private Keycloak keycloak = new Keycloak();
        
        public static class Keycloak {
            // 필수 속성을 조건부로 변경
            @NotBlank(groups = ProductionValidation.class)
            private String serverUrl;
            
            @NotBlank(groups = ProductionValidation.class)
            private String realm;
            
            @NotBlank(groups = ProductionValidation.class)
            private String clientId;
            
            // 테스트 환경에서는 검증 스킵을 위한 그룹 정의
        }
    }
    
    // 검증 그룹 인터페이스
    public interface ProductionValidation {}
}
```

### 4. 조건부 Bean 생성 개선
**파일**: `JwtAutoConfiguration.java`

```java
@Configuration
@ConditionalOnProperty(prefix = "hexacore.security", name = "enabled", havingValue = "true")
@ConditionalOnClass(JwtDecoder.class)
public class JwtAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "hexacore.security.token-provider.keycloak", name = "enabled", havingValue = "true")
    // 추가: 필수 속성이 모두 있을 때만 Bean 생성
    @ConditionalOnProperty(prefix = "hexacore.security.token-provider.keycloak", name = {"server-url", "realm", "client-id"})
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        // 기존 구현
    }
}
```

### 5. 테스트 지원 개선
**새 파일**: `TestSecurityAutoConfiguration.java`

```java
@TestConfiguration
@ConditionalOnClass(SpringBootTest.class)
public class TestSecurityAutoConfiguration {
    
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(csrf -> csrf.disable())
            .build();
    }
}
```

## 즉시 적용 가능한 Workaround

현재 hexa-hr 프로젝트에서 임시로 사용할 수 있는 방법:

### 1. 테스트 전용 Application 클래스 생성
**파일**: `src/test/java/.../TestHexaHrApplication.java`

```java
@SpringBootApplication(exclude = {
    SecurityFilterAutoConfiguration.class,
    JwtAutoConfiguration.class,
    WebStarterAutoConfiguration.class
})
@TestConfiguration
public class TestHexaHrApplication {
    // 테스트용 minimal 설정
}
```

### 2. build.gradle 테스트 태스크 수정
```gradle
test {
    // production 프로파일이 설정되어 있어도 테스트는 test 프로파일로 실행
    systemProperty "spring.profiles.active", "test"
    systemProperty "hexacore.security.enabled", "false"
    systemProperty "web-starter.enabled", "false"
}
```

## 우선순위별 수정 권장사항

### 🔴 HIGH (즉시 수정 필요)
1. SecurityConfigurationValidator의 테스트 환경 감지 로직 추가
2. 조건부 Bean 생성에서 필수 속성 검증 추가
3. 자동 설정의 matchIfMissing 기본값을 false로 변경

### 🟡 MEDIUM (다음 버전에서 수정)
1. 테스트 지원을 위한 TestSecurityAutoConfiguration 추가
2. 설정 속성 검증 그룹 적용
3. 더 나은 에러 메시지 제공

### 🟢 LOW (장기적 개선)
1. 테스트 유틸리티 클래스 제공
2. 문서화 개선
3. 예제 프로젝트에 테스트 설정 포함

## 참고: 로그에서 확인된 정보

```
SecurityConfigurationValidator : 🔍 Security-Starter 설정 검증을 시작합니다...
SecurityConfigurationValidator : ⚠️ ⚠️ COMPATIBILITY: Spring Security 6.5.2는 테스트되지 않은 버전입니다
```

- Security-Starter 버전: 1.4.0
- Spring Security 버전: 6.5.2 (호환성 경고 발생)
- 에러 위치: SecurityConfigurationValidator.java:54

이 레포트를 바탕으로 security-starter 프로젝트에서 해당 이슈들을 수정하면 Jenkins 빌드 문제가 해결될 것입니다.