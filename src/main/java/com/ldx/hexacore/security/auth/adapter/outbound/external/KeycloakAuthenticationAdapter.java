package com.ldx.hexacore.security.auth.adapter.outbound.external;

import com.ldx.hexacore.security.auth.adapter.outbound.external.dto.KeycloakErrorResponse;
import com.ldx.hexacore.security.auth.adapter.outbound.external.dto.KeycloakTokenResponse;
import com.ldx.hexacore.security.auth.adapter.outbound.external.dto.TokenIntrospectionResponse;
import com.ldx.hexacore.security.auth.application.command.port.out.ExternalAuthException;
import com.ldx.hexacore.security.auth.application.command.port.out.ExternalAuthProvider;
import com.ldx.hexacore.security.auth.domain.vo.Credentials;
import com.ldx.hexacore.security.auth.domain.vo.Token;
import com.ldx.hexacore.security.util.ValidationMessages;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Keycloak 인증 어댑터.
 * Keycloak 서버와 통신하여 인증 및 토큰 관리를 수행합니다.
 * 
 * @since 1.0.0
 */
@Slf4j
@ConditionalOnProperty(prefix = "security.auth.keycloak", name = "enabled", havingValue = "true", matchIfMissing = false)
class KeycloakAuthenticationAdapter implements ExternalAuthProvider {
    
    private static final String REFRESH_TOKEN_GRANT_TYPE = "refresh_token";
    
    private final KeycloakProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public KeycloakAuthenticationAdapter(KeycloakProperties properties, 
                                        @Qualifier("keycloakRestTemplate") RestTemplate restTemplate, 
                                        ObjectMapper objectMapper) {
        this.properties = properties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        
        if (!properties.isValid()) {
            throw new IllegalStateException("Invalid Keycloak configuration. Please check your properties.");
        }
    }
    
    @Override
    public Token authenticate(Credentials credentials) throws ExternalAuthException {
        if (credentials == null) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNull("Credentials"));
        }
        
        log.debug("Authenticating user with Keycloak");
        
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", properties.getGrantType());
            formData.add("client_id", properties.getClientId());
            formData.add("client_secret", properties.getClientSecret());
            formData.add("username", credentials.getUsername());
            formData.add("password", credentials.getPassword());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
            
            ResponseEntity<KeycloakTokenResponse> response = restTemplate.postForEntity(
                    properties.getTokenEndpoint(),
                    request,
                    KeycloakTokenResponse.class
            );
            
            KeycloakTokenResponse tokenResponse = response.getBody();
            if (tokenResponse == null || !tokenResponse.isValid()) {
                throw new ExternalAuthException("Invalid token response from Keycloak");
            }
            
            log.info("Successfully authenticated user with Keycloak");
            
            return Token.of(
                    tokenResponse.getAccessToken(),
                    tokenResponse.getRefreshToken(),
                    tokenResponse.getExpiresIn()
            );
            
        } catch (HttpClientErrorException e) {
            log.error("Authentication failed", e);
            String errorMessage = parseErrorResponse(e);
            throw new ExternalAuthException("Authentication failed: " + errorMessage, e);
        } catch (Exception e) {
            log.error("Unexpected error during authentication", e);
            throw new ExternalAuthException("Failed to communicate with Keycloak", e);
        }
    }
    
    @Override
    public Token refreshToken(String refreshToken) throws ExternalAuthException {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNullOrEmpty("Refresh token"));
        }
        
        log.debug("Refreshing token with Keycloak");
        
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", REFRESH_TOKEN_GRANT_TYPE);
            formData.add("client_id", properties.getClientId());
            formData.add("client_secret", properties.getClientSecret());
            formData.add("refresh_token", refreshToken);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
            
            ResponseEntity<KeycloakTokenResponse> response = restTemplate.postForEntity(
                    properties.getTokenEndpoint(),
                    request,
                    KeycloakTokenResponse.class
            );
            
            KeycloakTokenResponse tokenResponse = response.getBody();
            if (tokenResponse == null || !tokenResponse.isValid()) {
                throw new ExternalAuthException("Invalid token response from Keycloak");
            }
            
            log.info("Successfully refreshed token with Keycloak");
            
            return Token.of(
                    tokenResponse.getAccessToken(),
                    tokenResponse.getRefreshToken(),
                    tokenResponse.getExpiresIn()
            );
            
        } catch (HttpClientErrorException e) {
            log.error("Token refresh failed", e);
            String errorMessage = parseErrorResponse(e);
            throw new ExternalAuthException("Token refresh failed: " + errorMessage, e);
        } catch (Exception e) {
            log.error("Unexpected error during token refresh", e);
            throw new ExternalAuthException("Failed to communicate with Keycloak", e);
        }
    }
    
    @Override
    public boolean validateToken(String accessToken) throws ExternalAuthException {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNullOrEmpty("Access token"));
        }
        
        log.debug("Validating token with Keycloak");
        
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("token", accessToken);
            formData.add("client_id", properties.getClientId());
            formData.add("client_secret", properties.getClientSecret());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
            
            ResponseEntity<TokenIntrospectionResponse> response = restTemplate.postForEntity(
                    properties.getIntrospectionEndpoint(),
                    request,
                    TokenIntrospectionResponse.class
            );
            
            TokenIntrospectionResponse introspectionResponse = response.getBody();
            boolean isValid = introspectionResponse != null && introspectionResponse.isActive();
            
            log.debug("Token validation result: {}", isValid);
            
            return isValid;
            
        } catch (HttpClientErrorException e) {
            log.error("Token validation failed", e);
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return false;
            }
            throw new ExternalAuthException("Failed to validate token", e);
        } catch (Exception e) {
            log.error("Unexpected error during token validation", e);
            throw new ExternalAuthException("Failed to communicate with Keycloak", e);
        }
    }
    
    private String parseErrorResponse(HttpClientErrorException e) {
        try {
            String responseBody = e.getResponseBodyAsString();
            KeycloakErrorResponse errorResponse = objectMapper.readValue(responseBody, KeycloakErrorResponse.class);
            return errorResponse.getFormattedError();
        } catch (Exception parseException) {
            log.warn("Failed to parse error response", parseException);
            return e.getMessage();
        }
    }
}