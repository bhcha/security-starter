package com.ldx.hexacore.security.auth.application.command.port.in;

/**
 * 인증 관련 사용 사례를 정의하는 인바운드 포트.
 * 사용자 인증 처리를 위한 비즈니스 로직의 진입점을 제공합니다.
 * 
 * @since 1.0.0
 */
public interface AuthenticationUseCase {
    
    /**
     * 사용자 인증을 수행합니다.
     * 제공된 인증 정보를 검증하고 성공 시 토큰을 발급합니다.
     * 
     * @param command 인증 명령 (사용자명, 비밀번호 포함)
     * @return 인증 결과 (성공/실패, 토큰 정보 포함)
     * @throws IllegalArgumentException command가 null인 경우
     */
    AuthenticationResult authenticate(AuthenticateCommand command);
}