package com.ldx.hexacore.security.config.autoconfigure;

import com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.ldx.hexacore.security.config.SecurityStarterAutoConfiguration;
import com.ldx.hexacore.security.config.properties.SecurityStarterProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class TokenProviderAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    JacksonAutoConfiguration.class,
                    SecurityStarterAutoConfiguration.class
            ))
            .withPropertyValues(
                    "spring.datasource.url=jdbc:h2:mem:testdb",
                    "spring.datasource.driver-class-name=org.h2.Driver",
                    "spring.jpa.hibernate.ddl-auto=create-drop"
            );

    @Test
    void shouldCreateJwtProviderByDefault() {
        contextRunner
                .withPropertyValues(
                        "security-starter.token-provider.jwt.enabled=true"
                )
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
                        "security-starter.enabled=true",
                        "security-starter.token-provider.provider=keycloak",
                        "security-starter.token-provider.keycloak.enabled=true",
                        "security-starter.token-provider.keycloak.server-url=http://localhost:8080",
                        "security-starter.token-provider.keycloak.realm=test",
                        "security-starter.token-provider.keycloak.client-id=test-client",
                        "security-starter.token-provider.keycloak.client-secret=test-secret",
                        "security-starter.token-provider.jwt.enabled=false"
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
                        "security-starter.enabled=true",
                        "security-starter.token-provider.jwt.enabled=true",
                        "security-starter.token-provider.jwt.secret=my-super-secret-key-that-is-long-enough-for-256-bits"
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
        // Create a separate runner for this test to avoid loading other configurations
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        JacksonAutoConfiguration.class,
                        TokenProviderAutoConfiguration.class
                ))
                .withPropertyValues(
                        "security-starter.enabled=true",
                        "security-starter.token-provider.keycloak.enabled=false",
                        "security-starter.token-provider.jwt.enabled=false",
                        // Keycloak 필수 필드를 위한 더미 값
                        "security-starter.token-provider.keycloak.server-url=http://dummy",
                        "security-starter.token-provider.keycloak.realm=dummy",
                        "security-starter.token-provider.keycloak.client-id=dummy"
                )
                .run(context -> {
                    assertThat(context).doesNotHaveBean(TokenProvider.class);
                    assertThat(context).doesNotHaveBean("keycloakTokenProvider");
                    assertThat(context).doesNotHaveBean("springJwtTokenProvider");
                });
    }

    @Test
    void shouldBindPropertiesCorrectly() {
        contextRunner
                .withPropertyValues(
                        "security-starter.enabled=true",
                        "security-starter.token-provider.jwt.enabled=true",
                        "security-starter.token-provider.jwt.secret=my-super-secret-key-that-is-long-enough-for-256-bits",
                        "security-starter.token-provider.jwt.issuer=test-issuer",
                        "security-starter.token-provider.jwt.access-token-expiration=7200",
                        "security-starter.token-provider.jwt.refresh-token-expiration=1209600"
                )
                .run(context -> {
                    // Properties bean should exist when properly configured
                    assertThat(context).hasSingleBean(SecurityStarterProperties.class);
                    
                    // TokenProvider implementation should be created
                    assertThat(context).hasSingleBean(TokenProvider.class);
                    assertThat(context).hasBean("springJwtTokenProvider");
                });
    }
}