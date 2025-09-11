package com.ldx.hexacore.security.config.autoconfigure;

import com.ldx.hexacore.security.config.properties.SecurityStarterProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Mode별 차별화된 Security Filter 설정
 * 
 * <p>Traditional/Hexagonal 모드에 따라 다른 보안 정책을 적용합니다.
 * 
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@ConditionalOnClass(SecurityFilterChain.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ModeAwareSecurityFilterConfiguration {
    
    private final SecurityStarterProperties properties;
    
    /**
     * Mode별 SecurityFilterChain 생성
     */
    @Bean
    @ConditionalOnMissingBean(name = "modeAwareSecurityFilterChain")
    public SecurityFilterChain modeAwareSecurityFilterChain(HttpSecurity http) throws Exception {
        log.info("Mode별 Security Filter 설정 중: {}", properties.getMode());
        
        switch (properties.getMode()) {
            case TRADITIONAL:
                configureTraditionalMode(http);
                break;
            case HEXAGONAL:
                configureHexagonalMode(http);
                break;
            default:
                configureTraditionalMode(http);
                break;
        }
        
        return http.build();
    }
    
    /**
     * Traditional 모드 설정
     * - 간단한 보안 설정
     * - 빠른 개발 중심
     */
    private void configureTraditionalMode(HttpSecurity http) throws Exception {
        log.info("📌 Traditional 모드 Security 설정 적용");
        
        http
            // CSRF 비활성화 (개발 편의성)
            .csrf(csrf -> csrf.disable())
            
            // 세션 정책 - Traditional은 세션 사용 가능
            .sessionManagement(session -> {
                if (properties.isSessionEnabled()) {
                    session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
                    log.debug("  - 세션 관리: 활성화 (IF_REQUIRED)");
                } else {
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                    log.debug("  - 세션 관리: 비활성화 (STATELESS)");
                }
            })
            
            // 권한 설정 - 기본적으로 열려있음
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/public/**", "/health/**", "/actuator/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            
            // Exception 처리 - 간단한 응답
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Unauthorized\",\"mode\":\"TRADITIONAL\"}");
                })
            );
        
        // JWT 필터 추가 (활성화된 경우)
        if (properties.isJwtEnabled()) {
            log.debug("  - JWT 필터: 활성화");
            // JWT 필터는 별도 Configuration에서 추가됨
        }
        
        log.info("  ✓ Traditional 모드 설정 완료");
    }
    
    /**
     * Hexagonal 모드 설정
     * - 엄격한 보안 정책
     * - 레이어별 접근 제어
     */
    private void configureHexagonalMode(HttpSecurity http) throws Exception {
        log.info("🔷 Hexagonal 모드 Security 설정 적용");
        
        http
            // CSRF 보호 활성화 (Production 권장)
            .csrf(csrf -> {
                if (isProductionProfile()) {
                    csrf.ignoringRequestMatchers("/api/public/**");
                    log.debug("  - CSRF 보호: 활성화 (Production)");
                } else {
                    csrf.disable();
                    log.debug("  - CSRF 보호: 비활성화 (Development)");
                }
            })
            
            // 세션 정책 - Hexagonal은 기본적으로 Stateless
            .sessionManagement(session -> {
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                log.debug("  - 세션 관리: STATELESS (JWT 기반)");
            })
            
            // 권한 설정 - 엄격한 접근 제어
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/health/**", "/actuator/health/**").permitAll()
                
                // Domain Layer endpoints (제한적)
                .requestMatchers("/api/domain/**").denyAll()  // Domain은 직접 노출 안함
                
                // Application Layer endpoints
                .requestMatchers("/api/application/**").hasAnyRole("USER", "ADMIN")
                
                // Adapter Layer endpoints
                .requestMatchers("/api/adapter/**").hasRole("ADMIN")
                
                // Admin endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // 나머지는 인증 필요
                .anyRequest().authenticated()
            )
            
            // Exception 처리 - RFC 7807 Problem Details
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType("application/problem+json");
                    response.getWriter().write("""
                        {
                            "type": "https://hexacore.security/problems/unauthorized",
                            "title": "Unauthorized",
                            "status": 401,
                            "detail": "Authentication required",
                            "mode": "HEXAGONAL"
                        }
                        """);
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(403);
                    response.setContentType("application/problem+json");
                    response.getWriter().write("""
                        {
                            "type": "https://hexacore.security/problems/forbidden",
                            "title": "Forbidden",
                            "status": 403,
                            "detail": "Insufficient privileges",
                            "mode": "HEXAGONAL"
                        }
                        """);
                })
            )
            
            // 보안 헤더 강화
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny())
                .contentSecurityPolicy(csp -> 
                    csp.policyDirectives("default-src 'self'; frame-ancestors 'none';"))
                .httpStrictTransportSecurity(hsts -> 
                    hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
            );
        
        log.info("  ✓ Hexagonal 모드 설정 완료");
        log.info("  ✓ Port-Adapter 패턴 기반 접근 제어 활성화");
    }
    
    /**
     * Production 프로파일 확인
     */
    private boolean isProductionProfile() {
        String[] activeProfiles = System.getProperty("spring.profiles.active", "").split(",");
        for (String profile : activeProfiles) {
            if ("prod".equalsIgnoreCase(profile.trim()) || 
                "production".equalsIgnoreCase(profile.trim())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Mode별 기본 설정값 제공
     */
    @Bean
    @ConditionalOnMissingBean
    public ModeDefaults modeDefaults() {
        ModeDefaults defaults = new ModeDefaults();
        
        switch (properties.getMode()) {
            case TRADITIONAL:
                defaults.setDefaultSessionTimeout(1800); // 30분
                defaults.setDefaultTokenExpiry(3600);     // 1시간
                defaults.setStrictValidation(false);
                defaults.setEnableAuditLog(false);
                break;
                
            case HEXAGONAL:
                defaults.setDefaultSessionTimeout(900);   // 15분
                defaults.setDefaultTokenExpiry(1800);     // 30분
                defaults.setStrictValidation(true);
                defaults.setEnableAuditLog(true);
                break;
        }
        
        log.debug("Mode 기본값 설정: {}", defaults);
        return defaults;
    }
    
    /**
     * Mode별 기본 설정값 클래스
     */
    public static class ModeDefaults {
        private int defaultSessionTimeout;
        private int defaultTokenExpiry;
        private boolean strictValidation;
        private boolean enableAuditLog;
        
        // Getters and Setters
        public int getDefaultSessionTimeout() {
            return defaultSessionTimeout;
        }
        
        public void setDefaultSessionTimeout(int defaultSessionTimeout) {
            this.defaultSessionTimeout = defaultSessionTimeout;
        }
        
        public int getDefaultTokenExpiry() {
            return defaultTokenExpiry;
        }
        
        public void setDefaultTokenExpiry(int defaultTokenExpiry) {
            this.defaultTokenExpiry = defaultTokenExpiry;
        }
        
        public boolean isStrictValidation() {
            return strictValidation;
        }
        
        public void setStrictValidation(boolean strictValidation) {
            this.strictValidation = strictValidation;
        }
        
        public boolean isEnableAuditLog() {
            return enableAuditLog;
        }
        
        public void setEnableAuditLog(boolean enableAuditLog) {
            this.enableAuditLog = enableAuditLog;
        }
        
        @Override
        public String toString() {
            return "ModeDefaults{" +
                    "sessionTimeout=" + defaultSessionTimeout +
                    ", tokenExpiry=" + defaultTokenExpiry +
                    ", strictValidation=" + strictValidation +
                    ", auditLog=" + enableAuditLog +
                    '}';
        }
    }
}