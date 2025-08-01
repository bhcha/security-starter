package com.dx.hexacore.security.auth.adapter.outbound.token.jwt;

import com.dx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.dx.hexacore.security.auth.application.command.port.out.TokenProviderException;
import com.dx.hexacore.security.auth.application.command.port.out.TokenProviderErrorCode;
import com.dx.hexacore.security.auth.application.command.port.out.TokenProviderType;
import com.dx.hexacore.security.auth.application.command.port.out.TokenValidationResult;
import com.dx.hexacore.security.auth.domain.vo.Credentials;
import com.dx.hexacore.security.auth.domain.vo.Token;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;


public class SpringJwtTokenProvider implements TokenProvider {

    private static final String AUDIENCE = "hexacore-app";
    private static final String TOKEN_TYPE_CLAIM = "type";
    private static final String REFRESH_TOKEN_TYPE = "refresh";
    private static final String USERNAME_CLAIM = "username";
    
    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public SpringJwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = validateAndCreateSecretKey(jwtProperties.getSecret());
    }

    @Override
    public Token issueToken(Credentials credentials) throws TokenProviderException {
        if (credentials == null) {
            throw TokenProviderException.invalidCredentials("SPRING_JWT");
        }
        
        // Credentials 객체 자체에서 이미 검증되므로 추가 검증 불필요
        // 이미 유효한 credentials가 들어왔다고 가정

        try {
            String username = credentials.getUsername();
            Instant now = Instant.now();
            
            // Access Token 생성
            String accessToken = createAccessToken(username, now);
            
            // Refresh Token 생성
            String refreshToken = createRefreshToken(username, now);
            
            return Token.of(accessToken, refreshToken, jwtProperties.getAccessTokenExpiration());
            
        } catch (Exception e) {
            throw TokenProviderException.tokenIssueFailed("SPRING_JWT", e);
        }
    }

    @Override
    public TokenValidationResult validateToken(String accessToken) throws TokenProviderException {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            throw TokenProviderException.tokenValidationFailed("SPRING_JWT", 
                new IllegalArgumentException("Token cannot be null or empty"));
        }

        try {
            Claims claims = parseToken(accessToken);
            
            // 발급자 검증
            if (!jwtProperties.getIssuer().equals(claims.getIssuer())) {
                throw TokenProviderException.tokenValidationFailed("SPRING_JWT", 
                    new IllegalArgumentException("Invalid token issuer"));
            }
            
            // Audience 검증
            if (!claims.getAudience().contains(AUDIENCE)) {
                throw TokenProviderException.tokenValidationFailed("SPRING_JWT", 
                    new IllegalArgumentException("Invalid token audience"));
            }
            
            // Refresh 토큰인지 확인 (Access 토큰이어야 함)
            if (REFRESH_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
                throw TokenProviderException.tokenValidationFailed("SPRING_JWT", 
                    new IllegalArgumentException("Cannot validate refresh token as access token"));
            }
            
            String username = claims.get(USERNAME_CLAIM, String.class);
            String userId = claims.getSubject();
            Instant expiresAt = claims.getExpiration().toInstant();
            
            return TokenValidationResult.valid(userId, username, null, expiresAt);
            
        } catch (ExpiredJwtException e) {
            throw TokenProviderException.tokenExpired("SPRING_JWT");
        } catch (SignatureException e) {
            throw TokenProviderException.tokenValidationFailed("SPRING_JWT", e);
        } catch (MalformedJwtException e) {
            throw TokenProviderException.tokenValidationFailed("SPRING_JWT", e);
        } catch (UnsupportedJwtException e) {
            throw TokenProviderException.tokenValidationFailed("SPRING_JWT", e);
        } catch (JwtException e) {
            throw TokenProviderException.tokenValidationFailed("SPRING_JWT", e);
        }
    }

    @Override
    public Token refreshToken(String refreshToken) throws TokenProviderException {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw TokenProviderException.tokenRefreshFailed("SPRING_JWT", 
                new IllegalArgumentException("Refresh token cannot be null or empty"));
        }

        try {
            Claims claims = parseToken(refreshToken);
            
            // 발급자 검증
            if (!jwtProperties.getIssuer().equals(claims.getIssuer())) {
                throw TokenProviderException.tokenRefreshFailed("SPRING_JWT", 
                    new IllegalArgumentException("Invalid refresh token issuer"));
            }
            
            // Refresh 토큰인지 확인
            if (!REFRESH_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
                throw TokenProviderException.tokenRefreshFailed("SPRING_JWT", 
                    new IllegalArgumentException("Invalid refresh token type"));
            }
            
            String username = claims.getSubject();
            Instant now = Instant.now();
            
            // 새로운 토큰 발급
            String newAccessToken = createAccessToken(username, now);
            String newRefreshToken = createRefreshToken(username, now);
            
            return Token.of(newAccessToken, newRefreshToken, jwtProperties.getAccessTokenExpiration());
            
        } catch (ExpiredJwtException e) {
            throw TokenProviderException.tokenExpired("SPRING_JWT");
        } catch (SignatureException e) {
            throw TokenProviderException.tokenRefreshFailed("SPRING_JWT", e);
        } catch (MalformedJwtException e) {
            throw TokenProviderException.tokenRefreshFailed("SPRING_JWT", e);
        } catch (UnsupportedJwtException e) {
            throw TokenProviderException.tokenRefreshFailed("SPRING_JWT", e);
        } catch (JwtException e) {
            throw TokenProviderException.tokenRefreshFailed("SPRING_JWT", e);
        }
    }

    @Override
    public TokenProviderType getProviderType() {
        return TokenProviderType.SPRING_JWT;
    }

    private String createAccessToken(String username, Instant now) {
        Instant expiration = now.plus(jwtProperties.getAccessTokenExpiration(), ChronoUnit.SECONDS);
        
        return Jwts.builder()
            .issuer(jwtProperties.getIssuer())
            .subject(username)
            .audience().add(AUDIENCE).and()
            .expiration(Date.from(expiration))
            .issuedAt(Date.from(now))
            .id(UUID.randomUUID().toString())
            .claim(USERNAME_CLAIM, username)
            .signWith(secretKey)
            .compact();
    }

    private String createRefreshToken(String username, Instant now) {
        Instant expiration = now.plus(jwtProperties.getRefreshTokenExpiration(), ChronoUnit.SECONDS);
        
        return Jwts.builder()
            .issuer(jwtProperties.getIssuer())
            .subject(username)
            .expiration(Date.from(expiration))
            .issuedAt(Date.from(now))
            .id(UUID.randomUUID().toString())
            .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
            .signWith(secretKey)
            .compact();
    }

    private Claims parseToken(String token) throws JwtException {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private SecretKey validateAndCreateSecretKey(String secret) {
        if (secret == null) {
            throw new TokenProviderException("Secret key is required for JWT token provider", 
                TokenProviderErrorCode.CONFIGURATION_ERROR, "SPRING_JWT");
        }
        
        if (secret.length() < 32) {
            throw new TokenProviderException("Secret key must be at least 32 characters long for security", 
                TokenProviderErrorCode.CONFIGURATION_ERROR, "SPRING_JWT");
        }
        
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}