package com.dx.hexacore.security.config.autoconfigure;

import com.dx.hexacore.security.auth.adapter.outbound.token.jwt.SpringJwtTokenProvider;
import com.dx.hexacore.security.auth.adapter.outbound.token.keycloak.KeycloakTokenProvider;
import com.dx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.dx.hexacore.security.config.properties.HexacoreSecurityProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(HexacoreSecurityProperties.class)
@ConditionalOnClass(TokenProvider.class)
public class TokenProviderAutoConfiguration {
    
    @Bean
    @ConditionalOnProperty(
        prefix = "hexacore.security.token-provider", 
        name = "provider", 
        havingValue = "keycloak"
    )
    @ConditionalOnProperty(
        prefix = "hexacore.security.token-provider.keycloak", 
        name = "enabled", 
        havingValue = "true", 
        matchIfMissing = true
    )
    public TokenProvider keycloakTokenProvider(HexacoreSecurityProperties properties) {
        HexacoreSecurityProperties.TokenProvider.KeycloakProperties keycloakConfig = 
            properties.getTokenProvider().getKeycloak();
        
        return new KeycloakTokenProvider(keycloakConfig);
    }
    
    @Bean
    @ConditionalOnProperty(
        prefix = "hexacore.security.token-provider", 
        name = "provider", 
        havingValue = "jwt",
        matchIfMissing = true  // 기본값으로 JWT 제공자 사용
    )
    @ConditionalOnProperty(
        prefix = "hexacore.security.token-provider.jwt", 
        name = "enabled", 
        havingValue = "true",
        matchIfMissing = true
    )
    @ConditionalOnMissingBean(TokenProvider.class)  // 다른 TokenProvider가 없을 때만
    public TokenProvider springJwtTokenProvider(HexacoreSecurityProperties properties) {
        com.dx.hexacore.security.auth.adapter.outbound.token.jwt.JwtProperties jwtProperties = 
            convertToJwtProperties(properties.getTokenProvider().getJwt());
        return new SpringJwtTokenProvider(jwtProperties);
    }
    
    private com.dx.hexacore.security.auth.adapter.outbound.token.jwt.JwtProperties convertToJwtProperties(
            HexacoreSecurityProperties.TokenProvider.JwtProperties configProperties) {
        com.dx.hexacore.security.auth.adapter.outbound.token.jwt.JwtProperties jwtProperties = 
            new com.dx.hexacore.security.auth.adapter.outbound.token.jwt.JwtProperties();
        jwtProperties.setSecret(configProperties.getSecret());
        jwtProperties.setAccessTokenExpiration(configProperties.getAccessTokenExpiration());
        jwtProperties.setRefreshTokenExpiration(configProperties.getRefreshTokenExpiration());
        jwtProperties.setIssuer(configProperties.getIssuer());
        return jwtProperties;
    }
    
}