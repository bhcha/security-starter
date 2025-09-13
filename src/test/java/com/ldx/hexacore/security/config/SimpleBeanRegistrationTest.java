package com.ldx.hexacore.security.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 간단한 Bean 등록 검증 테스트
 */
@SpringBootTest
@TestPropertySource(properties = {
    "security-starter.enabled=true",
    "security-starter.jwt-toggle.enabled=true",
    "security-starter.token-provider.jwt.enabled=true",
    "security-starter.token-provider.jwt.secret=test-secret-key-for-verification-purpose-only",
    "spring.main.allow-bean-definition-overriding=true"
})
class SimpleBeanRegistrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void 기본_security_설정이_활성화되어야_함() {
        // Given & When & Then
        assertThat(applicationContext).isNotNull();

        // SecurityStarterAutoConfiguration Bean이 등록되었는지 확인
        boolean hasSecurityConfig = applicationContext.containsBean("securityStarterAutoConfiguration");

        System.out.println("🔍 Bean 등록 상태:");
        System.out.println("  - securityStarterAutoConfiguration: " + hasSecurityConfig);

        // 등록된 모든 Bean 이름 출력 (security 관련만)
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        System.out.println("🔍 Security 관련 Bean들:");
        for (String beanName : beanNames) {
            if (beanName.toLowerCase().contains("security") ||
                beanName.toLowerCase().contains("hexacore")) {
                System.out.println("  - " + beanName);
            }
        }

        // 기본적인 검증: ApplicationContext가 정상적으로 로드되었다면 성공
        assertThat(hasSecurityConfig).isTrue();
    }

    @Configuration
    @EnableAutoConfiguration
    static class TestConfiguration {
        // Test configuration
    }
}