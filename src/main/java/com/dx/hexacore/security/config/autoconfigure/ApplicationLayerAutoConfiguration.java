package com.dx.hexacore.security.config.autoconfigure;

import com.dx.hexacore.security.auth.application.command.port.out.AuthenticationRepository;
import com.dx.hexacore.security.config.properties.HexacoreSecurityProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
    prefix = "hexacore.security.application", 
    name = "enabled", 
    havingValue = "true", 
    matchIfMissing = true
)
@EnableConfigurationProperties(HexacoreSecurityProperties.class)
@ComponentScan(basePackages = {
    "com.dx.hexacore.security.auth.application",
    "com.dx.hexacore.security.session.application", 
    "com.dx.hexacore.security.auth.domain",
    "com.dx.hexacore.security.session.domain",
    "com.dx.hexacore.security.auth.adapter.inbound.config",
    "com.dx.hexacore.security.session.adapter.inbound.config"
})
@Import({
    com.dx.hexacore.security.auth.application.command.handler.AuthenticationCommandConfiguration.class,
    com.dx.hexacore.security.session.application.command.handler.SessionCommandConfiguration.class,
    com.dx.hexacore.security.auth.application.query.handler.AuthenticationQueryConfiguration.class,
    com.dx.hexacore.security.session.application.query.handler.SessionQueryConfiguration.class
})
public class ApplicationLayerAutoConfiguration {
    // Application layer will be loaded only when persistence is available
}