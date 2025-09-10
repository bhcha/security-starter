package com.ldx.hexacore.security.auth.application.command.handler;

import com.ldx.hexacore.security.auth.application.command.port.in.*;
import com.ldx.hexacore.security.auth.application.command.port.out.AuthenticationRepository;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenProviderException;
import com.ldx.hexacore.security.auth.domain.Authentication;
import com.ldx.hexacore.security.auth.domain.service.JwtPolicy;
import com.ldx.hexacore.security.auth.domain.service.SessionPolicy;
import com.ldx.hexacore.security.auth.domain.vo.Token;

import java.util.Objects;
import java.util.Optional;

/**
 * 토큰 관리 사용 사례 구현체.
 * 토큰 검증 및 갱신을 담당하는 애플리케이션 서비스입니다.
 * 
 * @since 1.0.0
 */
class TokenManagementUseCaseImpl implements TokenManagementUseCase {
    
    private final AuthenticationRepository authenticationRepository;
    private final TokenProvider tokenProvider;
    private final JwtPolicy jwtPolicy;
    private final SessionPolicy sessionPolicy;
    
    /**
     * 토큰 관리 사용 사례를 생성합니다.
     * 
     * @param authenticationRepository 인증 저장소
     * @param tokenProvider 토큰 제공자
     * @param jwtPolicy JWT 정책
     * @param sessionPolicy 세션 정책
     */
    public TokenManagementUseCaseImpl(
        AuthenticationRepository authenticationRepository,
        TokenProvider tokenProvider,
        JwtPolicy jwtPolicy,
        SessionPolicy sessionPolicy
    ) {
        this.authenticationRepository = Objects.requireNonNull(authenticationRepository, "authenticationRepository cannot be null");
        this.tokenProvider = Objects.requireNonNull(tokenProvider, "tokenProvider cannot be null");
        this.jwtPolicy = Objects.requireNonNull(jwtPolicy, "jwtPolicy cannot be null");
        this.sessionPolicy = Objects.requireNonNull(sessionPolicy, "sessionPolicy cannot be null");
    }
    
    @Override
    public TokenValidationResult validateToken(ValidateTokenCommand command) {
        Objects.requireNonNull(command, "command cannot be null");
        
        String accessToken = command.getAccessToken();
        
        // 토큰으로 인증 정보 조회
        Optional<Authentication> authOptional = authenticationRepository.findByAccessToken(accessToken);
        if (authOptional.isEmpty()) {
            return TokenValidationResult.invalid(accessToken, "Token not found");
        }
        
        Authentication authentication = authOptional.get();
        
        // 토큰 만료 여부 확인
        if (!authentication.isTokenValid()) {
            return TokenValidationResult.invalid(accessToken, "Token expired");
        }
        
        Token token = authentication.getToken();
        if (token == null) {
            return TokenValidationResult.invalid(accessToken, "No token associated with authentication");
        }
        
        // JWT 정책 검증
        if (!jwtPolicy.validate(token)) {
            return TokenValidationResult.invalid(accessToken, "JWT policy violation");
        }
        
        // 세션 정책 검증
        if (!sessionPolicy.validateSession(authentication)) {
            return TokenValidationResult.invalid(accessToken, "Session policy violation");
        }
        
        return TokenValidationResult.valid(accessToken);
    }
    
    @Override
    public Token refreshToken(RefreshTokenCommand command) {
        Objects.requireNonNull(command, "command cannot be null");
        
        String refreshToken = command.getRefreshToken();
        
        // 리프레시 토큰으로 인증 정보 조회
        Optional<Authentication> authOptional = authenticationRepository.findByRefreshToken(refreshToken);
        if (authOptional.isEmpty()) {
            throw new TokenRefreshException("Refresh token not found");
        }
        
        Authentication authentication = authOptional.get();
        
        // 토큰 만료 여부 확인
        if (!authentication.isTokenValid()) {
            throw new TokenRefreshException("Refresh token expired");
        }
        
        try {
            // 토큰 제공자를 통한 토큰 갱신
            Token newToken = tokenProvider.refreshToken(refreshToken);
            
            // 새 토큰으로 인증 정보 업데이트 (SUCCESS 상태에서 토큰만 업데이트)
            authentication.updateToken(newToken);
            authenticationRepository.save(authentication);
            
            return newToken;
            
        } catch (TokenProviderException e) {
            throw new TokenRefreshException("Failed to refresh token", e);
        }
    }
}