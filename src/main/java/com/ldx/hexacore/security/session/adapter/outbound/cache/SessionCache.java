package com.ldx.hexacore.security.session.adapter.outbound.cache;

import java.util.Optional;

/**
 * 세션 캐시 인터페이스
 * 
 * @param <K> 캐시 키 타입
 * @param <V> 캐시 값 타입
 */
public interface SessionCache<K, V> {
    
    /**
     * 캐시에서 값을 조회합니다.
     * 
     * @param key 캐시 키
     * @return 캐시된 값 (없으면 Optional.empty())
     */
    Optional<V> get(K key);
    
    /**
     * 캐시에 값을 저장합니다.
     * 
     * @param key 캐시 키
     * @param value 저장할 값
     */
    void put(K key, V value);
    
    /**
     * 캐시에서 특정 키를 제거합니다.
     * 
     * @param key 제거할 키
     */
    void evict(K key);
    
    /**
     * 전체 캐시를 비웁니다.
     */
    void evictAll();
}