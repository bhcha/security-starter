package com.ldx.hexacore.security.session.adapter.outbound.persistence;

import com.ldx.hexacore.security.session.adapter.outbound.persistence.repository.SessionJpaRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ldx.hexacore.security.config.properties.HexacoreSecurityProperties;

/**
 * Session 모듈의 Persistence 어댑터 설정 클래스
 */
@Configuration
@ConditionalOnClass(JpaRepository.class)
@Order(100)  // 기본 JPA 구현체를 먼저 등록
public class SessionPersistenceConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SessionMapper sessionMapper(HexacoreSecurityProperties securityProperties) {
        return new SessionMapper(securityProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public SessionJpaAdapter sessionJpaAdapter(
            SessionJpaRepository jpaRepository,
            SessionMapper mapper) {
        return new SessionJpaAdapter(jpaRepository, mapper);
    }

    // SessionJpaAdapter는 3개 인터페이스를 모두 구현하므로 하나의 Bean으로만 등록
    // Spring이 타입으로 자동 매핑하도록 함
}