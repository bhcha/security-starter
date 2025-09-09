package com.dx.hexacore.security.auth.domain;

import com.dx.hexacore.security.auth.domain.event.AuthenticationAttempted;
import com.dx.hexacore.security.auth.domain.event.AuthenticationSucceeded;
import com.dx.hexacore.security.auth.domain.event.AuthenticationFailed;
import com.dx.hexacore.security.auth.domain.event.TokenExpired;
import com.dx.hexacore.security.auth.domain.vo.AuthenticationStatus;
import com.dx.hexacore.security.auth.domain.vo.Credentials;
import com.dx.hexacore.security.auth.domain.vo.Token;
import com.dx.hexacore.security.util.ValidationMessages;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Authentication extends AggregateRoot {
    private final UUID id;
    private final Credentials credentials;
    private AuthenticationStatus status;
    private Token token;
    private final LocalDateTime attemptTime;
    private LocalDateTime successTime;
    private LocalDateTime failureTime;
    private String failureReason;
    private LocalDateTime tokenExpiredTime;

    private Authentication(UUID id, Credentials credentials, AuthenticationStatus status, LocalDateTime attemptTime) {
        this.id = id;
        this.credentials = credentials;
        this.status = status;
        this.attemptTime = attemptTime;
    }

    public static Authentication attemptAuthentication(Credentials credentials) {
        if (credentials == null) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNull("Credentials"));
        }

        UUID id = UUID.randomUUID();
        LocalDateTime attemptTime = LocalDateTime.now();
        
        Authentication authentication = new Authentication(id, credentials, AuthenticationStatus.pending(), attemptTime);
        authentication.addDomainEvent(AuthenticationAttempted.of(id, credentials.getUsername(), attemptTime));
        
        return authentication;
    }

    public static Authentication of(UUID id, Credentials credentials, AuthenticationStatus status, LocalDateTime attemptTime) {
        return new Authentication(id, credentials, status, attemptTime);
    }

    public void markAsSuccessful(Token token) {
        if (token == null) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNull("Token"));
        }
        if (!status.isPending()) {
            throw new IllegalStateException("Cannot mark as successful: authentication is not in PENDING state");
        }

        this.status = AuthenticationStatus.success();
        this.token = token;
        this.successTime = LocalDateTime.now();
        
        addDomainEvent(AuthenticationSucceeded.of(id, token, successTime));
    }

    public void markAsFailed(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNullOrEmpty("Failure reason"));
        }
        if (!status.isPending()) {
            throw new IllegalStateException("Cannot mark as failed: authentication is not in PENDING state");
        }

        this.status = AuthenticationStatus.failed();
        this.failureReason = reason;
        this.failureTime = LocalDateTime.now();
        
        addDomainEvent(AuthenticationFailed.of(id, reason, failureTime));
    }
    
    public void updateToken(Token newToken) {
        if (newToken == null) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNull("Token"));
        }
        if (!status.isSuccess()) {
            throw new IllegalStateException("Cannot update token: authentication is not in SUCCESS state");
        }

        this.token = newToken;
        this.successTime = LocalDateTime.now(); // 토큰 갱신 시간 업데이트
        
        addDomainEvent(AuthenticationSucceeded.of(id, newToken, successTime));
    }

    public void expireToken() {
        if (token == null) {
            throw new IllegalStateException("Cannot expire token: no token present");
        }
        if (token.isExpired()) {
            throw new IllegalStateException("Cannot expire token: token is already expired");
        }

        this.token = token.expire();
        this.tokenExpiredTime = LocalDateTime.now();
        
        addDomainEvent(TokenExpired.of(id, this.token, tokenExpiredTime));
    }

    public boolean isTokenValid() {
        return token != null && !token.isExpired();
    }

    public UUID getId() {
        return id;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public AuthenticationStatus getStatus() {
        return status;
    }

    public Token getToken() {
        return token;
    }

    public LocalDateTime getAttemptTime() {
        return attemptTime;
    }

    public LocalDateTime getSuccessTime() {
        return successTime;
    }

    public LocalDateTime getFailureTime() {
        return failureTime;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public LocalDateTime getTokenExpiredTime() {
        return tokenExpiredTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Authentication that = (Authentication) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Authentication{" +
               "id=" + id +
               ", username='" + credentials.getUsername() + '\'' +
               ", status=" + status +
               ", attemptTime=" + attemptTime +
               '}';
    }
}