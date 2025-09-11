package com.ldx.hexacore.security.session.application.command.handler;

import com.ldx.hexacore.security.session.application.command.port.in.CheckLockoutUseCase;
import com.ldx.hexacore.security.session.application.command.port.in.RecordAttemptUseCase;
import com.ldx.hexacore.security.session.application.command.port.in.SessionManagementUseCase;
import com.ldx.hexacore.security.session.application.command.port.in.UnlockAccountUseCase;
import com.ldx.hexacore.security.session.application.command.port.out.AuthenticationSessionRepository;
import com.ldx.hexacore.security.session.application.command.port.out.SessionEventPublisher;
import com.ldx.hexacore.security.config.properties.SecurityStarterProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Session Command Use Case 설정 클래스
 * 같은 패키지에 있는 package-private 구현체들을 Bean으로 등록합니다.
 */
@Configuration
public class SessionCommandConfiguration {

    // Command Use Cases
    @Bean
    @ConditionalOnMissingBean
    public RecordAttemptUseCase recordAttemptUseCase(
            AuthenticationSessionRepository repository,
            SessionEventPublisher eventPublisher,
            SecurityStarterProperties securityProperties) {
        return new RecordAttemptUseCaseImpl(repository, eventPublisher, securityProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public CheckLockoutUseCase checkLockoutUseCase(
            AuthenticationSessionRepository repository) {
        return new CheckLockoutUseCaseImpl(repository);
    }

    @Bean
    @ConditionalOnMissingBean
    public UnlockAccountUseCase unlockAccountUseCase(
            AuthenticationSessionRepository repository) {
        return new UnlockAccountUseCaseImpl(repository);
    }

    @Bean
    @ConditionalOnMissingBean
    public SessionManagementUseCase sessionManagementUseCase(
            CheckLockoutUseCase checkLockoutUseCase,
            RecordAttemptUseCase recordAttemptUseCase,
            UnlockAccountUseCase unlockAccountUseCase) {
        return new SessionManagementUseCaseImpl(checkLockoutUseCase, recordAttemptUseCase, unlockAccountUseCase);
    }
}