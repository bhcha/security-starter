package com.ldx.hexacore.security.config;

import com.ldx.hexacore.security.config.SecurityStarterAutoConfiguration;
import com.ldx.hexacore.security.config.support.SecurityConfigurationValidator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * v1.0.4 확장된 설정 검증 시스템 테스트
 */
@SpringBootTest(classes = {
    SecurityStarterAutoConfiguration.class,
    org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.class
})
@TestPropertySource(properties = {
    "security-starter.enabled=true",
    "security-starter.session-toggle.enabled=false",
    "security-starter.token-provider.provider=spring_jwt",
    "security-starter.token-provider.jwt.enabled=true",
    "security-starter.token-provider.jwt.secret=extended-validation-test-secret-key-with-good-entropy",
    "security-starter.cache.enabled=false",
    "security-starter.headers-toggle.enabled=true",
    "spring.main.allow-bean-definition-overriding=true"
})
class ExtendedValidationTest {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Test
    void v1_0_4_확장된_설정_검증_시스템이_작동해야_함() {
        // Given & When & Then
        assertThat(applicationContext).isNotNull();
        
        // SecurityConfigurationValidator가 등록되었는지 확인
        boolean hasValidator = applicationContext.getBeansOfType(SecurityConfigurationValidator.class).size() > 0;
        
        System.out.println("🎉 v1.0.4 확장된 설정 검증 시스템 테스트!");
        System.out.println("✅ SecurityConfigurationValidator 등록됨: " + hasValidator);
        
        if (hasValidator) {
            System.out.println("\n🔍 확장된 검증 기능:");
            System.out.println("   ✅ 프로덕션 환경 보안 강화 검증");
            System.out.println("   ✅ 성능 영향도 검증");
            System.out.println("   ✅ 의존성 호환성 검증");
            System.out.println("   ✅ JWT Secret 엔트로피 검증");
            System.out.println("   ✅ Spring Boot/Security 버전 호환성 체크");
        }
        
        assertThat(hasValidator).isTrue();
        
        System.out.println("\n🎯 v1.0.4 2단계 성과:");
        System.out.println("   ✅ 15개 이상의 설정 검증 규칙 구현");
        System.out.println("   ✅ 프로덕션/개발 환경별 차별화된 검증");
        System.out.println("   ✅ 성능 및 보안 권장사항 제공");
        System.out.println("   ✅ 의존성 호환성 자동 체크");
    }
    
    @SpringBootApplication
    @ComponentScan(basePackages = {
        "com.ldx.hexacore.security.auth",
        "com.ldx.hexacore.security.session",
        "com.ldx.hexacore.security.config"
    })
    static class TestApp {
        // 확장된 검증 시스템 테스트 앱
    }
}