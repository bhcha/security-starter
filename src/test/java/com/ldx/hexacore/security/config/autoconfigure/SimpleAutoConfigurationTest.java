package com.ldx.hexacore.security.config.autoconfigure;

import com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.ldx.hexacore.security.config.properties.HexacoreSecurityProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    TokenProviderAutoConfiguration.class
            ));

    @Test
    void shouldLoadAutoConfiguration() {
        contextRunner.run(context -> {
            // Check if properties are loaded
            assertThat(context).hasSingleBean(HexacoreSecurityProperties.class);
            // Check if TokenProvider is created (default JWT provider)
            assertThat(context).hasSingleBean(TokenProvider.class);
        });
    }

    @Test
    void shouldNotLoadWhenDisabled() {
        contextRunner
                .withPropertyValues("hexacore.security.token-provider.jwt.enabled=false")
                .run(context -> {
                    // When JWT provider is disabled, no TokenProvider should be created
                    assertThat(context).doesNotHaveBean(TokenProvider.class);
                });
    }
}