package com.ldx.hexacore.security.config.autoconfigure;

import com.ldx.hexacore.security.config.properties.SecurityStarterProperties;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * 캐시 어댑터 자동 설정
 * 
 * Caffeine 또는 Redis 캐시를 조건부로 활성화합니다.
 */
@Configuration
@EnableConfigurationProperties(SecurityStarterProperties.class)
public class CacheAutoConfiguration {
    
    /**
     * Caffeine 캐시 어댑터 설정
     */
    @Configuration
    @ConditionalOnClass(Caffeine.class)
    @ConditionalOnProperty(
        prefix = "security-starter.cache",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    @ComponentScan(
        basePackages = {
            "com.ldx.security-starter.session.adapter.outbound.cache"
        },
        includeFilters = @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = ".*CacheAdapter.*"
        )
    )
    public static class CaffeineCacheConfiguration {
        // Caffeine 캐시 관련 추가 설정이 필요한 경우 여기에 추가
    }
    
    /**
     * Redis 캐시 어댑터 설정 (향후 지원 예정)
     */
    @Configuration
    @ConditionalOnProperty(
        prefix = "security-starter.cache.redis",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false
    )
    public static class RedisCacheConfiguration {
        // Redis 지원 시 구현 예정
    }
}