package com.ldx.hexacore.security.auth.application.command.port.in;

import com.ldx.hexacore.security.auth.domain.vo.Token;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AuthenticationResult 테스트")
class AuthenticationResultTest {

    @Test
    @DisplayName("성공 결과를 생성할 수 있다")
    void shouldCreateSuccessResult() {
        // given
        String username = "testuser";
        Token token = Token.of("access.token", "refresh.token", 3600);

        // when
        AuthenticationResult result = AuthenticationResult.success(username, token);

        // then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isFailure()).isFalse();
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getToken()).isPresent();
        assertThat(result.getToken().get()).isEqualTo(token);
        assertThat(result.getFailureReason()).isEmpty();
    }

    @Test
    @DisplayName("실패 결과를 생성할 수 있다")
    void shouldCreateFailureResult() {
        // given
        String username = "testuser";
        String reason = "Invalid credentials";

        // when
        AuthenticationResult result = AuthenticationResult.failure(username, reason);

        // then
        assertThat(result.isFailure()).isTrue();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getToken()).isEmpty();
        assertThat(result.getFailureReason()).isPresent();
        assertThat(result.getFailureReason().get()).isEqualTo(reason);
    }

    @Test
    @DisplayName("성공 결과 생성 시 null 토큰이면 예외가 발생한다")
    void shouldThrowExceptionWhenTokenIsNullForSuccess() {
        // given
        String username = "testuser";
        Token token = null;

        // when & then
        assertThatThrownBy(() -> AuthenticationResult.success(username, token))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("token cannot be null for success result");
    }

    @Test
    @DisplayName("실패 결과 생성 시 null 이유면 예외가 발생한다")
    void shouldThrowExceptionWhenReasonIsNullForFailure() {
        // given
        String username = "testuser";
        String reason = null;

        // when & then
        assertThatThrownBy(() -> AuthenticationResult.failure(username, reason))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("reason cannot be null for failure result");
    }

    @Test
    @DisplayName("null username으로 성공 결과 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenUsernameIsNullForSuccess() {
        // given
        String username = null;
        Token token = Token.of("access.token", "refresh.token", 3600);

        // when & then
        assertThatThrownBy(() -> AuthenticationResult.success(username, token))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("username cannot be null");
    }

    @Test
    @DisplayName("null username으로 실패 결과 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenUsernameIsNullForFailure() {
        // given
        String username = null;
        String reason = "Invalid credentials";

        // when & then
        assertThatThrownBy(() -> AuthenticationResult.failure(username, reason))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("username cannot be null");
    }

    @Test
    @DisplayName("같은 값으로 생성된 성공 결과는 동일하다")
    void shouldBeEqualWhenCreatedWithSameValuesForSuccess() {
        // given
        String username = "testuser";
        Token token = Token.of("access.token", "refresh.token", 3600);

        // when
        AuthenticationResult result1 = AuthenticationResult.success(username, token);
        AuthenticationResult result2 = AuthenticationResult.success(username, token);

        // then
        assertThat(result1).isEqualTo(result2);
        assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
    }

    @Test
    @DisplayName("같은 값으로 생성된 실패 결과는 동일하다")
    void shouldBeEqualWhenCreatedWithSameValuesForFailure() {
        // given
        String username = "testuser";
        String reason = "Invalid credentials";

        // when
        AuthenticationResult result1 = AuthenticationResult.failure(username, reason);
        AuthenticationResult result2 = AuthenticationResult.failure(username, reason);

        // then
        assertThat(result1).isEqualTo(result2);
        assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
    }

    @Test
    @DisplayName("성공 결과와 실패 결과는 동일하지 않다")
    void shouldNotBeEqualBetweenSuccessAndFailure() {
        // given
        String username = "testuser";
        Token token = Token.of("access.token", "refresh.token", 3600);
        
        AuthenticationResult successResult = AuthenticationResult.success(username, token);
        AuthenticationResult failureResult = AuthenticationResult.failure(username, "Failed");

        // then
        assertThat(successResult).isNotEqualTo(failureResult);
    }

    @Test
    @DisplayName("toString 메서드가 올바르게 동작한다")
    void shouldHaveCorrectToStringRepresentation() {
        // given
        String username = "testuser";
        Token token = Token.of("access.token", "refresh.token", 3600);
        AuthenticationResult successResult = AuthenticationResult.success(username, token);
        AuthenticationResult failureResult = AuthenticationResult.failure(username, "Failed");

        // when & then
        assertThat(successResult.toString()).contains("success=true", "username='testuser'", "hasToken=true");
        assertThat(failureResult.toString()).contains("success=false", "username='testuser'", "hasToken=false", "failureReason='Failed'");
    }
}