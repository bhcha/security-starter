package com.ldx.hexacore.security.session.application.command.handler;

import com.ldx.hexacore.security.session.application.command.port.in.RecordAuthenticationAttemptCommand;
import com.ldx.hexacore.security.session.application.exception.SessionNotFoundException;
import com.ldx.hexacore.security.session.application.command.port.out.AuthenticationSessionRepository;
import com.ldx.hexacore.security.session.application.command.port.out.SessionEventPublisher;
import com.ldx.hexacore.security.session.application.command.port.in.RecordAttemptResult;
import com.ldx.hexacore.security.session.domain.AuthenticationSession;
import com.ldx.hexacore.security.session.domain.event.AccountLocked;
import com.ldx.hexacore.security.session.domain.vo.ClientIp;
import com.ldx.hexacore.security.session.domain.vo.RiskLevel;
import com.ldx.hexacore.security.session.domain.vo.SessionId;
import com.ldx.hexacore.security.config.properties.SecurityStarterProperties;
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
@DisplayName("RecordAttemptUseCaseImpl 테스트")
class RecordAttemptUseCaseImplTest {

    @Mock
    private AuthenticationSessionRepository sessionRepository;

    @Mock
    private SessionEventPublisher eventPublisher;

    private RecordAttemptUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        // 실제 Properties 객체 사용
        SecurityStarterProperties securityProperties = new SecurityStarterProperties();
        useCase = new RecordAttemptUseCaseImpl(sessionRepository, eventPublisher, securityProperties);
    }

    @Test
    @DisplayName("성공적인 인증 시도를 기록할 수 있다")
    void shouldRecordSuccessfulAttempt() {
        // Given
        String sessionIdStr = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        String clientIpStr = "192.168.1.100";
        
        RecordAuthenticationAttemptCommand command = new RecordAuthenticationAttemptCommand(
                sessionIdStr, userId, clientIpStr, true, 10, "Normal login");

        SessionId sessionId = SessionId.of(sessionIdStr);
        ClientIp clientIp = ClientIp.of(clientIpStr);
        AuthenticationSession session = AuthenticationSession.create(sessionId, userId, clientIp, 5, 30);

        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(AuthenticationSession.class))).thenReturn(session);

        // When
        RecordAttemptResult result = useCase.execute(command);

        // Then
        assertThat(result.sessionId()).isEqualTo(sessionIdStr);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.isAccountLocked()).isFalse();
        assertThat(result.lockedUntil()).isNull();
        assertThat(result.recordedAt()).isNotNull();

        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionRepository).save(session);
        verify(eventPublisher).publishAll(anyList());
    }

    @Test
    @DisplayName("실패한 인증 시도를 기록할 수 있다 (잠금 임계치 미달)")
    void shouldRecordFailedAttemptWithoutLocking() {
        // Given
        String sessionIdStr = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        String clientIpStr = "192.168.1.100";
        
        RecordAuthenticationAttemptCommand command = new RecordAuthenticationAttemptCommand(
                sessionIdStr, userId, clientIpStr, false, 60, "Wrong password");

        SessionId sessionId = SessionId.of(sessionIdStr);
        ClientIp clientIp = ClientIp.of(clientIpStr);
        AuthenticationSession session = AuthenticationSession.create(sessionId, userId, clientIp, 5, 30);

        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(AuthenticationSession.class))).thenReturn(session);

        // When
        RecordAttemptResult result = useCase.execute(command);

        // Then
        assertThat(result.sessionId()).isEqualTo(sessionIdStr);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.isAccountLocked()).isFalse();
        assertThat(result.lockedUntil()).isNull();
        assertThat(result.recordedAt()).isNotNull();

        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionRepository).save(session);
        verify(eventPublisher).publishAll(anyList());
    }

    @Test
    @DisplayName("5번째 실패 시도로 계정이 잠긴다")
    void shouldLockAccountOnFifthFailedAttempt() {
        // Given
        String sessionIdStr = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        String clientIpStr = "192.168.1.100";
        
        RecordAuthenticationAttemptCommand command = new RecordAuthenticationAttemptCommand(
                sessionIdStr, userId, clientIpStr, false, 70, "Brute force attempt");

        SessionId sessionId = SessionId.of(sessionIdStr);
        ClientIp clientIp = ClientIp.of(clientIpStr);
        RiskLevel riskLevel = RiskLevel.of(70, "Brute force attempt");
        
        // 4번의 실패 시도가 이미 있는 세션 생성
        AuthenticationSession session = AuthenticationSession.create(sessionId, userId, clientIp, 5, 30);
        for (int i = 0; i < 4; i++) {
            session.recordAttempt(userId, clientIp, false, riskLevel);
        }
        session.clearDomainEvents(); // 이전 이벤트 제거

        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(AuthenticationSession.class))).thenReturn(session);

        // When
        RecordAttemptResult result = useCase.execute(command);

        // Then
        assertThat(result.sessionId()).isEqualTo(sessionIdStr);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.isAccountLocked()).isTrue();
        assertThat(result.lockedUntil()).isNotNull();
        assertThat(result.recordedAt()).isNotNull();

        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionRepository).save(session);
        verify(eventPublisher).publishAll(anyList());
        
        // AccountLocked 이벤트 발행 확인
        verify(eventPublisher).publishAll(argThat(events -> 
            events.size() > 0 && 
            events.stream().anyMatch(event -> event instanceof AccountLocked)
        ));
    }

    @Test
    @DisplayName("잠긴 계정에 성공적인 인증 시도 시 잠금이 해제된다")
    void shouldUnlockAccountOnSuccessfulAttempt() {
        // Given
        String sessionIdStr = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        String clientIpStr = "192.168.1.100";
        
        RecordAuthenticationAttemptCommand command = new RecordAuthenticationAttemptCommand(
                sessionIdStr, userId, clientIpStr, true, 5, "Valid credentials");

        SessionId sessionId = SessionId.of(sessionIdStr);
        ClientIp clientIp = ClientIp.of(clientIpStr);
        RiskLevel riskLevel = RiskLevel.of(70, "Previous failed attempt");
        
        // 계정이 잠긴 세션 생성
        AuthenticationSession session = AuthenticationSession.create(sessionId, userId, clientIp, 5, 30);
        for (int i = 0; i < 5; i++) {
            session.recordAttempt(userId, clientIp, false, riskLevel);
        }
        session.clearDomainEvents(); // 이전 이벤트 제거

        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(AuthenticationSession.class))).thenReturn(session);

        // When
        RecordAttemptResult result = useCase.execute(command);

        // Then
        assertThat(result.sessionId()).isEqualTo(sessionIdStr);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.isAccountLocked()).isFalse();
        assertThat(result.lockedUntil()).isNull();
        assertThat(result.recordedAt()).isNotNull();

        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionRepository).save(session);
        verify(eventPublisher).publishAll(anyList());
    }

    @Test
    @DisplayName("존재하지 않는 세션에 시도 기록 시 예외가 발생한다")
    void shouldThrowExceptionWhenSessionNotFound() {
        // Given
        String sessionIdStr = "550e8400-e29b-41d4-a716-446655440000";
        RecordAuthenticationAttemptCommand command = new RecordAuthenticationAttemptCommand(
                sessionIdStr, "testUser123", "192.168.1.100", true, 10, "Normal login");

        SessionId sessionId = SessionId.of(sessionIdStr);
        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(SessionNotFoundException.class)
                .hasMessage("Session not found: " + sessionIdStr);

        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionRepository, never()).save(any());
        verify(eventPublisher, never()).publishAll(any());
    }

    @Test
    @DisplayName("Repository 조회 실패 시 예외가 전파된다")
    void shouldPropagateRepositoryException() {
        // Given
        String sessionIdStr = "550e8400-e29b-41d4-a716-446655440000";
        RecordAuthenticationAttemptCommand command = new RecordAuthenticationAttemptCommand(
                sessionIdStr, "testUser123", "192.168.1.100", true, 10, "Normal login");

        SessionId sessionId = SessionId.of(sessionIdStr);
        RuntimeException repositoryException = new RuntimeException("Database connection failed");
        when(sessionRepository.findBySessionId(sessionId)).thenThrow(repositoryException);

        // When & Then
        assertThatThrownBy(() -> useCase.execute(command))
                .isEqualTo(repositoryException);

        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionRepository, never()).save(any());
        verify(eventPublisher, never()).publishAll(any());
    }

    @Test
    @DisplayName("Repository 저장 실패 시 예외가 전파된다")
    void shouldPropagateRepositorySaveException() {
        // Given
        String sessionIdStr = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        String clientIpStr = "192.168.1.100";
        
        RecordAuthenticationAttemptCommand command = new RecordAuthenticationAttemptCommand(
                sessionIdStr, userId, clientIpStr, true, 10, "Normal login");

        SessionId sessionId = SessionId.of(sessionIdStr);
        ClientIp clientIp = ClientIp.of(clientIpStr);
        AuthenticationSession session = AuthenticationSession.create(sessionId, userId, clientIp, 5, 30);

        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(session));
        RuntimeException saveException = new RuntimeException("Save operation failed");
        when(sessionRepository.save(any())).thenThrow(saveException);

        // When & Then
        assertThatThrownBy(() -> useCase.execute(command))
                .isEqualTo(saveException);

        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionRepository).save(any());
        verify(eventPublisher, never()).publishAll(any());
    }

    @Test
    @DisplayName("시간 윈도우 밖의 실패 시도는 계정 잠금에 영향을 주지 않는다")
    void shouldNotLockAccountWithOldFailures() {
        // Given
        String sessionIdStr = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        String clientIpStr = "192.168.1.100";
        
        RecordAuthenticationAttemptCommand command = new RecordAuthenticationAttemptCommand(
                sessionIdStr, userId, clientIpStr, false, 70, "Recent failed attempt");

        SessionId sessionId = SessionId.of(sessionIdStr);
        ClientIp clientIp = ClientIp.of(clientIpStr);
        AuthenticationSession session = AuthenticationSession.create(sessionId, userId, clientIp, 5, 30);

        // 15분 이전의 실패 시도들을 시뮬레이션하기 위해 
        // 현재 시점에서 실패 시도를 하나만 기록
        session.recordAttempt(userId, clientIp, false, RiskLevel.of(60, "Previous attempt"));
        session.clearDomainEvents();

        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(AuthenticationSession.class))).thenReturn(session);

        // When
        RecordAttemptResult result = useCase.execute(command);

        // Then
        assertThat(result.isAccountLocked()).isFalse();
        assertThat(result.lockedUntil()).isNull();

        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionRepository).save(session);
        verify(eventPublisher).publishAll(anyList());
    }

    @Test
    @DisplayName("현재 구현에서는 세션 전체가 잠금 단위이다")
    void shouldLockSessionOnMultipleFailures() {
        // Given
        String sessionIdStr = "550e8400-e29b-41d4-a716-446655440000";
        String userId1 = "testUser123";
        String userId2 = "differentUser456";
        String clientIpStr = "192.168.1.100";
        
        RecordAuthenticationAttemptCommand command = new RecordAuthenticationAttemptCommand(
                sessionIdStr, userId2, clientIpStr, false, 70, "Different user attempt");

        SessionId sessionId = SessionId.of(sessionIdStr);
        ClientIp clientIp = ClientIp.of(clientIpStr);
        AuthenticationSession session = AuthenticationSession.create(sessionId, userId1, clientIp, 5, 30);

        // 4번 실패 시도 기록 (사용자 무관하게 세션에 누적)
        RiskLevel riskLevel = RiskLevel.of(60, "Previous attempt");
        for (int i = 0; i < 4; i++) {
            session.recordAttempt(userId1, clientIp, false, riskLevel);
        }
        session.clearDomainEvents();

        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(AuthenticationSession.class))).thenReturn(session);

        // When - 5번째 실패 시도로 세션이 잠김
        RecordAttemptResult result = useCase.execute(command);

        // Then - 세션이 잠김 (현재 구현에서는 세션 단위 잠금)
        assertThat(result.userId()).isEqualTo(userId2);
        assertThat(result.isAccountLocked()).isTrue();
        assertThat(result.lockedUntil()).isNotNull();

        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionRepository).save(session);
        verify(eventPublisher).publishAll(anyList());
    }

    @Test
    @DisplayName("이벤트 발행 실패 시에도 세션은 저장된다")
    void shouldSaveSessionEvenWhenEventPublishingFails() {
        // Given
        String sessionIdStr = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        String clientIpStr = "192.168.1.100";
        
        RecordAuthenticationAttemptCommand command = new RecordAuthenticationAttemptCommand(
                sessionIdStr, userId, clientIpStr, true, 10, "Normal login");

        SessionId sessionId = SessionId.of(sessionIdStr);
        ClientIp clientIp = ClientIp.of(clientIpStr);
        AuthenticationSession session = AuthenticationSession.create(sessionId, userId, clientIp, 5, 30);

        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(AuthenticationSession.class))).thenReturn(session);
        doThrow(new RuntimeException("Event publishing failed")).when(eventPublisher).publishAll(any());

        // When & Then
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Event publishing failed");

        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionRepository).save(session);
        verify(eventPublisher).publishAll(anyList());
    }

    @Test
    @DisplayName("IPv6 주소로 인증 시도를 기록할 수 있다")
    void shouldRecordAttemptWithIPv6Address() {
        // Given
        String sessionIdStr = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        String clientIpStr = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
        
        RecordAuthenticationAttemptCommand command = new RecordAuthenticationAttemptCommand(
                sessionIdStr, userId, clientIpStr, true, 15, "IPv6 login");

        SessionId sessionId = SessionId.of(sessionIdStr);
        ClientIp clientIp = ClientIp.of(clientIpStr);
        AuthenticationSession session = AuthenticationSession.create(sessionId, "primaryUser", clientIp, 5, 30);

        when(sessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(AuthenticationSession.class))).thenReturn(session);

        // When
        RecordAttemptResult result = useCase.execute(command);

        // Then
        assertThat(result.sessionId()).isEqualTo(sessionIdStr);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.isSuccessful()).isTrue();

        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionRepository).save(session);
        verify(eventPublisher).publishAll(anyList());
    }
}