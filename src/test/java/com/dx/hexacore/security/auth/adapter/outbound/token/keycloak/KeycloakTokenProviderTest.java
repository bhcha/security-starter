package com.dx.hexacore.security.auth.adapter.outbound.token.keycloak;

import com.dx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.dx.hexacore.security.auth.application.command.port.out.TokenProviderException;
import com.dx.hexacore.security.auth.application.command.port.out.TokenProviderType;
import com.dx.hexacore.security.auth.application.command.port.out.TokenValidationResult;
import com.dx.hexacore.security.auth.domain.vo.Credentials;
import com.dx.hexacore.security.config.properties.HexacoreSecurityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("KeycloakTokenProvider 리팩토링 테스트")
class KeycloakTokenProviderTest {

    private KeycloakTokenProvider keycloakTokenProvider;
    private HexacoreSecurityProperties.TokenProvider.KeycloakProperties properties;

    @BeforeEach
    void setUp() {
        properties = new HexacoreSecurityProperties.TokenProvider.KeycloakProperties();
        properties.setServerUrl("https://keycloak.example.com");
        properties.setRealm("test-realm");
        properties.setClientId("test-client");
        properties.setClientSecret("test-secret");
        
        keycloakTokenProvider = new KeycloakTokenProvider(properties);
    }

    @Test
    @DisplayName("TokenProvider 인터페이스가 정확히 구현되었는지 확인")
    void shouldImplementTokenProviderInterface() {
        // Given & When & Then
        assertThat(keycloakTokenProvider).isInstanceOf(TokenProvider.class);
        assertThat(keycloakTokenProvider.getProviderType()).isEqualTo(TokenProviderType.KEYCLOAK);
    }

    @Test
    @DisplayName("null 자격증명으로 토큰 발급 시 예외 발생")
    void shouldThrowExceptionWhenNullCredentials() {
        // Given
        Credentials nullCredentials = null;
        
        // When & Then
        assertThatThrownBy(() -> keycloakTokenProvider.issueToken(nullCredentials))
            .isInstanceOf(TokenProviderException.class);
    }

    @Test
    @DisplayName("null 토큰 검증 시 invalid 결과 반환")
    void shouldReturnInvalidWhenNullToken() {
        // Given
        String nullToken = null;
        
        // When
        TokenValidationResult result = keycloakTokenProvider.validateToken(nullToken);
        
        // Then
        assertThat(result.valid()).isFalse();
    }

    @Test
    @DisplayName("빈 토큰 검증 시 invalid 결과 반환")
    void shouldReturnInvalidWhenEmptyToken() {
        // Given
        String emptyToken = "";
        
        // When
        TokenValidationResult result = keycloakTokenProvider.validateToken(emptyToken);
        
        // Then
        assertThat(result.valid()).isFalse();
    }

    @Test
    @DisplayName("빈 공백 토큰 검증 시 invalid 결과 반환")
    void shouldReturnInvalidWhenBlankToken() {
        // Given
        String blankToken = "   ";
        
        // When
        TokenValidationResult result = keycloakTokenProvider.validateToken(blankToken);
        
        // Then
        assertThat(result.valid()).isFalse();
    }

    @Test
    @DisplayName("서버 URL에 trailing slash가 있어도 정상적인 endpoint URL 생성")
    void shouldHandleTrailingSlashInServerUrl() throws Exception {
        // Given
        HexacoreSecurityProperties.TokenProvider.KeycloakProperties propertiesWithSlash = 
            new HexacoreSecurityProperties.TokenProvider.KeycloakProperties();
        propertiesWithSlash.setServerUrl("https://authdev.daewoong.co.kr/");
        propertiesWithSlash.setRealm("backoffice-api");
        propertiesWithSlash.setClientId("test-client");
        propertiesWithSlash.setClientSecret("test-secret");
        
        KeycloakTokenProvider providerWithSlash = new KeycloakTokenProvider(propertiesWithSlash);
        
        // When - Use reflection to access the private properties field
        java.lang.reflect.Field propertiesField = KeycloakTokenProvider.class.getDeclaredField("properties");
        propertiesField.setAccessible(true);
        KeycloakProperties internalProperties = (KeycloakProperties) propertiesField.get(providerWithSlash);
        
        String tokenEndpoint = internalProperties.getTokenEndpoint();
        String introspectionEndpoint = internalProperties.getIntrospectionEndpoint();
        String userInfoEndpoint = internalProperties.getUserInfoEndpoint();
        
        // Then
        assertThat(tokenEndpoint).isEqualTo("https://authdev.daewoong.co.kr/realms/backoffice-api/protocol/openid-connect/token");
        assertThat(introspectionEndpoint).isEqualTo("https://authdev.daewoong.co.kr/realms/backoffice-api/protocol/openid-connect/token/introspect");
        assertThat(userInfoEndpoint).isEqualTo("https://authdev.daewoong.co.kr/realms/backoffice-api/protocol/openid-connect/userinfo");
    }

    @Test
    @DisplayName("null refresh 토큰으로 갱신 시 예외 발생")
    void shouldThrowExceptionWhenNullRefreshToken() {
        // Given
        String nullRefreshToken = null;
        
        // When & Then
        assertThatThrownBy(() -> keycloakTokenProvider.refreshToken(nullRefreshToken))
            .isInstanceOf(TokenProviderException.class);
    }

    @Test
    @DisplayName("빈 refresh 토큰으로 갱신 시 예외 발생")
    void shouldThrowExceptionWhenEmptyRefreshToken() {
        // Given
        String emptyRefreshToken = "";
        
        // When & Then
        assertThatThrownBy(() -> keycloakTokenProvider.refreshToken(emptyRefreshToken))
            .isInstanceOf(TokenProviderException.class);
    }

    @Test
    @DisplayName("빈 공백 refresh 토큰으로 갱신 시 예외 발생")
    void shouldThrowExceptionWhenBlankRefreshToken() {
        // Given
        String blankRefreshToken = "   ";
        
        // When & Then
        assertThatThrownBy(() -> keycloakTokenProvider.refreshToken(blankRefreshToken))
            .isInstanceOf(TokenProviderException.class);
    }

    @Test
    @DisplayName("잘못된 설정으로 KeycloakTokenProvider 생성 시 예외 발생")
    void shouldThrowExceptionWhenInvalidConfiguration() {
        // Given
        HexacoreSecurityProperties.TokenProvider.KeycloakProperties invalidProperties = 
            new HexacoreSecurityProperties.TokenProvider.KeycloakProperties();
        // serverUrl, realm, clientId, clientSecret를 설정하지 않음
        
        // When & Then
        assertThatThrownBy(() -> new KeycloakTokenProvider(invalidProperties))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Invalid Keycloak configuration");
    }
}