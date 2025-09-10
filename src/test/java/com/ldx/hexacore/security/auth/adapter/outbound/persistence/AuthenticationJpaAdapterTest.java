package com.ldx.hexacore.security.auth.adapter.outbound.persistence;

import com.ldx.hexacore.security.auth.adapter.outbound.persistence.entity.AuthenticationJpaEntity;
import com.ldx.hexacore.security.auth.adapter.outbound.persistence.repository.AuthenticationJpaRepository;
import com.ldx.hexacore.security.auth.application.projection.AuthenticationProjection;
import com.ldx.hexacore.security.auth.application.projection.TokenInfoProjection;
import com.ldx.hexacore.security.auth.domain.Authentication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationJpaAdapterTest {
    
    @Mock
    private AuthenticationJpaRepository jpaRepository;
    
    @Mock
    private AuthenticationJpaMapper mapper;
    
    private AuthenticationJpaAdapter adapter;
    
    @BeforeEach
    void setUp() {
        adapter = new AuthenticationJpaAdapter(jpaRepository, mapper);
    }
    
    @Test
    @DisplayName("Authentication 저장 - 성공")
    void save_Success() {
        // Given
        Authentication authentication = AuthenticationJpaTestFixture.createSuccessAuthentication();
        AuthenticationJpaEntity entity = AuthenticationJpaTestFixture.createSuccessEntity();
        
        when(mapper.toEntity(authentication)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);
        
        // When
        adapter.save(authentication);
        
        // Then
        verify(mapper).toEntity(authentication);
        verify(jpaRepository).save(entity);
    }
    
    @Test
    @DisplayName("Authentication 저장 - null 입력시 예외")
    void save_NullInput() {
        // When & Then
        assertThatThrownBy(() -> adapter.save(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Authentication cannot be null");
        
        verify(jpaRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("ID로 Authentication 조회 - 존재하는 경우")
    void findById_Found() {
        // Given
        UUID id = UUID.randomUUID();
        AuthenticationJpaEntity entity = AuthenticationJpaTestFixture.createSuccessEntity();
        Authentication authentication = AuthenticationJpaTestFixture.createSuccessAuthentication();
        
        when(jpaRepository.findById(id.toString())).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(authentication);
        
        // When
        Optional<Authentication> result = adapter.findById(id);
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(authentication);
        verify(jpaRepository).findById(id.toString());
        verify(mapper).toDomain(entity);
    }
    
    @Test
    @DisplayName("ID로 Authentication 조회 - 존재하지 않는 경우")
    void findById_NotFound() {
        // Given
        UUID id = UUID.randomUUID();
        when(jpaRepository.findById(id.toString())).thenReturn(Optional.empty());
        
        // When
        Optional<Authentication> result = adapter.findById(id);
        
        // Then
        assertThat(result).isEmpty();
        verify(jpaRepository).findById(id.toString());
        verify(mapper, never()).toDomain(any());
    }
    
    @Test
    @DisplayName("ID로 Authentication 조회 - null ID 예외")
    void findById_NullId() {
        // When & Then
        assertThatThrownBy(() -> adapter.findById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Authentication ID cannot be null");
        
        verify(jpaRepository, never()).findById(anyString());
    }
    
    @Test
    @DisplayName("최근 성공한 인증 조회 - 성공")
    void findLatestSuccessfulByUsername_Found() {
        // Given
        String username = "testuser";
        AuthenticationJpaEntity entity = AuthenticationJpaTestFixture.createSuccessEntity();
        Authentication authentication = AuthenticationJpaTestFixture.createSuccessAuthentication();
        
        when(jpaRepository.findByUsername(username)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(authentication);
        
        // When
        Optional<Authentication> result = adapter.findLatestSuccessfulByUsername(username);
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(authentication);
        verify(jpaRepository).findByUsername(username);
        verify(mapper).toDomain(entity);
    }
    
    @Test
    @DisplayName("ID로 AuthenticationProjection 조회 - 성공")
    void loadById_Success() {
        // Given
        String id = "test-id";
        AuthenticationJpaEntity entity = AuthenticationJpaTestFixture.createSuccessEntity();
        AuthenticationProjection projection = mock(AuthenticationProjection.class);
        
        when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toProjection(entity)).thenReturn(projection);
        
        // When
        Optional<AuthenticationProjection> result = adapter.loadById(id);
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(projection);
        verify(jpaRepository).findById(id);
        verify(mapper).toProjection(entity);
    }
    
    @Test
    @DisplayName("토큰으로 TokenInfoProjection 조회 - access token으로 찾기")
    void loadByToken_FoundByAccessToken() {
        // Given
        String token = "test-access-token";
        AuthenticationJpaEntity entity = AuthenticationJpaTestFixture.createSuccessEntity();
        TokenInfoProjection projection = mock(TokenInfoProjection.class);
        
        when(jpaRepository.findByAccessToken(token)).thenReturn(Optional.of(entity));
        when(mapper.toTokenProjection(entity)).thenReturn(projection);
        
        // When
        Optional<TokenInfoProjection> result = adapter.loadByToken(token);
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(projection);
        verify(jpaRepository).findByAccessToken(token);
        verify(jpaRepository, never()).findByRefreshToken(anyString());
        verify(mapper).toTokenProjection(entity);
    }
    
    @Test
    @DisplayName("토큰으로 TokenInfoProjection 조회 - refresh token으로 찾기")
    void loadByToken_FoundByRefreshToken() {
        // Given
        String token = "test-refresh-token";
        AuthenticationJpaEntity entity = AuthenticationJpaTestFixture.createSuccessEntity();
        TokenInfoProjection projection = mock(TokenInfoProjection.class);
        
        when(jpaRepository.findByAccessToken(token)).thenReturn(Optional.empty());
        when(jpaRepository.findByRefreshToken(token)).thenReturn(Optional.of(entity));
        when(mapper.toTokenProjection(entity)).thenReturn(projection);
        
        // When
        Optional<TokenInfoProjection> result = adapter.loadByToken(token);
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(projection);
        verify(jpaRepository).findByAccessToken(token);
        verify(jpaRepository).findByRefreshToken(token);
        verify(mapper).toTokenProjection(entity);
    }
    
    @Test
    @DisplayName("토큰으로 TokenInfoProjection 조회 - 찾지 못함")
    void loadByToken_NotFound() {
        // Given
        String token = "non-existent-token";
        
        when(jpaRepository.findByAccessToken(token)).thenReturn(Optional.empty());
        when(jpaRepository.findByRefreshToken(token)).thenReturn(Optional.empty());
        
        // When
        Optional<TokenInfoProjection> result = adapter.loadByToken(token);
        
        // Then
        assertThat(result).isEmpty();
        verify(jpaRepository).findByAccessToken(token);
        verify(jpaRepository).findByRefreshToken(token);
        verify(mapper, never()).toTokenProjection(any());
    }
    
    @Test
    @DisplayName("토큰으로 TokenInfoProjection 조회 - null 토큰 예외")
    void loadByToken_NullToken() {
        // When & Then
        assertThatThrownBy(() -> adapter.loadByToken(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Token cannot be null or empty");
        
        verify(jpaRepository, never()).findByAccessToken(anyString());
    }
}