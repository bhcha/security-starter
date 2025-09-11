package com.ldx.hexacore.security.config.autoconfigure;

import com.ldx.hexacore.security.config.SecurityConstants;
import com.ldx.hexacore.security.logging.SecurityEventLogger;
import com.ldx.hexacore.security.logging.SecurityRequestLogger;
import com.ldx.hexacore.security.logging.SecurityStartupLogger;
import com.ldx.hexacore.security.logging.SuspiciousActivityTracker;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for Security Logging Beans
 * 
 * Explicitly registers logging beans to ensure Component Scan independence.
 */
@Configuration
public class LoggingBeansAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SecurityEventLogger securityEventLogger(SecurityConstants securityConstants) {
        return new SecurityEventLogger(securityConstants);
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityRequestLogger securityRequestLogger() {
        return new SecurityRequestLogger();
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityStartupLogger securityStartupLogger() {
        return new SecurityStartupLogger();
    }

    @Bean
    @ConditionalOnMissingBean
    public SuspiciousActivityTracker suspiciousActivityTracker() {
        return new SuspiciousActivityTracker();
    }
}