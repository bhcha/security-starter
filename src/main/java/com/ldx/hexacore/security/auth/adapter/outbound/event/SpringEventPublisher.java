package com.ldx.hexacore.security.auth.adapter.outbound.event;

import com.ldx.hexacore.security.auth.application.command.port.out.EventPublisher;
import com.ldx.hexacore.security.auth.domain.event.DomainEvent;
import com.ldx.hexacore.security.util.ValidationMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Spring의 ApplicationEventPublisher를 사용하는 이벤트 발행 어댑터.
 * 도메인 이벤트를 Spring 이벤트 시스템으로 전파합니다.
 *
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
class SpringEventPublisher implements EventPublisher {
    
    private final ApplicationEventPublisher applicationEventPublisher;
    
    @Override
    public void publish(DomainEvent event) {
        if (event == null) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNull("Event"));
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
            throw new IllegalArgumentException(ValidationMessages.cannotBeNullOrEmpty("Events"));
        }
        
        log.debug("Publishing {} domain events", events.length);
        
        for (DomainEvent event : events) {
            if (event == null) {
                throw new IllegalArgumentException(ValidationMessages.cannotBeNull("Event in array"));
            }
            publish(event);
        }
        
        log.info("All {} domain events published successfully", events.length);
    }
}