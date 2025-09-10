package com.ldx.hexacore.security.session.adapter.outbound.cache;

import com.ldx.hexacore.security.session.application.query.port.out.LoadFailedAttemptsQueryPort;
import com.ldx.hexacore.security.session.application.query.port.out.LoadSessionStatusQueryPort;
import com.ldx.hexacore.security.session.application.projection.FailedAttemptProjection;
import com.ldx.hexacore.security.session.application.projection.SessionStatusProjection;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;

import java.time.Duration;
import java.util.List;

/**
 * 세션 캐시 설정
 * 
 * 캐시 어댑터를 활성화하고 Caffeine 기반 캐시를 설정합니다.
 * hexacore.session.cache.enabled=true 일 때만 활성화됩니다.
 */
@Configuration
@ConditionalOnProperty(
    prefix = "hexacore.session.cache",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = false
)
@EnableConfigurationProperties(SessionCacheConfiguration.CacheProperties.class)
@Order(200)  // JPA 구현체 이후에 캐시 래퍼 등록
class SessionCacheConfiguration {
    
    @Bean
    @ConditionalOnProperty(prefix = "hexacore.session.cache", name = "enabled", havingValue = "true")
    public SessionCache<String, SessionStatusProjection> sessionStatusCache(CacheProperties properties) {
        return new CaffeineSessionCache<>(
            "sessionStatus",
            properties.getTtl(),
            properties.getMaximumSize()
        );
    }
    
    @Bean
    @ConditionalOnProperty(prefix = "hexacore.session.cache", name = "enabled", havingValue = "true")
    public SessionCache<String, List<FailedAttemptProjection>> failedAttemptsCache(CacheProperties properties) {
        return new CaffeineSessionCache<>(
            "failedAttempts",
            properties.getTtl(),
            properties.getMaximumSize()
        );
    }
    
    @Bean("sessionCacheAdapter")
    @Primary
    @ConditionalOnProperty(prefix = "hexacore.session.cache", name = "enabled", havingValue = "true")
    public SessionCacheAdapter sessionCacheAdapter(
            @Qualifier("sessionJpaQueryAdapter") LoadSessionStatusQueryPort delegate,
            @Qualifier("sessionJpaFailedAttemptsAdapter") LoadFailedAttemptsQueryPort failedAttemptsDelegate,
            SessionCache<String, SessionStatusProjection> sessionStatusCache,
            SessionCache<String, List<FailedAttemptProjection>> failedAttemptsCache) {
        return new SessionCacheAdapter(delegate, failedAttemptsDelegate, sessionStatusCache, failedAttemptsCache);
    }
    
    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "hexacore.session.cache", name = "enabled", havingValue = "true")
    public LoadSessionStatusQueryPort loadSessionStatusQueryPort(SessionCacheAdapter adapter) {
        return adapter;
    }
    
    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "hexacore.session.cache", name = "enabled", havingValue = "true")
    public LoadFailedAttemptsQueryPort loadFailedAttemptsQueryPort(SessionCacheAdapter adapter) {
        return adapter;
    }
    
    // Cache Configuration에서는 Port Bean을 등록하지 않음
    // SessionCacheAdapter만 등록하고, JPA Configuration에서 Port Bean을 관리
    
    /**
     * 캐시 설정 프로퍼티
     */
    @ConfigurationProperties(prefix = "hexacore.session.cache")
    public static class CacheProperties {
        
        private boolean enabled = false;
        private Duration ttl = Duration.ofMinutes(15);
        private long maximumSize = 10000;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public Duration getTtl() {
            return ttl;
        }
        
        public void setTtl(Duration ttl) {
            this.ttl = ttl;
        }
        
        public long getMaximumSize() {
            return maximumSize;
        }
        
        public void setMaximumSize(long maximumSize) {
            this.maximumSize = maximumSize;
        }
    }
}