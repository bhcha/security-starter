package com.dx.hexacore.security.session.adapter.outbound.event;

import com.dx.hexacore.security.session.application.command.port.out.SessionEventPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Session 모듈의 Event Publisher 설정 클래스
 */
@Configuration
public class SessionEventPublisherConfiguration {

    /**
     * Spring 환경에서 ApplicationEventPublisher를 사용한 구현체
     */
    @Bean
    @ConditionalOnClass(ApplicationEventPublisher.class)
    @ConditionalOnMissingBean
    public SessionEventPublisher springSessionEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        return new SpringSessionEventPublisher(applicationEventPublisher);
    }

    /**
     * ApplicationEventPublisher가 없는 환경에서 기본 No-Op 구현체
     */
    @Bean
    @ConditionalOnMissingBean
    public SessionEventPublisher noOpSessionEventPublisher() {
        return new NoOpSessionEventPublisher();
    }
}