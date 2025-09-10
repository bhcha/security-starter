package com.ldx.hexacore.security.auth.application.exception;

/**
 * 입력값 검증 실패 예외.
 * 
 * @since 1.0.0
 */
public class ValidationException extends ApplicationException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getErrorCode() {
        return "VALIDATION_FAILED";
    }
}