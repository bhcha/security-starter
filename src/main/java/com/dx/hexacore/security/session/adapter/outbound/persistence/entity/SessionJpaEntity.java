package com.dx.hexacore.security.session.adapter.outbound.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "authentication_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SessionJpaEntity {
    
    @Id
    @Column(name = "session_id", length = 36)
    private String sessionId;
    
    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;
    
    @Column(name = "lockout_until")
    private LocalDateTime lockoutUntil;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("attemptedAt DESC")
    @Builder.Default
    private List<AuthenticationAttemptJpaEntity> attempts = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public void addAttempt(AuthenticationAttemptJpaEntity attempt) {
        attempts.add(attempt);
        attempt.setSession(this);
    }
    
    public void setLockoutUntil(LocalDateTime lockoutUntil) {
        this.lockoutUntil = lockoutUntil;
    }
    
    public AuthenticationAttemptJpaEntity getLastAttempt() {
        return attempts.isEmpty() ? null : attempts.get(0);
    }
    
    public int getFailedAttemptCount() {
        return (int) attempts.stream()
            .filter(attempt -> !attempt.isSuccessful())
            .count();
    }
}