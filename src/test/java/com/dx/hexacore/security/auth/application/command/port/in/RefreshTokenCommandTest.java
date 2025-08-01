package com.dx.hexacore.security.auth.application.command.port.in;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RefreshTokenCommand 테스트")
class RefreshTokenCommandTest {

    @Test
    @DisplayName("유효한 리프레시 토큰으로 Command를 생성할 수 있다")
    void shouldCreateCommandWithValidRefreshToken() {
        // given
        String refreshToken = "refresh.token.eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";

        // when
        RefreshTokenCommand command = new RefreshTokenCommand(refreshToken);

        // then
        assertThat(command.getRefreshToken()).isEqualTo(refreshToken);
    }

    @Test
    @DisplayName("null 리프레시 토큰으로 Command 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenRefreshTokenIsNull() {
        // given
        String refreshToken = null;

        // when & then
        assertThatThrownBy(() -> new RefreshTokenCommand(refreshToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("refreshToken cannot be null");
    }

    @Test
    @DisplayName("빈 리프레시 토큰으로 Command 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenRefreshTokenIsEmpty() {
        // given
        String refreshToken = "";

        // when & then
        assertThatThrownBy(() -> new RefreshTokenCommand(refreshToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("refreshToken cannot be empty or blank");
    }

    @Test
    @DisplayName("공백만 있는 리프레시 토큰으로 Command 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenRefreshTokenIsBlank() {
        // given
        String refreshToken = "   ";

        // when & then
        assertThatThrownBy(() -> new RefreshTokenCommand(refreshToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("refreshToken cannot be empty or blank");
    }

    @Test
    @DisplayName("정적 팩토리 메서드로 Command를 생성할 수 있다")
    void shouldCreateCommandUsingStaticFactoryMethod() {
        // given
        String refreshToken = "valid.refresh.token";

        // when
        RefreshTokenCommand command = RefreshTokenCommand.of(refreshToken);

        // then
        assertThat(command.getRefreshToken()).isEqualTo(refreshToken);
    }

    @Test
    @DisplayName("같은 값으로 생성된 Command는 동일하다")
    void shouldBeEqualWhenCreatedWithSameValues() {
        // given
        String refreshToken = "same.refresh.token";

        // when
        RefreshTokenCommand command1 = new RefreshTokenCommand(refreshToken);
        RefreshTokenCommand command2 = new RefreshTokenCommand(refreshToken);

        // then
        assertThat(command1).isEqualTo(command2);
        assertThat(command1.hashCode()).isEqualTo(command2.hashCode());
    }

    @Test
    @DisplayName("다른 값으로 생성된 Command는 동일하지 않다")
    void shouldNotBeEqualWhenCreatedWithDifferentValues() {
        // given
        RefreshTokenCommand command1 = new RefreshTokenCommand("refresh.token.one");
        RefreshTokenCommand command2 = new RefreshTokenCommand("refresh.token.two");

        // then
        assertThat(command1).isNotEqualTo(command2);
    }
}