package com.ldx.hexacore.security.auth.application.command.port.out;

/**
 * 외부 인증 제공자와의 통신 실패 시 발생하는 예외.
 * Keycloak 등의 외부 시스템과의 연동 중 오류가 발생했을 때 사용됩니다.
 * 
 * @since 1.0.0
 */
public class ExternalAuthException extends Exception {
    
    /**
     * 메시지와 함께 예외를 생성합니다.
     * 
     * @param message 예외 메시지
     */
    public ExternalAuthException(String message) {
        super(message);
    }
    
    /**
     * 메시지와 원인과 함께 예외를 생성합니다.
     * 
     * @param message 예외 메시지
     * @param cause 원인 예외
     */
    public ExternalAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}