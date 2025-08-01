package com.dx.hexacore.security.session.application.exception;

/**
 * 세션 검증 실패 시 발생하는 예외
 * 
 * 세션 상태나 데이터가 유효하지 않을 때 발생합니다.
 */
public class SessionValidationException extends RuntimeException {
    
    /**
     * 기본 생성자
     */
    public SessionValidationException() {
        super();
    }
    
    /**
     * 메시지와 함께 예외 생성
     */
    public SessionValidationException(String message) {
        super(message);
    }
    
    /**
     * 메시지와 원인과 함께 예외 생성
     */
    public SessionValidationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 원인만으로 예외 생성
     */
    public SessionValidationException(Throwable cause) {
        super(cause);
    }
}