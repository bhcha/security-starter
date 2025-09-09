package com.dx.hexacore.security.auth.domain.event;

import com.dx.hexacore.security.auth.domain.vo.Token;
import com.dx.hexacore.security.util.ValidationMessages;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class TokenExpired extends DomainEvent {
    private final UUID authenticationId;
    private final Token expiredToken;
    private final LocalDateTime expiredTime;

    private TokenExpired(UUID authenticationId, Token expiredToken, LocalDateTime expiredTime) {
        super();
        this.authenticationId = authenticationId;
        this.expiredToken = expiredToken;
        this.expiredTime = expiredTime;
    }

    public static TokenExpired of(UUID authenticationId, Token expiredToken, LocalDateTime expiredTime) {
        if (authenticationId == null) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNull("Authentication ID"));
        }
        if (expiredToken == null) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNull("Expired token"));
        }
        if (expiredTime == null) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNull("Expired time"));
        }

        return new TokenExpired(authenticationId, expiredToken, expiredTime);
    }

    public UUID getAuthenticationId() {
        return authenticationId;
    }

    public Token getExpiredToken() {
        return expiredToken;
    }

    public LocalDateTime getExpiredTime() {
        return expiredTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TokenExpired that = (TokenExpired) o;
        return Objects.equals(authenticationId, that.authenticationId) &&
               Objects.equals(expiredToken, that.expiredToken) &&
               Objects.equals(expiredTime, that.expiredTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), authenticationId, expiredToken, expiredTime);
    }

    @Override
    public String toString() {
        return "TokenExpired{" +
               "authenticationId=" + authenticationId +
               ", expiredToken=" + expiredToken +
               ", expiredTime=" + expiredTime +
               ", eventId=" + getEventId() +
               ", occurredOn=" + getOccurredOn() +
               '}';
    }
}