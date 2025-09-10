package com.ldx.hexacore.security.logging;

import com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.ldx.hexacore.security.config.properties.HexacoreSecurityProperties;
import com.ldx.hexacore.security.auth.adapter.inbound.config.SecurityProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Security Starter 시작 시 설정 및 상태를 로깅하는 컴포넌트
 * 
 * 부모 프로젝트에서 Security Starter가 어떻게 초기화되었는지
 * 한눈에 파악할 수 있도록 체계적으로 로그를 출력합니다.
 */
@Component
@Order(1) // 가장 먼저 실행
public class SecurityStartupLogger implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger("SECURITY.STARTUP");
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private Environment env;
    
    @Autowired(required = false)
    private HexacoreSecurityProperties hexacoreProperties;
    
    @Autowired(required = false)
    private SecurityProperties securityProperties;
    
    @Override
    public void run(String... args) {
        logStartupBanner();
        logSecurityMode();
        logTokenProvider();
        logAuthenticationSettings();
        logFilterConfiguration();
        logEndpointProtection();
        logKeycloakConfiguration();
        logSecurityFilterChains();
        logHealthCheck();
        logStartupComplete();
    }
    
    private void logStartupBanner() {
        logger.info("");
        logger.info("╔═══════════════════════════════════════════════════════════════════════╗");
        logger.info("║                    🔐 HEXACORE SECURITY STARTER                      ║");
        logger.info("║                           Version 1.2.0                              ║");
        logger.info("╚═══════════════════════════════════════════════════════════════════════╝");
        logger.info("");
    }
    
    private void logSecurityMode() {
        logger.info("┌─── 🎯 Security Mode ───────────────────────────────────────────────┐");
        
        boolean securityEnabled = env.getProperty("hexacore.security.enabled", Boolean.class, true);
        String provider = env.getProperty("hexacore.security.token-provider.provider", "jwt");
        
        logger.info("│ Security Enabled: {}", securityEnabled ? "✅ YES" : "❌ NO");
        logger.info("│ Token Provider: {} {}", 
            provider.toUpperCase(),
            provider.equals("keycloak") ? "🔑" : "🎫");
        
        logger.info("└────────────────────────────────────────────────────────────────────┘");
    }
    
    private void logTokenProvider() {
        logger.info("┌─── 🔑 Token Provider Details ──────────────────────────────────────┐");
        
        try {
            Map<String, TokenProvider> providers = applicationContext.getBeansOfType(TokenProvider.class);
            if (providers.isEmpty()) {
                logger.warn("│ ⚠️  No TokenProvider found!");
            } else {
                providers.forEach((name, provider) -> {
                    logger.info("│ Bean Name: {}", name);
                    logger.info("│ Implementation: {}", provider.getClass().getSimpleName());
                    logger.info("│ Provider Type: {}", provider.getProviderType());
                });
            }
        } catch (Exception e) {
            logger.error("│ ❌ Error checking TokenProvider: {}", e.getMessage());
        }
        
        logger.info("└────────────────────────────────────────────────────────────────────┘");
    }
    
    private void logAuthenticationSettings() {
        logger.info("┌─── 🛡️  Authentication Settings ────────────────────────────────────┐");
        
        if (securityProperties != null && securityProperties.getAuthentication() != null) {
            var auth = securityProperties.getAuthentication();
            
            // 이것이 핵심! 리소스 권한 체크 설정
            boolean resourceCheck = auth.isCheckResourcePermission();
            logger.info("│ 🎯 Resource Permission Check: {} {}", 
                resourceCheck ? "ENABLED" : "DISABLED",
                resourceCheck ? "✅" : "❌");
            
            if (resourceCheck) {
                logger.info("│    └─ Keycloak UMA 2.0 authorization is ACTIVE");
                logger.info("│    └─ Each request will be validated against Keycloak resources");
            } else {
                logger.info("│    └─ Only basic token validation (no resource check)");
            }
            
            logger.info("│ Default Role: {}", auth.getDefaultRole());
            
            var errorResponse = auth.getErrorResponse();
            logger.info("│ Error Response Config:");
            logger.info("│    ├─ Include Timestamp: {}", errorResponse.isIncludeTimestamp());
            logger.info("│    ├─ Include Status: {}", errorResponse.isIncludeStatus());
            logger.info("│    └─ Default Message: {}", errorResponse.getDefaultMessage());
        } else {
            logger.warn("│ ⚠️  SecurityProperties not configured!");
            logger.warn("│     Resource permission check will be DISABLED");
        }
        
        logger.info("└────────────────────────────────────────────────────────────────────┘");
    }
    
    private void logFilterConfiguration() {
        logger.info("┌─── 🔍 Filter Configuration ────────────────────────────────────────┐");
        
        boolean filterEnabled = env.getProperty("hexacore.security.filter.enabled", Boolean.class, true);
        logger.info("│ JWT Filter: {}", filterEnabled ? "✅ ENABLED" : "❌ DISABLED");
        
        if (hexacoreProperties != null) {
            String[] excludePaths = hexacoreProperties.getFilter().getExcludePaths();
            if (excludePaths != null && excludePaths.length > 0) {
                logger.info("│ Excluded Paths (no authentication required):");
                for (String path : excludePaths) {
                    logger.info("│    ├─ {}", path);
                }
            }
        }
        
        logger.info("└────────────────────────────────────────────────────────────────────┘");
    }
    
    private void logEndpointProtection() {
        logger.info("┌─── 🚦 Endpoint Protection Status ──────────────────────────────────┐");
        
        boolean resourceCheck = securityProperties != null && 
            securityProperties.getAuthentication() != null &&
            securityProperties.getAuthentication().isCheckResourcePermission();
        
        if (resourceCheck) {
            logger.info("│ ✅ Keycloak Resource-based Authorization ACTIVE");
            logger.info("│");
            logger.info("│ Protected Endpoints:");
            logger.info("│    ├─ /api/employees/group/indonesia → Only 'indonesia' resource");
            logger.info("│    ├─ /api/users → Requires 'users' resource");
            logger.info("│    ├─ /api/admin → Requires 'admin' resource");
            logger.info("│    └─ All others → Denied by default");
        } else {
            logger.info("│ ⚠️  Basic Token Validation Only (No Resource Check)");
            logger.info("│    └─ All authenticated users can access any endpoint");
        }
        
        logger.info("└────────────────────────────────────────────────────────────────────┘");
    }
    
    private void logKeycloakConfiguration() {
        String provider = env.getProperty("hexacore.security.token-provider.provider");
        if (!"keycloak".equals(provider)) {
            return;
        }
        
        logger.info("┌─── 🔐 Keycloak Configuration ──────────────────────────────────────┐");
        
        if (hexacoreProperties != null && hexacoreProperties.getTokenProvider() != null) {
            var keycloak = hexacoreProperties.getTokenProvider().getKeycloak();
            if (keycloak != null) {
                logger.info("│ Server URL: {}", maskSensitiveInfo(keycloak.getServerUrl()));
                logger.info("│ Realm: {}", keycloak.getRealm());
                logger.info("│ Client ID: {}", keycloak.getClientId());
                logger.info("│ Client Secret: {}", maskSecret(keycloak.getClientSecret()));
                logger.info("│ Grant Type: {}", keycloak.getGrantType());
                logger.info("│ Scopes: {}", keycloak.getScopes());
                logger.info("│ Public Client: {}", keycloak.getPublicClient());
                
                // Endpoint URLs
                String baseUrl = keycloak.getServerUrl() + "realms/" + keycloak.getRealm();
                logger.info("│");
                logger.info("│ Endpoints:");
                logger.info("│    ├─ Token: {}/protocol/openid-connect/token", baseUrl);
                logger.info("│    ├─ UserInfo: {}/protocol/openid-connect/userinfo", baseUrl);
                logger.info("│    └─ Introspection: {}/protocol/openid-connect/token/introspect", baseUrl);
            }
        }
        
        logger.info("└────────────────────────────────────────────────────────────────────┘");
    }
    
    private void logSecurityFilterChains() {
        logger.info("┌─── ⛓️  Security Filter Chains ─────────────────────────────────────┐");
        
        Map<String, SecurityFilterChain> chains = applicationContext.getBeansOfType(SecurityFilterChain.class);
        if (chains.isEmpty()) {
            logger.info("│ No SecurityFilterChain beans found");
        } else {
            chains.forEach((name, chain) -> {
                logger.info("│ Chain: {} ({})", name, chain.getClass().getSimpleName());
            });
        }
        
        logger.info("└────────────────────────────────────────────────────────────────────┘");
    }
    
    private void logHealthCheck() {
        logger.info("┌─── ✅ Health Check ────────────────────────────────────────────────┐");
        
        boolean allGood = true;
        
        // Check TokenProvider
        boolean hasTokenProvider = !applicationContext.getBeansOfType(TokenProvider.class).isEmpty();
        logger.info("│ TokenProvider: {}", hasTokenProvider ? "✅ OK" : "❌ MISSING");
        allGood &= hasTokenProvider;
        
        // Check SecurityProperties
        boolean hasSecurityProps = securityProperties != null;
        logger.info("│ SecurityProperties: {}", hasSecurityProps ? "✅ OK" : "❌ MISSING");
        allGood &= hasSecurityProps;
        
        // Check Resource Permission
        boolean resourceCheckEnabled = hasSecurityProps && 
            securityProperties.getAuthentication() != null &&
            securityProperties.getAuthentication().isCheckResourcePermission();
        logger.info("│ Resource Permission Check: {}", 
            resourceCheckEnabled ? "✅ ENABLED" : "⚠️  DISABLED");
        
        // Overall status
        logger.info("│");
        if (allGood && resourceCheckEnabled) {
            logger.info("│ 🎉 Overall Status: FULLY OPERATIONAL");
        } else if (allGood) {
            logger.info("│ ⚠️  Overall Status: BASIC MODE (No Resource Check)");
        } else {
            logger.info("│ ❌ Overall Status: CONFIGURATION ISSUES DETECTED");
        }
        
        logger.info("└────────────────────────────────────────────────────────────────────┘");
    }
    
    private void logStartupComplete() {
        logger.info("");
        logger.info("╔═══════════════════════════════════════════════════════════════════════╗");
        logger.info("║          🚀 SECURITY STARTER INITIALIZATION COMPLETE                 ║");
        logger.info("╚═══════════════════════════════════════════════════════════════════════╝");
        logger.info("");
    }
    
    private String maskSensitiveInfo(String value) {
        if (value == null || value.length() < 10) {
            return value;
        }
        return value.substring(0, 10) + "****";
    }
    
    private String maskSecret(String secret) {
        if (secret == null || secret.isEmpty()) {
            return "NOT_SET";
        }
        return secret.substring(0, Math.min(4, secret.length())) + "****";
    }
}