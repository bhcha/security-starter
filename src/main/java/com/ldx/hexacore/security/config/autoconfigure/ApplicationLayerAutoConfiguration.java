package com.ldx.hexacore.security.config.autoconfigure;

import com.ldx.hexacore.security.auth.application.command.handler.AuthenticationCommandConfiguration;
import com.ldx.hexacore.security.auth.application.command.port.out.AuthenticationRepository;
import com.ldx.hexacore.security.config.properties.SecurityStarterProperties;
import com.ldx.hexacore.security.auth.application.query.handler.AuthenticationQueryConfiguration;
import com.ldx.hexacore.security.session.application.command.handler.SessionCommandConfiguration;
import com.ldx.hexacore.security.session.application.query.handler.SessionQueryConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Application layer auto-configuration.
 * 
 * Only loads when persistence adapters are available.
 */
@Configuration
@ConditionalOnClass(AuthenticationRepository.class)
@ConditionalOnProperty(
    prefix = "security-starter.application", 
    name = "enabled", 
    havingValue = "true", 
    matchIfMissing = true
)
@EnableConfigurationProperties(SecurityStarterProperties.class)
@ComponentScan(basePackages = {
    "com.dx.security-starter.auth.application",
    "com.dx.security-starter.session.application", 
    "com.dx.security-starter.auth.domain",
    "com.dx.security-starter.session.domain",
    "com.dx.security-starter.auth.adapter.inbound.config",
    "com.dx.security-starter.session.adapter.inbound.config"
})
@Import({
    AuthenticationCommandConfiguration.class,
    SessionCommandConfiguration.class,
    AuthenticationQueryConfiguration.class,
    SessionQueryConfiguration.class
})
public class ApplicationLayerAutoConfiguration {
    // Application layer will be loaded only when persistence is available
}