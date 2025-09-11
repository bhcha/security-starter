package com.ldx.hexacore.security.config;

import com.ldx.hexacore.security.auth.application.command.port.in.AuthenticationUseCase;
import com.ldx.hexacore.security.config.autoconfigure.*;
import com.ldx.hexacore.security.config.properties.SecurityStarterProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Security Starter 메인 자동 설정 클래스.
 * 
 * <p>Spring Boot Starter 표준 아키텍처 가이드라인을 준수합니다.
 * 
 * <p>핵심 원칙:
 * 1. 단일 조건만 사용 (@ConditionalOnProperty 1개)
 * 2. 모든 Bean은 @ConditionalOnMissingBean (부모 우선)
 * 3. 조건 로직은 Properties 편의 메서드에서 처리
 * 4. Mode 기반 아키텍처 지원 (Traditional/Hexagonal)
 * 5. 기존 설정과의 완전한 호환성 유지
 * 
 * @author security-starter
 * @since 1.9.0
 */
@Configuration("securityStarterAutoConfiguration")
@ConditionalOnClass(AuthenticationUseCase.class)
@ConditionalOnProperty(
    prefix = "security-starter", 
    name = "enabled", 
    havingValue = "true", 
    matchIfMissing = true  // Zero Configuration 원칙
)
@EnableConfigurationProperties({
    SecurityStarterProperties.class,
    SecurityConstants.class
})
@Import({
    // Mode별 Configuration
    TraditionalModeConfiguration.class,
    HexagonalModeConfiguration.class,
    // 기능별 AutoConfiguration
    TokenProviderAutoConfiguration.class,
    PersistenceAutoConfiguration.class,
    SecurityFilterAutoConfiguration.class,
    ApplicationLayerAutoConfiguration.class,
    SupportBeansAutoConfiguration.class,
    LoggingBeansAutoConfiguration.class
})
public class SecurityStarterAutoConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityStarterAutoConfiguration.class);
    
    private final SecurityStarterProperties securityStarterProperties;
    
    public SecurityStarterAutoConfiguration(
            SecurityStarterProperties securityStarterProperties) {
        this.securityStarterProperties = securityStarterProperties;
    }
    
    @PostConstruct
    public void init() {
        logger.info("🔐 Security Starter v1.9.0 initialized");
        logger.info("   - Mode: {}", securityStarterProperties.getMode());
        logger.info("   - Authentication: {}", securityStarterProperties.isAuthenticationEnabled() ? "✅" : "❌");
        logger.info("   - Session Management: {}", securityStarterProperties.isSessionManagementEnabled() ? "✅" : "❌");
        logger.info("   - JWT: {}", securityStarterProperties.isJwtEnabled() ? "✅" : "❌");
        logger.info("   - Keycloak: {}", securityStarterProperties.isKeycloakEnabled() ? "✅" : "❌");
        logger.info("   - Rate Limiting: {}", securityStarterProperties.isRateLimitEnabled() ? "✅" : "❌");
        logger.info("   - IP Restriction: {}", securityStarterProperties.isIpRestrictionEnabled() ? "✅" : "❌");
        logger.info("   - Security Headers: {}", securityStarterProperties.isSecurityHeadersEnabled() ? "✅" : "❌");
    }
    
}