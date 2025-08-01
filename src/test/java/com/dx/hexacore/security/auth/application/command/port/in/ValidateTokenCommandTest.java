package com.dx.hexacore.security.auth.application.command.port.in;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ValidateTokenCommand 테스트")
class ValidateTokenCommandTest {

    @Test
    @DisplayName("유효한 액세스 토큰으로 Command를 생성할 수 있다")
    void shouldCreateCommandWithValidAccessToken() {
        // given
        String accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

        // when
        ValidateTokenCommand command = new ValidateTokenCommand(accessToken);

        // then
        assertThat(command.getAccessToken()).isEqualTo(accessToken);
    }

    @Test
    @DisplayName("null 액세스 토큰으로 Command 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenAccessTokenIsNull() {
        // given
        String accessToken = null;

        // when & then
        assertThatThrownBy(() -> new ValidateTokenCommand(accessToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("accessToken cannot be null");
    }

    @Test
    @DisplayName("빈 액세스 토큰으로 Command 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenAccessTokenIsEmpty() {
        // given
        String accessToken = "";

        // when & then
        assertThatThrownBy(() -> new ValidateTokenCommand(accessToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("accessToken cannot be empty or blank");
    }

    @Test
    @DisplayName("공백만 있는 액세스 토큰으로 Command 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenAccessTokenIsBlank() {
        // given
        String accessToken = "   ";

        // when & then
        assertThatThrownBy(() -> new ValidateTokenCommand(accessToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("accessToken cannot be empty or blank");
    }

    @Test
    @DisplayName("정적 팩토리 메서드로 Command를 생성할 수 있다")
    void shouldCreateCommandUsingStaticFactoryMethod() {
        // given
        String accessToken = "valid.jwt.token";

        // when
        ValidateTokenCommand command = ValidateTokenCommand.of(accessToken);

        // then
        assertThat(command.getAccessToken()).isEqualTo(accessToken);
    }

    @Test
    @DisplayName("같은 값으로 생성된 Command는 동일하다")
    void shouldBeEqualWhenCreatedWithSameValues() {
        // given
        String accessToken = "same.token.value";

        // when
        ValidateTokenCommand command1 = new ValidateTokenCommand(accessToken);
        ValidateTokenCommand command2 = new ValidateTokenCommand(accessToken);

        // then
        assertThat(command1).isEqualTo(command2);
        assertThat(command1.hashCode()).isEqualTo(command2.hashCode());
    }

    @Test
    @DisplayName("다른 값으로 생성된 Command는 동일하지 않다")
    void shouldNotBeEqualWhenCreatedWithDifferentValues() {
        // given
        ValidateTokenCommand command1 = new ValidateTokenCommand("token.one");
        ValidateTokenCommand command2 = new ValidateTokenCommand("token.two");

        // then
        assertThat(command1).isNotEqualTo(command2);
    }
}