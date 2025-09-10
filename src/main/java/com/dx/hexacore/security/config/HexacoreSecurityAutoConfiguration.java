package com.dx.hexacore.security.config;

import com.dx.hexacore.security.auth.application.command.port.in.AuthenticationUseCase;
import com.dx.hexacore.security.config.autoconfigure.TokenProviderAutoConfiguration;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration for Hexacore Security library.
 * This configuration enables component scanning for all security-related beans.
 * 
 * Users can override specific beans by defining their own with the same type.
 */
@Configuration("hexacoreSecurityAutoConfiguration")
@ConditionalOnClass(AuthenticationUseCase.class)
@ConditionalOnProperty(prefix = "hexacore.security", name = "enabled", havingValue = "true", matchIfMissing = false)
@EnableConfigurationProperties({
    com.dx.hexacore.security.config.properties.HexacoreSecurityProperties.class,
    com.dx.hexacore.security.config.SecurityConstants.class
})
@Import({
    TokenProviderAutoConfiguration.class,
    com.dx.hexacore.security.config.autoconfigure.PersistenceAutoConfiguration.class,
    com.dx.hexacore.security.config.autoconfigure.SecurityFilterAutoConfiguration.class,
    com.dx.hexacore.security.config.autoconfigure.HexacoreSecurityFilterAutoConfiguration.class,
    com.dx.hexacore.security.config.autoconfigure.ApplicationLayerAutoConfiguration.class
})
@ComponentScan(basePackages = {
    "com.dx.hexacore.security.config.support",
    "com.dx.hexacore.security.logging"
}, excludeFilters = {
    @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = ".*persistence.*"
    )
})
public class HexacoreSecurityAutoConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(HexacoreSecurityAutoConfiguration.class);
    
    @PostConstruct
    public void init() {
        logger.info("🚀 Hexacore Security Starter 초기화 중...");
        
        // Spring Security 버전 체크
        try {
            Package securityPackage = Package.getPackage("org.springframework.security");
            if (securityPackage != null) {
                String version = securityPackage.getImplementationVersion();
                logger.info("📊 Spring Security 버전: {}", version != null ? version : "UNKNOWN");
                
                // 지원되는 버전 체크 (6.0.x ~ 6.3.x)
                if (version != null && !version.startsWith("6.")) {
                    logger.warn("⚠️ 지원되지 않는 Spring Security 버전입니다. 권장 버전: 6.0.x ~ 6.3.x");
                }
            }
        } catch (Exception e) {
            logger.debug("Spring Security 버전 확인 실패: {}", e.getMessage());
        }
        
        logger.info("✅ Hexacore Security Starter 초기화 완료");
    }
    
    /**
     * Marker bean to indicate that Hexacore Security is configured
     */
    @Bean
    public Object hexacoreSecurityMarker() {
        return new Object();
    }
}