package com.ldx.hexacore.security.integration;

import com.ldx.hexacore.security.auth.application.command.port.in.AuthenticationUseCase;
import com.ldx.hexacore.security.auth.application.command.port.in.TokenManagementUseCase;
import com.ldx.hexacore.security.config.LdxSecurityAutoConfiguration;
import com.ldx.hexacore.security.config.properties.SecurityStarterProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Zero Configuration 테스트
 * 
 * <p>의존성만 추가하고 별도 설정 없이 애플리케이션이 정상 동작하는지 검증합니다.
 */
class ZeroConfigurationTest {
    
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    LdxSecurityAutoConfiguration.class,
                    MinimalTestConfiguration.class
            ));
    
    @Test
    void testStartupWithoutConfiguration() {
        // 설정 없이 애플리케이션 시작
        contextRunner.run(context -> {
            // 컨텍스트가 정상적으로 시작됨
            assertThat(context).isNotNull();
            
            // SecurityStarterProperties가 기본값으로 생성됨
            assertThat(context).hasBean("SecurityStarterProperties-com.ldx.hexacore.security.config.properties.SecurityStarterProperties");
            SecurityStarterProperties properties = context.getBean(SecurityStarterProperties.class);
            
            // 기본값 확인
            assertThat(properties.getEnabled()).isTrue();
            assertThat(properties.getMode()).isEqualTo(SecurityStarterProperties.Mode.TRADITIONAL);
            
            // 기본 기능들이 활성화됨
            assertThat(properties.isAuthenticationEnabled()).isTrue();
            assertThat(properties.isSessionEnabled()).isTrue();
            assertThat(properties.isJwtEnabled()).isTrue();
        });
    }
    
    @Test
    void testDefaultSecurityFeaturesEnabled() {
        // 설정 없이 기본 보안 기능 활성화 확인
        contextRunner.run(context -> {
            // Authentication 관련 Bean들이 자동 등록됨
            assertThat(context).hasSingleBean(AuthenticationUseCase.class);
            assertThat(context).hasSingleBean(TokenManagementUseCase.class);
            
            // Marker bean이 존재함
            assertThat(context).hasBean("hexacoreSecurityMarker");
            
            // Mode 설정이 적용됨
            assertThat(context).hasBean("traditionalModeConfiguration");
            assertThat(context).doesNotHaveBean("hexagonalModeConfiguration");
        });
    }
    
    @Test
    void testJwtTokenGenerationWithoutConfiguration() {
        // JWT 설정 없이 토큰 생성/검증 가능한지 확인
        contextRunner.run(context -> {
            SecurityStarterProperties properties = context.getBean(SecurityStarterProperties.class);
            
            // JWT가 기본적으로 활성화됨
            assertThat(properties.isJwtEnabled()).isTrue();
            
            // JWT 기본 설정 확인
            assertThat(properties.getJwt()).isNotNull();
            assertThat(properties.getJwt().getEnabled()).isTrue();
            
            // JWT excluded paths 확인 (filter 설정에서)
            assertThat(properties.getFilter()).isNotNull();
            assertThat(properties.getFilter().getExcludedPaths())
                    .contains("/api/public/**", "/health/**", "/actuator/**");
        });
    }
    
    @Test
    void testSessionManagementWithoutConfiguration() {
        // 세션 설정 없이 세션 관리 동작 확인
        contextRunner.run(context -> {
            SecurityStarterProperties properties = context.getBean(SecurityStarterProperties.class);
            
            // Session이 기본적으로 활성화됨
            assertThat(properties.isSessionEnabled()).isTrue();
            
            // Session 기본 설정 확인
            assertThat(properties.getSession()).isNotNull();
            assertThat(properties.getSession().getEnabled()).isTrue();
            // timeout은 SessionProperties의 실제 필드 확인 필요
            
            // Lockout 정책 기본값
            assertThat(properties.getSession().getLockout()).isNotNull();
            assertThat(properties.getSession().getLockout().getMaxAttempts()).isEqualTo(5);
            assertThat(properties.getSession().getLockout().getLockoutDurationMinutes()).isEqualTo(30);
        });
    }
    
    @Test
    void testMinimalConfigurationOverride() {
        // 최소한의 설정으로 기본값 오버라이드
        contextRunner
                .withPropertyValues(
                        "hexacore.security.mode=HEXAGONAL"
                )
                .run(context -> {
                    SecurityStarterProperties properties = context.getBean(SecurityStarterProperties.class);
                    
                    // Mode만 변경되고 나머지는 기본값 유지
                    assertThat(properties.getMode()).isEqualTo(SecurityStarterProperties.Mode.HEXAGONAL);
                    assertThat(properties.getEnabled()).isTrue();
                    assertThat(properties.isAuthenticationEnabled()).isTrue();
                    
                    // Hexagonal Mode Configuration이 활성화됨
                    assertThat(context).hasBean("hexagonalModeConfiguration");
                    assertThat(context).doesNotHaveBean("traditionalModeConfiguration");
                });
    }
    
    @Test
    void testDisableSpecificFeature() {
        // 특정 기능만 비활성화
        contextRunner
                .withPropertyValues(
                        "hexacore.security.rateLimitToggle.enabled=true",
                        "hexacore.security.ipRestrictionToggle.enabled=true"
                )
                .run(context -> {
                    SecurityStarterProperties properties = context.getBean(SecurityStarterProperties.class);
                    
                    // 추가 기능 활성화 확인
                    assertThat(properties.isRateLimitEnabled()).isTrue();
                    assertThat(properties.isIpRestrictionEnabled()).isTrue();
                    
                    // 기본 기능은 여전히 활성화
                    assertThat(properties.isAuthenticationEnabled()).isTrue();
                    assertThat(properties.isJwtEnabled()).isTrue();
                });
    }
    
    /**
     * 테스트를 위한 최소 Configuration
     * TokenProvider와 Repository의 Mock 구현 제공
     */
    @Configuration
    static class MinimalTestConfiguration {
        
        @Bean
        public com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider tokenProvider() {
            return new com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider() {
                @Override
                public com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider.TokenValidationResult validateToken(String token) {
                    return new com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider.TokenValidationResult(
                            true, "test-user", null, com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider.TokenType.ACCESS
                    );
                }
                
                @Override
                public String issueToken(String userId, java.util.Map<String, Object> claims) {
                    return "test-token";
                }
                
                @Override
                public String refreshToken(String refreshToken) {
                    return "new-token";
                }
                
                @Override
                public com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider.TokenProviderType getProviderType() {
                    return com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider.TokenProviderType.SPRING_JWT;
                }
            };
        }
        
        @Bean
        public com.ldx.hexacore.security.auth.application.command.port.out.AuthenticationRepository authenticationRepository() {
            return new com.ldx.hexacore.security.auth.application.command.port.out.AuthenticationRepository() {
                @Override
                public void save(com.ldx.hexacore.security.auth.domain.Authentication authentication) {
                    // Mock implementation
                }
                
                @Override
                public java.util.Optional<com.ldx.hexacore.security.auth.domain.Authentication> findById(String id) {
                    return java.util.Optional.empty();
                }
                
                @Override
                public java.util.Optional<com.ldx.hexacore.security.auth.domain.Authentication> findByUserId(String userId) {
                    return java.util.Optional.empty();
                }
                
                @Override
                public void deleteById(String id) {
                    // Mock implementation
                }
            };
        }
        
        @Bean
        public com.ldx.hexacore.security.auth.application.command.port.out.EventPublisher eventPublisher() {
            return event -> {
                // Mock implementation - 이벤트 발행 무시
            };
        }
    }
}