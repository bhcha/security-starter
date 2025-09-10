package com.ldx.hexacore.security.session.adapter.outbound.event;

import com.ldx.hexacore.security.auth.domain.event.DomainEvent;
import com.ldx.hexacore.security.session.application.command.port.out.SessionEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Spring의 ApplicationEventPublisher를 사용한 세션 이벤트 발행 어댑터
 */
@Component
class SpringSessionEventPublisher implements SessionEventPublisher {
    
    private final ApplicationEventPublisher applicationEventPublisher;
    
    public SpringSessionEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
    
    @Override
    public void publish(DomainEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
    
    @Override
    public void publishAll(List<DomainEvent> events) {
        events.forEach(this::publish);
    }
}