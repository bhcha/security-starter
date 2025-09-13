package com.ldx.hexacore.security.config.condition;

import com.ldx.hexacore.security.config.properties.SecurityStarterProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 단순화된 조건 어노테이션 테스트
 */
class SimplifiedConditionsTest {
    
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TestConfiguration.class));
    
    @Test
    void testAuthenticationFeatureToggle() {
        contextRunner
                .withPropertyValues(
                        "security-starter.enabled=true",
                        "security-starter.authentication-toggle.enabled=true"
                )
                .run(context -> {
                    assertThat(context).hasBean("authenticationTestBean");
                });
        
        contextRunner
                .withPropertyValues(
                        "security-starter.enabled=true",
                        "security-starter.authentication-toggle.enabled=false"
                )
                .run(context -> {
                    assertThat(context).doesNotHaveBean("authenticationTestBean");
                });
    }
    
    @Test
    void testJwtFeatureToggle() {
        contextRunner
                .withPropertyValues(
                        "security-starter.enabled=true",
                        "security-starter.jwt-toggle.enabled=true"
                )
                .run(context -> {
                    assertThat(context).hasBean("jwtTestBean");
                });
        
        contextRunner
                .withPropertyValues(
                        "security-starter.enabled=true",
                        "security-starter.jwt-toggle.enabled=false"
                )
                .run(context -> {
                    assertThat(context).doesNotHaveBean("jwtTestBean");
                });
    }
    
    @Test
    void testModeConditions() {
        // Traditional Mode (기본값)
        contextRunner
                .withPropertyValues("security-starter.enabled=true")
                .run(context -> {
                    assertThat(context).hasBean("traditionalTestBean");
                    assertThat(context).doesNotHaveBean("hexagonalTestBean");
                });
        
        // Hexagonal Mode
        contextRunner
                .withPropertyValues(
                        "security-starter.enabled=true",
                        "security-starter.mode=HEXAGONAL"
                )
                .run(context -> {
                    assertThat(context).doesNotHaveBean("traditionalTestBean");
                    assertThat(context).hasBean("hexagonalTestBean");
                });
    }
    
    
    @Test
    void testProviderConditions() {
        // Keycloak Provider
        contextRunner
                .withPropertyValues(
                        "security-starter.enabled=true",
                        "security-starter.tokenProvider.provider=keycloak"
                )
                .run(context -> {
                    assertThat(context).hasBean("keycloakTestBean");
                    assertThat(context).doesNotHaveBean("springJwtTestBean");
                });
        
        // Spring JWT Provider
        contextRunner
                .withPropertyValues(
                        "security-starter.enabled=true",
                        "security-starter.tokenProvider.provider=spring_jwt"
                )
                .run(context -> {
                    assertThat(context).doesNotHaveBean("keycloakTestBean");
                    assertThat(context).hasBean("springJwtTestBean");
                });
    }
    
    @Test
    void testDisabledStarter() {
        // 전체 스타터가 비활성화되면 모든 Feature가 비활성화
        contextRunner
                .withPropertyValues("security-starter.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean("authenticationTestBean");
                    assertThat(context).doesNotHaveBean("jwtTestBean");
                    assertThat(context).doesNotHaveBean("traditionalTestBean");
                    assertThat(context).doesNotHaveBean("hexagonalTestBean");
                });
    }
    
    /**
     * 테스트용 Configuration
     */
    @Configuration
    static class TestConfiguration {
        
        @Bean
        @SimplifiedConditions.ConditionalOnAuthentication
        public String authenticationTestBean() {
            return "authentication";
        }
        
        @Bean
        @SimplifiedConditions.ConditionalOnJwt
        public String jwtTestBean() {
            return "jwt";
        }
        
        @Bean
        @SimplifiedConditions.ConditionalOnTraditionalMode
        public String traditionalTestBean() {
            return "traditional";
        }
        
        @Bean
        @SimplifiedConditions.ConditionalOnHexagonalMode
        public String hexagonalTestBean() {
            return "hexagonal";
        }
        
        
        @Bean
        @SimplifiedConditions.ConditionalOnKeycloakProvider
        public String keycloakTestBean() {
            return "keycloak";
        }
        
        @Bean
        @SimplifiedConditions.ConditionalOnSpringJwtProvider
        public String springJwtTestBean() {
            return "springJwt";
        }
    }
}