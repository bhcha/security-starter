package com.ldx.hexacore.security.session.domain;

import com.ldx.hexacore.security.session.domain.vo.ClientIp;
import com.ldx.hexacore.security.session.domain.vo.RiskLevel;
import com.ldx.hexacore.security.util.ValidationMessages;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 인증 시도를 나타내는 Entity
 */
public class AuthenticationAttempt {
    
    private Long attemptId;
    private final String userId;
    private final LocalDateTime attemptedAt;
    private final boolean isSuccessful;
    private final ClientIp clientIp;
    private final RiskLevel riskLevel;

    private AuthenticationAttempt(String userId, LocalDateTime attemptedAt, boolean isSuccessful, 
                                 ClientIp clientIp, RiskLevel riskLevel) {
        this.userId = userId;
        this.attemptedAt = attemptedAt;
        this.isSuccessful = isSuccessful;
        this.clientIp = clientIp;
        this.riskLevel = riskLevel;
    }

    /**
     * AuthenticationAttempt 생성 팩토리 메서드
     */
    public static AuthenticationAttempt create(String userId, LocalDateTime attemptedAt, boolean isSuccessful,
                                             ClientIp clientIp, RiskLevel riskLevel) {
        validateParameters(userId, attemptedAt, clientIp, riskLevel);
        return new AuthenticationAttempt(userId, attemptedAt, isSuccessful, clientIp, riskLevel);
    }

    private static void validateParameters(String userId, LocalDateTime attemptedAt, ClientIp clientIp, RiskLevel riskLevel) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNullOrEmpty("UserId"));
        }
        if (attemptedAt == null) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNull("Attempted time"));
        }
        if (clientIp == null) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNull("ClientIp"));
        }
        if (riskLevel == null) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNull("Risk level"));
        }
    }

    /**
     * 지정된 시간 윈도우 내의 시도인지 확인
     */
    public boolean isWithinTimeWindow(LocalDateTime windowStart) {
        if (windowStart == null) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNull("Window start time"));
        }
        return attemptedAt.isAfter(windowStart) || attemptedAt.isEqual(windowStart);
    }

    /**
     * 동일한 소스에서의 시도인지 확인
     */
    public boolean isFromSameSource(ClientIp otherIp) {
        if (otherIp == null) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNull("Other IP"));
        }
        return clientIp.equals(otherIp);
    }

    /**
     * 시도의 위험 점수를 계산
     */
    public int calculateRiskScore() {
        int baseScore = riskLevel.getScore();
        
        // 실패한 시도에 대해 페널티 점수 추가
        if (!isSuccessful) {
            baseScore += 30;
        }
        
        return Math.min(baseScore, 100); // 최대 100점
    }

    // Getters
    public Long getAttemptId() {
        return attemptId;
    }

    public String getUserId() {
        return userId;
    }

    public LocalDateTime getAttemptedAt() {
        return attemptedAt;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public ClientIp getClientIp() {
        return clientIp;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        AuthenticationAttempt that = (AuthenticationAttempt) o;
        
        // ID가 있으면 ID로 비교, 없으면 비즈니스 키로 비교
        if (attemptId != null && that.attemptId != null) {
            return Objects.equals(attemptId, that.attemptId);
        }
        
        return Objects.equals(userId, that.userId) &&
               Objects.equals(attemptedAt, that.attemptedAt) &&
               Objects.equals(clientIp, that.clientIp);
    }

    @Override
    public int hashCode() {
        if (attemptId != null) {
            return Objects.hash(attemptId);
        }
        return Objects.hash(userId, attemptedAt, clientIp);
    }

    @Override
    public String toString() {
        return "AuthenticationAttempt{" +
               "attemptId=" + attemptId +
               ", userId='" + userId + '\'' +
               ", attemptedAt=" + attemptedAt +
               ", isSuccessful=" + isSuccessful +
               ", clientIp=" + clientIp +
               ", riskLevel=" + riskLevel +
               '}';
    }
}