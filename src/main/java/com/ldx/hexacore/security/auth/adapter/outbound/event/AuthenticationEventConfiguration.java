package com.ldx.hexacore.security.auth.adapter.outbound.event;

import com.ldx.hexacore.security.auth.application.command.port.out.EventPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Authentication 모듈의 Event Publisher 설정 클래스
 */
@Configuration
public class AuthenticationEventConfiguration {

    /**
     * Spring 환경에서 ApplicationEventPublisher를 사용한 구현체
     */
    @Bean
    @ConditionalOnClass(ApplicationEventPublisher.class)
    @ConditionalOnMissingBean
    public EventPublisher springEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        return new SpringEventPublisher(applicationEventPublisher);
    }

    /**
     * ApplicationEventPublisher가 없는 환경에서 기본 No-Op 구현체
     */
    @Bean
    @ConditionalOnMissingBean
    public EventPublisher noOpEventPublisher() {
        return new NoOpEventPublisher();
    }
}