package com.dx.hexacore.security.auth.adapter.outbound.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Keycloak 토큰 응답 DTO.
 * Keycloak의 token endpoint 응답을 매핑합니다.
 * 
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
public class KeycloakTokenResponse {
    
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("refresh_token")
    private String refreshToken;
    
    @JsonProperty("expires_in")
    private Long expiresIn;
    
    @JsonProperty("refresh_expires_in")
    private Long refreshExpiresIn;
    
    @JsonProperty("token_type")
    private String tokenType;
    
    @JsonProperty("not-before-policy")
    private Integer notBeforePolicy;
    
    @JsonProperty("session_state")
    private String sessionState;
    
    @JsonProperty("scope")
    private String scope;
    
    /**
     * 유효한 응답인지 확인합니다.
     * 
     * @return 필수 필드가 모두 있으면 true
     */
    public boolean isValid() {
        return accessToken != null && !accessToken.isBlank() &&
               refreshToken != null && !refreshToken.isBlank() &&
               expiresIn != null && expiresIn > 0;
    }
}