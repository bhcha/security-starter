package com.dx.hexacore.security.session.adapter.outbound.persistence;

import com.dx.hexacore.security.session.adapter.outbound.persistence.repository.SessionJpaRepository;
import com.dx.hexacore.security.session.application.command.port.out.AuthenticationSessionRepository;
import com.dx.hexacore.security.session.application.query.port.out.LoadFailedAttemptsQueryPort;
import com.dx.hexacore.security.session.application.query.port.out.LoadSessionStatusQueryPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Session 모듈의 Persistence 어댑터 설정 클래스
 */
@Configuration
public class SessionPersistenceConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SessionMapper sessionMapper() {
        return new SessionMapper();
    }

    @Bean
    @ConditionalOnProperty(prefix = "hexacore.security.persistence", name = "jpa.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public SessionJpaAdapter sessionJpaAdapter(
            SessionJpaRepository jpaRepository,
            SessionMapper mapper) {
        return new SessionJpaAdapter(jpaRepository, mapper);
    }

    @Bean
    @ConditionalOnProperty(prefix = "hexacore.security.persistence", name = "jpa.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public AuthenticationSessionRepository authenticationSessionRepository(SessionJpaAdapter adapter) {
        return adapter;
    }

    @Bean
    @ConditionalOnProperty(prefix = "hexacore.security.persistence", name = "jpa.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public LoadSessionStatusQueryPort loadSessionStatusQueryPort(SessionJpaAdapter adapter) {
        return adapter;
    }

    @Bean
    @ConditionalOnProperty(prefix = "hexacore.security.persistence", name = "jpa.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public LoadFailedAttemptsQueryPort loadFailedAttemptsQueryPort(SessionJpaAdapter adapter) {
        return adapter;
    }
}