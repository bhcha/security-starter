package com.dx.hexacore.security.session.adapter.outbound.persistence;

import com.dx.hexacore.security.session.application.projection.FailedAttemptProjection;
import com.dx.hexacore.security.session.application.projection.SessionStatusProjection;
import com.dx.hexacore.security.session.domain.AuthenticationSession;
import com.dx.hexacore.security.session.domain.AuthenticationAttempt;
import com.dx.hexacore.security.session.domain.vo.SessionId;
import com.dx.hexacore.security.session.domain.vo.ClientIp;
import com.dx.hexacore.security.session.domain.vo.RiskLevel;
import com.dx.hexacore.security.session.adapter.outbound.persistence.entity.AuthenticationAttemptJpaEntity;
import com.dx.hexacore.security.session.adapter.outbound.persistence.entity.SessionJpaEntity;
import com.dx.hexacore.security.config.properties.HexacoreSecurityProperties;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SessionMapper {
    
    private final HexacoreSecurityProperties securityProperties;
    
    public SessionMapper(HexacoreSecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }
    
    public SessionJpaEntity toEntity(AuthenticationSession session) {
        SessionJpaEntity entity = SessionJpaEntity.builder()
            .sessionId(session.getSessionId().toString())
            .userId(session.getUserId())
            .lockoutUntil(session.getLockedUntil())
            .build();
            
        // Convert attempts
        session.getAttempts().forEach(attempt -> {
            AuthenticationAttemptJpaEntity attemptEntity = toAttemptEntity(attempt, session.getSessionId().toString());
            entity.addAttempt(attemptEntity);
        });
        
        return entity;
    }
    
    public void updateEntity(SessionJpaEntity entity, AuthenticationSession session) {
        entity.setLockoutUntil(session.getLockedUntil());
        
        // Clear and re-add attempts to ensure sync
        entity.getAttempts().clear();
        session.getAttempts().forEach(attempt -> {
            AuthenticationAttemptJpaEntity attemptEntity = toAttemptEntity(attempt, session.getSessionId().toString());
            entity.addAttempt(attemptEntity);
        });
    }
    
    public AuthenticationAttemptJpaEntity toAttemptEntity(AuthenticationAttempt attempt, String sessionId) {
        return AuthenticationAttemptJpaEntity.builder()
            .attemptedAt(attempt.getAttemptedAt())
            .successful(attempt.isSuccessful())
            .reason(attempt.isSuccessful() ? null : attempt.getRiskLevel().getReason())
            .clientIp(attempt.getClientIp().getIpAddress())
            .riskLevel(attempt.getRiskLevel().getScore())
            .build();
    }
    
    public SessionStatusProjection toStatusProjection(SessionJpaEntity entity) {
        AuthenticationAttemptJpaEntity lastAttempt = entity.getLastAttempt();
        
        // Get primary client IP from first attempt or use a default
        String primaryClientIp = entity.getAttempts().isEmpty() ? "unknown" : 
            entity.getAttempts().get(entity.getAttempts().size() - 1).getClientIp();
            
        return new SessionStatusProjection(
            entity.getSessionId(),
            entity.getUserId(),
            primaryClientIp,
            entity.getLockoutUntil() != null && entity.getLockoutUntil().isAfter(LocalDateTime.now()),
            entity.getLockoutUntil(),
            entity.getCreatedAt(),
            lastAttempt != null ? lastAttempt.getAttemptedAt() : entity.getCreatedAt(),
            entity.getAttempts().size(),
            entity.getFailedAttemptCount()
        );
    }
    
    public FailedAttemptProjection toFailedAttemptProjection(AuthenticationAttemptJpaEntity entity) {
        // Need to get userId from session entity
        String userId = entity.getSession() != null ? entity.getSession().getUserId() : "unknown";
        
        return new FailedAttemptProjection(
            userId,
            entity.getClientIp(),
            entity.getRiskLevel(),
            entity.getReason(),
            entity.getAttemptedAt()
        );
    }
    
    public AuthenticationSession toDomain(SessionJpaEntity entity) {
        SessionId sessionId = SessionId.of(entity.getSessionId());
        // Get first attempt's clientIp or create default
        ClientIp clientIp = entity.getAttempts().isEmpty() ? 
            ClientIp.of("127.0.0.1") : 
            ClientIp.of(entity.getAttempts().get(0).getClientIp());
            
        // 설정값으로 세션 생성
        AuthenticationSession session = AuthenticationSession.create(
            sessionId, 
            entity.getUserId(), 
            clientIp,
            securityProperties.getSession().getLockout().getMaxAttempts(),
            securityProperties.getSession().getLockout().getLockoutDurationMinutes()
        );
        
        // Restore lockout state
        if (entity.getLockoutUntil() != null && entity.getLockoutUntil().isAfter(LocalDateTime.now())) {
            // Set internal state - this would need reflection or a package-private method
            // For now, we'll rely on the attempts to trigger lockout
        }
        
        // Convert attempts
        entity.getAttempts().forEach(attemptEntity -> {
            ClientIp attemptClientIp = ClientIp.of(attemptEntity.getClientIp());
            RiskLevel riskLevel = RiskLevel.of(attemptEntity.getRiskLevel(), attemptEntity.getReason() != null ? attemptEntity.getReason() : "No reason");
            
            session.recordAttemptAtSpecificTime(
                entity.getUserId(),
                attemptClientIp,
                attemptEntity.isSuccessful(),
                riskLevel,
                attemptEntity.getAttemptedAt()
            );
        });
        
        return session;
    }
}