package com.dx.hexacore.security.auth.adapter.outbound.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Keycloak 에러 응답 DTO.
 * Keycloak API 호출 실패 시 반환되는 에러 정보를 매핑합니다.
 * 
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
public class KeycloakErrorResponse {
    
    @JsonProperty("error")
    private String error;
    
    @JsonProperty("error_description")
    private String errorDescription;
    
    /**
     * 에러 메시지를 포맷팅하여 반환합니다.
     * 
     * @return 포맷팅된 에러 메시지
     */
    public String getFormattedError() {
        if (errorDescription != null && !errorDescription.isBlank()) {
            return String.format("%s: %s", error, errorDescription);
        }
        return error != null ? error : "Unknown error";
    }
}