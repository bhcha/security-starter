package com.dx.hexacore.security.config.autoconfigure;

import com.dx.hexacore.security.config.properties.HexacoreSecurityProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import jakarta.persistence.EntityManager;

/**
 * 영속성 어댑터 자동 설정
 * 
 * JPA 또는 MongoDB 어댑터를 조건부로 활성화합니다.
 */
@Configuration
@EnableConfigurationProperties(HexacoreSecurityProperties.class)
public class PersistenceAutoConfiguration {
    
    /**
     * JPA 영속성 어댑터 설정
     */
    @Configuration
    @ConditionalOnClass(EntityManager.class)
    @ConditionalOnProperty(
        prefix = "hexacore.security.persistence",
        name = "jpa.enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    @EntityScan(basePackages = {
        "com.dx.hexacore.security.auth.adapter.outbound.persistence.entity",
        "com.dx.hexacore.security.session.adapter.outbound.persistence.entity"
    })
    @EnableJpaRepositories(
        basePackages = {
            "com.dx.hexacore.security.auth.adapter.outbound.persistence.repository",
            "com.dx.hexacore.security.session.adapter.outbound.persistence.repository"
        }
    )
    @ComponentScan(
        basePackages = {
            "com.dx.hexacore.security.auth.adapter.outbound.persistence",
            "com.dx.hexacore.security.session.adapter.outbound.persistence"
        },
        includeFilters = @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = ".*JpaAdapter.*"
        )
    )
    public static class JpaPersistenceConfiguration {
        // JPA 관련 추가 설정이 필요한 경우 여기에 추가
    }
    
    /**
     * MongoDB 영속성 어댑터 설정 (향후 지원 예정)
     */
    @Configuration
    @ConditionalOnProperty(
        prefix = "hexacore.security.persistence",
        name = "mongo-enabled",
        havingValue = "true"
    )
    public static class MongoPersistenceConfiguration {
        // MongoDB 지원 시 구현 예정
    }
}