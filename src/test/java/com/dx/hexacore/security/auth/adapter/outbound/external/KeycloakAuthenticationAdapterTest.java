package com.dx.hexacore.security.auth.adapter.outbound.external;

import com.dx.hexacore.security.auth.adapter.outbound.external.KeycloakAuthenticationAdapter;
import com.dx.hexacore.security.auth.adapter.outbound.external.KeycloakProperties;
import com.dx.hexacore.security.auth.adapter.outbound.external.dto.KeycloakErrorResponse;
import com.dx.hexacore.security.auth.adapter.outbound.external.dto.KeycloakTokenResponse;
import com.dx.hexacore.security.auth.adapter.outbound.external.dto.TokenIntrospectionResponse;
import com.dx.hexacore.security.auth.application.command.port.out.ExternalAuthException;
import com.dx.hexacore.security.auth.domain.vo.Credentials;
import com.dx.hexacore.security.auth.domain.vo.Token;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class KeycloakAuthenticationAdapterTest {
    
    @Mock
    private KeycloakProperties properties;
    
    @Mock
    private RestTemplate restTemplate;
    
    @Mock
    private ObjectMapper objectMapper;
    
    private KeycloakAuthenticationAdapter adapter;
    
    @BeforeEach
    void setUp() {
        // 기본 설정값은 각 테스트에서 필요한 경우에만 설정
    }
    
    private void setupValidProperties() {
        when(properties.isValid()).thenReturn(true);
        when(properties.getGrantType()).thenReturn("password");
        when(properties.getClientId()).thenReturn("test-client");
        when(properties.getClientSecret()).thenReturn("test-secret");
        when(properties.getTokenEndpoint()).thenReturn("http://localhost:8080/token");
        when(properties.getIntrospectionEndpoint()).thenReturn("http://localhost:8080/introspect");
    }
    
    @Test
    @DisplayName("유효한 자격증명으로 인증 - 성공")
    void authenticate_ValidCredentials() throws Exception {
        // Given
        setupValidProperties();
        adapter = new KeycloakAuthenticationAdapter(properties, restTemplate, objectMapper);
        
        Credentials credentials = Credentials.of("testuser", "testpass");
        
        KeycloakTokenResponse tokenResponse = new KeycloakTokenResponse();
        tokenResponse.setAccessToken("test-access-token");
        tokenResponse.setRefreshToken("test-refresh-token");
        tokenResponse.setExpiresIn(3600L);
        
        ResponseEntity<KeycloakTokenResponse> responseEntity = ResponseEntity.ok(tokenResponse);
        
        when(restTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(KeycloakTokenResponse.class)
        )).thenReturn(responseEntity);
        
        // When
        Token result = adapter.authenticate(credentials);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("test-access-token");
        assertThat(result.getRefreshToken()).isEqualTo("test-refresh-token");
        assertThat(result.getExpiresIn()).isEqualTo(3600L);
        
        verify(restTemplate).postForEntity(
                eq("http://localhost:8080/token"),
                any(HttpEntity.class),
                eq(KeycloakTokenResponse.class)
        );
    }
    
    @Test
    @DisplayName("잘못된 자격증명으로 인증 - 실패")
    void authenticate_InvalidCredentials() throws Exception {
        // Given
        setupValidProperties();
        adapter = new KeycloakAuthenticationAdapter(properties, restTemplate, objectMapper);
        
        Credentials credentials = Credentials.of("testuser", "wrongpass");
        
        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                HttpHeaders.EMPTY,
                "{\"error\":\"invalid_grant\",\"error_description\":\"Invalid user credentials\"}".getBytes(),
                null
        );
        
        when(restTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(KeycloakTokenResponse.class)
        )).thenThrow(exception);
        
        KeycloakErrorResponse errorResponse = new KeycloakErrorResponse();
        errorResponse.setError("invalid_grant");
        errorResponse.setErrorDescription("Invalid user credentials");
        
        when(objectMapper.readValue(anyString(), eq(KeycloakErrorResponse.class)))
                .thenReturn(errorResponse);
        
        // When & Then
        assertThatThrownBy(() -> adapter.authenticate(credentials))
                .isInstanceOf(ExternalAuthException.class)
                .hasMessageContaining("Authentication failed");
    }
    
    @Test
    @DisplayName("null 자격증명으로 인증 - 예외")
    void authenticate_NullCredentials() {
        // Given
        setupValidProperties();
        adapter = new KeycloakAuthenticationAdapter(properties, restTemplate, objectMapper);
        // When & Then
        assertThatThrownBy(() -> adapter.authenticate(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Credentials cannot be null");
        
        verifyNoInteractions(restTemplate);
    }
    
    @Test
    @DisplayName("리프레시 토큰으로 갱신 - 성공")
    void refreshToken_Success() throws Exception {
        // Given
        setupValidProperties();
        adapter = new KeycloakAuthenticationAdapter(properties, restTemplate, objectMapper);
        
        String refreshToken = "test-refresh-token";
        
        KeycloakTokenResponse tokenResponse = new KeycloakTokenResponse();
        tokenResponse.setAccessToken("new-access-token");
        tokenResponse.setRefreshToken("new-refresh-token");
        tokenResponse.setExpiresIn(3600L);
        
        ResponseEntity<KeycloakTokenResponse> responseEntity = ResponseEntity.ok(tokenResponse);
        
        when(restTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(KeycloakTokenResponse.class)
        )).thenReturn(responseEntity);
        
        // When
        Token result = adapter.refreshToken(refreshToken);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("new-access-token");
        assertThat(result.getRefreshToken()).isEqualTo("new-refresh-token");
    }
    
    @Test
    @DisplayName("만료된 리프레시 토큰으로 갱신 - 실패")
    void refreshToken_ExpiredToken() throws Exception {
        // Given
        setupValidProperties();
        adapter = new KeycloakAuthenticationAdapter(properties, restTemplate, objectMapper);
        
        String refreshToken = "expired-refresh-token";
        
        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                HttpHeaders.EMPTY,
                "{\"error\":\"invalid_grant\",\"error_description\":\"Token is not active\"}".getBytes(),
                null
        );
        
        when(restTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(KeycloakTokenResponse.class)
        )).thenThrow(exception);
        
        KeycloakErrorResponse errorResponse = new KeycloakErrorResponse();
        errorResponse.setError("invalid_grant");
        errorResponse.setErrorDescription("Token is not active");
        
        when(objectMapper.readValue(anyString(), eq(KeycloakErrorResponse.class)))
                .thenReturn(errorResponse);
        
        // When & Then
        assertThatThrownBy(() -> adapter.refreshToken(refreshToken))
                .isInstanceOf(ExternalAuthException.class)
                .hasMessageContaining("Token refresh failed");
    }
    
    @Test
    @DisplayName("유효한 토큰 검증 - true 반환")
    void validateToken_ValidToken() throws Exception {
        // Given
        setupValidProperties();
        adapter = new KeycloakAuthenticationAdapter(properties, restTemplate, objectMapper);
        
        String accessToken = "valid-access-token";
        
        TokenIntrospectionResponse introspectionResponse = new TokenIntrospectionResponse();
        introspectionResponse.setActive(true);
        introspectionResponse.setExp(1234567890L);
        
        ResponseEntity<TokenIntrospectionResponse> responseEntity = ResponseEntity.ok(introspectionResponse);
        
        when(restTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(TokenIntrospectionResponse.class)
        )).thenReturn(responseEntity);
        
        // When
        boolean result = adapter.validateToken(accessToken);
        
        // Then
        assertThat(result).isTrue();
    }
    
    @Test
    @DisplayName("만료된 토큰 검증 - false 반환")
    void validateToken_ExpiredToken() throws Exception {
        // Given
        setupValidProperties();
        adapter = new KeycloakAuthenticationAdapter(properties, restTemplate, objectMapper);
        
        String accessToken = "expired-access-token";
        
        TokenIntrospectionResponse introspectionResponse = new TokenIntrospectionResponse();
        introspectionResponse.setActive(false);
        
        ResponseEntity<TokenIntrospectionResponse> responseEntity = ResponseEntity.ok(introspectionResponse);
        
        when(restTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(TokenIntrospectionResponse.class)
        )).thenReturn(responseEntity);
        
        // When
        boolean result = adapter.validateToken(accessToken);
        
        // Then
        assertThat(result).isFalse();
    }
    
    @Test
    @DisplayName("Keycloak 서버 다운 - 통신 실패")
    void authenticate_ServerDown() {
        // Given
        setupValidProperties();
        adapter = new KeycloakAuthenticationAdapter(properties, restTemplate, objectMapper);
        
        Credentials credentials = Credentials.of("testuser", "testpass");
        
        when(restTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(KeycloakTokenResponse.class)
        )).thenThrow(new RuntimeException("Connection refused"));
        
        // When & Then
        assertThatThrownBy(() -> adapter.authenticate(credentials))
                .isInstanceOf(ExternalAuthException.class)
                .hasMessageContaining("Failed to communicate with Keycloak");
    }
    
    @Test
    @DisplayName("잘못된 설정으로 어댑터 생성 - 예외")
    void createAdapter_InvalidConfiguration() {
        // Given
        when(properties.isValid()).thenReturn(false);
        
        // When & Then
        assertThatThrownBy(() -> new KeycloakAuthenticationAdapter(properties, restTemplate, objectMapper))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid Keycloak configuration");
    }
}