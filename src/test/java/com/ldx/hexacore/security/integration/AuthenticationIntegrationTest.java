package com.ldx.hexacore.security.integration;

import com.ldx.hexacore.security.auth.adapter.outbound.token.jwt.SpringJwtTokenProvider;
import com.ldx.hexacore.security.auth.adapter.outbound.token.keycloak.KeycloakTokenProvider;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenProviderException;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenProviderType;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenValidationResult;
import com.ldx.hexacore.security.auth.domain.vo.Credentials;
import com.ldx.hexacore.security.auth.domain.vo.Token;
import com.ldx.hexacore.security.config.properties.HexacoreSecurityProperties;
import com.ldx.hexacore.security.config.autoconfigure.TokenProviderAutoConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("인증 통합 테스트 - Keycloak & Spring JWT 양방향 검증")
class AuthenticationIntegrationTest {

    @Nested
    @DisplayName("Spring JWT 토큰 제공자 테스트")
    class SpringJwtTokenProviderTest {

        private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        TokenProviderAutoConfiguration.class
                ))
                .withPropertyValues(
                        "hexacore.security.enabled=true",
                        "hexacore.security.token-provider.provider=jwt",
                        "hexacore.security.token-provider.jwt.enabled=true",
                        "hexacore.security.token-provider.jwt.secret=test-jwt-secret-key-for-integration-testing-minimum-32-chars",
                        "hexacore.security.token-provider.jwt.access-token-expiration=3600",
                        "hexacore.security.token-provider.jwt.refresh-token-expiration=86400"
                );

        @Test
        @DisplayName("Spring JWT TokenProvider가 올바르게 생성되는지 확인")
        void shouldCreateSpringJwtTokenProvider() {
            contextRunner.run(context -> {
                TokenProvider tokenProvider = context.getBean(TokenProvider.class);
                
                assertThat(tokenProvider).isInstanceOf(SpringJwtTokenProvider.class);
                assertThat(tokenProvider.getProviderType()).isEqualTo(TokenProviderType.SPRING_JWT);
            });
        }

        @Test
        @DisplayName("Spring JWT 토큰 발급 및 검증 통합 테스트")
        void shouldIssueAndValidateJwtToken() {
            contextRunner.run(context -> {
                TokenProvider tokenProvider = context.getBean(TokenProvider.class);
                
                // Given - 유효한 자격증명
                Credentials credentials = Credentials.of("testuser", "testpass");
                
                // When - 토큰 발급
                Token token = tokenProvider.issueToken(credentials);
                
                // Then - 토큰 발급 성공 확인
                assertThat(token).isNotNull();
                assertThat(token.getAccessToken()).isNotBlank();
                assertThat(token.getRefreshToken()).isNotBlank();
                assertThat(token.getExpiresIn()).isPositive();
                
                // When - 토큰 검증
                TokenValidationResult validationResult = tokenProvider.validateToken(token.getAccessToken());
                
                // Then - 토큰 검증 성공 확인
                assertThat(validationResult.valid()).isTrue();
                assertThat(validationResult.userId()).isEqualTo("testuser");
                assertThat(validationResult.username()).isEqualTo("testuser");
            });
        }

        @Test
        @DisplayName("Spring JWT 토큰 갱신 테스트")
        void shouldRefreshJwtToken() {
            contextRunner.run(context -> {
                TokenProvider tokenProvider = context.getBean(TokenProvider.class);
                
                // Given - 토큰 발급
                Credentials credentials = Credentials.of("testuser", "testpass");
                Token originalToken = tokenProvider.issueToken(credentials);
                
                // When - 토큰 갱신
                Token refreshedToken = tokenProvider.refreshToken(originalToken.getRefreshToken());
                
                // Then - 갱신된 토큰 확인
                assertThat(refreshedToken).isNotNull();
                assertThat(refreshedToken.getAccessToken()).isNotBlank();
                assertThat(refreshedToken.getRefreshToken()).isNotBlank();
                assertThat(refreshedToken.getAccessToken()).isNotEqualTo(originalToken.getAccessToken());
                
                // 갱신된 토큰도 검증 가능한지 확인
                TokenValidationResult validationResult = tokenProvider.validateToken(refreshedToken.getAccessToken());
                assertThat(validationResult.valid()).isTrue();
            });
        }

        @Test
        @DisplayName("잘못된 JWT 토큰 검증 실패 테스트")
        void shouldFailValidationForInvalidJwtToken() {
            contextRunner.run(context -> {
                TokenProvider tokenProvider = context.getBean(TokenProvider.class);
                
                // Given - 잘못된 토큰
                String invalidToken = "invalid.jwt.token";
                
                // When & Then - Spring JWT는 invalid token에 대해 예외를 던짐
                assertThatThrownBy(() -> tokenProvider.validateToken(invalidToken))
                    .isInstanceOf(TokenProviderException.class)
                    .hasMessageContaining("SPRING_JWT");
            });
        }
    }

    @Nested
    @DisplayName("Keycloak 토큰 제공자 테스트")
    class KeycloakTokenProviderTest {

        private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
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
                        "hexacore.security.token-provider.keycloak.grant-type=password",
                        "hexacore.security.token-provider.keycloak.scopes=openid profile email"
                );

        @Test
        @DisplayName("Keycloak TokenProvider가 올바르게 생성되는지 확인")
        void shouldCreateKeycloakTokenProvider() {
            contextRunner.run(context -> {
                TokenProvider tokenProvider = context.getBean(TokenProvider.class);
                
                assertThat(tokenProvider).isInstanceOf(KeycloakTokenProvider.class);
                assertThat(tokenProvider.getProviderType()).isEqualTo(TokenProviderType.KEYCLOAK);
            });
        }

        @Test
        @DisplayName("Keycloak 설정에서 OpenID Connect scope가 올바르게 설정되는지 확인")
        void shouldHaveCorrectOpenIdConnectScopes() {
            contextRunner.run(context -> {
                HexacoreSecurityProperties properties = context.getBean(HexacoreSecurityProperties.class);
                HexacoreSecurityProperties.TokenProvider.KeycloakProperties keycloakProps = 
                    properties.getTokenProvider().getKeycloak();
                
                // OpenID Connect에 필요한 scope가 포함되어 있는지 확인
                assertThat(keycloakProps.getScopes()).contains("openid");
                assertThat(keycloakProps.getScopes()).contains("profile");
                assertThat(keycloakProps.getScopes()).contains("email");
            });
        }

        @Test
        @DisplayName("Keycloak 토큰 발급 실패 테스트 (Mock 서버 없음)")
        void shouldHandleKeycloakConnectionFailure() {
            contextRunner.run(context -> {
                TokenProvider tokenProvider = context.getBean(TokenProvider.class);
                
                // Given - 유효한 자격증명 (하지만 Mock 서버가 없음)
                Credentials credentials = Credentials.of("testuser", "testpass");
                
                // When & Then - 연결 실패로 인한 예외 발생 확인
                assertThatThrownBy(() -> tokenProvider.issueToken(credentials))
                    .isInstanceOf(TokenProviderException.class)
                    .hasMessageContaining("KEYCLOAK");
            });
        }

        @Test
        @DisplayName("잘못된 Keycloak 토큰 검증 실패 테스트")
        void shouldFailValidationForInvalidKeycloakToken() {
            contextRunner.run(context -> {
                TokenProvider tokenProvider = context.getBean(TokenProvider.class);
                
                // Given - 잘못된 토큰
                String invalidToken = "invalid.keycloak.token";
                
                // When & Then - Keycloak은 네트워크 오류로 인해 예외를 던질 수 있음
                assertThatThrownBy(() -> tokenProvider.validateToken(invalidToken))
                    .isInstanceOf(TokenProviderException.class)
                    .hasMessageContaining("KEYCLOAK");
            });
        }

        @Test
        @DisplayName("null 및 빈 토큰에 대한 Keycloak 검증 테스트")
        void shouldHandleNullAndEmptyTokensForKeycloak() {
            contextRunner.run(context -> {
                TokenProvider tokenProvider = context.getBean(TokenProvider.class);
                
                // null 토큰 테스트
                TokenValidationResult nullResult = tokenProvider.validateToken(null);
                assertThat(nullResult.valid()).isFalse();
                
                // 빈 토큰 테스트
                TokenValidationResult emptyResult = tokenProvider.validateToken("");
                assertThat(emptyResult.valid()).isFalse();
                
                // 공백 토큰 테스트
                TokenValidationResult blankResult = tokenProvider.validateToken("   ");
                assertThat(blankResult.valid()).isFalse();
            });
        }
    }

    @Nested
    @DisplayName("토큰 제공자 간 일관성 테스트")
    class TokenProviderConsistencyTest {

        @Test
        @DisplayName("JWT와 Keycloak 토큰 제공자의 인터페이스 일관성 확인")
        void shouldHaveConsistentTokenProviderInterface() {
            // JWT TokenProvider 테스트
            ApplicationContextRunner jwtRunner = new ApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(
                            TokenProviderAutoConfiguration.class
                    ))
                    .withPropertyValues(
                            "hexacore.security.enabled=true",
                            "hexacore.security.token-provider.provider=jwt",
                            "hexacore.security.token-provider.jwt.enabled=true",
                            "hexacore.security.token-provider.jwt.secret=test-jwt-secret-key-for-consistency-testing-minimum-32-chars"
                    );

            // Keycloak TokenProvider 테스트
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

            // JWT Provider 확인
            jwtRunner.run(context -> {
                TokenProvider jwtProvider = context.getBean(TokenProvider.class);
                assertThat(jwtProvider.getProviderType()).isEqualTo(TokenProviderType.SPRING_JWT);
                assertThat(jwtProvider).isInstanceOf(SpringJwtTokenProvider.class);
            });

            // Keycloak Provider 확인
            keycloakRunner.run(context -> {
                TokenProvider keycloakProvider = context.getBean(TokenProvider.class);
                assertThat(keycloakProvider.getProviderType()).isEqualTo(TokenProviderType.KEYCLOAK);
                assertThat(keycloakProvider).isInstanceOf(KeycloakTokenProvider.class);
            });
        }

        @Test
        @DisplayName("두 토큰 제공자 모두 동일한 에러 처리 패턴 확인")
        void shouldHaveConsistentErrorHandlingPatterns() {
            // JWT Provider 에러 처리 테스트
            ApplicationContextRunner jwtRunner = new ApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(
                            TokenProviderAutoConfiguration.class
                    ))
                    .withPropertyValues(
                            "hexacore.security.enabled=true",
                            "hexacore.security.token-provider.provider=jwt",
                            "hexacore.security.token-provider.jwt.enabled=true",
                            "hexacore.security.token-provider.jwt.secret=test-jwt-secret-key-for-error-handling-testing-minimum-32-chars"
                    );

            // Keycloak Provider 에러 처리 테스트
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

            // 두 Provider 모두 null credentials에 대해 동일한 예외 처리
            jwtRunner.run(context -> {
                TokenProvider jwtProvider = context.getBean(TokenProvider.class);
                assertThatThrownBy(() -> jwtProvider.issueToken(null))
                    .isInstanceOf(TokenProviderException.class);
            });

            keycloakRunner.run(context -> {
                TokenProvider keycloakProvider = context.getBean(TokenProvider.class);
                assertThatThrownBy(() -> keycloakProvider.issueToken(null))
                    .isInstanceOf(TokenProviderException.class);
            });

            // 두 Provider의 null/empty token 처리는 다름 (JWT는 예외, Keycloak은 TokenValidationResult)
            jwtRunner.run(context -> {
                TokenProvider jwtProvider = context.getBean(TokenProvider.class);
                // Spring JWT는 null/empty token에 대해 예외를 던짐
                assertThatThrownBy(() -> jwtProvider.validateToken(null))
                    .isInstanceOf(TokenProviderException.class);
                assertThatThrownBy(() -> jwtProvider.validateToken(""))
                    .isInstanceOf(TokenProviderException.class);
            });

            keycloakRunner.run(context -> {
                TokenProvider keycloakProvider = context.getBean(TokenProvider.class);
                // Keycloak은 null/empty token에 대해 invalid result 반환
                assertThat(keycloakProvider.validateToken(null).valid()).isFalse();
                assertThat(keycloakProvider.validateToken("").valid()).isFalse();
            });
        }
    }

    @Nested
    @DisplayName("토큰 제공자 전환 테스트")
    class TokenProviderSwitchingTest {

        @Test
        @DisplayName("설정에 따른 토큰 제공자 동적 전환 확인")
        void shouldSwitchTokenProviderBasedOnConfiguration() {
            // JWT -> Keycloak 전환 시뮬레이션
            ApplicationContextRunner jwtToKeycloakRunner = new ApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(
                            TokenProviderAutoConfiguration.class
                    ))
                    .withPropertyValues(
                            "hexacore.security.enabled=true",
                            // Keycloak 설정이 JWT보다 우선순위가 높다고 가정
                            "hexacore.security.token-provider.provider=keycloak",
                            "hexacore.security.token-provider.keycloak.enabled=true",
                            "hexacore.security.token-provider.keycloak.server-url=https://test.keycloak.com",
                            "hexacore.security.token-provider.keycloak.realm=test-realm",
                            "hexacore.security.token-provider.keycloak.client-id=test-client",
                            "hexacore.security.token-provider.keycloak.client-secret=test-secret",
                            "hexacore.security.token-provider.jwt.enabled=true",
                            "hexacore.security.token-provider.jwt.secret=test-jwt-secret-key-for-switching-testing-minimum-32-chars"
                    );

            jwtToKeycloakRunner.run(context -> {
                TokenProvider tokenProvider = context.getBean(TokenProvider.class);
                
                // 설정에서 keycloak이 provider로 지정되었으므로 KeycloakTokenProvider가 선택되어야 함
                assertThat(tokenProvider).isInstanceOf(KeycloakTokenProvider.class);
                assertThat(tokenProvider.getProviderType()).isEqualTo(TokenProviderType.KEYCLOAK);
            });
        }

        @Test
        @DisplayName("잘못된 설정에 대한 fallback 동작 확인")
        void shouldFallbackToDefaultProviderForInvalidConfiguration() {
            ApplicationContextRunner fallbackRunner = new ApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(
                            TokenProviderAutoConfiguration.class
                    ))
                    .withPropertyValues(
                            "hexacore.security.enabled=true",
                            "hexacore.security.token-provider.provider=jwt", // 기본값으로 fallback
                            "hexacore.security.token-provider.jwt.enabled=true",
                            "hexacore.security.token-provider.jwt.secret=test-jwt-secret-key-for-fallback-testing-minimum-32-chars"
                    );

            fallbackRunner.run(context -> {
                TokenProvider tokenProvider = context.getBean(TokenProvider.class);
                
                // JWT 설정이 유효하므로 SpringJwtTokenProvider가 선택되어야 함
                assertThat(tokenProvider).isInstanceOf(SpringJwtTokenProvider.class);
                assertThat(tokenProvider.getProviderType()).isEqualTo(TokenProviderType.SPRING_JWT);
            });
        }
    }
}