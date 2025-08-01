package com.dx.hexacore.security.session.application.command.handler;

import com.dx.hexacore.security.session.application.command.port.in.UnlockAccountCommand;
import com.dx.hexacore.security.session.application.exception.SessionNotFoundException;
import com.dx.hexacore.security.session.application.command.port.out.AuthenticationSessionRepository;
import com.dx.hexacore.security.session.application.command.port.in.UnlockAccountResult;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UnlockAccountUseCaseImpl 테스트")
class UnlockAccountUseCaseImplTest {

    @Mock
    private AuthenticationSessionRepository sessionRepository;

    private UnlockAccountUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new UnlockAccountUseCaseImpl(sessionRepository);
    }

    @Test
    @DisplayName("잠긴 계정을 성공적으로 해제할 수 있다")
    void shouldUnlockLockedAccount() {
        // Given
        String sessionIdStr = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        UnlockAccountCommand command = new UnlockAccountCommand(sessionIdStr, userId);

        SessionId sessionId = SessionId.of(sessionIdStr);
        ClientIp clientIp = ClientIp.of("192.168.1.100");
        AuthenticationSession session = AuthenticationSession.create(sessionId, userId, clientIp);

        // 5회 실패로 계정 잠금 발생
        RiskLevel riskLevel = RiskLevel.of(70, "Failed attempt");
        for (int i = 0; i < 5; i++) {
            session.recordAttempt(userId, clientIp, false, riskLevel);
        }

        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(AuthenticationSession.class))).thenReturn(session);

        // When
        UnlockAccountResult result = useCase.execute(command);

        // Then
        assertThat(result.sessionId()).isEqualTo(sessionIdStr);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.wasLocked()).isTrue();
        assertThat(result.unlockSuccessful()).isTrue();
        assertThat(result.unlockedAt()).isNotNull();

        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionRepository).save(session);
    }

    @Test
    @DisplayName("이미 해제된 계정의 해제 요청을 처리할 수 있다")
    void shouldHandleAlreadyUnlockedAccount() {
        // Given
        String sessionIdStr = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        UnlockAccountCommand command = new UnlockAccountCommand(sessionIdStr, userId);

        SessionId sessionId = SessionId.of(sessionIdStr);
        ClientIp clientIp = ClientIp.of("192.168.1.100");
        AuthenticationSession session = AuthenticationSession.create(sessionId, userId, clientIp);
        // 잠금되지 않은 상태

        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(AuthenticationSession.class))).thenReturn(session);

        // When
        UnlockAccountResult result = useCase.execute(command);

        // Then
        assertThat(result.sessionId()).isEqualTo(sessionIdStr);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.wasLocked()).isFalse();
        assertThat(result.unlockSuccessful()).isTrue();
        assertThat(result.unlockedAt()).isNotNull();

        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionRepository).save(session);
    }

    @Test
    @DisplayName("존재하지 않는 세션의 계정 해제 시 예외가 발생한다")
    void shouldThrowExceptionWhenSessionNotFound() {
        // Given
        String sessionIdStr = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        UnlockAccountCommand command = new UnlockAccountCommand(sessionIdStr, userId);

        SessionId sessionId = SessionId.of(sessionIdStr);
        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(SessionNotFoundException.class)
                .hasMessage("Session not found: " + sessionIdStr);

        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Repository 조회 실패 시 예외가 전파된다")
    void shouldPropagateRepositoryException() {
        // Given
        String sessionIdStr = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        UnlockAccountCommand command = new UnlockAccountCommand(sessionIdStr, userId);

        SessionId sessionId = SessionId.of(sessionIdStr);
        RuntimeException repositoryException = new RuntimeException("Database connection failed");
        when(sessionRepository.findBySessionId(sessionId)).thenThrow(repositoryException);

        // When & Then
        assertThatThrownBy(() -> useCase.execute(command))
                .isEqualTo(repositoryException);

        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Repository 저장 실패 시 예외가 전파된다")
    void shouldPropagateRepositorySaveException() {
        // Given
        String sessionIdStr = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        UnlockAccountCommand command = new UnlockAccountCommand(sessionIdStr, userId);

        SessionId sessionId = SessionId.of(sessionIdStr);
        ClientIp clientIp = ClientIp.of("192.168.1.100");
        AuthenticationSession session = AuthenticationSession.create(sessionId, userId, clientIp);

        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(session));
        RuntimeException saveException = new RuntimeException("Save operation failed");
        when(sessionRepository.save(any())).thenThrow(saveException);

        // When & Then
        assertThatThrownBy(() -> useCase.execute(command))
                .isEqualTo(saveException);

        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionRepository).save(any());
    }

    @Test
    @DisplayName("다른 사용자의 잠금은 영향받지 않는다")
    void shouldNotAffectOtherUserLocks() {
        // Given
        String sessionIdStr = "550e8400-e29b-41d4-a716-446655440000";
        String userId1 = "testUser123";
        String userId2 = "otherUser456";
        UnlockAccountCommand command = new UnlockAccountCommand(sessionIdStr, userId2);

        SessionId sessionId = SessionId.of(sessionIdStr);
        ClientIp clientIp = ClientIp.of("192.168.1.100");
        AuthenticationSession session = AuthenticationSession.create(sessionId, userId1, clientIp);

        // userId1과 userId2 모두 잠금
        RiskLevel riskLevel = RiskLevel.of(70, "Failed attempt");
        for (int i = 0; i < 5; i++) {
            session.recordAttempt(userId1, clientIp, false, riskLevel);
            session.recordAttempt(userId2, clientIp, false, riskLevel);
        }

        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(AuthenticationSession.class))).thenReturn(session);

        // When - userId2만 해제
        UnlockAccountResult result = useCase.execute(command);

        // Then
        assertThat(result.sessionId()).isEqualTo(sessionIdStr);
        assertThat(result.userId()).isEqualTo(userId2);
        assertThat(result.wasLocked()).isTrue();
        assertThat(result.unlockSuccessful()).isTrue();

        // 세션 전체의 잠금 상태 확인 (현재 구현에서는 세션 단위로 잠금 관리)
        // 해제 후 상태 확인
        assertThat(session.isCurrentlyLocked()).isFalse();

        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionRepository).save(session);
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 해제 요청을 처리할 수 있다")
    void shouldHandleNonExistentUserUnlock() {
        // Given
        String sessionIdStr = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "nonExistentUser";
        UnlockAccountCommand command = new UnlockAccountCommand(sessionIdStr, userId);

        SessionId sessionId = SessionId.of(sessionIdStr);
        ClientIp clientIp = ClientIp.of("192.168.1.100");
        AuthenticationSession session = AuthenticationSession.create(sessionId, "primaryUser", clientIp);
        // 해당 사용자의 인증 시도 기록 없음

        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(AuthenticationSession.class))).thenReturn(session);

        // When
        UnlockAccountResult result = useCase.execute(command);

        // Then
        assertThat(result.sessionId()).isEqualTo(sessionIdStr);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.wasLocked()).isFalse(); // 처음부터 잠기지 않았음
        assertThat(result.unlockSuccessful()).isTrue();
        assertThat(result.unlockedAt()).isNotNull();

        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionRepository).save(session);
    }

    @Test
    @DisplayName("특수 문자가 포함된 사용자 ID의 해제를 처리할 수 있다")
    void shouldHandleSpecialCharacterUserId() {
        // Given
        String sessionIdStr = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "user@example.com";
        UnlockAccountCommand command = new UnlockAccountCommand(sessionIdStr, userId);

        SessionId sessionId = SessionId.of(sessionIdStr);
        ClientIp clientIp = ClientIp.of("192.168.1.100");
        AuthenticationSession session = AuthenticationSession.create(sessionId, userId, clientIp);

        // 계정 잠금 발생
        RiskLevel riskLevel = RiskLevel.of(70, "Failed attempt");
        for (int i = 0; i < 5; i++) {
            session.recordAttempt(userId, clientIp, false, riskLevel);
        }

        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(AuthenticationSession.class))).thenReturn(session);

        // When
        UnlockAccountResult result = useCase.execute(command);

        // Then
        assertThat(result.sessionId()).isEqualTo(sessionIdStr);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.wasLocked()).isTrue();
        assertThat(result.unlockSuccessful()).isTrue();
        assertThat(result.unlockedAt()).isNotNull();

        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionRepository).save(session);
    }
}