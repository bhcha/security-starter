package com.dx.hexacore.security.auth.domain.event;

import com.dx.hexacore.security.auth.domain.vo.Credentials;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AuthenticationAttempted 도메인 이벤트 테스트")
class AuthenticationAttemptedTest {

    @Test
    @DisplayName("유효한 정보로 AuthenticationAttempted 이벤트를 생성할 수 있다")
    void shouldCreateAuthenticationAttemptedEventWithValidData() {
        // Given
        UUID authenticationId = UUID.randomUUID();
        Credentials credentials = Credentials.of("testuser", "password123");
        LocalDateTime attemptTime = LocalDateTime.now();

        // When
        AuthenticationAttempted event = AuthenticationAttempted.of(
            authenticationId,
            credentials.getUsername(),
            attemptTime
        );

        // Then
        assertThat(event).isNotNull();
        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getAuthenticationId()).isEqualTo(authenticationId);
        assertThat(event.getUsername()).isEqualTo("testuser");
        assertThat(event.getAttemptTime()).isEqualTo(attemptTime);
        assertThat(event.getOccurredOn()).isNotNull();
    }

    @Test
    @DisplayName("null authenticationId로 이벤트 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenAuthenticationIdIsNull() {
        // Given
        Credentials credentials = Credentials.of("testuser", "password123");
        LocalDateTime attemptTime = LocalDateTime.now();

        // When & Then
        assertThatThrownBy(() -> AuthenticationAttempted.of(
            null,
            credentials.getUsername(),
            attemptTime
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Authentication ID cannot be null");
    }

    @Test
    @DisplayName("null username으로 이벤트 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenUsernameIsNull() {
        // Given
        UUID authenticationId = UUID.randomUUID();
        LocalDateTime attemptTime = LocalDateTime.now();

        // When & Then
        assertThatThrownBy(() -> AuthenticationAttempted.of(
            authenticationId,
            null,
            attemptTime
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Username cannot be null or empty");
    }

    @Test
    @DisplayName("빈 username으로 이벤트 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenUsernameIsEmpty() {
        // Given
        UUID authenticationId = UUID.randomUUID();
        LocalDateTime attemptTime = LocalDateTime.now();

        // When & Then
        assertThatThrownBy(() -> AuthenticationAttempted.of(
            authenticationId,
            "",
            attemptTime
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Username cannot be null or empty");
    }

    @Test
    @DisplayName("null attemptTime으로 이벤트 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenAttemptTimeIsNull() {
        // Given
        UUID authenticationId = UUID.randomUUID();

        // When & Then
        assertThatThrownBy(() -> AuthenticationAttempted.of(
            authenticationId,
            "testuser",
            null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Attempt time cannot be null");
    }

    @Test
    @DisplayName("같은 데이터로 생성된 두 이벤트는 다르다 (eventId가 다르므로)")
    void shouldNotBeEqualWhenCreatedWithSameData() {
        // Given
        UUID authenticationId = UUID.randomUUID();
        String username = "testuser";
        LocalDateTime attemptTime = LocalDateTime.now();

        // When
        AuthenticationAttempted event1 = AuthenticationAttempted.of(authenticationId, username, attemptTime);
        AuthenticationAttempted event2 = AuthenticationAttempted.of(authenticationId, username, attemptTime);

        // Then
        assertThat(event1).isNotEqualTo(event2);
        assertThat(event1.getEventId()).isNotEqualTo(event2.getEventId());
        assertThat(event1.getAuthenticationId()).isEqualTo(event2.getAuthenticationId());
        assertThat(event1.getUsername()).isEqualTo(event2.getUsername());
        assertThat(event1.getAttemptTime()).isEqualTo(event2.getAttemptTime());
    }

    @Test
    @DisplayName("다른 데이터로 생성된 두 이벤트는 다르다")
    void shouldNotBeEqualWhenCreatedWithDifferentData() {
        // Given
        UUID authenticationId1 = UUID.randomUUID();
        UUID authenticationId2 = UUID.randomUUID();
        LocalDateTime attemptTime = LocalDateTime.now();

        // When
        AuthenticationAttempted event1 = AuthenticationAttempted.of(authenticationId1, "user1", attemptTime);
        AuthenticationAttempted event2 = AuthenticationAttempted.of(authenticationId2, "user2", attemptTime);

        // Then
        assertThat(event1).isNotEqualTo(event2);
    }

    @Test
    @DisplayName("toString 메서드가 올바른 정보를 포함한다")
    void shouldIncludeCorrectInformationInToString() {
        // Given
        UUID authenticationId = UUID.randomUUID();
        String username = "testuser";
        LocalDateTime attemptTime = LocalDateTime.now();

        // When
        AuthenticationAttempted event = AuthenticationAttempted.of(authenticationId, username, attemptTime);
        String toString = event.toString();

        // Then
        assertThat(toString).contains("AuthenticationAttempted");
        assertThat(toString).contains(authenticationId.toString());
        assertThat(toString).contains(username);
    }
}