package com.dx.hexacore.security.session.domain;

import com.dx.hexacore.security.session.domain.vo.ClientIp;
import com.dx.hexacore.security.session.domain.vo.RiskLevel;
import com.dx.hexacore.security.session.domain.vo.RiskCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AuthenticationAttempt Entity 테스트")
class AuthenticationAttemptTest {

    @Test
    @DisplayName("성공적인 AuthenticationAttempt 생성")
    void shouldCreateAuthenticationAttemptSuccessfully() {
        // Given
        String userId = "user123";
        LocalDateTime attemptedAt = LocalDateTime.now();
        boolean isSuccessful = true;
        ClientIp clientIp = ClientIp.of("192.168.1.100");
        RiskLevel riskLevel = RiskLevel.low("Normal login attempt");

        // When
        AuthenticationAttempt attempt = AuthenticationAttempt.create(
            userId, attemptedAt, isSuccessful, clientIp, riskLevel
        );

        // Then
        assertThat(attempt).isNotNull();
        assertThat(attempt.getUserId()).isEqualTo(userId);
        assertThat(attempt.getAttemptedAt()).isEqualTo(attemptedAt);
        assertThat(attempt.isSuccessful()).isEqualTo(isSuccessful);
        assertThat(attempt.getClientIp()).isEqualTo(clientIp);
        assertThat(attempt.getRiskLevel()).isEqualTo(riskLevel);
    }

    @Test
    @DisplayName("실패한 인증 시도 생성")
    void shouldCreateFailedAuthenticationAttempt() {
        // Given
        String userId = "user123";
        LocalDateTime attemptedAt = LocalDateTime.now();
        boolean isSuccessful = false;
        ClientIp clientIp = ClientIp.of("192.168.1.100");
        RiskLevel riskLevel = RiskLevel.medium("Failed login attempt");

        // When
        AuthenticationAttempt attempt = AuthenticationAttempt.create(
            userId, attemptedAt, isSuccessful, clientIp, riskLevel
        );

        // Then
        assertThat(attempt.isSuccessful()).isFalse();
        assertThat(attempt.getRiskLevel()).isEqualTo(riskLevel);
    }

    @Test
    @DisplayName("높은 위험도 시도 생성")
    void shouldCreateHighRiskAttempt() {
        // Given
        String userId = "user123";
        LocalDateTime attemptedAt = LocalDateTime.now();
        boolean isSuccessful = false;
        ClientIp clientIp = ClientIp.of("10.0.0.1");
        RiskLevel riskLevel = RiskLevel.high("Suspicious activity detected");

        // When
        AuthenticationAttempt attempt = AuthenticationAttempt.create(
            userId, attemptedAt, isSuccessful, clientIp, riskLevel
        );

        // Then
        assertThat(attempt.getRiskLevel()).isEqualTo(riskLevel);
        assertThat(attempt.getRiskLevel().getCategory()).isEqualTo(RiskCategory.HIGH);
    }

    @Test
    @DisplayName("시간 윈도우 내 시도 확인 - 윈도우 내")
    void shouldReturnTrueWhenAttemptIsWithinTimeWindow() {
        // Given
        LocalDateTime attemptedAt = LocalDateTime.now().minusMinutes(15);
        AuthenticationAttempt attempt = AuthenticationAttempt.create(
            "user123", attemptedAt, false, ClientIp.of("192.168.1.100"), RiskLevel.low("Normal activity")
        );
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(20);

        // When
        boolean result = attempt.isWithinTimeWindow(windowStart);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("시간 윈도우 내 시도 확인 - 윈도우 밖")
    void shouldReturnFalseWhenAttemptIsOutsideTimeWindow() {
        // Given
        LocalDateTime attemptedAt = LocalDateTime.now().minusMinutes(25);
        AuthenticationAttempt attempt = AuthenticationAttempt.create(
            "user123", attemptedAt, false, ClientIp.of("192.168.1.100"), RiskLevel.low("Normal activity")
        );
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(20);

        // When
        boolean result = attempt.isWithinTimeWindow(windowStart);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("동일 소스 확인 - 같은 IP")
    void shouldReturnTrueWhenFromSameSource() {
        // Given
        ClientIp originalIp = ClientIp.of("192.168.1.100");
        AuthenticationAttempt attempt = AuthenticationAttempt.create(
            "user123", LocalDateTime.now(), false, originalIp, RiskLevel.low("Normal activity")
        );
        ClientIp sameIp = ClientIp.of("192.168.1.100");

        // When
        boolean result = attempt.isFromSameSource(sameIp);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("동일 소스 확인 - 다른 IP")
    void shouldReturnFalseWhenFromDifferentSource() {
        // Given
        ClientIp originalIp = ClientIp.of("192.168.1.100");
        AuthenticationAttempt attempt = AuthenticationAttempt.create(
            "user123", LocalDateTime.now(), false, originalIp, RiskLevel.low("Normal activity")
        );
        ClientIp differentIp = ClientIp.of("192.168.1.200");

        // When
        boolean result = attempt.isFromSameSource(differentIp);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("위험 점수 계산 - 실패 + 높은 위험도")
    void shouldCalculateHighRiskScoreForFailedHighRiskAttempt() {
        // Given
        AuthenticationAttempt attempt = AuthenticationAttempt.create(
            "user123", LocalDateTime.now(), false, 
            ClientIp.of("192.168.1.100"), RiskLevel.high("Suspicious activity detected")
        );

        // When
        int riskScore = attempt.calculateRiskScore();

        // Then
        assertThat(riskScore).isGreaterThanOrEqualTo(80);
    }

    @Test
    @DisplayName("위험 점수 계산 - 성공 + 낮은 위험도")
    void shouldCalculateLowRiskScoreForSuccessfulLowRiskAttempt() {
        // Given
        AuthenticationAttempt attempt = AuthenticationAttempt.create(
            "user123", LocalDateTime.now(), true, 
            ClientIp.of("192.168.1.100"), RiskLevel.low("Normal activity")
        );

        // When
        int riskScore = attempt.calculateRiskScore();

        // Then
        assertThat(riskScore).isLessThanOrEqualTo(20);
    }

    @Test
    @DisplayName("널 파라미터로 생성 시도 - userId null")
    void shouldThrowExceptionWhenUserIdIsNull() {
        // Given & When & Then
        assertThatThrownBy(() -> AuthenticationAttempt.create(
            null, LocalDateTime.now(), true, 
            ClientIp.of("192.168.1.100"), RiskLevel.low("Normal activity")
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("UserId cannot be null or empty");
    }

    @Test
    @DisplayName("널 파라미터로 생성 시도 - attemptedAt null")
    void shouldThrowExceptionWhenAttemptedAtIsNull() {
        // Given & When & Then
        assertThatThrownBy(() -> AuthenticationAttempt.create(
            "user123", null, true, 
            ClientIp.of("192.168.1.100"), RiskLevel.low("Normal activity")
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Attempted time cannot be null");
    }

    @Test
    @DisplayName("널 파라미터로 생성 시도 - clientIp null")
    void shouldThrowExceptionWhenClientIpIsNull() {
        // Given & When & Then
        assertThatThrownBy(() -> AuthenticationAttempt.create(
            "user123", LocalDateTime.now(), true, null, RiskLevel.low("Normal activity")
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("ClientIp cannot be null");
    }

    @Test
    @DisplayName("널 파라미터로 생성 시도 - riskLevel null")
    void shouldThrowExceptionWhenRiskLevelIsNull() {
        // Given & When & Then
        assertThatThrownBy(() -> AuthenticationAttempt.create(
            "user123", LocalDateTime.now(), true, 
            ClientIp.of("192.168.1.100"), null
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Risk level cannot be null");
    }


    @Test
    @DisplayName("시간 윈도우 시작과 동일한 시간 - 윈도우 내")
    void shouldReturnTrueWhenAttemptTimeEqualsWindowStart() {
        // Given
        LocalDateTime attemptTime = LocalDateTime.now();
        LocalDateTime windowStart = attemptTime;
        AuthenticationAttempt attempt = AuthenticationAttempt.create(
            "user123", attemptTime, true, 
            ClientIp.of("192.168.1.100"), RiskLevel.low("Normal activity")
        );

        // When
        boolean result = attempt.isWithinTimeWindow(windowStart);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("시간 윈도우 확인 시 null 파라미터 예외")
    void shouldThrowExceptionWhenWindowStartIsNull() {
        // Given
        AuthenticationAttempt attempt = AuthenticationAttempt.create(
            "user123", LocalDateTime.now(), true, 
            ClientIp.of("192.168.1.100"), RiskLevel.low("Normal activity")
        );

        // When & Then
        assertThatThrownBy(() -> attempt.isWithinTimeWindow(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Window start time cannot be null");
    }

    @Test
    @DisplayName("동일한 소스에서의 시도 여부 확인 - 동일한 IP")
    void shouldReturnTrueWhenAttemptIsFromSameSource() {
        // Given
        ClientIp clientIp = ClientIp.of("192.168.1.100");
        ClientIp sameIp = ClientIp.of("192.168.1.100");
        AuthenticationAttempt attempt = AuthenticationAttempt.create(
            "user123", LocalDateTime.now(), true, clientIp, RiskLevel.low("Normal activity")
        );

        // When
        boolean result = attempt.isFromSameSource(sameIp);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("동일한 소스에서의 시도 여부 확인 - 다른 IP")
    void shouldReturnFalseWhenAttemptIsFromDifferentSource() {
        // Given
        ClientIp clientIp = ClientIp.of("192.168.1.100");
        ClientIp differentIp = ClientIp.of("192.168.1.200");
        AuthenticationAttempt attempt = AuthenticationAttempt.create(
            "user123", LocalDateTime.now(), true, clientIp, RiskLevel.low("Normal activity")
        );

        // When
        boolean result = attempt.isFromSameSource(differentIp);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("동일한 소스 확인 시 null 파라미터 예외")
    void shouldThrowExceptionWhenOtherIpIsNull() {
        // Given
        AuthenticationAttempt attempt = AuthenticationAttempt.create(
            "user123", LocalDateTime.now(), true, 
            ClientIp.of("192.168.1.100"), RiskLevel.low("Normal activity")
        );

        // When & Then
        assertThatThrownBy(() -> attempt.isFromSameSource(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Other IP cannot be null");
    }

    @Test
    @DisplayName("위험 점수 계산 - 성공한 시도")
    void shouldCalculateRiskScoreForSuccessfulAttempt() {
        // Given
        RiskLevel riskLevel = RiskLevel.medium("Normal activity");
        AuthenticationAttempt attempt = AuthenticationAttempt.create(
            "user123", LocalDateTime.now(), true, 
            ClientIp.of("192.168.1.100"), riskLevel
        );

        // When
        int riskScore = attempt.calculateRiskScore();

        // Then
        assertThat(riskScore).isEqualTo(riskLevel.getScore()); // 성공시 기본 점수
    }

    @Test
    @DisplayName("위험 점수 계산 - 실패한 시도")
    void shouldCalculateRiskScoreForFailedAttempt() {
        // Given
        RiskLevel riskLevel = RiskLevel.medium("Failed login");
        AuthenticationAttempt attempt = AuthenticationAttempt.create(
            "user123", LocalDateTime.now(), false, 
            ClientIp.of("192.168.1.100"), riskLevel
        );

        // When
        int riskScore = attempt.calculateRiskScore();

        // Then
        assertThat(riskScore).isGreaterThan(riskLevel.getScore()); // 실패시 페널티 추가
    }
}