package com.dx.hexacore.security.auth.adapter.outbound.persistence.repository;

import com.dx.hexacore.security.auth.adapter.outbound.persistence.entity.AuthenticationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthenticationJpaRepository extends JpaRepository<AuthenticationJpaEntity, String> {
    
    @Query("SELECT a FROM AuthenticationJpaEntity a WHERE a.token.accessToken = :token")
    Optional<AuthenticationJpaEntity> findByAccessToken(@Param("token") String token);
    
    @Query("SELECT a FROM AuthenticationJpaEntity a WHERE a.token.refreshToken = :token")
    Optional<AuthenticationJpaEntity> findByRefreshToken(@Param("token") String token);
    
    Optional<AuthenticationJpaEntity> findByUsername(String username);
}