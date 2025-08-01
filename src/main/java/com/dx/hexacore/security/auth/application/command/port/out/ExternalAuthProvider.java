package com.dx.hexacore.security.auth.application.command.port.out;

import com.dx.hexacore.security.auth.domain.vo.Credentials;
import com.dx.hexacore.security.auth.domain.vo.Token;

/**
 * 외부 인증 제공자와의 통신을 담당하는 아웃바운드 포트.
 * Keycloak 등의 외부 인증 시스템과 연동하여 토큰을 발급받습니다.
 * 
 * @since 1.0.0
 */
public interface ExternalAuthProvider {
    
    /**
     * 외부 인증 제공자를 통해 사용자를 인증하고 토큰을 발급받습니다.
     * 
     * @param credentials 인증 정보 (사용자명, 비밀번호)
     * @return 발급받은 토큰
     * @throws ExternalAuthException 외부 인증 제공자와의 통신 실패 시
     * @throws IllegalArgumentException credentials가 null인 경우
     */
    Token authenticate(Credentials credentials) throws ExternalAuthException;
    
    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.
     * 
     * @param refreshToken 리프레시 토큰
     * @return 새로 발급받은 토큰
     * @throws ExternalAuthException 외부 인증 제공자와의 통신 실패 시
     * @throws IllegalArgumentException refreshToken이 null이거나 빈 값인 경우
     */
    Token refreshToken(String refreshToken) throws ExternalAuthException;
    
    /**
     * 토큰의 유효성을 외부 인증 제공자에서 검증합니다.
     * 
     * @param accessToken 검증할 액세스 토큰
     * @return 토큰 유효성 여부
     * @throws ExternalAuthException 외부 인증 제공자와의 통신 실패 시
     * @throws IllegalArgumentException accessToken이 null이거나 빈 값인 경우
     */
    boolean validateToken(String accessToken) throws ExternalAuthException;
}