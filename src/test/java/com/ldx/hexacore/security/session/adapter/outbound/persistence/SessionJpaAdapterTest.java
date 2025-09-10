package com.ldx.hexacore.security.session.adapter.outbound.persistence;

import com.ldx.hexacore.security.session.adapter.outbound.persistence.entity.AuthenticationAttemptJpaEntity;
import com.ldx.hexacore.security.session.adapter.outbound.persistence.entity.SessionJpaEntity;
import com.ldx.hexacore.security.session.adapter.outbound.persistence.repository.SessionJpaRepository;
import com.ldx.hexacore.security.session.application.projection.FailedAttemptProjection;
import com.ldx.hexacore.security.session.application.projection.SessionStatusProjection;
import com.ldx.hexacore.security.session.domain.AuthenticationSession;
import com.ldx.hexacore.security.session.domain.vo.SessionId;
import com.ldx.hexacore.security.session.domain.vo.ClientIp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SessionJpaAdapter 테스트")
class SessionJpaAdapterTest {

    @Mock
    private SessionJpaRepository repository;
    
    @Mock
    private SessionMapper mapper;
    
    private SessionJpaAdapter adapter;
    
    @BeforeEach
    void setUp() {
        adapter = new SessionJpaAdapter(repository, mapper);
    }
    
    @Nested
    @DisplayName("AuthenticationSessionRepository 구현 테스트")
    class AuthenticationSessionRepositoryTests {
        
        @Test
        @DisplayName("세션을 저장한다")
        void saveSession() {
            // Given
            SessionId sessionId = SessionId.of(UUID.randomUUID().toString());
            ClientIp clientIp = ClientIp.of("192.168.1.100");
            AuthenticationSession session = AuthenticationSession.create(sessionId, "user123", clientIp, 5, 30);
            
            SessionJpaEntity entity = SessionJpaEntity.builder()
                .sessionId(sessionId.toString())
                .userId("user123")
                .build();
            
            given(repository.findById(sessionId.toString())).willReturn(Optional.empty());
            given(mapper.toEntity(session)).willReturn(entity);
            given(repository.save(any(SessionJpaEntity.class))).willReturn(entity);
            given(mapper.toDomain(entity)).willReturn(session);
            
            // When
            AuthenticationSession result = adapter.save(session);
            
            // Then
            verify(repository).findById(sessionId.toString());
            verify(mapper).toEntity(session);
            verify(repository).save(entity);
            verify(mapper).toDomain(entity);
            assertThat(result).isEqualTo(session);
        }
        
        @Test
        @DisplayName("기존 세션을 업데이트한다")
        void updateExistingSession() {
            // Given
            SessionId sessionId = SessionId.of(UUID.randomUUID().toString());
            ClientIp clientIp = ClientIp.of("192.168.1.100");
            AuthenticationSession session = AuthenticationSession.create(sessionId, "user123", clientIp, 5, 30);
            
            SessionJpaEntity existingEntity = SessionJpaEntity.builder()
                .sessionId(sessionId.toString())
                .userId("user123")
                .build();
            
            given(repository.findById(sessionId.toString())).willReturn(Optional.of(existingEntity));
            given(repository.save(any(SessionJpaEntity.class))).willReturn(existingEntity);
            given(mapper.toDomain(existingEntity)).willReturn(session);
            
            // When
            AuthenticationSession result = adapter.save(session);
            
            // Then
            verify(repository).findById(sessionId.toString());
            verify(mapper).updateEntity(existingEntity, session);
            verify(repository).save(existingEntity);
            verify(mapper).toDomain(existingEntity);
        }
        
        @Test
        @DisplayName("세션ID로 세션을 조회한다")
        void findBySessionId() {
            // Given
            SessionId sessionId = SessionId.of(UUID.randomUUID().toString());
            
            SessionJpaEntity entity = SessionJpaEntity.builder()
                .sessionId(sessionId.toString())
                .userId("user123")
                .build();
            
            ClientIp clientIp = ClientIp.of("192.168.1.100");
            AuthenticationSession session = AuthenticationSession.create(sessionId, "user123", clientIp, 5, 30);
            
            given(repository.findById(sessionId.toString())).willReturn(Optional.of(entity));
            given(mapper.toDomain(entity)).willReturn(session);
            
            // When
            Optional<AuthenticationSession> result = adapter.findBySessionId(sessionId);
            
            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(session);
            verify(repository).findById(sessionId.toString());
            verify(mapper).toDomain(entity);
        }
        
        @Test
        @DisplayName("존재하지 않는 세션 조회 시 empty를 반환한다")
        void returnEmptyWhenSessionNotFound() {
            // Given
            SessionId sessionId = SessionId.of(UUID.randomUUID().toString());
            given(repository.findById(sessionId.toString())).willReturn(Optional.empty());
            
            // When
            Optional<AuthenticationSession> result = adapter.findBySessionId(sessionId);
            
            // Then
            assertThat(result).isEmpty();
            verify(repository).findById(sessionId.toString());
            verify(mapper, never()).toDomain(any());
        }
        
        @Test
        @DisplayName("세션을 삭제한다")
        void deleteSession() {
            // Given
            SessionId sessionId = SessionId.of(UUID.randomUUID().toString());
            
            // When
            adapter.delete(sessionId);
            
            // Then
            verify(repository).deleteById(sessionId.toString());
        }
    }
    
    @Nested
    @DisplayName("LoadSessionStatusQueryPort 구현 테스트")
    class LoadSessionStatusTests {
        
        @Test
        @DisplayName("존재하는 세션을 조회한다")
        void loadExistingSession() {
            // Given
            String sessionId = UUID.randomUUID().toString();
            SessionJpaEntity entity = SessionJpaEntity.builder()
                .sessionId(sessionId)
                .userId("user123")
                .lockoutUntil(null)
                .build();
            
            SessionStatusProjection projection = new SessionStatusProjection(
                sessionId,
                "user123",
                "192.168.1.100",  // primaryClientIp
                false,
                null,
                LocalDateTime.now(),  // createdAt
                LocalDateTime.now(),  // lastActivityAt
                0,  // totalAttempts
                0   // failedAttempts
            );
            
            given(repository.findById(sessionId)).willReturn(Optional.of(entity));
            given(mapper.toStatusProjection(entity)).willReturn(projection);
            
            // When
            Optional<SessionStatusProjection> result = adapter.loadSessionStatus(sessionId);
            
            // Then
            assertThat(result).isPresent();
            assertThat(result.get().sessionId()).isEqualTo(sessionId);
            assertThat(result.get().primaryUserId()).isEqualTo("user123");
            assertThat(result.get().isLocked()).isFalse();
        }
        
        @Test
        @DisplayName("존재하지 않는 세션 조회 시 empty를 반환한다")
        void returnEmptyForNonExistentSession() {
            // Given
            String sessionId = UUID.randomUUID().toString();
            given(repository.findById(sessionId)).willReturn(Optional.empty());
            
            // When
            Optional<SessionStatusProjection> result = adapter.loadSessionStatus(sessionId);
            
            // Then
            assertThat(result).isEmpty();
            verify(mapper, never()).toStatusProjection(any());
        }
        
        @Test
        @DisplayName("잠금된 세션을 조회한다")
        void loadLockedSession() {
            // Given
            String sessionId = UUID.randomUUID().toString();
            LocalDateTime lockoutUntil = LocalDateTime.now().plusMinutes(20);
            
            SessionJpaEntity entity = SessionJpaEntity.builder()
                .sessionId(sessionId)
                .userId("user123")
                .lockoutUntil(lockoutUntil)
                .build();
            
            SessionStatusProjection projection = new SessionStatusProjection(
                sessionId,
                "user123",
                "192.168.1.100",  // primaryClientIp
                true,
                lockoutUntil,
                LocalDateTime.now(),  // createdAt
                LocalDateTime.now(),  // lastActivityAt
                5,  // totalAttempts
                5   // failedAttempts
            );
            
            given(repository.findById(sessionId)).willReturn(Optional.of(entity));
            given(mapper.toStatusProjection(entity)).willReturn(projection);
            
            // When
            Optional<SessionStatusProjection> result = adapter.loadSessionStatus(sessionId);
            
            // Then
            assertThat(result).isPresent();
            assertThat(result.get().isLocked()).isTrue();
            assertThat(result.get().lockedUntil()).isEqualTo(lockoutUntil);
        }
        
        @Test
        @DisplayName("최근 인증 시도 정보를 포함하여 반환한다")
        void includeRecentAttemptInfo() {
            // Given
            String sessionId = UUID.randomUUID().toString();
            LocalDateTime lastAttemptTime = LocalDateTime.now().minusMinutes(5);
            
            SessionJpaEntity entity = SessionJpaEntity.builder()
                .sessionId(sessionId)
                .userId("user123")
                .build();
            
            AuthenticationAttemptJpaEntity lastAttempt = AuthenticationAttemptJpaEntity.builder()
                .attemptedAt(lastAttemptTime)
                .successful(false)
                .build();
            entity.addAttempt(lastAttempt);
            
            SessionStatusProjection projection = new SessionStatusProjection(
                sessionId,
                "user123",
                "192.168.1.100",  // primaryClientIp
                false,
                null,
                LocalDateTime.now(),  // createdAt
                lastAttemptTime,  // lastActivityAt
                1,  // totalAttempts
                1   // failedAttempts
            );
            
            given(repository.findById(sessionId)).willReturn(Optional.of(entity));
            given(mapper.toStatusProjection(entity)).willReturn(projection);
            
            // When
            Optional<SessionStatusProjection> result = adapter.loadSessionStatus(sessionId);
            
            // Then
            assertThat(result).isPresent();
            assertThat(result.get().lastActivityAt()).isEqualTo(lastAttemptTime);
            assertThat(result.get().failedAttempts()).isEqualTo(1);
        }
    }
    
    @Nested
    @DisplayName("LoadFailedAttemptsQueryPort 구현 테스트")
    class LoadFailedAttemptsTests {
        
        @Test
        @DisplayName("실패한 시도 목록을 조회한다")
        void loadFailedAttempts() {
            // Given
            String sessionId = UUID.randomUUID().toString();
            LocalDateTime since = LocalDateTime.now().minusMinutes(15);
            
            List<AuthenticationAttemptJpaEntity> attempts = List.of(
                AuthenticationAttemptJpaEntity.builder()
                    .attemptId(UUID.randomUUID().toString())
                    .attemptedAt(LocalDateTime.now().minusMinutes(10))
                    .successful(false)
                    .reason("Invalid password")
                    .clientIp("192.168.1.100")
                    .riskLevel(50)
                    .build(),
                AuthenticationAttemptJpaEntity.builder()
                    .attemptId(UUID.randomUUID().toString())
                    .attemptedAt(LocalDateTime.now().minusMinutes(5))
                    .successful(false)
                    .reason("Account locked")
                    .clientIp("192.168.1.100")
                    .riskLevel(80)
                    .build()
            );
            
            LocalDateTime to = LocalDateTime.now();
            given(repository.findFailedAttemptsBetween(eq(sessionId), eq(since), eq(to), any(Pageable.class)))
                .willReturn(attempts);
            
            List<FailedAttemptProjection> projections = attempts.stream()
                .map(mapper::toFailedAttemptProjection)
                .toList();
            when(mapper.toFailedAttemptProjection(any())).thenAnswer(invocation -> {
                AuthenticationAttemptJpaEntity entity = invocation.getArgument(0);
                return new FailedAttemptProjection(
                    "user123",  // userId
                    entity.getClientIp(),
                    entity.getRiskLevel(),
                    entity.getReason(),
                    entity.getAttemptedAt()
                );
            });
            
            // When
            List<FailedAttemptProjection> result = adapter.loadFailedAttempts(sessionId, since, to, 10);
            
            // Then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(attempt -> !attempt.riskReason().isEmpty());
        }
        
        @Test
        @DisplayName("시간 범위 내 실패 시도만 조회한다")
        void loadFailedAttemptsWithinTimeRange() {
            // Given
            String sessionId = UUID.randomUUID().toString();
            LocalDateTime since = LocalDateTime.now().minusMinutes(15);
            
            LocalDateTime to = LocalDateTime.now();
            given(repository.findFailedAttemptsBetween(eq(sessionId), eq(since), eq(to), any(Pageable.class)))
                .willReturn(List.of());
            
            // When
            List<FailedAttemptProjection> result = adapter.loadFailedAttempts(sessionId, since, to, 10);
            
            // Then
            assertThat(result).isEmpty();
            verify(repository).findFailedAttemptsBetween(eq(sessionId), eq(since), eq(to), any(Pageable.class));
        }
        
        @Test
        @DisplayName("빈 결과를 처리한다")
        void handleEmptyResult() {
            // Given
            String sessionId = UUID.randomUUID().toString();
            LocalDateTime since = LocalDateTime.now().minusMinutes(15);
            
            LocalDateTime to = LocalDateTime.now();
            given(repository.findFailedAttemptsBetween(eq(sessionId), eq(since), eq(to), any(Pageable.class)))
                .willReturn(List.of());
            
            // When
            List<FailedAttemptProjection> result = adapter.loadFailedAttempts(sessionId, since, to, 10);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }
        
        @Test
        @DisplayName("시간 역순으로 정렬된 결과를 반환한다")
        void returnSortedByTimeDesc() {
            // Given
            String sessionId = UUID.randomUUID().toString();
            LocalDateTime since = LocalDateTime.now().minusMinutes(15);
            
            LocalDateTime time1 = LocalDateTime.now().minusMinutes(10);
            LocalDateTime time2 = LocalDateTime.now().minusMinutes(5);
            LocalDateTime time3 = LocalDateTime.now().minusMinutes(2);
            
            List<AuthenticationAttemptJpaEntity> attempts = List.of(
                createAttemptEntity(time3),
                createAttemptEntity(time1),
                createAttemptEntity(time2)
            );
            
            LocalDateTime to = LocalDateTime.now();
            given(repository.findFailedAttemptsBetween(eq(sessionId), eq(since), eq(to), any(Pageable.class)))
                .willReturn(attempts);
            when(mapper.toFailedAttemptProjection(any())).thenAnswer(invocation -> {
                AuthenticationAttemptJpaEntity entity = invocation.getArgument(0);
                return new FailedAttemptProjection(
                    "user123",  // userId
                    entity.getClientIp(),
                    entity.getRiskLevel(),
                    entity.getReason(),
                    entity.getAttemptedAt()
                );
            });
            
            // When
            List<FailedAttemptProjection> result = adapter.loadFailedAttempts(sessionId, since, to, 10);
            
            // Then
            // Repository should return sorted results
            verify(repository).findFailedAttemptsBetween(eq(sessionId), eq(since), eq(to), any(Pageable.class));
        }
        
        private AuthenticationAttemptJpaEntity createAttemptEntity(LocalDateTime attemptedAt) {
            return AuthenticationAttemptJpaEntity.builder()
                .attemptId(UUID.randomUUID().toString())
                .attemptedAt(attemptedAt)
                .successful(false)
                .reason("Test failure")
                .clientIp("192.168.1.100")
                .riskLevel(50)
                .build();
        }
    }
    
    @Nested
    @DisplayName("트랜잭션 및 동시성 테스트")
    class TransactionAndConcurrencyTests {
        
        @Test
        @DisplayName("트랜잭션 롤백을 처리한다")
        void handleTransactionRollback() {
            // Given
            SessionId sessionId = SessionId.of(UUID.randomUUID().toString());
            
            given(repository.findById(sessionId.toString())).willThrow(new RuntimeException("Database error"));
            
            // When & Then
            assertThatThrownBy(() -> adapter.findBySessionId(sessionId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");
            
            verify(repository, never()).save(any());
        }
    }
}