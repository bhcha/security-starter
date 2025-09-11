package com.ldx.hexacore.security.integration;

import com.ldx.hexacore.security.auth.adapter.outbound.token.keycloak.KeycloakTokenProvider;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenProviderType;
import com.ldx.hexacore.security.config.properties.SecurityStarterProperties;
import com.ldx.hexacore.security.config.autoconfigure.TokenProviderAutoConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Keycloak 통합 테스트")
class KeycloakIntegrationTest {

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
    @DisplayName("Keycloak 설정으로 컨텍스트가 로드되는지 확인")
    void contextLoadsWithKeycloakConfiguration() {
        contextRunner.run(context -> {
            assertThat(context).isNotNull();
            assertThat(context).hasSingleBean(TokenProvider.class);
        });
    }

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
    @DisplayName("Keycloak 설정 프로퍼티가 올바르게 바인딩되는지 확인")
    void shouldBindKeycloakPropertiesCorrectly() {
        contextRunner.run(context -> {
            SecurityStarterProperties properties = context.getBean(SecurityStarterProperties.class);
            SecurityStarterProperties.TokenProvider.KeycloakProperties keycloakProps = 
                properties.getTokenProvider().getKeycloak();
            
            assertThat(keycloakProps.getEnabled()).isTrue();
            assertThat(keycloakProps.getServerUrl()).isEqualTo("https://test.keycloak.com");
            assertThat(keycloakProps.getRealm()).isEqualTo("test-realm");
            assertThat(keycloakProps.getClientId()).isEqualTo("test-client");
            assertThat(keycloakProps.getClientSecret()).isEqualTo("test-secret");
            assertThat(keycloakProps.getGrantType()).isEqualTo("password");
            assertThat(keycloakProps.getScopes()).isEqualTo("openid profile email");
        });
    }

    @Test
    @DisplayName("기본 scope 설정이 올바르게 적용되는지 확인")
    void shouldHaveDefaultScopeConfiguration() {
        ApplicationContextRunner runnerWithDefaults = new ApplicationContextRunner()
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
                        // scopes 설정 없음 - 기본값 사용
                );

        runnerWithDefaults.run(context -> {
            SecurityStarterProperties properties = context.getBean(SecurityStarterProperties.class);
            SecurityStarterProperties.TokenProvider.KeycloakProperties keycloakProps = 
                properties.getTokenProvider().getKeycloak();
            
            // 기본값으로 openid profile email이 설정되어야 함
            assertThat(keycloakProps.getScopes()).isEqualTo("openid profile email");
        });
    }

    @Test
    @DisplayName("커스텀 scope 설정이 올바르게 적용되는지 확인")
    void shouldApplyCustomScopeConfiguration() {
        ApplicationContextRunner runnerWithCustomScopes = new ApplicationContextRunner()
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
                        "hexacore.security.token-provider.keycloak.scopes=openid profile email roles"
                );

        runnerWithCustomScopes.run(context -> {
            SecurityStarterProperties properties = context.getBean(SecurityStarterProperties.class);
            SecurityStarterProperties.TokenProvider.KeycloakProperties keycloakProps = 
                properties.getTokenProvider().getKeycloak();
            
            // 커스텀 scope가 설정되어야 함
            assertThat(keycloakProps.getScopes()).isEqualTo("openid profile email roles");
        });
    }
}