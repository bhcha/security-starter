package com.dx.hexacore.security.config.autoconfigure;

import com.dx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.dx.hexacore.security.config.properties.HexacoreSecurityProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class TokenProviderAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    JacksonAutoConfiguration.class,
                    TokenProviderAutoConfiguration.class
            ));

    @Test
    void shouldCreateJwtProviderByDefault() {
        contextRunner
                .run(context -> {
                    assertThat(context).hasSingleBean(TokenProvider.class);
                    assertThat(context).doesNotHaveBean("keycloakTokenProvider");
                    assertThat(context).hasBean("springJwtTokenProvider");
                    
                    TokenProvider provider = context.getBean(TokenProvider.class);
                    assertThat(provider.getProviderType().name()).isEqualTo("SPRING_JWT");
                });
    }

    @Test
    void shouldCreateKeycloakProviderWhenConfigured() {
        contextRunner
                .withPropertyValues(
                        "hexacore.security.enabled=true",
                        "hexacore.security.token-provider.provider=keycloak",
                        "hexacore.security.token-provider.keycloak.enabled=true",
                        "hexacore.security.token-provider.keycloak.server-url=http://localhost:8080",
                        "hexacore.security.token-provider.keycloak.realm=test",
                        "hexacore.security.token-provider.keycloak.client-id=test-client",
                        "hexacore.security.token-provider.keycloak.client-secret=test-secret"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(TokenProvider.class);
                    assertThat(context).hasBean("keycloakTokenProvider");
                    assertThat(context).doesNotHaveBean("springJwtTokenProvider");
                    
                    TokenProvider provider = context.getBean(TokenProvider.class);
                    assertThat(provider.getProviderType().name()).isEqualTo("KEYCLOAK");
                });
    }

    @Test  
    void shouldCreateSpringJwtProviderWhenConfigured() {
        contextRunner
                .withPropertyValues(
                        "hexacore.security.enabled=true",
                        "hexacore.security.token-provider.provider=jwt",
                        "hexacore.security.token-provider.jwt.enabled=true",
                        "hexacore.security.token-provider.jwt.secret=my-super-secret-key-that-is-long-enough-for-256-bits"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(TokenProvider.class);
                    assertThat(context).hasBean("springJwtTokenProvider");
                    assertThat(context).doesNotHaveBean("keycloakTokenProvider");
                    
                    TokenProvider provider = context.getBean(TokenProvider.class);
                    assertThat(provider.getProviderType().name()).isEqualTo("SPRING_JWT");
                });
    }

    @Test
    void shouldNotCreateProviderWhenDisabled() {
        // Keycloak provider disabled
        contextRunner
                .withPropertyValues(
                        "hexacore.security.enabled=true",
                        "hexacore.security.token-provider.provider=keycloak",
                        "hexacore.security.token-provider.keycloak.enabled=false"
                )
                .run(context -> {
                    assertThat(context).doesNotHaveBean(TokenProvider.class);
                    assertThat(context).doesNotHaveBean("keycloakTokenProvider");
                });

        // Spring JWT provider disabled
        contextRunner
                .withPropertyValues(
                        "hexacore.security.enabled=true",
                        "hexacore.security.token-provider.provider=jwt",
                        "hexacore.security.token-provider.jwt.enabled=false"
                )
                .run(context -> {
                    assertThat(context).doesNotHaveBean(TokenProvider.class);
                    assertThat(context).doesNotHaveBean("springJwtTokenProvider");
                });
    }

    @Test
    void shouldBindPropertiesCorrectly() {
        contextRunner
                .withPropertyValues(
                        "hexacore.security.enabled=true",
                        "hexacore.security.token-provider.provider=jwt",
                        "hexacore.security.token-provider.jwt.enabled=true",
                        "hexacore.security.token-provider.jwt.secret=my-super-secret-key-that-is-long-enough-for-256-bits",
                        "hexacore.security.token-provider.jwt.issuer=test-issuer",
                        "hexacore.security.token-provider.jwt.access-token-expiration=7200",
                        "hexacore.security.token-provider.jwt.refresh-token-expiration=1209600"
                )
                .run(context -> {
                    HexacoreSecurityProperties properties = context.getBean(HexacoreSecurityProperties.class);
                    var jwt = properties.getTokenProvider().getJwt();
                    
                    assertThat(jwt.getEnabled()).isTrue();
                    assertThat(jwt.getSecret()).isEqualTo("my-super-secret-key-that-is-long-enough-for-256-bits");
                    assertThat(jwt.getIssuer()).isEqualTo("test-issuer");
                    assertThat(jwt.getAccessTokenExpiration()).isEqualTo(7200);
                    assertThat(jwt.getRefreshTokenExpiration()).isEqualTo(1209600);
                });
    }
}