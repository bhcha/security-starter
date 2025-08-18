package com.dx.hexacore.security.auth.adapter.outbound.persistence;

import com.dx.hexacore.security.auth.adapter.outbound.persistence.repository.AuthenticationJpaRepository;
import com.dx.hexacore.security.auth.application.command.port.out.AuthenticationRepository;
import com.dx.hexacore.security.auth.application.query.port.out.LoadAuthenticationQueryPort;
import com.dx.hexacore.security.auth.application.query.port.out.LoadTokenInfoQueryPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Authentication 모듈의 Persistence 어댑터 설정 클래스
 */
@Configuration
@ConditionalOnClass(JpaRepository.class)
public class AuthenticationPersistenceConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AuthenticationJpaMapper authenticationJpaMapper() {
        return new AuthenticationJpaMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthenticationJpaAdapter authenticationJpaAdapter(
            AuthenticationJpaRepository jpaRepository,
            AuthenticationJpaMapper mapper) {
        return new AuthenticationJpaAdapter(jpaRepository, mapper);
    }

    @Bean
    @Primary  // JPA 구현체를 우선 선택
    @ConditionalOnMissingBean
    public AuthenticationRepository authenticationRepository(AuthenticationJpaAdapter adapter) {
        return adapter;
    }

    @Bean("jpaLoadAuthenticationQueryPort")
    @ConditionalOnMissingBean(LoadAuthenticationQueryPort.class)
    public LoadAuthenticationQueryPort loadAuthenticationQueryPort(AuthenticationJpaAdapter adapter) {
        return adapter;
    }

    @Bean
    @Primary  // JPA 구현체를 우선 선택
    @ConditionalOnMissingBean
    public LoadTokenInfoQueryPort loadTokenInfoQueryPort(AuthenticationJpaAdapter adapter) {
        return adapter;
    }
}