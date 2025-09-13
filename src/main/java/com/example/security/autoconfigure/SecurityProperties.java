package com.example.security.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.starter")
public class SecurityProperties {
    
    private boolean enabled = true;
    private Mode mode = Mode.HEXAGONAL; // Mode 지원
    
    // Feature Toggle 패턴
    private Authentication auth = new Authentication();
    private Encryption encryption = new Encryption();
    private Audit audit = new Audit();
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public Mode getMode() {
        return mode;
    }
    
    public void setMode(Mode mode) {
        this.mode = mode;
    }
    
    public Authentication getAuth() {
        return auth;
    }
    
    public void setAuth(Authentication auth) {
        this.auth = auth;
    }
    
    public Encryption getEncryption() {
        return encryption;
    }
    
    public void setEncryption(Encryption encryption) {
        this.encryption = encryption;
    }
    
    public Audit getAudit() {
        return audit;
    }
    
    public void setAudit(Audit audit) {
        this.audit = audit;
    }
    
    // Mode enum
    public enum Mode {
        TRADITIONAL,
        HEXAGONAL
    }
    
    // Feature Toggle classes
    public static class Authentication {
        private boolean enabled = true;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
    
    public static class Encryption {
        private boolean enabled = false;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
    
    public static class Audit {
        private boolean enabled = false;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}