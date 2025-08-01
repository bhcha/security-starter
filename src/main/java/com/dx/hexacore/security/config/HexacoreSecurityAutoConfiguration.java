package com.dx.hexacore.security.config;

import com.dx.hexacore.security.auth.application.command.port.in.AuthenticationUseCase;
import com.dx.hexacore.security.config.autoconfigure.TokenProviderAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration for Hexacore Security library.
 * This configuration enables component scanning for all security-related beans.
 * 
 * Users can override specific beans by defining their own with the same type.
 */
@Configuration
@ConditionalOnClass(AuthenticationUseCase.class)
@ConditionalOnProperty(prefix = "hexacore.security", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(com.dx.hexacore.security.config.properties.HexacoreSecurityProperties.class)
@Import({
    TokenProviderAutoConfiguration.class,
    com.dx.hexacore.security.config.autoconfigure.PersistenceAutoConfiguration.class
})
@ComponentScan(basePackages = {
    "com.dx.hexacore.security.auth.application.command.handler",
    "com.dx.hexacore.security.auth.application.query.handler", 
    "com.dx.hexacore.security.session.application.command.handler",
    "com.dx.hexacore.security.session.application.query.handler",
    "com.dx.hexacore.security.auth.adapter.outbound",
    "com.dx.hexacore.security.session.adapter.outbound",
    "com.dx.hexacore.security.auth.adapter.inbound",
    "com.dx.hexacore.security.session.adapter.inbound"
})
public class HexacoreSecurityAutoConfiguration {
    
    /**
     * Marker bean to indicate that Hexacore Security is configured
     */
    @Bean
    public Object hexacoreSecurityMarker() {
        return new Object();
    }
}