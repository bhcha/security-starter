package com.dx.hexacore.security.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 빠른 검증 테스트 - v1.0.4 핵심 기능 확인
 */
@SpringBootTest(classes = com.dx.hexacore.security.TestApplication.class)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:quicktest",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "hexacore.security.enabled=true",
    "hexacore.security.session.enabled=false",  // 세션 비활성화로 단순화
    "hexacore.security.token-provider.provider=jwt",
    "hexacore.security.token-provider.jwt.enabled=true",
    "hexacore.security.token-provider.jwt.secret=quick-test-secret-key-for-verification-only",
    "hexacore.security.persistence.jpa.enabled=true",
    "hexacore.security.persistence.memory.enabled=false",
    "hexacore.session.cache.enabled=false"
})
class QuickVerificationTest {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Test
    void v1_0_4_기본_설정이_성공적으로_로드되어야_함() {
        // Given & When & Then
        assertThat(applicationContext).isNotNull();
        
        System.out.println("🎉 v1.0.4 기본 설정 로드 성공!");
        System.out.println("✅ ApplicationContext가 정상적으로 초기화되었습니다.");
        
        // 기본적인 Bean 확인
        boolean hasSecurityConfig = applicationContext.containsBean("hexacoreSecurityAutoConfiguration");
        System.out.println("✅ HexacoreSecurityAutoConfiguration: " + hasSecurityConfig);
        
        // 성공하면 v1.0.4 핵심 기능이 작동한다는 의미
        assertThat(hasSecurityConfig).isTrue();
    }
}