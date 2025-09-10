package com.ldx.hexacore.security.auth.application.command.port.in;

/**
 * 토큰 갱신 실패 시 발생하는 예외.
 * 리프레시 토큰이 만료되었거나 유효하지 않을 때 발생합니다.
 * 
 * @since 1.0.0
 */
public class TokenRefreshException extends RuntimeException {
    
    /**
     * 메시지와 함께 예외를 생성합니다.
     * 
     * @param message 예외 메시지
     */
    public TokenRefreshException(String message) {
        super(message);
    }
    
    /**
     * 메시지와 원인과 함께 예외를 생성합니다.
     * 
     * @param message 예외 메시지
     * @param cause 원인 예외
     */
    public TokenRefreshException(String message, Throwable cause) {
        super(message, cause);
    }
}