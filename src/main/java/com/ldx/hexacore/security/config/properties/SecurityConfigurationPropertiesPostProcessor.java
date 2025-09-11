package com.ldx.hexacore.security.config.properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * 설정 프로퍼티 경로의 호환성을 위한 Post Processor
 * 
 * 사용자가 다양한 프로퍼티 경로를 사용할 수 있도록 aliases를 제공합니다.
 * 
 * 지원하는 alias 경로:
 * - hexa.security.auth.provider -> security-starter.token-provider.provider
 * - hexa.security.token.provider -> security-starter.token-provider.provider
 * - security.auth.provider -> security-starter.token-provider.provider
 */
public class SecurityConfigurationPropertiesPostProcessor implements EnvironmentPostProcessor {
    
    private static final String PROPERTY_SOURCE_NAME = "securityPropertyAliases";
    
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> aliasProperties = new HashMap<>();
        
        // Provider 설정 aliases
        addProviderAliases(environment, aliasProperties);
        
        // JWT 설정 aliases
        addJwtAliases(environment, aliasProperties);
        
        // Keycloak 설정 aliases
        addKeycloakAliases(environment, aliasProperties);
        
        if (!aliasProperties.isEmpty()) {
            MapPropertySource propertySource = new MapPropertySource(PROPERTY_SOURCE_NAME, aliasProperties);
            environment.getPropertySources().addLast(propertySource);
        }
    }
    
    private void addProviderAliases(ConfigurableEnvironment environment, Map<String, Object> aliasProperties) {
        // Provider 설정 경로 aliases
        String[] providerAliases = {
            "hexa.security.auth.provider",
            "hexa.security.token.provider", 
            "security.auth.provider",
            "security.token.provider"
        };
        
        for (String alias : providerAliases) {
            String value = environment.getProperty(alias);
            if (value != null) {
                aliasProperties.put("security-starter.token-provider.provider", value);
                break; // 첫 번째로 찾은 값 사용
            }
        }
    }
    
    private void addJwtAliases(ConfigurableEnvironment environment, Map<String, Object> aliasProperties) {
        // JWT 설정 aliases
        addPropertyAlias(environment, aliasProperties, 
            new String[]{"hexa.security.jwt.secret", "security.jwt.secret"},
            "security-starter.token-provider.jwt.secret");
            
        addPropertyAlias(environment, aliasProperties,
            new String[]{"hexa.security.jwt.enabled", "security.jwt.enabled"},
            "security-starter.token-provider.jwt.enabled");
            
        addPropertyAlias(environment, aliasProperties,
            new String[]{"hexa.security.jwt.access-token-expiration", "security.jwt.access-token-expiration"},
            "security-starter.token-provider.jwt.access-token-expiration");
            
        addPropertyAlias(environment, aliasProperties,
            new String[]{"hexa.security.jwt.refresh-token-expiration", "security.jwt.refresh-token-expiration"},
            "security-starter.token-provider.jwt.refresh-token-expiration");
    }
    
    private void addKeycloakAliases(ConfigurableEnvironment environment, Map<String, Object> aliasProperties) {
        // Keycloak 설정 aliases
        addPropertyAlias(environment, aliasProperties,
            new String[]{"hexa.security.keycloak.server-url", "security.keycloak.server-url"},
            "security-starter.token-provider.keycloak.server-url");
            
        addPropertyAlias(environment, aliasProperties,
            new String[]{"hexa.security.keycloak.realm", "security.keycloak.realm"},
            "security-starter.token-provider.keycloak.realm");
            
        addPropertyAlias(environment, aliasProperties,
            new String[]{"hexa.security.keycloak.client-id", "security.keycloak.client-id"},
            "security-starter.token-provider.keycloak.client-id");
            
        addPropertyAlias(environment, aliasProperties,
            new String[]{"hexa.security.keycloak.client-secret", "security.keycloak.client-secret"},
            "security-starter.token-provider.keycloak.client-secret");
            
        addPropertyAlias(environment, aliasProperties,
            new String[]{"hexa.security.keycloak.enabled", "security.keycloak.enabled"},
            "security-starter.token-provider.keycloak.enabled");
    }
    
    private void addPropertyAlias(ConfigurableEnvironment environment, Map<String, Object> aliasProperties,
                                 String[] aliasKeys, String targetKey) {
        for (String alias : aliasKeys) {
            String value = environment.getProperty(alias);
            if (value != null) {
                aliasProperties.put(targetKey, value);
                break; // 첫 번째로 찾은 값 사용
            }
        }
    }
}