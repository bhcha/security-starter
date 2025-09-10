package com.dx.hexacore.security.debug;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;

/**
 * Bean loading debug test
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
class BeanDebugTest {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Test
    void listAllBeans() {
        System.out.println("=== All Bean Names ===");
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
            System.out.println(beanName + " : " + applicationContext.getBean(beanName).getClass().getName());
        }
        
        System.out.println("\n=== Security Related Beans ===");
        for (String beanName : beanNames) {
            if (beanName.toLowerCase().contains("security") || 
                beanName.toLowerCase().contains("session") ||
                beanName.toLowerCase().contains("authentication")) {
                try {
                    Object bean = applicationContext.getBean(beanName);
                    System.out.println(beanName + " : " + bean.getClass().getName());
                } catch (Exception e) {
                    System.out.println(beanName + " : ERROR - " + e.getMessage());
                }
            }
        }
        
        System.out.println("\n=== Repository Beans ===");
        for (String beanName : beanNames) {
            if (beanName.toLowerCase().contains("repository")) {
                try {
                    Object bean = applicationContext.getBean(beanName);
                    System.out.println(beanName + " : " + bean.getClass().getName());
                } catch (Exception e) {
                    System.out.println(beanName + " : ERROR - " + e.getMessage());
                }
            }
        }
    }
}