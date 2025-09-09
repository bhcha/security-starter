package com.dx.hexacore.security.auth.application.command.port.in;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AuthenticateCommand 테스트")
class AuthenticateCommandTest {

    @Test
    @DisplayName("유효한 사용자명과 비밀번호로 Command를 생성할 수 있다")
    void shouldCreateCommandWithValidUsernameAndPassword() {
        // given
        String username = "testuser";
        String password = "testpassword123";

        // when
        AuthenticateCommand command = new AuthenticateCommand(username, password);

        // then
        assertThat(command.getUsername()).isEqualTo(username);
        assertThat(command.getPassword()).isEqualTo(password);
    }

    @Test
    @DisplayName("null username으로 Command 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenUsernameIsNull() {
        // given
        String username = null;
        String password = "testpassword123";

        // when & then
        assertThatThrownBy(() -> new AuthenticateCommand(username, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("username cannot be null");
    }

    @Test
    @DisplayName("빈 username으로 Command 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenUsernameIsEmpty() {
        // given
        String username = "";
        String password = "testpassword123";

        // when & then
        assertThatThrownBy(() -> new AuthenticateCommand(username, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("username cannot be null or empty");
    }

    @Test
    @DisplayName("공백만 있는 username으로 Command 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenUsernameIsBlank() {
        // given
        String username = "   ";
        String password = "testpassword123";

        // when & then
        assertThatThrownBy(() -> new AuthenticateCommand(username, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("username cannot be null or empty");
    }

    @Test
    @DisplayName("null password로 Command 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenPasswordIsNull() {
        // given
        String username = "testuser";
        String password = null;

        // when & then
        assertThatThrownBy(() -> new AuthenticateCommand(username, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("password cannot be null");
    }

    @Test
    @DisplayName("빈 password로 Command 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenPasswordIsEmpty() {
        // given
        String username = "testuser";
        String password = "";

        // when & then
        assertThatThrownBy(() -> new AuthenticateCommand(username, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("password cannot be null or empty");
    }

    @Test
    @DisplayName("공백만 있는 password로 Command 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenPasswordIsBlank() {
        // given
        String username = "testuser";
        String password = "   ";

        // when & then
        assertThatThrownBy(() -> new AuthenticateCommand(username, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("password cannot be null or empty");
    }

    @Test
    @DisplayName("정적 팩토리 메서드로 Command를 생성할 수 있다")
    void shouldCreateCommandUsingStaticFactoryMethod() {
        // given
        String username = "testuser";
        String password = "testpassword123";

        // when
        AuthenticateCommand command = AuthenticateCommand.of(username, password);

        // then
        assertThat(command.getUsername()).isEqualTo(username);
        assertThat(command.getPassword()).isEqualTo(password);
    }

    @Test
    @DisplayName("같은 값으로 생성된 Command는 동일하다")
    void shouldBeEqualWhenCreatedWithSameValues() {
        // given
        String username = "testuser";
        String password = "testpassword123";

        // when
        AuthenticateCommand command1 = new AuthenticateCommand(username, password);
        AuthenticateCommand command2 = new AuthenticateCommand(username, password);

        // then
        assertThat(command1).isEqualTo(command2);
        assertThat(command1.hashCode()).isEqualTo(command2.hashCode());
    }

    @Test
    @DisplayName("다른 값으로 생성된 Command는 동일하지 않다")
    void shouldNotBeEqualWhenCreatedWithDifferentValues() {
        // given
        AuthenticateCommand command1 = new AuthenticateCommand("user1", "password1");
        AuthenticateCommand command2 = new AuthenticateCommand("user2", "password2");

        // then
        assertThat(command1).isNotEqualTo(command2);
    }
}