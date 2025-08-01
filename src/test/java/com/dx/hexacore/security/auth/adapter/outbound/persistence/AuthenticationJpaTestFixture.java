package com.dx.hexacore.security.auth.adapter.outbound.persistence;

import com.dx.hexacore.security.auth.adapter.outbound.persistence.entity.AuthenticationJpaEntity;
import com.dx.hexacore.security.auth.adapter.outbound.persistence.entity.TokenEntity;
import com.dx.hexacore.security.auth.domain.Authentication;
import com.dx.hexacore.security.auth.domain.vo.Credentials;
import com.dx.hexacore.security.auth.domain.vo.Token;

import java.time.LocalDateTime;
import java.util.UUID;

public class AuthenticationJpaTestFixture {
    
    public static AuthenticationJpaEntity createPendingEntity() {
        return AuthenticationJpaEntity.builder()
                .id(UUID.randomUUID().toString())
                .username("testuser")
                .status(AuthenticationJpaEntity.AuthenticationStatusEntity.PENDING)
                .attemptTime(LocalDateTime.now())
                .version(0L)
                .build();
    }
    
    public static AuthenticationJpaEntity createSuccessEntity() {
        LocalDateTime now = LocalDateTime.now();
        return AuthenticationJpaEntity.builder()
                .id(UUID.randomUUID().toString())
                .username("testuser")
                .status(AuthenticationJpaEntity.AuthenticationStatusEntity.SUCCESS)
                .attemptTime(now.minusMinutes(1))
                .successTime(now)
                .token(TokenEntity.builder()
                        .accessToken("test-access-token")
                        .refreshToken("test-refresh-token")
                        .tokenExpiresIn(3600L)
                        .tokenIssuedAt(now)
                        .tokenExpiresAt(now.plusSeconds(3600))
                        .build())
                .version(0L)
                .build();
    }
    
    public static AuthenticationJpaEntity createFailedEntity() {
        LocalDateTime now = LocalDateTime.now();
        return AuthenticationJpaEntity.builder()
                .id(UUID.randomUUID().toString())
                .username("testuser")
                .status(AuthenticationJpaEntity.AuthenticationStatusEntity.FAILED)
                .attemptTime(now.minusMinutes(1))
                .failureTime(now)
                .failureReason("Invalid credentials")
                .version(0L)
                .build();
    }
    
    public static Authentication createPendingAuthentication() {
        return Authentication.attemptAuthentication(
                Credentials.of("testuser", "testpass")
        );
    }
    
    public static Authentication createSuccessAuthentication() {
        Authentication auth = createPendingAuthentication();
        Token token = Token.of("test-access-token", "test-refresh-token", 3600L);
        auth.markAsSuccessful(token);
        return auth;
    }
    
    public static Authentication createFailedAuthentication() {
        Authentication auth = createPendingAuthentication();
        auth.markAsFailed("Invalid credentials");
        return auth;
    }
}