package com.dx.hexacore.security.auth.application.command.port.in;

import com.dx.hexacore.security.auth.domain.vo.Token;
import java.util.Objects;
import java.util.Optional;

/**
 * 인증 결과를 나타내는 객체.
 * 인증 성공/실패와 관련된 정보를 포함합니다.
 * 
 * @since 1.0.0
 */
public class AuthenticationResult {
    
    private final boolean success;
    private final Token token;
    private final String failureReason;
    private final String username;
    
    private AuthenticationResult(boolean success, Token token, String failureReason, String username) {
        this.success = success;
        this.token = token;
        this.failureReason = failureReason;
        this.username = Objects.requireNonNull(username, "username cannot be null");
    }
    
    /**
     * 인증 성공 결과를 생성합니다.
     * 
     * @param username 사용자명
     * @param token 발급된 토큰
     * @return 성공 결과
     */
    public static AuthenticationResult success(String username, Token token) {
        Objects.requireNonNull(token, "token cannot be null for success result");
        return new AuthenticationResult(true, token, null, username);
    }
    
    /**
     * 인증 실패 결과를 생성합니다.
     * 
     * @param username 사용자명
     * @param reason 실패 이유
     * @return 실패 결과
     */
    public static AuthenticationResult failure(String username, String reason) {
        Objects.requireNonNull(reason, "reason cannot be null for failure result");
        return new AuthenticationResult(false, null, reason, username);
    }
    
    /**
     * 인증 성공 여부를 반환합니다.
     * 
     * @return 성공 시 true, 실패 시 false
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * 인증 실패 여부를 반환합니다.
     * 
     * @return 실패 시 true, 성공 시 false
     */
    public boolean isFailure() {
        return !success;
    }
    
    /**
     * 발급된 토큰을 반환합니다.
     * 
     * @return 성공 시 토큰, 실패 시 empty
     */
    public Optional<Token> getToken() {
        return Optional.ofNullable(token);
    }
    
    /**
     * 실패 이유를 반환합니다.
     * 
     * @return 실패 시 이유, 성공 시 empty
     */
    public Optional<String> getFailureReason() {
        return Optional.ofNullable(failureReason);
    }
    
    /**
     * 사용자명을 반환합니다.
     * 
     * @return 사용자명
     */
    public String getUsername() {
        return username;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthenticationResult that = (AuthenticationResult) o;
        return success == that.success &&
               Objects.equals(token, that.token) &&
               Objects.equals(failureReason, that.failureReason) &&
               Objects.equals(username, that.username);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(success, token, failureReason, username);
    }
    
    @Override
    public String toString() {
        return "AuthenticationResult{" +
                "success=" + success +
                ", username='" + username + '\'' +
                ", hasToken=" + (token != null) +
                ", failureReason='" + failureReason + '\'' +
                '}';
    }
}