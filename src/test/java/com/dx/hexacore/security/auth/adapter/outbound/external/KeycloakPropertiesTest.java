package com.dx.hexacore.security.auth.adapter.outbound.external;

import com.dx.hexacore.security.auth.adapter.outbound.external.KeycloakProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class KeycloakPropertiesTest {
    
    @Test
    @DisplayName("유효한 설정 검증 - true 반환")
    void isValid_AllRequiredProperties() {
        // Given
        KeycloakProperties properties = new KeycloakProperties();
        properties.setServerUrl("http://localhost:8080");
        properties.setRealm("test-realm");
        properties.setClientId("test-client");
        properties.setClientSecret("test-secret");
        
        // When
        boolean result = properties.isValid();
        
        // Then
        assertThat(result).isTrue();
    }
    
    @Test
    @DisplayName("필수 설정 누락 - false 반환")
    void isValid_MissingRequiredProperties() {
        // Given
        KeycloakProperties properties = new KeycloakProperties();
        properties.setServerUrl("http://localhost:8080");
        properties.setRealm("test-realm");
        // clientId와 clientSecret 누락
        
        // When
        boolean result = properties.isValid();
        
        // Then
        assertThat(result).isFalse();
    }
    
    @Test
    @DisplayName("Token endpoint URL 생성")
    void getTokenEndpoint() {
        // Given
        KeycloakProperties properties = new KeycloakProperties();
        properties.setServerUrl("http://localhost:8080");
        properties.setRealm("test-realm");
        
        // When
        String endpoint = properties.getTokenEndpoint();
        
        // Then
        assertThat(endpoint).isEqualTo("http://localhost:8080/realms/test-realm/protocol/openid-connect/token");
    }
    
    @Test
    @DisplayName("Introspection endpoint URL 생성")
    void getIntrospectionEndpoint() {
        // Given
        KeycloakProperties properties = new KeycloakProperties();
        properties.setServerUrl("http://localhost:8080");
        properties.setRealm("test-realm");
        
        // When
        String endpoint = properties.getIntrospectionEndpoint();
        
        // Then
        assertThat(endpoint).isEqualTo("http://localhost:8080/realms/test-realm/protocol/openid-connect/token/introspect");
    }
    
    @Test
    @DisplayName("기본값 확인")
    void defaultValues() {
        // Given
        KeycloakProperties properties = new KeycloakProperties();
        
        // Then
        assertThat(properties.getGrantType()).isEqualTo("password");
        assertThat(properties.isDisableSslValidation()).isFalse();
        assertThat(properties.getConnectTimeout()).isEqualTo(10000);
        assertThat(properties.getReadTimeout()).isEqualTo(10000);
    }
}