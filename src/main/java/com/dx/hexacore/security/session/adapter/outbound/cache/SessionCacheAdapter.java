package com.dx.hexacore.security.session.adapter.outbound.cache;

import com.dx.hexacore.security.session.application.query.port.out.LoadFailedAttemptsQueryPort;
import com.dx.hexacore.security.session.application.query.port.out.LoadSessionStatusQueryPort;
import com.dx.hexacore.security.session.application.projection.FailedAttemptProjection;
import com.dx.hexacore.security.session.application.projection.SessionStatusProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

/**
 * 세션 캐시 어댑터
 * 
 * 세션 상태와 실패 시도를 캐싱하여 성능을 향상시킵니다.
 */
@Slf4j
@RequiredArgsConstructor
class SessionCacheAdapter implements LoadSessionStatusQueryPort, LoadFailedAttemptsQueryPort {
    
    private final LoadSessionStatusQueryPort sessionStatusDelegate;
    private final LoadFailedAttemptsQueryPort failedAttemptsDelegate;
    private final SessionCache<String, SessionStatusProjection> sessionStatusCache;
    private final SessionCache<String, List<FailedAttemptProjection>> failedAttemptsCache;
    
    @Override
    public Optional<SessionStatusProjection> loadSessionStatus(String sessionId) {
        // Try cache first
        Optional<SessionStatusProjection> cached = sessionStatusCache.get(sessionId);
        
        if (cached.isPresent()) {
            log.debug("Session status cache hit for sessionId: {}", sessionId);
            return cached;
        }
        
        log.debug("Session status cache miss for sessionId: {}", sessionId);
        
        // Load from delegate
        Optional<SessionStatusProjection> result = sessionStatusDelegate.loadSessionStatus(sessionId);
        
        // Cache the result if present
        result.ifPresent(projection -> sessionStatusCache.put(sessionId, projection));
        
        return result;
    }
    
    @Override
    public Optional<SessionStatusProjection> loadSessionStatusByUser(String sessionId, String userId) {
        // Delegate to loadSessionStatus as caching by user is complex
        return loadSessionStatus(sessionId)
            .filter(projection -> projection.primaryUserId().equals(userId));
    }
    
    @Override
    public List<FailedAttemptProjection> loadFailedAttempts(String sessionId, LocalDateTime from, LocalDateTime to, int limit) {
        String cacheKey = generateFailedAttemptsCacheKey(sessionId, from, to, limit);
        
        // Try cache first
        Optional<List<FailedAttemptProjection>> cached = failedAttemptsCache.get(cacheKey);
        
        if (cached.isPresent()) {
            log.debug("Failed attempts cache hit for key: {}", cacheKey);
            return cached.get();
        }
        
        log.debug("Failed attempts cache miss for key: {}", cacheKey);
        
        // Load from delegate
        List<FailedAttemptProjection> result = failedAttemptsDelegate.loadFailedAttempts(sessionId, from, to, limit);
        
        // Cache the result
        failedAttemptsCache.put(cacheKey, result);
        
        return result;
    }
    
    @Override
    public List<FailedAttemptProjection> loadFailedAttemptsByUser(String sessionId, String userId, 
                                                                  LocalDateTime from, LocalDateTime to, int limit) {
        // Delegate without caching as user-specific queries are less frequent
        return failedAttemptsDelegate.loadFailedAttemptsByUser(sessionId, userId, from, to, limit);
    }
    
    @Override
    public int countFailedAttempts(String sessionId, LocalDateTime from, LocalDateTime to) {
        // Delegate without caching as count queries can change frequently
        return failedAttemptsDelegate.countFailedAttempts(sessionId, from, to);
    }
    
    @Override
    public int countFailedAttemptsByUser(String sessionId, String userId, LocalDateTime from, LocalDateTime to) {
        // Delegate without caching
        return failedAttemptsDelegate.countFailedAttemptsByUser(sessionId, userId, from, to);
    }
    
    /**
     * 세션 캐시를 무효화합니다.
     * 새로운 인증 시도나 상태 변경 시 호출됩니다.
     */
    public void invalidateSession(String sessionId) {
        log.debug("Invalidating cache for sessionId: {}", sessionId);
        sessionStatusCache.evict(sessionId);
        
        // Also evict all failed attempts cache entries for this session
        // Note: In a real implementation, we might want to track keys by session
        failedAttemptsCache.evict(sessionId);
    }
    
    private String generateFailedAttemptsCacheKey(String sessionId, LocalDateTime from, LocalDateTime to, int limit) {
        return String.format("%s:%d:%d:%d", 
            sessionId, 
            from.toEpochSecond(ZoneOffset.UTC), 
            to.toEpochSecond(ZoneOffset.UTC), 
            limit
        );
    }
}