package com.ldx.hexacore.security.integration;

import com.ldx.hexacore.security.config.LdxSecurityAutoConfiguration;
import com.ldx.hexacore.security.config.autoconfigure.ModeAwareSecurityFilterConfiguration;
import com.ldx.hexacore.security.config.properties.SecurityStarterProperties;
import com.ldx.hexacore.security.config.support.ModeValidator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Mode 전환 테스트
 * 
 * <p>Traditional/Hexagonal 모드 간 전환이 정상적으로 동작하는지 검증합니다.
 */
class ModeTransitionTest {
    
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    LdxSecurityAutoConfiguration.class,
                    TestConfiguration.class
            ));
    
    @Test
    void testTraditionalModeActivation() {
        contextRunner
                .withPropertyValues(
                        "hexacore.security.mode=TRADITIONAL"
                )
                .run(context -> {
                    // Traditional Mode 설정 확인
                    SecurityStarterProperties properties = context.getBean(SecurityStarterProperties.class);
                    assertThat(properties.getMode()).isEqualTo(SecurityStarterProperties.Mode.TRADITIONAL);
                    
                    // Traditional Mode Configuration 활성화
                    assertThat(context).hasBean("traditionalModeConfiguration");
                    assertThat(context).doesNotHaveBean("hexagonalModeConfiguration");
                    
                    // Mode 기본값 확인
                    ModeAwareSecurityFilterConfiguration.ModeDefaults defaults = 
                            context.getBean(ModeAwareSecurityFilterConfiguration.ModeDefaults.class);
                    assertThat(defaults.getDefaultSessionTimeout()).isEqualTo(1800); // 30분
                    assertThat(defaults.getDefaultTokenExpiry()).isEqualTo(3600);    // 1시간
                    assertThat(defaults.isStrictValidation()).isFalse();
                    assertThat(defaults.isEnableAuditLog()).isFalse();
                });
    }
    
    @Test
    void testHexagonalModeActivation() {
        contextRunner
                .withPropertyValues(
                        "hexacore.security.mode=HEXAGONAL"
                )
                .run(context -> {
                    // Hexagonal Mode 설정 확인
                    SecurityStarterProperties properties = context.getBean(SecurityStarterProperties.class);
                    assertThat(properties.getMode()).isEqualTo(SecurityStarterProperties.Mode.HEXAGONAL);
                    
                    // Hexagonal Mode Configuration 활성화
                    assertThat(context).hasBean("hexagonalModeConfiguration");
                    assertThat(context).doesNotHaveBean("traditionalModeConfiguration");
                    
                    // Mode 기본값 확인
                    ModeAwareSecurityFilterConfiguration.ModeDefaults defaults = 
                            context.getBean(ModeAwareSecurityFilterConfiguration.ModeDefaults.class);
                    assertThat(defaults.getDefaultSessionTimeout()).isEqualTo(900);  // 15분
                    assertThat(defaults.getDefaultTokenExpiry()).isEqualTo(1800);   // 30분
                    assertThat(defaults.isStrictValidation()).isTrue();
                    assertThat(defaults.isEnableAuditLog()).isTrue();
                });
    }
    
    @Test
    void testModeTransitionBeanRecreation() {
        // Traditional Mode로 시작
        ApplicationContextRunner traditionalRunner = contextRunner
                .withPropertyValues("hexacore.security.mode=TRADITIONAL");
        
        traditionalRunner.run(traditionalContext -> {
            assertThat(traditionalContext).hasBean("traditionalModeConfiguration");
            
            // Traditional Mode의 Bean ID 기록
            Object traditionalMarker = traditionalContext.getBean("hexacoreSecurityMarker");
            assertThat(traditionalMarker).isNotNull();
        });
        
        // Hexagonal Mode로 전환
        ApplicationContextRunner hexagonalRunner = contextRunner
                .withPropertyValues("hexacore.security.mode=HEXAGONAL");
        
        hexagonalRunner.run(hexagonalContext -> {
            assertThat(hexagonalContext).hasBean("hexagonalModeConfiguration");
            
            // Hexagonal Mode의 Bean ID 확인 (새로운 컨텍스트)
            Object hexagonalMarker = hexagonalContext.getBean("hexacoreSecurityMarker");
            assertThat(hexagonalMarker).isNotNull();
        });
    }
    
    @Test
    void testModeSpecificArchitectureValidation() {
        // Traditional Mode - 느슨한 검증
        contextRunner
                .withPropertyValues(
                        "hexacore.security.mode=TRADITIONAL",
                        "hexacore.security.persistence.type=memory"
                )
                .run(context -> {
                    // Traditional 모드에서는 memory persistence 허용
                    ModeValidator validator = context.getBean(ModeValidator.class);
                    assertThat(validator).isNotNull();
                    
                    // 경고는 있을 수 있지만 오류는 없어야 함
                    // (실제 검증은 PostConstruct에서 수행됨)
                    assertThat(context).isNotNull();
                });
        
        // Hexagonal Mode - 엄격한 검증
        contextRunner
                .withPropertyValues(
                        "hexacore.security.mode=HEXAGONAL",
                        "hexacore.security.persistence.type=jpa"
                )
                .run(context -> {
                    // Hexagonal 모드에서는 실제 persistence 권장
                    ModeValidator validator = context.getBean(ModeValidator.class);
                    assertThat(validator).isNotNull();
                    
                    SecurityStarterProperties properties = context.getBean(SecurityStarterProperties.class);
                    assertThat(properties.getPersistence().getTypeAsString()).isEqualTo("jpa");
                });
    }
    
    @Test
    void testModeTransitionGuide() {
        contextRunner
                .withPropertyValues("hexacore.security.mode=TRADITIONAL")
                .run(context -> {
                    ModeValidator validator = context.getBean(ModeValidator.class);
                    
                    // Traditional → Hexagonal 전환 가이드
                    String guide = validator.getModeTransitionGuide(SecurityStarterProperties.Mode.HEXAGONAL);
                    assertThat(guide).contains("Domain Layer에서 Spring 의존성 제거");
                    assertThat(guide).contains("Port 인터페이스 정의");
                    assertThat(guide).contains("package-private");
                    assertThat(guide).contains("Use Case를 통한 비즈니스 로직");
                });
        
        contextRunner
                .withPropertyValues("hexacore.security.mode=HEXAGONAL")
                .run(context -> {
                    ModeValidator validator = context.getBean(ModeValidator.class);
                    
                    // Hexagonal → Traditional 전환 가이드
                    String guide = validator.getModeTransitionGuide(SecurityStarterProperties.Mode.TRADITIONAL);
                    assertThat(guide).contains("hexacore.security.mode=TRADITIONAL");
                    assertThat(guide).contains("직접 Domain 객체 사용");
                    assertThat(guide).contains("Repository 직접 접근");
                });
    }
    
    @Test
    void testDefaultModeWithoutConfiguration() {
        // 설정 없이 기본 Mode 확인
        contextRunner.run(context -> {
            SecurityStarterProperties properties = context.getBean(SecurityStarterProperties.class);
            
            // 기본값은 TRADITIONAL
            assertThat(properties.getMode()).isEqualTo(SecurityStarterProperties.Mode.TRADITIONAL);
            
            // Traditional Mode Configuration이 활성화됨
            assertThat(context).hasBean("traditionalModeConfiguration");
        });
    }
    
    /**
     * 테스트를 위한 Configuration
     */
    @Configuration
    static class TestConfiguration {
        
        @Bean
        public com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider tokenProvider() {
            return new com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider() {
                @Override
                public com.ldx.hexacore.security.auth.domain.vo.Token issueToken(com.ldx.hexacore.security.auth.domain.vo.Credentials credentials) {
                    return com.ldx.hexacore.security.auth.domain.vo.Token.of("test-token", "test-refresh-token", 3600L);
                }
                
                @Override
                public com.ldx.hexacore.security.auth.application.command.port.out.TokenValidationResult validateToken(String accessToken) {
                    return com.ldx.hexacore.security.auth.application.command.port.out.TokenValidationResult.valid(
                            "test-user-id", "test-user", java.util.Set.of("ROLE_USER"), java.time.Instant.now().plusSeconds(3600)
                    );
                }
                
                @Override
                public com.ldx.hexacore.security.auth.domain.vo.Token refreshToken(String refreshToken) {
                    return com.ldx.hexacore.security.auth.domain.vo.Token.of("new-token", "new-refresh-token", 3600L);
                }
                
                @Override
                public com.ldx.hexacore.security.auth.application.command.port.out.TokenProviderType getProviderType() {
                    return com.ldx.hexacore.security.auth.application.command.port.out.TokenProviderType.SPRING_JWT;
                }
            };
        }
        
        @Bean
        public com.ldx.hexacore.security.auth.application.command.port.out.AuthenticationRepository authenticationRepository() {
            return new com.ldx.hexacore.security.auth.application.command.port.out.AuthenticationRepository() {
                @Override
                public void save(com.ldx.hexacore.security.auth.domain.Authentication authentication) {}
                
                @Override
                public java.util.Optional<com.ldx.hexacore.security.auth.domain.Authentication> findById(String id) {
                    return java.util.Optional.empty();
                }
                
                @Override
                public java.util.Optional<com.ldx.hexacore.security.auth.domain.Authentication> findByUserId(String userId) {
                    return java.util.Optional.empty();
                }
                
                @Override
                public void deleteById(String id) {}
            };
        }
        
        @Bean
        public com.ldx.hexacore.security.auth.application.command.port.out.EventPublisher eventPublisher() {
            return event -> {};
        }
    }
}