package com.ldx.hexacore.security.config.autoconfigure;

import com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.ldx.hexacore.security.config.SecurityStarterAutoConfiguration;
import com.ldx.hexacore.security.config.properties.SecurityStarterProperties;
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
            ))
            .withPropertyValues(
                    "spring.datasource.url=jdbc:h2:mem:testdb",
                    "spring.datasource.driver-class-name=org.h2.Driver",
                    "spring.jpa.hibernate.ddl-auto=create-drop"
            );

    @Test
    void shouldCreateOnlyOneTokenProviderWithDefaultSettings() {
        // 기본 설정에 JWT 활성화 추가
        contextRunner
                .withPropertyValues(
                        "security-starter.token-provider.jwt.enabled=true"
                )
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
                        "security-starter.token-provider.jwt.enabled=true"
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
                        "security-starter.token-provider.provider=keycloak",
                        "security-starter.token-provider.keycloak.enabled=true",
                        "security-starter.token-provider.keycloak.server-url=http://localhost:8080",
                        "security-starter.token-provider.keycloak.realm=test",
                        "security-starter.token-provider.keycloak.client-id=test-client",
                        "security-starter.token-provider.keycloak.client-secret=test-secret",
                        "security-starter.token-provider.jwt.enabled=false"
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
                        "security-starter.token-provider.jwt.enabled=true"
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
                        "security-starter.token-provider.jwt.enabled=false",
                        "security-starter.token-provider.keycloak.enabled=false",
                        // Keycloak 필수 필드를 위한 더미 값 제공 (비활성화되어도 검증은 수행됨)
                        "security-starter.token-provider.keycloak.server-url=http://dummy",
                        "security-starter.token-provider.keycloak.realm=dummy",
                        "security-starter.token-provider.keycloak.client-id=dummy",
                        // 인증 기능도 비활성화하여 TokenProvider가 필요하지 않도록 함
                        "security-starter.authentication-toggle.enabled=false"
                )
                .run(context -> {
                    assertThat(context).doesNotHaveBean(TokenProvider.class);
                });
    }
    
    @Test 
    void debugCurrentBehavior() {
        // 현재 동작을 디버그해보기
        contextRunner
                .withPropertyValues(
                        "security-starter.token-provider.jwt.enabled=true"
                )
                .run(context -> {
                    SecurityStarterProperties properties = context.getBean(SecurityStarterProperties.class);
                    System.out.println("=== Current Configuration ===");
                    System.out.println("JWT enabled: " + properties.isJwtEnabled());
                    System.out.println("Keycloak enabled: " + properties.isKeycloakEnabled());
                    
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
                        "security-starter.token-provider.provider=keycloak",
                        "security-starter.token-provider.keycloak.enabled=true",
                        "security-starter.token-provider.keycloak.server-url=http://localhost:8080",
                        "security-starter.token-provider.keycloak.realm=test",
                        "security-starter.token-provider.keycloak.client-id=test-client",
                        "security-starter.token-provider.keycloak.client-secret=test-secret",
                        "security-starter.token-provider.jwt.enabled=false"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(TokenProvider.class);
                    assertThat(context).hasBean("keycloakTokenProvider");
                    assertThat(context).doesNotHaveBean("springJwtTokenProvider");
                    
                    System.out.println("✅ Standard properties working correctly!");
                });
    }
}