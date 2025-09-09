package com.dx.hexacore.security.session.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.dx.hexacore.security.auth.domain.event.DomainEvent;
import com.dx.hexacore.security.util.ValidationMessages;

import java.time.LocalDateTime;

/**
 * 계정 잠금 도메인 이벤트
 */
public class AccountLocked extends DomainEvent {
    
    private final String sessionId;
    private final String userId;
    private final String clientIp;
    private final LocalDateTime lockedUntil;
    private final int failedAttemptCount;

    @JsonCreator
    private AccountLocked(
        @JsonProperty("sessionId") String sessionId,
        @JsonProperty("userId") String userId,
        @JsonProperty("clientIp") String clientIp,
        @JsonProperty("lockedUntil") LocalDateTime lockedUntil,
        @JsonProperty("failedAttemptCount") int failedAttemptCount,
        @JsonProperty("occurredAt") LocalDateTime occurredAt
    ) {
        super();
        validateParameters(sessionId, userId, clientIp, lockedUntil, failedAttemptCount, occurredAt);
        
        this.sessionId = sessionId;
        this.userId = userId;
        this.clientIp = clientIp;
        this.lockedUntil = lockedUntil;
        this.failedAttemptCount = failedAttemptCount;
    }

    /**
     * AccountLocked 이벤트 생성 팩토리 메서드
     */
    public static AccountLocked of(String sessionId, String userId, String clientIp,
                                  LocalDateTime lockedUntil, int failedAttemptCount, LocalDateTime occurredAt) {
        return new AccountLocked(sessionId, userId, clientIp, lockedUntil, failedAttemptCount, occurredAt);
    }

    private static void validateParameters(String sessionId, String userId, String clientIp,
                                         LocalDateTime lockedUntil, int failedAttemptCount, LocalDateTime occurredAt) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNullOrEmpty("SessionId"));
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNullOrEmpty("UserId"));
        }
        if (clientIp == null || clientIp.trim().isEmpty()) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNullOrEmpty("ClientIp"));
        }
        if (lockedUntil == null) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNull("Locked until time"));
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNull("Occurred time"));
        }
        if (failedAttemptCount <= 0) {
            throw new IllegalArgumentException("Failed attempt count must be positive");
        }
        if (lockedUntil.isBefore(occurredAt)) {
            throw new IllegalArgumentException("Locked until time must be in the future");
        }
    }

    public String eventType() {
        return "AccountLocked";
    }

    public String aggregateId() {
        return sessionId;
    }

    // Getters
    public String sessionId() {
        return sessionId;
    }

    public String userId() {
        return userId;
    }

    public String clientIp() {
        return clientIp;
    }

    public LocalDateTime lockedUntil() {
        return lockedUntil;
    }

    public int failedAttemptCount() {
        return failedAttemptCount;
    }

    public LocalDateTime occurredAt() {
        return getOccurredOn();
    }
}