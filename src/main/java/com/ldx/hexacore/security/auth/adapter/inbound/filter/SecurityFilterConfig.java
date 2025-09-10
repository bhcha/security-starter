package com.ldx.hexacore.security.auth.adapter.inbound.filter;
import com.ldx.hexacore.security.auth.adapter.inbound.config.SecurityProperties;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.ldx.hexacore.security.config.properties.HexacoreSecurityProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ldx.hexacore.security.logging.SecurityEventLogger;
import com.ldx.hexacore.security.logging.SecurityRequestLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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
@EnableConfigurationProperties({SecurityProperties.class})
@ConditionalOnProperty(
    prefix = "hexacore.security",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
@ConditionalOnBean(TokenProvider.class)
@ConditionalOnClass(ObjectMapper.class)
public class SecurityFilterConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityFilterConfig.class);
    
    public SecurityFilterConfig() {
        logger.info("🔧 SecurityFilterConfig 초기화됨 - JWT 인증 필터 설정 시작");
    }

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
            SecurityProperties securityProperties,
            SecurityRequestLogger requestLogger,
            SecurityEventLogger eventLogger) {
        
        logger.info("🛡️ JwtAuthenticationFilter Bean 생성됨");
        logger.info("TokenProvider 타입: {}", tokenProvider.getClass().getSimpleName());
        logger.info("제외 경로 수: {}", excludeProperties.getPaths().size());
        
        return new JwtAuthenticationFilter(
            tokenProvider,
            objectMapper,
            excludeProperties.getPaths(),
            securityProperties,
            requestLogger,
            eventLogger
        );
    }

    @Bean
    public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint(
            ObjectMapper objectMapper) {
        return new JwtAuthenticationEntryPoint(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)  // 사용자가 정의하지 않은 경우에만
    @Order(Ordered.LOWEST_PRECEDENCE - 10)  // 낮은 우선순위로 fallback 역할
    public SecurityFilterChain defaultSecurityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            JwtExcludeProperties excludeProperties,
            HexacoreSecurityProperties hexacoreProperties) throws Exception {
        
        logger.info("⚙️ SecurityFilterChain 구성 시작");
        logger.info("JwtAuthenticationFilter: {}", jwtAuthenticationFilter.getClass().getSimpleName());
        
        http
            // CSRF 비활성화 (JWT는 stateless)
            .csrf(AbstractHttpConfigurer::disable)
            
            // 세션 정책 설정 (stateless)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 인증 요구사항 설정
            .authorizeHttpRequests(auth -> {
                // 모든 설정 소스에서 exclude 경로 수집 및 적용
                List<String> allExcludePaths = collectAllExcludePaths(excludeProperties, hexacoreProperties);
                
                if (!allExcludePaths.isEmpty()) {
                    String[] pathArray = allExcludePaths.toArray(new String[0]);
                    auth.requestMatchers(pathArray).permitAll();
                    logger.info("Applied all exclude paths: {}", allExcludePaths);
                } else {
                    logger.warn("No exclude paths configured - all requests will require authentication");
                }
                
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

        SecurityFilterChain filterChain = http.build();
        logger.info("✅ SecurityFilterChain 구성 완료 - JWT 인증 필터 포함됨");
        return filterChain;
    }
    
    /**
     * 모든 설정 소스에서 exclude 경로들을 수집합니다.
     */
    private List<String> collectAllExcludePaths(
            JwtExcludeProperties excludeProperties, 
            HexacoreSecurityProperties hexacoreProperties) {
        
        List<String> allPaths = new ArrayList<>();
        
        // JWT Exclude Properties에서 수집
        if (excludeProperties.getPaths() != null) {
            allPaths.addAll(excludeProperties.getPaths());
            logger.debug("Added from security.auth.jwt.exclude.paths: {}", excludeProperties.getPaths());
        }
        
        // Hexacore Security filter excludePaths에서 수집
        var hexacoreExcludePaths = hexacoreProperties.getFilter().getExcludePaths();
        if (hexacoreExcludePaths != null && hexacoreExcludePaths.length > 0) {
            for (String path : hexacoreExcludePaths) {
                allPaths.add(path);
            }
            logger.debug("Added from hexacore.security.filter.excludePaths: {}", (Object) hexacoreExcludePaths);
        }
        
        // JWT Token Provider excludedPaths에서 수집
        var jwtExcludedPaths = hexacoreProperties.getTokenProvider().getJwt().getExcludedPaths();
        if (jwtExcludedPaths != null && !jwtExcludedPaths.isEmpty()) {
            allPaths.addAll(jwtExcludedPaths);
            logger.debug("Added from hexacore.security.tokenProvider.jwt.excludedPaths: {}", jwtExcludedPaths);
        }
        
        return allPaths;
    }
    
    /**
     * JWT 제외 경로 설정 (설정 파일에서만 지정)
     */
    public static class JwtExcludeProperties {
        private List<String> paths = new ArrayList<>(); // 기본값 없음 - 설정 파일에서 지정

        public List<String> getPaths() {
            return paths;
        }

        public void setPaths(List<String> paths) {
            this.paths = paths;
        }
    }
}

