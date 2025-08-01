package com.dx.hexacore.security.auth.adapter.outbound.event;

import com.dx.hexacore.security.auth.adapter.outbound.event.DomainEventWrapper;
import com.dx.hexacore.security.auth.domain.event.AuthenticationAttempted;
import com.dx.hexacore.security.auth.domain.event.DomainEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class DomainEventWrapperTest {
    
    @Test
    @DisplayName("DomainEventWrapper 생성 - 성공")
    void createWrapper_Success() {
        // Given
        UUID aggregateId = UUID.randomUUID();
        AuthenticationAttempted domainEvent = AuthenticationAttempted.of(
                aggregateId, 
                "testuser", 
                LocalDateTime.now()
        );
        
        // When
        DomainEventWrapper wrapper = new DomainEventWrapper(domainEvent);
        
        // Then
        assertThat(wrapper).isNotNull();
        assertThat(wrapper.getDomainEvent()).isEqualTo(domainEvent);
        assertThat(wrapper.getEventType()).isEqualTo("AuthenticationAttempted");
        assertThat(wrapper.getDomainEventId()).isEqualTo(domainEvent.getEventId());
        assertThat(wrapper.getDomainEventOccurredOn()).isEqualTo(domainEvent.getOccurredOn());
        assertThat(wrapper.getPublishedAt()).isNotNull();
        assertThat(wrapper.getWrapperEventId()).isNotNull();
        assertThat(wrapper.getSource()).isEqualTo(domainEvent);
    }
    
    @Test
    @DisplayName("DomainEventWrapper 생성 - null 이벤트로 생성 시 예외")
    void createWrapper_NullEvent() {
        // When & Then
        assertThatThrownBy(() -> new DomainEventWrapper(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Domain event cannot be null");
    }
    
    @Test
    @DisplayName("toString 메서드 동작 확인")
    void toString_Format() {
        // Given
        UUID aggregateId = UUID.randomUUID();
        DomainEvent domainEvent = AuthenticationAttempted.of(
                aggregateId, 
                "testuser", 
                LocalDateTime.now()
        );
        
        // When
        DomainEventWrapper wrapper = new DomainEventWrapper(domainEvent);
        String result = wrapper.toString();
        
        // Then
        assertThat(result).contains("DomainEventWrapper");
        assertThat(result).contains("eventType='AuthenticationAttempted'");
        assertThat(result).contains("domainEventId=" + domainEvent.getEventId());
        assertThat(result).contains("publishedAt=");
    }
    
    @Test
    @DisplayName("래퍼 생성 시 고유 ID 생성 확인")
    void createWrapper_UniqueIds() {
        // Given
        DomainEvent domainEvent = AuthenticationAttempted.of(
                UUID.randomUUID(), 
                "testuser", 
                LocalDateTime.now()
        );
        
        // When
        DomainEventWrapper wrapper1 = new DomainEventWrapper(domainEvent);
        DomainEventWrapper wrapper2 = new DomainEventWrapper(domainEvent);
        
        // Then
        assertThat(wrapper1.getWrapperEventId()).isNotEqualTo(wrapper2.getWrapperEventId());
        assertThat(wrapper1.getDomainEventId()).isEqualTo(wrapper2.getDomainEventId());
    }
    
    @Test
    @DisplayName("발행 시간 기록 확인")
    void createWrapper_PublishedAtTimestamp() {
        // Given
        LocalDateTime beforeCreation = LocalDateTime.now();
        DomainEvent domainEvent = AuthenticationAttempted.of(
                UUID.randomUUID(), 
                "testuser", 
                LocalDateTime.now()
        );
        
        // When
        DomainEventWrapper wrapper = new DomainEventWrapper(domainEvent);
        LocalDateTime afterCreation = LocalDateTime.now();
        
        // Then
        assertThat(wrapper.getPublishedAt()).isAfterOrEqualTo(beforeCreation);
        assertThat(wrapper.getPublishedAt()).isBeforeOrEqualTo(afterCreation);
    }
}