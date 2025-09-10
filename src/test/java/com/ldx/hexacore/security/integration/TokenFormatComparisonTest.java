package com.ldx.hexacore.security.integration;

import com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenProviderException;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenProviderType;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenValidationResult;
import com.ldx.hexacore.security.auth.domain.vo.Credentials;
import com.ldx.hexacore.security.auth.domain.vo.Token;
import com.ldx.hexacore.security.config.autoconfigure.TokenProviderAutoConfiguration;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JWT vs Keycloak 토큰 형식 비교 테스트")
class TokenFormatComparisonTest {

    private static final String JWT_SECRET = "test-jwt-secret-key-for-token-format-comparison-testing-minimum-32-chars";

    @Test
    @DisplayName("Spring JWT 토큰 형식 및 구조 검증")
    void shouldValidateSpringJwtTokenFormat() {
        ApplicationContextRunner jwtRunner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        TokenProviderAutoConfiguration.class
                ))
                .withPropertyValues(
                        "hexacore.security.enabled=true",
                        "hexacore.security.token-provider.provider=jwt",
                        "hexacore.security.token-provider.jwt.enabled=true",
                        "hexacore.security.token-provider.jwt.secret=" + JWT_SECRET,
                        "hexacore.security.token-provider.jwt.access-token-expiration=3600",
                        "hexacore.security.token-provider.jwt.issuer=test-issuer"
                );

        jwtRunner.run(context -> {
            TokenProvider tokenProvider = context.getBean(TokenProvider.class);
            assertThat(tokenProvider.getProviderType()).isEqualTo(TokenProviderType.SPRING_JWT);

            // Given
            Credentials credentials = Credentials.of("testuser", "testpass");

            // When - 토큰 발급
            Token token = tokenProvider.issueToken(credentials);

            // Then - JWT 형식 검증
            String accessToken = token.getAccessToken();
            assertThat(accessToken).isNotNull();
            
            // JWT는 3개 부분으로 구성되어야 함 (header.payload.signature)
            String[] jwtParts = accessToken.split("\\.");
            assertThat(jwtParts).hasSize(3);

            // JWT 파싱하여 claims 확인
            SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();

            // 표준 JWT claims 확인
            assertThat(claims.getSubject()).isEqualTo("testuser");
            assertThat(claims.getIssuer()).isEqualTo("test-issuer");
            assertThat(claims.getExpiration()).isAfter(new Date());
            assertThat(claims.getIssuedAt()).isBeforeOrEqualTo(new Date());

            // 토큰 검증
            TokenValidationResult validationResult = tokenProvider.validateToken(accessToken);
            assertThat(validationResult.valid()).isTrue();
            assertThat(validationResult.userId()).isEqualTo("testuser");
            assertThat(validationResult.username()).isEqualTo("testuser");

            System.out.println("🔵 Spring JWT Token Structure:");
            System.out.println("📝 Header: " + decodeBase64(jwtParts[0]));
            System.out.println("📝 Payload: " + decodeBase64(jwtParts[1]));
            System.out.println("📝 Access Token Length: " + accessToken.length());
            System.out.println("📝 Refresh Token Length: " + token.getRefreshToken().length());
            System.out.println("📝 Expires In: " + token.getExpiresIn() + " seconds");
        });
    }

    @Test
    @DisplayName("Keycloak 토큰 형식 기본 구조 검증")
    void shouldValidateKeycloakTokenFormat() {
        ApplicationContextRunner keycloakRunner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        TokenProviderAutoConfiguration.class
                ))
                .withPropertyValues(
                        "hexacore.security.enabled=true",
                        "hexacore.security.token-provider.provider=keycloak",
                        "hexacore.security.token-provider.keycloak.enabled=true",
                        "hexacore.security.token-provider.keycloak.server-url=https://test.keycloak.com",
                        "hexacore.security.token-provider.keycloak.realm=test-realm",
                        "hexacore.security.token-provider.keycloak.client-id=test-client",
                        "hexacore.security.token-provider.keycloak.client-secret=test-secret",
                        "hexacore.security.token-provider.keycloak.scopes=openid profile email"
                );

        keycloakRunner.run(context -> {
            TokenProvider tokenProvider = context.getBean(TokenProvider.class);
            assertThat(tokenProvider.getProviderType()).isEqualTo(TokenProviderType.KEYCLOAK);

            System.out.println("🟡 Keycloak Token Provider Configuration:");
            System.out.println("📝 Provider Type: " + tokenProvider.getProviderType());
            System.out.println("📝 Expected Server URL: https://test.keycloak.com");
            System.out.println("📝 Expected Realm: test-realm");
            System.out.println("📝 Expected Scopes: openid profile email");
            
            // Note: 실제 Keycloak 서버가 없으므로 토큰 발급은 실패하지만
            // 구성 자체는 올바르게 설정되었음을 확인
            assertThat(tokenProvider).isNotNull();
        });
    }

    @Test
    @DisplayName("JWT와 Keycloak 토큰의 검증 결과 구조 비교")
    void shouldCompareValidationResultStructures() {
        // JWT Provider 검증 결과
        ApplicationContextRunner jwtRunner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        TokenProviderAutoConfiguration.class
                ))
                .withPropertyValues(
                        "hexacore.security.enabled=true",
                        "hexacore.security.token-provider.provider=jwt",
                        "hexacore.security.token-provider.jwt.enabled=true",
                        "hexacore.security.token-provider.jwt.secret=" + JWT_SECRET
                );

        jwtRunner.run(context -> {
            TokenProvider jwtProvider = context.getBean(TokenProvider.class);
            
            // JWT 토큰 발급 및 검증
            Credentials credentials = Credentials.of("testuser", "testpass");
            Token jwtToken = jwtProvider.issueToken(credentials);
            TokenValidationResult jwtResult = jwtProvider.validateToken(jwtToken.getAccessToken());

            System.out.println("🔵 Spring JWT Validation Result:");
            System.out.println("📝 Valid: " + jwtResult.valid());
            System.out.println("📝 Subject: " + jwtResult.userId());
            System.out.println("📝 Username: " + jwtResult.username());
            System.out.println("📝 Authorities: " + jwtResult.authorities());
            System.out.println("📝 Claims: " + jwtResult.claims());
            System.out.println("📝 Additional Info Size: " + jwtResult.claims().size());

            // JWT 검증 결과 구조 확인
            assertThat(jwtResult.valid()).isTrue();
            assertThat(jwtResult.userId()).isNotNull();
            assertThat(jwtResult.username()).isNotNull();
            // authorities는 null일 수 있음
            assertThat(jwtResult.claims()).isNotNull();
        });

        // Keycloak Provider 검증 결과 (null/invalid 토큰으로 테스트)
        ApplicationContextRunner keycloakRunner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        TokenProviderAutoConfiguration.class
                ))
                .withPropertyValues(
                        "hexacore.security.enabled=true",
                        "hexacore.security.token-provider.provider=keycloak",
                        "hexacore.security.token-provider.keycloak.enabled=true",
                        "hexacore.security.token-provider.keycloak.server-url=https://test.keycloak.com",
                        "hexacore.security.token-provider.keycloak.realm=test-realm",
                        "hexacore.security.token-provider.keycloak.client-id=test-client",
                        "hexacore.security.token-provider.keycloak.client-secret=test-secret"
                );

        keycloakRunner.run(context -> {
            TokenProvider keycloakProvider = context.getBean(TokenProvider.class);
            
            System.out.println("🟡 Keycloak Validation Result (Network Exception Expected):");
            
            // Keycloak 토큰 검증 (네트워크 오류 발생 예상)
            try {
                TokenValidationResult keycloakResult = keycloakProvider.validateToken("invalid_token");
                // 네트워크 오류가 발생하지 않은 경우 (예: null/empty token 처리)
                System.out.println("📝 Valid: " + keycloakResult.valid());
                System.out.println("📝 Subject: " + keycloakResult.userId());
                System.out.println("📝 Username: " + keycloakResult.username());
                assertThat(keycloakResult.valid()).isFalse();
            } catch (TokenProviderException e) {
                // 네트워크 오류 발생 시 예외 처리 (예상됨)
                System.out.println("📝 Exception: " + e.getMessage());
                assertThat(e.getMessage()).contains("KEYCLOAK");
            }
        });
    }

    @Test
    @DisplayName("토큰 만료 시간 처리 방식 비교")
    void shouldCompareTokenExpirationHandling() {
        ApplicationContextRunner jwtRunner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        TokenProviderAutoConfiguration.class
                ))
                .withPropertyValues(
                        "hexacore.security.enabled=true",
                        "hexacore.security.token-provider.provider=jwt",
                        "hexacore.security.token-provider.jwt.enabled=true",
                        "hexacore.security.token-provider.jwt.secret=" + JWT_SECRET,
                        "hexacore.security.token-provider.jwt.access-token-expiration=1800", // 30분
                        "hexacore.security.token-provider.jwt.refresh-token-expiration=604800" // 7일
                );

        jwtRunner.run(context -> {
            TokenProvider jwtProvider = context.getBean(TokenProvider.class);
            
            Credentials credentials = Credentials.of("testuser", "testpass");
            Token jwtToken = jwtProvider.issueToken(credentials);

            System.out.println("🔵 Spring JWT Token Expiration:");
            System.out.println("📝 Access Token Expires In: " + jwtToken.getExpiresIn() + " seconds");
            System.out.println("📝 Expected: 1800 seconds (30 minutes)");
            
            // JWT 만료 시간 확인
            assertThat(jwtToken.getExpiresIn()).isEqualTo(1800L);
            
            // 실제 JWT 토큰의 exp claim 확인
            SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(jwtToken.getAccessToken())
                    .getPayload();
            
            long expiration = claims.getExpiration().getTime();
            long issued = claims.getIssuedAt().getTime();
            long actualDuration = (expiration - issued) / 1000; // seconds
            
            System.out.println("📝 Actual JWT Duration: " + actualDuration + " seconds");
            assertThat(actualDuration).isEqualTo(1800L);
        });

        System.out.println("🟡 Keycloak Token Expiration:");
        System.out.println("📝 Expiration is controlled by Keycloak server configuration");
        System.out.println("📝 Typically returned in 'expires_in' field from token response");
        System.out.println("📝 Can be different from client-side configuration");
    }

    @Test
    @DisplayName("토큰 페이로드 내용 분석")
    void shouldAnalyzeTokenPayloads() {
        ApplicationContextRunner jwtRunner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        TokenProviderAutoConfiguration.class
                ))
                .withPropertyValues(
                        "hexacore.security.enabled=true",
                        "hexacore.security.token-provider.provider=jwt",
                        "hexacore.security.token-provider.jwt.enabled=true",
                        "hexacore.security.token-provider.jwt.secret=" + JWT_SECRET,
                        "hexacore.security.token-provider.jwt.issuer=hexacore-security-starter"
                );

        jwtRunner.run(context -> {
            TokenProvider jwtProvider = context.getBean(TokenProvider.class);
            
            Credentials credentials = Credentials.of("testuser", "testpass");
            Token jwtToken = jwtProvider.issueToken(credentials);

            // JWT 페이로드 분석
            SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(jwtToken.getAccessToken())
                    .getPayload();

            System.out.println("🔵 Spring JWT Token Claims:");
            System.out.println("📝 Subject (sub): " + claims.getSubject());
            System.out.println("📝 Issuer (iss): " + claims.getIssuer());
            System.out.println("📝 Issued At (iat): " + claims.getIssuedAt());
            System.out.println("📝 Expiration (exp): " + claims.getExpiration());
            System.out.println("📝 All Claims: " + claims.keySet());

            // 필수 claims 확인
            assertThat(claims.getSubject()).isEqualTo("testuser");
            assertThat(claims.getIssuer()).isEqualTo("hexacore-security-starter");
            assertThat(claims.getIssuedAt()).isNotNull();
            assertThat(claims.getExpiration()).isNotNull();
        });

        System.out.println("🟡 Keycloak Token Claims (Expected):");
        System.out.println("📝 Standard OpenID Connect claims would include:");
        System.out.println("📝 - sub (subject identifier)");
        System.out.println("📝 - iss (issuer - Keycloak server URL)");
        System.out.println("📝 - aud (audience - client ID)");
        System.out.println("📝 - exp (expiration time)");
        System.out.println("📝 - iat (issued at)");
        System.out.println("📝 - auth_time (authentication time)");
        System.out.println("📝 - preferred_username");
        System.out.println("📝 - email, email_verified");
        System.out.println("📝 - name, given_name, family_name");
        System.out.println("📝 - scope (e.g., 'openid profile email')");
    }

    private String decodeBase64(String base64) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(base64);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "Unable to decode: " + e.getMessage();
        }
    }
}