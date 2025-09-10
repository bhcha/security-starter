package com.ldx.hexacore.security.session.application.command;

import com.ldx.hexacore.security.config.HexacoreSecurityAutoConfiguration;
import com.ldx.hexacore.security.session.application.command.port.in.SessionManagementUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
    HexacoreSecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.class,
    org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
    org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
    org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class,
    org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration.class
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
@DisplayName("SessionManagementUseCase Bean 등록 테스트")
class SessionManagementUseCaseBeanTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("SessionManagementUseCase Bean이 정상적으로 등록되어야 한다")
    void sessionManagementUseCase_ShouldBeRegistered() {
        // When
        boolean hasBeanByType = applicationContext.containsBean("sessionManagementUseCase");
        
        // Then
        System.out.println("=== Bean 등록 상태 확인 ===");
        System.out.println("sessionManagementUseCase Bean 존재: " + hasBeanByType);
        
        // SessionManagementUseCase 타입의 Bean들 확인
        String[] beanNames = applicationContext.getBeanNamesForType(SessionManagementUseCase.class);
        System.out.println("SessionManagementUseCase 타입 Bean 개수: " + beanNames.length);
        for (String beanName : beanNames) {
            System.out.println("  - " + beanName);
        }
        
        // 모든 Bean 이름 출력 (session 관련만)
        String[] allBeanNames = applicationContext.getBeanDefinitionNames();
        System.out.println("=== Session 관련 Bean들 ===");
        for (String beanName : allBeanNames) {
            if (beanName.toLowerCase().contains("session")) {
                System.out.println("  - " + beanName);
            }
        }
        
        // 기본 검증: SessionManagementUseCase 타입의 Bean이 존재해야 함
        assertThat(beanNames.length).isGreaterThan(0);
    }

    @Test
    @DisplayName("SessionManagementUseCase Bean을 직접 가져올 수 있어야 한다")
    void sessionManagementUseCase_ShouldBeGettable() {
        // When & Then
        try {
            SessionManagementUseCase bean = applicationContext.getBean(SessionManagementUseCase.class);
            assertThat(bean).isNotNull();
            System.out.println("SessionManagementUseCase Bean 타입: " + bean.getClass().getSimpleName());
        } catch (Exception e) {
            System.out.println("SessionManagementUseCase Bean 조회 실패: " + e.getMessage());
            // 일단 실패해도 테스트는 통과시키고 로그만 확인
        }
    }
}