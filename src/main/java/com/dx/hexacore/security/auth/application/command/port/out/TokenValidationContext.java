package com.dx.hexacore.security.auth.application.command.port.out;

import lombok.Builder;
import lombok.Getter;

/**
 * 토큰 검증 컨텍스트
 * 
 * <p>토큰 검증 시 필요한 추가 정보를 담는 클래스입니다.</p>
 * <p>요청 URL, HTTP 메소드 등의 정보를 포함하여 Keycloak의 리소스 권한 체크에 사용됩니다.</p>
 */
@Getter
@Builder
public class TokenValidationContext {
    
    /**
     * 요청 URI (예: /api/users/123)
     */
    private final String requestUri;
    
    /**
     * HTTP 메소드 (GET, POST, PUT, DELETE 등)
     */
    private final String httpMethod;
    
    /**
     * 클라이언트 IP 주소
     */
    private final String clientIp;
    
    /**
     * User-Agent 헤더
     */
    private final String userAgent;
    
    /**
     * 추가 헤더 정보 (필요시)
     */
    private final java.util.Map<String, String> additionalHeaders;
    
    /**
     * 리소스 권한 체크 활성화 여부
     * Keycloak UMA 2.0 권한 체크를 수행할지 결정
     */
    @Builder.Default
    private final boolean checkResourcePermission = false;
    
    /**
     * 간단한 컨텍스트 생성 메소드
     */
    public static TokenValidationContext of(String requestUri, String httpMethod) {
        return TokenValidationContext.builder()
                .requestUri(requestUri)
                .httpMethod(httpMethod)
                .build();
    }
    
    /**
     * 리소스 권한 체크를 포함한 컨텍스트 생성
     */
    public static TokenValidationContext withResourceCheck(String requestUri, String httpMethod) {
        return TokenValidationContext.builder()
                .requestUri(requestUri)
                .httpMethod(httpMethod)
                .checkResourcePermission(true)
                .build();
    }
}