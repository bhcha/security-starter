package com.ldx.hexacore.security.auth.adapter.outbound.external;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Keycloak 연동을 위한 설정 클래스.
 * RestTemplate 및 SSL 설정을 구성합니다.
 * 
 * @since 1.0.0
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "security-starter.token-provider.keycloak", name = "enabled", havingValue = "true", matchIfMissing = false)
class KeycloakConfig {
    
    private final KeycloakProperties properties;
    
    @Bean(name = "keycloakRestTemplate")
    public RestTemplate keycloakRestTemplate(RestTemplateBuilder builder) {
        // Spring Boot 3.2에서는 setConnectTimeout과 setReadTimeout 사용
        return builder
                .setConnectTimeout(Duration.ofMillis(properties.getConnectTimeout()))
                .setReadTimeout(Duration.ofMillis(properties.getReadTimeout()))
                .build();
    }
}