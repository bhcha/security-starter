package com.dx.hexacore.security.auth.application.command.port.out;

import java.util.Arrays;

/**
 * 토큰 제공자 타입을 정의하는 열거형
 * 
 * <p>각 토큰 제공자의 종류를 구분하고 코드값과 설명을 제공합니다.</p>
 */
public enum TokenProviderType {
    
    /**
     * Keycloak OAuth2 기반 토큰 제공자
     */
    KEYCLOAK("keycloak", "Keycloak OAuth2 Provider"),
    
    /**
     * Spring JWT 기반 토큰 제공자
     */
    SPRING_JWT("jwt", "Spring JWT Provider");
    
    private final String code;
    private final String description;
    
    TokenProviderType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 토큰 제공자 타입 코드를 반환합니다.
     * 
     * @return 타입 코드
     */
    public String getCode() {
        return code;
    }
    
    /**
     * 토큰 제공자 타입 설명을 반환합니다.
     * 
     * @return 타입 설명
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 코드값으로 토큰 제공자 타입을 찾습니다.
     * 
     * @param code 찾을 코드값
     * @return 매칭되는 토큰 제공자 타입
     * @throws IllegalArgumentException 코드가 null이거나 매칭되는 타입이 없는 경우
     */
    public static TokenProviderType fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("Token provider type code cannot be null");
        }
        
        return Arrays.stream(values())
                .filter(type -> type.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown token provider type code: " + code));
    }
}