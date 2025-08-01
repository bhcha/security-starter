package com.dx.hexacore.security.auth.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Token Value Object 테스트.
 */
@DisplayName("Token 값 객체")
class TokenTest {

    private static final String VALID_JWT_ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0IiwiaWF0IjoxNjE2MjM5MDIyfQ.test";
    private static final String VALID_REFRESH_TOKEN = "refresh_token_123456789";

    @Test
    @DisplayName("유효한 accessToken, refreshToken, expiresIn으로 Token 생성에 성공한다")
    void shouldCreateTokenWhenValidTokens() {
        // Given
        String accessToken = VALID_JWT_ACCESS_TOKEN;
        String refreshToken = VALID_REFRESH_TOKEN;
        long expiresIn = 3600L;

        // When
        Token token = Token.of(accessToken, refreshToken, expiresIn);

        // Then
        assertThat(token).isNotNull();
        assertThat(token.getAccessToken()).isEqualTo(accessToken);
        assertThat(token.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(token.getExpiresIn()).isEqualTo(expiresIn);
    }

    @Test
    @DisplayName("expiresIn 최소값(1초)으로 생성에 성공한다")
    void shouldCreateTokenWhenExpiresInMinValue() {
        // Given
        String accessToken = VALID_JWT_ACCESS_TOKEN;
        String refreshToken = VALID_REFRESH_TOKEN;
        long expiresIn = 1L;

        // When
        Token token = Token.of(accessToken, refreshToken, expiresIn);

        // Then
        assertThat(token).isNotNull();
        assertThat(token.getExpiresIn()).isEqualTo(expiresIn);
    }

    @Test
    @DisplayName("expiresIn 최대값(86400초)으로 생성에 성공한다")
    void shouldCreateTokenWhenExpiresInMaxValue() {
        // Given
        String accessToken = VALID_JWT_ACCESS_TOKEN;
        String refreshToken = VALID_REFRESH_TOKEN;
        long expiresIn = 86400L;

        // When
        Token token = Token.of(accessToken, refreshToken, expiresIn);

        // Then
        assertThat(token).isNotNull();
        assertThat(token.getExpiresIn()).isEqualTo(expiresIn);
    }

    @Test
    @DisplayName("동일한 값으로 생성된 Token은 equals true를 반환한다")
    void shouldReturnTrueWhenSameToken() {
        // Given
        String accessToken = VALID_JWT_ACCESS_TOKEN;
        String refreshToken = VALID_REFRESH_TOKEN;
        long expiresIn = 3600L;
        Token token1 = Token.of(accessToken, refreshToken, expiresIn);
        Token token2 = Token.of(accessToken, refreshToken, expiresIn);

        // When & Then
        assertThat(token1).isEqualTo(token2);
    }

    @Test
    @DisplayName("다른 값으로 생성된 Token은 equals false를 반환한다")
    void shouldReturnFalseWhenDifferentToken() {
        // Given
        Token token1 = Token.of(VALID_JWT_ACCESS_TOKEN, VALID_REFRESH_TOKEN, 3600L);
        Token token2 = Token.of(VALID_JWT_ACCESS_TOKEN, "different_refresh", 3600L);

        // When & Then
        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    @DisplayName("동일한 값으로 생성된 Token은 동일한 hashCode를 가진다")
    void shouldHaveSameHashCodeWhenSameToken() {
        // Given
        String accessToken = VALID_JWT_ACCESS_TOKEN;
        String refreshToken = VALID_REFRESH_TOKEN;
        long expiresIn = 3600L;
        Token token1 = Token.of(accessToken, refreshToken, expiresIn);
        Token token2 = Token.of(accessToken, refreshToken, expiresIn);

        // When & Then
        assertThat(token1.hashCode()).isEqualTo(token2.hashCode());
    }

    @Test
    @DisplayName("JWT 형식의 accessToken으로 생성에 성공한다")
    void shouldCreateTokenWhenJwtFormatAccessToken() {
        // Given
        String accessToken = VALID_JWT_ACCESS_TOKEN;
        String refreshToken = VALID_REFRESH_TOKEN;
        long expiresIn = 3600L;

        // When
        Token token = Token.of(accessToken, refreshToken, expiresIn);

        // Then
        assertThat(token).isNotNull();
        assertThat(token.getAccessToken()).isEqualTo(accessToken);
    }

    @Test
    @DisplayName("accessToken이 null일 때 IllegalArgumentException이 발생한다")
    void shouldThrowExceptionWhenAccessTokenIsNull() {
        // Given
        String accessToken = null;
        String refreshToken = VALID_REFRESH_TOKEN;
        long expiresIn = 3600L;

        // When & Then
        assertThatThrownBy(() -> Token.of(accessToken, refreshToken, expiresIn))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Access token cannot be empty");
    }

    @Test
    @DisplayName("accessToken이 empty일 때 IllegalArgumentException이 발생한다")
    void shouldThrowExceptionWhenAccessTokenIsEmpty() {
        // Given
        String accessToken = "";
        String refreshToken = VALID_REFRESH_TOKEN;
        long expiresIn = 3600L;

        // When & Then
        assertThatThrownBy(() -> Token.of(accessToken, refreshToken, expiresIn))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Access token cannot be empty");
    }

    @Test
    @DisplayName("refreshToken이 null일 때 IllegalArgumentException이 발생한다")
    void shouldThrowExceptionWhenRefreshTokenIsNull() {
        // Given
        String accessToken = VALID_JWT_ACCESS_TOKEN;
        String refreshToken = null;
        long expiresIn = 3600L;

        // When & Then
        assertThatThrownBy(() -> Token.of(accessToken, refreshToken, expiresIn))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Refresh token cannot be empty");
    }

    @Test
    @DisplayName("refreshToken이 empty일 때 IllegalArgumentException이 발생한다")
    void shouldThrowExceptionWhenRefreshTokenIsEmpty() {
        // Given
        String accessToken = VALID_JWT_ACCESS_TOKEN;
        String refreshToken = "";
        long expiresIn = 3600L;

        // When & Then
        assertThatThrownBy(() -> Token.of(accessToken, refreshToken, expiresIn))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Refresh token cannot be empty");
    }

    @Test
    @DisplayName("expiresIn이 0일 때 IllegalArgumentException이 발생한다")
    void shouldThrowExceptionWhenExpiresInIsZero() {
        // Given
        String accessToken = VALID_JWT_ACCESS_TOKEN;
        String refreshToken = VALID_REFRESH_TOKEN;
        long expiresIn = 0L;

        // When & Then
        assertThatThrownBy(() -> Token.of(accessToken, refreshToken, expiresIn))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Expires in must be positive");
    }

    @Test
    @DisplayName("expiresIn이 음수일 때 IllegalArgumentException이 발생한다")
    void shouldThrowExceptionWhenExpiresInIsNegative() {
        // Given
        String accessToken = VALID_JWT_ACCESS_TOKEN;
        String refreshToken = VALID_REFRESH_TOKEN;
        long expiresIn = -1L;

        // When & Then
        assertThatThrownBy(() -> Token.of(accessToken, refreshToken, expiresIn))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Expires in must be positive");
    }

    @Test
    @DisplayName("expiresIn이 86401초 이상일 때 IllegalArgumentException이 발생한다")
    void shouldThrowExceptionWhenExpiresInExceedsMaxValue() {
        // Given
        String accessToken = VALID_JWT_ACCESS_TOKEN;
        String refreshToken = VALID_REFRESH_TOKEN;
        long expiresIn = 86401L;

        // When & Then
        assertThatThrownBy(() -> Token.of(accessToken, refreshToken, expiresIn))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Expires in cannot exceed 86400 seconds");
    }

    @Test
    @DisplayName("새로 생성된 Token은 만료되지 않은 상태이다")
    void shouldNotBeExpiredWhenNewlyCreated() {
        // Given
        String accessToken = VALID_JWT_ACCESS_TOKEN;
        String refreshToken = VALID_REFRESH_TOKEN;
        long expiresIn = 3600L;

        // When
        Token token = Token.of(accessToken, refreshToken, expiresIn);

        // Then
        assertThat(token.isExpired()).isFalse();
    }

    @Test
    @DisplayName("expire() 메서드는 만료된 상태의 새로운 Token을 반환한다")
    void shouldReturnExpiredTokenWhenExpireCalled() {
        // Given
        String accessToken = VALID_JWT_ACCESS_TOKEN;
        String refreshToken = VALID_REFRESH_TOKEN;
        long expiresIn = 3600L;
        Token originalToken = Token.of(accessToken, refreshToken, expiresIn);

        // When
        Token expiredToken = originalToken.expire();

        // Then
        assertThat(originalToken.isExpired()).isFalse(); // 원본은 변경되지 않음
        assertThat(expiredToken.isExpired()).isTrue(); // 새 토큰은 만료됨
        assertThat(expiredToken.getAccessToken()).isEqualTo(accessToken);
        assertThat(expiredToken.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(expiredToken.getExpiresIn()).isEqualTo(expiresIn);
    }

    @Test
    @DisplayName("만료된 Token을 다시 expire()해도 새로운 인스턴스를 반환한다")
    void shouldReturnNewInstanceWhenExpireAlreadyExpiredToken() {
        // Given
        String accessToken = VALID_JWT_ACCESS_TOKEN;
        String refreshToken = VALID_REFRESH_TOKEN;
        long expiresIn = 3600L;
        Token originalToken = Token.of(accessToken, refreshToken, expiresIn);
        Token firstExpired = originalToken.expire();

        // When
        Token secondExpired = firstExpired.expire();

        // Then
        assertThat(firstExpired).isNotSameAs(secondExpired);
        assertThat(firstExpired.isExpired()).isTrue();
        assertThat(secondExpired.isExpired()).isTrue();
        assertThat(firstExpired).isEqualTo(secondExpired); // 값은 동일
    }
}