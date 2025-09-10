package com.ldx.hexacore.security.session.application.command.port.in;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UnlockAccountCommand 테스트")
class UnlockAccountCommandTest {

    @Test
    @DisplayName("유효한 계정 잠금 해제 명령을 생성할 수 있다")
    void shouldCreateValidUnlockAccountCommand() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";

        // When
        UnlockAccountCommand command = new UnlockAccountCommand(sessionId, userId);

        // Then
        assertThat(command.sessionId()).isEqualTo(sessionId);
        assertThat(command.userId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("null 세션 ID로 명령 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenSessionIdIsNull() {
        // Given
        String sessionId = null;
        String userId = "testUser123";

        // When & Then
        assertThatThrownBy(() -> new UnlockAccountCommand(sessionId, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("SessionId cannot be null or empty");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    @DisplayName("빈 세션 ID로 명령 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenSessionIdIsEmpty(String sessionId) {
        // Given
        String userId = "testUser123";

        // When & Then
        assertThatThrownBy(() -> new UnlockAccountCommand(sessionId, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("SessionId cannot be null or empty");
    }

    @Test
    @DisplayName("null 사용자 ID로 명령 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenUserIdIsNull() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = null;

        // When & Then
        assertThatThrownBy(() -> new UnlockAccountCommand(sessionId, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("UserId cannot be null or empty");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    @DisplayName("빈 사용자 ID로 명령 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenUserIdIsEmpty(String userId) {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";

        // When & Then
        assertThatThrownBy(() -> new UnlockAccountCommand(sessionId, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("UserId cannot be null or empty");
    }

    @Test
    @DisplayName("동등성 비교가 올바르게 동작한다")
    void shouldCompareEquality() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";

        UnlockAccountCommand command1 = new UnlockAccountCommand(sessionId, userId);
        UnlockAccountCommand command2 = new UnlockAccountCommand(sessionId, userId);

        // When & Then
        assertThat(command1).isEqualTo(command2);
        assertThat(command1.hashCode()).isEqualTo(command2.hashCode());
    }

    @Test
    @DisplayName("서로 다른 값을 가진 명령은 동등하지 않다")
    void shouldNotBeEqualWithDifferentValues() {
        // Given
        String sessionId1 = "550e8400-e29b-41d4-a716-446655440000";
        String sessionId2 = "550e8400-e29b-41d4-a716-446655440001";
        String userId = "testUser123";

        UnlockAccountCommand command1 = new UnlockAccountCommand(sessionId1, userId);
        UnlockAccountCommand command2 = new UnlockAccountCommand(sessionId2, userId);

        // When & Then
        assertThat(command1).isNotEqualTo(command2);
    }

    @Test
    @DisplayName("toString이 적절한 정보를 반환한다")
    void shouldReturnAppropriateToString() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";

        UnlockAccountCommand command = new UnlockAccountCommand(sessionId, userId);

        // When
        String result = command.toString();

        // Then
        assertThat(result).contains("UnlockAccountCommand");
        assertThat(result).contains(sessionId);
        assertThat(result).contains(userId);
    }

    @Test
    @DisplayName("특수 문자가 포함된 사용자 ID로 명령을 생성할 수 있다")
    void shouldCreateCommandWithSpecialCharacterUserId() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "user@example.com";

        // When
        UnlockAccountCommand command = new UnlockAccountCommand(sessionId, userId);

        // Then
        assertThat(command.sessionId()).isEqualTo(sessionId);
        assertThat(command.userId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("긴 사용자 ID로 명령을 생성할 수 있다")
    void shouldCreateCommandWithLongUserId() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "very_long_user_id_with_multiple_underscores_and_numbers_123456789";

        // When
        UnlockAccountCommand command = new UnlockAccountCommand(sessionId, userId);

        // Then
        assertThat(command.sessionId()).isEqualTo(sessionId);
        assertThat(command.userId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("UUID가 아닌 세션 ID로도 명령을 생성할 수 있다")
    void shouldCreateCommandWithNonUUIDSessionId() {
        // Given
        String sessionId = "custom-session-id-123";
        String userId = "testUser123";

        // When
        UnlockAccountCommand command = new UnlockAccountCommand(sessionId, userId);

        // Then
        assertThat(command.sessionId()).isEqualTo(sessionId);
        assertThat(command.userId()).isEqualTo(userId);
    }
}