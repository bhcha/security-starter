package com.dx.hexacore.security.auth.adapter.outbound.token.keycloak;

import org.springframework.util.StringUtils;

public class KeycloakProperties {
    
    private String serverUrl;
    private String realm;
    private String clientId;
    private String clientSecret;
    private String grantType = "password";
    
    public String getServerUrl() {
        return serverUrl;
    }
    
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }
    
    public String getRealm() {
        return realm;
    }
    
    public void setRealm(String realm) {
        this.realm = realm;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public String getClientSecret() {
        return clientSecret;
    }
    
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
    
    public String getGrantType() {
        return grantType;
    }
    
    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }
    
    public String getTokenEndpoint() {
        return String.format("%s/realms/%s/protocol/openid-connect/token", serverUrl, realm);
    }
    
    public String getIntrospectionEndpoint() {
        return String.format("%s/realms/%s/protocol/openid-connect/token/introspect", serverUrl, realm);
    }
    
    public String getUserInfoEndpoint() {
        return String.format("%s/realms/%s/protocol/openid-connect/userinfo", serverUrl, realm);
    }
    
    public boolean isValid() {
        return StringUtils.hasText(serverUrl) &&
               StringUtils.hasText(realm) &&
               StringUtils.hasText(clientId) &&
               StringUtils.hasText(clientSecret);
    }
}