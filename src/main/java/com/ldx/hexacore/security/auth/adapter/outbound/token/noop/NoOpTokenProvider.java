package com.ldx.hexacore.security.auth.adapter.outbound.token.noop;

import com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenProviderException;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenProviderType;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenValidationResult;
import com.ldx.hexacore.security.auth.domain.vo.Credentials;
import com.ldx.hexacore.security.auth.domain.vo.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Set;

/**
 * 토큰 처리를 하지 않는 No-Op 구현체.
 * JWT 라이브러리가 없는 환경에서 기본 구현체로 사용됩니다.
 * 
 * @since 1.0.0
 */
public class NoOpTokenProvider implements TokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(NoOpTokenProvider.class);
    private static final String NO_OP_TOKEN = "no-op-token";

    @Override
    public Token issueToken(Credentials credentials) throws TokenProviderException {
        logger.debug("No-Op TokenProvider: Issuing mock token for username: {}", credentials.getUsername());
        String username = credentials.getUsername();
        return Token.of(
            NO_OP_TOKEN + "-access-" + username,
            NO_OP_TOKEN + "-refresh-" + username,
            3600L // 1 hour expiration
        );
    }

    @Override
    public TokenValidationResult validateToken(String accessToken) throws TokenProviderException {
        logger.debug("No-Op TokenProvider: Validating token (always returns valid)");
        String username = extractUsernameFromToken(accessToken);
        return TokenValidationResult.valid(
            username, // userId
            username, // username  
            Set.of("USER"), // authorities
            Instant.now().plusSeconds(3600) // expiresAt
        );
    }

    @Override
    public Token refreshToken(String refreshToken) throws TokenProviderException {
        String username = extractUsernameFromToken(refreshToken);
        logger.debug("No-Op TokenProvider: Refreshing token for username: {}", username);
        
        return Token.of(
            NO_OP_TOKEN + "-access-" + username,
            NO_OP_TOKEN + "-refresh-" + username,
            3600L // 1 hour expiration
        );
    }

    @Override
    public TokenProviderType getProviderType() {
        return TokenProviderType.SPRING_JWT;
    }

    private String extractUsernameFromToken(String token) {
        if (token != null && token.startsWith(NO_OP_TOKEN)) {
            String[] parts = token.split("-");
            if (parts.length >= 3) {
                return parts[2]; // username part
            }
        }
        return "anonymous";
    }
}