package com.ldx.hexacore.security.auth.application.command.port.in;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TokenValidationResult 테스트")
class TokenValidationResultTest {

    @Test
    @DisplayName("유효한 토큰 결과를 생성할 수 있다")
    void shouldCreateValidResult() {
        // given
        String accessToken = "valid.access.token";

        // when
        TokenValidationResult result = TokenValidationResult.valid(accessToken);

        // then
        assertThat(result.isValid()).isTrue();
        assertThat(result.isInvalid()).isFalse();
        assertThat(result.getAccessToken()).isEqualTo(accessToken);
        assertThat(result.getInvalidReason()).isEmpty();
    }

    @Test
    @DisplayName("무효한 토큰 결과를 생성할 수 있다")
    void shouldCreateInvalidResult() {
        // given
        String accessToken = "invalid.access.token";
        String reason = "Token expired";

        // when
        TokenValidationResult result = TokenValidationResult.invalid(accessToken, reason);

        // then
        assertThat(result.isInvalid()).isTrue();
        assertThat(result.isValid()).isFalse();
        assertThat(result.getAccessToken()).isEqualTo(accessToken);
        assertThat(result.getInvalidReason()).isPresent();
        assertThat(result.getInvalidReason().get()).isEqualTo(reason);
    }

    @Test
    @DisplayName("유효한 결과 생성 시 null 토큰이면 예외가 발생한다")
    void shouldThrowExceptionWhenTokenIsNullForValid() {
        // given
        String accessToken = null;

        // when & then
        assertThatThrownBy(() -> TokenValidationResult.valid(accessToken))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("accessToken cannot be null");
    }

    @Test
    @DisplayName("무효한 결과 생성 시 null 토큰이면 예외가 발생한다")
    void shouldThrowExceptionWhenTokenIsNullForInvalid() {
        // given
        String accessToken = null;
        String reason = "Token expired";

        // when & then
        assertThatThrownBy(() -> TokenValidationResult.invalid(accessToken, reason))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("accessToken cannot be null");
    }

    @Test
    @DisplayName("무효한 결과 생성 시 null 이유면 예외가 발생한다")
    void shouldThrowExceptionWhenReasonIsNullForInvalid() {
        // given
        String accessToken = "invalid.access.token";
        String reason = null;

        // when & then
        assertThatThrownBy(() -> TokenValidationResult.invalid(accessToken, reason))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("reason cannot be null for invalid result");
    }

    @Test
    @DisplayName("같은 값으로 생성된 유효한 결과는 동일하다")
    void shouldBeEqualWhenCreatedWithSameValuesForValid() {
        // given
        String accessToken = "valid.access.token";

        // when
        TokenValidationResult result1 = TokenValidationResult.valid(accessToken);
        TokenValidationResult result2 = TokenValidationResult.valid(accessToken);

        // then
        assertThat(result1).isEqualTo(result2);
        assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
    }

    @Test
    @DisplayName("같은 값으로 생성된 무효한 결과는 동일하다")
    void shouldBeEqualWhenCreatedWithSameValuesForInvalid() {
        // given
        String accessToken = "invalid.access.token";
        String reason = "Token expired";

        // when
        TokenValidationResult result1 = TokenValidationResult.invalid(accessToken, reason);
        TokenValidationResult result2 = TokenValidationResult.invalid(accessToken, reason);

        // then
        assertThat(result1).isEqualTo(result2);
        assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
    }

    @Test
    @DisplayName("유효한 결과와 무효한 결과는 동일하지 않다")
    void shouldNotBeEqualBetweenValidAndInvalid() {
        // given
        String accessToken = "access.token";
        
        TokenValidationResult validResult = TokenValidationResult.valid(accessToken);
        TokenValidationResult invalidResult = TokenValidationResult.invalid(accessToken, "Expired");

        // then
        assertThat(validResult).isNotEqualTo(invalidResult);
    }

    @Test
    @DisplayName("다른 토큰으로 생성된 결과는 동일하지 않다")
    void shouldNotBeEqualWhenCreatedWithDifferentTokens() {
        // given
        TokenValidationResult result1 = TokenValidationResult.valid("token1");
        TokenValidationResult result2 = TokenValidationResult.valid("token2");

        // then
        assertThat(result1).isNotEqualTo(result2);
    }

    @Test
    @DisplayName("toString 메서드가 올바르게 동작한다")
    void shouldHaveCorrectToStringRepresentation() {
        // given
        String accessToken = "access.token";
        TokenValidationResult validResult = TokenValidationResult.valid(accessToken);
        TokenValidationResult invalidResult = TokenValidationResult.invalid(accessToken, "Expired");

        // when & then
        assertThat(validResult.toString()).contains("valid=true", "accessToken='[PROTECTED]'");
        assertThat(invalidResult.toString()).contains("valid=false", "accessToken='[PROTECTED]'", "invalidReason='Expired'");
    }
}