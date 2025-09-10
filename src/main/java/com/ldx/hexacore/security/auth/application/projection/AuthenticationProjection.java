package com.ldx.hexacore.security.auth.application.projection;

import java.time.LocalDateTime;

/**
 * 인증 정보 조회용 Projection.
 * 
 * @since 1.0.0
 */
public class AuthenticationProjection {

    private final String id;
    private final String username;
    private final String status;
    private final LocalDateTime attemptTime;
    private final LocalDateTime successTime;
    private final LocalDateTime failureTime;
    private final String failureReason;
    private final String accessToken;
    private final String refreshToken;
    private final Long tokenExpiresIn;
    private final LocalDateTime tokenExpiredTime;

    private AuthenticationProjection(Builder builder) {
        this.id = builder.id;
        this.username = builder.username;
        this.status = builder.status;
        this.attemptTime = builder.attemptTime;
        this.successTime = builder.successTime;
        this.failureTime = builder.failureTime;
        this.failureReason = builder.failureReason;
        this.accessToken = builder.accessToken;
        this.refreshToken = builder.refreshToken;
        this.tokenExpiresIn = builder.tokenExpiresIn;
        this.tokenExpiredTime = builder.tokenExpiredTime;
    }

    /**
     * Builder 패턴을 위한 Builder 객체를 생성합니다.
     * 
     * @return Builder 객체
     */
    public static Builder builder() {
        return new Builder();
    }

    // Getter 메서드들
    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getStatus() {
        return status;
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

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Long getTokenExpiresIn() {
        return tokenExpiresIn;
    }

    public LocalDateTime getTokenExpiredTime() {
        return tokenExpiredTime;
    }

    /**
     * Builder 클래스.
     */
    public static class Builder {
        private String id;
        private String username;
        private String status;
        private LocalDateTime attemptTime;
        private LocalDateTime successTime;
        private LocalDateTime failureTime;
        private String failureReason;
        private String accessToken;
        private String refreshToken;
        private Long tokenExpiresIn;
        private LocalDateTime tokenExpiredTime;

        private Builder() {}

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder attemptTime(LocalDateTime attemptTime) {
            this.attemptTime = attemptTime;
            return this;
        }

        public Builder successTime(LocalDateTime successTime) {
            this.successTime = successTime;
            return this;
        }

        public Builder failureTime(LocalDateTime failureTime) {
            this.failureTime = failureTime;
            return this;
        }

        public Builder failureReason(String failureReason) {
            this.failureReason = failureReason;
            return this;
        }

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public Builder tokenExpiresIn(Long tokenExpiresIn) {
            this.tokenExpiresIn = tokenExpiresIn;
            return this;
        }

        public Builder tokenExpiredTime(LocalDateTime tokenExpiredTime) {
            this.tokenExpiredTime = tokenExpiredTime;
            return this;
        }

        public AuthenticationProjection build() {
            return new AuthenticationProjection(this);
        }
    }
}