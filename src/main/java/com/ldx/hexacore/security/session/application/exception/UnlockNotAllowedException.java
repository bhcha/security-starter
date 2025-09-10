package com.ldx.hexacore.security.session.application.exception;

/**
 * 계정 잠금 해제가 허용되지 않는 경우 발생하는 예외
 * 
 * 권한 부족, 정책 위반, 시간 제한 등으로 계정 잠금 해제가 거부될 때 발생합니다.
 */
public class UnlockNotAllowedException extends RuntimeException {
    
    /**
     * 기본 생성자
     */
    public UnlockNotAllowedException() {
        super();
    }
    
    /**
     * 메시지와 함께 예외 생성
     */
    public UnlockNotAllowedException(String message) {
        super(message);
    }
    
    /**
     * 메시지와 원인과 함께 예외 생성
     */
    public UnlockNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 원인만으로 예외 생성
     */
    public UnlockNotAllowedException(Throwable cause) {
        super(cause);
    }
}