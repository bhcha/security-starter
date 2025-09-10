package com.ldx.hexacore.security.session.adapter.outbound.memory;

import com.ldx.hexacore.security.session.application.command.port.out.AuthenticationSessionRepository;
import com.ldx.hexacore.security.session.application.query.port.out.LoadFailedAttemptsQueryPort;
import com.ldx.hexacore.security.session.application.query.port.out.LoadSessionStatusQueryPort;
import com.ldx.hexacore.security.session.application.projection.FailedAttemptProjection;
import com.ldx.hexacore.security.session.application.projection.SessionStatusProjection;
import com.ldx.hexacore.security.session.domain.AuthenticationSession;
import com.ldx.hexacore.security.session.domain.vo.SessionId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 메모리 기반 세션 어댑터.
 * JPA가 없는 환경에서 기본 구현체로 사용됩니다.
 * 
 * @since 1.0.0
 */
class InMemorySessionAdapter implements AuthenticationSessionRepository, LoadSessionStatusQueryPort, LoadFailedAttemptsQueryPort {

    private static final Logger logger = LoggerFactory.getLogger(InMemorySessionAdapter.class);
    
    private final Map<UUID, AuthenticationSession> sessionStore = new ConcurrentHashMap<>();

    @Override
    public Optional<AuthenticationSession> findBySessionId(SessionId sessionId) {
        logger.debug("Loading session by ID: {} (using in-memory adapter)", sessionId.getValue());
        return Optional.ofNullable(sessionStore.get(sessionId.getValue()));
    }

    @Override
    public AuthenticationSession save(AuthenticationSession session) {
        logger.debug("Saving session: {} (using in-memory adapter)", session.getSessionId().getValue());
        sessionStore.put(session.getSessionId().getValue(), session);
        return session;
    }

    @Override
    public void delete(SessionId sessionId) {
        logger.debug("Deleting session: {} (using in-memory adapter)", sessionId.getValue());
        sessionStore.remove(sessionId.getValue());
    }

    @Override
    public Optional<SessionStatusProjection> loadSessionStatus(String sessionId) {
        logger.debug("Loading session status by sessionId: {} (using in-memory adapter)", sessionId);
        UUID sessionUuid = UUID.fromString(sessionId);
        return Optional.ofNullable(sessionStore.get(sessionUuid))
            .map(SessionStatusProjection::from);
    }

    @Override
    public Optional<SessionStatusProjection> loadSessionStatusByUser(String sessionId, String userId) {
        logger.debug("Loading session status by sessionId: {} and userId: {} (using in-memory adapter)", sessionId, userId);
        UUID sessionUuid = UUID.fromString(sessionId);
        return Optional.ofNullable(sessionStore.get(sessionUuid))
            .filter(session -> session.getUserId().equals(userId))
            .map(SessionStatusProjection::from);
    }

    @Override
    public List<FailedAttemptProjection> loadFailedAttempts(String sessionId, LocalDateTime from, LocalDateTime to, int limit) {
        logger.debug("Loading failed attempts for sessionId: {} (using in-memory adapter)", sessionId);
        // 간단한 구현: 빈 리스트 반환 (실제로는 attempts 정보를 매핑해야 함)
        return List.of();
    }

    @Override
    public List<FailedAttemptProjection> loadFailedAttemptsByUser(String sessionId, String userId, LocalDateTime from, LocalDateTime to, int limit) {
        logger.debug("Loading failed attempts for sessionId: {} and userId: {} (using in-memory adapter)", sessionId, userId);
        // 간단한 구현: 빈 리스트 반환
        return List.of();
    }

    @Override
    public int countFailedAttempts(String sessionId, LocalDateTime from, LocalDateTime to) {
        logger.debug("Counting failed attempts for sessionId: {} (using in-memory adapter)", sessionId);
        // 간단한 구현: 0 반환
        return 0;
    }

    @Override
    public int countFailedAttemptsByUser(String sessionId, String userId, LocalDateTime from, LocalDateTime to) {
        logger.debug("Counting failed attempts for sessionId: {} and userId: {} (using in-memory adapter)", sessionId, userId);
        // 간단한 구현: 0 반환
        return 0;
    }

    /**
     * 테스트나 개발용으로 세션을 추가합니다.
     */
    public void addSession(AuthenticationSession session) {
        sessionStore.put(session.getSessionId().getValue(), session);
    }
    
    /**
     * 저장된 데이터를 모두 삭제합니다.
     */
    public void clear() {
        sessionStore.clear();
    }
}