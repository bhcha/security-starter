package com.dx.hexacore.security.auth.application.command.port.out;

/**
 * 토큰 제공자에서 발생할 수 있는 에러 코드를 정의하는 열거형
 * 
 * <p>각 토큰 제공자에서 공통으로 사용할 수 있는 에러 분류를 제공합니다.</p>
 */
public enum TokenProviderErrorCode {
    
    /**
     * 토큰 발급 실패
     */
    TOKEN_ISSUE_FAILED,
    
    /**
     * 토큰 검증 실패
     */
    TOKEN_VALIDATION_FAILED,
    
    /**
     * 토큰 갱신 실패
     */
    TOKEN_REFRESH_FAILED,
    
    /**
     * 유효하지 않은 자격증명
     */
    INVALID_CREDENTIALS,
    
    /**
     * 토큰 만료
     */
    TOKEN_EXPIRED,
    
    /**
     * 토큰 제공자 서비스 불가
     */
    PROVIDER_UNAVAILABLE,
    
    /**
     * 설정 오류
     */
    CONFIGURATION_ERROR
}