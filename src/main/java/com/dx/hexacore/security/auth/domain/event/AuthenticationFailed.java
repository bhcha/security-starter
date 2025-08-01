package com.dx.hexacore.security.auth.domain.event;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class AuthenticationFailed extends DomainEvent {
    private final UUID authenticationId;
    private final String reason;
    private final LocalDateTime failureTime;

    private AuthenticationFailed(UUID authenticationId, String reason, LocalDateTime failureTime) {
        super();
        this.authenticationId = authenticationId;
        this.reason = reason;
        this.failureTime = failureTime;
    }

    public static AuthenticationFailed of(UUID authenticationId, String reason, LocalDateTime failureTime) {
        if (authenticationId == null) {
            throw new IllegalArgumentException("Authentication ID cannot be null");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Reason cannot be null or empty");
        }
        if (failureTime == null) {
            throw new IllegalArgumentException("Failure time cannot be null");
        }

        return new AuthenticationFailed(authenticationId, reason, failureTime);
    }

    public UUID getAuthenticationId() {
        return authenticationId;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getFailureTime() {
        return failureTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AuthenticationFailed that = (AuthenticationFailed) o;
        return Objects.equals(authenticationId, that.authenticationId) &&
               Objects.equals(reason, that.reason) &&
               Objects.equals(failureTime, that.failureTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), authenticationId, reason, failureTime);
    }

    @Override
    public String toString() {
        return "AuthenticationFailed{" +
               "authenticationId=" + authenticationId +
               ", reason='" + reason + '\'' +
               ", failureTime=" + failureTime +
               ", eventId=" + getEventId() +
               ", occurredOn=" + getOccurredOn() +
               '}';
    }
}