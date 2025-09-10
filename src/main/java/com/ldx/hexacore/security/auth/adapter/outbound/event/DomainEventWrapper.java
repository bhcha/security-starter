package com.ldx.hexacore.security.auth.adapter.outbound.event;

import com.ldx.hexacore.security.auth.domain.event.DomainEvent;
import com.ldx.hexacore.security.util.ValidationMessages;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 도메인 이벤트를 Spring ApplicationEvent로 래핑하는 클래스.
 * Spring 이벤트 시스템과의 통합을 위해 사용됩니다.
 * 
 * @since 1.0.0
 */
public class DomainEventWrapper extends ApplicationEvent {
    
    private final DomainEvent domainEvent;
    private final LocalDateTime publishedAt;
    private final UUID wrapperEventId;
    
    public DomainEventWrapper(DomainEvent domainEvent) {
        super(domainEvent != null ? domainEvent : new Object());
        if (domainEvent == null) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNull("Domain event"));
        }
        this.domainEvent = domainEvent;
        this.publishedAt = LocalDateTime.now();
        this.wrapperEventId = UUID.randomUUID();
    }
    
    /**
     * 래핑된 도메인 이벤트를 반환합니다.
     * 
     * @return 도메인 이벤트
     */
    public DomainEvent getDomainEvent() {
        return domainEvent;
    }
    
    /**
     * 이벤트가 발행된 시간을 반환합니다.
     * 
     * @return 발행 시간
     */
    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
    
    /**
     * 래퍼 이벤트의 고유 ID를 반환합니다.
     * 
     * @return 래퍼 이벤트 ID
     */
    public UUID getWrapperEventId() {
        return wrapperEventId;
    }
    
    /**
     * 도메인 이벤트의 타입을 반환합니다.
     * 
     * @return 도메인 이벤트 클래스 이름
     */
    public String getEventType() {
        return domainEvent.getClass().getSimpleName();
    }
    
    /**
     * 도메인 이벤트의 ID를 반환합니다.
     * 
     * @return 도메인 이벤트 ID
     */
    public UUID getDomainEventId() {
        return domainEvent.getEventId();
    }
    
    /**
     * 도메인 이벤트가 발생한 시간을 반환합니다.
     * 
     * @return 도메인 이벤트 발생 시간
     */
    public LocalDateTime getDomainEventOccurredOn() {
        return domainEvent.getOccurredOn();
    }
    
    @Override
    public String toString() {
        return String.format("DomainEventWrapper{eventType='%s', domainEventId=%s, publishedAt=%s}",
                getEventType(), getDomainEventId(), publishedAt);
    }
}