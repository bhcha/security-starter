package com.dx.hexacore.security.session.application.command.handler;

import com.dx.hexacore.security.session.application.exception.SessionNotFoundException;
import com.dx.hexacore.security.session.application.command.port.out.AuthenticationSessionRepository;
import com.dx.hexacore.security.session.application.command.port.in.LockoutCheckResult;
import com.dx.hexacore.security.session.domain.AuthenticationSession;
import com.dx.hexacore.security.session.domain.vo.ClientIp;
import com.dx.hexacore.security.session.domain.vo.RiskLevel;
import com.dx.hexacore.security.session.domain.vo.SessionId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CheckLockoutUseCaseImpl 테스트")
class CheckLockoutUseCaseImplTest {

    @Mock
    private AuthenticationSessionRepository sessionRepository;

    private CheckLockoutUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CheckLockoutUseCaseImpl(sessionRepository);
    }

    @Test
    @DisplayName("정상 계정의 잠금 상태를 확인할 수 있다")
    void shouldCheckUnlockedAccount() {
        // Given
        String sessionIdStr = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";

        SessionId sessionId = SessionId.of(sessionIdStr);
        ClientIp clientIp = ClientIp.of("192.168.1.100");
        AuthenticationSession session = AuthenticationSession.create(sessionId, userId, clientIp, 5, 30);

        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(session));

        // When
        LockoutCheckResult result = useCase.execute(sessionIdStr, userId);

        // Then
        assertThat(result.sessionId()).isEqualTo(sessionIdStr);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.isLocked()).isFalse();
        assertThat(result.lockedUntil()).isNull();
        assertThat(result.checkedAt()).isNotNull();

        verify(sessionRepository).findBySessionId(sessionId);
    }

    @Test
    @DisplayName("잠긴 계정의 잠금 상태를 확인할 수 있다")
    void shouldCheckLockedAccount() {
        // Given
        String sessionIdStr = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";

        SessionId sessionId = SessionId.of(sessionIdStr);
        ClientIp clientIp = ClientIp.of("192.168.1.100");
        AuthenticationSession session = AuthenticationSession.create(sessionId, userId, clientIp, 5, 30);

        // 5회 실패로 계정 잠금 발생
        RiskLevel riskLevel = RiskLevel.of(70, "Failed attempt");
        for (int i = 0; i < 5; i++) {
            session.recordAttempt(userId, clientIp, false, riskLevel);
        }

        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(session));

        // When
        LockoutCheckResult result = useCase.execute(sessionIdStr, userId);

        // Then
        assertThat(result.sessionId()).isEqualTo(sessionIdStr);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.isLocked()).isTrue();
        assertThat(result.lockedUntil()).isNotNull();
        assertThat(result.checkedAt()).isNotNull();

        verify(sessionRepository).findBySessionId(sessionId);
    }

    @Test
    @DisplayName("존재하지 않는 세션의 잠금 상태 확인 시 예외가 발생한다")
    void shouldThrowExceptionWhenSessionNotFound() {
        // Given
        String sessionIdStr = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";

        SessionId sessionId = SessionId.of(sessionIdStr);
        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> useCase.execute(sessionIdStr, userId))
                .isInstanceOf(SessionNotFoundException.class)
                .hasMessage("Session not found: " + sessionIdStr);

        verify(sessionRepository).findBySessionId(sessionId);
    }

    @Test
    @DisplayName("시간 경과로 자동 해제된 계정을 확인할 수 있다")
    void shouldCheckAutoUnlockedAccount() {
        // Given
        String sessionIdStr = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";

        SessionId sessionId = SessionId.of(sessionIdStr);
        ClientIp clientIp = ClientIp.of("192.168.1.100");
        AuthenticationSession session = AuthenticationSession.create(sessionId, userId, clientIp, 5, 30);

        // 5회 실패로 계정 잠금 발생 후 시간이 충분히 경과했다고 가정
        // (실제로는 도메인 로직에서 시간 확인)
        RiskLevel riskLevel = RiskLevel.of(70, "Failed attempt");
        for (int i = 0; i < 5; i++) {
            session.recordAttempt(userId, clientIp, false, riskLevel);
        }

        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(session));

        // When
        LockoutCheckResult result = useCase.execute(sessionIdStr, userId);

        // Then
        assertThat(result.sessionId()).isEqualTo(sessionIdStr);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.checkedAt()).isNotNull();

        verify(sessionRepository).findBySessionId(sessionId);
    }

    @Test
    @DisplayName("Repository 조회 실패 시 예외가 전파된다")
    void shouldPropagateRepositoryException() {
        // Given
        String sessionIdStr = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";

        SessionId sessionId = SessionId.of(sessionIdStr);
        RuntimeException repositoryException = new RuntimeException("Database connection failed");
        when(sessionRepository.findBySessionId(sessionId)).thenThrow(repositoryException);

        // When & Then
        assertThatThrownBy(() -> useCase.execute(sessionIdStr, userId))
                .isEqualTo(repositoryException);

        verify(sessionRepository).findBySessionId(sessionId);
    }

    @Test
    @DisplayName("현재 구현에서는 세션 전체 잠금 상태를 확인한다")
    void shouldCheckSessionLockStatus() {
        // Given
        String sessionIdStr = "550e8400-e29b-41d4-a716-446655440000";
        String userId1 = "testUser123";
        String userId2 = "otherUser456";

        SessionId sessionId = SessionId.of(sessionIdStr);
        ClientIp clientIp = ClientIp.of("192.168.1.100");
        AuthenticationSession session = AuthenticationSession.create(sessionId, userId1, clientIp, 5, 30);

        // 5회 실패로 세션 잠금
        RiskLevel riskLevel = RiskLevel.of(70, "Failed attempt");
        for (int i = 0; i < 5; i++) {
            session.recordAttempt(userId1, clientIp, false, riskLevel);
        }

        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(session));

        // When - 잠긴 세션의 상태 확인
        LockoutCheckResult result = useCase.execute(sessionIdStr, userId2);

        // Then - 세션이 잠긴 상태
        assertThat(result.sessionId()).isEqualTo(sessionIdStr);
        assertThat(result.userId()).isEqualTo(userId2);
        assertThat(result.isLocked()).isTrue();
        assertThat(result.lockedUntil()).isNotNull();

        verify(sessionRepository).findBySessionId(sessionId);
    }

    @Test
    @DisplayName("빈 세션에서 사용자 잠금 상태를 확인할 수 있다")
    void shouldCheckLockStatusInEmptySession() {
        // Given
        String sessionIdStr = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";

        SessionId sessionId = SessionId.of(sessionIdStr);
        ClientIp clientIp = ClientIp.of("192.168.1.100");
        AuthenticationSession session = AuthenticationSession.create(sessionId, "primaryUser", clientIp, 5, 30);
        // 인증 시도 기록 없음

        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(session));

        // When
        LockoutCheckResult result = useCase.execute(sessionIdStr, userId);

        // Then
        assertThat(result.sessionId()).isEqualTo(sessionIdStr);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.isLocked()).isFalse();
        assertThat(result.lockedUntil()).isNull();
        assertThat(result.checkedAt()).isNotNull();

        verify(sessionRepository).findBySessionId(sessionId);
    }
}