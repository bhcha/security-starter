package com.ldx.hexacore.security.auth.application.query.port.in;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * 토큰 정보 조회 응답 DTO.
 * 
 * @since 1.0.0
 */
public class TokenInfoResponse {

    private final String token;
    private final boolean isValid;
    private final LocalDateTime issuedAt;
    private final LocalDateTime expiresAt;
    private final boolean canRefresh;
    private final String tokenType;
    private final String scope;
    private final String authenticationId;
    private final String invalidReason;

    private TokenInfoResponse(Builder builder) {
        this.token = Objects.requireNonNull(builder.token, "Token cannot be null");
        this.isValid = builder.isValid;
        this.issuedAt = builder.issuedAt;
        this.expiresAt = builder.expiresAt;
        this.canRefresh = builder.canRefresh;
        this.tokenType = builder.tokenType;
        this.scope = builder.scope;
        this.authenticationId = builder.authenticationId;
        this.invalidReason = builder.invalidReason;
    }

    /**
     * 유효한 토큰 응답을 생성합니다.
     * 
     * @param token 토큰
     * @param expiresAt 만료 시간
     * @param authenticationId 인증 ID
     * @return 유효한 토큰 응답
     */
    public static TokenInfoResponse valid(String token, LocalDateTime expiresAt, String authenticationId) {
        return builder()
            .token(token)
            .isValid(true)
            .issuedAt(LocalDateTime.now().minusMinutes(30)) // 기본적으로 30분 전 발급
            .expiresAt(expiresAt)
            .authenticationId(authenticationId)
            .canRefresh(true)
            .tokenType("Bearer")
            .build();
    }

    /**
     * 만료된 토큰 응답을 생성합니다.
     * 
     * @param token 토큰
     * @param expiredAt 만료된 시간
     * @return 만료된 토큰 응답
     */
    public static TokenInfoResponse expired(String token, LocalDateTime expiredAt) {
        return builder()
            .token(token)
            .isValid(false)
            .issuedAt(expiredAt.minusHours(1)) // 발급 시간을 만료 1시간 전으로 설정
            .expiresAt(expiredAt)
            .canRefresh(false)
            .build();
    }

    /**
     * 유효하지 않은 토큰 응답을 생성합니다.
     * 
     * @param token 토큰
     * @param reason 유효하지 않은 이유
     * @return 유효하지 않은 토큰 응답
     */
    public static TokenInfoResponse invalid(String token, String reason) {
        return builder()
            .token(token)
            .isValid(false)
            .canRefresh(false)
            .invalidReason(reason)
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
    public String getToken() {
        return token;
    }

    public boolean isValid() {
        return isValid;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean canRefresh() {
        return canRefresh;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getScope() {
        return scope;
    }

    public String getAuthenticationId() {
        return authenticationId;
    }

    public String getInvalidReason() {
        return invalidReason;
    }

    // 상태 검사 메서드들
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isActive() {
        return isValid && !isExpired();
    }

    public boolean isRefreshable() {
        return canRefresh && isValid;
    }

    public boolean hasInvalidReason() {
        return invalidReason != null && !invalidReason.trim().isEmpty();
    }

    /**
     * 토큰 만료까지 남은 시간(분)을 계산합니다.
     * 
     * @return 만료까지 남은 시간(분), 이미 만료된 경우 0
     */
    public long getMinutesUntilExpiration() {
        if (expiresAt == null || isExpired()) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(LocalDateTime.now(), expiresAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenInfoResponse that = (TokenInfoResponse) o;
        return Objects.equals(token, that.token) &&
               isValid == that.isValid &&
               Objects.equals(expiresAt, that.expiresAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token, isValid, expiresAt);
    }

    @Override
    public String toString() {
        return "TokenInfoResponse{" +
               "token='" + token + '\'' +
               ", isValid=" + isValid +
               ", issuedAt=" + issuedAt +
               ", expiresAt=" + expiresAt +
               ", canRefresh=" + canRefresh +
               ", tokenType='" + tokenType + '\'' +
               ", authenticationId='" + authenticationId + '\'' +
               '}';
    }

    /**
     * Builder 클래스.
     */
    public static class Builder {
        private String token;
        private boolean isValid;
        private LocalDateTime issuedAt;
        private LocalDateTime expiresAt;
        private boolean canRefresh;
        private String tokenType;
        private String scope;
        private String authenticationId;
        private String invalidReason;

        private Builder() {}

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder isValid(boolean isValid) {
            this.isValid = isValid;
            return this;
        }

        public Builder issuedAt(LocalDateTime issuedAt) {
            this.issuedAt = issuedAt;
            return this;
        }

        public Builder expiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder canRefresh(boolean canRefresh) {
            this.canRefresh = canRefresh;
            return this;
        }

        public Builder tokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public Builder scope(String scope) {
            this.scope = scope;
            return this;
        }

        public Builder authenticationId(String authenticationId) {
            this.authenticationId = authenticationId;
            return this;
        }

        public Builder invalidReason(String invalidReason) {
            this.invalidReason = invalidReason;
            return this;
        }

        public TokenInfoResponse build() {
            return new TokenInfoResponse(this);
        }
    }
}