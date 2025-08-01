package com.dx.hexacore.security.session.application.exception;

/**
 * 세션 쿼리 처리 실패 시 발생하는 예외
 * 
 * 세션 관련 쿼리 처리 중 발생하는 예외를 나타냅니다.
 */
public class SessionQueryException extends RuntimeException {
    
    /**
     * 기본 생성자
     */
    public SessionQueryException() {
        super();
    }
    
    /**
     * 메시지와 함께 예외 생성
     */
    public SessionQueryException(String message) {
        super(message);
    }
    
    /**
     * 메시지와 원인과 함께 예외 생성
     */
    public SessionQueryException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 원인만으로 예외 생성
     */
    public SessionQueryException(Throwable cause) {
        super(cause);
    }
}