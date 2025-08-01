package com.dx.hexacore.security.auth.adapter.outbound.external;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Keycloak 연동을 위한 설정 프로퍼티.
 * application.yml에서 security.auth.keycloak 하위 설정을 매핑합니다.
 * 
 * @since 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "security.auth.keycloak")
public class KeycloakProperties {
    
    /**
     * Keycloak 서버 URL
     */
    private String serverUrl;
    
    /**
     * Keycloak Realm 이름
     */
    private String realm;
    
    /**
     * Client ID
     */
    private String clientId;
    
    /**
     * Client Secret
     */
    private String clientSecret;
    
    /**
     * Grant Type (기본값: password)
     */
    private String grantType = "password";
    
    /**
     * SSL 검증 비활성화 여부 (개발 환경용)
     */
    private boolean disableSslValidation = false;
    
    /**
     * 연결 타임아웃 (밀리초)
     */
    private int connectTimeout = 10000;
    
    /**
     * 읽기 타임아웃 (밀리초)
     */
    private int readTimeout = 10000;
    
    /**
     * Token endpoint URL을 생성합니다.
     * 
     * @return token endpoint URL
     */
    public String getTokenEndpoint() {
        return String.format("%s/realms/%s/protocol/openid-connect/token", serverUrl, realm);
    }
    
    /**
     * Token introspection endpoint URL을 생성합니다.
     * 
     * @return introspection endpoint URL
     */
    public String getIntrospectionEndpoint() {
        return String.format("%s/realms/%s/protocol/openid-connect/token/introspect", serverUrl, realm);
    }
    
    /**
     * 필수 설정이 모두 입력되었는지 검증합니다.
     * 
     * @return 유효한 설정인 경우 true
     */
    public boolean isValid() {
        return serverUrl != null && !serverUrl.isBlank() &&
               realm != null && !realm.isBlank() &&
               clientId != null && !clientId.isBlank() &&
               clientSecret != null && !clientSecret.isBlank();
    }
}