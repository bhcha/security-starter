package com.dx.hexacore.security.auth.domain.event;

import com.dx.hexacore.security.auth.domain.vo.Token;
import com.dx.hexacore.security.util.ValidationMessages;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class AuthenticationSucceeded extends DomainEvent {
    private final UUID authenticationId;
    private final Token token;
    private final LocalDateTime successTime;

    private AuthenticationSucceeded(UUID authenticationId, Token token, LocalDateTime successTime) {
        super();
        this.authenticationId = authenticationId;
        this.token = token;
        this.successTime = successTime;
    }

    public static AuthenticationSucceeded of(UUID authenticationId, Token token, LocalDateTime successTime) {
        if (authenticationId == null) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNull("Authentication ID"));
        }
        if (token == null) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNull("Token"));
        }
        if (successTime == null) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNull("Success time"));
        }

        return new AuthenticationSucceeded(authenticationId, token, successTime);
    }

    public UUID getAuthenticationId() {
        return authenticationId;
    }

    public Token getToken() {
        return token;
    }

    public LocalDateTime getSuccessTime() {
        return successTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AuthenticationSucceeded that = (AuthenticationSucceeded) o;
        return Objects.equals(authenticationId, that.authenticationId) &&
               Objects.equals(token, that.token) &&
               Objects.equals(successTime, that.successTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), authenticationId, token, successTime);
    }

    @Override
    public String toString() {
        return "AuthenticationSucceeded{" +
               "authenticationId=" + authenticationId +
               ", token=" + token +
               ", successTime=" + successTime +
               ", eventId=" + getEventId() +
               ", occurredOn=" + getOccurredOn() +
               '}';
    }
}