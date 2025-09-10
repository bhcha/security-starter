package com.ldx.hexacore.security.auth.domain.event;

import com.ldx.hexacore.security.util.ValidationMessages;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class AuthenticationAttempted extends DomainEvent {
    private final UUID authenticationId;
    private final String username;
    private final LocalDateTime attemptTime;

    private AuthenticationAttempted(UUID authenticationId, String username, LocalDateTime attemptTime) {
        super();
        this.authenticationId = authenticationId;
        this.username = username;
        this.attemptTime = attemptTime;
    }

    public static AuthenticationAttempted of(UUID authenticationId, String username, LocalDateTime attemptTime) {
        if (authenticationId == null) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNull("Authentication ID"));
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNullOrEmpty("Username"));
        }
        if (attemptTime == null) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNull("Attempt time"));
        }

        return new AuthenticationAttempted(authenticationId, username, attemptTime);
    }

    public UUID getAuthenticationId() {
        return authenticationId;
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getAttemptTime() {
        return attemptTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AuthenticationAttempted that = (AuthenticationAttempted) o;
        return Objects.equals(authenticationId, that.authenticationId) &&
               Objects.equals(username, that.username) &&
               Objects.equals(attemptTime, that.attemptTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), authenticationId, username, attemptTime);
    }

    @Override
    public String toString() {
        return "AuthenticationAttempted{" +
               "authenticationId=" + authenticationId +
               ", username='" + username + '\'' +
               ", attemptTime=" + attemptTime +
               ", eventId=" + getEventId() +
               ", occurredOn=" + getOccurredOn() +
               '}';
    }
}