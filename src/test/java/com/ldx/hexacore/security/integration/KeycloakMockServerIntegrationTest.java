package com.ldx.hexacore.security.integration;

import com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenProviderException;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenValidationResult;
import com.ldx.hexacore.security.auth.domain.vo.Credentials;
import com.ldx.hexacore.security.auth.domain.vo.Token;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ldx.hexacore.security.config.autoconfigure.TokenProviderAutoConfiguration;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Keycloak Mock 서버 통합 테스트")
class KeycloakMockServerIntegrationTest {

    private MockWebServer mockWebServer;
    private ObjectMapper objectMapper;
    private String baseUrl;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        objectMapper = new ObjectMapper();
        baseUrl = mockWebServer.url("/").toString();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("Keycloak 토큰 발급 성공 테스트 - OpenID Connect scope 포함")
    void shouldIssueTokenWithOpenIdConnectScope() throws Exception {
        // Given - Keycloak 토큰 응답 Mock
        Map<String, Object> tokenResponse = new HashMap<>();
        tokenResponse.put("access_token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");
        tokenResponse.put("refresh_token", "refresh_token_value");
        tokenResponse.put("expires_in", 3600);
        tokenResponse.put("scope", "openid profile email");

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(tokenResponse)));

        ApplicationContextRunner contextRunner = createKeycloakContextRunner();

        contextRunner.run(context -> {
            TokenProvider tokenProvider = context.getBean(TokenProvider.class);
            Credentials credentials = Credentials.of("testuser", "testpass");

            // When - 토큰 발급
            Token token = tokenProvider.issueToken(credentials);

            // Then - 토큰 발급 성공 확인
            assertThat(token).isNotNull();
            assertThat(token.getAccessToken()).isEqualTo("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");
            assertThat(token.getRefreshToken()).isEqualTo("refresh_token_value");
            assertThat(token.getExpiresIn()).isEqualTo(3600L);

            // 요청에 OpenID Connect scope가 포함되었는지 확인
            RecordedRequest recordedRequest = mockWebServer.takeRequest();
            String requestBody = recordedRequest.getBody().readUtf8();
            
            assertThat(requestBody).contains("scope=openid+profile+email");
            assertThat(requestBody).contains("grant_type=password");
            assertThat(requestBody).contains("username=testuser");
            assertThat(requestBody).contains("password=testpass");
        });
    }

    @Test
    @DisplayName("Keycloak 토큰 검증 성공 테스트 - userinfo 엔드포인트")
    void shouldValidateTokenWithUserInfoEndpoint() throws Exception {
        // Given - userinfo 응답 Mock
        Map<String, Object> userInfoResponse = new HashMap<>();
        userInfoResponse.put("sub", "123456789");
        userInfoResponse.put("preferred_username", "testuser");
        userInfoResponse.put("email", "testuser@example.com");
        userInfoResponse.put("name", "Test User");

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(userInfoResponse)));

        ApplicationContextRunner contextRunner = createKeycloakContextRunner();

        contextRunner.run(context -> {
            TokenProvider tokenProvider = context.getBean(TokenProvider.class);
            String accessToken = "valid_access_token";

            // When - 토큰 검증
            TokenValidationResult result = tokenProvider.validateToken(accessToken);

            // Then - 검증 성공 확인
            assertThat(result.valid()).isTrue();
            assertThat(result.userId()).isEqualTo("123456789");
            assertThat(result.username()).isEqualTo("testuser");
            assertThat(result.claims()).containsKey("email");

            // userinfo 엔드포인트 호출 확인
            RecordedRequest recordedRequest = mockWebServer.takeRequest();
            assertThat(recordedRequest.getPath()).contains("/realms/test-realm/protocol/openid-connect/userinfo");
            assertThat(recordedRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer valid_access_token");
        });
    }

    @Test
    @DisplayName("Keycloak 토큰 검증 실패 후 introspection fallback 테스트")
    void shouldFallbackToIntrospectionWhenUserInfoFails() throws Exception {
        // Given - userinfo 403 FORBIDDEN 응답
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(403)
                .setBody("Forbidden"));

        // introspection 성공 응답
        Map<String, Object> introspectionResponse = new HashMap<>();
        introspectionResponse.put("active", true);
        introspectionResponse.put("sub", "123456789");
        introspectionResponse.put("username", "testuser");

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(introspectionResponse)));

        ApplicationContextRunner contextRunner = createKeycloakContextRunner();

        contextRunner.run(context -> {
            TokenProvider tokenProvider = context.getBean(TokenProvider.class);
            String accessToken = "token_without_openid_scope";

            // When - 토큰 검증
            TokenValidationResult result = tokenProvider.validateToken(accessToken);

            // Then - introspection으로 검증 성공 확인
            assertThat(result.valid()).isTrue();
            assertThat(result.userId()).isEqualTo("123456789");
            assertThat(result.username()).isEqualTo("testuser");
            assertThat(result.claims()).containsKey("introspection");

            // 두 번의 요청이 있었는지 확인 (userinfo -> introspection)
            assertThat(mockWebServer.getRequestCount()).isEqualTo(2);

            // 첫 번째 요청: userinfo
            RecordedRequest userInfoRequest = mockWebServer.takeRequest();
            assertThat(userInfoRequest.getPath()).contains("/userinfo");

            // 두 번째 요청: introspection
            RecordedRequest introspectionRequest = mockWebServer.takeRequest();
            assertThat(introspectionRequest.getPath()).contains("/token/introspect");
            
            String introspectionBody = introspectionRequest.getBody().readUtf8();
            assertThat(introspectionBody).contains("token=token_without_openid_scope");
        });
    }

    @Test
    @DisplayName("Keycloak 토큰 갱신 테스트 - scope 유지")
    void shouldRefreshTokenWithSameScope() throws Exception {
        // Given - 토큰 갱신 응답 Mock
        Map<String, Object> refreshResponse = new HashMap<>();
        refreshResponse.put("access_token", "new_access_token");
        refreshResponse.put("refresh_token", "new_refresh_token");
        refreshResponse.put("expires_in", 3600);
        refreshResponse.put("scope", "openid profile email");

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(refreshResponse)));

        ApplicationContextRunner contextRunner = createKeycloakContextRunner();

        contextRunner.run(context -> {
            TokenProvider tokenProvider = context.getBean(TokenProvider.class);
            String refreshToken = "valid_refresh_token";

            // When - 토큰 갱신
            Token newToken = tokenProvider.refreshToken(refreshToken);

            // Then - 갱신 성공 확인
            assertThat(newToken).isNotNull();
            assertThat(newToken.getAccessToken()).isEqualTo("new_access_token");
            assertThat(newToken.getRefreshToken()).isEqualTo("new_refresh_token");

            // 갱신 요청에 scope가 포함되었는지 확인
            RecordedRequest recordedRequest = mockWebServer.takeRequest();
            String requestBody = recordedRequest.getBody().readUtf8();
            
            assertThat(requestBody).contains("scope=openid+profile+email");
            assertThat(requestBody).contains("grant_type=refresh_token");
            assertThat(requestBody).contains("refresh_token=valid_refresh_token");
        });
    }

    @Test
    @DisplayName("Keycloak 인증 실패 테스트")
    void shouldHandleAuthenticationFailure() throws Exception {
        // Given - 401 Unauthorized 응답
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "invalid_grant");
        errorResponse.put("error_description", "Invalid user credentials");

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(errorResponse)));

        ApplicationContextRunner contextRunner = createKeycloakContextRunner();

        contextRunner.run(context -> {
            TokenProvider tokenProvider = context.getBean(TokenProvider.class);
            Credentials invalidCredentials = Credentials.of("invaliduser", "wrongpass");

            // When & Then - 인증 실패 예외 확인
            assertThatThrownBy(() -> tokenProvider.issueToken(invalidCredentials))
                .isInstanceOf(TokenProviderException.class)
                .hasMessageContaining("KEYCLOAK");
        });
    }

    @Test
    @DisplayName("Keycloak 서버 오류 테스트")
    void shouldHandleServerError() throws Exception {
        // Given - 500 Internal Server Error 응답
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        ApplicationContextRunner contextRunner = createKeycloakContextRunner();

        contextRunner.run(context -> {
            TokenProvider tokenProvider = context.getBean(TokenProvider.class);
            Credentials credentials = Credentials.of("testuser", "testpass");

            // When & Then - 서버 오류 예외 확인
            assertThatThrownBy(() -> tokenProvider.issueToken(credentials))
                .isInstanceOf(TokenProviderException.class)
                .hasMessageContaining("KEYCLOAK");
        });
    }

    private ApplicationContextRunner createKeycloakContextRunner() {
        return new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        TokenProviderAutoConfiguration.class
                ))
                .withPropertyValues(
                        "hexacore.security.enabled=true",
                        "hexacore.security.token-provider.provider=keycloak",
                        "hexacore.security.token-provider.keycloak.enabled=true",
                        "hexacore.security.token-provider.keycloak.server-url=" + baseUrl,
                        "hexacore.security.token-provider.keycloak.realm=test-realm",
                        "hexacore.security.token-provider.keycloak.client-id=test-client",
                        "hexacore.security.token-provider.keycloak.client-secret=test-secret",
                        "hexacore.security.token-provider.keycloak.grant-type=password",
                        "hexacore.security.token-provider.keycloak.scopes=openid profile email"
                );
    }
}