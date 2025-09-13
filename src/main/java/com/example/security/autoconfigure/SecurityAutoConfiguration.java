package com.example.security.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(SecurityManager.class)
@ConditionalOnProperty(prefix = "security.starter", name = "enabled", havingValue = "true", matchIfMissing = true) // Zero Configuration 지원
@EnableConfigurationProperties(SecurityProperties.class)
// ComponentScan 없음 - 좋은 패턴
public class SecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean // Parent First 원칙 준수
    public SecurityManager securityManager(SecurityProperties properties) {
        return new SecurityManager(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityService securityService() {
        return new SecurityService();
    }

    @Bean
    @ConditionalOnProperty(prefix = "security.starter", name = "auth.enabled") // 단일 조건 원칙 준수
    @ConditionalOnMissingBean
    public AuthenticationService authenticationService() {
        return new AuthenticationService();
    }
}

class SecurityManager {
    private final SecurityProperties properties;
    
    public SecurityManager(SecurityProperties properties) {
        this.properties = properties;
    }
}

class SecurityService {
}

class AuthenticationService {
}