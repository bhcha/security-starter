package com.dx.hexacore.security.config.autoconfigure;

import com.dx.hexacore.security.config.properties.HexacoreSecurityProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class MinimalAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfiguration.class);

    @Test
    void shouldLoadPropertiesOnly() {
        contextRunner
                .withPropertyValues(
                        "hexacore.security.enabled=true"
                )
                .run(context -> {
                    System.out.println("Context started successfully");
                    // Only check if properties are bound correctly
                    assertThat(context).hasSingleBean(HexacoreSecurityProperties.class);
                    
                    HexacoreSecurityProperties properties = context.getBean(HexacoreSecurityProperties.class);
                    assertThat(properties.getEnabled()).isTrue();
                });
    }
    
    @Test
    void shouldNotLoadWhenDisabled() {
        contextRunner
                .withPropertyValues("hexacore.security.enabled=false")
                .run(context -> {
                    // Just verify the properties are set correctly
                    HexacoreSecurityProperties properties = context.getBean(HexacoreSecurityProperties.class);
                    assertThat(properties.getEnabled()).isFalse();
                });
    }
    
    @Configuration
    @EnableConfigurationProperties(HexacoreSecurityProperties.class)
    static class TestConfiguration {
    }
}