package com.ldx.hexacore.security.auth.application.command.port.in;

import com.ldx.hexacore.security.auth.domain.vo.Token;

/**
 * 토큰 관리 관련 사용 사례를 정의하는 인바운드 포트.
 * 토큰 검증 및 갱신을 위한 비즈니스 로직의 진입점을 제공합니다.
 * 
 * @since 1.0.0
 */
public interface TokenManagementUseCase {
    
    /**
     * 토큰의 유효성을 검증합니다.
     * 토큰의 만료 여부, 형식, 정책 준수 여부를 확인합니다.
     * 
     * @param command 토큰 검증 명령 (액세스 토큰 포함)
     * @return 토큰 검증 결과 (유효/무효, 무효 이유 포함)
     * @throws IllegalArgumentException command가 null인 경우
     */
    TokenValidationResult validateToken(ValidateTokenCommand command);
    
    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급합니다.
     * 리프레시 토큰의 유효성을 검증하고 새로운 토큰을 생성합니다.
     * 
     * @param command 토큰 갱신 명령 (리프레시 토큰 포함)
     * @return 새로 발급된 토큰
     * @throws IllegalArgumentException command가 null인 경우
     * @throws TokenRefreshException 토큰 갱신이 실패한 경우
     */
    Token refreshToken(RefreshTokenCommand command);
}