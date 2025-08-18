package com.dx.hexacore.security.session.adapter.outbound.persistence;

import com.dx.hexacore.security.session.adapter.outbound.persistence.repository.SessionJpaRepository;
import com.dx.hexacore.security.session.application.command.port.out.AuthenticationSessionRepository;
import com.dx.hexacore.security.session.application.query.port.out.LoadFailedAttemptsQueryPort;
import com.dx.hexacore.security.session.application.query.port.out.LoadSessionStatusQueryPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Session 모듈의 Persistence 어댑터 설정 클래스
 */
@Configuration
@ConditionalOnClass(JpaRepository.class)
@Order(100)  // 기본 JPA 구현체를 먼저 등록
public class SessionPersistenceConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SessionMapper sessionMapper() {
        return new SessionMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public SessionJpaAdapter sessionJpaAdapter(
            SessionJpaRepository jpaRepository,
            SessionMapper mapper) {
        return new SessionJpaAdapter(jpaRepository, mapper);
    }

    @Bean
    @Primary  // JPA 구현체를 우선 선택
    @ConditionalOnMissingBean
    public AuthenticationSessionRepository authenticationSessionRepository(SessionJpaAdapter adapter) {
        return adapter;
    }

    @Bean("sessionJpaQueryAdapter")
    @ConditionalOnMissingBean(name = "sessionJpaQueryAdapter")
    public LoadSessionStatusQueryPort loadSessionStatusQueryPort(SessionJpaAdapter adapter) {
        return adapter;
    }

    @Bean("sessionJpaFailedAttemptsAdapter")
    @ConditionalOnMissingBean(name = "sessionJpaFailedAttemptsAdapter")
    public LoadFailedAttemptsQueryPort loadFailedAttemptsQueryPort(SessionJpaAdapter adapter) {
        return adapter;
    }
}