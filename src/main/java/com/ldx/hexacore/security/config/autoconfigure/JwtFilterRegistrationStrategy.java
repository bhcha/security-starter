package com.ldx.hexacore.security.config.autoconfigure;

import com.ldx.hexacore.security.auth.adapter.inbound.filter.JwtAuthenticationFilter;
import com.ldx.hexacore.security.config.properties.SecurityStarterProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * JWT 필터 등록 전략
 * 
 * 전략 1: SecurityFilterChain이 없으면 직접 생성
 * 전략 2: SecurityFilterChain이 있으면 FilterRegistrationBean으로 등록
 * 
 * 이렇게 하면 사용자가 SecurityFilterChain을 정의해도 JWT 필터는 자동으로 적용됨
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(
    prefix = "security-starter.jwt",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class JwtFilterRegistrationStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtFilterRegistrationStrategy.class);
    
    /**
     * 전략 1: Spring Security 외부에서 필터 직접 등록
     * SecurityFilterChain과 독립적으로 동작
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "security-starter.jwt.registration",
        name = "strategy",
        havingValue = "servlet",
        matchIfMissing = false
    )
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(
            JwtAuthenticationFilter jwtFilter,
            SecurityStarterProperties properties) {
        
        logger.info("🔧 Registering JWT filter via FilterRegistrationBean (Servlet Container)");
        
        FilterRegistrationBean<JwtAuthenticationFilter> registration = 
            new FilterRegistrationBean<>();
        
        registration.setFilter(jwtFilter);
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 100);
        registration.setName("jwtAuthenticationFilter");
        
        // 제외 경로는 필터 내부에서 처리
        logger.info("✅ JWT filter registered independently from SecurityFilterChain");
        
        return registration;
    }
    
    /**
     * 전략 2: SecurityFilterChainPostProcessor를 통한 자동 주입
     * 모든 SecurityFilterChain에 JWT 필터를 자동으로 추가
     */
    @Configuration
    @ConditionalOnProperty(
        prefix = "security-starter.jwt.registration",
        name = "strategy",
        havingValue = "auto-inject",
        matchIfMissing = true  // 기본 전략
    )
    public static class AutoInjectConfiguration {
        
        /**
         * BeanPostProcessor를 통해 모든 SecurityFilterChain에 JWT 필터 주입
         */
        @Bean
        public SecurityFilterChainPostProcessor securityFilterChainPostProcessor(
                JwtAuthenticationFilter jwtFilter) {
            return new SecurityFilterChainPostProcessor(jwtFilter);
        }
    }
    
    /**
     * 전략 3: 혼합 전략 - 사용자가 선택적으로 사용
     */
    @Bean
    public JwtSecurityConfigurer jwtSecurityConfigurer(
            JwtAuthenticationFilter jwtFilter,
            SecurityStarterProperties properties) {
        
        return new JwtSecurityConfigurer(jwtFilter, properties);
    }
    
    /**
     * JWT 설정을 쉽게 적용할 수 있는 헬퍼 클래스
     */
    public static class JwtSecurityConfigurer extends AbstractHttpConfigurer<JwtSecurityConfigurer, HttpSecurity> {
        
        private final JwtAuthenticationFilter jwtFilter;
        private final SecurityStarterProperties properties;
        
        public JwtSecurityConfigurer(JwtAuthenticationFilter jwtFilter, 
                                    SecurityStarterProperties properties) {
            this.jwtFilter = jwtFilter;
            this.properties = properties;
        }
        
        @Override
        public void configure(HttpSecurity http) throws Exception {
            // JWT 필터 추가
            http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
            
            // CSRF 비활성화 (JWT는 stateless)
            http.csrf(AbstractHttpConfigurer::disable);
            
            // 세션 정책
            http.sessionManagement(session -> 
                session.sessionCreationPolicy(
                    org.springframework.security.config.http.SessionCreationPolicy.STATELESS));
            
            logger.info("✅ JWT configuration applied to SecurityFilterChain");
        }
        
        /**
         * 사용자가 쉽게 적용할 수 있는 static 메소드
         */
        public static void applyJwt(HttpSecurity http, 
                                   JwtAuthenticationFilter jwtFilter,
                                   SecurityStarterProperties properties) throws Exception {
            http.apply(new JwtSecurityConfigurer(jwtFilter, properties));
        }
    }
}