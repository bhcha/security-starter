package com.ldx.hexacore.security.config.properties;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityStarterPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfiguration.class);

    @Test
    void shouldBindDefaultValues() {
        contextRunner.run(context -> {
            SecurityStarterProperties properties = context.getBean(SecurityStarterProperties.class);
            
            assertThat(properties.getEnabled()).isTrue(); // Zero Configuration - 기본값 true
            
            // TokenProvider defaults
            var tokenProvider = properties.getTokenProvider();
            assertThat(tokenProvider.getProvider()).isEqualTo("jwt");
            assertThat(tokenProvider.getKeycloak().getEnabled()).isTrue();
            assertThat(tokenProvider.getJwt().getEnabled()).isTrue();
        });
    }

    @Test
    void shouldBindCompleteConfiguration() {
        contextRunner
                .withPropertyValues(
                        "security-starter.enabled=true",
                        "security-starter.token-provider.provider=spring_jwt",
                        "security-starter.token-provider.jwt.enabled=true",
                        "security-starter.token-provider.jwt.secret=my-super-secret-key-that-is-long-enough-for-256-bits",
                        "security-starter.token-provider.jwt.issuer=test-issuer",
                        "security-starter.token-provider.jwt.access-token-expiration=7200",
                        "security-starter.token-provider.jwt.refresh-token-expiration=604800"
                )
                .run(context -> {
                    SecurityStarterProperties properties = context.getBean(SecurityStarterProperties.class);
                    
                    assertThat(properties.getEnabled()).isTrue();
                    
                    // TokenProvider
                    var tokenProvider = properties.getTokenProvider();
                    assertThat(tokenProvider.getProvider()).isEqualTo("spring_jwt");
                    assertThat(tokenProvider.getJwt().getEnabled()).isTrue();
                    assertThat(tokenProvider.getJwt().getSecret()).isEqualTo("my-super-secret-key-that-is-long-enough-for-256-bits");
                    assertThat(tokenProvider.getJwt().getIssuer()).isEqualTo("test-issuer");
                    assertThat(tokenProvider.getJwt().getAccessTokenExpiration()).isEqualTo(7200);
                    assertThat(tokenProvider.getJwt().getRefreshTokenExpiration()).isEqualTo(604800);
                });
    }

    @Configuration
    @EnableConfigurationProperties(SecurityStarterProperties.class)
    static class TestConfiguration {
    }
}