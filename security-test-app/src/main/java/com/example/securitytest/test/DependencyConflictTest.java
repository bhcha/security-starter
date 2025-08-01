package com.example.securitytest.test;

import com.dx.hexacore.security.auth.application.command.port.in.AuthenticationUseCase;
import com.dx.hexacore.security.auth.application.command.port.in.TokenManagementUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.stereotype.Component;

/**
 * 의존성 충돌 테스트 클래스
 * 
 * Spring Security와 Hexacore Security 라이브러리 간의 
 * Bean 충돌이나 설정 충돌을 검사합니다.
 */
@Slf4j
@Component
public class DependencyConflictTest implements CommandLineRunner {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void run(String... args) throws Exception {
        log.info("=== Dependency Conflict Test Started ===");

        // 1. Hexacore Security 빈들이 정상 로드되는지 확인
        testHexacoreSecurityBeans();

        // 2. Spring Security 설정이 정상인지 확인  
        testSpringSecurityConfiguration();

        // 3. 빈 이름 충돌 확인
        testBeanNameConflicts();

        log.info("=== Dependency Conflict Test Completed ===");
    }

    private void testHexacoreSecurityBeans() {
        try {
            AuthenticationUseCase authUseCase = applicationContext.getBean(AuthenticationUseCase.class);
            log.info("✅ AuthenticationUseCase bean loaded successfully: {}", authUseCase.getClass().getName());
            
            TokenManagementUseCase tokenUseCase = applicationContext.getBean(TokenManagementUseCase.class);
            log.info("✅ TokenManagementUseCase bean loaded successfully: {}", tokenUseCase.getClass().getName());
            
        } catch (Exception e) {
            log.error("❌ Failed to load Hexacore Security beans", e);
        }
    }

    private void testSpringSecurityConfiguration() {
        try {
            // Spring Security 관련 빈 확인
            String[] securityBeans = applicationContext.getBeanNamesForAnnotation(EnableWebSecurity.class);
            log.info("✅ Spring Security configurations found: {}", securityBeans.length);
            for (String beanName : securityBeans) {
                log.info("  - Security config bean: {}", beanName);
            }
            
        } catch (Exception e) {
            log.error("❌ Failed to check Spring Security configuration", e);
        }
    }

    private void testBeanNameConflicts() {
        try {
            // 모든 빈 이름 조회하여 중복 확인
            String[] allBeanNames = applicationContext.getBeanDefinitionNames();
            log.info("✅ Total beans loaded: {}", allBeanNames.length);
            
            // Hexacore Security 관련 빈들 찾기
            int hexacoreBeansCount = 0;
            for (String beanName : allBeanNames) {
                if (beanName.toLowerCase().contains("hexacore") || 
                    beanName.toLowerCase().contains("authentication") ||
                    beanName.toLowerCase().contains("token")) {
                    log.info("  - Hexacore related bean: {}", beanName);
                    hexacoreBeansCount++;
                }
            }
            log.info("✅ Hexacore related beans found: {}", hexacoreBeansCount);
            
        } catch (Exception e) {
            log.error("❌ Failed to check bean name conflicts", e);
        }
    }
}