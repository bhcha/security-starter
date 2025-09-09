package com.dx.hexacore.security.session.domain;

import com.dx.hexacore.security.session.domain.event.AccountLocked;
import com.dx.hexacore.security.session.domain.vo.ClientIp;
import com.dx.hexacore.security.session.domain.vo.RiskLevel;
import com.dx.hexacore.security.session.domain.vo.SessionId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AuthenticationSession Aggregate Root 테스트")
class AuthenticationSessionTest {

    private com.dx.hexacore.security.config.SecurityConstants securityConstants;
    
    @BeforeEach
    void setUp() {
        securityConstants = new com.dx.hexacore.security.config.SecurityConstants();
    }

    @Test
    @DisplayName("AuthenticationSession 생성")
    void shouldCreateAuthenticationSession() {
        // Given
        SessionId sessionId = SessionId.generate();
        String userId = "user123";
        ClientIp clientIp = ClientIp.of("192.168.1.100");

        // When
        AuthenticationSession session = AuthenticationSession.create(sessionId, userId, clientIp, 
                                                                    securityConstants.getSession().getMaxFailedAttempts(),
                                                                    securityConstants.getSession().getLockoutDurationMinutes(),
                                                                    securityConstants.getSession().getTimeWindowMinutes());

        // Then
        assertThat(session).isNotNull();
        assertThat(session.getSessionId()).isEqualTo(sessionId);
        assertThat(session.getUserId()).isEqualTo(userId);
        assertThat(session.getClientIp()).isEqualTo(clientIp);
        assertThat(session.isLocked()).isFalse();
        assertThat(session.getLockedUntil()).isNull();
        assertThat(session.getAttempts()).isEmpty();
        assertThat(session.getCreatedAt()).isNotNull();
        assertThat(session.getLastActivityAt()).isNotNull();
    }

    @Test
    @DisplayName("널 파라미터로 생성 시도 - sessionId null")
    void shouldThrowExceptionWhenSessionIdIsNull() {
        // Given & When & Then
        assertThatThrownBy(() -> AuthenticationSession.create(
            null, "user123", ClientIp.of("192.168.1.100"), 
            securityConstants.getSession().getMaxFailedAttempts(),
            securityConstants.getSession().getLockoutDurationMinutes(),
            securityConstants.getSession().getTimeWindowMinutes()
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("SessionId cannot be null");
    }

    @Test
    @DisplayName("널 파라미터로 생성 시도 - userId null")
    void shouldThrowExceptionWhenUserIdIsNull() {
        // Given & When & Then
        assertThatThrownBy(() -> AuthenticationSession.create(
            SessionId.generate(), null, ClientIp.of("192.168.1.100"), 
            securityConstants.getSession().getMaxFailedAttempts(),
            securityConstants.getSession().getLockoutDurationMinutes(),
            securityConstants.getSession().getTimeWindowMinutes()
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("UserId cannot be null or empty");
    }

    @Test
    @DisplayName("널 파라미터로 생성 시도 - userId empty")
    void shouldThrowExceptionWhenUserIdIsEmpty() {
        // Given & When & Then
        assertThatThrownBy(() -> AuthenticationSession.create(
            SessionId.generate(), "", ClientIp.of("192.168.1.100"), 
            securityConstants.getSession().getMaxFailedAttempts(),
            securityConstants.getSession().getLockoutDurationMinutes(),
            securityConstants.getSession().getTimeWindowMinutes()
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("UserId cannot be null or empty");
    }

    @Test
    @DisplayName("널 파라미터로 생성 시도 - clientIp null")
    void shouldThrowExceptionWhenClientIpIsNull() {
        // Given & When & Then
        assertThatThrownBy(() -> AuthenticationSession.create(
            SessionId.generate(), "user123", null, 
            securityConstants.getSession().getMaxFailedAttempts(),
            securityConstants.getSession().getLockoutDurationMinutes(),
            securityConstants.getSession().getTimeWindowMinutes()
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessage("ClientIp cannot be null");
    }

    @Test
    @DisplayName("성공한 인증 시도 기록")
    void shouldRecordSuccessfulAttempt() throws InterruptedException {
        // Given
        AuthenticationSession session = AuthenticationSession.create(
            SessionId.generate(), "user123", ClientIp.of("192.168.1.100"), 
            securityConstants.getSession().getMaxFailedAttempts(),
            securityConstants.getSession().getLockoutDurationMinutes(),
            securityConstants.getSession().getTimeWindowMinutes()
        );
        String userId = "user123";
        ClientIp clientIp = ClientIp.of("192.168.1.100");
        RiskLevel riskLevel = RiskLevel.low("Normal login");

        LocalDateTime beforeRecord = LocalDateTime.now();
        Thread.sleep(1); // 시간 차이를 위해

        // When
        session.recordAttempt(userId, clientIp, true, riskLevel);

        // Then
        assertThat(session.getAttempts()).hasSize(1);
        AuthenticationAttempt attempt = session.getAttempts().get(0);
        assertThat(attempt.getUserId()).isEqualTo(userId);
        assertThat(attempt.isSuccessful()).isTrue();
        assertThat(attempt.getClientIp()).isEqualTo(clientIp);
        assertThat(attempt.getRiskLevel()).isEqualTo(riskLevel);
        assertThat(session.getLastActivityAt()).isAfter(beforeRecord);
    }

    @Test
    @DisplayName("실패한 인증 시도 기록")
    void shouldRecordFailedAttempt() {
        // Given
        AuthenticationSession session = AuthenticationSession.create(
            SessionId.generate(), "user123", ClientIp.of("192.168.1.100"), 
            securityConstants.getSession().getMaxFailedAttempts(),
            securityConstants.getSession().getLockoutDurationMinutes(),
            securityConstants.getSession().getTimeWindowMinutes()
        );
        String userId = "user123";
        ClientIp clientIp = ClientIp.of("192.168.1.100");
        RiskLevel riskLevel = RiskLevel.medium("Failed login");

        // When
        session.recordAttempt(userId, clientIp, false, riskLevel);

        // Then
        assertThat(session.getAttempts()).hasSize(1);
        AuthenticationAttempt attempt = session.getAttempts().get(0);
        assertThat(attempt.isSuccessful()).isFalse();
        assertThat(attempt.getRiskLevel()).isEqualTo(riskLevel);
    }

    @Test
    @DisplayName("다중 실패 시도 기록")
    void shouldRecordMultipleFailedAttempts() throws InterruptedException {
        // Given
        AuthenticationSession session = AuthenticationSession.create(
            SessionId.generate(), "user123", ClientIp.of("192.168.1.100"), 
            securityConstants.getSession().getMaxFailedAttempts(),
            securityConstants.getSession().getLockoutDurationMinutes(),
            securityConstants.getSession().getTimeWindowMinutes()
        );
        String userId = "user123";
        ClientIp clientIp = ClientIp.of("192.168.1.100");

        // When
        session.recordAttempt(userId, clientIp, false, RiskLevel.low("First attempt"));
        Thread.sleep(2); // 시간 차이를 위해
        session.recordAttempt(userId, clientIp, false, RiskLevel.medium("Second attempt"));
        Thread.sleep(2);
        session.recordAttempt(userId, clientIp, false, RiskLevel.high("Third attempt"));

        // Then
        assertThat(session.getAttempts()).hasSize(3);
        
        // 시간 순으로 정렬되어 있는지 확인
        List<AuthenticationAttempt> attempts = session.getAttempts();
        for (int i = 1; i < attempts.size(); i++) {
            assertThat(attempts.get(i).getAttemptedAt())
                .isAfterOrEqualTo(attempts.get(i - 1).getAttemptedAt());
        }
    }

    @Test
    @DisplayName("계정 잠금 필요 여부 판단 - 임계값 미달")
    void shouldNotLockAccountWhenBelowThreshold() {
        // Given
        AuthenticationSession session = AuthenticationSession.create(
            SessionId.generate(), "user123", ClientIp.of("192.168.1.100"), 
            securityConstants.getSession().getMaxFailedAttempts(),
            securityConstants.getSession().getLockoutDurationMinutes(),
            securityConstants.getSession().getTimeWindowMinutes()
        );
        
        // 4번의 실패 시도 (MAX_FAILED_ATTEMPTS보다 1개 적음)
        for (int i = 0; i < 4; i++) {
            session.recordAttempt("user123", ClientIp.of("192.168.1.100"), 
                                false, RiskLevel.low("Attempt " + (i + 1)));
        }

        // When
        boolean shouldLock = session.shouldLockAccount();

        // Then
        assertThat(shouldLock).isFalse();
    }

    @Test
    @DisplayName("계정 잠금 필요 여부 판단 - 임계값 달성")
    void shouldLockAccountWhenThresholdReached() {
        // Given
        AuthenticationSession session = AuthenticationSession.create(
            SessionId.generate(), "user123", ClientIp.of("192.168.1.100"), 
            securityConstants.getSession().getMaxFailedAttempts(),
            securityConstants.getSession().getLockoutDurationMinutes(),
            securityConstants.getSession().getTimeWindowMinutes()
        );
        
        // 5번의 실패 시도 추가 후 확인
        for (int i = 0; i < 5; i++) {
            session.recordAttempt("user123", ClientIp.of("192.168.1.100"), 
                                false, RiskLevel.low("Attempt " + (i + 1)));
        }

        // When & Then
        // 5번의 실패 후에는 자동으로 잠금되어야 함 (recordAttempt 내부에서 처리)
        assertThat(session.isCurrentlyLocked()).isTrue();
        
        // 이미 잠금된 상태에서는 shouldLockAccount가 false를 반환 (이미 잠금된 상태이므로)
        assertThat(session.shouldLockAccount()).isFalse();
    }

    @Test
    @DisplayName("계정 잠금 실행")
    void shouldLockAccount() {
        // Given
        AuthenticationSession session = AuthenticationSession.create(
            SessionId.generate(), "user123", ClientIp.of("192.168.1.100"), 
            securityConstants.getSession().getMaxFailedAttempts(),
            securityConstants.getSession().getLockoutDurationMinutes(),
            securityConstants.getSession().getTimeWindowMinutes()
        );
        
        // 5번의 실패 시도
        for (int i = 0; i < 5; i++) {
            session.recordAttempt("user123", ClientIp.of("192.168.1.100"), 
                                false, RiskLevel.low("Attempt " + (i + 1)));
        }

        LocalDateTime beforeLock = LocalDateTime.now();

        // When
        session.lockAccount();

        // Then
        assertThat(session.isLocked()).isTrue();
        assertThat(session.getLockedUntil()).isAfter(beforeLock);
        assertThat(session.getLockedUntil()).isBefore(LocalDateTime.now().plusMinutes(31));
        
        // 도메인 이벤트 발생 확인
        assertThat(session.getDomainEvents()).hasSize(1);
        assertThat(session.getDomainEvents().get(0)).isInstanceOf(AccountLocked.class);
        
        AccountLocked event = (AccountLocked) session.getDomainEvents().get(0);
        assertThat(event.sessionId()).isEqualTo(session.getSessionId().getValue().toString());
        assertThat(event.userId()).isEqualTo(session.getUserId());
        assertThat(event.clientIp()).isEqualTo(session.getClientIp().getIpAddress());
        assertThat(event.failedAttemptCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("계정 잠금 해제")
    void shouldUnlockAccount() {
        // Given
        AuthenticationSession session = AuthenticationSession.create(
            SessionId.generate(), "user123", ClientIp.of("192.168.1.100"), 
            securityConstants.getSession().getMaxFailedAttempts(),
            securityConstants.getSession().getLockoutDurationMinutes(),
            securityConstants.getSession().getTimeWindowMinutes()
        );
        
        // 잠금 상태로 만들기
        for (int i = 0; i < 5; i++) {
            session.recordAttempt("user123", ClientIp.of("192.168.1.100"), 
                                false, RiskLevel.low("Attempt " + (i + 1)));
        }
        session.lockAccount();

        // When
        session.unlockAccount();

        // Then
        assertThat(session.isLocked()).isFalse();
        assertThat(session.getLockedUntil()).isNull();
    }

    @Test
    @DisplayName("현재 잠금 상태 확인 - 잠김")
    void shouldReturnTrueWhenCurrentlyLocked() {
        // Given
        AuthenticationSession session = AuthenticationSession.create(
            SessionId.generate(), "user123", ClientIp.of("192.168.1.100"), 
            securityConstants.getSession().getMaxFailedAttempts(),
            securityConstants.getSession().getLockoutDurationMinutes(),
            securityConstants.getSession().getTimeWindowMinutes()
        );
        
        // 잠금 상태로 만들기
        for (int i = 0; i < 5; i++) {
            session.recordAttempt("user123", ClientIp.of("192.168.1.100"), 
                                false, RiskLevel.low("Attempt " + (i + 1)));
        }
        session.lockAccount();

        // When
        boolean isCurrentlyLocked = session.isCurrentlyLocked();

        // Then
        assertThat(isCurrentlyLocked).isTrue();
    }

    @Test
    @DisplayName("현재 잠금 상태 확인 - 잠금 해제됨")
    void shouldReturnFalseWhenLockExpired() {
        // Given
        AuthenticationSession session = AuthenticationSession.create(
            SessionId.generate(), "user123", ClientIp.of("192.168.1.100"), 
            securityConstants.getSession().getMaxFailedAttempts(),
            securityConstants.getSession().getLockoutDurationMinutes(),
            securityConstants.getSession().getTimeWindowMinutes()
        );
        
        // 과거 시각으로 잠금 해제 시각 강제 설정 (테스트를 위해)
        session.recordAttempt("user123", ClientIp.of("192.168.1.100"), 
                            false, RiskLevel.low("Test attempt"));
        session.forceSetLockStatus(true, LocalDateTime.now().minusMinutes(1));

        // When
        boolean isCurrentlyLocked = session.isCurrentlyLocked();

        // Then
        assertThat(isCurrentlyLocked).isFalse();
    }

    @Test
    @DisplayName("시간 윈도우 내 실패 횟수 - 모두 포함")
    void shouldCountAllFailedAttemptsInWindow() {
        // Given
        AuthenticationSession session = AuthenticationSession.create(
            SessionId.generate(), "user123", ClientIp.of("192.168.1.100"), 
            securityConstants.getSession().getMaxFailedAttempts(),
            securityConstants.getSession().getLockoutDurationMinutes(),
            securityConstants.getSession().getTimeWindowMinutes()
        );
        
        // 시간 윈도우 내 3번의 실패 시도
        LocalDateTime now = LocalDateTime.now();
        session.recordAttemptAtSpecificTime("user123", ClientIp.of("192.168.1.100"), 
                                          false, RiskLevel.low("Attempt 1"), now.minusMinutes(5));
        session.recordAttemptAtSpecificTime("user123", ClientIp.of("192.168.1.100"), 
                                          false, RiskLevel.low("Attempt 2"), now.minusMinutes(10));
        session.recordAttemptAtSpecificTime("user123", ClientIp.of("192.168.1.100"), 
                                          false, RiskLevel.low("Attempt 3"), now.minusMinutes(14));

        // When
        int failedCount = session.getFailedAttemptsInWindow();

        // Then
        assertThat(failedCount).isEqualTo(3);
    }

    @Test
    @DisplayName("시간 윈도우 내 실패 횟수 - 일부만 포함")
    void shouldCountOnlyFailedAttemptsInWindow() {
        // Given
        AuthenticationSession session = AuthenticationSession.create(
            SessionId.generate(), "user123", ClientIp.of("192.168.1.100"), 
            securityConstants.getSession().getMaxFailedAttempts(),
            securityConstants.getSession().getLockoutDurationMinutes(),
            securityConstants.getSession().getTimeWindowMinutes()
        );
        
        // 윈도우 내 2번, 윈도우 밖 2번의 실패 시도
        LocalDateTime now = LocalDateTime.now();
        session.recordAttemptAtSpecificTime("user123", ClientIp.of("192.168.1.100"), 
                                          false, RiskLevel.low("Inside 1"), now.minusMinutes(5));  // 윈도우 내
        session.recordAttemptAtSpecificTime("user123", ClientIp.of("192.168.1.100"), 
                                          false, RiskLevel.low("Inside 2"), now.minusMinutes(10)); // 윈도우 내
        session.recordAttemptAtSpecificTime("user123", ClientIp.of("192.168.1.100"), 
                                          false, RiskLevel.low("Outside 1"), now.minusMinutes(20)); // 윈도우 밖
        session.recordAttemptAtSpecificTime("user123", ClientIp.of("192.168.1.100"), 
                                          false, RiskLevel.low("Outside 2"), now.minusMinutes(25)); // 윈도우 밖

        // When
        int failedCount = session.getFailedAttemptsInWindow();

        // Then
        assertThat(failedCount).isEqualTo(2);
    }

    @Test
    @DisplayName("성공 시도 후 실패 카운터 리셋")
    void shouldResetFailedCounterAfterSuccessfulAttempt() {
        // Given
        AuthenticationSession session = AuthenticationSession.create(
            SessionId.generate(), "user123", ClientIp.of("192.168.1.100"), 
            securityConstants.getSession().getMaxFailedAttempts(),
            securityConstants.getSession().getLockoutDurationMinutes(),
            securityConstants.getSession().getTimeWindowMinutes()
        );
        
        // 3번 실패 후 1번 성공
        LocalDateTime now = LocalDateTime.now();
        session.recordAttemptAtSpecificTime("user123", ClientIp.of("192.168.1.100"), 
                                          false, RiskLevel.low("Failed 1"), now.minusMinutes(10));
        session.recordAttemptAtSpecificTime("user123", ClientIp.of("192.168.1.100"), 
                                          false, RiskLevel.low("Failed 2"), now.minusMinutes(8));
        session.recordAttemptAtSpecificTime("user123", ClientIp.of("192.168.1.100"), 
                                          false, RiskLevel.low("Failed 3"), now.minusMinutes(6));
        session.recordAttemptAtSpecificTime("user123", ClientIp.of("192.168.1.100"), 
                                          true, RiskLevel.low("Success"), now.minusMinutes(4));  // 성공

        // When
        int failedCount = session.getFailedAttemptsInWindow();

        // Then
        assertThat(failedCount).isEqualTo(0); // 성공 이후 실패 없음
    }

    @Test
    @DisplayName("마지막 활동 시각 갱신")
    void shouldUpdateLastActivityTime() throws InterruptedException {
        // Given
        AuthenticationSession session = AuthenticationSession.create(
            SessionId.generate(), "user123", ClientIp.of("192.168.1.100"), 
            securityConstants.getSession().getMaxFailedAttempts(),
            securityConstants.getSession().getLockoutDurationMinutes(),
            securityConstants.getSession().getTimeWindowMinutes()
        );
        LocalDateTime initialLastActivity = session.getLastActivityAt();

        // When
        Thread.sleep(2); // 시간 차이를 위해
        session.updateLastActivity();

        // Then
        assertThat(session.getLastActivityAt()).isAfter(initialLastActivity);
    }
}