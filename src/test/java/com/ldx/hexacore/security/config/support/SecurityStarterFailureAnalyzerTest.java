package com.ldx.hexacore.security.config.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.diagnostics.FailureAnalysis;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SecurityStarterFailureAnalyzer 테스트")
class SecurityStarterFailureAnalyzerTest {

    private SecurityStarterFailureAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new SecurityStarterFailureAnalyzer();
    }

    @Test
    @DisplayName("sessionManagementUseCase Bean 누락 시 분석 결과를 반환한다")
    void analyze_WithMissingSessionManagementUseCase_ShouldReturnAnalysis() {
        // Given
        NoSuchBeanDefinitionException exception = new NoSuchBeanDefinitionException("sessionManagementUseCase");

        // When
        FailureAnalysis analysis = analyzer.analyze(exception, exception);

        // Then
        assertThat(analysis).isNotNull();
        assertThat(analysis.getDescription()).contains("세션 관리 기능을 찾을 수 없습니다");
        assertThat(analysis.getAction()).contains("세션 관리 활성화");
        assertThat(analysis.getAction()).contains("hexacore:");
    }

    @Test
    @DisplayName("authenticationUseCase Bean 누락 시 분석 결과를 반환한다")
    void analyze_WithMissingAuthenticationUseCase_ShouldReturnAnalysis() {
        // Given
        NoSuchBeanDefinitionException exception = new NoSuchBeanDefinitionException("authenticationUseCase");

        // When
        FailureAnalysis analysis = analyzer.analyze(exception, exception);

        // Then
        assertThat(analysis).isNotNull();
        assertThat(analysis.getDescription()).contains("인증 기능을 찾을 수 없습니다");
        assertThat(analysis.getAction()).contains("Security-Starter 활성화");
    }

    @Test
    @DisplayName("keycloakTokenProvider Bean 누락 시 분석 결과를 반환한다")
    void analyze_WithMissingKeycloakTokenProvider_ShouldReturnAnalysis() {
        // Given
        NoSuchBeanDefinitionException exception = new NoSuchBeanDefinitionException("keycloakTokenProvider");

        // When
        FailureAnalysis analysis = analyzer.analyze(exception, exception);

        // Then
        assertThat(analysis).isNotNull();
        assertThat(analysis.getDescription()).contains("Keycloak 인증 제공자를 찾을 수 없습니다");
        assertThat(analysis.getAction()).contains("Keycloak 설정을 확인하고");
    }

    @Test
    @DisplayName("Security-Starter와 관련 없는 Bean 누락 시 null을 반환한다")
    void analyze_WithNonSecurityRelatedBean_ShouldReturnNull() {
        // Given
        NoSuchBeanDefinitionException exception = new NoSuchBeanDefinitionException("someUnrelatedBean");

        // When
        FailureAnalysis analysis = analyzer.analyze(exception, exception);

        // Then
        assertThat(analysis).isNull();
    }

    @Test
    @DisplayName("Bean 이름이 null인 경우에도 메시지로 판단한다")
    void analyze_WithNullBeanNameButSecurityMessage_ShouldReturnAnalysis() {
        // Given
        NoSuchBeanDefinitionException exception = new NoSuchBeanDefinitionException(
            "No qualifying bean of type 'com.ldx.hexacore.security.auth.application.command.port.in.AuthenticationUseCase' available"
        );

        // When
        FailureAnalysis analysis = analyzer.analyze(exception, exception);

        // Then
        assertThat(analysis).isNotNull();
        assertThat(analysis.getDescription()).contains("Security-Starter");
    }
}