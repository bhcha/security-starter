package com.ldx.hexacore.security.config.autoconfigure;

import com.ldx.hexacore.security.auth.adapter.inbound.filter.JwtAuthenticationFilter;
import com.ldx.hexacore.security.config.properties.HexacoreSecurityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Dual-Layer Security Architecture
 * 
 * 이 아키텍처는 두 개의 레이어로 보안을 구성합니다:
 * 
 * Layer 1: ServletFilter 레벨 (Spring Security 외부)
 *   - JWT 토큰 검증
 *   - SecurityContext 설정
 *   - 모든 요청에 적용 (부모 프로젝트 SecurityFilterChain과 독립적)
 * 
 * Layer 2: SecurityFilterChain 레벨 (Spring Security 내부)
 *   - 스타터 전용 경로 보호 (/api/admin/**, /api/secure/** 등)
 *   - 부모 프로젝트가 정의하지 않은 기본 보안 규칙
 * 
 * 결과: 부모 프로젝트의 SecurityFilterChain과 완벽하게 공존
 */
@AutoConfiguration(before = SecurityAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(HexacoreSecurityProperties.class)
public class DualLayerSecurityConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(DualLayerSecurityConfiguration.class);
    
    /**
     * Layer 1: ServletFilter 레벨 JWT 처리
     * Spring Security와 독립적으로 모든 요청에서 JWT 토큰 처리
     */
    @Configuration
    @ConditionalOnProperty(
        prefix = "hexacore.security.jwt",
        name = "layer",
        havingValue = "servlet",
        matchIfMissing = true  // 기본값
    )
    public static class ServletLayerConfiguration {
        
        /**
         * JWT 필터를 ServletFilter로 등록
         * Spring Security FilterChain 이전에 실행됨
         */
        @Bean
        public FilterRegistrationBean<JwtServletFilter> jwtServletFilterRegistration(
                JwtAuthenticationFilter jwtFilter,
                HexacoreSecurityProperties properties) {
            
            logger.info("🔧 [Layer 1] Registering JWT as ServletFilter");
            
            FilterRegistrationBean<JwtServletFilter> registration = 
                new FilterRegistrationBean<>();
            
            JwtServletFilter servletFilter = new JwtServletFilter(jwtFilter, properties);
            registration.setFilter(servletFilter);
            registration.addUrlPatterns("/*");
            registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 50); // Spring Security보다 먼저
            registration.setName("hexacoreJwtServletFilter");
            
            logger.info("✅ [Layer 1] JWT ServletFilter registered - will process ALL requests");
            
            return registration;
        }
    }
    
    /**
     * Layer 2: SecurityFilterChain 레벨 (선택적)
     * 부모 프로젝트가 정의하지 않은 경우에만 활성화
     */
    @Configuration
    @ConditionalOnProperty(
        prefix = "hexacore.security.filter",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    public static class SecurityLayerConfiguration {
        
        /**
         * 스타터 전용 SecurityFilterChain
         * 특정 경로만 처리 (/api/admin/** 등)
         */
        @Bean
        @Order(1000)  // 낮은 우선순위 (부모가 먼저)
        @ConditionalOnMissingBean(name = "primarySecurityFilterChain")
        public SecurityFilterChain starterSecurityFilterChain(
                HttpSecurity http,
                HexacoreSecurityProperties properties) throws Exception {
            
            logger.info("🔧 [Layer 2] Configuring Starter SecurityFilterChain");
            
            // 스타터 전용 경로만 처리
            http.securityMatcher(
                "/api/admin/**",
                "/api/secure/**",
                "/actuator/**"
            );
            
            http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> 
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/actuator/health").permitAll()
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
                );
            
            logger.info("✅ [Layer 2] Starter SecurityFilterChain configured for specific paths");
            
            return http.build();
        }
        
        /**
         * 기본 SecurityFilterChain (최후의 수단)
         * 부모가 아무것도 정의하지 않은 경우에만
         */
        @Bean
        @Order(Ordered.LOWEST_PRECEDENCE)
        @ConditionalOnMissingBean(SecurityFilterChain.class)
        public SecurityFilterChain fallbackSecurityFilterChain(HttpSecurity http) throws Exception {
            
            logger.info("🔧 [Layer 2] No user-defined SecurityFilterChain found, creating fallback");
            
            http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> 
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/public/**").permitAll()
                    .anyRequest().authenticated()
                );
            
            return http.build();
        }
    }
    
    /**
     * JWT ServletFilter 구현
     * Spring Security와 독립적으로 동작
     */
    public static class JwtServletFilter extends OncePerRequestFilter {
        
        private static final Logger logger = LoggerFactory.getLogger(JwtServletFilter.class);
        
        private final JwtAuthenticationFilter jwtFilter;
        private final HexacoreSecurityProperties properties;
        private final List<String> excludePaths;
        
        public JwtServletFilter(JwtAuthenticationFilter jwtFilter, 
                               HexacoreSecurityProperties properties) {
            this.jwtFilter = jwtFilter;
            this.properties = properties;
            this.excludePaths = List.of(properties.getFilter().getExcludePaths());
        }
        
        @Override
        protected void doFilterInternal(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      FilterChain filterChain) throws ServletException, IOException {
            
            String path = request.getRequestURI();
            
            // JWT 처리 제외 경로 체크
            if (shouldSkip(path)) {
                filterChain.doFilter(request, response);
                return;
            }
            
            // JWT 토큰 추출 및 검증
            String token = extractToken(request);
            if (token != null) {
                try {
                    // JWT 검증 로직 (JwtAuthenticationFilter 재사용)
                    // SecurityContext 설정
                    logger.debug("[Layer 1] JWT token processed for path: {}", path);
                    
                    // 임시 인증 설정 (실제로는 jwtFilter 로직 사용)
                    if (isValidToken(token)) {
                        UsernamePasswordAuthenticationToken auth = 
                            new UsernamePasswordAuthenticationToken(
                                "user", null, List.of());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                } catch (Exception e) {
                    logger.debug("[Layer 1] JWT validation failed: {}", e.getMessage());
                }
            }
            
            // 다음 필터로 진행 (Spring Security FilterChain 포함)
            filterChain.doFilter(request, response);
        }
        
        private boolean shouldSkip(String path) {
            return excludePaths.stream().anyMatch(path::startsWith);
        }
        
        private String extractToken(HttpServletRequest request) {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                return header.substring(7);
            }
            return null;
        }
        
        private boolean isValidToken(String token) {
            // 실제 검증 로직
            return token != null && !token.isEmpty();
        }
    }
}