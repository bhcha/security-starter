package com.ldx.hexacore.security.auth.application.command.port.out;

/**
 * 토큰 검증 컨텍스트
 * 
 * <p>토큰 검증 시 필요한 추가 정보를 담는 클래스입니다.</p>
 * <p>요청 URL, HTTP 메소드 등의 정보를 포함하여 Keycloak의 리소스 권한 체크에 사용됩니다.</p>
 */
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
    private final boolean checkResourcePermission;
    
    public TokenValidationContext(String requestUri, String httpMethod, String clientIp, 
                                 String userAgent, java.util.Map<String, String> additionalHeaders, 
                                 boolean checkResourcePermission) {
        this.requestUri = requestUri;
        this.httpMethod = httpMethod;
        this.clientIp = clientIp;
        this.userAgent = userAgent;
        this.additionalHeaders = additionalHeaders;
        this.checkResourcePermission = checkResourcePermission;
    }
    
    // Getter methods
    public String getRequestUri() { return requestUri; }
    public String getHttpMethod() { return httpMethod; }
    public String getClientIp() { return clientIp; }
    public String getUserAgent() { return userAgent; }
    public java.util.Map<String, String> getAdditionalHeaders() { return additionalHeaders; }
    public boolean isCheckResourcePermission() { return checkResourcePermission; }
    
    // Builder pattern
    public static TokenValidationContextBuilder builder() {
        return new TokenValidationContextBuilder();
    }
    
    public static class TokenValidationContextBuilder {
        private String requestUri;
        private String httpMethod;
        private String clientIp;
        private String userAgent;
        private java.util.Map<String, String> additionalHeaders;
        private boolean checkResourcePermission = false;
        
        public TokenValidationContextBuilder requestUri(String requestUri) {
            this.requestUri = requestUri;
            return this;
        }
        
        public TokenValidationContextBuilder httpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }
        
        public TokenValidationContextBuilder clientIp(String clientIp) {
            this.clientIp = clientIp;
            return this;
        }
        
        public TokenValidationContextBuilder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }
        
        public TokenValidationContextBuilder additionalHeaders(java.util.Map<String, String> additionalHeaders) {
            this.additionalHeaders = additionalHeaders;
            return this;
        }
        
        public TokenValidationContextBuilder checkResourcePermission(boolean checkResourcePermission) {
            this.checkResourcePermission = checkResourcePermission;
            return this;
        }
        
        public TokenValidationContext build() {
            return new TokenValidationContext(requestUri, httpMethod, clientIp, userAgent, additionalHeaders, checkResourcePermission);
        }
    }
    
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