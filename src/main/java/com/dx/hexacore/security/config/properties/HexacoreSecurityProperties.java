package com.dx.hexacore.security.config.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Hexacore Security의 메인 설정 프로퍼티입니다.
 * 
 * application.yml에서 hexacore.security.* 설정을 관리합니다.
 * security-auth-starter의 모든 설정을 통합하여 제공합니다.
 */
@Data
@ConfigurationProperties(prefix = "hexacore.security")
@Validated
public class HexacoreSecurityProperties {
    
    /**
     * Hexacore Security 기능 활성화 여부
     */
    @NotNull
    private Boolean enabled = true;
    
    /**
     * 토큰 제공자 설정
     */
    @Valid
    @NestedConfigurationProperty
    private TokenProvider tokenProvider = new TokenProvider();
    
    /**
     * 인증 필터 설정
     */
    @Valid
    @NestedConfigurationProperty
    private AuthFilterProperties filter = new AuthFilterProperties();
    
    /**
     * 세션 관리 설정
     */
    @Valid
    @NestedConfigurationProperty
    private SessionProperties session = new SessionProperties();
    
    /**
     * 영속성 설정
     */
    @Valid
    @NestedConfigurationProperty
    private PersistenceProperties persistence = new PersistenceProperties();
    
    /**
     * 캐시 설정
     */
    @Valid
    @NestedConfigurationProperty
    private CacheProperties cache = new CacheProperties();
    
    /**
     * Rate Limiting 설정 (starter에서 통합)
     */
    @Valid
    @NestedConfigurationProperty
    private RateLimitProperties rateLimit = new RateLimitProperties();
    
    /**
     * IP 제한 설정 (starter에서 통합)
     */
    @Valid
    @NestedConfigurationProperty
    private IpRestrictionProperties ipRestriction = new IpRestrictionProperties();
    
    /**
     * 보안 헤더 설정 (starter에서 통합)
     */
    @Valid
    @NestedConfigurationProperty
    private HeadersProperties headers = new HeadersProperties();
    
    @Data
    public static class AuthFilterProperties {
        /**
         * 인증 필터 활성화 여부
         */
        @NotNull
        private Boolean enabled = true;
        
        /**
         * 인증 제외 경로 목록
         */
        private String[] excludePaths = {
            "/api/health",
            "/api/docs/**",
            "/swagger-ui/**",
            "/v3/api-docs/**"
        };
    }
    
    @Data
    public static class SessionProperties {
        /**
         * 세션 관리 활성화 여부
         */
        @NotNull
        private Boolean enabled = true;
        
        /**
         * 세션 이벤트 처리 활성화 여부
         */
        private EventProperties event = new EventProperties();
        
        /**
         * 계정 잠금 정책
         */
        private LockoutPolicy lockout = new LockoutPolicy();
        
        @Data
        public static class EventProperties {
            @NotNull
            private Boolean enabled = true;
        }
        
        @Data
        public static class LockoutPolicy {
            /**
             * 최대 실패 허용 횟수
             */
            private int maxAttempts = 5;
            
            /**
             * 잠금 시간 (분)
             */
            private int lockoutDurationMinutes = 30;
            
            /**
             * 실패 카운트 윈도우 (분)
             */
            private int attemptWindowMinutes = 15;
        }
    }
    
    @Data
    public static class PersistenceProperties {
        /**
         * Persistence type.
         * Options: jpa, mongodb, redis
         * Default is jpa.
         */
        @NotNull
        private PersistenceType type = PersistenceType.JPA;
        
        /**
         * Command database name (for CQRS write side).
         * Default is "security_write".
         */
        @NotBlank
        private String commandDb = "security_write";
        
        /**
         * Query database name (for CQRS read side).
         * Default is "security_read".
         */
        @NotBlank
        private String queryDb = "security_read";
        
        /**
         * Enable JPA repositories.
         * Default is true when type is JPA.
         */
        @NotNull
        private Boolean jpaEnabled = true;
        
        /**
         * Enable MongoDB repositories.
         * Default is true when type is MONGODB.
         */
        @NotNull
        private Boolean mongoEnabled = false;
        
        /**
         * Enable Redis for caching.
         * Default is false.
         */
        @NotNull
        private Boolean redisEnabled = false;
        
        /**
         * Enable local cache (Caffeine).
         * Default is true.
         */
        @NotNull
        private Boolean localCacheEnabled = true;
        
        /**
         * Schema generation strategy for JPA.
         * Options: none, create, create-drop, update, validate
         * Default is validate.
         */
        @NotNull
        private String schemaGeneration = "validate";
        
        /**
         * Show SQL queries in logs.
         * Default is false.
         */
        @NotNull
        private Boolean showSql = false;
        
        /**
         * Format SQL queries in logs.
         * Default is false.
         */
        @NotNull
        private Boolean formatSql = false;
        
        /**
         * External authentication provider configuration.
         */
        private ExternalAuthProperties external = new ExternalAuthProperties();
        
        /**
         * Cache expiry time in minutes.
         * Default is 30 minutes.
         */
        private Integer cacheExpiryMinutes = 30;
        
        /**
         * Maximum cache size.
         * Default is 10000 entries.
         */
        private Long cacheMaxSize = 10000L;
        
        /**
         * Persistence type enum.
         */
        public enum PersistenceType {
            JPA,
            MONGODB,
            REDIS
        }
        
        /**
         * Get persistence type as lowercase string.
         * 
         * @return persistence type as lowercase string
         */
        public String getTypeAsString() {
            return type.name().toLowerCase();
        }
        
        /**
         * External authentication provider properties.
         */
        @Data
        public static class ExternalAuthProperties {
            
            /**
             * External auth provider type.
             * Options: none, keycloak, auth0, okta
             * Default is none.
             */
            @NotNull
            private ExternalAuthType type = ExternalAuthType.NONE;
            
            /**
             * External auth provider server URL.
             */
            private String serverUrl;
            
            /**
             * Realm name (for Keycloak).
             */
            private String realm;
            
            /**
             * Client ID.
             */
            private String clientId;
            
            /**
             * Client secret.
             */
            private String clientSecret;
            
            /**
             * External auth type enum.
             */
            public enum ExternalAuthType {
                NONE,
                KEYCLOAK,
                AUTH0,
                OKTA
            }
        }
    }
    
    @Data
    public static class CacheProperties {
        /**
         * 캐시 활성화 여부
         */
        @NotNull
        private Boolean enabled = true;
        
        /**
         * 캐시 타입 (caffeine, redis)
         */
        private String type = "caffeine";
        
        /**
         * Caffeine 캐시 설정
         */
        private CaffeineProperties caffeine = new CaffeineProperties();
        
        @Data
        public static class CaffeineProperties {
            /**
             * 최대 캐시 크기
             */
            private long maximumSize = 10000;
            
            /**
             * TTL (초)
             */
            private long expireAfterWriteSeconds = 900; // 15분
            
            /**
             * 통계 수집 여부
             */
            @NotNull
            private Boolean recordStats = true;
        }
    }
    
    @Data
    public static class TokenProvider {
        /**
         * 토큰 제공자 타입 (keycloak, jwt)
         * 기본값: jwt
         */
        private String provider = "jwt";
        
        /**
         * Keycloak 설정
         */
        private KeycloakProperties keycloak = new KeycloakProperties();
        
        /**
         * JWT 설정
         */
        private JwtProperties jwt = new JwtProperties();
        
        @Data
        public static class KeycloakProperties {
            @NotNull
            private Boolean enabled = true;
            private String serverUrl;
            private String realm;
            private String clientId;
            private String clientSecret;
        }
        
        @Data
        public static class JwtProperties {
            /**
             * Enable JWT authentication.
             * Default is true.
             */
            @NotNull
            private Boolean enabled = true;
            
            /**
             * JWT secret key for signing tokens.
             * Default is a generated secret for development purposes.
             * IMPORTANT: Change this in production!
             */
            private String secret = "default-jwt-secret-for-development-change-this-in-production";
            
            /**
             * JWT access token expiration time in seconds.
             * Default is 3600 seconds (1 hour).
             */
            @NotNull
            @Min(60) // Minimum 1 minute
            private Integer accessTokenExpiration = 3600;
            
            /**
             * JWT refresh token expiration time in seconds.
             * Default is 604800 seconds (7 days).
             */
            @NotNull
            @Min(60) // Minimum 1 minute
            private Integer refreshTokenExpiration = 604800;
            
            /**
             * Token issuer.
             * Default is "security-starter".
             */
            @NotBlank
            private String issuer = "security-starter";
            
            /**
             * Paths to exclude from JWT authentication.
             * These paths will not require JWT tokens.
             */
            private List<String> excludedPaths = new ArrayList<>();
            
            /**
             * Algorithm to use for JWT signing.
             * Default is HS256.
             */
            @NotBlank
            private String algorithm = "HS256";
            
            /**
             * Token prefix in Authorization header.
             * Default is "Bearer ".
             */
            @NotBlank
            private String tokenPrefix = "Bearer ";
            
            /**
             * Header name for JWT token.
             * Default is "Authorization".
             */
            @NotBlank
            private String headerName = "Authorization";
            
            /**
             * Get access token expiration as Duration.
             * 
             * @return access token expiration duration
             */
            public Duration getAccessTokenExpirationDuration() {
                return Duration.ofSeconds(accessTokenExpiration);
            }
            
            /**
             * Get refresh token expiration as Duration.
             * 
             * @return refresh token expiration duration
             */
            public Duration getRefreshTokenExpirationDuration() {
                return Duration.ofSeconds(refreshTokenExpiration);
            }
        }
    }
    
    @Data
    public static class RateLimitProperties {
        /**
         * Enable rate limiting.
         * Default is false.
         */
        @NotNull
        private Boolean enabled = false;
        
        /**
         * Default rate limit per time window.
         * Default is 100 requests.
         */
        @NotNull
        @Min(1)
        private Integer defaultLimit = 100;
        
        /**
         * Time window for rate limiting in seconds.
         * Default is 60 seconds (1 minute).
         */
        @NotNull
        @Min(1)
        private Integer timeWindow = 60;
        
        /**
         * Rate limit per IP address.
         * If not set, uses defaultLimit.
         */
        @Min(1)
        private Integer perIpLimit;
        
        /**
         * Rate limit per user.
         * If not set, uses defaultLimit.
         */
        @Min(1)
        private Integer perUserLimit;
        
        /**
         * Rate limit per API endpoint.
         * If not set, uses defaultLimit.
         */
        @Min(1)
        private Integer perEndpointLimit;
        
        /**
         * Strategy for rate limiting.
         * Options: SLIDING_WINDOW, FIXED_WINDOW, TOKEN_BUCKET
         * Default is SLIDING_WINDOW.
         */
        @NotNull
        private RateLimitStrategy strategy = RateLimitStrategy.SLIDING_WINDOW;
        
        /**
         * Enable distributed rate limiting using Redis.
         * Default is false.
         */
        @NotNull
        private Boolean distributed = false;
        
        /**
         * Key prefix for rate limit storage.
         * Default is "rate_limit:".
         */
        @NotNull
        private String keyPrefix = "rate_limit:";
        
        /**
         * Rate limit strategy enum.
         */
        public enum RateLimitStrategy {
            SLIDING_WINDOW,
            FIXED_WINDOW,
            TOKEN_BUCKET
        }
        
        /**
         * Get effective per IP limit.
         */
        public Integer getPerIpLimit() {
            return perIpLimit != null ? perIpLimit : defaultLimit;
        }
        
        /**
         * Get effective per user limit.
         */
        public Integer getPerUserLimit() {
            return perUserLimit != null ? perUserLimit : defaultLimit;
        }
        
        /**
         * Get effective per endpoint limit.
         */
        public Integer getPerEndpointLimit() {
            return perEndpointLimit != null ? perEndpointLimit : defaultLimit;
        }
        
        /**
         * Get time window as Duration.
         * 
         * @return time window duration
         */
        public Duration getTimeWindowDuration() {
            return Duration.ofSeconds(timeWindow);
        }
    }
    
    @Data
    public static class IpRestrictionProperties {
        /**
         * Enable IP restriction.
         * Default is false.
         */
        @NotNull
        private Boolean enabled = false;
        
        /**
         * IP restriction mode.
         * WHITELIST: Only allow IPs in the list
         * BLACKLIST: Block IPs in the list
         * Default is WHITELIST.
         */
        @NotNull
        private IpRestrictionMode mode = IpRestrictionMode.WHITELIST;
        
        /**
         * List of allowed IP addresses (for WHITELIST mode).
         * Supports CIDR notation (e.g., 192.168.0.0/16).
         */
        private List<String> allowedIps = new ArrayList<>();
        
        /**
         * List of blocked IP addresses (for BLACKLIST mode).
         * Supports CIDR notation (e.g., 192.168.0.0/16).
         */
        private List<String> blockedIps = new ArrayList<>();
        
        /**
         * Always allow localhost connections.
         * Default is true.
         */
        @NotNull
        private Boolean allowLocalhost = true;
        
        /**
         * Check X-Forwarded-For header for client IP.
         * Use with caution, only enable if behind a trusted proxy.
         * Default is false.
         */
        @NotNull
        private Boolean checkForwardedHeader = false;
        
        /**
         * Header name to check for client IP when behind proxy.
         * Default is "X-Real-IP".
         */
        @NotNull
        private String clientIpHeader = "X-Real-IP";
        
        /**
         * Maximum number of IPs to cache for performance.
         * Default is 10000.
         */
        @NotNull
        private Integer cacheSize = 10000;
        
        /**
         * Cache TTL in seconds.
         * Default is 3600 (1 hour).
         */
        @NotNull
        private Integer cacheTtl = 3600;
        
        /**
         * IP restriction mode enum.
         */
        public enum IpRestrictionMode {
            WHITELIST,
            BLACKLIST
        }
        
        /**
         * Get effective IP list based on mode.
         * 
         * @return list of IPs to check
         */
        public List<String> getEffectiveIpList() {
            return mode == IpRestrictionMode.WHITELIST ? allowedIps : blockedIps;
        }
    }
    
    @Data
    public static class HeadersProperties {
        /**
         * Enable security headers.
         * Default is true.
         */
        @NotNull
        private Boolean enabled = true;
        
        /**
         * X-Frame-Options header value.
         * Options: DENY, SAMEORIGIN, ALLOW-FROM
         * Default is DENY.
         */
        @NotNull
        private String frameOptions = "DENY";
        
        /**
         * X-Content-Type-Options header value.
         * Default is "nosniff".
         */
        @NotNull
        private String contentTypeOptions = "nosniff";
        
        /**
         * X-XSS-Protection header value.
         * Default is "1; mode=block".
         */
        @NotNull
        private String xssProtection = "1; mode=block";
        
        /**
         * Strict-Transport-Security header value.
         * Default is "max-age=31536000; includeSubDomains".
         */
        @NotNull
        private String hsts = "max-age=31536000; includeSubDomains";
        
        /**
         * Content-Security-Policy header value.
         * Default is "default-src 'self'".
         */
        @NotNull
        private String contentSecurityPolicy = "default-src 'self'";
        
        /**
         * Referrer-Policy header value.
         * Default is "no-referrer-when-downgrade".
         */
        @NotNull
        private String referrerPolicy = "no-referrer-when-downgrade";
        
        /**
         * Feature-Policy header value.
         * Default is "geolocation 'none'; microphone 'none'; camera 'none'".
         */
        @NotNull
        private String featurePolicy = "geolocation 'none'; microphone 'none'; camera 'none'";
        
        /**
         * Enable HSTS header.
         * Default is true.
         */
        @NotNull
        private Boolean hstsEnabled = true;
        
        /**
         * Enable CSP header.
         * Default is true.
         */
        @NotNull
        private Boolean cspEnabled = true;
    }
}