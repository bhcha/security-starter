package com.ldx.hexacore.security.auth.domain.event;

import com.ldx.hexacore.security.auth.domain.vo.Token;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TokenExpired 도메인 이벤트 테스트")
class TokenExpiredTest {

    @Test
    @DisplayName("유효한 정보로 TokenExpired 이벤트를 생성할 수 있다")
    void shouldCreateTokenExpiredEventWithValidData() {
        // Given
        UUID authenticationId = UUID.randomUUID();
        Token expiredToken = Token.of("access-token-123", "refresh-token-456", 3600);
        LocalDateTime expiredTime = LocalDateTime.now();

        // When
        TokenExpired event = TokenExpired.of(
            authenticationId,
            expiredToken,
            expiredTime
        );

        // Then
        assertThat(event).isNotNull();
        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getAuthenticationId()).isEqualTo(authenticationId);
        assertThat(event.getExpiredToken()).isEqualTo(expiredToken);
        assertThat(event.getExpiredTime()).isEqualTo(expiredTime);
        assertThat(event.getOccurredOn()).isNotNull();
    }

    @Test
    @DisplayName("null authenticationId로 이벤트 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenAuthenticationIdIsNull() {
        // Given
        Token expiredToken = Token.of("access-token-123", "refresh-token-456", 3600);
        LocalDateTime expiredTime = LocalDateTime.now();

        // When & Then
        assertThatThrownBy(() -> TokenExpired.of(
            null,
            expiredToken,
            expiredTime
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Authentication ID cannot be null");
    }

    @Test
    @DisplayName("null token으로 이벤트 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenTokenIsNull() {
        // Given
        UUID authenticationId = UUID.randomUUID();
        LocalDateTime expiredTime = LocalDateTime.now();

        // When & Then
        assertThatThrownBy(() -> TokenExpired.of(
            authenticationId,
            null,
            expiredTime
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Expired token cannot be null");
    }

    @Test
    @DisplayName("null expiredTime으로 이벤트 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenExpiredTimeIsNull() {
        // Given
        UUID authenticationId = UUID.randomUUID();
        Token expiredToken = Token.of("access-token-123", "refresh-token-456", 3600);

        // When & Then
        assertThatThrownBy(() -> TokenExpired.of(
            authenticationId,
            expiredToken,
            null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Expired time cannot be null");
    }

    @Test
    @DisplayName("같은 데이터로 생성된 두 이벤트는 다르다 (eventId가 다르므로)")
    void shouldNotBeEqualWhenCreatedWithSameData() {
        // Given
        UUID authenticationId = UUID.randomUUID();
        Token expiredToken = Token.of("access-token-123", "refresh-token-456", 3600);
        LocalDateTime expiredTime = LocalDateTime.now();

        // When
        TokenExpired event1 = TokenExpired.of(authenticationId, expiredToken, expiredTime);
        TokenExpired event2 = TokenExpired.of(authenticationId, expiredToken, expiredTime);

        // Then
        assertThat(event1).isNotEqualTo(event2);
        assertThat(event1.getEventId()).isNotEqualTo(event2.getEventId());
        assertThat(event1.getAuthenticationId()).isEqualTo(event2.getAuthenticationId());
        assertThat(event1.getExpiredToken()).isEqualTo(event2.getExpiredToken());
        assertThat(event1.getExpiredTime()).isEqualTo(event2.getExpiredTime());
    }

    @Test
    @DisplayName("다른 데이터로 생성된 두 이벤트는 다르다")
    void shouldNotBeEqualWhenCreatedWithDifferentData() {
        // Given
        UUID authenticationId1 = UUID.randomUUID();
        UUID authenticationId2 = UUID.randomUUID();
        Token expiredToken = Token.of("access-token-123", "refresh-token-456", 3600);
        LocalDateTime expiredTime = LocalDateTime.now();

        // When
        TokenExpired event1 = TokenExpired.of(authenticationId1, expiredToken, expiredTime);
        TokenExpired event2 = TokenExpired.of(authenticationId2, expiredToken, expiredTime);

        // Then
        assertThat(event1).isNotEqualTo(event2);
    }

    @Test
    @DisplayName("toString 메서드가 올바른 정보를 포함한다")
    void shouldIncludeCorrectInformationInToString() {
        // Given
        UUID authenticationId = UUID.randomUUID();
        Token expiredToken = Token.of("access-token-123", "refresh-token-456", 3600);
        LocalDateTime expiredTime = LocalDateTime.now();

        // When
        TokenExpired event = TokenExpired.of(authenticationId, expiredToken, expiredTime);
        String toString = event.toString();

        // Then
        assertThat(toString).contains("TokenExpired");
        assertThat(toString).contains(authenticationId.toString());
    }
}