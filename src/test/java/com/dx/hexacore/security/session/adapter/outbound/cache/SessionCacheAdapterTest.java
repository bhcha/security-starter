package com.dx.hexacore.security.session.adapter.outbound.cache;

import com.dx.hexacore.security.session.application.query.port.out.LoadFailedAttemptsQueryPort;
import com.dx.hexacore.security.session.application.query.port.out.LoadSessionStatusQueryPort;
import com.dx.hexacore.security.session.application.projection.FailedAttemptProjection;
import com.dx.hexacore.security.session.application.projection.SessionStatusProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SessionCacheAdapter 테스트")
class SessionCacheAdapterTest {

    @Mock
    private LoadSessionStatusQueryPort delegate;
    
    @Mock
    private LoadFailedAttemptsQueryPort failedAttemptsDelegate;
    
    @Mock
    private SessionCache<String, SessionStatusProjection> sessionStatusCache;
    
    @Mock
    private SessionCache<String, List<FailedAttemptProjection>> failedAttemptsCache;
    
    private SessionCacheAdapter adapter;
    
    @BeforeEach
    void setUp() {
        adapter = new SessionCacheAdapter(
            delegate, 
            failedAttemptsDelegate,
            sessionStatusCache, 
            failedAttemptsCache
        );
    }
    
    @Nested
    @DisplayName("세션 상태 캐시 테스트")
    class SessionStatusCacheTests {
        
        @Test
        @DisplayName("캐시 미스 후 저장한다")
        void cacheMissAndStore() {
            // Given
            String sessionId = UUID.randomUUID().toString();
            SessionStatusProjection projection = createSessionStatusProjection(sessionId);
            
            given(sessionStatusCache.get(sessionId)).willReturn(Optional.empty());
            given(delegate.loadSessionStatus(sessionId)).willReturn(Optional.of(projection));
            
            // When
            Optional<SessionStatusProjection> result = adapter.loadSessionStatus(sessionId);
            
            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(projection);
            
            verify(sessionStatusCache).get(sessionId);
            verify(delegate).loadSessionStatus(sessionId);
            verify(sessionStatusCache).put(sessionId, projection);
        }
        
        @Test
        @DisplayName("캐시 히트 시 delegate를 호출하지 않는다")
        void cacheHitSkipsDelegate() {
            // Given
            String sessionId = UUID.randomUUID().toString();
            SessionStatusProjection cachedProjection = createSessionStatusProjection(sessionId);
            
            given(sessionStatusCache.get(sessionId)).willReturn(Optional.of(cachedProjection));
            
            // When
            Optional<SessionStatusProjection> result = adapter.loadSessionStatus(sessionId);
            
            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(cachedProjection);
            
            verify(sessionStatusCache).get(sessionId);
            verify(delegate, never()).loadSessionStatus(anyString());
            verify(sessionStatusCache, never()).put(anyString(), any());
        }
        
        @Test
        @DisplayName("존재하지 않는 세션은 캐시하지 않는다")
        void doesNotCacheEmptyResult() {
            // Given
            String sessionId = UUID.randomUUID().toString();
            
            given(sessionStatusCache.get(sessionId)).willReturn(Optional.empty());
            given(delegate.loadSessionStatus(sessionId)).willReturn(Optional.empty());
            
            // When
            Optional<SessionStatusProjection> result = adapter.loadSessionStatus(sessionId);
            
            // Then
            assertThat(result).isEmpty();
            
            verify(sessionStatusCache).get(sessionId);
            verify(delegate).loadSessionStatus(sessionId);
            verify(sessionStatusCache, never()).put(anyString(), any());
        }
        
        @Test
        @DisplayName("새로운 인증 시도 시 캐시를 무효화한다")
        void invalidateCacheOnNewAttempt() {
            // Given
            String sessionId = UUID.randomUUID().toString();
            
            // When
            adapter.invalidateSession(sessionId);
            
            // Then
            verify(sessionStatusCache).evict(sessionId);
            verify(failedAttemptsCache).evict(sessionId);
        }
    }
    
    @Nested
    @DisplayName("실패 시도 캐시 테스트")
    class FailedAttemptsCacheTests {
        
        @Test
        @DisplayName("실패 시도 목록을 캐시한다")
        void cacheFailedAttempts() {
            // Given
            String sessionId = UUID.randomUUID().toString();
            LocalDateTime from = LocalDateTime.now().minusMinutes(15);
            LocalDateTime to = LocalDateTime.now();
            int limit = 10;
            String cacheKey = generateCacheKey(sessionId, from, to, limit);
            
            List<FailedAttemptProjection> attempts = List.of(
                createFailedAttemptProjection("user1"),
                createFailedAttemptProjection("user2")
            );
            
            given(failedAttemptsCache.get(cacheKey)).willReturn(Optional.empty());
            given(failedAttemptsDelegate.loadFailedAttempts(sessionId, from, to, limit))
                .willReturn(attempts);
            
            // When
            List<FailedAttemptProjection> result = adapter.loadFailedAttempts(sessionId, from, to, limit);
            
            // Then
            assertThat(result).hasSize(2);
            verify(failedAttemptsCache).get(cacheKey);
            verify(failedAttemptsDelegate).loadFailedAttempts(sessionId, from, to, limit);
            verify(failedAttemptsCache).put(cacheKey, attempts);
        }
        
        @Test
        @DisplayName("캐시된 실패 시도를 반환한다")
        void returnCachedFailedAttempts() {
            // Given
            String sessionId = UUID.randomUUID().toString();
            LocalDateTime from = LocalDateTime.now().minusMinutes(15);
            LocalDateTime to = LocalDateTime.now();
            int limit = 10;
            String cacheKey = generateCacheKey(sessionId, from, to, limit);
            
            List<FailedAttemptProjection> cachedAttempts = List.of(
                createFailedAttemptProjection("user1")
            );
            
            given(failedAttemptsCache.get(cacheKey)).willReturn(Optional.of(cachedAttempts));
            
            // When
            List<FailedAttemptProjection> result = adapter.loadFailedAttempts(sessionId, from, to, limit);
            
            // Then
            assertThat(result).hasSize(1);
            verify(failedAttemptsCache).get(cacheKey);
            verify(failedAttemptsDelegate, never()).loadFailedAttempts(anyString(), any(), any(), anyInt());
        }
        
        @Test
        @DisplayName("빈 결과도 캐시한다")
        void cacheEmptyResults() {
            // Given
            String sessionId = UUID.randomUUID().toString();
            LocalDateTime from = LocalDateTime.now().minusMinutes(15);
            LocalDateTime to = LocalDateTime.now();
            int limit = 10;
            String cacheKey = generateCacheKey(sessionId, from, to, limit);
            
            given(failedAttemptsCache.get(cacheKey)).willReturn(Optional.empty());
            given(failedAttemptsDelegate.loadFailedAttempts(sessionId, from, to, limit))
                .willReturn(List.of());
            
            // When
            List<FailedAttemptProjection> result = adapter.loadFailedAttempts(sessionId, from, to, limit);
            
            // Then
            assertThat(result).isEmpty();
            verify(failedAttemptsCache).put(cacheKey, List.of());
        }
    }
    
    @Nested
    @DisplayName("캐시 통합 테스트")
    class CacheIntegrationTests {
        
        @Test
        @DisplayName("TTL이 만료되면 캐시 미스가 발생한다")
        void ttlExpirationCausesCacheMiss() throws InterruptedException {
            // Given
            String sessionId = UUID.randomUUID().toString();
            SessionStatusProjection projection = createSessionStatusProjection(sessionId);
            
            // First access - cache miss
            given(sessionStatusCache.get(sessionId)).willReturn(Optional.empty());
            given(delegate.loadSessionStatus(sessionId)).willReturn(Optional.of(projection));
            
            adapter.loadSessionStatus(sessionId);
            
            // Simulate TTL expiration
            given(sessionStatusCache.get(sessionId)).willReturn(Optional.empty());
            
            // When - second access after TTL
            adapter.loadSessionStatus(sessionId);
            
            // Then
            verify(delegate, times(2)).loadSessionStatus(sessionId);
            verify(sessionStatusCache, times(2)).put(sessionId, projection);
        }
    }
    
    // Helper methods
    private SessionStatusProjection createSessionStatusProjection(String sessionId) {
        return new SessionStatusProjection(
            sessionId,
            "user123",
            "192.168.1.100",
            false,
            null,
            LocalDateTime.now(),
            LocalDateTime.now(),
            0,
            0
        );
    }
    
    private FailedAttemptProjection createFailedAttemptProjection(String userId) {
        return new FailedAttemptProjection(
            userId,
            "192.168.1.100",
            75,
            "Suspicious activity",
            LocalDateTime.now()
        );
    }
    
    private String generateCacheKey(String sessionId, LocalDateTime from, LocalDateTime to, int limit) {
        return String.format("%s:%d:%d:%d", sessionId, from.toEpochSecond(ZoneOffset.UTC), to.toEpochSecond(ZoneOffset.UTC), limit);
    }
}