package com.ldx.hexacore.security.auth.application.command.handler;

import com.ldx.hexacore.security.auth.application.command.port.in.AuthenticationUseCase;
import com.ldx.hexacore.security.auth.application.command.port.in.TokenManagementUseCase;
import com.ldx.hexacore.security.auth.application.command.port.out.AuthenticationRepository;
import com.ldx.hexacore.security.auth.application.command.port.out.EventPublisher;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.ldx.hexacore.security.auth.domain.service.AuthenticationDomainService;
import com.ldx.hexacore.security.auth.domain.service.JwtPolicy;
import com.ldx.hexacore.security.auth.domain.service.SessionPolicy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Authentication Command Use Case 설정 클래스
 * 같은 패키지에 있는 package-private 구현체들을 Bean으로 등록합니다.
 */
@Configuration
public class AuthenticationCommandConfiguration {

    // Domain Services
    @Bean
    @ConditionalOnMissingBean
    public AuthenticationDomainService authenticationDomainService() {
        return new AuthenticationDomainService();
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtPolicy jwtPolicy() {
        return new JwtPolicy();
    }

    @Bean
    @ConditionalOnMissingBean
    public SessionPolicy sessionPolicy() {
        return new SessionPolicy();
    }

    // Application Services - Command Side
    @Bean
    @ConditionalOnMissingBean
    public AuthenticationUseCase authenticationUseCase(
            AuthenticationRepository authenticationRepository,
            TokenProvider tokenProvider,
            EventPublisher eventPublisher) {
        return new AuthenticateUseCaseImpl(
                authenticationRepository,
                tokenProvider,
                eventPublisher
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public TokenManagementUseCase tokenManagementUseCase(
            AuthenticationRepository authenticationRepository,
            TokenProvider tokenProvider,
            JwtPolicy jwtPolicy,
            SessionPolicy sessionPolicy) {
        return new TokenManagementUseCaseImpl(
                authenticationRepository,
                tokenProvider,
                jwtPolicy,
                sessionPolicy
        );
    }
}