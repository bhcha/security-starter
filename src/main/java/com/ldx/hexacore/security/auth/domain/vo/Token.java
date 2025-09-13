package com.ldx.hexacore.security.auth.domain.vo;

import com.ldx.hexacore.security.util.ValidationUtils;
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
public final class Token {
    
    // 상수는 SecurityConstants에서 주입받도록 변경
    
    private final String accessToken;
    private final String refreshToken;
    private final long expiresIn;
    private final boolean expired;

    // private 생성자 - 상수 주입 버전
    private Token(String accessToken, String refreshToken, long expiresIn, 
                  long minExpiresIn, long maxExpiresIn) {
        this(accessToken, refreshToken, expiresIn, false, minExpiresIn, maxExpiresIn);
    }
    
    // private 생성자 - 기본 상수 버전 (deprecated)
    private Token(String accessToken, String refreshToken, long expiresIn, 
                  long minExpiresIn, long maxExpiresIn, boolean deprecated) {
        this(accessToken, refreshToken, expiresIn, false, minExpiresIn, maxExpiresIn);
    }
    
    // private 생성자 (만료 상태 포함)
    private Token(String accessToken, String refreshToken, long expiresIn, boolean expired,
                  long minExpiresIn, long maxExpiresIn) {
        ValidationUtils.requireNonNullOrEmpty(accessToken, "Access token");
        ValidationUtils.requireNonNullOrEmpty(refreshToken, "Refresh token");
        ValidationUtils.requireInRange(expiresIn, minExpiresIn, maxExpiresIn, "Expires in");
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
     * @param minExpiresIn 최소 만료시간 (초)
     * @param maxExpiresIn 최대 만료시간 (초)
     * @return 생성된 Token
     * @throws IllegalArgumentException 유효하지 않은 값인 경우
     */
    public static Token of(String accessToken, String refreshToken, long expiresIn, 
                          long minExpiresIn, long maxExpiresIn) {
        return new Token(accessToken, refreshToken, expiresIn, minExpiresIn, maxExpiresIn);
    }

    /**
     * 기본 제약사항으로 토큰 정보를 생성합니다.
     * 
     * @deprecated 외부에서 상수값을 주입받는 {@link #of(String, String, long, long, long)} 사용을 권장합니다.
     * @param accessToken 액세스 토큰
     * @param refreshToken 리프레시 토큰
     * @param expiresIn 만료시간(초)
     * @return 생성된 Token
     * @throws IllegalArgumentException 유효하지 않은 값인 경우
     */
    @Deprecated
    public static Token of(String accessToken, String refreshToken, long expiresIn) {
        return new Token(accessToken, refreshToken, expiresIn, 1L, 86400L, true);
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
     * @param minExpiresIn 최소 만료시간 (초) - 원본과 동일한 제약사항 적용
     * @param maxExpiresIn 최대 만료시간 (초) - 원본과 동일한 제약사항 적용  
     * @return 만료된 상태의 새로운 Token
     */
    public Token expire(long minExpiresIn, long maxExpiresIn) {
        return new Token(this.accessToken, this.refreshToken, this.expiresIn, true, 
                        minExpiresIn, maxExpiresIn);
    }

    /**
     * 기본 제약사항으로 만료된 상태의 새로운 Token을 반환합니다.
     * 
     * @deprecated 외부에서 상수값을 주입받는 {@link #expire(long, long)} 사용을 권장합니다.
     * @return 만료된 상태의 새로운 Token
     */
    @Deprecated  
    public Token expire() {
        return new Token(this.accessToken, this.refreshToken, this.expiresIn, true, 1L, 86400L);
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