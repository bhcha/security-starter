package com.dx.hexacore.security.config.properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Properties Validation 테스트")
class PropertiesValidationTest {

    private HexacoreSecurityProperties properties;

    @BeforeEach
    void setUp() {
        properties = new HexacoreSecurityProperties();
    }

    @Test
    @DisplayName("기본 설정이 올바르게 설정된다")
    void defaultConfiguration_ShouldBeValid() {
        // Then
        assertThat(properties.getEnabled()).isTrue();
        assertThat(properties.getTokenProvider().getProvider()).isEqualTo("jwt");
        assertThat(properties.getTokenProvider().getJwt().getEnabled()).isTrue();
        assertThat(properties.getSession().getEnabled()).isTrue();
    }

    @Test
    @DisplayName("JWT 설정이 올바르게 동작한다")
    void jwtConfiguration_ShouldWork() {
        // Given
        var jwt = properties.getTokenProvider().getJwt();
        
        // When & Then
        assertThat(jwt.getSecret()).isNotEmpty();
        assertThat(jwt.getAccessTokenExpiration()).isEqualTo(3600);
        assertThat(jwt.getRefreshTokenExpiration()).isEqualTo(604800);
        assertThat(jwt.getAlgorithm()).isEqualTo("HS256");
        assertThat(jwt.getIssuer()).isEqualTo("security-starter");
    }

    @Test
    @DisplayName("Keycloak 설정이 올바르게 동작한다")
    void keycloakConfiguration_ShouldWork() {
        // Given
        var keycloak = properties.getTokenProvider().getKeycloak();
        
        // When
        keycloak.setEnabled(true);
        keycloak.setServerUrl("http://localhost:8080");
        keycloak.setRealm("test-realm");
        keycloak.setClientId("test-client");
        
        // Then
        assertThat(keycloak.getEnabled()).isTrue();
        assertThat(keycloak.getServerUrl()).isEqualTo("http://localhost:8080");
        assertThat(keycloak.getRealm()).isEqualTo("test-realm");
        assertThat(keycloak.getClientId()).isEqualTo("test-client");
    }

    @Test
    @DisplayName("세션 관리 설정이 올바르게 동작한다")
    void sessionConfiguration_ShouldWork() {
        // Given
        var session = properties.getSession();
        var lockout = session.getLockout();
        
        // Then
        assertThat(session.getEnabled()).isTrue();
        assertThat(lockout.getMaxAttempts()).isEqualTo(5);
        assertThat(lockout.getLockoutDurationMinutes()).isEqualTo(30);
        assertThat(lockout.getAttemptWindowMinutes()).isEqualTo(15);
    }

    @Test
    @DisplayName("캐시 설정이 올바르게 동작한다")
    void cacheConfiguration_ShouldWork() {
        // Given
        var cache = properties.getCache();
        
        // Then
        assertThat(cache.getEnabled()).isTrue();
        assertThat(cache.getType()).isEqualTo("caffeine");
        assertThat(cache.getCaffeine().getMaximumSize()).isEqualTo(10000);
        assertThat(cache.getCaffeine().getExpireAfterWriteSeconds()).isEqualTo(900);
    }

    @Test
    @DisplayName("토큰 제공자 변경이 올바르게 동작한다")
    void tokenProviderChange_ShouldWork() {
        // Given
        var tokenProvider = properties.getTokenProvider();
        
        // When
        tokenProvider.setProvider("keycloak");
        
        // Then
        assertThat(tokenProvider.getProvider()).isEqualTo("keycloak");
    }
}