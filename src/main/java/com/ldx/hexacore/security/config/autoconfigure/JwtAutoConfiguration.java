package com.ldx.hexacore.security.config.autoconfigure;

import com.ldx.hexacore.security.auth.adapter.inbound.filter.JwtAuthenticationFilter;
import com.ldx.hexacore.security.auth.adapter.inbound.filter.JwtAuthenticationEntryPoint;
import com.ldx.hexacore.security.config.properties.SecurityStarterProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * JWT 자동 구성
 * 
 * 이 구성은 세 가지 전략을 제공합니다:
 * 
 * 1. ServletFilter 전략 (jwt.strategy=servlet-filter)
 *    - Spring Security와 독립적으로 JWT 처리
 *    - 부모 프로젝트의 SecurityFilterChain과 완전히 독립적
 * 
 * 2. Security Integration 전략 (jwt.strategy=security-integration, 기본값)
 *    - JWT 필터를 Bean으로 제공
 *    - 부모가 SecurityFilterChain 정의 시 자동으로 백오프
 *    - 부모는 JwtAuthenticationFilter Bean을 주입받아 사용 가능
 * 
 * 3. Manual 전략 (jwt.strategy=manual)
 *    - JWT 필터만 Bean으로 제공
 *    - SecurityFilterChain 생성하지 않음
 *    - 부모가 완전히 제어
 */
@AutoConfiguration(before = SecurityAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(SecurityStarterProperties.class)
public class JwtAutoConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAutoConfiguration.class);
    
    /**
     * 전략 1: ServletFilter로 JWT 처리 (Spring Security 외부)
     */
    @Configuration
    @ConditionalOnProperty(
        prefix = "security-starter.jwt",
        name = "strategy",
        havingValue = "servlet-filter"
    )
    public static class ServletFilterStrategy {
        
        @Bean
        public FilterRegistrationBean<JwtAuthenticationFilter> jwtServletFilterRegistration(
                JwtAuthenticationFilter jwtFilter,
                SecurityStarterProperties properties) {
            
            logger.info("🚀 JWT Strategy: ServletFilter (Spring Security 독립 실행)");
            
            FilterRegistrationBean<JwtAuthenticationFilter> registration = 
                new FilterRegistrationBean<>();
            registration.setFilter(jwtFilter);
            registration.addUrlPatterns("/*");
            registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 100);
            registration.setName("jwtAuthenticationServletFilter");
            
            logger.info("✅ JWT가 ServletFilter로 등록됨 - 모든 요청에서 JWT 처리");
            
            return registration;
        }
    }
    
    /**
     * 전략 2: Security Integration (기본값)
     * JWT 필터를 Bean으로 제공하고, 필요시 fallback SecurityFilterChain 생성
     */
    @Configuration
    @ConditionalOnProperty(
        prefix = "security-starter.jwt",
        name = "strategy",
        havingValue = "security-integration",
        matchIfMissing = true  // 기본 전략
    )
    public static class SecurityIntegrationStrategy {
        
        /**
         * Fallback SecurityFilterChain
         * 부모 프로젝트가 정의하지 않은 경우에만 생성
         */
        @Bean
        @ConditionalOnMissingBean(SecurityFilterChain.class)
        public SecurityFilterChain jwtSecurityFilterChain(
                HttpSecurity http,
                JwtAuthenticationFilter jwtFilter,
                JwtAuthenticationEntryPoint jwtEntryPoint,
                SecurityStarterProperties properties) throws Exception {
            
            logger.info("🎯 JWT Strategy: Security Integration (Fallback SecurityFilterChain 생성)");
            logger.info("ℹ️ 부모 프로젝트에서 SecurityFilterChain을 정의하면 이 Bean은 생성되지 않습니다.");
            
            http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> 
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    // JWT 제외 경로 적용
                    String[] excludePaths = properties.getFilter().getExcludePaths();
                    if (excludePaths != null && excludePaths.length > 0) {
                        auth.requestMatchers(excludePaths).permitAll();
                    }
                    auth.anyRequest().authenticated();
                })
                .exceptionHandling(exception -> 
                    exception.authenticationEntryPoint(jwtEntryPoint))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
            
            logger.info("✅ Fallback SecurityFilterChain 구성 완료 (JWT 필터 포함)");
            
            return http.build();
        }
    }
    
    /**
     * 전략 3: Manual 전략
     * JWT 필터만 제공, SecurityFilterChain 생성 안 함
     */
    @Configuration
    @ConditionalOnProperty(
        prefix = "security-starter.jwt",
        name = "strategy",
        havingValue = "manual"
    )
    public static class ManualStrategy {
        // JWT 필터만 Bean으로 제공, SecurityFilterChain은 생성하지 않음
        // 부모 프로젝트가 완전히 제어
        
        public ManualStrategy() {
            logger.info("🔧 JWT Strategy: Manual (JWT 필터만 제공, 부모가 완전 제어)");
        }
    }
    
    /**
     * JWT 헬퍼 클래스
     * 부모 프로젝트에서 쉽게 JWT를 통합할 수 있도록 지원
     */
    @Bean
    @ConditionalOnBean(JwtAuthenticationFilter.class)
    public JwtSecurityHelper jwtSecurityHelper(
            JwtAuthenticationFilter jwtFilter,
            JwtAuthenticationEntryPoint jwtEntryPoint,
            SecurityStarterProperties properties) {
        return new JwtSecurityHelper(jwtFilter, jwtEntryPoint, properties);
    }
    
    /**
     * JWT Security 통합 헬퍼
     * 부모 프로젝트에서 사용 예시:
     * 
     * @Bean
     * public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtSecurityHelper jwtHelper) {
     *     jwtHelper.configureJwt(http);  // JWT 설정 적용
     *     
     *     http.authorizeHttpRequests(auth -> auth
     *         .requestMatchers("/custom/**").hasRole("USER")
     *         .anyRequest().authenticated()
     *     );
     *     
     *     return http.build();
     * }
     */
    public static class JwtSecurityHelper {
        
        private final JwtAuthenticationFilter jwtFilter;
        private final JwtAuthenticationEntryPoint jwtEntryPoint;
        private final SecurityStarterProperties properties;
        
        public JwtSecurityHelper(JwtAuthenticationFilter jwtFilter,
                                JwtAuthenticationEntryPoint jwtEntryPoint,
                                SecurityStarterProperties properties) {
            this.jwtFilter = jwtFilter;
            this.jwtEntryPoint = jwtEntryPoint;
            this.properties = properties;
        }
        
        /**
         * HttpSecurity에 JWT 설정 적용
         */
        public void configureJwt(HttpSecurity http) throws Exception {
            http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> 
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> 
                    exception.authenticationEntryPoint(jwtEntryPoint))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
            
            logger.info("✅ JWT 설정이 HttpSecurity에 적용됨");
        }
        
        /**
         * JWT 제외 경로 적용
         */
        public void configureExcludePaths(HttpSecurity http) throws Exception {
            String[] excludePaths = properties.getFilter().getExcludePaths();
            if (excludePaths != null && excludePaths.length > 0) {
                http.authorizeHttpRequests(auth -> 
                    auth.requestMatchers(excludePaths).permitAll()
                );
                logger.info("✅ JWT 제외 경로 적용: {}", (Object) excludePaths);
            }
        }
        
        public JwtAuthenticationFilter getJwtFilter() {
            return jwtFilter;
        }
        
        public JwtAuthenticationEntryPoint getJwtEntryPoint() {
            return jwtEntryPoint;
        }
    }
}