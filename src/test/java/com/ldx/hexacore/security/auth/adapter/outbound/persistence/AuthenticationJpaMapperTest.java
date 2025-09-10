package com.ldx.hexacore.security.auth.adapter.outbound.persistence;

import com.ldx.hexacore.security.auth.adapter.outbound.persistence.entity.AuthenticationJpaEntity;
import com.ldx.hexacore.security.auth.application.projection.AuthenticationProjection;
import com.ldx.hexacore.security.auth.application.projection.TokenInfoProjection;
import com.ldx.hexacore.security.auth.domain.Authentication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class AuthenticationJpaMapperTest {
    
    private AuthenticationJpaMapper mapper;
    
    @BeforeEach
    void setUp() {
        mapper = new AuthenticationJpaMapper();
    }
    
    @Test
    @DisplayName("Domain to Entity 변환 - 성공 케이스")
    void toEntity_Success() {
        // Given
        Authentication authentication = AuthenticationJpaTestFixture.createSuccessAuthentication();
        
        // When
        AuthenticationJpaEntity entity = mapper.toEntity(authentication);
        
        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(authentication.getId().toString());
        assertThat(entity.getUsername()).isEqualTo(authentication.getCredentials().getUsername());
        assertThat(entity.getStatus()).isEqualTo(AuthenticationJpaEntity.AuthenticationStatusEntity.SUCCESS);
        assertThat(entity.getToken()).isNotNull();
        assertThat(entity.getToken().getAccessToken()).isEqualTo(authentication.getToken().getAccessToken());
    }
    
    @Test
    @DisplayName("Domain to Entity 변환 - 실패 케이스")
    void toEntity_Failed() {
        // Given
        Authentication authentication = AuthenticationJpaTestFixture.createFailedAuthentication();
        
        // When
        AuthenticationJpaEntity entity = mapper.toEntity(authentication);
        
        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getStatus()).isEqualTo(AuthenticationJpaEntity.AuthenticationStatusEntity.FAILED);
        assertThat(entity.getFailureReason()).isEqualTo("Invalid credentials");
        assertThat(entity.getToken()).isNull();
    }
    
    @Test
    @DisplayName("Domain to Entity 변환 - null 입력시 예외")
    void toEntity_NullInput() {
        // When & Then
        assertThatThrownBy(() -> mapper.toEntity(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Authentication cannot be null");
    }
    
    @Test
    @DisplayName("Entity to Domain 변환 - 성공 케이스")
    void toDomain_Success() {
        // Given
        AuthenticationJpaEntity entity = AuthenticationJpaTestFixture.createSuccessEntity();
        
        // When
        Authentication authentication = mapper.toDomain(entity);
        
        // Then
        assertThat(authentication).isNotNull();
        assertThat(authentication.getId().toString()).isEqualTo(entity.getId());
        assertThat(authentication.getCredentials().getUsername()).isEqualTo(entity.getUsername());
        assertThat(authentication.getStatus().isSuccess()).isTrue();
        assertThat(authentication.getToken()).isNotNull();
    }
    
    @Test
    @DisplayName("Entity to Domain 변환 - 대기 상태")
    void toDomain_Pending() {
        // Given
        AuthenticationJpaEntity entity = AuthenticationJpaTestFixture.createPendingEntity();
        
        // When
        Authentication authentication = mapper.toDomain(entity);
        
        // Then
        assertThat(authentication).isNotNull();
        assertThat(authentication.getStatus().isPending()).isTrue();
        assertThat(authentication.getToken()).isNull();
    }
    
    @Test
    @DisplayName("Entity to Domain 변환 - null 입력")
    void toDomain_NullInput() {
        // When
        Authentication result = mapper.toDomain(null);
        
        // Then
        assertThat(result).isNull();
    }
    
    @Test
    @DisplayName("Entity to AuthenticationProjection 변환")
    void toProjection_Success() {
        // Given
        AuthenticationJpaEntity entity = AuthenticationJpaTestFixture.createSuccessEntity();
        
        // When
        AuthenticationProjection projection = mapper.toProjection(entity);
        
        // Then
        assertThat(projection).isNotNull();
        assertThat(projection.getId()).isEqualTo(entity.getId());
        assertThat(projection.getUsername()).isEqualTo(entity.getUsername());
        assertThat(projection.getStatus()).isEqualTo("SUCCESS");
        assertThat(projection.getAccessToken()).isEqualTo(entity.getToken().getAccessToken());
    }
    
    @Test
    @DisplayName("Entity to TokenInfoProjection 변환 - 유효한 토큰")
    void toTokenProjection_ValidToken() {
        // Given
        AuthenticationJpaEntity entity = AuthenticationJpaTestFixture.createSuccessEntity();
        entity.getToken().setTokenExpiresAt(LocalDateTime.now().plusHours(1));
        
        // When
        TokenInfoProjection projection = mapper.toTokenProjection(entity);
        
        // Then
        assertThat(projection).isNotNull();
        assertThat(projection.getToken()).isEqualTo(entity.getToken().getAccessToken());
        assertThat(projection.isValid()).isTrue();
        assertThat(projection.canRefresh()).isTrue();
        assertThat(projection.getAuthenticationId()).isEqualTo(entity.getId());
    }
    
    @Test
    @DisplayName("Entity to TokenInfoProjection 변환 - 만료된 토큰")
    void toTokenProjection_ExpiredToken() {
        // Given
        AuthenticationJpaEntity entity = AuthenticationJpaTestFixture.createSuccessEntity();
        entity.getToken().setTokenExpiresAt(LocalDateTime.now().minusHours(1));
        
        // When
        TokenInfoProjection projection = mapper.toTokenProjection(entity);
        
        // Then
        assertThat(projection).isNotNull();
        assertThat(projection.isValid()).isFalse();
    }
    
    @Test
    @DisplayName("Entity to TokenInfoProjection 변환 - 토큰 없음")
    void toTokenProjection_NoToken() {
        // Given
        AuthenticationJpaEntity entity = AuthenticationJpaTestFixture.createPendingEntity();
        
        // When
        TokenInfoProjection projection = mapper.toTokenProjection(entity);
        
        // Then
        assertThat(projection).isNull();
    }
}