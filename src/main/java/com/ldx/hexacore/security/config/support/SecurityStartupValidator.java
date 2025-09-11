package com.ldx.hexacore.security.config.support;

import com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import java.util.Map;

/**
 * Security Starter 초기화 검증 도구
 * 애플리케이션 시작 시 보안 구성 요소들이 올바르게 로드되었는지 확인합니다.
 */
@Component
public class SecurityStartupValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityStartupValidator.class);
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired(required = false)
    private TokenProvider tokenProvider;
    
    @EventListener(ApplicationReadyEvent.class)
    public void validateSecurityConfiguration() {
        logger.info("=== Hexacore Security Starter 구성 검증 시작 ===");
        
        // 1. TokenProvider 확인
        if (tokenProvider != null) {
            logger.info("✅ TokenProvider 구현체: {}", tokenProvider.getClass().getSimpleName());
        } else {
            logger.error("❌ TokenProvider가 생성되지 않았습니다!");
        }
        
        // 2. SecurityFilterChain 확인
        try {
            Map<String, SecurityFilterChain> filterChains = applicationContext.getBeansOfType(SecurityFilterChain.class);
            logger.info("✅ SecurityFilterChain 개수: {}", filterChains.size());
            filterChains.forEach((name, chain) -> {
                logger.info("  - {} ({})", name, chain.getClass().getName());
            });
        } catch (Exception e) {
            logger.error("❌ SecurityFilterChain 조회 실패: {}", e.getMessage());
        }
        
        // 3. JWT 필터 확인
        try {
            Map<String, Filter> filters = applicationContext.getBeansOfType(Filter.class);
            long jwtFilterCount = filters.entrySet().stream()
                .filter(entry -> entry.getValue().getClass().getName().contains("JwtAuthenticationFilter"))
                .count();
            
            if (jwtFilterCount > 0) {
                logger.info("✅ JwtAuthenticationFilter 등록됨");
            } else {
                logger.warn("⚠️ JwtAuthenticationFilter가 Bean으로 등록되지 않았습니다. (SecurityFilterChain 내부에만 존재할 수 있음)");
            }
        } catch (Exception e) {
            logger.error("❌ Filter 조회 실패: {}", e.getMessage());
        }
        
        // 4. 보안 설정 속성 확인
        logSecurityProperties();
        
        logger.info("=== Hexacore Security Starter 구성 검증 완료 ===");
    }
    
    private void logSecurityProperties() {
        try {
            logger.info("📋 보안 설정 속성:");
            
            // Spring Security 자동 구성 확인
            String[] autoConfigExclusions = applicationContext.getEnvironment()
                .getProperty("spring.autoconfigure.exclude", String[].class, new String[0]);
            if (autoConfigExclusions.length > 0) {
                logger.warn("  ⚠️ 제외된 자동 구성: {}", String.join(", ", autoConfigExclusions));
            }
            
            // Hexacore Security 속성
            Boolean hexacoreEnabled = applicationContext.getEnvironment()
                .getProperty("security-starter.enabled", Boolean.class, true);
            logger.info("  - security-starter.enabled: {}", hexacoreEnabled);
            
            Boolean filterEnabled = applicationContext.getEnvironment()
                .getProperty("security-starter.filter.enabled", Boolean.class, true);
            logger.info("  - security-starter.filter.enabled: {}", filterEnabled);
            
            String tokenProvider = applicationContext.getEnvironment()
                .getProperty("security-starter.token-provider.provider", "jwt");
            logger.info("  - security-starter.token-provider.provider: {}", tokenProvider);
            
            Boolean jwtEnabled = applicationContext.getEnvironment()
                .getProperty("security-starter.token-provider.jwt.enabled", Boolean.class, true);
            logger.info("  - security-starter.token-provider.jwt.enabled: {}", jwtEnabled);
            
            // Legacy 속성 (호환성)
            Boolean authJwtEnabled = applicationContext.getEnvironment()
                .getProperty("security.auth.jwt.enabled", Boolean.class, true);
            logger.info("  - security.auth.jwt.enabled: {}", authJwtEnabled);
            
        } catch (Exception e) {
            logger.error("속성 조회 중 오류 발생: {}", e.getMessage());
        }
    }
}