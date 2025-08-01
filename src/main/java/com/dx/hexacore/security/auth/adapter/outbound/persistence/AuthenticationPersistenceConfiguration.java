package com.dx.hexacore.security.auth.adapter.outbound.persistence;

import com.dx.hexacore.security.auth.adapter.outbound.persistence.repository.AuthenticationJpaRepository;
import com.dx.hexacore.security.auth.application.command.port.out.AuthenticationRepository;
import com.dx.hexacore.security.auth.application.query.port.out.LoadAuthenticationQueryPort;
import com.dx.hexacore.security.auth.application.query.port.out.LoadTokenInfoQueryPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Authentication 모듈의 Persistence 어댑터 설정 클래스
 */
@Configuration
public class AuthenticationPersistenceConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AuthenticationJpaMapper authenticationJpaMapper() {
        return new AuthenticationJpaMapper();
    }

    @Bean
    @ConditionalOnProperty(prefix = "hexacore.security.persistence", name = "jpa.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public AuthenticationJpaAdapter authenticationJpaAdapter(
            AuthenticationJpaRepository jpaRepository,
            AuthenticationJpaMapper mapper) {
        return new AuthenticationJpaAdapter(jpaRepository, mapper);
    }

    @Bean
    @ConditionalOnProperty(prefix = "hexacore.security.persistence", name = "jpa.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public AuthenticationRepository authenticationRepository(AuthenticationJpaAdapter adapter) {
        return adapter;
    }

    @Bean
    @ConditionalOnProperty(prefix = "hexacore.security.persistence", name = "jpa.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public LoadAuthenticationQueryPort loadAuthenticationQueryPort(AuthenticationJpaAdapter adapter) {
        return adapter;
    }

    @Bean
    @ConditionalOnProperty(prefix = "hexacore.security.persistence", name = "jpa.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public LoadTokenInfoQueryPort loadTokenInfoQueryPort(AuthenticationJpaAdapter adapter) {
        return adapter;
    }
}