package com.ldx.hexacore.security.auth.adapter.outbound.persistence;

import com.ldx.hexacore.security.auth.adapter.outbound.persistence.entity.AuthenticationJpaEntity;
import com.ldx.hexacore.security.util.ValidationMessages;
import com.ldx.hexacore.security.auth.adapter.outbound.persistence.entity.TokenEntity;
import com.ldx.hexacore.security.auth.application.projection.AuthenticationProjection;
import com.ldx.hexacore.security.auth.application.projection.TokenInfoProjection;
import com.ldx.hexacore.security.auth.domain.Authentication;
import com.ldx.hexacore.security.auth.domain.vo.AuthenticationStatus;
import com.ldx.hexacore.security.auth.domain.vo.Credentials;
import com.ldx.hexacore.security.auth.domain.vo.Token;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class AuthenticationJpaMapper {
    
    public AuthenticationJpaEntity toEntity(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNull("Authentication"));
        }
        
        AuthenticationJpaEntity.AuthenticationJpaEntityBuilder builder = AuthenticationJpaEntity.builder()
                .id(authentication.getId().toString())
                .username(authentication.getCredentials().getUsername())
                .status(mapStatus(authentication.getStatus()))
                .attemptTime(authentication.getAttemptTime())
                .successTime(authentication.getSuccessTime())
                .failureTime(authentication.getFailureTime())
                .failureReason(authentication.getFailureReason());
        
        if (authentication.getToken() != null) {
            Token token = authentication.getToken();
            LocalDateTime issuedAt = LocalDateTime.now();
            LocalDateTime expiresAt = issuedAt.plusSeconds(token.getExpiresIn());
            
            builder.token(TokenEntity.builder()
                    .accessToken(token.getAccessToken())
                    .refreshToken(token.getRefreshToken())
                    .tokenExpiresIn(token.getExpiresIn())
                    .tokenIssuedAt(issuedAt)
                    .tokenExpiresAt(expiresAt)
                    .build());
        }
        
        return builder.build();
    }
    
    public Authentication toDomain(AuthenticationJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        
        // JPA 엔티티에는 비밀번호가 저장되지 않으므로 더미 값 사용
        Credentials credentials = Credentials.of(entity.getUsername(), "dummy-password");
        
        UUID id = UUID.fromString(entity.getId());
        
        Authentication authentication = switch (entity.getStatus()) {
            case PENDING -> Authentication.of(id, credentials, AuthenticationStatus.pending(), entity.getAttemptTime());
            case SUCCESS -> {
                Authentication auth = Authentication.of(id, credentials, AuthenticationStatus.pending(), entity.getAttemptTime());
                if (entity.getToken() != null) {
                    Token token = Token.of(
                            entity.getToken().getAccessToken(),
                            entity.getToken().getRefreshToken(),
                            entity.getToken().getTokenExpiresIn()
                    );
                    auth.markAsSuccessful(token);
                }
                yield auth;
            }
            case FAILED -> {
                Authentication auth = Authentication.of(id, credentials, AuthenticationStatus.pending(), entity.getAttemptTime());
                auth.markAsFailed(entity.getFailureReason());
                yield auth;
            }
        };
        
        return authentication;
    }
    
    public AuthenticationProjection toProjection(AuthenticationJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        
        AuthenticationProjection.Builder builder = AuthenticationProjection.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .status(entity.getStatus().name())
                .attemptTime(entity.getAttemptTime())
                .successTime(entity.getSuccessTime())
                .failureTime(entity.getFailureTime())
                .failureReason(entity.getFailureReason());
        
        if (entity.getToken() != null) {
            builder.accessToken(entity.getToken().getAccessToken())
                    .refreshToken(entity.getToken().getRefreshToken())
                    .tokenExpiresIn(entity.getToken().getTokenExpiresIn())
                    .tokenExpiredTime(entity.getToken().getTokenExpiresAt());
        }
        
        return builder.build();
    }
    
    public TokenInfoProjection toTokenProjection(AuthenticationJpaEntity entity) {
        if (entity == null || entity.getToken() == null) {
            return null;
        }
        
        TokenEntity token = entity.getToken();
        boolean isValid = entity.getStatus() == AuthenticationJpaEntity.AuthenticationStatusEntity.SUCCESS &&
                         token.getTokenExpiresAt() != null &&
                         token.getTokenExpiresAt().isAfter(LocalDateTime.now());
        
        return TokenInfoProjection.builder()
                .token(token.getAccessToken())
                .isValid(isValid)
                .issuedAt(token.getTokenIssuedAt())
                .expiresAt(token.getTokenExpiresAt())
                .canRefresh(token.getRefreshToken() != null)
                .tokenType("Bearer")
                .scope("api")
                .authenticationId(entity.getId())
                .build();
    }
    
    private AuthenticationJpaEntity.AuthenticationStatusEntity mapStatus(AuthenticationStatus status) {
        if (status.isPending()) {
            return AuthenticationJpaEntity.AuthenticationStatusEntity.PENDING;
        } else if (status.isSuccess()) {
            return AuthenticationJpaEntity.AuthenticationStatusEntity.SUCCESS;
        } else {
            return AuthenticationJpaEntity.AuthenticationStatusEntity.FAILED;
        }
    }
}