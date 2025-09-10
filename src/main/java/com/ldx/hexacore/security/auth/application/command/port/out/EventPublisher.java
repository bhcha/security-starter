package com.ldx.hexacore.security.auth.application.command.port.out;

import com.ldx.hexacore.security.auth.domain.event.DomainEvent;

/**
 * 도메인 이벤트 발행을 담당하는 아웃바운드 포트.
 * 도메인에서 발생한 이벤트를 외부 시스템으로 전파합니다.
 * 
 * @since 1.0.0
 */
public interface EventPublisher {
    
    /**
     * 도메인 이벤트를 발행합니다.
     * 
     * @param event 발행할 도메인 이벤트
     * @throws IllegalArgumentException event가 null인 경우
     */
    void publish(DomainEvent event);
    
    /**
     * 여러 도메인 이벤트를 일괄 발행합니다.
     * 
     * @param events 발행할 도메인 이벤트들
     * @throws IllegalArgumentException events가 null이거나 비어있는 경우
     */
    void publishAll(DomainEvent... events);
}