package com.dx.hexacore.security.auth.application.projection;

import java.time.LocalDateTime;

/**
 * 토큰 정보 조회용 Projection.
 * 
 * @since 1.0.0
 */
public class TokenInfoProjection {

    private final String token;
    private final boolean isValid;
    private final LocalDateTime issuedAt;
    private final LocalDateTime expiresAt;
    private final boolean canRefresh;
    private final String tokenType;
    private final String scope;
    private final String authenticationId;

    private TokenInfoProjection(Builder builder) {
        this.token = builder.token;
        this.isValid = builder.isValid;
        this.issuedAt = builder.issuedAt;
        this.expiresAt = builder.expiresAt;
        this.canRefresh = builder.canRefresh;
        this.tokenType = builder.tokenType;
        this.scope = builder.scope;
        this.authenticationId = builder.authenticationId;
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

        public TokenInfoProjection build() {
            return new TokenInfoProjection(this);
        }
    }
}