package com.ldx.hexacore.security.integration;

import com.ldx.hexacore.security.config.properties.SecurityStarterProperties;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.ldx.hexacore.security.config.autoconfigure.TokenProviderAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleIntegrationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    TokenProviderAutoConfiguration.class
            ))
            .withPropertyValues(
                    "hexacore.security.enabled=true",
                    "hexacore.security.token-provider.provider=jwt"
            );

    @Test
    void contextLoads() {
        contextRunner.run(context -> {
            assertThat(context).isNotNull();
        });
    }

    @Test
    void shouldHaveSecurityStarterPropertiesBean() {
        contextRunner.run(context -> {
            // SecurityStarterProperties 빈이 타입으로 존재하는지 확인
            assertThat(context).hasSingleBean(SecurityStarterProperties.class);
            
            SecurityStarterProperties properties = context.getBean(SecurityStarterProperties.class);
            assertThat(properties.getEnabled()).isTrue();
            assertThat(properties.getTokenProvider().getProvider()).isEqualTo("jwt");
        });
    }

    @Test
    void shouldHaveAutoConfigurationBeans() {
        contextRunner.run(context -> {
            // TokenProvider가 기본적으로 생성되는지 확인
            assertThat(context).hasSingleBean(TokenProvider.class);
        });
    }
}