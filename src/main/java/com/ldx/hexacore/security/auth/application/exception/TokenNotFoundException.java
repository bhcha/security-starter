package com.ldx.hexacore.security.auth.application.exception;

/**
 * 토큰을 찾을 수 없을 때 발생하는 예외.
 * 
 * @since 1.0.0
 */
public class TokenNotFoundException extends ApplicationException {

    private final String token;

    public TokenNotFoundException(String token) {
        super(String.format("Token not found: %s", maskToken(token)));
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    @Override
    public String getErrorCode() {
        return "TOKEN_NOT_FOUND";
    }

    /**
     * 보안을 위해 토큰을 마스킹합니다.
     * 
     * @param token 원본 토큰
     * @return 마스킹된 토큰
     */
    private static String maskToken(String token) {
        if (token == null || token.length() <= 8) {
            return "***";
        }
        return token.substring(0, 4) + "***" + token.substring(token.length() - 4);
    }
}