package com.dx.hexacore.security.auth.adapter.inbound.filter;
import com.dx.hexacore.security.auth.adapter.inbound.config.SecurityProperties;
import com.dx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * JWT 인증 필터 설정
 * Spring Boot Starter로 제공될 보안 필터 체인을 구성합니다.
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(
    prefix = "hexacore.security.filter",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
@ConditionalOnProperty(
    prefix = "security.auth.jwt",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
@ConditionalOnBean(TokenProvider.class)
public class SecurityFilterConfig {

    @Bean
    @ConfigurationProperties(prefix = "security.auth.jwt.exclude")
    public JwtExcludeProperties jwtExcludeProperties() {
        return new JwtExcludeProperties();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            TokenProvider tokenProvider,
            ObjectMapper objectMapper,
            JwtExcludeProperties excludeProperties,
            SecurityProperties securityProperties) {
        return new JwtAuthenticationFilter(
            tokenProvider,
            objectMapper,
            excludeProperties.getPaths(),
            securityProperties
        );
    }

    @Bean
    public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint(
            ObjectMapper objectMapper) {
        return new JwtAuthenticationEntryPoint(objectMapper);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            JwtExcludeProperties excludeProperties) throws Exception {
        
        http
            // CSRF 비활성화 (JWT는 stateless)
            .csrf(AbstractHttpConfigurer::disable)
            
            // 세션 정책 설정 (stateless)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 인증 요구사항 설정
            .authorizeHttpRequests(auth -> {
                // 제외 경로 설정
                excludeProperties.getPaths().forEach(path -> 
                    auth.requestMatchers(path).permitAll()
                );
                
                // 기본 제외 경로
                auth.requestMatchers("/actuator/health", "/error").permitAll();
                
                // 나머지 모든 요청은 인증 필요
                auth.anyRequest().authenticated();
            })
            
            // 인증 실패 처리
            .exceptionHandling(exception -> 
                exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            
            // JWT 필터 추가
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // 보안 헤더 설정
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny())
                .contentTypeOptions(content -> {})
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true))
                .referrerPolicy(referrer -> 
                    referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
            );

        return http.build();
    }
    
    /**
     * JWT 제외 경로 설정
     */
    public static class JwtExcludeProperties {
        private List<String> paths = new ArrayList<>();

        public JwtExcludeProperties() {
            // 기본 제외 경로
            paths.add("/actuator/health");
            paths.add("/error");
            paths.add("/swagger-ui/**");
            paths.add("/v3/api-docs/**");
        }

        public List<String> getPaths() {
            return paths;
        }

        public void setPaths(List<String> paths) {
            this.paths = paths;
        }
    }
}

