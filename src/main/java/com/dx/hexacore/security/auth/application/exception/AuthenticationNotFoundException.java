package com.dx.hexacore.security.auth.application.exception;

/**
 * 인증 정보를 찾을 수 없을 때 발생하는 예외.
 * 
 * @since 1.0.0
 */
public class AuthenticationNotFoundException extends ApplicationException {

    private final String authenticationId;

    public AuthenticationNotFoundException(String authenticationId) {
        super(String.format("Authentication not found with ID: %s", authenticationId));
        this.authenticationId = authenticationId;
    }

    public String getAuthenticationId() {
        return authenticationId;
    }

    @Override
    public String getErrorCode() {
        return "AUTHENTICATION_NOT_FOUND";
    }
}