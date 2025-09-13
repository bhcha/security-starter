package com.ldx.hexacore.security.config;

import com.ldx.hexacore.security.auth.application.command.port.in.AuthenticationUseCase;
import com.ldx.hexacore.security.auth.application.command.port.in.TokenManagementUseCase;
import com.ldx.hexacore.security.auth.application.command.port.out.EventPublisher;
import com.ldx.hexacore.security.auth.application.command.port.out.ExternalAuthProvider;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.ldx.hexacore.security.logging.SecurityRequestLogger;
import com.ldx.hexacore.security.logging.SecurityEventLogger;
import com.ldx.hexacore.security.config.support.SecurityConfigurationValidator;
import com.ldx.hexacore.security.config.properties.SecurityStarterProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * 테스트용 Spring Bean 설정.
 * 실제 구현체 대신 Mock을 제공하여 테스트 컨텍스트 로딩 문제를 해결합니다.
 *
 * @since 1.0.0
 */
@TestConfiguration
@EnableWebMvc
public class SecurityTestConfig {

    // Authentication Aggregate - Command Side
    @Bean
    @Primary
    public AuthenticationUseCase mockAuthenticationUseCase() {
        return Mockito.mock(AuthenticationUseCase.class);
    }

    @Bean
    @Primary
    public TokenManagementUseCase mockTokenManagementUseCase() {
        return Mockito.mock(TokenManagementUseCase.class);
    }

    // Authentication Aggregate - Outbound Ports
    @Bean
    @Primary
    public EventPublisher mockEventPublisher() {
        return Mockito.mock(EventPublisher.class);
    }

    @Bean
    @Primary
    public ExternalAuthProvider mockExternalAuthProvider() {
        return Mockito.mock(ExternalAuthProvider.class);
    }

    @Bean("testMockTokenProvider")
    @Primary
    public TokenProvider mockTokenProvider() {
        return Mockito.mock(TokenProvider.class);
    }

    // Additional beans for test infrastructure
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @Primary
    public SecurityRequestLogger mockSecurityRequestLogger() {
        return Mockito.mock(SecurityRequestLogger.class);
    }

    @Bean
    @Primary
    public SecurityEventLogger mockSecurityEventLogger() {
        return Mockito.mock(SecurityEventLogger.class);
    }

    @Bean
    @Primary
    public SecurityConfigurationValidator securityConfigurationValidator(SecurityStarterProperties properties) {
        return new SecurityConfigurationValidator(properties);
    }

    @Bean
    @Primary
    public SecurityStarterProperties SecurityStarterProperties() {
        return new SecurityStarterProperties();
    }
}