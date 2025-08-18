package com.dx.hexacore.security.config.autoconfigure;

import com.dx.hexacore.security.auth.adapter.outbound.token.jwt.SpringJwtTokenProvider;
import com.dx.hexacore.security.auth.adapter.outbound.token.noop.NoOpTokenProvider;
import com.dx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.dx.hexacore.security.config.properties.HexacoreSecurityProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(HexacoreSecurityProperties.class)
@ConditionalOnClass(TokenProvider.class)
public class TokenProviderAutoConfiguration {
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TokenProviderAutoConfiguration.class);
    
    /**
     * Keycloak Token Provider Configuration
     * Separated into a nested configuration to avoid class loading issues
     */
    @Configuration
    @ConditionalOnProperty(
        prefix = "hexacore.security.token-provider", 
        name = "provider", 
        havingValue = "keycloak"
    )
    @ConditionalOnClass(name = "com.dx.hexacore.security.auth.adapter.outbound.token.keycloak.KeycloakTokenProvider")
    public static class KeycloakTokenProviderConfiguration {
        
        @Bean
        @ConditionalOnProperty(
            prefix = "hexacore.security.token-provider.keycloak", 
            name = "enabled", 
            havingValue = "true", 
            matchIfMissing = false
        )
        public TokenProvider keycloakTokenProvider(HexacoreSecurityProperties properties) {
            HexacoreSecurityProperties.TokenProvider.KeycloakProperties keycloakConfig = 
                properties.getTokenProvider().getKeycloak();
            
            try {
                Class<?> keycloakProviderClass = Class.forName("com.dx.hexacore.security.auth.adapter.outbound.token.keycloak.KeycloakTokenProvider");
                return (TokenProvider) keycloakProviderClass.getConstructor(HexacoreSecurityProperties.TokenProvider.KeycloakProperties.class)
                    .newInstance(keycloakConfig);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to create KeycloakTokenProvider", e);
            }
        }
    }
    
    /**
     * JWT TokenProvider Configuration (JWT 라이브러리가 있을 때)
     */
    @Bean(name = "springJwtTokenProvider")  // 명시적 Bean 이름 지정
    @ConditionalOnClass(name = "io.jsonwebtoken.JwtBuilder")  // JWT 라이브러리 체크
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
        
        // Keycloak 설정 충돌 체크
        String providerType = properties.getTokenProvider().getProvider();
        if ("keycloak".equals(providerType)) {
            logger.warn("⚠️ Provider가 'keycloak'으로 설정되었지만 JWT TokenProvider가 생성됩니다. 설정을 확인해주세요.");
        }
        
        com.dx.hexacore.security.auth.adapter.outbound.token.jwt.JwtProperties jwtProperties = 
            convertToJwtProperties(properties.getTokenProvider().getJwt());
        
        SpringJwtTokenProvider jwtProvider = new SpringJwtTokenProvider(jwtProperties);
        
        // 디버깅을 위한 로그 추가
        logger.info("✅ SpringJwtTokenProvider Bean registered successfully");
        logger.info("JWT Settings - Secret: [PROTECTED], Issuer: {}, Access Token Expiration: {}s", 
            jwtProperties.getIssuer(), jwtProperties.getAccessTokenExpiration());
        
        return jwtProvider;
    }

    /**
     * No-Op TokenProvider Configuration (JWT 라이브러리가 없을 때)
     */
    @Bean(name = "noOpTokenProvider")  // 명시적 Bean 이름 지정
    @ConditionalOnMissingClass("io.jsonwebtoken.JwtBuilder")  // JWT 라이브러리가 없을 때
    @ConditionalOnMissingBean(TokenProvider.class)
    public TokenProvider noOpTokenProvider() {
        logger.warn("⚠️ JWT 라이브러리가 없어 NoOpTokenProvider를 사용합니다. 실제 JWT 인증을 위해서는 JWT 의존성이 필요합니다.");
        return new NoOpTokenProvider();
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