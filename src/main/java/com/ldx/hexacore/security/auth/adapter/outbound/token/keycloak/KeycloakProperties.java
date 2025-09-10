package com.ldx.hexacore.security.auth.adapter.outbound.token.keycloak;

import org.springframework.util.StringUtils;

public class KeycloakProperties {
    
    private String serverUrl;
    private String realm;
    private String clientId;
    private String clientSecret;
    private String grantType = "password";
    private String scopes = "openid profile email";
    private boolean publicClient = false;
    
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
    
    public String getScopes() {
        return scopes;
    }
    
    public void setScopes(String scopes) {
        this.scopes = scopes;
    }
    
    public boolean isPublicClient() {
        return publicClient;
    }
    
    public void setPublicClient(boolean publicClient) {
        this.publicClient = publicClient;
    }
    
    public String getTokenEndpoint() {
        return String.format("%s/realms/%s/protocol/openid-connect/token", normalizeServerUrl(), realm);
    }
    
    public String getIntrospectionEndpoint() {
        return String.format("%s/realms/%s/protocol/openid-connect/token/introspect", normalizeServerUrl(), realm);
    }
    
    public String getUserInfoEndpoint() {
        return String.format("%s/realms/%s/protocol/openid-connect/userinfo", normalizeServerUrl(), realm);
    }
    
    public boolean isValid() {
        return StringUtils.hasText(serverUrl) &&
               StringUtils.hasText(realm) &&
               StringUtils.hasText(clientId) &&
               (publicClient || StringUtils.hasText(clientSecret));
    }
    
    /**
     * Normalizes the server URL by removing trailing slashes to prevent double slashes in endpoint URLs.
     * 
     * @return normalized server URL without trailing slash
     */
    private String normalizeServerUrl() {
        if (serverUrl == null) {
            return null;
        }
        return serverUrl.endsWith("/") ? serverUrl.substring(0, serverUrl.length() - 1) : serverUrl;
    }
}