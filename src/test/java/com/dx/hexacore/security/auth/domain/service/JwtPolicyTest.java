package com.dx.hexacore.security.auth.domain.service;

import com.dx.hexacore.security.auth.domain.vo.Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtPolicy 테스트")
class JwtPolicyTest {

    private JwtPolicy jwtPolicy;

    @BeforeEach
    void setUp() {
        jwtPolicy = new JwtPolicy();
    }

    @Test
    @DisplayName("유효한 토큰이 정책을 통과한다")
    void shouldValidateValidToken() {
        // Given
        Token validToken = Token.of("valid-access-token", "valid-refresh-token", 3600);

        // When
        boolean isValid = jwtPolicy.validate(validToken);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("만료된 토큰이 정책을 통과하지 못한다")
    void shouldNotValidateExpiredToken() {
        // Given
        Token token = Token.of("expired-access-token", "expired-refresh-token", 3600);
        token = token.expire(); // 토큰을 만료시킴

        // When
        boolean isValid = jwtPolicy.validate(token);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("null 토큰에 대해 false를 반환한다")
    void shouldReturnFalseForNullToken() {
        // When
        boolean isValid = jwtPolicy.validate(null);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("짧은 만료 시간을 가진 토큰도 유효하다면 통과한다")
    void shouldValidateTokenWithShortExpiration() {
        // Given
        Token shortExpirationToken = Token.of("short-token", "refresh-token", 60); // 1분

        // When
        boolean isValid = jwtPolicy.validate(shortExpirationToken);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("긴 만료 시간을 가진 토큰도 유효하다면 통과한다")
    void shouldValidateTokenWithLongExpiration() {
        // Given
        Token longExpirationToken = Token.of("long-token", "refresh-token", 86400); // 24시간

        // When
        boolean isValid = jwtPolicy.validate(longExpirationToken);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("토큰 형식 검증을 수행한다")
    void shouldValidateTokenFormat() {
        // When & Then
        // Token VO 생성 시 검증되어 예외 발생
        assertThatThrownBy(() -> Token.of("", "refresh-token", 3600))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("정책 검증은 stateless하게 동작한다")
    void shouldOperateStateless() {
        // Given
        Token token1 = Token.of("token1", "refresh1", 3600);
        Token token2 = Token.of("token2", "refresh2", 3600);

        // When
        boolean isValid1 = jwtPolicy.validate(token1);
        boolean isValid2 = jwtPolicy.validate(token2);

        // Then
        assertThat(isValid1).isTrue();
        assertThat(isValid2).isTrue();
        
        // 이전 검증이 다음 검증에 영향을 주지 않음을 확인
        boolean isValid1Again = jwtPolicy.validate(token1);
        assertThat(isValid1Again).isTrue();
    }

    @Test
    @DisplayName("동일한 토큰에 대해 일관된 검증 결과를 반환한다")
    void shouldReturnConsistentValidationResults() {
        // Given
        Token token = Token.of("consistent-token", "refresh-token", 3600);

        // When
        boolean result1 = jwtPolicy.validate(token);
        boolean result2 = jwtPolicy.validate(token);
        boolean result3 = jwtPolicy.validate(token);

        // Then
        assertThat(result1).isEqualTo(result2).isEqualTo(result3);
    }
}