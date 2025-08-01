package com.dx.hexacore.security.auth.adapter.outbound.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.LocalDateTime;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenEntity {
    
    @Column(name = "access_token", length = 2000)
    private String accessToken;
    
    @Column(name = "refresh_token", length = 2000)
    private String refreshToken;
    
    @Column(name = "token_expires_in")
    private Long tokenExpiresIn;
    
    @Column(name = "token_issued_at")
    private LocalDateTime tokenIssuedAt;
    
    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;
}