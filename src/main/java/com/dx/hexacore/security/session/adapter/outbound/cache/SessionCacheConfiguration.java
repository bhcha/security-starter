package com.dx.hexacore.security.session.adapter.outbound.cache;

import com.dx.hexacore.security.session.application.query.port.out.LoadFailedAttemptsQueryPort;
import com.dx.hexacore.security.session.application.query.port.out.LoadSessionStatusQueryPort;
import com.dx.hexacore.security.session.application.projection.FailedAttemptProjection;
import com.dx.hexacore.security.session.application.projection.SessionStatusProjection;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

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
class SessionCacheConfiguration {
    
    @Bean
    public SessionCache<String, SessionStatusProjection> sessionStatusCache(CacheProperties properties) {
        return new CaffeineSessionCache<>(
            "sessionStatus",
            properties.getTtl(),
            properties.getMaximumSize()
        );
    }
    
    @Bean
    public SessionCache<String, List<FailedAttemptProjection>> failedAttemptsCache(CacheProperties properties) {
        return new CaffeineSessionCache<>(
            "failedAttempts",
            properties.getTtl(),
            properties.getMaximumSize()
        );
    }
    
    @Bean
    @Primary
    public SessionCacheAdapter cachedLoadSessionStatusQueryPort(
            @Qualifier("sessionJpaAdapter") LoadSessionStatusQueryPort delegate,
            @Qualifier("sessionJpaAdapter") LoadFailedAttemptsQueryPort failedAttemptsDelegate,
            SessionCache<String, SessionStatusProjection> sessionStatusCache,
            SessionCache<String, List<FailedAttemptProjection>> failedAttemptsCache) {
        return new SessionCacheAdapter(delegate, failedAttemptsDelegate, sessionStatusCache, failedAttemptsCache);
    }
    
    @Bean
    @Primary
    public LoadFailedAttemptsQueryPort cachedLoadFailedAttemptsQueryPort(
            SessionCacheAdapter cacheAdapter) {
        return cacheAdapter;
    }
    
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