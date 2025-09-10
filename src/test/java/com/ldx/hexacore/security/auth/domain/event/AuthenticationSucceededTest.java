package com.ldx.hexacore.security.auth.domain.event;

import com.ldx.hexacore.security.auth.domain.vo.Token;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AuthenticationSucceeded 도메인 이벤트 테스트")
class AuthenticationSucceededTest {

    @Test
    @DisplayName("유효한 정보로 AuthenticationSucceeded 이벤트를 생성할 수 있다")
    void shouldCreateAuthenticationSucceededEventWithValidData() {
        // Given
        UUID authenticationId = UUID.randomUUID();
        Token token = Token.of("access-token-123", "refresh-token-456", 3600);
        LocalDateTime successTime = LocalDateTime.now();

        // When
        AuthenticationSucceeded event = AuthenticationSucceeded.of(
            authenticationId,
            token,
            successTime
        );

        // Then
        assertThat(event).isNotNull();
        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getAuthenticationId()).isEqualTo(authenticationId);
        assertThat(event.getToken()).isEqualTo(token);
        assertThat(event.getSuccessTime()).isEqualTo(successTime);
        assertThat(event.getOccurredOn()).isNotNull();
    }

    @Test
    @DisplayName("null authenticationId로 이벤트 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenAuthenticationIdIsNull() {
        // Given
        Token token = Token.of("access-token-123", "refresh-token-456", 3600);
        LocalDateTime successTime = LocalDateTime.now();

        // When & Then
        assertThatThrownBy(() -> AuthenticationSucceeded.of(
            null,
            token,
            successTime
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Authentication ID cannot be null");
    }

    @Test
    @DisplayName("null token으로 이벤트 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenTokenIsNull() {
        // Given
        UUID authenticationId = UUID.randomUUID();
        LocalDateTime successTime = LocalDateTime.now();

        // When & Then
        assertThatThrownBy(() -> AuthenticationSucceeded.of(
            authenticationId,
            null,
            successTime
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Token cannot be null");
    }

    @Test
    @DisplayName("null successTime으로 이벤트 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenSuccessTimeIsNull() {
        // Given
        UUID authenticationId = UUID.randomUUID();
        Token token = Token.of("access-token-123", "refresh-token-456", 3600);

        // When & Then
        assertThatThrownBy(() -> AuthenticationSucceeded.of(
            authenticationId,
            token,
            null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Success time cannot be null");
    }

    @Test
    @DisplayName("같은 데이터로 생성된 두 이벤트는 다르다 (eventId가 다르므로)")
    void shouldNotBeEqualWhenCreatedWithSameData() {
        // Given
        UUID authenticationId = UUID.randomUUID();
        Token token = Token.of("access-token-123", "refresh-token-456", 3600);
        LocalDateTime successTime = LocalDateTime.now();

        // When
        AuthenticationSucceeded event1 = AuthenticationSucceeded.of(authenticationId, token, successTime);
        AuthenticationSucceeded event2 = AuthenticationSucceeded.of(authenticationId, token, successTime);

        // Then
        assertThat(event1).isNotEqualTo(event2);
        assertThat(event1.getEventId()).isNotEqualTo(event2.getEventId());
        assertThat(event1.getAuthenticationId()).isEqualTo(event2.getAuthenticationId());
        assertThat(event1.getToken()).isEqualTo(event2.getToken());
        assertThat(event1.getSuccessTime()).isEqualTo(event2.getSuccessTime());
    }

    @Test
    @DisplayName("다른 데이터로 생성된 두 이벤트는 다르다")
    void shouldNotBeEqualWhenCreatedWithDifferentData() {
        // Given
        UUID authenticationId1 = UUID.randomUUID();
        UUID authenticationId2 = UUID.randomUUID();
        Token token = Token.of("access-token-123", "refresh-token-456", 3600);
        LocalDateTime successTime = LocalDateTime.now();

        // When
        AuthenticationSucceeded event1 = AuthenticationSucceeded.of(authenticationId1, token, successTime);
        AuthenticationSucceeded event2 = AuthenticationSucceeded.of(authenticationId2, token, successTime);

        // Then
        assertThat(event1).isNotEqualTo(event2);
    }

    @Test
    @DisplayName("toString 메서드가 올바른 정보를 포함한다")
    void shouldIncludeCorrectInformationInToString() {
        // Given
        UUID authenticationId = UUID.randomUUID();
        Token token = Token.of("access-token-123", "refresh-token-456", 3600);
        LocalDateTime successTime = LocalDateTime.now();

        // When
        AuthenticationSucceeded event = AuthenticationSucceeded.of(authenticationId, token, successTime);
        String toString = event.toString();

        // Then
        assertThat(toString).contains("AuthenticationSucceeded");
        assertThat(toString).contains(authenticationId.toString());
    }
}