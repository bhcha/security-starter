package com.dx.hexacore.security.auth.adapter.outbound.event;

import com.dx.hexacore.security.auth.application.command.port.out.EventPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Authentication 모듈의 Event Publisher 설정 클래스
 */
@Configuration
public class AuthenticationEventConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EventPublisher eventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        return new SpringEventPublisher(applicationEventPublisher);
    }
}