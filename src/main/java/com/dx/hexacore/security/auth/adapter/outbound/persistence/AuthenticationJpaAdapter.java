package com.dx.hexacore.security.auth.adapter.outbound.persistence;

import com.dx.hexacore.security.auth.adapter.outbound.persistence.entity.AuthenticationJpaEntity;
import com.dx.hexacore.security.util.ValidationMessages;
import com.dx.hexacore.security.auth.adapter.outbound.persistence.repository.AuthenticationJpaRepository;
import com.dx.hexacore.security.auth.application.command.port.out.AuthenticationRepository;
import com.dx.hexacore.security.auth.application.query.port.out.LoadAuthenticationQueryPort;
import com.dx.hexacore.security.auth.application.query.port.out.LoadTokenInfoQueryPort;
import com.dx.hexacore.security.auth.application.projection.AuthenticationProjection;
import com.dx.hexacore.security.auth.application.projection.TokenInfoProjection;
import com.dx.hexacore.security.auth.domain.Authentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
class AuthenticationJpaAdapter implements AuthenticationRepository, LoadAuthenticationQueryPort, LoadTokenInfoQueryPort {
    
    private final AuthenticationJpaRepository jpaRepository;
    private final AuthenticationJpaMapper mapper;
    
    @Override
    @Transactional
    public Authentication save(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNull("Authentication"));
        }
        
        log.debug("Saving authentication with id: {}", authentication.getId());
        
        AuthenticationJpaEntity entity = mapper.toEntity(authentication);
        AuthenticationJpaEntity savedEntity = jpaRepository.save(entity);
        
        log.info("Authentication saved successfully with id: {}", authentication.getId());
        return mapper.toDomain(savedEntity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Authentication> findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNull("Authentication ID"));
        }
        
        log.debug("Finding authentication by id: {}", id);
        
        return jpaRepository.findById(id.toString())
                .map(mapper::toDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Authentication> findLatestSuccessfulByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNullOrEmpty("Username"));
        }
        
        log.debug("Finding latest successful authentication by username: {}", username);
        
        return jpaRepository.findByUsername(username)
                .filter(entity -> entity.getStatus() == AuthenticationJpaEntity.AuthenticationStatusEntity.SUCCESS)
                .map(mapper::toDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Authentication> findByAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNullOrEmpty("Access token"));
        }
        
        log.debug("Finding authentication by access token");
        
        return jpaRepository.findByAccessToken(accessToken)
                .map(mapper::toDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Authentication> findByRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNullOrEmpty("Refresh token"));
        }
        
        log.debug("Finding authentication by refresh token");
        
        return jpaRepository.findByRefreshToken(refreshToken)
                .map(mapper::toDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<AuthenticationProjection> loadById(String authenticationId) {
        if (authenticationId == null || authenticationId.isBlank()) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNullOrEmpty("Authentication ID"));
        }
        
        log.debug("Loading authentication projection by id: {}", authenticationId);
        
        return jpaRepository.findById(authenticationId)
                .map(mapper::toProjection);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<TokenInfoProjection> loadByToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNullOrEmpty("Token"));
        }
        
        log.debug("Loading token info projection by token");
        
        // 먼저 access token으로 찾기
        Optional<AuthenticationJpaEntity> entity = jpaRepository.findByAccessToken(token);
        
        // access token으로 못 찾으면 refresh token으로 찾기
        if (entity.isEmpty()) {
            entity = jpaRepository.findByRefreshToken(token);
        }
        
        return entity.map(mapper::toTokenProjection);
    }
}