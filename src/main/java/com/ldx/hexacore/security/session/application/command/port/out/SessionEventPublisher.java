package com.ldx.hexacore.security.session.application.command.port.out;

import com.ldx.hexacore.security.auth.domain.event.DomainEvent;

import java.util.List;

/**
 * 세션 도메인 이벤트 발행 포트
 * 
 * AuthenticationSession 애그리거트에서 발생하는 도메인 이벤트를 발행하는 아웃바운드 포트입니다.
 */
public interface SessionEventPublisher {
    
    /**
     * 단일 도메인 이벤트 발행
     * 
     * @param event 발행할 도메인 이벤트
     */
    void publish(DomainEvent event);
    
    /**
     * 다중 도메인 이벤트 발행
     * 
     * @param events 발행할 도메인 이벤트 목록
     */
    void publishAll(List<DomainEvent> events);
}