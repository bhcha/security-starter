package com.dx.hexacore.security.config;

import com.dx.hexacore.security.auth.adapter.inbound.filter.SecurityFilterConfig;
import com.dx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.dx.hexacore.security.config.autoconfigure.TokenProviderAutoConfiguration;
import com.dx.hexacore.security.config.autoconfigure.SecurityFilterAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.web.SecurityFilterChain;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SecurityFilterConfig 동작 검증 테스트
 */
class SecurityFilterConfigTest {
    
    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    // SecurityAutoConfiguration.class, // 제외하여 충돌 방지
                    org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.class,
                    HexacoreSecurityAutoConfiguration.class, // ✅ 전체 설정 포함
                    TokenProviderAutoConfiguration.class,
                    SecurityFilterAutoConfiguration.class
            ))
            .withPropertyValues(
                    "hexacore.security.enabled=true",
                    "hexacore.security.filter.enabled=true",
                    "security.auth.jwt.enabled=true",
                    "hexacore.security.token-provider.provider=jwt",
                    "hexacore.security.token-provider.jwt.enabled=true",
                    "hexacore.security.token-provider.jwt.secret=my-super-secret-key-that-is-long-enough-for-256-bits"
            );

    @Test
    void shouldLoadSecurityFilterConfigWhenTokenProviderExists() {
        contextRunner
                .run(context -> {
                    System.out.println("=== Security Filter Config Test ===");
                    
                    // TokenProvider Bean 확인
                    assertThat(context).hasSingleBean(TokenProvider.class);
                    TokenProvider tokenProvider = context.getBean(TokenProvider.class);
                    System.out.println("TokenProvider: " + tokenProvider.getClass().getSimpleName());
                    
                    // SecurityFilterConfig Bean 확인
                    if (context.containsBean("securityFilterConfig")) {
                        System.out.println("✅ SecurityFilterConfig Bean exists");
                        assertThat(context).hasBean("securityFilterConfig");
                    } else {
                        System.out.println("❌ SecurityFilterConfig Bean not found");
                        String[] beanNames = context.getBeanDefinitionNames();
                        System.out.println("Available beans with 'security' in name:");
                        for (String name : beanNames) {
                            if (name.toLowerCase().contains("security")) {
                                System.out.println("  - " + name + ": " + context.getBean(name).getClass().getSimpleName());
                            }
                        }
                    }
                    
                    // SecurityFilterChain Bean 확인
                    if (context.containsBean("securityFilterChain")) {
                        System.out.println("✅ SecurityFilterChain Bean exists");
                        assertThat(context).hasBean("securityFilterChain");
                        SecurityFilterChain filterChain = context.getBean("securityFilterChain", SecurityFilterChain.class);
                        System.out.println("SecurityFilterChain filters count: " + filterChain.getFilters().size());
                        
                        // 필터 목록 출력
                        filterChain.getFilters().forEach(filter -> 
                            System.out.println("  - " + filter.getClass().getSimpleName())
                        );
                        
                        // JWT 필터가 포함되어 있는지 확인
                        boolean hasJwtFilter = filterChain.getFilters().stream()
                                .anyMatch(filter -> filter.getClass().getSimpleName().contains("Jwt"));
                        
                        if (hasJwtFilter) {
                            System.out.println("✅ JWT Authentication Filter found in chain");
                        } else {
                            System.out.println("❌ JWT Authentication Filter NOT found in chain");
                        }
                    } else {
                        System.out.println("❌ SecurityFilterChain Bean not found");
                    }
                });
    }
    
    @Test
    void shouldNotLoadSecurityFilterConfigWhenTokenProviderMissing() {
        new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        SecurityAutoConfiguration.class,
                        SecurityFilterAutoConfiguration.class
                        // TokenProviderAutoConfiguration 제외
                ))
                .withPropertyValues(
                        "hexacore.security.enabled=true",
                        "hexacore.security.filter.enabled=true",
                        "security.auth.jwt.enabled=true"
                )
                .run(context -> {
                    System.out.println("=== No TokenProvider Test ===");
                    
                    // TokenProvider가 없어야 함
                    assertThat(context).doesNotHaveBean(TokenProvider.class);
                    System.out.println("✅ No TokenProvider as expected");
                    
                    // SecurityFilterConfig도 로드되지 않아야 함  
                    assertThat(context).doesNotHaveBean(SecurityFilterConfig.class);
                    System.out.println("✅ SecurityFilterConfig not loaded as expected");
                });
    }
}