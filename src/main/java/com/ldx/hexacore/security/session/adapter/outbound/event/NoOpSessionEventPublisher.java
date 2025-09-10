package com.ldx.hexacore.security.session.adapter.outbound.event;

import com.ldx.hexacore.security.auth.domain.event.DomainEvent;
import com.ldx.hexacore.security.session.application.command.port.out.SessionEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 이벤트 발행을 하지 않는 No-Op 구현체.
 * 이벤트 처리가 필요하지 않은 환경에서 기본 구현체로 사용됩니다.
 * 
 * @since 1.0.0
 */
class NoOpSessionEventPublisher implements SessionEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(NoOpSessionEventPublisher.class);

    @Override
    public void publish(DomainEvent event) {
        logger.debug("No-Op SessionEventPublisher: Ignoring event {}", event.getClass().getSimpleName());
    }

    @Override
    public void publishAll(List<DomainEvent> events) {
        logger.debug("No-Op SessionEventPublisher: Ignoring {} events", events.size());
    }
}