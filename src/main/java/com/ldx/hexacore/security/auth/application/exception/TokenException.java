package com.ldx.hexacore.security.auth.application.exception;

/**
 * Token 관련 예외
 * 
 * @since 1.0.0
 */
public class TokenException extends RuntimeException {
    
    public TokenException(String message) {
        super(message);
    }
    
    public TokenException(String message, Throwable cause) {
        super(message, cause);
    }
    
    // Factory methods for common token errors
    public static TokenException expired() {
        return new TokenException("Token has expired");
    }
    
    public static TokenException invalid() {
        return new TokenException("Invalid token");
    }
    
    public static TokenException notFound() {
        return new TokenException("Token not found");
    }
    
    public static TokenException malformed() {
        return new TokenException("Malformed token");
    }
}