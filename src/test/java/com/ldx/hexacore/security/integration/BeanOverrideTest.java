package com.ldx.hexacore.security.integration;

import com.ldx.hexacore.security.auth.application.command.port.in.AuthenticationUseCase;
import com.ldx.hexacore.security.auth.application.command.port.in.AuthenticateCommand;
import com.ldx.hexacore.security.auth.application.command.port.in.AuthenticationResult;
import com.ldx.hexacore.security.auth.application.command.port.out.AuthenticationRepository;
import com.ldx.hexacore.security.auth.application.command.port.out.EventPublisher;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.ldx.hexacore.security.auth.domain.Authentication;
import com.ldx.hexacore.security.config.LdxSecurityAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Bean Override 테스트
 * 
 * <p>부모 프로젝트에서 Bean을 Override할 때 충돌 없이 동작하는지 검증합니다.
 */
class BeanOverrideTest {
    
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LdxSecurityAutoConfiguration.class));
    
    @Test
    void testParentBeanOverride() {
        contextRunner
                .withUserConfiguration(ParentOverrideConfiguration.class)
                .run(context -> {
                    // 부모 프로젝트의 Bean이 우선적으로 사용됨
                    AuthenticationUseCase useCase = context.getBean(AuthenticationUseCase.class);
                    assertThat(useCase).isNotNull();
                    
                    // 커스텀 구현체인지 확인
                    AuthenticateCommand command = new AuthenticateCommand("custom-user", "password");
                    AuthenticationResult result = useCase.authenticate(command);
                    
                    // 커스텀 구현체의 응답 확인
                    assertThat(result).isNotNull();
                    assertThat(result.isSuccess()).isTrue();
                    assertThat(result.getToken()).isEqualTo("custom-token");
                });
    }
    
    @Test
    void testTokenProviderOverride() {
        contextRunner
                .withUserConfiguration(CustomTokenProviderConfiguration.class)
                .run(context -> {
                    // 커스텀 TokenProvider가 사용됨
                    TokenProvider tokenProvider = context.getBean(TokenProvider.class);
                    assertThat(tokenProvider).isNotNull();
                    
                    // 커스텀 구현 확인
                    String token = tokenProvider.issueToken("user", Map.of());
                    assertThat(token).isEqualTo("custom-provider-token");
                    
                    TokenProvider.TokenValidationResult validation = tokenProvider.validateToken(token);
                    assertThat(validation.isValid()).isTrue();
                    assertThat(validation.getUserId()).isEqualTo("custom-user");
                });
    }
    
    @Test
    void testPriorityWithPrimaryAnnotation() {
        contextRunner
                .withUserConfiguration(PrimaryBeanConfiguration.class)
                .run(context -> {
                    // @Primary가 적용된 Bean이 우선 사용됨
                    AuthenticationRepository repository = context.getBean(AuthenticationRepository.class);
                    assertThat(repository).isNotNull();
                    
                    // Primary Bean의 커스텀 동작 확인
                    Optional<Authentication> auth = repository.findByUserId("primary-test");
                    assertThat(auth).isPresent();
                    assertThat(auth.get().getCredentials().getUsername()).isEqualTo("primary-user");
                });
    }
    
    @Test
    void testMultipleBeanOverrides() {
        contextRunner
                .withUserConfiguration(MultipleOverrideConfiguration.class)
                .run(context -> {
                    // 여러 Bean을 동시에 Override
                    assertThat(context).hasBean("authenticationUseCase");
                    assertThat(context).hasBean("tokenProvider");
                    assertThat(context).hasBean("eventPublisher");
                    
                    // 모두 커스텀 구현체인지 확인
                    EventPublisher publisher = context.getBean(EventPublisher.class);
                    assertThat(publisher).isInstanceOf(CustomEventPublisher.class);
                });
    }
    
    @Test
    void testConflictFreeOperation() {
        // 기본 설정과 커스텀 설정이 충돌 없이 동작
        contextRunner
                .withUserConfiguration(ParentOverrideConfiguration.class)
                .withPropertyValues(
                        "hexacore.security.mode=HEXAGONAL",
                        "hexacore.security.jwtToggle.enabled=true"
                )
                .run(context -> {
                    // 설정과 커스텀 Bean이 모두 정상 동작
                    assertThat(context).hasBean("hexagonalModeConfiguration");
                    assertThat(context).hasBean("customAuthenticationUseCase");
                    
                    // 충돌 없이 동작 확인
                    assertThat(context.getStartupFailure()).isNull();
                });
    }
    
    /**
     * 부모 프로젝트의 Override Configuration
     */
    @Configuration
    static class ParentOverrideConfiguration {
        
        @Bean(name = "customAuthenticationUseCase")
        @Primary
        public AuthenticationUseCase authenticationUseCase() {
            return new AuthenticationUseCase() {
                @Override
                public AuthenticationResult authenticate(AuthenticateCommand command) {
                    return AuthenticationResult.success("custom-token", "refresh-token");
                }
            };
        }
    }
    
    /**
     * 커스텀 TokenProvider Configuration
     */
    @Configuration
    static class CustomTokenProviderConfiguration {
        
        @Bean
        @Primary
        public TokenProvider tokenProvider() {
            return new TokenProvider() {
                @Override
                public TokenValidationResult validateToken(String token) {
                    return new TokenValidationResult(true, "custom-user", null, TokenType.ACCESS);
                }
                
                @Override
                public String issueToken(String userId, Map<String, Object> claims) {
                    return "custom-provider-token";
                }
                
                @Override
                public String refreshToken(String refreshToken) {
                    return "custom-refresh-token";
                }
                
                @Override
                public TokenProviderType getProviderType() {
                    return TokenProviderType.SPRING_JWT;
                }
            };
        }
        
        @Bean
        public AuthenticationRepository authenticationRepository() {
            return new AuthenticationRepository() {
                @Override
                public void save(Authentication authentication) {}
                
                @Override
                public Optional<Authentication> findById(String id) {
                    return Optional.empty();
                }
                
                @Override
                public Optional<Authentication> findByUserId(String userId) {
                    return Optional.empty();
                }
                
                @Override
                public void deleteById(String id) {}
            };
        }
        
        @Bean
        public EventPublisher eventPublisher() {
            return event -> {};
        }
    }
    
    /**
     * @Primary를 사용한 Bean Configuration
     */
    @Configuration
    static class PrimaryBeanConfiguration {
        
        @Bean
        @Primary
        public AuthenticationRepository primaryRepository() {
            return new AuthenticationRepository() {
                @Override
                public void save(Authentication authentication) {}
                
                @Override
                public Optional<Authentication> findById(String id) {
                    return Optional.empty();
                }
                
                @Override
                public Optional<Authentication> findByUserId(String userId) {
                    if ("primary-test".equals(userId)) {
                        // Authentication 객체 생성 (실제 팩토리 메서드 사용)
                        com.ldx.hexacore.security.auth.domain.vo.Credentials credentials = 
                            com.ldx.hexacore.security.auth.domain.vo.Credentials.of("primary-user", "password");
                        Authentication auth = Authentication.attemptAuthentication(credentials);
                        return Optional.of(auth);
                    }
                    return Optional.empty();
                }
                
                @Override
                public void deleteById(String id) {}
            };
        }
        
        @Bean
        public TokenProvider tokenProvider() {
            return new TokenProvider() {
                @Override
                public TokenValidationResult validateToken(String token) {
                    return new TokenValidationResult(true, "user", null, TokenType.ACCESS);
                }
                
                @Override
                public String issueToken(String userId, Map<String, Object> claims) {
                    return "token";
                }
                
                @Override
                public String refreshToken(String refreshToken) {
                    return "refresh";
                }
                
                @Override
                public TokenProviderType getProviderType() {
                    return TokenProviderType.SPRING_JWT;
                }
            };
        }
        
        @Bean
        public EventPublisher eventPublisher() {
            return event -> {};
        }
    }
    
    /**
     * 여러 Bean을 Override하는 Configuration
     */
    @Configuration
    static class MultipleOverrideConfiguration {
        
        @Bean
        @Primary
        public AuthenticationUseCase authenticationUseCase() {
            return command -> AuthenticationResult.success("multi-token", "multi-refresh");
        }
        
        @Bean
        @Primary
        public TokenProvider tokenProvider() {
            return new TokenProvider() {
                @Override
                public TokenValidationResult validateToken(String token) {
                    return new TokenValidationResult(true, "multi-user", null, TokenType.ACCESS);
                }
                
                @Override
                public String issueToken(String userId, Map<String, Object> claims) {
                    return "multi-token";
                }
                
                @Override
                public String refreshToken(String refreshToken) {
                    return "multi-refresh";
                }
                
                @Override
                public TokenProviderType getProviderType() {
                    return TokenProviderType.SPRING_JWT;
                }
            };
        }
        
        @Bean
        @Primary
        public EventPublisher eventPublisher() {
            return new CustomEventPublisher();
        }
        
        @Bean
        public AuthenticationRepository authenticationRepository() {
            return new AuthenticationRepository() {
                @Override
                public void save(Authentication authentication) {}
                
                @Override
                public Optional<Authentication> findById(String id) {
                    return Optional.empty();
                }
                
                @Override
                public Optional<Authentication> findByUserId(String userId) {
                    return Optional.empty();
                }
                
                @Override
                public void deleteById(String id) {}
            };
        }
    }
    
    /**
     * 커스텀 EventPublisher 구현
     */
    static class CustomEventPublisher implements EventPublisher {
        @Override
        public void publish(Object event) {
            // 커스텀 이벤트 발행 로직
        }
    }
}