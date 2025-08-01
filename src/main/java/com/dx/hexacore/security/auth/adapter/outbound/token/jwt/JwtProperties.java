package com.dx.hexacore.security.auth.adapter.outbound.token.jwt;

import org.springframework.util.StringUtils;

public class JwtProperties {
    
    private String secret;
    private int accessTokenExpiration = 3600; // 1시간 (초)
    private int refreshTokenExpiration = 604800; // 7일 (초)
    private String issuer = "security-starter";

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public int getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public void setAccessTokenExpiration(int accessTokenExpiration) {
        this.accessTokenExpiration = accessTokenExpiration;
    }

    public int getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    public void setRefreshTokenExpiration(int refreshTokenExpiration) {
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
    
    public boolean isValid() {
        return StringUtils.hasText(secret) && secret.length() >= 32; // 256bit 이상
    }
}