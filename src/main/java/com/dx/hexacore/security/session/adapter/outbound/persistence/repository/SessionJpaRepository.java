package com.dx.hexacore.security.session.adapter.outbound.persistence.repository;

import com.dx.hexacore.security.session.adapter.outbound.persistence.entity.AuthenticationAttemptJpaEntity;
import com.dx.hexacore.security.session.adapter.outbound.persistence.entity.SessionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SessionJpaRepository extends JpaRepository<SessionJpaEntity, String> {
    
    @Query("""
        SELECT a 
        FROM AuthenticationAttemptJpaEntity a 
        WHERE a.session.sessionId = :sessionId 
        AND a.successful = false 
        AND a.attemptedAt >= :from 
        AND a.attemptedAt <= :to 
        ORDER BY a.attemptedAt DESC
        """)
    List<AuthenticationAttemptJpaEntity> findFailedAttemptsBetween(
        @Param("sessionId") String sessionId,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to,
        org.springframework.data.domain.Pageable pageable
    );
    
    @Query("""
        SELECT s 
        FROM SessionJpaEntity s 
        LEFT JOIN FETCH s.attempts 
        WHERE s.sessionId = :sessionId
        """)
    SessionJpaEntity findByIdWithAttempts(@Param("sessionId") String sessionId);
    
    @Query("""
        SELECT s 
        FROM SessionJpaEntity s 
        WHERE s.userId = :userId 
        AND s.lockoutUntil > :currentTime
        """)
    List<SessionJpaEntity> findActiveLockedSessionsByUserId(
        @Param("userId") String userId,
        @Param("currentTime") LocalDateTime currentTime
    );
    
    @Query("""
        SELECT COUNT(a) 
        FROM AuthenticationAttemptJpaEntity a 
        WHERE a.session.sessionId = :sessionId 
        AND a.successful = false 
        AND a.attemptedAt >= :from 
        AND a.attemptedAt <= :to
        """)
    int countFailedAttemptsBetween(
        @Param("sessionId") String sessionId,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );
    
    void deleteByCreatedAtBefore(LocalDateTime cutoffTime);
}