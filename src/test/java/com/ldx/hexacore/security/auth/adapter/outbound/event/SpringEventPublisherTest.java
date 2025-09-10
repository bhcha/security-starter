package com.ldx.hexacore.security.auth.adapter.outbound.event;

import com.ldx.hexacore.security.auth.domain.event.AuthenticationSucceeded;
import com.ldx.hexacore.security.auth.domain.event.AuthenticationFailed;
import com.ldx.hexacore.security.auth.domain.event.DomainEvent;
import com.ldx.hexacore.security.auth.domain.vo.Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpringEventPublisherTest {
    
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    
    private SpringEventPublisher eventPublisher;
    
    @BeforeEach
    void setUp() {
        eventPublisher = new SpringEventPublisher(applicationEventPublisher);
    }
    
    @Test
    @DisplayName("AuthenticationSucceeded 이벤트 발행 - 성공")
    void publish_AuthenticationSucceeded() {
        // Given
        UUID aggregateId = UUID.randomUUID();
        Token token = Token.of("access-token", "refresh-token", 3600L);
        AuthenticationSucceeded event = AuthenticationSucceeded.of(aggregateId, token, LocalDateTime.now());
        
        ArgumentCaptor<DomainEventWrapper> captor = ArgumentCaptor.forClass(DomainEventWrapper.class);
        
        // When
        eventPublisher.publish(event);
        
        // Then
        verify(applicationEventPublisher).publishEvent(captor.capture());
        
        DomainEventWrapper wrapper = captor.getValue();
        assertThat(wrapper).isNotNull();
        assertThat(wrapper.getDomainEvent()).isEqualTo(event);
        assertThat(wrapper.getEventType()).isEqualTo("AuthenticationSucceeded");
        assertThat(wrapper.getDomainEventId()).isEqualTo(event.getEventId());
    }
    
    @Test
    @DisplayName("AuthenticationFailed 이벤트 발행 - 성공")
    void publish_AuthenticationFailed() {
        // Given
        UUID aggregateId = UUID.randomUUID();
        AuthenticationFailed event = AuthenticationFailed.of(aggregateId, "Invalid credentials", LocalDateTime.now());
        
        ArgumentCaptor<DomainEventWrapper> captor = ArgumentCaptor.forClass(DomainEventWrapper.class);
        
        // When
        eventPublisher.publish(event);
        
        // Then
        verify(applicationEventPublisher).publishEvent(captor.capture());
        
        DomainEventWrapper wrapper = captor.getValue();
        assertThat(wrapper).isNotNull();
        assertThat(wrapper.getDomainEvent()).isEqualTo(event);
        assertThat(wrapper.getEventType()).isEqualTo("AuthenticationFailed");
    }
    
    @Test
    @DisplayName("null 이벤트 발행 시도 - 예외 발생")
    void publish_NullEvent() {
        // When & Then
        assertThatThrownBy(() -> eventPublisher.publish(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Event cannot be null");
        
        verify(applicationEventPublisher, never()).publishEvent(any());
    }
    
    @Test
    @DisplayName("여러 이벤트 동시 발행 - 성공")
    void publishAll_MultipleEvents() {
        // Given
        UUID aggregateId = UUID.randomUUID();
        Token token = Token.of("access-token", "refresh-token", 3600L);
        
        DomainEvent event1 = AuthenticationSucceeded.of(aggregateId, token, LocalDateTime.now());
        DomainEvent event2 = AuthenticationFailed.of(aggregateId, "Failed attempt", LocalDateTime.now());
        DomainEvent event3 = AuthenticationSucceeded.of(aggregateId, token, LocalDateTime.now());
        
        // When
        eventPublisher.publishAll(event1, event2, event3);
        
        // Then
        verify(applicationEventPublisher, times(3)).publishEvent(any(DomainEventWrapper.class));
    }
    
    @Test
    @DisplayName("빈 이벤트 배열 발행 - 예외 발생")
    void publishAll_EmptyArray() {
        // When & Then
        assertThatThrownBy(() -> eventPublisher.publishAll())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Events cannot be null or empty");
        
        verify(applicationEventPublisher, never()).publishEvent(any());
    }
    
    @Test
    @DisplayName("null 이벤트 배열 발행 - 예외 발생")
    void publishAll_NullArray() {
        // When & Then
        assertThatThrownBy(() -> eventPublisher.publishAll((DomainEvent[]) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Events cannot be null or empty");
        
        verify(applicationEventPublisher, never()).publishEvent(any());
    }
    
    @Test
    @DisplayName("배열 내 null 이벤트 포함 - 예외 발생")
    void publishAll_ArrayContainsNull() {
        // Given
        DomainEvent validEvent = AuthenticationSucceeded.of(
                UUID.randomUUID(), 
                Token.of("token", "refresh", 3600L), 
                LocalDateTime.now()
        );
        
        // When & Then
        assertThatThrownBy(() -> eventPublisher.publishAll(validEvent, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Event in array cannot be null");
        
        // 첫 번째 이벤트는 발행되었을 수 있음
        verify(applicationEventPublisher, atMost(1)).publishEvent(any());
    }
    
    @Test
    @DisplayName("이벤트 발행 중 예외 발생 시 전파")
    void publish_ExceptionPropagation() {
        // Given
        DomainEvent event = AuthenticationFailed.of(
                UUID.randomUUID(), 
                "Test failure", 
                LocalDateTime.now()
        );
        
        RuntimeException exception = new RuntimeException("Event bus error");
        doThrow(exception).when(applicationEventPublisher).publishEvent(any());
        
        // When & Then
        assertThatThrownBy(() -> eventPublisher.publish(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Event bus error");
    }
}