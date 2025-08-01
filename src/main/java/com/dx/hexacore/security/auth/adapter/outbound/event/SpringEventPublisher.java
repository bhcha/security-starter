package com.dx.hexacore.security.auth.adapter.outbound.event;

import com.dx.hexacore.security.auth.application.command.port.out.EventPublisher;
import com.dx.hexacore.security.auth.domain.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Spring의 ApplicationEventPublisher를 사용하는 이벤트 발행 어댑터.
 * 도메인 이벤트를 Spring 이벤트 시스템으로 전파합니다.
 * 
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
class SpringEventPublisher implements EventPublisher {
    
    private final ApplicationEventPublisher applicationEventPublisher;
    
    @Override
    public void publish(DomainEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        
        log.debug("Publishing domain event: {} with id: {}", 
                event.getClass().getSimpleName(), event.getEventId());
        
        // 도메인 이벤트를 래핑하여 발행
        DomainEventWrapper wrapper = new DomainEventWrapper(event);
        applicationEventPublisher.publishEvent(wrapper);
        
        log.info("Domain event published successfully: {} at {}", 
                event.getClass().getSimpleName(), event.getOccurredOn());
    }
    
    @Override
    public void publishAll(DomainEvent... events) {
        if (events == null || events.length == 0) {
            throw new IllegalArgumentException("Events cannot be null or empty");
        }
        
        log.debug("Publishing {} domain events", events.length);
        
        for (DomainEvent event : events) {
            if (event == null) {
                throw new IllegalArgumentException("Event in array cannot be null");
            }
            publish(event);
        }
        
        log.info("All {} domain events published successfully", events.length);
    }
}