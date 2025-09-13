package com.ldx.hexacore.security.debug;

import com.ldx.hexacore.security.config.SecurityStarterAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Bean loading debug test
 */
@SpringBootTest(classes = {
    SecurityStarterAutoConfiguration.class,
    BeanDebugTest.TestConfig.class,
    org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.class
})
@TestPropertySource(properties = {
    "security-starter.enabled=true",
    "security-starter.token-provider.provider=jwt",
    "security-starter.token-provider.jwt.enabled=true",
    "security-starter.token-provider.jwt.secret=test-secret-key-for-verification-purpose-only-32chars"
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
                beanName.toLowerCase().contains("authentication") ||
                beanName.toLowerCase().contains("token")) {
                try {
                    Object bean = applicationContext.getBean(beanName);
                    System.out.println(beanName + " : " + bean.getClass().getName());
                } catch (Exception e) {
                    System.out.println(beanName + " : ERROR - " + e.getMessage());
                }
            }
        }
    }

    @Configuration
    @EnableAutoConfiguration
    static class TestConfig {
    }
}