package com.ldx.hexacore.security.config.autoconfigure;

import com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.ldx.hexacore.security.config.properties.HexacoreSecurityProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * TokenProvider Bean 중복 생성 문제를 검증하는 테스트
 */
class TokenProviderBeanDuplicationTest {
    
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    TokenProviderAutoConfiguration.class
            ));

    @Test
    void shouldCreateOnlyOneTokenProviderWithDefaultSettings() {
        // 기본 설정 (아무 설정 없음) - JWT가 기본값이므로 JWT Provider만 생성되어야 함
        contextRunner
                .run(context -> {
                    assertThat(context).hasSingleBean(TokenProvider.class);
                    assertThat(context).hasBean("springJwtTokenProvider");
                    assertThat(context).doesNotHaveBean("keycloakTokenProvider");
                });
    }
    
    @Test
    void shouldCreateOnlyJwtProviderWhenExplicitlySet() {
        // 명시적으로 JWT 설정
        contextRunner
                .withPropertyValues(
                        "hexacore.security.token-provider.provider=jwt",
                        "hexacore.security.token-provider.jwt.enabled=true"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(TokenProvider.class);
                    assertThat(context).hasBean("springJwtTokenProvider");
                    assertThat(context).doesNotHaveBean("keycloakTokenProvider");
                });
    }
    
    @Test
    void shouldCreateOnlyKeycloakProviderWhenExplicitlySet() {
        // 명시적으로 Keycloak 설정
        contextRunner
                .withPropertyValues(
                        "hexacore.security.token-provider.provider=keycloak",
                        "hexacore.security.token-provider.keycloak.enabled=true",
                        "hexacore.security.token-provider.keycloak.server-url=http://localhost:8080",
                        "hexacore.security.token-provider.keycloak.realm=test",
                        "hexacore.security.token-provider.keycloak.client-id=test-client",
                        "hexacore.security.token-provider.keycloak.client-secret=test-secret"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(TokenProvider.class);
                    assertThat(context).hasBean("keycloakTokenProvider");
                    assertThat(context).doesNotHaveBean("springJwtTokenProvider");
                });
    }
    
    @Test
    void shouldNotCreateMultipleTokenProviders() {
        // 현재 문제가 되는 상황을 재현해보기
        contextRunner
                .withPropertyValues(
                        "hexacore.security.token-provider.provider=jwt"
                        // keycloak.enabled는 기본값이 true
                        // jwt.enabled도 기본값이 true
                )
                .run(context -> {
                    // Bean 개수 확인
                    String[] tokenProviderBeans = context.getBeanNamesForType(TokenProvider.class);
                    System.out.println("Found TokenProvider beans: " + java.util.Arrays.toString(tokenProviderBeans));
                    
                    if (tokenProviderBeans.length > 1) {
                        System.out.println("❌ Multiple TokenProvider beans detected!");
                        for (String beanName : tokenProviderBeans) {
                            System.out.println("  - " + beanName + ": " + context.getBean(beanName).getClass().getSimpleName());
                        }
                    }
                    
                    // 실제로 중복이 발생하는지 확인
                    assertThat(context).hasSingleBean(TokenProvider.class);
                });
    }
    
    @Test
    void shouldNotCreateAnyProviderWhenBothDisabled() {
        contextRunner
                .withPropertyValues(
                        "hexacore.security.token-provider.jwt.enabled=false",
                        "hexacore.security.token-provider.keycloak.enabled=false"
                )
                .run(context -> {
                    assertThat(context).doesNotHaveBean(TokenProvider.class);
                });
    }
    
    @Test 
    void debugCurrentBehavior() {
        // 현재 동작을 디버그해보기
        contextRunner
                .run(context -> {
                    HexacoreSecurityProperties properties = context.getBean(HexacoreSecurityProperties.class);
                    System.out.println("=== Current Configuration ===");
                    System.out.println("Provider: " + properties.getTokenProvider().getProvider());
                    System.out.println("JWT enabled: " + properties.getTokenProvider().getJwt().getEnabled());
                    System.out.println("Keycloak enabled: " + properties.getTokenProvider().getKeycloak().getEnabled());
                    
                    String[] tokenProviderBeans = context.getBeanNamesForType(TokenProvider.class);
                    System.out.println("TokenProvider beans count: " + tokenProviderBeans.length);
                    for (String beanName : tokenProviderBeans) {
                        System.out.println("  - " + beanName + ": " + context.getBean(beanName).getClass().getSimpleName());
                    }
                    
                    if (tokenProviderBeans.length > 1) {
                        System.out.println("❌ BEAN DUPLICATION PROBLEM CONFIRMED!");
                    } else {
                        System.out.println("✅ NO BEAN DUPLICATION - IMPROVEMENT SUCCESSFUL!");
                    }
                });
    }
    
    @Test
    void shouldHandleStandardProperties() {
        // 표준 프로퍼티를 통한 Keycloak 설정 테스트
        contextRunner
                .withPropertyValues(
                        "hexacore.security.token-provider.provider=keycloak",  // 표준 경로 사용
                        "hexacore.security.token-provider.keycloak.enabled=true",  // 명시적으로 활성화
                        "hexacore.security.token-provider.keycloak.server-url=http://localhost:8080",
                        "hexacore.security.token-provider.keycloak.realm=test",
                        "hexacore.security.token-provider.keycloak.client-id=test-client",
                        "hexacore.security.token-provider.keycloak.client-secret=test-secret"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(TokenProvider.class);
                    assertThat(context).hasBean("keycloakTokenProvider");
                    assertThat(context).doesNotHaveBean("springJwtTokenProvider");
                    
                    System.out.println("✅ Standard properties working correctly!");
                });
    }
}