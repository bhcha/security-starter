package com.dx.hexacore.security.session.application.command.port.in;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RecordAuthenticationAttemptCommand 테스트")
class RecordAuthenticationAttemptCommandTest {

    @Test
    @DisplayName("유효한 성공적인 인증 시도 명령을 생성할 수 있다")
    void shouldCreateValidSuccessfulAttemptCommand() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        String clientIp = "192.168.1.100";
        boolean isSuccessful = true;
        int riskScore = 10;
        String riskReason = "Normal login";

        // When
        RecordAuthenticationAttemptCommand command = new RecordAuthenticationAttemptCommand(
                sessionId, userId, clientIp, isSuccessful, riskScore, riskReason);

        // Then
        assertThat(command.sessionId()).isEqualTo(sessionId);
        assertThat(command.userId()).isEqualTo(userId);
        assertThat(command.clientIp()).isEqualTo(clientIp);
        assertThat(command.isSuccessful()).isTrue();
        assertThat(command.riskScore()).isEqualTo(riskScore);
        assertThat(command.riskReason()).isEqualTo(riskReason);
    }

    @Test
    @DisplayName("유효한 실패한 인증 시도 명령을 생성할 수 있다")
    void shouldCreateValidFailedAttemptCommand() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        String clientIp = "192.168.1.100";
        boolean isSuccessful = false;
        int riskScore = 80;
        String riskReason = "Wrong password";

        // When
        RecordAuthenticationAttemptCommand command = new RecordAuthenticationAttemptCommand(
                sessionId, userId, clientIp, isSuccessful, riskScore, riskReason);

        // Then
        assertThat(command.sessionId()).isEqualTo(sessionId);
        assertThat(command.userId()).isEqualTo(userId);
        assertThat(command.clientIp()).isEqualTo(clientIp);
        assertThat(command.isSuccessful()).isFalse();
        assertThat(command.riskScore()).isEqualTo(riskScore);
        assertThat(command.riskReason()).isEqualTo(riskReason);
    }

    @Test
    @DisplayName("null 세션 ID로 명령 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenSessionIdIsNull() {
        // Given
        String sessionId = null;
        String userId = "testUser123";
        String clientIp = "192.168.1.100";
        boolean isSuccessful = true;
        int riskScore = 10;
        String riskReason = "Normal login";

        // When & Then
        assertThatThrownBy(() -> new RecordAuthenticationAttemptCommand(
                sessionId, userId, clientIp, isSuccessful, riskScore, riskReason))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("SessionId cannot be null or empty");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    @DisplayName("빈 세션 ID로 명령 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenSessionIdIsEmpty(String sessionId) {
        // Given
        String userId = "testUser123";
        String clientIp = "192.168.1.100";
        boolean isSuccessful = true;
        int riskScore = 10;
        String riskReason = "Normal login";

        // When & Then
        assertThatThrownBy(() -> new RecordAuthenticationAttemptCommand(
                sessionId, userId, clientIp, isSuccessful, riskScore, riskReason))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("SessionId cannot be null or empty");
    }

    @Test
    @DisplayName("null 사용자 ID로 명령 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenUserIdIsNull() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = null;
        String clientIp = "192.168.1.100";
        boolean isSuccessful = true;
        int riskScore = 10;
        String riskReason = "Normal login";

        // When & Then
        assertThatThrownBy(() -> new RecordAuthenticationAttemptCommand(
                sessionId, userId, clientIp, isSuccessful, riskScore, riskReason))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("UserId cannot be null or empty");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    @DisplayName("빈 사용자 ID로 명령 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenUserIdIsEmpty(String userId) {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String clientIp = "192.168.1.100";
        boolean isSuccessful = true;
        int riskScore = 10;
        String riskReason = "Normal login";

        // When & Then
        assertThatThrownBy(() -> new RecordAuthenticationAttemptCommand(
                sessionId, userId, clientIp, isSuccessful, riskScore, riskReason))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("UserId cannot be null or empty");
    }

    @Test
    @DisplayName("null 클라이언트 IP로 명령 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenClientIpIsNull() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        String clientIp = null;
        boolean isSuccessful = true;
        int riskScore = 10;
        String riskReason = "Normal login";

        // When & Then
        assertThatThrownBy(() -> new RecordAuthenticationAttemptCommand(
                sessionId, userId, clientIp, isSuccessful, riskScore, riskReason))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ClientIp cannot be null or empty");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    @DisplayName("빈 클라이언트 IP로 명령 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenClientIpIsEmpty(String clientIp) {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        boolean isSuccessful = true;
        int riskScore = 10;
        String riskReason = "Normal login";

        // When & Then
        assertThatThrownBy(() -> new RecordAuthenticationAttemptCommand(
                sessionId, userId, clientIp, isSuccessful, riskScore, riskReason))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ClientIp cannot be null or empty");
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "999.999.999.999", "256.1.1.1", "192.168.1", "192.168.1.1.1"})
    @DisplayName("유효하지 않은 IP 주소로 명령 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenClientIpIsInvalid(String clientIp) {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        boolean isSuccessful = true;
        int riskScore = 10;
        String riskReason = "Normal login";

        // When & Then
        assertThatThrownBy(() -> new RecordAuthenticationAttemptCommand(
                sessionId, userId, clientIp, isSuccessful, riskScore, riskReason))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid ClientIp format:");
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -10, 101, 150, Integer.MAX_VALUE})
    @DisplayName("위험도 점수가 범위를 벗어나면 예외가 발생한다")
    void shouldThrowExceptionWhenRiskScoreIsOutOfRange(int riskScore) {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        String clientIp = "192.168.1.100";
        boolean isSuccessful = true;
        String riskReason = "Normal login";

        // When & Then
        assertThatThrownBy(() -> new RecordAuthenticationAttemptCommand(
                sessionId, userId, clientIp, isSuccessful, riskScore, riskReason))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Risk score must be between 0 and 100");
    }

    @Test
    @DisplayName("null 위험도 이유로 명령 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenRiskReasonIsNull() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        String clientIp = "192.168.1.100";
        boolean isSuccessful = true;
        int riskScore = 10;
        String riskReason = null;

        // When & Then
        assertThatThrownBy(() -> new RecordAuthenticationAttemptCommand(
                sessionId, userId, clientIp, isSuccessful, riskScore, riskReason))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Risk reason cannot be null or empty");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    @DisplayName("빈 위험도 이유로 명령 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenRiskReasonIsEmpty(String riskReason) {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        String clientIp = "192.168.1.100";
        boolean isSuccessful = true;
        int riskScore = 10;

        // When & Then
        assertThatThrownBy(() -> new RecordAuthenticationAttemptCommand(
                sessionId, userId, clientIp, isSuccessful, riskScore, riskReason))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Risk reason cannot be null or empty");
    }

    @Test
    @DisplayName("최소 위험도 점수(0)로 명령을 생성할 수 있다")
    void shouldCreateCommandWithMinimumRiskScore() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        String clientIp = "192.168.1.100";
        boolean isSuccessful = true;
        int riskScore = 0;
        String riskReason = "Minimum risk";

        // When
        RecordAuthenticationAttemptCommand command = new RecordAuthenticationAttemptCommand(
                sessionId, userId, clientIp, isSuccessful, riskScore, riskReason);

        // Then
        assertThat(command.riskScore()).isEqualTo(0);
    }

    @Test
    @DisplayName("최대 위험도 점수(100)로 명령을 생성할 수 있다")
    void shouldCreateCommandWithMaximumRiskScore() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        String clientIp = "192.168.1.100";
        boolean isSuccessful = false;
        int riskScore = 100;
        String riskReason = "Maximum risk";

        // When
        RecordAuthenticationAttemptCommand command = new RecordAuthenticationAttemptCommand(
                sessionId, userId, clientIp, isSuccessful, riskScore, riskReason);

        // Then
        assertThat(command.riskScore()).isEqualTo(100);
    }

    @Test
    @DisplayName("IPv6 주소로 명령을 생성할 수 있다")
    void shouldCreateCommandWithIPv6Address() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        String clientIp = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
        boolean isSuccessful = true;
        int riskScore = 20;
        String riskReason = "IPv6 login";

        // When
        RecordAuthenticationAttemptCommand command = new RecordAuthenticationAttemptCommand(
                sessionId, userId, clientIp, isSuccessful, riskScore, riskReason);

        // Then
        assertThat(command.clientIp()).isEqualTo(clientIp);
    }

    @Test
    @DisplayName("동등성 비교가 올바르게 동작한다")
    void shouldCompareEquality() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        String clientIp = "192.168.1.100";
        boolean isSuccessful = true;
        int riskScore = 10;
        String riskReason = "Normal login";

        RecordAuthenticationAttemptCommand command1 = new RecordAuthenticationAttemptCommand(
                sessionId, userId, clientIp, isSuccessful, riskScore, riskReason);
        RecordAuthenticationAttemptCommand command2 = new RecordAuthenticationAttemptCommand(
                sessionId, userId, clientIp, isSuccessful, riskScore, riskReason);

        // When & Then
        assertThat(command1).isEqualTo(command2);
        assertThat(command1.hashCode()).isEqualTo(command2.hashCode());
    }

    @Test
    @DisplayName("toString이 적절한 정보를 반환한다")
    void shouldReturnAppropriateToString() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        String clientIp = "192.168.1.100";
        boolean isSuccessful = true;
        int riskScore = 10;
        String riskReason = "Normal login";

        RecordAuthenticationAttemptCommand command = new RecordAuthenticationAttemptCommand(
                sessionId, userId, clientIp, isSuccessful, riskScore, riskReason);

        // When
        String result = command.toString();

        // Then
        assertThat(result).contains("RecordAuthenticationAttemptCommand");
        assertThat(result).contains(sessionId);
        assertThat(result).contains(userId);
        assertThat(result).contains(clientIp);
        assertThat(result).contains(String.valueOf(isSuccessful));
        assertThat(result).contains(String.valueOf(riskScore));
        assertThat(result).contains(riskReason);
    }
}