package com.ldx.hexacore.security.auth.adapter.outbound.token.keycloak;

import com.ldx.hexacore.security.auth.adapter.outbound.external.dto.KeycloakTokenResponse;
import com.ldx.hexacore.security.auth.adapter.outbound.external.dto.TokenIntrospectionResponse;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenProviderException;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenProviderType;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenValidationContext;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenValidationResult;
import com.ldx.hexacore.security.config.properties.SecurityStarterProperties;
import com.ldx.hexacore.security.auth.domain.vo.Credentials;
import com.ldx.hexacore.security.auth.domain.vo.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class KeycloakTokenProvider implements TokenProvider {
    
    private static final String REFRESH_TOKEN_GRANT_TYPE = "refresh_token";
    
    private final KeycloakProperties properties;
    private final RestTemplate restTemplate;
    private final KeycloakAuthorizationService authorizationService;
    
    public KeycloakTokenProvider(SecurityStarterProperties.TokenProvider.KeycloakProperties configProperties) {
        this.properties = convertToKeycloakProperties(configProperties);
        this.restTemplate = createRestTemplate();
        this.authorizationService = new KeycloakAuthorizationService(properties);
        
        if (!properties.isValid()) {
            throw new IllegalStateException("Invalid Keycloak configuration. Please check your properties.");
        }
    }
    
    
    @Override
    public Token issueToken(Credentials credentials) throws TokenProviderException {
        if (credentials == null) {
            throw TokenProviderException.invalidCredentials("KEYCLOAK");
        }
        
        log.debug("Issuing token with Keycloak for user: {}", credentials.getUsername());
        
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", properties.getGrantType());
            formData.add("client_id", properties.getClientId());
            if (!properties.isPublicClient()) {
                formData.add("client_secret", properties.getClientSecret());
            }
            formData.add("username", credentials.getUsername());
            formData.add("password", credentials.getPassword());
            // Add OAuth2 scopes to enable userinfo endpoint access
            formData.add("scope", properties.getScopes());
            
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
                throw TokenProviderException.tokenIssueFailed("KEYCLOAK", 
                    new RuntimeException("Invalid token response from Keycloak"));
            }
            
            log.info("Successfully issued token with Keycloak for user: {}", credentials.getUsername());
            
            return Token.of(
                    tokenResponse.getAccessToken(),
                    tokenResponse.getRefreshToken(),
                    tokenResponse.getExpiresIn()
            );
            
        } catch (HttpClientErrorException e) {
            log.error("Token issuance failed for user: {}", credentials.getUsername(), e);
            
            throw TokenProviderException.tokenIssueFailed("KEYCLOAK", e);
        } catch (Exception e) {
            log.error("Unexpected error during token issuance", e);
            throw TokenProviderException.providerUnavailable("KEYCLOAK", e);
        }
    }
    
    @Override
    public TokenValidationResult validateToken(String accessToken) throws TokenProviderException {
        if (accessToken == null || accessToken.isBlank()) {
            return TokenValidationResult.invalid("Token is null or blank");
        }
        
        log.debug("Validating token with Keycloak");
        
        // Try userinfo endpoint first (requires openid scope)
        try {
            return validateTokenWithUserInfo(accessToken);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                log.debug("Userinfo endpoint returned 403 FORBIDDEN. Falling back to introspection endpoint.");
                // Fall back to introspection endpoint
                return validateTokenWithIntrospection(accessToken);
            }
            // For other HTTP errors, handle as before
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return new TokenValidationResult(false, null, null, null, null, Collections.emptyMap());
            }
            throw TokenProviderException.tokenValidationFailed("KEYCLOAK", e);
        } catch (Exception e) {
            log.error("Unexpected error during token validation", e);
            throw TokenProviderException.providerUnavailable("KEYCLOAK", e);
        }
    }
    
    private TokenValidationResult validateTokenWithUserInfo(String accessToken) throws HttpClientErrorException {
        log.debug("Validating token using userinfo endpoint");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(
                properties.getUserInfoEndpoint(),
                HttpMethod.GET,
                request,
                Map.class
        );
        
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> userInfo = response.getBody();
            
            log.debug("Token validation successful with userinfo endpoint");
            
            return new TokenValidationResult(
                true,
                (String) userInfo.get("sub"),
                (String) userInfo.get("preferred_username"),
                Collections.emptySet(),
                null,
                userInfo
            );
        }
        
        return new TokenValidationResult(false, null, null, null, null, Collections.emptyMap());
    }
    
    private TokenValidationResult validateTokenWithIntrospection(String accessToken) {
        log.debug("Validating token using introspection endpoint");
        
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("token", accessToken);
            formData.add("client_id", properties.getClientId());
            if (!properties.isPublicClient()) {
                formData.add("client_secret", properties.getClientSecret());
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
            
            ResponseEntity<TokenIntrospectionResponse> response = restTemplate.postForEntity(
                    properties.getIntrospectionEndpoint(),
                    request,
                    TokenIntrospectionResponse.class
            );
            
            TokenIntrospectionResponse introspectionResult = response.getBody();
            if (introspectionResult != null && introspectionResult.isActive()) {
                log.debug("Token validation successful with introspection endpoint");
                
                return new TokenValidationResult(
                    true,
                    introspectionResult.getSubject(),
                    introspectionResult.getUsername(),
                    Collections.emptySet(),
                    null,
                    Collections.singletonMap("introspection", "used")
                );
            }
            
            log.debug("Token validation failed - token is not active");
            return new TokenValidationResult(false, null, null, null, null, Collections.emptyMap());
            
        } catch (HttpClientErrorException e) {
            log.debug("Introspection endpoint validation failed: {} - {}", e.getStatusCode(), e.getMessage());
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return new TokenValidationResult(false, null, null, null, null, Collections.emptyMap());
            }
            throw TokenProviderException.tokenValidationFailed("KEYCLOAK", e);
        } catch (Exception e) {
            log.error("Unexpected error during introspection validation", e);
            throw TokenProviderException.providerUnavailable("KEYCLOAK", e);
        }
    }
    
    @Override
    public Token refreshToken(String refreshToken) throws TokenProviderException {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw TokenProviderException.tokenExpired("KEYCLOAK");
        }
        
        log.debug("Refreshing token with Keycloak");
        
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", REFRESH_TOKEN_GRANT_TYPE);
            formData.add("client_id", properties.getClientId());
            if (!properties.isPublicClient()) {
                formData.add("client_secret", properties.getClientSecret());
            }
            formData.add("refresh_token", refreshToken);
            // Add OAuth2 scopes to ensure refreshed token has same scopes
            formData.add("scope", properties.getScopes());
            
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
                throw TokenProviderException.tokenRefreshFailed("KEYCLOAK", 
                    new RuntimeException("Invalid token response from Keycloak"));
            }
            
            log.info("Successfully refreshed token with Keycloak");
            
            return Token.of(
                    tokenResponse.getAccessToken(),
                    tokenResponse.getRefreshToken(),
                    tokenResponse.getExpiresIn()
            );
            
        } catch (HttpClientErrorException e) {
            log.error("Token refresh failed", e);
            
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw TokenProviderException.tokenExpired("KEYCLOAK");
            }
            
            throw TokenProviderException.tokenRefreshFailed("KEYCLOAK", e);
        } catch (Exception e) {
            log.error("Unexpected error during token refresh", e);
            throw TokenProviderException.providerUnavailable("KEYCLOAK", e);
        }
    }
    
    @Override
    public TokenProviderType getProviderType() {
        return TokenProviderType.KEYCLOAK;
    }
    
    @Override
    public TokenValidationResult validateTokenWithContext(String accessToken, TokenValidationContext context) 
            throws TokenProviderException {
        
        // 컨텍스트 정보 상세 로깅
        if (context != null) {
            log.info("=== Token Validation Context ===");
            log.info("Request URI: {}", context.getRequestUri());
            log.info("HTTP Method: {}", context.getHttpMethod());
            log.info("Client IP: {}", context.getClientIp());
            log.info("User-Agent: {}", context.getUserAgent());
            log.info("Resource Permission Check: {}", context.isCheckResourcePermission());
            log.info("================================");
        }
        
        // 먼저 기본 토큰 검증 수행
        TokenValidationResult basicValidation = validateToken(accessToken);
        
        // 토큰이 유효하지 않으면 바로 반환
        if (!basicValidation.valid()) {
            log.warn("Basic token validation failed");
            return basicValidation;
        }
        
        log.info("Basic token validation successful for user: {}", basicValidation.username());
        
        // 리소스 권한 체크가 활성화되어 있고 컨텍스트가 있는 경우
        if (context != null && context.isCheckResourcePermission()) {
            log.info("Starting UMA resource permission check for URI: {} with method: {}", 
                context.getRequestUri(), context.getHttpMethod());
            
            // UMA 권한 체크 수행
            boolean hasPermission = checkUMAPermission(accessToken, context);
            
            if (!hasPermission) {
                log.warn("❌ Resource permission DENIED for URI: {} with method: {}", 
                    context.getRequestUri(), context.getHttpMethod());
                
                // 권한이 없는 경우 검증 실패로 처리
                Map<String, Object> claims = new HashMap<>(basicValidation.claims());
                claims.put("resource_permission_denied", true);
                claims.put("requested_uri", context.getRequestUri());
                claims.put("requested_method", context.getHttpMethod());
                
                return new TokenValidationResult(
                    false,
                    basicValidation.userId(),
                    basicValidation.username(),
                    basicValidation.authorities(),
                    basicValidation.expiresAt(),
                    claims
                );
            }
            
            // 권한이 있는 경우 컨텍스트 정보를 추가하여 반환
            log.info("✅ Resource permission GRANTED for URI: {} with method: {}", 
                context.getRequestUri(), context.getHttpMethod());
            
            Map<String, Object> claims = new HashMap<>(basicValidation.claims());
            claims.put("resource_permission_granted", true);
            claims.put("requested_uri", context.getRequestUri());
            claims.put("requested_method", context.getHttpMethod());
            
            return new TokenValidationResult(
                true,
                basicValidation.userId(),
                basicValidation.username(),
                basicValidation.authorities(),
                basicValidation.expiresAt(),
                claims
            );
        }
        
        // 리소스 권한 체크가 필요 없는 경우 기본 검증 결과 반환
        return basicValidation;
    }
    
    /**
     * Keycloak Admin Client Authorization을 사용하여 리소스 권한을 체크합니다.
     * 
     * <p>하드코딩 없이 Keycloak에 설정된 리소스와 URI를 직접 매칭하여 권한을 판단합니다.</p>
     */
    private boolean checkUMAPermission(String accessToken, TokenValidationContext context) {
        try {
            String requestUri = context.getRequestUri();
            String httpMethod = context.getHttpMethod();
            
            log.info("🔍 Admin Client 기반 리소스 권한 체크 시작: {} {}", httpMethod, requestUri);
            
            // 팀 Keycloak Client를 사용한 직접 authorization 체크
            return authorizationService.checkAuthorization(accessToken, requestUri, httpMethod);
            
        } catch (Exception e) {
            log.error("Unexpected error during team keycloak-client authorization check for URI: {} method: {}", 
                context.getRequestUri(), context.getHttpMethod(), e);
            return false;
        }
    }
    
    /**
     * 팀 Keycloak Client 기반 권한 검증 완료
     * 
     * <p>기존 UMA 2.0 수동 구현을 제거하고 팀 라이브러리를 사용합니다.</p>
     * <p>하드코딩 없이 Keycloak이 직접 엔드포인트 권한을 검증합니다.</p>
     */
    
    
    private KeycloakProperties convertToKeycloakProperties(SecurityStarterProperties.TokenProvider.KeycloakProperties configProperties) {
        KeycloakProperties properties = new KeycloakProperties();
        properties.setServerUrl(configProperties.getServerUrl());
        properties.setRealm(configProperties.getRealm());
        properties.setClientId(configProperties.getClientId());
        properties.setClientSecret(configProperties.getClientSecret());
        properties.setGrantType(configProperties.getGrantType());
        properties.setScopes(configProperties.getScopes());
        properties.setPublicClient(configProperties.getPublicClient());
        return properties;
    }
    
    private RestTemplate createRestTemplate() {
        return new RestTemplate();
    }
}