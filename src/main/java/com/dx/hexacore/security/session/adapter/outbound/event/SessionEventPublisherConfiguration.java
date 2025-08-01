package com.dx.hexacore.security.session.adapter.outbound.event;

import com.dx.hexacore.security.session.application.command.port.out.SessionEventPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Session 모듈의 Event Publisher 설정 클래스
 */
@Configuration
public class SessionEventPublisherConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SessionEventPublisher sessionEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        return new SpringSessionEventPublisher(applicationEventPublisher);
    }
}