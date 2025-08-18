package com.dx.hexacore.security.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Authentication만 활성화한 간단한 검증 테스트
 */
@SpringBootTest(classes = AuthOnlyVerificationTest.TestApp.class)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:authtest",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "hexacore.security.enabled=true",
    "hexacore.security.session.enabled=false",  // 세션 완전 비활성화
    "hexacore.security.token-provider.provider=jwt",
    "hexacore.security.token-provider.jwt.enabled=true",
    "hexacore.security.token-provider.jwt.secret=auth-only-test-secret-key",
    "hexacore.security.persistence.jpa.enabled=true",
    "hexacore.security.persistence.memory.enabled=false",
    "hexacore.session.cache.enabled=false"
})
class AuthOnlyVerificationTest {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Test
    void authentication_모듈만_성공적으로_로드되어야_함() {
        // Given & When & Then
        assertThat(applicationContext).isNotNull();
        
        System.out.println("🎉 Authentication 모듈 로드 성공!");
        System.out.println("✅ ApplicationContext가 정상적으로 초기화되었습니다.");
        
        // Authentication 관련 Bean 확인
        boolean hasTokenProvider = applicationContext.getBeansOfType(
            com.dx.hexacore.security.auth.application.command.port.out.TokenProvider.class
        ).size() > 0;
        
        System.out.println("✅ TokenProvider Bean 존재: " + hasTokenProvider);
        
        // 성공하면 핵심 기능이 작동한다는 의미
        assertThat(hasTokenProvider).isTrue();
    }
    
    @SpringBootApplication
    static class TestApp {
        // 최소한의 Spring Boot 애플리케이션
    }
}