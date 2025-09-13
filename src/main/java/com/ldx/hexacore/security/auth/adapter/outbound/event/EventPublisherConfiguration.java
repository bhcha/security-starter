package com.ldx.hexacore.security.auth.adapter.outbound.event;

import com.ldx.hexacore.security.auth.application.command.port.out.EventPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * EventPublisher 구현체를 Bean으로 등록하는 Configuration.
 * Package-private 구현체를 Spring Bean으로 노출합니다.
 */
@Configuration
public class EventPublisherConfiguration {

    @Bean
    @ConditionalOnMissingBean(EventPublisher.class)
    public EventPublisher eventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        return new SpringEventPublisher(applicationEventPublisher);
    }
}