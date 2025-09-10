package com.ldx.hexacore.security.auth.application.query.port.in;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 인증 정보 조회 응답 DTO.
 * 
 * @since 1.0.0
 */
public class AuthenticationResponse {

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

    private AuthenticationResponse(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "ID cannot be null");
        this.username = Objects.requireNonNull(builder.username, "Username cannot be null");
        this.status = Objects.requireNonNull(builder.status, "Status cannot be null");
        this.attemptTime = Objects.requireNonNull(builder.attemptTime, "Attempt time cannot be null");
        this.successTime = builder.successTime;
        this.failureTime = builder.failureTime;
        this.failureReason = builder.failureReason;
        this.accessToken = builder.accessToken;
        this.refreshToken = builder.refreshToken;
        this.tokenExpiresIn = builder.tokenExpiresIn;
    }

    /**
     * 인증 성공 응답을 생성합니다.
     * 
     * @param id 인증 ID
     * @param username 사용자명
     * @param accessToken 액세스 토큰
     * @param refreshToken 리프레시 토큰
     * @param tokenExpiresIn 토큰 만료 시간(초)
     * @return 성공 응답
     */
    public static AuthenticationResponse success(String id, String username, 
                                               String accessToken, String refreshToken, Long tokenExpiresIn) {
        LocalDateTime now = LocalDateTime.now();
        return builder()
            .id(id)
            .username(username)
            .status("SUCCESS")
            .attemptTime(now.minusMinutes(5)) // 기본적으로 5분 전에 시도된 것으로 설정
            .successTime(now)
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenExpiresIn(tokenExpiresIn)
            .build();
    }

    /**
     * 인증 실패 응답을 생성합니다.
     * 
     * @param id 인증 ID
     * @param username 사용자명
     * @param failureReason 실패 이유
     * @return 실패 응답
     */
    public static AuthenticationResponse failed(String id, String username, String failureReason) {
        LocalDateTime now = LocalDateTime.now();
        return builder()
            .id(id)
            .username(username)
            .status("FAILED")
            .attemptTime(now.minusMinutes(5)) // 기본적으로 5분 전에 시도된 것으로 설정
            .failureTime(now)
            .failureReason(failureReason)
            .build();
    }

    /**
     * 인증 대기 응답을 생성합니다.
     * 
     * @param id 인증 ID
     * @param username 사용자명
     * @return 대기 응답
     */
    public static AuthenticationResponse pending(String id, String username) {
        LocalDateTime now = LocalDateTime.now();
        return builder()
            .id(id)
            .username(username)
            .status("PENDING")
            .attemptTime(now)
            .build();
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

    // 상태 검사 메서드들
    public boolean isSuccess() {
        return "SUCCESS".equals(status);
    }

    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    public boolean isPending() {
        return "PENDING".equals(status);
    }

    public boolean hasToken() {
        return accessToken != null && !accessToken.trim().isEmpty();
    }

    public boolean hasFailureReason() {
        return failureReason != null && !failureReason.trim().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthenticationResponse that = (AuthenticationResponse) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(username, that.username) &&
               Objects.equals(status, that.status) &&
               Objects.equals(attemptTime, that.attemptTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, status, attemptTime);
    }

    @Override
    public String toString() {
        return "AuthenticationResponse{" +
               "id='" + id + '\'' +
               ", username='" + username + '\'' +
               ", status='" + status + '\'' +
               ", attemptTime=" + attemptTime +
               ", successTime=" + successTime +
               ", failureTime=" + failureTime +
               ", hasToken=" + hasToken() +
               '}';
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

        public AuthenticationResponse build() {
            return new AuthenticationResponse(this);
        }
    }
}