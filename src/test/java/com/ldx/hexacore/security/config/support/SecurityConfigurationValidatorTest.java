package com.ldx.hexacore.security.config.support;

import com.ldx.hexacore.security.config.properties.HexacoreSecurityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityConfigurationValidator 테스트")
class SecurityConfigurationValidatorTest {

    @Mock
    private ApplicationReadyEvent applicationReadyEvent;

    private SecurityConfigurationValidator validator;
    private HexacoreSecurityProperties properties;

    @BeforeEach
    void setUp() {
        properties = new HexacoreSecurityProperties();
        validator = new SecurityConfigurationValidator(properties);
    }

    @Test
    @DisplayName("기본 설정으로 검증이 성공한다")
    void onApplicationEvent_WithDefaultConfiguration_ShouldSucceed() {
        // Given
        properties.setEnabled(true);

        // When & Then
        assertThatCode(() -> validator.onApplicationEvent(applicationReadyEvent))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("JWT에서 액세스 토큰 만료 시간이 리프레시 토큰보다 긴 경우 오류를 발생시킨다")
    void onApplicationEvent_WithInvalidTokenExpiration_ShouldThrowException() {
        // Given - 프로덕션 환경 시뮬레이션
        String originalProfile = System.getProperty("spring.profiles.active");
        String originalForceProduction = System.getProperty("hexacore.security.force-production-validation");
        System.setProperty("spring.profiles.active", "prod");
        System.setProperty("hexacore.security.force-production-validation", "true");
        
        try {
            properties.setEnabled(true);
            properties.getTokenProvider().setProvider("jwt");
            properties.getTokenProvider().getJwt().setEnabled(true);
            properties.getTokenProvider().getJwt().setAccessTokenExpiration(7200); // 2시간
            properties.getTokenProvider().getJwt().setRefreshTokenExpiration(3600); // 1시간

            // When & Then
            assertThatThrownBy(() -> validator.onApplicationEvent(applicationReadyEvent))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Security-Starter 설정 오류가 발견되었습니다");
        } finally {
            // 원래 프로파일 복원
            if (originalProfile != null) {
                System.setProperty("spring.profiles.active", originalProfile);
            } else {
                System.clearProperty("spring.profiles.active");
            }
            if (originalForceProduction != null) {
                System.setProperty("hexacore.security.force-production-validation", originalForceProduction);
            } else {
                System.clearProperty("hexacore.security.force-production-validation");
            }
        }
    }

    @Test
    @DisplayName("Keycloak이 선택되었지만 필수 설정이 없는 경우 오류를 발생시킨다")
    void onApplicationEvent_WithKeycloakMissingConfiguration_ShouldThrowException() {
        // Given - 프로덕션 환경 시뮬레이션
        String originalProfile = System.getProperty("spring.profiles.active");
        String originalForceProduction = System.getProperty("hexacore.security.force-production-validation");
        System.setProperty("spring.profiles.active", "prod");
        System.setProperty("hexacore.security.force-production-validation", "true");
        
        try {
            properties.setEnabled(true);
            properties.getTokenProvider().setProvider("keycloak");
            properties.getTokenProvider().getKeycloak().setEnabled(true);
            // serverUrl, realm, clientId를 설정하지 않음

            // When & Then
            assertThatThrownBy(() -> validator.onApplicationEvent(applicationReadyEvent))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Security-Starter 설정 오류가 발견되었습니다");
        } finally {
            // 원래 프로파일 복원
            if (originalProfile != null) {
                System.setProperty("spring.profiles.active", originalProfile);
            } else {
                System.clearProperty("spring.profiles.active");
            }
            if (originalForceProduction != null) {
                System.setProperty("hexacore.security.force-production-validation", originalForceProduction);
            } else {
                System.clearProperty("hexacore.security.force-production-validation");
            }
        }
    }

    @Test
    @DisplayName("세션 관리 설정이 올바른 경우 검증이 성공한다")
    void onApplicationEvent_WithValidSessionConfiguration_ShouldSucceed() {
        // Given
        properties.setEnabled(true);
        properties.getSession().setEnabled(true);
        properties.getSession().getLockout().setMaxAttempts(5);
        properties.getSession().getLockout().setLockoutDurationMinutes(30);

        // When & Then
        assertThatCode(() -> validator.onApplicationEvent(applicationReadyEvent))
                .doesNotThrowAnyException();
    }
}