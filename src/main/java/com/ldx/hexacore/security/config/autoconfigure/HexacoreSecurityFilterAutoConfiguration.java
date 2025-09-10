package com.ldx.hexacore.security.config.autoconfigure;

import com.ldx.hexacore.security.config.properties.HexacoreSecurityProperties;
import com.ldx.hexacore.security.auth.adapter.inbound.filter.SecurityFilterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Hexacore Security 자동 설정 클래스.
 * Spring Security를 이용한 API 보안 설정을 제공합니다.
 * 
 * @since 1.0.0
 */
@AutoConfiguration(after = org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class)
@ConditionalOnClass(SecurityFilterChain.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableWebSecurity
@EnableConfigurationProperties(HexacoreSecurityProperties.class)
public class HexacoreSecurityFilterAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(HexacoreSecurityFilterAutoConfiguration.class);
    
    private final HexacoreSecurityProperties hexacoreProperties;
    
    public HexacoreSecurityFilterAutoConfiguration(HexacoreSecurityProperties hexacoreProperties) {
        this.hexacoreProperties = hexacoreProperties;
    }

    // SecurityProperties는 SecurityFilterConfig에서 @EnableConfigurationProperties로 생성됨

    /**
     * Security 필터 체인을 구성합니다.
     * 설정 파일의 excludePaths를 동적으로 적용합니다.
     */
    @Bean
    @ConditionalOnMissingBean({SecurityFilterChain.class, SecurityFilterConfig.class})
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring Security Filter Chain for API protection");
        
        // 모든 설정 소스에서 excludePaths 수집
        List<String> allExcludePaths = collectAllExcludePaths();
        
        logger.info("Collected exclude paths from all sources: {}", allExcludePaths);
        
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> {
                // 설정에서 가져온 exclude 경로들만 허용
                if (!allExcludePaths.isEmpty()) {
                    String[] excludePathArray = allExcludePaths.toArray(new String[0]);
                    auth.requestMatchers(excludePathArray).permitAll();
                    logger.info("Applied exclude paths: {}", Arrays.toString(excludePathArray));
                } else {
                    logger.warn("No exclude paths configured - all requests will require authentication");
                }
                
                // 기타 모든 요청은 인증 필요
                auth.anyRequest().authenticated();
            })
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable);

        logger.info("Security Filter Chain configured successfully with {} exclude paths", allExcludePaths.size());
        return http.build();
    }
    
    /**
     * 모든 설정 소스에서 excludePaths를 수집합니다.
     */
    private List<String> collectAllExcludePaths() {
        List<String> allPaths = new ArrayList<>();
        
        // HexacoreSecurityProperties.filter.excludePaths
        if (hexacoreProperties.getFilter().getExcludePaths() != null) {
            allPaths.addAll(Arrays.asList(hexacoreProperties.getFilter().getExcludePaths()));
            logger.debug("Added from hexacore.security.filter.excludePaths: {}", 
                Arrays.toString(hexacoreProperties.getFilter().getExcludePaths()));
        }
        
        // HexacoreSecurityProperties.tokenProvider.jwt.excludedPaths
        if (hexacoreProperties.getTokenProvider().getJwt().getExcludedPaths() != null) {
            allPaths.addAll(hexacoreProperties.getTokenProvider().getJwt().getExcludedPaths());
            logger.debug("Added from hexacore.security.tokenProvider.jwt.excludedPaths: {}", 
                hexacoreProperties.getTokenProvider().getJwt().getExcludedPaths());
        }
        
        // SecurityProperties는 SecurityFilterConfig에서 처리됨 (SecurityFilterConfig가 활성화되지 않은 경우만 여기 실행)
        
        return allPaths;
    }
}