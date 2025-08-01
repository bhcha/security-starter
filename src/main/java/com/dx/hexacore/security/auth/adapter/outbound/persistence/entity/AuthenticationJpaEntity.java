package com.dx.hexacore.security.auth.adapter.outbound.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "authentication")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticationJpaEntity {
    
    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;
    
    @Column(name = "username", nullable = false, length = 100)
    private String username;
    
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AuthenticationStatusEntity status;
    
    @Column(name = "attempt_time", nullable = false)
    private LocalDateTime attemptTime;
    
    @Column(name = "success_time")
    private LocalDateTime successTime;
    
    @Column(name = "failure_time")
    private LocalDateTime failureTime;
    
    @Column(name = "failure_reason", length = 500)
    private String failureReason;
    
    @Embedded
    private TokenEntity token;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum AuthenticationStatusEntity {
        PENDING,
        SUCCESS,
        FAILED
    }
}