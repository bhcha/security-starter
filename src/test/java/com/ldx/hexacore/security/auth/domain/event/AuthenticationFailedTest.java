package com.ldx.hexacore.security.auth.domain.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AuthenticationFailed 도메인 이벤트 테스트")
class AuthenticationFailedTest {

    @Test
    @DisplayName("유효한 정보로 AuthenticationFailed 이벤트를 생성할 수 있다")
    void shouldCreateAuthenticationFailedEventWithValidData() {
        // Given
        UUID authenticationId = UUID.randomUUID();
        String reason = "Invalid credentials";
        LocalDateTime failureTime = LocalDateTime.now();

        // When
        AuthenticationFailed event = AuthenticationFailed.of(
            authenticationId,
            reason,
            failureTime
        );

        // Then
        assertThat(event).isNotNull();
        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getAuthenticationId()).isEqualTo(authenticationId);
        assertThat(event.getReason()).isEqualTo(reason);
        assertThat(event.getFailureTime()).isEqualTo(failureTime);
        assertThat(event.getOccurredOn()).isNotNull();
    }

    @Test
    @DisplayName("null authenticationId로 이벤트 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenAuthenticationIdIsNull() {
        // Given
        String reason = "Invalid credentials";
        LocalDateTime failureTime = LocalDateTime.now();

        // When & Then
        assertThatThrownBy(() -> AuthenticationFailed.of(
            null,
            reason,
            failureTime
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Authentication ID cannot be null");
    }

    @Test
    @DisplayName("null reason으로 이벤트 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenReasonIsNull() {
        // Given
        UUID authenticationId = UUID.randomUUID();
        LocalDateTime failureTime = LocalDateTime.now();

        // When & Then
        assertThatThrownBy(() -> AuthenticationFailed.of(
            authenticationId,
            null,
            failureTime
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Reason cannot be null or empty");
    }

    @Test
    @DisplayName("빈 reason으로 이벤트 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenReasonIsEmpty() {
        // Given
        UUID authenticationId = UUID.randomUUID();
        LocalDateTime failureTime = LocalDateTime.now();

        // When & Then
        assertThatThrownBy(() -> AuthenticationFailed.of(
            authenticationId,
            "",
            failureTime
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Reason cannot be null or empty");
    }

    @Test
    @DisplayName("공백만 있는 reason으로 이벤트 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenReasonIsBlank() {
        // Given
        UUID authenticationId = UUID.randomUUID();
        LocalDateTime failureTime = LocalDateTime.now();

        // When & Then
        assertThatThrownBy(() -> AuthenticationFailed.of(
            authenticationId,
            "   ",
            failureTime
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Reason cannot be null or empty");
    }

    @Test
    @DisplayName("null failureTime으로 이벤트 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenFailureTimeIsNull() {
        // Given
        UUID authenticationId = UUID.randomUUID();
        String reason = "Invalid credentials";

        // When & Then
        assertThatThrownBy(() -> AuthenticationFailed.of(
            authenticationId,
            reason,
            null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Failure time cannot be null");
    }

    @Test
    @DisplayName("같은 데이터로 생성된 두 이벤트는 다르다 (eventId가 다르므로)")
    void shouldNotBeEqualWhenCreatedWithSameData() {
        // Given
        UUID authenticationId = UUID.randomUUID();
        String reason = "Invalid credentials";
        LocalDateTime failureTime = LocalDateTime.now();

        // When
        AuthenticationFailed event1 = AuthenticationFailed.of(authenticationId, reason, failureTime);
        AuthenticationFailed event2 = AuthenticationFailed.of(authenticationId, reason, failureTime);

        // Then
        assertThat(event1).isNotEqualTo(event2);
        assertThat(event1.getEventId()).isNotEqualTo(event2.getEventId());
        assertThat(event1.getAuthenticationId()).isEqualTo(event2.getAuthenticationId());
        assertThat(event1.getReason()).isEqualTo(event2.getReason());
        assertThat(event1.getFailureTime()).isEqualTo(event2.getFailureTime());
    }

    @Test
    @DisplayName("다른 데이터로 생성된 두 이벤트는 다르다")
    void shouldNotBeEqualWhenCreatedWithDifferentData() {
        // Given
        UUID authenticationId1 = UUID.randomUUID();
        UUID authenticationId2 = UUID.randomUUID();
        String reason = "Invalid credentials";
        LocalDateTime failureTime = LocalDateTime.now();

        // When
        AuthenticationFailed event1 = AuthenticationFailed.of(authenticationId1, reason, failureTime);
        AuthenticationFailed event2 = AuthenticationFailed.of(authenticationId2, reason, failureTime);

        // Then
        assertThat(event1).isNotEqualTo(event2);
    }

    @Test
    @DisplayName("toString 메서드가 올바른 정보를 포함한다")
    void shouldIncludeCorrectInformationInToString() {
        // Given
        UUID authenticationId = UUID.randomUUID();
        String reason = "Invalid credentials";
        LocalDateTime failureTime = LocalDateTime.now();

        // When
        AuthenticationFailed event = AuthenticationFailed.of(authenticationId, reason, failureTime);
        String toString = event.toString();

        // Then
        assertThat(toString).contains("AuthenticationFailed");
        assertThat(toString).contains(authenticationId.toString());
        assertThat(toString).contains(reason);
    }
}