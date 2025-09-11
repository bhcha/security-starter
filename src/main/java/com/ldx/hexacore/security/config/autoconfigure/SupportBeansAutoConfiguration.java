package com.ldx.hexacore.security.config.autoconfigure;

import com.ldx.hexacore.security.config.properties.SecurityStarterProperties;
import com.ldx.hexacore.security.config.support.SecurityAuthFailureAnalyzer;
import com.ldx.hexacore.security.config.support.SecurityAuthHealthIndicator;
import com.ldx.hexacore.security.config.support.SecurityConfigurationValidator;
import com.ldx.hexacore.security.config.support.SecurityStarterFailureAnalyzer;
import com.ldx.hexacore.security.config.support.SecurityStartupValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for Security Support Beans
 * 
 * Explicitly registers support beans like validators, health indicators,
 * and failure analyzers to ensure Component Scan independence.
 */
@Configuration
public class SupportBeansAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SecurityConfigurationValidator securityConfigurationValidator(
            SecurityStarterProperties properties) {
        return new SecurityConfigurationValidator(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityStartupValidator securityStartupValidator() {
        return new SecurityStartupValidator();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.springframework.boot.actuate.health.HealthIndicator")
    public SecurityAuthHealthIndicator securityAuthHealthIndicator() {
        return new SecurityAuthHealthIndicator();
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityAuthFailureAnalyzer securityAuthFailureAnalyzer() {
        return new SecurityAuthFailureAnalyzer();
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityStarterFailureAnalyzer securityStarterFailureAnalyzer() {
        return new SecurityStarterFailureAnalyzer();
    }
}