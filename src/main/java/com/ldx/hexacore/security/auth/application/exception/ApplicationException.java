package com.ldx.hexacore.security.auth.application.exception;

/**
 * 애플리케이션 계층 예외의 기본 클래스.
 * 
 * @since 1.0.0
 */
public abstract class ApplicationException extends RuntimeException {

    protected ApplicationException(String message) {
        super(message);
    }

    protected ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 예외에 대한 고유 에러 코드를 반환합니다.
     * 
     * @return 에러 코드
     */
    public abstract String getErrorCode();
}