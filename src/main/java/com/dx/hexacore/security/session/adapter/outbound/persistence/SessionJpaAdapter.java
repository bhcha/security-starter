package com.dx.hexacore.security.session.adapter.outbound.persistence;

import com.dx.hexacore.security.session.application.query.port.out.LoadFailedAttemptsQueryPort;
import com.dx.hexacore.security.session.application.query.port.out.LoadSessionStatusQueryPort;
import com.dx.hexacore.security.session.application.command.port.out.AuthenticationSessionRepository;
import com.dx.hexacore.security.session.application.projection.FailedAttemptProjection;
import com.dx.hexacore.security.session.application.projection.SessionStatusProjection;
import com.dx.hexacore.security.session.domain.AuthenticationSession;
import com.dx.hexacore.security.session.domain.vo.SessionId;
import com.dx.hexacore.security.session.adapter.outbound.persistence.entity.SessionJpaEntity;
import com.dx.hexacore.security.session.adapter.outbound.persistence.repository.SessionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
class SessionJpaAdapter implements AuthenticationSessionRepository, LoadSessionStatusQueryPort, LoadFailedAttemptsQueryPort {
    
    private final SessionJpaRepository repository;
    private final SessionMapper mapper;
    
    @Override
    public Optional<AuthenticationSession> findBySessionId(SessionId sessionId) {
        return repository.findById(sessionId.toString())
            .map(mapper::toDomain);
    }
    
    @Override
    @Transactional
    public AuthenticationSession save(AuthenticationSession session) {
        SessionJpaEntity entity = repository.findById(session.getSessionId().toString())
            .orElseGet(() -> mapper.toEntity(session));
        
        // Update entity with domain state
        mapper.updateEntity(entity, session);
        
        SessionJpaEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    @Transactional
    public void delete(SessionId sessionId) {
        repository.deleteById(sessionId.toString());
    }
    
    @Override
    public Optional<SessionStatusProjection> loadSessionStatus(String sessionId) {
        return repository.findById(sessionId)
            .map(mapper::toStatusProjection);
    }
    
    @Override
    public List<FailedAttemptProjection> loadFailedAttempts(String sessionId, LocalDateTime from, LocalDateTime to, int limit) {
        return repository.findFailedAttemptsBetween(sessionId, from, to, PageRequest.of(0, limit)).stream()
            .map(mapper::toFailedAttemptProjection)
            .toList();
    }
    
    @Override
    public List<FailedAttemptProjection> loadFailedAttemptsByUser(String sessionId, String userId, LocalDateTime from, LocalDateTime to, int limit) {
        // TODO: Implement user-specific filtering
        return loadFailedAttempts(sessionId, from, to, limit);
    }
    
    @Override
    public int countFailedAttempts(String sessionId, LocalDateTime from, LocalDateTime to) {
        return repository.countFailedAttemptsBetween(sessionId, from, to);
    }
    
    @Override
    public int countFailedAttemptsByUser(String sessionId, String userId, LocalDateTime from, LocalDateTime to) {
        // TODO: Implement user-specific counting
        return countFailedAttempts(sessionId, from, to);
    }
    
    @Override
    public Optional<SessionStatusProjection> loadSessionStatusByUser(String sessionId, String userId) {
        // TODO: Implement user-specific filtering
        return loadSessionStatus(sessionId);
    }
}