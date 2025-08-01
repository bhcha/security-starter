package com.dx.hexacore.security.auth.adapter.inbound.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 보안 관련 설정 프로퍼티
 */
@Component
@ConfigurationProperties(prefix = "security.auth")
public class SecurityProperties {
    
    /**
     * JWT 관련 설정
     */
    private Jwt jwt = new Jwt();
    
    /**
     * 인증 관련 설정
     */
    private Authentication authentication = new Authentication();
    
    public Jwt getJwt() {
        return jwt;
    }
    
    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }
    
    public Authentication getAuthentication() {
        return authentication;
    }
    
    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }
    
    public static class Jwt {
        private boolean enabled = true;
        private List<String> excludePaths = List.of("/actuator/health", "/error", "/swagger-ui/**", "/v3/api-docs/**");
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public List<String> getExcludePaths() {
            return excludePaths;
        }
        
        public void setExcludePaths(List<String> excludePaths) {
            this.excludePaths = excludePaths;
        }
    }
    
    public static class Authentication {
        private String defaultRole = "ROLE_USER";
        private ErrorResponse errorResponse = new ErrorResponse();
        
        public String getDefaultRole() {
            return defaultRole;
        }
        
        public void setDefaultRole(String defaultRole) {
            this.defaultRole = defaultRole;
        }
        
        public ErrorResponse getErrorResponse() {
            return errorResponse;
        }
        
        public void setErrorResponse(ErrorResponse errorResponse) {
            this.errorResponse = errorResponse;
        }
        
        public static class ErrorResponse {
            private boolean includeTimestamp = true;
            private boolean includeStatus = true;
            private String defaultMessage = "Authentication failed";
            
            public boolean isIncludeTimestamp() {
                return includeTimestamp;
            }
            
            public void setIncludeTimestamp(boolean includeTimestamp) {
                this.includeTimestamp = includeTimestamp;
            }
            
            public boolean isIncludeStatus() {
                return includeStatus;
            }
            
            public void setIncludeStatus(boolean includeStatus) {
                this.includeStatus = includeStatus;
            }
            
            public String getDefaultMessage() {
                return defaultMessage;
            }
            
            public void setDefaultMessage(String defaultMessage) {
                this.defaultMessage = defaultMessage;
            }
        }
    }
}