package com.dx.hexacore.security.auth.application.command.port.out;

import com.dx.hexacore.security.auth.domain.vo.Credentials;
import com.dx.hexacore.security.auth.domain.vo.Token;

/**
 * 토큰 제공자 인터페이스
 * 
 * <p>토큰의 발급, 검증, 갱신을 담당하는 포트 인터페이스입니다.</p>
 * <p>다양한 토큰 제공자 구현체(Keycloak, Spring JWT 등)를 추상화합니다.</p>
 * <p>헥사고날 아키텍처의 Outbound Port로서 Application Layer에서 정의됩니다.</p>
 */
public interface TokenProvider {
    
    /**
     * 주어진 자격증명으로 토큰을 발급합니다.
     * 
     * <p>사용자의 자격증명을 검증하고 액세스 토큰과 리프레시 토큰을 포함한 토큰을 발급합니다.</p>
     * 
     * @param credentials 사용자 자격증명 (사용자명, 비밀번호 등)
     * @return 발급된 토큰 (액세스 토큰 + 리프레시 토큰)
     * @throws TokenProviderException 토큰 발급에 실패한 경우
     *         - INVALID_CREDENTIALS: 자격증명이 유효하지 않은 경우
     *         - TOKEN_ISSUE_FAILED: 토큰 발급 과정에서 오류가 발생한 경우
     *         - PROVIDER_UNAVAILABLE: 토큰 제공자 서비스가 이용 불가한 경우
     */
    Token issueToken(Credentials credentials) throws TokenProviderException;
    
    /**
     * 주어진 액세스 토큰을 검증합니다.
     * 
     * <p>토큰의 서명, 만료시간, 발급자 등을 검증하고 토큰에 포함된 사용자 정보를 추출합니다.</p>
     * 
     * @param accessToken 검증할 액세스 토큰
     * @return 토큰 검증 결과 (유효성, 사용자 정보, 권한 정보 등)
     * @throws TokenProviderException 토큰 검증에 실패한 경우
     *         - TOKEN_VALIDATION_FAILED: 토큰이 유효하지 않은 경우
     *         - TOKEN_EXPIRED: 토큰이 만료된 경우
     *         - PROVIDER_UNAVAILABLE: 토큰 제공자 서비스가 이용 불가한 경우
     */
    TokenValidationResult validateToken(String accessToken) throws TokenProviderException;
    
    /**
     * 주어진 리프레시 토큰으로 새로운 토큰을 발급합니다.
     * 
     * <p>리프레시 토큰의 유효성을 검증하고 새로운 액세스 토큰과 리프레시 토큰을 발급합니다.</p>
     * 
     * @param refreshToken 리프레시 토큰
     * @return 갱신된 토큰 (새로운 액세스 토큰 + 리프레시 토큰)
     * @throws TokenProviderException 토큰 갱신에 실패한 경우
     *         - TOKEN_REFRESH_FAILED: 리프레시 토큰이 유효하지 않거나 만료된 경우
     *         - PROVIDER_UNAVAILABLE: 토큰 제공자 서비스가 이용 불가한 경우
     */
    Token refreshToken(String refreshToken) throws TokenProviderException;
    
    /**
     * 이 제공자의 타입을 반환합니다.
     * 
     * <p>토큰 제공자의 종류를 식별하는 데 사용됩니다.</p>
     * <p>로깅, 모니터링, 설정 등에서 제공자를 구분하기 위해 사용됩니다.</p>
     * 
     * @return 토큰 제공자 타입
     */
    TokenProviderType getProviderType();
}