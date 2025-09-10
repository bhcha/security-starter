package com.ldx.hexacore.security.config.autoconfigure;

import com.ldx.hexacore.security.auth.adapter.inbound.filter.SecurityFilterConfig;
import com.ldx.hexacore.security.config.properties.HexacoreSecurityProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 필터 체인 자동 설정
 * 
 * JWT 기반 인증 필터를 자동으로 구성합니다.
 * security-auth-starter의 모든 기능을 통합하여 제공합니다.
 * 실제 구현은 SecurityFilterConfig에 위임하고, 추가 설정을 제공합니다.
 */
@AutoConfiguration(after = {SecurityAutoConfiguration.class, PersistenceAutoConfiguration.class, TokenProviderAutoConfiguration.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(
    prefix = "hexacore.security.filter",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
@ConditionalOnClass(name = {
    "org.springframework.security.web.SecurityFilterChain",
        "com.ldx.hexacore.security.auth.adapter.inbound.filter.JwtAuthenticationFilter"
})
@EnableConfigurationProperties(HexacoreSecurityProperties.class)
@EnableWebSecurity
@Import(SecurityFilterConfig.class)
public class SecurityFilterAutoConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityFilterAutoConfiguration.class);
    
    private final HexacoreSecurityProperties properties;
    
    public SecurityFilterAutoConfiguration(HexacoreSecurityProperties properties) {
        this.properties = properties;
    }
    
    @PostConstruct
    public void init() {
        logger.info("Initializing Security Filter Auto Configuration");
        logger.info("Authentication Filter enabled: {}", properties.getFilter().getEnabled());
        logger.info("Security Headers enabled: {}", properties.getHeaders().getEnabled());
    }
    
    /**
     * Default security filter chain configuration for applications that don't define their own.
     * This provides a fallback configuration with security headers and basic authorization rules.
     * 
     * This bean is only created when SecurityFilterConfig is not active.
     */
    @Bean
    @Order(100) // Lower priority than custom filter chains
    @ConditionalOnMissingBean({SecurityFilterChain.class, SecurityFilterConfig.class})
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        logger.debug("Configuring default security filter chain");
        
        // Basic security configuration
        http
            .cors(cors -> cors.disable()) // Configure CORS as needed
            .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless JWT auth
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        
        // Configure authorization
        configureAuthorization(http);
        
        // Configure security headers
        if (properties.getHeaders().getEnabled()) {
            configureSecurityHeaders(http);
        }
        
        // Note: JWT filter is automatically configured by SecurityFilterConfig
        // when JwtAuthenticationFilter bean is present
        
        return http.build();
    }
    
    /**
     * Configure authorization rules.
     */
    private void configureAuthorization(HttpSecurity http) throws Exception {
        var jwtProperties = properties.getTokenProvider().getJwt();
        
        http.authorizeHttpRequests(authz -> {
            // Configure excluded paths (no authentication required)
            var excludePaths = properties.getFilter().getExcludePaths();
            if (excludePaths != null && excludePaths.length > 0) {
                authz.requestMatchers(excludePaths).permitAll();
                logger.debug("Excluded paths from authentication: {}", (Object) excludePaths);
            }
            
            // JWT specific excluded paths
            if (jwtProperties.getExcludedPaths() != null && !jwtProperties.getExcludedPaths().isEmpty()) {
                String[] jwtExcludedPaths = jwtProperties.getExcludedPaths().toArray(new String[0]);
                authz.requestMatchers(jwtExcludedPaths).permitAll();
                logger.debug("JWT excluded paths from authentication: {}", (Object) jwtExcludedPaths);
            }
            
            // Default: all other requests require authentication
            authz.anyRequest().authenticated();
        });
    }
    
    /**
     * Configure security headers based on properties.
     */
    private void configureSecurityHeaders(HttpSecurity http) throws Exception {
        var headersProperties = properties.getHeaders();
        
        http.headers(headers -> {
            // X-Frame-Options
            if (headersProperties.getFrameOptions() != null) {
                headers.frameOptions(frame -> {
                    switch (headersProperties.getFrameOptions().toUpperCase()) {
                        case "DENY":
                            frame.deny();
                            break;
                        case "SAMEORIGIN":
                            frame.sameOrigin();
                            break;
                        default:
                            frame.disable();
                    }
                });
            }
            
            // X-Content-Type-Options
            if (headersProperties.getContentTypeOptions() != null) {
                headers.contentTypeOptions(content -> {});
            }
            
            // X-XSS-Protection
            if (headersProperties.getXssProtection() != null) {
                headers.xssProtection(xss -> {});
            }
            
            // Strict-Transport-Security (HSTS)
            if (headersProperties.getHstsEnabled() && headersProperties.getHsts() != null) {
                headers.httpStrictTransportSecurity(hsts -> 
                    hsts.includeSubDomains(headersProperties.getHsts().contains("includeSubDomains"))
                );
            }
            
            // Content-Security-Policy
            if (headersProperties.getCspEnabled() && headersProperties.getContentSecurityPolicy() != null) {
                headers.contentSecurityPolicy(csp -> 
                    csp.policyDirectives(headersProperties.getContentSecurityPolicy())
                );
            }
            
            // Referrer-Policy
            if (headersProperties.getReferrerPolicy() != null) {
                headers.referrerPolicy(referrer -> {});
            }
        });
        
        logger.debug("Security headers configured");
    }
    
    /**
     * JWT configuration marker.
     * Indicates that JWT is configured through this starter.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(
        prefix = "hexacore.security.token-provider.jwt",
        name = "enabled",
        havingValue = "true"
    )
    static class JwtConfiguration {
        
        private static final Logger logger = LoggerFactory.getLogger(JwtConfiguration.class);
        
        @Bean
        public JwtConfigurationMarker jwtConfigurationMarker() {
            logger.debug("JWT authentication is enabled");
            return new JwtConfigurationMarker();
        }
        
        public static class JwtConfigurationMarker {
            // Marker class
        }
    }
    
    /**
     * Rate limiting configuration marker.
     * Actual rate limiting implementation will be in future phases.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(
        prefix = "hexacore.security.rate-limit",
        name = "enabled",
        havingValue = "true"
    )
    static class RateLimitConfiguration {
        
        private static final Logger logger = LoggerFactory.getLogger(RateLimitConfiguration.class);
        
        @Bean
        public RateLimitConfigurationMarker rateLimitConfigurationMarker() {
            logger.debug("Rate limiting is enabled (implementation pending)");
            return new RateLimitConfigurationMarker();
        }
        
        public static class RateLimitConfigurationMarker {
            // Marker class
        }
    }
    
    /**
     * IP restriction configuration marker.
     * Actual IP restriction implementation will be in future phases.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(
        prefix = "hexacore.security.ip-restriction",
        name = "enabled",
        havingValue = "true"
    )
    static class IpRestrictionConfiguration {
        
        private static final Logger logger = LoggerFactory.getLogger(IpRestrictionConfiguration.class);
        
        @Bean
        public IpRestrictionConfigurationMarker ipRestrictionConfigurationMarker() {
            logger.debug("IP restriction is enabled (implementation pending)");
            return new IpRestrictionConfigurationMarker();
        }
        
        public static class IpRestrictionConfigurationMarker {
            // Marker class
        }
    }
}