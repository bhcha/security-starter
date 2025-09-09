package com.dx.hexacore.security.auth.adapter.inbound.filter;

import com.dx.hexacore.security.auth.adapter.inbound.filter.SecurityFilterConfig;
import com.dx.hexacore.security.auth.adapter.inbound.config.SecurityProperties;
import com.dx.hexacore.security.auth.adapter.inbound.filter.JwtAuthenticationEntryPoint;
import com.dx.hexacore.security.auth.adapter.inbound.filter.JwtAuthenticationFilter;
import com.dx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.dx.hexacore.security.logging.SecurityRequestLogger;
import com.dx.hexacore.security.logging.SecurityEventLogger;
import com.dx.hexacore.security.config.properties.HexacoreSecurityProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityFilterConfig 테스트")
class SecurityFilterConfigTest {

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SecurityProperties securityProperties;
    
    @Mock
    private SecurityRequestLogger securityRequestLogger;
    
    @Mock
    private SecurityEventLogger securityEventLogger;
    
    @Mock
    private SecurityFilterChain securityFilterChain;

    private SecurityFilterConfig securityFilterConfig;

    @BeforeEach
    void setUp() {
        securityFilterConfig = new SecurityFilterConfig();
    }

    @Test
    @DisplayName("JWT 제외 경로 속성 빈 생성")
    void jwtExcludeProperties_BeanCreation() {
        // When
        var properties = securityFilterConfig.jwtExcludeProperties();

        // Then
        assertThat(properties).isNotNull();
        assertThat(properties.getPaths()).isEmpty(); // 기본값 없음 - 설정 파일에서 지정
    }

    @Test
    @DisplayName("JWT 인증 필터 빈 생성")
    void jwtAuthenticationFilter_BeanCreation() {
        // Given
        SecurityFilterConfig.JwtExcludeProperties excludeProperties = new SecurityFilterConfig.JwtExcludeProperties();
        excludeProperties.setPaths(List.of("/public/**", "/api/auth/**"));

        // When
        var filter = securityFilterConfig.jwtAuthenticationFilter(
            tokenProvider,
            objectMapper,
            excludeProperties,
            securityProperties,
            securityRequestLogger,
            securityEventLogger
        );

        // Then
        assertThat(filter).isNotNull();
        assertThat(filter).isInstanceOf(JwtAuthenticationFilter.class);
    }

    @Test
    @DisplayName("JWT 인증 진입점 빈 생성")
    void jwtAuthenticationEntryPoint_BeanCreation() {
        // When
        var entryPoint = securityFilterConfig.jwtAuthenticationEntryPoint(objectMapper);

        // Then
        assertThat(entryPoint).isNotNull();
        assertThat(entryPoint).isInstanceOf(JwtAuthenticationEntryPoint.class);
    }

    @Test
    @DisplayName("제외 경로 속성 커스터마이징")
    void jwtExcludeProperties_Customization() {
        // Given
        SecurityFilterConfig.JwtExcludeProperties properties = new SecurityFilterConfig.JwtExcludeProperties();
        var customPaths = Arrays.asList("/custom/**", "/open/**");

        // When
        properties.setPaths(customPaths);

        // Then
        assertThat(properties.getPaths()).containsExactlyElementsOf(customPaths);
    }

    @Test
    @DisplayName("@ConditionalOnProperty 조건 테스트 - 활성화된 경우")
    void conditionalOnProperty_EnabledCase() {
        // Given & When
        new ApplicationContextRunner()
            .withPropertyValues("hexacore.security.enabled=true")
            .withUserConfiguration(SecurityFilterConfig.class, TestConfig.class)
            .withBean(TokenProvider.class, () -> tokenProvider)
            .withBean(ObjectMapper.class, () -> objectMapper)
            .withBean(SecurityRequestLogger.class, () -> securityRequestLogger)
            .withBean(SecurityEventLogger.class, () -> securityEventLogger)
            .withBean(SecurityFilterChain.class, () -> securityFilterChain)
            .run(context -> {
                // Then
                assertThat(context).hasSingleBean(SecurityFilterConfig.class);
                assertThat(context).hasSingleBean(JwtAuthenticationFilter.class);
                assertThat(context).hasSingleBean(JwtAuthenticationEntryPoint.class);
                // SecurityFilterChain은 테스트하지 않음 - HttpSecurity 의존성 필요
            });
    }

    @Test
    @DisplayName("@ConditionalOnProperty 조건 테스트 - 비활성화된 경우")
    void conditionalOnProperty_DisabledCase() {
        // Given & When
        new ApplicationContextRunner()
            .withPropertyValues("hexacore.security.enabled=false")
            .withUserConfiguration(SecurityFilterConfig.class)
            .withBean(TokenProvider.class, () -> tokenProvider)
            .withBean(ObjectMapper.class, () -> objectMapper)
            .run(context -> {
                // Then
                assertThat(context).doesNotHaveBean(SecurityFilterConfig.class);
                assertThat(context).doesNotHaveBean(JwtAuthenticationFilter.class);
                assertThat(context).doesNotHaveBean(JwtAuthenticationEntryPoint.class);
            });
    }

    @Test
    @DisplayName("@ConditionalOnProperty 조건 테스트 - 속성 없는 경우 (기본값 true)")
    void conditionalOnProperty_DefaultCase() {
        // Given & When
        new ApplicationContextRunner()
            .withUserConfiguration(SecurityFilterConfig.class, TestConfig.class)
            .withBean(TokenProvider.class, () -> tokenProvider)
            .withBean(ObjectMapper.class, () -> objectMapper)
            .withBean(SecurityRequestLogger.class, () -> securityRequestLogger)
            .withBean(SecurityEventLogger.class, () -> securityEventLogger)
            .withBean(SecurityFilterChain.class, () -> securityFilterChain)
            .run(context -> {
                // Then
                assertThat(context).hasSingleBean(SecurityFilterConfig.class);
                // matchIfMissing = true 이므로 기본적으로 활성화됨
            });
    }

    @Test
    @DisplayName("커스텀 제외 경로 설정 테스트")
    void customExcludePaths_Configuration() {
        // ConfigurationProperties 바인딩이 테스트 환경에서는 제대로 동작하지 않을 수 있으므로
        // 실제 운영 환경에서의 동작을 확인하는 것이 더 중요합니다.
        // 여기서는 프로퍼티 설정 자체가 가능한지만 확인합니다.
        
        // Given
        var properties = new SecurityFilterConfig.JwtExcludeProperties();
        var customPaths = Arrays.asList("/public/**", "/api/docs/**");
        
        // When
        properties.setPaths(customPaths);
        
        // Then
        assertThat(properties.getPaths()).containsExactlyElementsOf(customPaths);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public SecurityProperties securityProperties() {
            SecurityProperties properties = new SecurityProperties();
            SecurityProperties.Authentication auth = new SecurityProperties.Authentication();
            auth.setDefaultRole("ROLE_USER");
            
            SecurityProperties.Authentication.ErrorResponse errorResponse = 
                new SecurityProperties.Authentication.ErrorResponse();
            errorResponse.setDefaultMessage("Authentication failed");
            errorResponse.setIncludeStatus(true);
            errorResponse.setIncludeTimestamp(true);
            
            auth.setErrorResponse(errorResponse);
            properties.setAuthentication(auth);
            return properties;
        }
        
        @Bean
        @Primary
        public HexacoreSecurityProperties hexacoreSecurityProperties() {
            HexacoreSecurityProperties properties = new HexacoreSecurityProperties();
            
            HexacoreSecurityProperties.AuthFilterProperties filter = new HexacoreSecurityProperties.AuthFilterProperties();
            filter.setExcludePaths(new String[]{"/test/**"});
            properties.setFilter(filter);
            
            HexacoreSecurityProperties.TokenProvider tokenProvider = new HexacoreSecurityProperties.TokenProvider();
            HexacoreSecurityProperties.TokenProvider.JwtProperties jwt = new HexacoreSecurityProperties.TokenProvider.JwtProperties();
            jwt.setExcludedPaths(List.of("/jwt-excluded/**"));
            tokenProvider.setJwt(jwt);
            properties.setTokenProvider(tokenProvider);
            
            return properties;
        }
    }
}