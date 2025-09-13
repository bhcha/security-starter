package com.ldx.hexacore.security.auth.application.command.handler;

import com.ldx.hexacore.security.auth.application.command.port.in.RefreshTokenCommand;
import com.ldx.hexacore.security.auth.application.command.port.in.TokenManagementUseCase;
import com.ldx.hexacore.security.auth.application.command.port.in.TokenRefreshException;
import com.ldx.hexacore.security.auth.application.command.port.in.ValidateTokenCommand;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenProviderException;
import com.ldx.hexacore.security.auth.domain.vo.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

/**
 * 토큰 관리 사용 사례 구현체.
 * 토큰 검증 및 갱신을 담당하는 애플리케이션 서비스입니다.
 * 
 * @since 1.0.0
 */
class TokenManagementUseCaseImpl implements TokenManagementUseCase {

    private static final Logger log = LoggerFactory.getLogger(TokenManagementUseCaseImpl.class);

    private final TokenProvider tokenProvider;
    
    /**
     * 토큰 관리 사용 사례를 생성합니다.
     *
     * @param tokenProvider 토큰 제공자
     */
    public TokenManagementUseCaseImpl(TokenProvider tokenProvider) {
        this.tokenProvider = Objects.requireNonNull(tokenProvider, "tokenProvider cannot be null");
    }
    
    @Override
    public com.ldx.hexacore.security.auth.application.command.port.in.TokenValidationResult validateToken(ValidateTokenCommand command) {
        Objects.requireNonNull(command, "command cannot be null");

        String accessToken = command.getAccessToken();
        log.debug("Validating token");

        try {
            // TokenProvider를 통한 토큰 검증 (JWT 서명 및 만료 검증)
            com.ldx.hexacore.security.auth.application.command.port.out.TokenValidationResult result = tokenProvider.validateToken(accessToken);

            if (result.valid()) {
                log.debug("Token validation successful for user: {}", result.username());
                return com.ldx.hexacore.security.auth.application.command.port.in.TokenValidationResult.valid(accessToken);
            } else {
                String reason = result.claims() != null && result.claims().get("error") != null
                    ? result.claims().get("error").toString()
                    : "Token validation failed";
                log.debug("Token validation failed: {}", reason);
                return com.ldx.hexacore.security.auth.application.command.port.in.TokenValidationResult.invalid(
                    accessToken,
                    reason
                );
            }
        } catch (TokenProviderException e) {
            log.warn("Token validation error: {}", e.getMessage());
            return com.ldx.hexacore.security.auth.application.command.port.in.TokenValidationResult.invalid(
                accessToken,
                "Token validation failed: " + e.getMessage()
            );
        }
    }
    
    @Override
    public Token refreshToken(RefreshTokenCommand command) {
        Objects.requireNonNull(command, "command cannot be null");

        String refreshToken = command.getRefreshToken();
        log.debug("Refreshing token");

        try {
            // TokenProvider를 통한 토큰 갱신 (리프레시 토큰 검증 및 새 토큰 발급)
            Token newToken = tokenProvider.refreshToken(refreshToken);

            log.info("Token refreshed successfully");
            return newToken;

        } catch (TokenProviderException e) {
            log.warn("Token refresh failed: {}", e.getMessage());
            throw new TokenRefreshException("Failed to refresh token: " + e.getMessage(), e);
        }
    }
}