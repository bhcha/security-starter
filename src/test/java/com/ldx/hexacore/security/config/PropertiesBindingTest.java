package com.ldx.hexacore.security.config;

import com.ldx.hexacore.security.config.properties.SecurityStarterProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SecurityStarterProperties 바인딩 테스트
 * 
 * Properties 바인딩 기능만 독립적으로 테스트합니다.
 */
class PropertiesBindingTest {
    
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(PropertiesOnlyConfiguration.class);
    
    @Test
    void zeroConfiguration_shouldStartWithDefaults() {
        // Zero Configuration: 아무 설정 없이도 기본값으로 동작
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(SecurityStarterProperties.class);
            
            SecurityStarterProperties properties = context.getBean(SecurityStarterProperties.class);
            assertThat(properties).isNotNull();
            assertThat(properties.getEnabled()).isTrue(); // 기본값 true
            assertThat(properties.getMode()).isEqualTo(SecurityStarterProperties.Mode.TRADITIONAL); // 기본 모드
        });
    }
    
    @Test
    void whenDisabled_propertiesShouldStillBind() {
        // enabled=false여도 Properties는 바인딩되어야 함
        contextRunner
            .withPropertyValues("security-starter.enabled=false")
            .run(context -> {
                SecurityStarterProperties properties = context.getBean(SecurityStarterProperties.class);
                assertThat(properties.getEnabled()).isFalse();
            });
    }
    
    @Test
    void properties_shouldBindCorrectly() {
        // Properties 바인딩 테스트
        contextRunner
            .withPropertyValues(
                "security-starter.enabled=true",
                "security-starter.mode=HEXAGONAL",
                "security-starter.jwt-toggle.enabled=true",
                "security-starter.session-toggle.enabled=true"
            )
            .run(context -> {
                SecurityStarterProperties properties = context.getBean(SecurityStarterProperties.class);
                
                assertThat(properties.getEnabled()).isTrue();
                assertThat(properties.getMode()).isEqualTo(SecurityStarterProperties.Mode.HEXAGONAL);
                assertThat(properties.isJwtEnabled()).isTrue();
                assertThat(properties.isSessionEnabled()).isTrue();
            });
    }
    
    @Test
    void featureToggle_jwt_shouldWorkCorrectly() {
        // JWT Feature Toggle 테스트
        contextRunner
            .withPropertyValues(
                "security-starter.enabled=true",
                "security-starter.jwt-toggle.enabled=true"
            )
            .run(context -> {
                SecurityStarterProperties properties = context.getBean(SecurityStarterProperties.class);
                assertThat(properties.isJwtEnabled()).isTrue();
            });
        
        // JWT 비활성화
        contextRunner
            .withPropertyValues(
                "security-starter.enabled=true",
                "security-starter.jwt-toggle.enabled=false"
            )
            .run(context -> {
                SecurityStarterProperties properties = context.getBean(SecurityStarterProperties.class);
                assertThat(properties.isJwtEnabled()).isFalse();
            });
    }
    
    @Test
    void featureToggle_keycloak_shouldWorkCorrectly() {
        // Keycloak Feature Toggle 테스트
        contextRunner
            .withPropertyValues(
                "security-starter.enabled=true",
                "security-starter.keycloak.enabled=true",
                "security-starter.keycloak.realm=test-realm",
                "security-starter.keycloak.server-url=http://localhost:8080",
                "security-starter.keycloak.client-id=test-client"
            )
            .run(context -> {
                SecurityStarterProperties properties = context.getBean(SecurityStarterProperties.class);
                assertThat(properties.isKeycloakEnabled()).isTrue();
            });
    }
    
    @Test
    void mode_traditional_shouldBeDefault() {
        // 기본 Mode는 TRADITIONAL이어야 함
        contextRunner.run(context -> {
            SecurityStarterProperties properties = context.getBean(SecurityStarterProperties.class);
            assertThat(properties.getMode()).isEqualTo(SecurityStarterProperties.Mode.TRADITIONAL);
        });
    }
    
    @Test
    void mode_hexagonal_shouldBeConfigurable() {
        // HEXAGONAL Mode 설정 가능
        contextRunner
            .withPropertyValues("security-starter.mode=HEXAGONAL")
            .run(context -> {
                SecurityStarterProperties properties = context.getBean(SecurityStarterProperties.class);
                assertThat(properties.getMode()).isEqualTo(SecurityStarterProperties.Mode.HEXAGONAL);
            });
    }
    
    /**
     * Properties만 로드하는 설정
     */
    @Configuration
    @EnableConfigurationProperties(SecurityStarterProperties.class)
    static class PropertiesOnlyConfiguration {
        // Properties만 활성화
    }
}