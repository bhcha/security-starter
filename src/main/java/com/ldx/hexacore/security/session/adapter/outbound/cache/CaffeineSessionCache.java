package com.ldx.hexacore.security.session.adapter.outbound.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Caffeine 기반 세션 캐시 구현
 * 
 * 로컬 메모리 캐시를 제공하며 TTL과 최대 크기를 지원합니다.
 */
@Slf4j
public class CaffeineSessionCache<K, V> implements SessionCache<K, V> {
    
    private final Cache<K, V> cache;
    private final String cacheName;
    
    public CaffeineSessionCache(String cacheName, Duration ttl, long maximumSize) {
        this.cacheName = cacheName;
        this.cache = Caffeine.newBuilder()
            .expireAfterWrite(ttl.toMillis(), TimeUnit.MILLISECONDS)
            .maximumSize(maximumSize)
            .recordStats()
            .build();
            
        log.info("Created CaffeineSessionCache '{}' with TTL: {}ms, maxSize: {}", 
                 cacheName, ttl.toMillis(), maximumSize);
    }
    
    @Override
    public Optional<V> get(K key) {
        V value = cache.getIfPresent(key);
        if (value != null) {
            log.trace("Cache hit in '{}' for key: {}", cacheName, key);
        } else {
            log.trace("Cache miss in '{}' for key: {}", cacheName, key);
        }
        return Optional.ofNullable(value);
    }
    
    @Override
    public void put(K key, V value) {
        cache.put(key, value);
        log.trace("Put value in cache '{}' for key: {}", cacheName, key);
    }
    
    @Override
    public void evict(K key) {
        cache.invalidate(key);
        log.trace("Evicted key from cache '{}': {}", cacheName, key);
    }
    
    @Override
    public void evictAll() {
        long size = cache.estimatedSize();
        cache.invalidateAll();
        log.info("Evicted all {} entries from cache '{}'", size, cacheName);
    }
    
    /**
     * 캐시 통계를 반환합니다.
     */
    public CacheStats getStats() {
        com.github.benmanes.caffeine.cache.stats.CacheStats stats = cache.stats();
        return new CacheStats(
            stats.hitCount(),
            stats.missCount(),
            stats.loadSuccessCount(),
            stats.loadFailureCount(),
            stats.totalLoadTime(),
            stats.evictionCount(),
            cache.estimatedSize()
        );
    }
    
    /**
     * 캐시 통계 정보
     */
    public record CacheStats(
        long hitCount,
        long missCount,
        long loadSuccessCount,
        long loadFailureCount,
        long totalLoadTime,
        long evictionCount,
        long estimatedSize
    ) {
        public double hitRate() {
            long total = hitCount + missCount;
            return total == 0 ? 0.0 : (double) hitCount / total;
        }
        
        public double missRate() {
            return 1.0 - hitRate();
        }
    }
}