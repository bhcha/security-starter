package com.dx.hexacore.security.auth.application.command.port.out;

import com.dx.hexacore.security.auth.domain.Authentication;
import java.util.Optional;
import java.util.UUID;

/**
 * 인증 정보 저장소 인터페이스.
 * 인증 애그리거트의 영속화를 담당하는 아웃바운드 포트입니다.
 * 
 * @since 1.0.0
 */
public interface AuthenticationRepository {
    
    /**
     * 인증 정보를 저장합니다.
     * 
     * @param authentication 저장할 인증 정보
     * @return 저장된 인증 정보
     * @throws IllegalArgumentException authentication이 null인 경우
     */
    Authentication save(Authentication authentication);
    
    /**
     * ID로 인증 정보를 조회합니다.
     * 
     * @param id 인증 ID
     * @return 인증 정보가 존재하면 해당 정보, 없으면 empty
     * @throws IllegalArgumentException id가 null인 경우
     */
    Optional<Authentication> findById(UUID id);
    
    /**
     * 사용자명으로 가장 최근의 성공한 인증 정보를 조회합니다.
     * 
     * @param username 사용자명
     * @return 성공한 인증 정보가 존재하면 해당 정보, 없으면 empty
     * @throws IllegalArgumentException username이 null이거나 빈 값인 경우
     */
    Optional<Authentication> findLatestSuccessfulByUsername(String username);
    
    /**
     * 액세스 토큰으로 인증 정보를 조회합니다.
     * 
     * @param accessToken 액세스 토큰
     * @return 해당 토큰을 가진 인증 정보가 존재하면 해당 정보, 없으면 empty
     * @throws IllegalArgumentException accessToken이 null이거나 빈 값인 경우
     */
    Optional<Authentication> findByAccessToken(String accessToken);
    
    /**
     * 리프레시 토큰으로 인증 정보를 조회합니다.
     * 
     * @param refreshToken 리프레시 토큰
     * @return 해당 토큰을 가진 인증 정보가 존재하면 해당 정보, 없으면 empty
     * @throws IllegalArgumentException refreshToken이 null이거나 빈 값인 경우
     */
    Optional<Authentication> findByRefreshToken(String refreshToken);
}