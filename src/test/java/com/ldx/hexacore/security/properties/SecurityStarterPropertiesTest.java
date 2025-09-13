package com.ldx.hexacore.security.properties;

import com.ldx.hexacore.security.config.properties.SecurityStarterProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SecurityStarterProperties 단위 테스트
 * 
 * Properties 바인딩과 기본값 동작을 검증합니다.
 */
class SecurityStarterPropertiesTest {
    
    @Test
    void defaultValues_shouldBeSet() {
        // 기본값 테스트
        SecurityStarterProperties properties = new SecurityStarterProperties();
        
        assertThat(properties.getEnabled()).isTrue();
        assertThat(properties.getMode()).isEqualTo(SecurityStarterProperties.Mode.TRADITIONAL);
    }
    
    @Test
    void bind_shouldWorkCorrectly() {
        // Properties 바인딩 테스트
        Map<String, String> map = new HashMap<>();
        map.put("security-starter.enabled", "true");
        map.put("security-starter.mode", "HEXAGONAL");
        map.put("security-starter.jwt-toggle.enabled", "true");
        map.put("security-starter.session-toggle.enabled", "false");
        
        ConfigurationPropertySource source = new MapConfigurationPropertySource(map);
        Binder binder = new Binder(source);
        
        SecurityStarterProperties properties = binder.bind("security-starter", SecurityStarterProperties.class).get();
        
        assertThat(properties.getEnabled()).isTrue();
        assertThat(properties.getMode()).isEqualTo(SecurityStarterProperties.Mode.HEXAGONAL);
        assertThat(properties.getJwtToggle()).isNotNull();
        assertThat(properties.getJwtToggle().getEnabled()).isTrue();
        assertThat(properties.getSessionToggle()).isNotNull();
        assertThat(properties.getSessionToggle().getEnabled()).isFalse();
    }
    
    @Test
    void isJwtEnabled_shouldCheckMultipleConditions() {
        SecurityStarterProperties properties = new SecurityStarterProperties();
        
        // 기본값으로 JWT 활성화 확인
        assertThat(properties.isJwtEnabled()).isTrue();
        
        // JWT Toggle 비활성화
        properties.getJwtToggle().setEnabled(false);
        assertThat(properties.isJwtEnabled()).isFalse();
        
        // 다시 활성화
        properties.getJwtToggle().setEnabled(true);
        assertThat(properties.isJwtEnabled()).isTrue();
    }
    
    @Test
    void isSessionEnabled_shouldCheckToggle() {
        SecurityStarterProperties properties = new SecurityStarterProperties();
        
        // 기본값으로 Session 활성화 확인
        assertThat(properties.isSessionEnabled()).isTrue();
        
        // Session Toggle 비활성화
        properties.getSessionToggle().setEnabled(false);
        assertThat(properties.isSessionEnabled()).isFalse();
    }
    
    @Test
    void mode_shouldHaveCorrectValues() {
        // Mode enum 테스트
        assertThat(SecurityStarterProperties.Mode.values())
            .containsExactly(
                SecurityStarterProperties.Mode.TRADITIONAL,
                SecurityStarterProperties.Mode.HEXAGONAL
            );
        
        assertThat(SecurityStarterProperties.Mode.valueOf("TRADITIONAL"))
            .isEqualTo(SecurityStarterProperties.Mode.TRADITIONAL);
        
        assertThat(SecurityStarterProperties.Mode.valueOf("HEXAGONAL"))
            .isEqualTo(SecurityStarterProperties.Mode.HEXAGONAL);
    }
}