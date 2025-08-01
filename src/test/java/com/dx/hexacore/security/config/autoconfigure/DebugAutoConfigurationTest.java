package com.dx.hexacore.security.config.autoconfigure;

import com.dx.hexacore.security.config.properties.HexacoreSecurityProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class DebugAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    DataSourceAutoConfiguration.class,
                    HibernateJpaAutoConfiguration.class,
                    JacksonAutoConfiguration.class,
                    com.dx.hexacore.security.config.HexacoreSecurityAutoConfiguration.class
            ));

    @Test
    void shouldLoadBasicAutoConfiguration() {
        contextRunner
                .withPropertyValues(
                    "hexacore.security.enabled=true",
                    "hexacore.security.persistence.jpa.enabled=true", // Enable JPA for proper testing
                    "spring.datasource.url=jdbc:h2:mem:testdb",
                    "spring.datasource.driver-class-name=org.h2.Driver",
                    "spring.jpa.hibernate.ddl-auto=create-drop",
                    
                    // Token provider configuration 
                    "hexacore.security.token-provider.provider=keycloak",
                    "hexacore.security.token-provider.keycloak.enabled=true",
                    "hexacore.security.token-provider.keycloak.server-url=http://localhost:8080",
                    "hexacore.security.token-provider.keycloak.realm=test-realm",
                    "hexacore.security.token-provider.keycloak.client-id=test-client",
                    "hexacore.security.token-provider.keycloak.client-secret=test-secret"
                )
                .run(context -> {
                    if (context.getStartupFailure() != null) {
                        System.err.println("Context startup failed:");
                        context.getStartupFailure().printStackTrace();
                        throw new AssertionError("Context failed to start", context.getStartupFailure());
                    }
                    
                    // This should work
                    assertThat(context).hasSingleBean(HexacoreSecurityProperties.class);
                });
    }
}