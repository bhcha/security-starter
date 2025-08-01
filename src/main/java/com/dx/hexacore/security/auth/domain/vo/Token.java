package com.dx.hexacore.security.auth.domain.vo;

import jakarta.persistence.Embeddable;
import java.util.Objects;

/**
 * JWT 토큰 정보를 담는 값 객체.
 * 
 * <p>액세스 토큰, 리프레시 토큰, 만료시간을 포함한 불변 객체입니다.
 * 
 * <p>제약사항:
 * <ul>
 *   <li>AccessToken: null/empty 불가</li>
 *   <li>RefreshToken: null/empty 불가</li>
 *   <li>ExpiresIn: 1-86400초 (최대 24시간)</li>
 * </ul>
 * 
 * @since 1.0.0
 */
@Embeddable
public final class Token {
    
    private static final long MIN_EXPIRES_IN = 1L;
    private static final long MAX_EXPIRES_IN = 86400L; // 24시간
    
    private final String accessToken;
    private final String refreshToken;
    private final long expiresIn;
    private final boolean expired;
    
    // JPA를 위한 protected 기본 생성자
    protected Token() {
        this.accessToken = null;
        this.refreshToken = null;
        this.expiresIn = 0L;
        this.expired = false;
    }
    
    // private 생성자
    private Token(String accessToken, String refreshToken, long expiresIn) {
        this(accessToken, refreshToken, expiresIn, false);
    }
    
    // private 생성자 (만료 상태 포함)
    private Token(String accessToken, String refreshToken, long expiresIn, boolean expired) {
        validateAccessToken(accessToken);
        validateRefreshToken(refreshToken);
        validateExpiresIn(expiresIn);
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.expired = expired;
    }
    
    // ===== 정적 팩토리 메서드 =====
    
    /**
     * 토큰 정보로부터 Token을 생성합니다.
     * 
     * @param accessToken 액세스 토큰
     * @param refreshToken 리프레시 토큰
     * @param expiresIn 만료시간(초)
     * @return 생성된 Token
     * @throws IllegalArgumentException 유효하지 않은 값인 경우
     */
    public static Token of(String accessToken, String refreshToken, long expiresIn) {
        return new Token(accessToken, refreshToken, expiresIn);
    }
    
    // ===== 유틸리티 메서드 =====
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public long getExpiresIn() {
        return expiresIn;
    }
    
    public boolean isExpired() {
        return expired;
    }
    
    /**
     * 만료된 상태의 새로운 Token을 반환합니다.
     * 
     * @return 만료된 상태의 새로운 Token
     */
    public Token expire() {
        return new Token(this.accessToken, this.refreshToken, this.expiresIn, true);
    }
    
    // ===== Private 검증 메서드 =====
    
    private static void validateAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("Access token cannot be empty");
        }
    }
    
    private static void validateRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token cannot be empty");
        }
    }
    
    private static void validateExpiresIn(long expiresIn) {
        if (expiresIn <= 0) {
            throw new IllegalArgumentException("Expires in must be positive");
        }
        
        if (expiresIn > MAX_EXPIRES_IN) {
            throw new IllegalArgumentException(
                String.format("Expires in cannot exceed %d seconds", MAX_EXPIRES_IN)
            );
        }
    }
    
    // ===== Object 메서드 =====
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token = (Token) o;
        return expiresIn == token.expiresIn &&
               expired == token.expired &&
               Objects.equals(accessToken, token.accessToken) &&
               Objects.equals(refreshToken, token.refreshToken);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(accessToken, refreshToken, expiresIn, expired);
    }
    
    @Override
    public String toString() {
        return "Token{" +
                "accessToken='***'" +
                ", refreshToken='***'" +
                ", expiresIn=" + expiresIn +
                '}';
    }
}