package com.dx.hexacore.security.config;

import com.dx.hexacore.security.auth.application.command.port.in.AuthenticationUseCase;
import com.dx.hexacore.security.auth.application.command.port.in.TokenManagementUseCase;
import com.dx.hexacore.security.auth.application.command.port.out.AuthenticationRepository;
import com.dx.hexacore.security.auth.application.command.port.out.EventPublisher;
import com.dx.hexacore.security.auth.application.command.port.out.ExternalAuthProvider;
import com.dx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.dx.hexacore.security.auth.application.query.port.in.GetAuthenticationUseCase;
import com.dx.hexacore.security.auth.application.query.port.in.GetTokenInfoUseCase;
import com.dx.hexacore.security.auth.application.query.port.out.LoadAuthenticationQueryPort;
import com.dx.hexacore.security.auth.application.query.port.out.LoadTokenInfoQueryPort;
import com.dx.hexacore.security.auth.adapter.outbound.persistence.repository.AuthenticationJpaRepository;
import com.dx.hexacore.security.session.application.command.port.in.CheckLockoutUseCase;
import com.dx.hexacore.security.session.application.command.port.in.RecordAttemptUseCase;
import com.dx.hexacore.security.session.application.command.port.in.UnlockAccountUseCase;
import com.dx.hexacore.security.session.application.command.port.out.AuthenticationSessionRepository;
import com.dx.hexacore.security.session.application.command.port.out.SessionEventPublisher;
import com.dx.hexacore.security.session.application.query.port.out.LoadFailedAttemptsQueryPort;
import com.dx.hexacore.security.session.application.query.port.out.LoadSessionStatusQueryPort;
import com.dx.hexacore.security.session.adapter.outbound.persistence.repository.SessionJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
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

    // Authentication Aggregate - Query Side
    @Bean
    @Primary
    public GetAuthenticationUseCase mockGetAuthenticationUseCase() {
        return Mockito.mock(GetAuthenticationUseCase.class);
    }

    @Bean
    @Primary
    public GetTokenInfoUseCase mockGetTokenInfoUseCase() {
        return Mockito.mock(GetTokenInfoUseCase.class);
    }

    // AuthenticationSession Aggregate - Command Side
    @Bean
    @Primary
    public CheckLockoutUseCase mockCheckLockoutUseCase() {
        return Mockito.mock(CheckLockoutUseCase.class);
    }

    @Bean
    @Primary
    public RecordAttemptUseCase mockRecordAttemptUseCase() {
        return Mockito.mock(RecordAttemptUseCase.class);
    }

    @Bean
    @Primary
    public UnlockAccountUseCase mockUnlockAccountUseCase() {
        return Mockito.mock(UnlockAccountUseCase.class);
    }

    // Authentication Aggregate - Outbound Ports
    @Bean
    @Primary
    public AuthenticationRepository mockAuthenticationRepository() {
        return Mockito.mock(AuthenticationRepository.class);
    }

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

    @Bean
    @Primary
    public LoadAuthenticationQueryPort mockLoadAuthenticationQueryPort() {
        return Mockito.mock(LoadAuthenticationQueryPort.class);
    }

    @Bean
    @Primary
    public LoadTokenInfoQueryPort mockLoadTokenInfoQueryPort() {
        return Mockito.mock(LoadTokenInfoQueryPort.class);
    }

    // AuthenticationSession Aggregate - Outbound Ports
    @Bean
    @Primary
    public AuthenticationSessionRepository mockAuthenticationSessionRepository() {
        return Mockito.mock(AuthenticationSessionRepository.class);
    }

    @Bean
    @Primary
    public SessionEventPublisher mockSessionEventPublisher() {
        return Mockito.mock(SessionEventPublisher.class);
    }

    @Bean
    @Primary
    public LoadSessionStatusQueryPort mockLoadSessionStatusQueryPort() {
        return Mockito.mock(LoadSessionStatusQueryPort.class);
    }

    @Bean
    @Primary
    public LoadFailedAttemptsQueryPort mockLoadFailedAttemptsQueryPort() {
        return Mockito.mock(LoadFailedAttemptsQueryPort.class);
    }

    // JPA Repositories (Adapter Layer - if needed)
    @Bean
    @Primary
    public AuthenticationJpaRepository mockAuthenticationJpaRepository() {
        return Mockito.mock(AuthenticationJpaRepository.class);
    }

    @Bean
    @Primary
    public SessionJpaRepository mockSessionJpaRepository() {
        return Mockito.mock(SessionJpaRepository.class);
    }

    // Additional beans for test infrastructure
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }


}