package com.ldx.hexacore.security.auth.adapter.outbound.token.jwt;

import com.ldx.hexacore.security.auth.application.command.port.out.TokenProviderException;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenProviderType;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenValidationResult;
import com.ldx.hexacore.security.auth.domain.vo.Credentials;
import com.ldx.hexacore.security.auth.domain.vo.Token;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class SpringJwtTokenProviderTest {

    private SpringJwtTokenProvider tokenProvider;
    private JwtProperties jwtProperties;
    private final String testSecret = "this-is-a-test-secret-key-for-jwt-signing-minimum-256-bits-long";

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        // enabled property was removed from JwtProperties
        jwtProperties.setSecret(testSecret);
        jwtProperties.setAccessTokenExpiration(3600);
        jwtProperties.setRefreshTokenExpiration(604800);
        jwtProperties.setIssuer("security-starter");
        
        tokenProvider = new SpringJwtTokenProvider(jwtProperties);
    }

    @Test
    void shouldReturnCorrectProviderType() {
        TokenProviderType type = tokenProvider.getProviderType();
        
        assertThat(type).isEqualTo(TokenProviderType.SPRING_JWT);
    }

    @Test
    void shouldIssueTokenSuccessfully() {
        Credentials credentials = Credentials.of("testuser", "password123");
        
        Token token = tokenProvider.issueToken(credentials);
        
        assertThat(token).isNotNull();
        assertThat(token.getAccessToken()).isNotBlank();
        assertThat(token.getRefreshToken()).isNotBlank();
        assertThat(token.getExpiresIn()).isEqualTo(3600);
    }

    @Test
    void shouldThrowExceptionForNullCredentials() {
        assertThatThrownBy(() -> tokenProvider.issueToken(null))
            .isInstanceOf(TokenProviderException.class);
    }

    @Test
    void shouldIssueTokenForValidCredentials() {
        // Credentials는 생성 시점에 이미 검증되므로, 유효한 Credentials로 테스트
        Credentials validCredentials = Credentials.of("validuser", "validpassword123");
        
        Token token = tokenProvider.issueToken(validCredentials);
        
        assertThat(token).isNotNull();
        assertThat(token.getAccessToken()).isNotBlank();
    }

    @Test
    void shouldValidateTokenSuccessfully() {
        Credentials credentials = Credentials.of("testuser", "password123");
        Token token = tokenProvider.issueToken(credentials);
        
        TokenValidationResult result = tokenProvider.validateToken(token.getAccessToken());
        
        assertThat(result.valid()).isTrue();
        assertThat(result.username()).isEqualTo("testuser");
        assertThat(result.expiresAt()).isAfter(Instant.now());
    }

    @Test
    void shouldFailValidationForNullToken() {
        assertThatThrownBy(() -> tokenProvider.validateToken(null))
            .isInstanceOf(TokenProviderException.class);
    }

    @Test
    void shouldFailValidationForEmptyToken() {
        assertThatThrownBy(() -> tokenProvider.validateToken(""))
            .isInstanceOf(TokenProviderException.class);
    }

    @Test
    void shouldFailValidationForMalformedToken() {
        assertThatThrownBy(() -> tokenProvider.validateToken("invalid-token-format"))
            .isInstanceOf(TokenProviderException.class);
    }

    @Test
    void shouldFailValidationForExpiredToken() {
        // 과거 시간으로 만료된 토큰 생성
        String expiredToken = createExpiredToken();
        
        assertThatThrownBy(() -> tokenProvider.validateToken(expiredToken))
            .isInstanceOf(TokenProviderException.class);
    }

    @Test
    void shouldFailValidationForTamperedToken() {
        Credentials credentials = Credentials.of("testuser", "password123");
        Token token = tokenProvider.issueToken(credentials);
        String tamperedToken = token.getAccessToken() + "tampered";
        
        assertThatThrownBy(() -> tokenProvider.validateToken(tamperedToken))
            .isInstanceOf(TokenProviderException.class);
    }

    @Test
    void shouldFailValidationForWrongIssuerToken() {
        String wrongIssuerToken = createTokenWithWrongIssuer();
        
        assertThatThrownBy(() -> tokenProvider.validateToken(wrongIssuerToken))
            .isInstanceOf(TokenProviderException.class);
    }

    @Test
    void shouldRefreshTokenSuccessfully() {
        Credentials credentials = Credentials.of("testuser", "password123");
        Token originalToken = tokenProvider.issueToken(credentials);
        
        Token refreshedToken = tokenProvider.refreshToken(originalToken.getRefreshToken());
        
        assertThat(refreshedToken).isNotNull();
        assertThat(refreshedToken.getAccessToken()).isNotBlank();
        assertThat(refreshedToken.getRefreshToken()).isNotBlank();
        assertThat(refreshedToken.getAccessToken()).isNotEqualTo(originalToken.getAccessToken());
        assertThat(refreshedToken.getRefreshToken()).isNotEqualTo(originalToken.getRefreshToken());
    }

    @Test
    void shouldFailRefreshForNullToken() {
        assertThatThrownBy(() -> tokenProvider.refreshToken(null))
            .isInstanceOf(TokenProviderException.class);
    }

    @Test
    void shouldFailRefreshForExpiredRefreshToken() {
        String expiredRefreshToken = createExpiredRefreshToken();
        
        assertThatThrownBy(() -> tokenProvider.refreshToken(expiredRefreshToken))
            .isInstanceOf(TokenProviderException.class);
    }

    @Test
    void shouldFailRefreshForTamperedRefreshToken() {
        Credentials credentials = Credentials.of("testuser", "password123");
        Token token = tokenProvider.issueToken(credentials);
        String tamperedRefreshToken = token.getRefreshToken() + "tampered";
        
        assertThatThrownBy(() -> tokenProvider.refreshToken(tamperedRefreshToken))
            .isInstanceOf(TokenProviderException.class);
    }

    @Test
    void shouldFailRefreshForAccessToken() {
        Credentials credentials = Credentials.of("testuser", "password123");
        Token token = tokenProvider.issueToken(credentials);
        
        // Access token을 refresh token으로 사용 시도
        assertThatThrownBy(() -> tokenProvider.refreshToken(token.getAccessToken()))
            .isInstanceOf(TokenProviderException.class);
    }

    @Test
    void shouldHandleJwtClaims() {
        Credentials credentials = Credentials.of("testuser", "password123");
        Token token = tokenProvider.issueToken(credentials);
        
        // JWT 토큰 파싱하여 클레임 확인
        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes());
        Claims claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token.getAccessToken())
            .getPayload();
        
        assertThat(claims.getIssuer()).isEqualTo("security-starter");
        assertThat(claims.getSubject()).isEqualTo("testuser");
        assertThat(claims.getAudience().iterator().next()).isEqualTo("hexacore-app");
        assertThat(claims.get("username", String.class)).isEqualTo("testuser");
        assertThat(claims.getId()).isNotNull(); // JTI
    }

    @Test
    void shouldHandleRefreshTokenClaims() {
        Credentials credentials = Credentials.of("testuser", "password123");
        Token token = tokenProvider.issueToken(credentials);
        
        // Refresh JWT 토큰 파싱하여 클레임 확인
        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes());
        Claims claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token.getRefreshToken())
            .getPayload();
        
        assertThat(claims.getIssuer()).isEqualTo("security-starter");
        assertThat(claims.getSubject()).isEqualTo("testuser");
        assertThat(claims.get("type", String.class)).isEqualTo("refresh");
        assertThat(claims.getId()).isNotNull(); // JTI
    }

    @Test
    void shouldGenerateUniqueTokensForConcurrentRequests() throws Exception {
        Credentials credentials = Credentials.of("testuser", "password123");
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        CompletableFuture<Token>[] futures = new CompletableFuture[100];
        for (int i = 0; i < 100; i++) {
            futures[i] = CompletableFuture.supplyAsync(() -> 
                tokenProvider.issueToken(credentials), executor);
        }
        
        Token[] tokens = new Token[100];
        for (int i = 0; i < 100; i++) {
            tokens[i] = futures[i].get();
        }
        
        // 모든 토큰이 고유한지 확인
        for (int i = 0; i < 100; i++) {
            for (int j = i + 1; j < 100; j++) {
                assertThat(tokens[i].getAccessToken()).isNotEqualTo(tokens[j].getAccessToken());
                assertThat(tokens[i].getRefreshToken()).isNotEqualTo(tokens[j].getRefreshToken());
            }
        }
        
        executor.shutdown();
    }

    @Test
    void shouldThrowExceptionForShortSecretKey() {
        JwtProperties shortSecretProperties = new JwtProperties();
        shortSecretProperties.setSecret("short");
        
        assertThatThrownBy(() -> new SpringJwtTokenProvider(shortSecretProperties))
            .isInstanceOf(TokenProviderException.class);
    }

    @Test
    void shouldThrowExceptionForNullSecret() {
        JwtProperties nullSecretProperties = new JwtProperties();
        nullSecretProperties.setSecret(null);
        
        assertThatThrownBy(() -> new SpringJwtTokenProvider(nullSecretProperties))
            .isInstanceOf(TokenProviderException.class);
    }

    private String createExpiredToken() {
        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes());
        Instant pastTime = Instant.now().minus(1, ChronoUnit.HOURS);
        
        return Jwts.builder()
            .issuer("security-starter")
            .subject("testuser")
            .audience().add("hexacore-app").and()
            .expiration(Date.from(pastTime))
            .issuedAt(Date.from(pastTime.minus(1, ChronoUnit.HOURS)))
            .id("expired-token-id")
            .claim("username", "testuser")
            .signWith(key)
            .compact();
    }

    private String createExpiredRefreshToken() {
        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes());
        Instant pastTime = Instant.now().minus(1, ChronoUnit.HOURS);
        
        return Jwts.builder()
            .issuer("security-starter")
            .subject("testuser")
            .expiration(Date.from(pastTime))
            .issuedAt(Date.from(pastTime.minus(1, ChronoUnit.HOURS)))
            .id("expired-refresh-token-id")
            .claim("type", "refresh")
            .signWith(key)
            .compact();
    }

    private String createTokenWithWrongIssuer() {
        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes());
        Instant futureTime = Instant.now().plus(1, ChronoUnit.HOURS);
        
        return Jwts.builder()
            .issuer("wrong-issuer")
            .subject("testuser")
            .audience().add("hexacore-app").and()
            .expiration(Date.from(futureTime))
            .issuedAt(Date.from(Instant.now()))
            .id("wrong-issuer-token-id")
            .claim("username", "testuser")
            .signWith(key)
            .compact();
    }
}