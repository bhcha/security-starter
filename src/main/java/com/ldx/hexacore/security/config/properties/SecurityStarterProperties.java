package com.ldx.hexacore.security.config.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Security Starter의 메인 설정 프로퍼티입니다.
 * 
 * <p>application.yml에서 security-starter.* 설정을 관리합니다.
 * Spring Boot Starter 표준 아키텍처 가이드라인을 준수합니다.
 * 
 * <p>핵심 기능:
 * - Mode 기반 아키텍처 지원 (Traditional/Hexagonal)
 * - FeatureToggle 기반 기능 제어
 * - Zero Configuration 원칙 준수
 * - 기존 설정과의 완전한 호환성
 * 
 * @author security-starter
 * @since 1.9.0
 */
@Data
@ConfigurationProperties(prefix = "security-starter")
@Validated
public class SecurityStarterProperties {
    
    /**
     * 전체 스타터 활성화 여부
     * 기본값: true (Zero Configuration)
     */
    @NotNull
    private Boolean enabled = true;
    
    /**
     * 아키텍처 모드
     * traditional: 모든 레이어에서 자유 사용
     * hexagonal: Domain Layer에서 사용 제한
     */
    private Mode mode = Mode.TRADITIONAL;
    
    // === 표준 가이드라인 준수 FeatureToggle 구조 ===
    
    /**
     * 기능별 토글 - 인증 기능
     */
    @NestedConfigurationProperty
    private FeatureToggle authenticationToggle = new FeatureToggle(true);
    
    /**
     * 기능별 토글 - 세션 관리
     */
    @NestedConfigurationProperty
    private FeatureToggle sessionToggle = new FeatureToggle(true);
    
    /**
     * 기능별 토글 - JWT 토큰
     */
    @NestedConfigurationProperty
    private FeatureToggle jwtToggle = new FeatureToggle(true);
    
    /**
     * 기능별 토글 - Rate Limiting (기본 OFF)
     */
    @NestedConfigurationProperty
    private FeatureToggle rateLimitToggle = new FeatureToggle(false);
    
    /**
     * 기능별 토글 - IP 제한 (기본 OFF)
     */
    @NestedConfigurationProperty
    private FeatureToggle ipRestrictionToggle = new FeatureToggle(false);
    
    /**
     * 기능별 토글 - 보안 헤더 (기본 ON)
     */
    @NestedConfigurationProperty
    private FeatureToggle headersToggle = new FeatureToggle(true);
    
    // === 기존 설정 (하위 호환성 유지) ===
    
    /**
     * JWT 통합 전략 설정
     */
    @Valid
    @NestedConfigurationProperty
    private JwtIntegration jwt = new JwtIntegration();
    
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
         * 인증 제외 경로 목록 (설정 파일에서 지정해야 함)
         */
        private String[] excludePaths = {};
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
        
        public String getProvider() {
            return provider;
        }
        
        public void setProvider(String provider) {
            this.provider = provider;
        }
        
        public KeycloakProperties getKeycloak() {
            return keycloak;
        }
        
        public void setKeycloak(KeycloakProperties keycloak) {
            this.keycloak = keycloak;
        }
        
        public JwtProperties getJwt() {
            return jwt;
        }
        
        public void setJwt(JwtProperties jwt) {
            this.jwt = jwt;
        }
        
        @Data
        @Validated
        public static class KeycloakProperties {
            @NotNull
            private Boolean enabled = true;
            
            @NotEmpty(message = "Keycloak 서버 URL은 필수입니다")
            @Pattern(regexp = "^https?://.*", message = "올바른 URL 형식이어야 합니다 (http:// 또는 https://)")
            private String serverUrl;
            
            @NotEmpty(message = "Realm 이름은 필수입니다")
            @Size(min = 1, max = 100, message = "Realm 이름은 1-100자 사이여야 합니다")
            private String realm;
            
            @NotEmpty(message = "Client ID는 필수입니다")
            @Size(min = 1, max = 100, message = "Client ID는 1-100자 사이여야 합니다")
            private String clientId;
            
            // Optional for public clients
            @Size(max = 500, message = "Client Secret은 500자를 초과할 수 없습니다")
            private String clientSecret;
            
            /**
             * Whether this is a public client (no client secret required).
             * Default is false (confidential client).
             */
            @NotNull
            private Boolean publicClient = false;
            
            /**
             * OAuth2 scopes to request. Default includes openid profile email.
             */
            @NotBlank(message = "OAuth2 scopes는 필수입니다")
            private String scopes = "openid profile email";
            
            /**
             * Grant type for token requests. Default is password.
             */
            @NotBlank(message = "Grant type은 필수입니다")
            @Pattern(regexp = "password|authorization_code|client_credentials", 
                    message = "지원되는 grant type: password, authorization_code, client_credentials")
            private String grantType = "password";
            
            /**
             * 프로덕션 환경에서 HTTPS 사용 검증
             */
            @AssertTrue(message = "프로덕션 환경에서는 HTTPS를 사용해야 합니다")
            public boolean isValidServerUrlForProduction() {
                if (isProductionEnvironment() && serverUrl != null) {
                    return serverUrl.startsWith("https://") && !serverUrl.contains("localhost");
                }
                return true;
            }
            
            /**
             * 서버 URL이 설정되어 있고 enabled=true인 경우의 일관성 검증
             */
            @AssertTrue(message = "Keycloak이 활성화된 경우 모든 필수 설정이 있어야 합니다")
            public boolean isValidConfiguration() {
                if (enabled) {
                    return serverUrl != null && !serverUrl.trim().isEmpty() &&
                           realm != null && !realm.trim().isEmpty() &&
                           clientId != null && !clientId.trim().isEmpty();
                }
                return true;
            }
            
            /**
             * 프로덕션 환경 여부 확인
             */
            private boolean isProductionEnvironment() {
                String profile = System.getProperty("spring.profiles.active", "");
                String env = System.getenv("SPRING_PROFILES_ACTIVE");
                if (env != null) {
                    profile = env;
                }
                return profile.contains("prod") || profile.contains("production");
            }
        }
        
        @Data
        @Validated
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
            @NotEmpty(message = "JWT secret은 필수입니다")
            @Size(min = 32, max = 512, message = "JWT secret은 32-512자 사이여야 합니다")
            private String secret = "default-jwt-secret-for-development-change-this-in-production";
            
            /**
             * JWT access token expiration time in seconds.
             * Default is 3600 seconds (1 hour).
             */
            @NotNull
            @Min(value = 300, message = "액세스 토큰 만료 시간은 최소 5분(300초) 이상이어야 합니다")
            @Max(value = 86400, message = "액세스 토큰 만료 시간은 최대 24시간(86400초) 이하를 권장합니다")
            private Integer accessTokenExpiration = 3600;
            
            /**
             * JWT refresh token expiration time in seconds.
             * Default is 604800 seconds (7 days).
             */
            @NotNull
            @Min(value = 3600, message = "리프레시 토큰 만료 시간은 최소 1시간(3600초) 이상이어야 합니다")
            @Max(value = 2592000, message = "리프레시 토큰 만료 시간은 최대 30일(2592000초) 이하를 권장합니다")
            private Integer refreshTokenExpiration = 604800;
            
            /**
             * Token issuer.
             * Default is "security-starter".
             */
            @NotBlank(message = "토큰 발행자(issuer)는 필수입니다")
            @Size(min = 3, max = 100, message = "토큰 발행자는 3-100자 사이여야 합니다")
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
            @NotBlank(message = "알고리즘은 필수입니다")
            @Pattern(regexp = "HS256|HS384|HS512|RS256|RS384|RS512", 
                     message = "지원하는 알고리즘: HS256, HS384, HS512, RS256, RS384, RS512")
            private String algorithm = "HS256";
            
            /**
             * Token prefix in Authorization header.
             * Default is "Bearer ".
             */
            @NotBlank(message = "토큰 접두사는 필수입니다")
            private String tokenPrefix = "Bearer ";
            
            /**
             * Header name for JWT token.
             * Default is "Authorization".
             */
            @NotBlank(message = "헤더 이름은 필수입니다")
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
            
            /**
             * 프로덕션 환경에서 기본 secret 사용 여부 검증
             */
            @AssertTrue(message = "프로덕션 환경에서는 기본 secret을 사용할 수 없습니다")
            public boolean isValidSecretForProduction() {
                if (isProductionEnvironment()) {
                    return !secret.contains("default") && 
                           !secret.contains("example") && 
                           !secret.contains("test") &&
                           !secret.contains("development");
                }
                return true;
            }
            
            /**
             * 액세스 토큰과 리프레시 토큰 만료 시간의 논리적 관계 검증
             */
            @AssertTrue(message = "리프레시 토큰 만료 시간은 액세스 토큰 만료 시간보다 길어야 합니다")
            public boolean isValidTokenExpirationRelation() {
                return refreshTokenExpiration > accessTokenExpiration;
            }
            
            /**
             * 프로덕션 환경 여부 확인
             */
            private boolean isProductionEnvironment() {
                String profile = System.getProperty("spring.profiles.active", "");
                String env = System.getenv("SPRING_PROFILES_ACTIVE");
                if (env != null) {
                    profile = env;
                }
                return profile.contains("prod") || profile.contains("production");
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
    
    /**
     * JWT 통합 전략 설정
     */
    @Data
    public static class JwtIntegration {
        /**
         * JWT 기능 활성화 여부
         */
        @NotNull
        private Boolean enabled = true;
        
        /**
         * JWT 통합 전략
         * - security-integration (기본값): SecurityFilterChain과 통합, 부모가 정의하면 백오프
         * - servlet-filter: Spring Security와 독립적으로 ServletFilter로 동작
         * - manual: JWT 필터만 Bean으로 제공, 부모가 완전히 제어
         */
        @NotNull
        @Pattern(regexp = "security-integration|servlet-filter|manual",
                message = "JWT strategy must be one of: security-integration, servlet-filter, manual")
        private String strategy = "security-integration";
        
        /**
         * ServletFilter 전략 사용 시 우선순위
         */
        @Min(-100)
        @Max(100)
        private Integer filterOrder = 50;
        
        /**
         * 자동 주입 활성화 여부 (구 설정, deprecated)
         */
        @Deprecated
        private Boolean autoInject = false;
    }
    
    // === 편의 메서드들 (조건 로직을 Properties 내부로 이동) ===
    
    /**
     * 인증 기능 활성화 여부
     * 루트 enabled && authenticationToggle.enabled 확인
     */
    public boolean isAuthenticationEnabled() {
        return enabled && authenticationToggle.isEnabled();
    }
    
    /**
     * 세션 관리 기능 활성화 여부
     * 루트 enabled && sessionToggle.enabled && 기존 session.enabled 확인
     */
    public boolean isSessionEnabled() {
        return enabled && sessionToggle.isEnabled() && session.getEnabled();
    }
    
    /**
     * JWT 기능 활성화 여부
     * 루트 enabled && jwtToggle.enabled && 기존 jwt.enabled 확인
     */
    public boolean isJwtEnabled() {
        return enabled && jwtToggle.isEnabled() && jwt.getEnabled();
    }
    
    /**
     * Rate Limiting 기능 활성화 여부
     * 루트 enabled && rateLimitToggle.enabled && 기존 rateLimit.enabled 확인
     */
    public boolean isRateLimitEnabled() {
        return enabled && rateLimitToggle.isEnabled() && rateLimit.getEnabled();
    }
    
    /**
     * IP 제한 기능 활성화 여부
     * 루트 enabled && ipRestrictionToggle.enabled && 기존 ipRestriction.enabled 확인
     */
    public boolean isIpRestrictionEnabled() {
        return enabled && ipRestrictionToggle.isEnabled() && ipRestriction.getEnabled();
    }
    
    /**
     * 보안 헤더 기능 활성화 여부
     * 루트 enabled && headersToggle.enabled && 기존 headers.enabled 확인
     */
    public boolean isHeadersEnabled() {
        return enabled && headersToggle.isEnabled() && headers.getEnabled();
    }
    
    /**
     * 필터 기능 활성화 여부 (기존 호환성)
     */
    public boolean isFilterEnabled() {
        return enabled && filter.getEnabled();
    }
    
    /**
     * 캐시 기능 활성화 여부 (기존 호환성)
     */
    public boolean isCacheEnabled() {
        return enabled && cache.getEnabled();
    }
    
    
    /**
     * Keycloak 기능 활성화 여부
     */
    public boolean isKeycloakEnabled() {
        return enabled && tokenProvider.getKeycloak().getEnabled();
    }
    
    /**
     * 세션 관리 기능 활성화 여부 (별칭)
     */
    public boolean isSessionManagementEnabled() {
        return isSessionEnabled();
    }
    
    /**
     * 보안 헤더 기능 활성화 여부 (별칭)
     */
    public boolean isSecurityHeadersEnabled() {
        return isHeadersEnabled();
    }
    
    // === Getter 메서드들 ===
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public Mode getMode() {
        return mode;
    }
    
    public TokenProvider getTokenProvider() {
        return tokenProvider;
    }
    
    public AuthFilterProperties getFilter() {
        return filter;
    }
    
    public SessionProperties getSession() {
        return session;
    }
    
    
    public CacheProperties getCache() {
        return cache;
    }
    
    public HeadersProperties getHeaders() {
        return headers;
    }
    
    public FeatureToggle getRateLimit() {
        return rateLimitToggle;
    }
    
    public FeatureToggle getIpRestriction() {
        return ipRestrictionToggle;
    }
    
    // === 내부 클래스 정의 ===
    
    /**
     * 아키텍처 모드 정의
     */
    public enum Mode {
        /**
         * 전통적 MVC 아키텍처 - 모든 레이어에서 security-starter 자유 사용
         */
        TRADITIONAL,
        
        /**
         * 헥사고날 아키텍처 - Domain Layer에서 security-starter 사용 제한
         * Application Layer 이상에서만 사용 권장
         */
        HEXAGONAL
    }
    
    /**
     * 기능별 토글 클래스
     * 각 기능의 활성화/비활성화를 제어합니다.
     */
    @Data
    public static class FeatureToggle {
        
        /**
         * 기능 활성화 여부
         */
        private boolean enabled;
        
        /**
         * 기본 생성자
         */
        public FeatureToggle() {
            this.enabled = false;
        }
        
        /**
         * 기본값 지정 생성자
         * @param defaultEnabled 기본 활성화 여부
         */
        public FeatureToggle(boolean defaultEnabled) {
            this.enabled = defaultEnabled;
        }
        
        /**
         * 활성화 여부 반환
         * @return 활성화 여부
         */
        public boolean isEnabled() {
            return enabled;
        }
        
        /**
         * 활성화 여부 반환 (getEnabled 형식)
         * @return 활성화 여부
         */
        public Boolean getEnabled() {
            return enabled;
        }
        
        /**
         * 활성화 여부 설정
         * @param enabled 활성화 여부
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        @Override
        public String toString() {
            return "FeatureToggle{enabled=" + enabled + "}";
        }
    }
}