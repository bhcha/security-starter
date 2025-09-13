package com.ldx.hexacore.security.config.autoconfigure;

import com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.ldx.hexacore.security.config.properties.SecurityStarterProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    JacksonAutoConfiguration.class,
                    TokenProviderAutoConfiguration.class
            ));

    @Test
    void shouldLoadAutoConfiguration() {
        contextRunner
                .withPropertyValues(
                        "security-starter.enabled=true",
                        "security-starter.token-provider.jwt.enabled=true",
                        "security-starter.token-provider.jwt.secret=test-secret-key-for-testing-purpose-only-32chars"
                )
                .run(context -> {
                    // Check if properties are loaded
                    assertThat(context).hasSingleBean(SecurityStarterProperties.class);
                    // Check if TokenProvider is created (JWT provider)
                    assertThat(context).hasSingleBean(TokenProvider.class);
                });
    }

    @Test
    void shouldNotLoadWhenDisabled() {
        contextRunner
                .withPropertyValues(
                        "security-starter.enabled=true",
                        "security-starter.token-provider.jwt.enabled=false"
                )
                .run(context -> {
                    // When JWT provider is disabled, no TokenProvider should be created
                    assertThat(context).doesNotHaveBean(TokenProvider.class);
                });
    }
}