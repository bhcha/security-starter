package com.ldx.hexacore.security.session.application.query.handler;

import com.ldx.hexacore.security.session.application.exception.SessionNotFoundException;
import com.ldx.hexacore.security.session.application.exception.SessionQueryException;
import com.ldx.hexacore.security.session.application.query.port.out.LoadFailedAttemptsQueryPort;
import com.ldx.hexacore.security.session.application.query.port.out.LoadSessionStatusQueryPort;
import com.ldx.hexacore.security.session.application.projection.FailedAttemptProjection;
import com.ldx.hexacore.security.session.application.projection.SessionStatusProjection;
import com.ldx.hexacore.security.session.application.query.port.in.GetFailedAttemptsQuery;
import com.ldx.hexacore.security.session.application.query.port.in.GetSessionStatusQuery;
import com.ldx.hexacore.security.session.application.query.port.in.FailedAttemptsResponse;
import com.ldx.hexacore.security.session.application.query.port.in.SessionStatusResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SessionQueryHandler 테스트")
class SessionQueryHandlerTest {

    @Mock
    private LoadSessionStatusQueryPort loadSessionStatusQueryPort;

    @Mock
    private LoadFailedAttemptsQueryPort loadFailedAttemptsQueryPort;

    private SessionQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new SessionQueryHandler(loadSessionStatusQueryPort, loadFailedAttemptsQueryPort);
    }

    @Test
    @DisplayName("존재하는 세션의 상태를 조회할 수 있다")
    void shouldLoadExistingSessionStatus() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        GetSessionStatusQuery query = new GetSessionStatusQuery(sessionId, null);

        SessionStatusProjection projection = new SessionStatusProjection(
                sessionId, "testUser123", "192.168.1.100", false, null,
                LocalDateTime.now().minusHours(1), LocalDateTime.now(), 5, 1);

        when(loadSessionStatusQueryPort.loadSessionStatus(sessionId))
                .thenReturn(Optional.of(projection));

        // When
        SessionStatusResponse response = handler.handle(query);

        // Then
        assertThat(response.sessionId()).isEqualTo(sessionId);
        assertThat(response.primaryUserId()).isEqualTo("testUser123");
        assertThat(response.primaryClientIp()).isEqualTo("192.168.1.100");
        assertThat(response.isLocked()).isFalse();
        assertThat(response.totalAttempts()).isEqualTo(5);
        assertThat(response.failedAttempts()).isEqualTo(1);

        verify(loadSessionStatusQueryPort).loadSessionStatus(sessionId);
    }

    @Test
    @DisplayName("존재하지 않는 세션 조회 시 예외가 발생한다")
    void shouldThrowExceptionWhenSessionNotFound() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        GetSessionStatusQuery query = new GetSessionStatusQuery(sessionId, null);

        when(loadSessionStatusQueryPort.loadSessionStatus(sessionId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> handler.handle(query))
                .isInstanceOf(SessionNotFoundException.class)
                .hasMessage("Session not found: " + sessionId);

        verify(loadSessionStatusQueryPort).loadSessionStatus(sessionId);
    }

    @Test
    @DisplayName("사용자별 세션 상태를 조회할 수 있다")
    void shouldLoadSessionStatusByUser() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        GetSessionStatusQuery query = new GetSessionStatusQuery(sessionId, userId);

        SessionStatusProjection projection = new SessionStatusProjection(
                sessionId, userId, "192.168.1.100", false, null,
                LocalDateTime.now().minusHours(1), LocalDateTime.now(), 3, 0);

        when(loadSessionStatusQueryPort.loadSessionStatusByUser(sessionId, userId))
                .thenReturn(Optional.of(projection));

        // When
        SessionStatusResponse response = handler.handle(query);

        // Then
        assertThat(response.sessionId()).isEqualTo(sessionId);
        assertThat(response.primaryUserId()).isEqualTo(userId);
        assertThat(response.totalAttempts()).isEqualTo(3);
        assertThat(response.failedAttempts()).isEqualTo(0);

        verify(loadSessionStatusQueryPort).loadSessionStatusByUser(sessionId, userId);
    }

    @Test
    @DisplayName("Query Port 조회 실패 시 SessionQueryException이 발생한다")
    void shouldThrowSessionQueryExceptionWhenPortFails() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        GetSessionStatusQuery query = new GetSessionStatusQuery(sessionId, null);

        when(loadSessionStatusQueryPort.loadSessionStatus(sessionId))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThatThrownBy(() -> handler.handle(query))
                .isInstanceOf(SessionQueryException.class)
                .hasMessage("Failed to load session status: " + sessionId)
                .hasCauseInstanceOf(RuntimeException.class);

        verify(loadSessionStatusQueryPort).loadSessionStatus(sessionId);
    }

    @Test
    @DisplayName("실패한 인증 시도 목록을 조회할 수 있다")
    void shouldLoadFailedAttempts() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        LocalDateTime from = LocalDateTime.now().minusHours(1);
        LocalDateTime to = LocalDateTime.now();
        int limit = 10;

        GetFailedAttemptsQuery query = new GetFailedAttemptsQuery(sessionId, null, from, to, limit);

        List<FailedAttemptProjection> projections = List.of(
                new FailedAttemptProjection("user1", "192.168.1.100", 60, "Wrong password", from.plusMinutes(10)),
                new FailedAttemptProjection("user2", "192.168.1.101", 70, "Invalid user", from.plusMinutes(20))
        );

        when(loadFailedAttemptsQueryPort.loadFailedAttempts(sessionId, from, to, limit))
                .thenReturn(projections);
        when(loadFailedAttemptsQueryPort.countFailedAttempts(sessionId, from, to))
                .thenReturn(5);

        // When
        FailedAttemptsResponse response = handler.handle(query);

        // Then
        assertThat(response.sessionId()).isEqualTo(sessionId);
        assertThat(response.attempts()).hasSize(2);
        assertThat(response.totalCount()).isEqualTo(5);
        assertThat(response.queriedAt()).isNotNull();

        assertThat(response.attempts().get(0).userId()).isEqualTo("user1");
        assertThat(response.attempts().get(0).riskScore()).isEqualTo(60);
        assertThat(response.attempts().get(1).userId()).isEqualTo("user2");
        assertThat(response.attempts().get(1).riskScore()).isEqualTo(70);

        verify(loadFailedAttemptsQueryPort).loadFailedAttempts(sessionId, from, to, limit);
        verify(loadFailedAttemptsQueryPort).countFailedAttempts(sessionId, from, to);
    }

    @Test
    @DisplayName("사용자별 실패한 인증 시도 목록을 조회할 수 있다")
    void shouldLoadFailedAttemptsByUser() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        LocalDateTime from = LocalDateTime.now().minusHours(1);
        LocalDateTime to = LocalDateTime.now();
        int limit = 10;

        GetFailedAttemptsQuery query = new GetFailedAttemptsQuery(sessionId, userId, from, to, limit);

        List<FailedAttemptProjection> projections = List.of(
                new FailedAttemptProjection(userId, "192.168.1.100", 60, "Wrong password", from.plusMinutes(10))
        );

        when(loadFailedAttemptsQueryPort.loadFailedAttemptsByUser(sessionId, userId, from, to, limit))
                .thenReturn(projections);
        when(loadFailedAttemptsQueryPort.countFailedAttemptsByUser(sessionId, userId, from, to))
                .thenReturn(3);

        // When
        FailedAttemptsResponse response = handler.handle(query);

        // Then
        assertThat(response.sessionId()).isEqualTo(sessionId);
        assertThat(response.attempts()).hasSize(1);
        assertThat(response.totalCount()).isEqualTo(3);
        assertThat(response.attempts().get(0).userId()).isEqualTo(userId);

        verify(loadFailedAttemptsQueryPort).loadFailedAttemptsByUser(sessionId, userId, from, to, limit);
        verify(loadFailedAttemptsQueryPort).countFailedAttemptsByUser(sessionId, userId, from, to);
    }

    @Test
    @DisplayName("빈 실패 시도 목록을 조회할 수 있다")
    void shouldLoadEmptyFailedAttempts() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        LocalDateTime from = LocalDateTime.now().minusHours(1);
        LocalDateTime to = LocalDateTime.now();
        int limit = 10;

        GetFailedAttemptsQuery query = new GetFailedAttemptsQuery(sessionId, null, from, to, limit);

        when(loadFailedAttemptsQueryPort.loadFailedAttempts(sessionId, from, to, limit))
                .thenReturn(List.of());
        when(loadFailedAttemptsQueryPort.countFailedAttempts(sessionId, from, to))
                .thenReturn(0);

        // When
        FailedAttemptsResponse response = handler.handle(query);

        // Then
        assertThat(response.sessionId()).isEqualTo(sessionId);
        assertThat(response.attempts()).isEmpty();
        assertThat(response.totalCount()).isEqualTo(0);
        assertThat(response.queriedAt()).isNotNull();

        verify(loadFailedAttemptsQueryPort).loadFailedAttempts(sessionId, from, to, limit);
        verify(loadFailedAttemptsQueryPort).countFailedAttempts(sessionId, from, to);
    }

    @Test
    @DisplayName("실패 시도 조회 중 예외 발생 시 SessionQueryException이 발생한다")
    void shouldThrowSessionQueryExceptionWhenFailedAttemptsQueryFails() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        LocalDateTime from = LocalDateTime.now().minusHours(1);
        LocalDateTime to = LocalDateTime.now();
        int limit = 10;

        GetFailedAttemptsQuery query = new GetFailedAttemptsQuery(sessionId, null, from, to, limit);

        when(loadFailedAttemptsQueryPort.loadFailedAttempts(sessionId, from, to, limit))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThatThrownBy(() -> handler.handle(query))
                .isInstanceOf(SessionQueryException.class)
                .hasMessage("Failed to load failed attempts: " + sessionId)
                .hasCauseInstanceOf(RuntimeException.class);

        verify(loadFailedAttemptsQueryPort).loadFailedAttempts(sessionId, from, to, limit);
    }
}