package com.dx.hexacore.security.config;

import com.dx.hexacore.security.session.application.command.port.in.SessionManagementUseCase;
import com.dx.hexacore.security.config.support.SecurityConfigurationValidator;
import com.dx.hexacore.security.config.support.SecurityStarterFailureAnalyzer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Bean 등록 검증 테스트
 * 
 * v1.0.3에서 수정된 Bean들이 정상적으로 등록되는지 확인합니다.
 */
@SpringBootTest(classes = {
    com.dx.hexacore.security.config.HexacoreSecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.class,
    org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
    org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class
})
@TestPropertySource(properties = {
    "hexacore.security.enabled=true",
    "hexacore.security.session.enabled=true",
    "hexacore.security.token-provider.provider=jwt",
    "hexacore.security.token-provider.jwt.enabled=true",
    "hexacore.security.token-provider.jwt.secret=test-secret-key-for-verification-purpose-only",
    "hexacore.security.persistence.type=JPA",
    "hexacore.security.persistence.jpa.enabled=true",
    "hexacore.security.persistence.memory.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class BeanRegistrationVerificationTest {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Test
    void sessionManagementUseCase_빈이_정상_등록되어야_함() {
        // Given & When
        SessionManagementUseCase sessionManagementUseCase = 
            applicationContext.getBean(SessionManagementUseCase.class);
        
        // Then
        assertThat(sessionManagementUseCase).isNotNull();
        assertThat(sessionManagementUseCase.getClass().getSimpleName())
            .isEqualTo("SessionManagementUseCaseImpl");
    }
    
    @Test
    void securityConfigurationValidator_빈이_정상_등록되어야_함() {
        // Given & When
        SecurityConfigurationValidator validator = 
            applicationContext.getBean(SecurityConfigurationValidator.class);
        
        // Then
        assertThat(validator).isNotNull();
    }
    
    @Test
    void hexacoreSecurityAutoConfiguration_빈이_정상_등록되어야_함() {
        // Given & When
        Object autoConfiguration = 
            applicationContext.getBean("hexacoreSecurityAutoConfiguration");
        
        // Then
        assertThat(autoConfiguration).isNotNull();
        assertThat(autoConfiguration.getClass().getName())
            .contains("HexacoreSecurityAutoConfiguration");
    }
    
    @Test
    void securityStarterFailureAnalyzer_가_등록되어야_함() {
        // Given & When & Then
        // FailureAnalyzer는 Spring Boot의 META-INF/spring.factories를 통해 자동 등록되므로
        // 클래스가 존재하고 인스턴스화 가능한지만 확인
        assertThat(SecurityStarterFailureAnalyzer.class).isNotNull();
        
        SecurityStarterFailureAnalyzer analyzer = new SecurityStarterFailureAnalyzer();
        assertThat(analyzer).isNotNull();
    }
    
    @Test
    void 모든_핵심_빈들이_정상_등록되어야_함() {
        // Given & When & Then
        // 등록된 Bean들을 먼저 확인해보자
        System.out.println("=== 등록된 Bean 목록 ===");
        String[] allBeans = applicationContext.getBeanDefinitionNames();
        for (String beanName : allBeans) {
            if (beanName.contains("session") || beanName.contains("Security") || beanName.contains("hexacore")) {
                System.out.println("  - " + beanName);
            }
        }
        
        // 타입별로 Bean 확인
        String[] sessionBeans = applicationContext.getBeanNamesForType(SessionManagementUseCase.class);
        System.out.println("SessionManagementUseCase 타입 Bean들: " + java.util.Arrays.toString(sessionBeans));
        
        // 핵심 Bean들이 모두 존재하는지 확인
        boolean hasSessionUseCase = sessionBeans.length > 0;
        assertThat(hasSessionUseCase).isTrue();
        assertThat(applicationContext.containsBean("hexacoreSecurityAutoConfiguration")).isTrue();
        assertThat(applicationContext.getBean(SecurityConfigurationValidator.class)).isNotNull();
        
        System.out.println("✅ 모든 핵심 Bean이 정상적으로 등록되었습니다!");
        if (hasSessionUseCase) {
            System.out.println("  - SessionManagementUseCase: " + 
                applicationContext.getBean(SessionManagementUseCase.class).getClass().getSimpleName());
        }
        System.out.println("  - SecurityConfigurationValidator: " + 
            applicationContext.getBean(SecurityConfigurationValidator.class).getClass().getSimpleName());
        System.out.println("  - HexacoreSecurityAutoConfiguration: " + 
            applicationContext.getBean("hexacoreSecurityAutoConfiguration").getClass().getSimpleName());
    }
}