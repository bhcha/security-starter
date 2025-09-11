package com.ldx.hexacore.security.config.support;

import com.ldx.hexacore.security.config.properties.SecurityStarterProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Security-Starter 설정의 전역 검증자
 * 
 * 애플리케이션 시작 시 설정을 검증하고 경고 및 오류를 출력합니다.
 */
@Component
@ConditionalOnProperty(prefix = "security-starter", name = "enabled", havingValue = "true")
public class SecurityConfigurationValidator implements ApplicationListener<ApplicationReadyEvent> {
    
    private static final Logger log = LoggerFactory.getLogger(SecurityConfigurationValidator.class);
    
    private final SecurityStarterProperties properties;
    
    public SecurityConfigurationValidator(SecurityStarterProperties properties) {
        this.properties = properties;
    }
    
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // 테스트 환경에서는 검증 스킵
        if (isTestEnvironment()) {
            log.info("🧪 테스트 환경에서는 Security-Starter 설정 검증을 스킵합니다.");
            return;
        }
        
        log.info("🔍 Security-Starter 설정 검증을 시작합니다...");
        
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        List<String> info = new ArrayList<>();
        
        validateOverallConfiguration(warnings, errors, info);
        validateTokenProviderConfiguration(warnings, errors, info);
        validateSessionConfiguration(warnings, errors, info);
        validateSecurityConfiguration(warnings, errors, info);
        
        // v1.0.4 확장 검증
        validateProductionSecurity(warnings, errors, info);
        validatePerformanceSettings(warnings, errors, info);
        validateDependencyCompatibility(warnings, errors, info);
        
        // 결과 출력
        logValidationResults(warnings, errors, info);
        
        if (!errors.isEmpty()) {
            throw new IllegalStateException("Security-Starter 설정 오류가 발견되었습니다. 위의 오류를 수정해주세요.");
        }
    }
    
    private void validateOverallConfiguration(List<String> warnings, List<String> errors, List<String> info) {
        // 기본 활성화 상태 확인
        if (properties.getEnabled()) {
            info.add("Security-Starter가 활성화되었습니다");
        }
        
        // 프로덕션 환경 확인
        if (isProductionEnvironment()) {
            info.add("프로덕션 환경이 감지되었습니다 - 강화된 보안 검증을 수행합니다");
        } else {
            info.add("개발 환경이 감지되었습니다 - 기본 보안 검증을 수행합니다");
        }
    }
    
    private void validateTokenProviderConfiguration(List<String> warnings, List<String> errors, List<String> info) {
        String provider = properties.getTokenProvider().getProvider();
        info.add("토큰 제공자: " + provider);
        
        // JWT vs Keycloak 동시 활성화 체크
        boolean jwtEnabled = properties.getTokenProvider().getJwt().getEnabled();
        boolean keycloakEnabled = properties.getTokenProvider().getKeycloak().getEnabled();
        
        if ("jwt".equals(provider)) {
            if (!jwtEnabled) {
                errors.add("JWT가 선택되었지만 JWT 설정이 비활성화되어 있습니다");
            }
            if (keycloakEnabled) {
                warnings.add("JWT가 선택되었지만 Keycloak도 활성화되어 있습니다. JWT가 우선 사용됩니다");
            }
            
            validateJwtConfiguration(warnings, errors, info);
            
        } else if ("keycloak".equals(provider)) {
            if (!keycloakEnabled) {
                errors.add("Keycloak이 선택되었지만 Keycloak 설정이 비활성화되어 있습니다");
            }
            if (jwtEnabled) {
                warnings.add("Keycloak이 선택되었지만 JWT도 활성화되어 있습니다. Keycloak이 우선 사용됩니다");
            }
            
            validateKeycloakConfiguration(warnings, errors, info);
        }
    }
    
    private void validateJwtConfiguration(List<String> warnings, List<String> errors, List<String> info) {
        var jwt = properties.getTokenProvider().getJwt();
        
        // Secret 보안 검증
        if (isProductionEnvironment()) {
            if (jwt.getSecret().length() < 64) {
                warnings.add("프로덕션 환경에서는 64자 이상의 JWT secret 사용을 권장합니다");
            }
            
            if (jwt.getSecret().contains("default") || 
                jwt.getSecret().contains("example") || 
                jwt.getSecret().contains("test")) {
                errors.add("프로덕션 환경에서는 기본 secret을 사용할 수 없습니다");
            }
        }
        
        // 토큰 만료 시간 검증
        if (jwt.getAccessTokenExpiration() > jwt.getRefreshTokenExpiration()) {
            errors.add("액세스 토큰 만료 시간이 리프레시 토큰 만료 시간보다 깁니다");
        }
        
        // 알고리즘 보안성 검증
        if (isProductionEnvironment() && jwt.getAlgorithm().startsWith("HS")) {
            warnings.add("프로덕션 환경에서는 RS256 등의 비대칭 알고리즘 사용을 권장합니다");
        }
        
        info.add("JWT 설정: 알고리즘=" + jwt.getAlgorithm() + 
                ", 액세스토큰만료=" + jwt.getAccessTokenExpiration() + "초" +
                ", 리프레시토큰만료=" + jwt.getRefreshTokenExpiration() + "초");
    }
    
    private void validateKeycloakConfiguration(List<String> warnings, List<String> errors, List<String> info) {
        var keycloak = properties.getTokenProvider().getKeycloak();
        
        // URL 보안 검증
        if (isProductionEnvironment() && keycloak.getServerUrl() != null) {
            if (!keycloak.getServerUrl().startsWith("https://")) {
                errors.add("프로덕션 환경에서는 Keycloak에 HTTPS를 사용해야 합니다");
            }
            if (keycloak.getServerUrl().contains("localhost")) {
                warnings.add("프로덕션 환경에서 localhost를 Keycloak 서버로 사용하고 있습니다");
            }
        }
        
        // 필수 설정 확인
        if (keycloak.getEnabled()) {
            String serverUrl = keycloak.getServerUrl();
            String realm = keycloak.getRealm(); 
            String clientId = keycloak.getClientId();
            
            // Placeholder 미해결 검증
            if (serverUrl == null || serverUrl.trim().isEmpty() || serverUrl.contains("${")) {
                if (serverUrl != null && serverUrl.contains("${")) {
                    errors.add("Keycloak 서버 URL의 placeholder가 해결되지 않았습니다: " + serverUrl);
                } else {
                    errors.add("Keycloak 서버 URL이 설정되지 않았습니다");
                }
            }
            if (realm == null || realm.trim().isEmpty() || realm.contains("${")) {
                if (realm != null && realm.contains("${")) {
                    errors.add("Keycloak Realm의 placeholder가 해결되지 않았습니다: " + realm);
                } else {
                    errors.add("Keycloak Realm이 설정되지 않았습니다");
                }
            }
            if (clientId == null || clientId.trim().isEmpty() || clientId.contains("${")) {
                if (clientId != null && clientId.contains("${")) {
                    errors.add("Keycloak Client ID의 placeholder가 해결되지 않았습니다: " + clientId);
                } else {
                    errors.add("Keycloak Client ID가 설정되지 않았습니다");
                }
            }
        }
        
        info.add("Keycloak 설정: 서버=" + keycloak.getServerUrl() + 
                ", Realm=" + keycloak.getRealm() + 
                ", Client=" + keycloak.getClientId());
    }
    
    private void validateSessionConfiguration(List<String> warnings, List<String> errors, List<String> info) {
        if (!properties.getSession().getEnabled()) {
            warnings.add("세션 관리가 비활성화되어 있습니다. 계정 잠금 기능을 사용할 수 없습니다");
            return;
        }
        
        var lockout = properties.getSession().getLockout();
        
        // 계정 잠금 정책 검증
        if (lockout.getMaxAttempts() < 3) {
            warnings.add("최대 시도 횟수가 3회 미만입니다. 보안상 3회 이상을 권장합니다");
        }
        if (lockout.getMaxAttempts() > 10) {
            warnings.add("최대 시도 횟수가 10회 초과입니다. 보안상 10회 이하를 권장합니다");
        }
        
        if (lockout.getLockoutDurationMinutes() < 5) {
            warnings.add("잠금 시간이 5분 미만입니다. 보안상 5분 이상을 권장합니다");
        }
        
        info.add("세션 관리: 최대시도=" + lockout.getMaxAttempts() + "회" +
                ", 잠금시간=" + lockout.getLockoutDurationMinutes() + "분" +
                ", 시도윈도우=" + lockout.getAttemptWindowMinutes() + "분");
    }
    
    private void validateSecurityConfiguration(List<String> warnings, List<String> errors, List<String> info) {
        // 캐시 설정 확인
        if (properties.getCache().getEnabled()) {
            info.add("캐시 활성화: " + properties.getCache().getType());
        } else {
            warnings.add("캐시가 비활성화되어 있습니다. 성능에 영향을 줄 수 있습니다");
        }
        
        // 보안 헤더 확인
        if (properties.getHeaders().getEnabled()) {
            info.add("보안 헤더가 활성화되었습니다");
        } else {
            warnings.add("보안 헤더가 비활성화되어 있습니다. 보안에 취약할 수 있습니다");
        }
    }
    
    private void logValidationResults(List<String> warnings, List<String> errors, List<String> info) {
        // 정보 출력
        if (!info.isEmpty()) {
            log.info("📊 Security-Starter 설정 정보:");
            info.forEach(msg -> log.info("   ℹ️ {}", msg));
        }
        
        // 경고 출력
        if (!warnings.isEmpty()) {
            log.warn("🔶 Security-Starter 설정 경고사항:");
            warnings.forEach(msg -> log.warn("   ⚠️ {}", msg));
        }
        
        // 오류 출력
        if (!errors.isEmpty()) {
            log.error("🚨 Security-Starter 설정 오류:");
            errors.forEach(msg -> log.error("   ❌ {}", msg));
        }
        
        // 최종 결과
        if (errors.isEmpty() && warnings.isEmpty()) {
            log.info("✅ Security-Starter 설정 검증이 성공적으로 완료되었습니다");
        } else if (errors.isEmpty()) {
            log.info("✅ Security-Starter 설정 검증 완료 ({}개 경고)", warnings.size());
        } else {
            log.error("❌ Security-Starter 설정 검증 실패 ({}개 오류, {}개 경고)", errors.size(), warnings.size());
        }
    }
    
    /**
     * v1.0.4 확장: 프로덕션 환경 보안 강화 검증
     */
    private void validateProductionSecurity(List<String> warnings, List<String> errors, List<String> info) {
        if (!isProductionEnvironment()) {
            return;
        }
        
        info.add("프로덕션 환경 보안 검증을 수행합니다");
        
        // JWT Secret 강도 검증
        var jwt = properties.getTokenProvider().getJwt();
        if (jwt.getEnabled()) {
            String secret = jwt.getSecret();
            
            // 최소 256비트 (32바이트) 검증
            if (secret.length() < 32) {
                errors.add("🚨 CRITICAL: JWT secret key는 최소 256비트(32자)여야 합니다. 현재: " + (secret.length() * 8) + "비트");
            }
            
            // 테스트/데모 키 검증
            if (secret.toLowerCase().contains("test") || 
                secret.toLowerCase().contains("demo") || 
                secret.toLowerCase().contains("example") ||
                secret.toLowerCase().contains("default")) {
                errors.add("🚨 CRITICAL: 프로덕션에서 테스트용 시크릿 키를 사용 중입니다!");
            }
            
            // 엔트로피 검증 (간단한 체크)
            if (isLowEntropySecret(secret)) {
                warnings.add("⚠️ SECURITY: JWT secret의 엔트로피가 낮습니다. 더 복잡한 키를 사용하세요");
            }
        }
        
        // HTTPS 강제 검증
        validateHttpsConfiguration(warnings, errors, info);
        
        // 보안 헤더 검증
        validateSecurityHeaders(warnings, errors, info);
    }
    
    /**
     * v1.0.4 확장: 성능 영향도 검증
     */
    private void validatePerformanceSettings(List<String> warnings, List<String> errors, List<String> info) {
        info.add("성능 설정을 검증합니다");
        
        // 세션 관리 성능 검증
        if (properties.getSession().getEnabled()) {
            var lockout = properties.getSession().getLockout();
            
            // 최대 세션 수 경고 (가상의 설정이지만 예시로)
            if (lockout.getMaxAttempts() > 100) {
                warnings.add("⚠️ PERFORMANCE: 최대 시도 횟수가 너무 높습니다. 메모리 사용량을 확인하세요");
            }
            
            // 시도 윈도우 시간 검증
            if (lockout.getAttemptWindowMinutes() > 60) {
                warnings.add("⚠️ PERFORMANCE: 시도 윈도우가 너무 깁니다. 메모리 사용량에 영향을 줄 수 있습니다");
            }
        }
        
        // 토큰 만료 시간 성능 검증
        var jwt = properties.getTokenProvider().getJwt();
        if (jwt.getEnabled()) {
            // 30일 = 2,592,000초
            if (jwt.getAccessTokenExpiration() > 2592000) {
                warnings.add("⚠️ SECURITY: 액세스 토큰 만료 시간이 너무 깁니다 (30일 초과). 보안 정책을 확인하세요");
            }
            
            // 1년 = 31,536,000초
            if (jwt.getRefreshTokenExpiration() > 31536000) {
                warnings.add("⚠️ SECURITY: 리프레시 토큰 만료 시간이 너무 깁니다 (1년 초과). 보안 정책을 확인하세요");
            }
        }
        
        // 캐시 설정 성능 검증
        if (properties.getCache().getEnabled()) {
            info.add("캐시가 활성화되어 성능이 향상됩니다");
        } else {
            warnings.add("⚠️ PERFORMANCE: 캐시가 비활성화되어 있습니다. 성능에 영향을 줄 수 있습니다");
        }
    }
    
    /**
     * v1.0.4 확장: 의존성 호환성 검증
     */
    private void validateDependencyCompatibility(List<String> warnings, List<String> errors, List<String> info) {
        info.add("의존성 호환성을 검증합니다");
        
        // Spring Security 버전 호환성
        try {
            String springSecurityVersion = getSpringSecurityVersion();
            if (springSecurityVersion != null) {
                info.add("Spring Security 버전: " + springSecurityVersion);
                
                if (!isCompatibleSpringSecurityVersion(springSecurityVersion)) {
                    warnings.add("⚠️ COMPATIBILITY: Spring Security " + springSecurityVersion + "는 테스트되지 않은 버전입니다");
                }
            }
        } catch (Exception e) {
            warnings.add("⚠️ Spring Security 버전을 확인할 수 없습니다");
        }
        
        // JWT 라이브러리 호환성
        validateJwtLibraryCompatibility(warnings, errors, info);
        
        // Spring Boot 버전 호환성
        validateSpringBootCompatibility(warnings, errors, info);
    }
    
    private void validateHttpsConfiguration(List<String> warnings, List<String> errors, List<String> info) {
        // 실제 환경에서는 server.ssl.enabled 등을 체크할 수 있음
        String serverPort = System.getProperty("server.port", "8080");
        if ("8080".equals(serverPort) && isProductionEnvironment()) {
            warnings.add("⚠️ SECURITY: 프로덕션 환경에서 기본 HTTP 포트(8080)를 사용하고 있습니다. HTTPS 설정을 확인하세요");
        }
    }
    
    private void validateSecurityHeaders(List<String> warnings, List<String> errors, List<String> info) {
        if (!properties.getHeaders().getEnabled()) {
            warnings.add("⚠️ SECURITY: 보안 헤더가 비활성화되어 있습니다. XSS, CSRF 등에 취약할 수 있습니다");
        }
    }
    
    private boolean isLowEntropySecret(String secret) {
        // 간단한 엔트로피 체크: 반복 문자나 순차 문자 검증
        if (secret.matches("(.)\\1{3,}")) { // 같은 문자 4번 이상 반복
            return true;
        }
        if (secret.matches(".*(?:abc|123|qwe|aaa|111).*")) { // 간단한 패턴
            return true;
        }
        return false;
    }
    
    private String getSpringSecurityVersion() {
        try {
            Class<?> securityClass = Class.forName("org.springframework.security.core.SpringSecurityCoreVersion");
            Object version = securityClass.getMethod("getVersion").invoke(null);
            return version != null ? version.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }
    
    private boolean isCompatibleSpringSecurityVersion(String version) {
        // 지원되는 Spring Security 버전 목록 (예시)
        return version.startsWith("6.1") || 
               version.startsWith("6.2") || 
               version.startsWith("6.3");
    }
    
    private void validateJwtLibraryCompatibility(List<String> warnings, List<String> errors, List<String> info) {
        try {
            // JWT 라이브러리 존재 확인
            Class.forName("io.jsonwebtoken.Jwts");
            info.add("JWT 라이브러리가 정상적으로 로드되었습니다");
            
            // 버전 확인 (가능한 경우)
            try {
                Class<?> jwtClass = Class.forName("io.jsonwebtoken.lang.Classes");
                info.add("JWT 라이브러리 호환성 확인 완료");
            } catch (ClassNotFoundException e) {
                warnings.add("⚠️ JWT 라이브러리 버전이 예상과 다를 수 있습니다");
            }
        } catch (ClassNotFoundException e) {
            if (properties.getTokenProvider().getJwt().getEnabled()) {
                errors.add("❌ JWT가 활성화되었지만 JWT 라이브러리를 찾을 수 없습니다");
            }
        }
    }
    
    private void validateSpringBootCompatibility(List<String> warnings, List<String> errors, List<String> info) {
        try {
            String springBootVersion = getSpringBootVersion();
            if (springBootVersion != null) {
                info.add("Spring Boot 버전: " + springBootVersion);
                
                if (!isCompatibleSpringBootVersion(springBootVersion)) {
                    warnings.add("⚠️ COMPATIBILITY: Spring Boot " + springBootVersion + "는 테스트되지 않은 버전입니다");
                }
            }
        } catch (Exception e) {
            warnings.add("⚠️ Spring Boot 버전을 확인할 수 없습니다");
        }
    }
    
    private String getSpringBootVersion() {
        try {
            Class<?> bootVersionClass = Class.forName("org.springframework.boot.SpringBootVersion");
            Object version = bootVersionClass.getMethod("getVersion").invoke(null);
            return version != null ? version.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }
    
    private boolean isCompatibleSpringBootVersion(String version) {
        // 지원되는 Spring Boot 버전 목록
        return version.startsWith("3.2") || 
               version.startsWith("3.3") || 
               version.startsWith("3.4") ||
               version.startsWith("3.5");
    }
    
    private boolean isProductionEnvironment() {
        String profile = System.getProperty("spring.profiles.active", "");
        String env = System.getenv("SPRING_PROFILES_ACTIVE");
        if (env != null) {
            profile = env;
        }
        return profile.contains("prod") || profile.contains("production");
    }
    
    /**
     * 테스트 환경 여부를 확인합니다.
     * 다음 조건 중 하나라도 만족하면 테스트 환경으로 판단:
     * 1. Spring profiles에 "test" 포함
     * 2. 테스트 관련 시스템 프로퍼티 존재
     * 3. Spring Boot Test 컨텍스트 활성화
     */
    private boolean isTestEnvironment() {
        // 강제 프로덕션 모드 시스템 프로퍼티 확인 (테스트용)
        if ("true".equals(System.getProperty("security-starter.force-production-validation"))) {
            return false;
        }
        
        // 1. Spring profiles 확인
        String profile = System.getProperty("spring.profiles.active", "");
        String env = System.getenv("SPRING_PROFILES_ACTIVE");
        if (env != null) {
            profile = env;
        }
        
        if (profile.contains("test") || profile.isEmpty()) {
            return true;
        }
        
        // 2. 테스트 관련 시스템 프로퍼티 확인
        if ("true".equals(System.getProperty("spring.boot.test.context.SpringBootTestContextBootstrapper"))) {
            return true;
        }
        
        // 3. Spring Boot Test 관련 클래스 존재 확인
        try {
            Class.forName("org.springframework.boot.test.context.SpringBootTest");
            // TestContext 활성화 여부 확인
            String testContextActive = System.getProperty("spring.test.context.cache.maxSize");
            if (testContextActive != null) {
                return true;
            }
        } catch (ClassNotFoundException e) {
            // 테스트 클래스가 없으면 프로덕션 환경
        }
        
        // 4. JUnit 실행 여부 확인
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            if (className.contains("junit") || 
                className.contains("Test") || 
                className.contains("gradle.api.internal.tasks.testing")) {
                return true;
            }
        }
        
        return false;
    }
}