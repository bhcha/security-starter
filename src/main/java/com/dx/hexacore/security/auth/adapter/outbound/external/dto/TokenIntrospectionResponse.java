package com.dx.hexacore.security.auth.adapter.outbound.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Keycloak 토큰 검증(introspection) 응답
 */
public class TokenIntrospectionResponse {
    
    @JsonProperty("active")
    private boolean active;
    
    @JsonProperty("exp")
    private Long exp;
    
    @JsonProperty("iat")
    private Long iat;
    
    @JsonProperty("sub")
    private String subject;
    
    @JsonProperty("username")
    private String username;
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public Long getExp() {
        return exp;
    }
    
    public void setExp(Long exp) {
        this.exp = exp;
    }
    
    public Long getIat() {
        return iat;
    }
    
    public void setIat(Long iat) {
        this.iat = iat;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
}