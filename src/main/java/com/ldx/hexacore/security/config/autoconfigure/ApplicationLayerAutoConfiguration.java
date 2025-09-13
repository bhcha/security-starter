package com.ldx.hexacore.security.config.autoconfigure;

import com.ldx.hexacore.security.auth.application.command.handler.AuthenticationCommandConfiguration;
import com.ldx.hexacore.security.auth.adapter.outbound.event.EventPublisherConfiguration;
import com.ldx.hexacore.security.config.properties.SecurityStarterProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Application layer auto-configuration.
 */
@Configuration
@ConditionalOnProperty(
    prefix = "security-starter",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
@EnableConfigurationProperties(SecurityStarterProperties.class)
@ComponentScan(basePackages = {
    "com.ldx.hexacore.security.auth.application",
    "com.ldx.hexacore.security.auth.domain",
    "com.ldx.hexacore.security.auth.adapter.outbound.event"
})
@Import({
    AuthenticationCommandConfiguration.class,
    EventPublisherConfiguration.class
})
public class ApplicationLayerAutoConfiguration {
    // Application layer configuration without persistence dependencies
}