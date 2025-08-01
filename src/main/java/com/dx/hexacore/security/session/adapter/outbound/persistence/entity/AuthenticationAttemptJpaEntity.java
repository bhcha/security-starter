package com.dx.hexacore.security.session.adapter.outbound.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "authentication_attempts", indexes = {
    @Index(name = "idx_session_attempted_at", columnList = "session_id, attempted_at DESC"),
    @Index(name = "idx_session_successful", columnList = "session_id, successful")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AuthenticationAttemptJpaEntity {
    
    @Id
    @Column(name = "attempt_id", length = 36)
    private String attemptId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    @Setter
    private SessionJpaEntity session;
    
    @Column(name = "attempted_at", nullable = false)
    private LocalDateTime attemptedAt;
    
    @Column(name = "successful", nullable = false)
    private boolean successful;
    
    @Column(name = "reason", length = 255)
    private String reason;
    
    @Column(name = "client_ip", nullable = false, length = 45)
    private String clientIp;
    
    @Column(name = "risk_level", nullable = false)
    private int riskLevel;
    
    @PrePersist
    protected void onCreate() {
        if (attemptId == null) {
            attemptId = java.util.UUID.randomUUID().toString();
        }
    }
}