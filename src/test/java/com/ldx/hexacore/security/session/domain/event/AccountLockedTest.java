package com.ldx.hexacore.security.session.domain.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AccountLocked Domain Event 테스트")
class AccountLockedTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("AccountLocked 이벤트 생성")
    void shouldCreateAccountLockedEvent() {
        // Given
        String sessionId = "session-123";
        String userId = "user123";
        String clientIp = "192.168.1.100";
        LocalDateTime lockedUntil = LocalDateTime.now().plusMinutes(30);
        int failedAttemptCount = 5;
        LocalDateTime occurredAt = LocalDateTime.now();

        // When
        AccountLocked event = AccountLocked.of(
            sessionId, userId, clientIp, lockedUntil, failedAttemptCount, occurredAt
        );

        // Then
        assertThat(event).isNotNull();
        assertThat(event.sessionId()).isEqualTo(sessionId);
        assertThat(event.userId()).isEqualTo(userId);
        assertThat(event.clientIp()).isEqualTo(clientIp);
        assertThat(event.lockedUntil()).isEqualTo(lockedUntil);
        assertThat(event.failedAttemptCount()).isEqualTo(failedAttemptCount);
        assertThat(event.occurredAt()).isEqualToIgnoringNanos(occurredAt);
    }

    // 직렬화 테스트는 DomainEvent 구조상 복잡하므로 별도 처리 필요

    @Test
    @DisplayName("널 파라미터로 생성 시도 - sessionId null")
    void shouldThrowExceptionWhenSessionIdIsNull() {
        // Given & When & Then
        assertThatThrownBy(() -> AccountLocked.of(
            null, "user123", "192.168.1.100", 
            LocalDateTime.now().plusMinutes(30), 5, LocalDateTime.now()
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("SessionId cannot be null or empty");
    }

    @Test
    @DisplayName("널 파라미터로 생성 시도 - userId null")
    void shouldThrowExceptionWhenUserIdIsNull() {
        // Given & When & Then
        assertThatThrownBy(() -> AccountLocked.of(
            "session-123", null, "192.168.1.100", 
            LocalDateTime.now().plusMinutes(30), 5, LocalDateTime.now()
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("UserId cannot be null or empty");
    }

    @Test
    @DisplayName("널 파라미터로 생성 시도 - clientIp null")
    void shouldThrowExceptionWhenClientIpIsNull() {
        // Given & When & Then
        assertThatThrownBy(() -> AccountLocked.of(
            "session-123", "user123", null, 
            LocalDateTime.now().plusMinutes(30), 5, LocalDateTime.now()
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("ClientIp cannot be null or empty");
    }

    @Test
    @DisplayName("널 파라미터로 생성 시도 - lockedUntil null")
    void shouldThrowExceptionWhenLockedUntilIsNull() {
        // Given & When & Then
        assertThatThrownBy(() -> AccountLocked.of(
            "session-123", "user123", "192.168.1.100", null, 5, LocalDateTime.now()
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Locked until time cannot be null");
    }

    @Test
    @DisplayName("널 파라미터로 생성 시도 - occurredAt null")
    void shouldThrowExceptionWhenOccurredAtIsNull() {
        // Given & When & Then
        assertThatThrownBy(() -> AccountLocked.of(
            "session-123", "user123", "192.168.1.100", 
            LocalDateTime.now().plusMinutes(30), 5, null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Occurred time cannot be null");
    }

    @Test
    @DisplayName("잘못된 실패 시도 횟수")
    void shouldThrowExceptionWhenFailedAttemptCountIsInvalid() {
        // Given & When & Then
        assertThatThrownBy(() -> AccountLocked.of(
            "session-123", "user123", "192.168.1.100", 
            LocalDateTime.now().plusMinutes(30), -1, LocalDateTime.now()
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Failed attempt count must be positive");
    }

    @Test
    @DisplayName("과거 시각으로 잠금 해제 시각 설정")
    void shouldThrowExceptionWhenLockedUntilIsInPast() {
        // Given & When & Then
        assertThatThrownBy(() -> AccountLocked.of(
            "session-123", "user123", "192.168.1.100", 
            LocalDateTime.now().minusMinutes(10), 5, LocalDateTime.now()
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Locked until time must be in the future");
    }

    @Test
    @DisplayName("빈 문자열 sessionId로 생성 시도")
    void shouldThrowExceptionWhenSessionIdIsEmpty() {
        // Given & When & Then
        assertThatThrownBy(() -> AccountLocked.of(
            "   ", "user123", "192.168.1.100", 
            LocalDateTime.now().plusMinutes(30), 5, LocalDateTime.now()
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("SessionId cannot be null or empty");
    }

    @Test
    @DisplayName("빈 문자열 userId로 생성 시도")
    void shouldThrowExceptionWhenUserIdIsEmpty() {
        // Given & When & Then
        assertThatThrownBy(() -> AccountLocked.of(
            "session-123", "   ", "192.168.1.100", 
            LocalDateTime.now().plusMinutes(30), 5, LocalDateTime.now()
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("UserId cannot be null or empty");
    }

    @Test
    @DisplayName("빈 문자열 clientIp로 생성 시도")
    void shouldThrowExceptionWhenClientIpIsEmpty() {
        // Given & When & Then
        assertThatThrownBy(() -> AccountLocked.of(
            "session-123", "user123", "   ", 
            LocalDateTime.now().plusMinutes(30), 5, LocalDateTime.now()
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("ClientIp cannot be null or empty");
    }

    @Test
    @DisplayName("0인 실패 시도 횟수")
    void shouldThrowExceptionWhenFailedAttemptCountIsZero() {
        // Given & When & Then
        assertThatThrownBy(() -> AccountLocked.of(
            "session-123", "user123", "192.168.1.100", 
            LocalDateTime.now().plusMinutes(30), 0, LocalDateTime.now()
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Failed attempt count must be positive");
    }

    @Test
    @DisplayName("이벤트 타입 확인")
    void shouldReturnCorrectEventType() {
        // Given
        AccountLocked event = AccountLocked.of(
            "session-123", "user123", "192.168.1.100", 
            LocalDateTime.now().plusMinutes(30), 5, LocalDateTime.now()
        );

        // When & Then
        assertThat(event.eventType()).isEqualTo("AccountLocked");
    }

    @Test
    @DisplayName("애그리거트 ID 확인")
    void shouldReturnCorrectAggregateId() {
        // Given
        String sessionId = "session-123";
        AccountLocked event = AccountLocked.of(
            sessionId, "user123", "192.168.1.100", 
            LocalDateTime.now().plusMinutes(30), 5, LocalDateTime.now()
        );

        // When & Then
        assertThat(event.aggregateId()).isEqualTo(sessionId);
    }

    @Test
    @DisplayName("이벤트는 고유한 식별자를 가짐")
    void shouldHaveUniqueEventId() {
        // Given
        String sessionId = "session-123";
        String userId = "user123";
        String clientIp = "192.168.1.100";
        LocalDateTime lockedUntil = LocalDateTime.now().plusMinutes(30);
        int failedAttemptCount = 5;
        LocalDateTime occurredAt = LocalDateTime.now();

        // When
        AccountLocked event1 = AccountLocked.of(
            sessionId, userId, clientIp, lockedUntil, failedAttemptCount, occurredAt
        );
        AccountLocked event2 = AccountLocked.of(
            sessionId, userId, clientIp, lockedUntil, failedAttemptCount, occurredAt
        );

        // Then - DomainEvent는 UUID 기반으로 식별되므로 같은 데이터라도 다른 이벤트임
        assertThat(event1).isNotEqualTo(event2);
        assertThat(event1.getEventId()).isNotEqualTo(event2.getEventId());
        assertThat(event1.sessionId()).isEqualTo(event2.sessionId());
        assertThat(event1.userId()).isEqualTo(event2.userId());
        assertThat(event1.clientIp()).isEqualTo(event2.clientIp());
    }

    @Test
    @DisplayName("동일한 이벤트 인스턴스 동등성 테스트")
    void shouldBeEqualToItself() {
        // Given
        AccountLocked event = AccountLocked.of(
            "session-123", "user123", "192.168.1.100", 
            LocalDateTime.now().plusMinutes(30), 5, LocalDateTime.now()
        );

        // When & Then
        assertThat(event).isEqualTo(event);
        assertThat(event.hashCode()).isEqualTo(event.hashCode());
    }
}