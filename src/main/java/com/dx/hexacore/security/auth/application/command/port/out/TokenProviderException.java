package com.dx.hexacore.security.auth.application.command.port.out;

/**
 * 토큰 제공자에서 발생하는 예외를 정의하는 클래스
 * 
 * <p>토큰 발급, 검증, 갱신 과정에서 발생하는 모든 예외 상황을 처리합니다.</p>
 * <p>에러 코드와 제공자 타입 정보를 포함하여 상세한 에러 분석이 가능합니다.</p>
 */
public class TokenProviderException extends RuntimeException {
    
    private final TokenProviderErrorCode errorCode;
    private final String providerType;
    
    /**
     * 기본 생성자
     * 
     * @param message 에러 메시지
     * @param errorCode 에러 코드
     * @param providerType 제공자 타입
     * @throws IllegalArgumentException 필수 매개변수가 null이거나 빈 값인 경우
     */
    public TokenProviderException(String message, TokenProviderErrorCode errorCode, String providerType) {
        super(message);
        validateParameters(message, errorCode, providerType);
        this.errorCode = errorCode;
        this.providerType = providerType;
    }
    
    /**
     * 원인 예외를 포함한 생성자
     * 
     * @param message 에러 메시지
     * @param cause 원인 예외
     * @param errorCode 에러 코드
     * @param providerType 제공자 타입
     * @throws IllegalArgumentException 필수 매개변수가 null이거나 빈 값인 경우
     */
    public TokenProviderException(String message, Throwable cause, TokenProviderErrorCode errorCode, String providerType) {
        super(message, cause);
        validateParameters(message, errorCode, providerType);
        this.errorCode = errorCode;
        this.providerType = providerType;
    }
    
    /**
     * 에러 코드를 반환합니다.
     * 
     * @return 에러 코드
     */
    public TokenProviderErrorCode getErrorCode() {
        return errorCode;
    }
    
    /**
     * 제공자 타입을 반환합니다.
     * 
     * @return 제공자 타입
     */
    public String getProviderType() {
        return providerType;
    }
    
    /**
     * 토큰 발급 실패 예외를 생성합니다.
     * 
     * @param providerType 제공자 타입
     * @param cause 원인 예외
     * @return 토큰 발급 실패 예외
     * @throws IllegalArgumentException 제공자 타입이 null이거나 빈 값인 경우
     */
    public static TokenProviderException tokenIssueFailed(String providerType, Throwable cause) {
        validateProviderType(providerType);
        return new TokenProviderException(
                "Failed to issue token using provider: " + providerType,
                cause,
                TokenProviderErrorCode.TOKEN_ISSUE_FAILED,
                providerType
        );
    }
    
    /**
     * 토큰 검증 실패 예외를 생성합니다.
     * 
     * @param providerType 제공자 타입
     * @param cause 원인 예외
     * @return 토큰 검증 실패 예외
     * @throws IllegalArgumentException 제공자 타입이 null이거나 빈 값인 경우
     */
    public static TokenProviderException tokenValidationFailed(String providerType, Throwable cause) {
        validateProviderType(providerType);
        return new TokenProviderException(
                "Failed to validate token using provider: " + providerType,
                cause,
                TokenProviderErrorCode.TOKEN_VALIDATION_FAILED,
                providerType
        );
    }
    
    /**
     * 토큰 갱신 실패 예외를 생성합니다.
     * 
     * @param providerType 제공자 타입
     * @param cause 원인 예외
     * @return 토큰 갱신 실패 예외
     * @throws IllegalArgumentException 제공자 타입이 null이거나 빈 값인 경우
     */
    public static TokenProviderException tokenRefreshFailed(String providerType, Throwable cause) {
        validateProviderType(providerType);
        return new TokenProviderException(
                "Failed to refresh token using provider: " + providerType,
                cause,
                TokenProviderErrorCode.TOKEN_REFRESH_FAILED,
                providerType
        );
    }
    
    /**
     * 유효하지 않은 자격증명 예외를 생성합니다.
     * 
     * @param providerType 제공자 타입
     * @return 유효하지 않은 자격증명 예외
     * @throws IllegalArgumentException 제공자 타입이 null이거나 빈 값인 경우
     */
    public static TokenProviderException invalidCredentials(String providerType) {
        validateProviderType(providerType);
        return new TokenProviderException(
                "Invalid credentials provided to provider: " + providerType,
                TokenProviderErrorCode.INVALID_CREDENTIALS,
                providerType
        );
    }
    
    /**
     * 토큰 만료 예외를 생성합니다.
     * 
     * @param providerType 제공자 타입
     * @return 토큰 만료 예외
     * @throws IllegalArgumentException 제공자 타입이 null이거나 빈 값인 경우
     */
    public static TokenProviderException tokenExpired(String providerType) {
        validateProviderType(providerType);
        return new TokenProviderException(
                "Token has expired in provider: " + providerType,
                TokenProviderErrorCode.TOKEN_EXPIRED,
                providerType
        );
    }
    
    /**
     * 제공자 서비스 불가 예외를 생성합니다.
     * 
     * @param providerType 제공자 타입
     * @param cause 원인 예외
     * @return 제공자 서비스 불가 예외
     * @throws IllegalArgumentException 제공자 타입이 null이거나 빈 값인 경우
     */
    public static TokenProviderException providerUnavailable(String providerType, Throwable cause) {
        validateProviderType(providerType);
        return new TokenProviderException(
                "Token provider is unavailable: " + providerType,
                cause,
                TokenProviderErrorCode.PROVIDER_UNAVAILABLE,
                providerType
        );
    }
    
    private static void validateParameters(String message, TokenProviderErrorCode errorCode, String providerType) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }
        if (errorCode == null) {
            throw new IllegalArgumentException("Error code cannot be null");
        }
        validateProviderType(providerType);
    }
    
    private static void validateProviderType(String providerType) {
        if (providerType == null || providerType.trim().isEmpty()) {
            throw new IllegalArgumentException("Provider type cannot be null or empty");
        }
    }
}