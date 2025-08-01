package com.dx.hexacore.security.auth.application.command.port.in;

import java.util.Objects;
import java.util.Optional;

/**
 * 토큰 검증 결과를 나타내는 객체.
 * 토큰의 유효성 검증 결과와 관련된 정보를 포함합니다.
 * 
 * @since 1.0.0
 */
public class TokenValidationResult {
    
    private final boolean valid;
    private final String accessToken;
    private final String invalidReason;
    
    private TokenValidationResult(boolean valid, String accessToken, String invalidReason) {
        this.valid = valid;
        this.accessToken = Objects.requireNonNull(accessToken, "accessToken cannot be null");
        this.invalidReason = invalidReason;
    }
    
    /**
     * 토큰 유효성 검증 성공 결과를 생성합니다.
     * 
     * @param accessToken 유효한 액세스 토큰
     * @return 유효성 검증 성공 결과
     */
    public static TokenValidationResult valid(String accessToken) {
        return new TokenValidationResult(true, accessToken, null);
    }
    
    /**
     * 토큰 유효성 검증 실패 결과를 생성합니다.
     * 
     * @param accessToken 무효한 액세스 토큰
     * @param reason 무효한 이유
     * @return 유효성 검증 실패 결과
     */
    public static TokenValidationResult invalid(String accessToken, String reason) {
        Objects.requireNonNull(reason, "reason cannot be null for invalid result");
        return new TokenValidationResult(false, accessToken, reason);
    }
    
    /**
     * 토큰 유효 여부를 반환합니다.
     * 
     * @return 유효 시 true, 무효 시 false
     */
    public boolean isValid() {
        return valid;
    }
    
    /**
     * 토큰 무효 여부를 반환합니다.
     * 
     * @return 무효 시 true, 유효 시 false
     */
    public boolean isInvalid() {
        return !valid;
    }
    
    /**
     * 검증 대상 액세스 토큰을 반환합니다.
     * 
     * @return 액세스 토큰
     */
    public String getAccessToken() {
        return accessToken;
    }
    
    /**
     * 토큰이 무효한 이유를 반환합니다.
     * 
     * @return 무효 시 이유, 유효 시 empty
     */
    public Optional<String> getInvalidReason() {
        return Optional.ofNullable(invalidReason);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenValidationResult that = (TokenValidationResult) o;
        return valid == that.valid &&
               Objects.equals(accessToken, that.accessToken) &&
               Objects.equals(invalidReason, that.invalidReason);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(valid, accessToken, invalidReason);
    }
    
    @Override
    public String toString() {
        return "TokenValidationResult{" +
                "valid=" + valid +
                ", accessToken='[PROTECTED]'" +
                ", invalidReason='" + invalidReason + '\'' +
                '}';
    }
}