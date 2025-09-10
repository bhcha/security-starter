package com.ldx.hexacore.security.auth.adapter.inbound.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 보안 관련 설정 프로퍼티
 */
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
        private List<String> excludePaths = List.of(); // 설정 파일에서 지정해야 함 (레거시)
        private Exclude exclude = new Exclude(); // 새로운 nested 구조
        
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
        
        public Exclude getExclude() {
            return exclude;
        }
        
        public void setExclude(Exclude exclude) {
            this.exclude = exclude;
        }
        
        public static class Exclude {
            private String[] paths = {}; // 설정 파일에서 지정해야 함
            
            public String[] getPaths() {
                return paths;
            }
            
            public void setPaths(String[] paths) {
                this.paths = paths;
            }
        }
    }
    
    public static class Authentication {
        private String defaultRole = "ROLE_USER";
        private ErrorResponse errorResponse = new ErrorResponse();
        private boolean checkResourcePermission = false;
        
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
        
        public boolean isCheckResourcePermission() {
            return checkResourcePermission;
        }
        
        public void setCheckResourcePermission(boolean checkResourcePermission) {
            this.checkResourcePermission = checkResourcePermission;
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