package com.ldx.hexacore.security.auth.application.exception;

/**
 * Authentication 관련 예외
 * 
 * @since 1.0.0
 */
public class AuthenticationException extends RuntimeException {
    
    public AuthenticationException(String message) {
        super(message);
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    // Factory methods for common authentication errors
    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException("Invalid credentials");
    }
    
    public static AuthenticationException accountLocked() {
        return new AuthenticationException("Account is locked");
    }
    
    public static AuthenticationException accountDisabled() {
        return new AuthenticationException("Account is disabled");
    }
    
    public static AuthenticationException sessionExpired() {
        return new AuthenticationException("Session has expired");
    }
}