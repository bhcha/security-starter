package com.ldx.hexacore.security.autoconfigure;

import com.ldx.hexacore.security.config.properties.SecurityStarterProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SecurityStarter Properties 단순 테스트
 * 
 * AutoConfiguration 전체를 로드하지 않고 Properties만 테스트합니다.
 */
class SimplePropertiesTest {
    
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(PropertiesOnlyConfig.class);
    
    @Test
    void zeroConfiguration_shouldHaveDefaults() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(SecurityStarterProperties.class);
            
            SecurityStarterProperties properties = context.getBean(SecurityStarterProperties.class);
            assertThat(properties.getEnabled()).isTrue();
            assertThat(properties.getMode()).isEqualTo(SecurityStarterProperties.Mode.TRADITIONAL);
        });
    }
    
    @Test
    void properties_shouldBind() {
        contextRunner
            .withPropertyValues(
                "security-starter.enabled=false",
                "security-starter.mode=HEXAGONAL"
            )
            .run(context -> {
                SecurityStarterProperties properties = context.getBean(SecurityStarterProperties.class);
                assertThat(properties.getEnabled()).isFalse();
                assertThat(properties.getMode()).isEqualTo(SecurityStarterProperties.Mode.HEXAGONAL);
            });
    }
    
    @Test
    void jwtToggle_shouldWork() {
        contextRunner
            .withPropertyValues(
                "security-starter.jwt-toggle.enabled=true"
            )
            .run(context -> {
                SecurityStarterProperties properties = context.getBean(SecurityStarterProperties.class);
                assertThat(properties.getJwtToggle()).isNotNull();
                assertThat(properties.getJwtToggle().getEnabled()).isTrue();
                assertThat(properties.isJwtEnabled()).isTrue();
            });
    }
    
    @Test
    void sessionToggle_shouldWork() {
        contextRunner
            .withPropertyValues(
                "security-starter.session-toggle.enabled=false"
            )
            .run(context -> {
                SecurityStarterProperties properties = context.getBean(SecurityStarterProperties.class);
                assertThat(properties.getSessionToggle()).isNotNull();
                assertThat(properties.getSessionToggle().getEnabled()).isFalse();
                assertThat(properties.isSessionEnabled()).isFalse();
            });
    }
    
    @Configuration
    @EnableConfigurationProperties(SecurityStarterProperties.class)
    static class PropertiesOnlyConfig {
        // Properties만 활성화
    }
}