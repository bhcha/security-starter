package com.dx.hexacore.security.config.autoconfigure;

import com.dx.hexacore.security.config.properties.HexacoreSecurityProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import jakarta.persistence.EntityManager;

/**
 * 영속성 어댑터 자동 설정
 * 
 * JPA가 있으면 JPA 어댑터를, 없으면 InMemory 어댑터를 활성화합니다.
 */
@Configuration
@EnableConfigurationProperties(HexacoreSecurityProperties.class)
public class PersistenceAutoConfiguration {
    
    /**
     * JPA 영속성 어댑터 설정
     */
    @Configuration
    @Order(1)  // JPA 설정이 먼저 로드되도록 함
    @ConditionalOnClass(EntityManager.class)
    @ConditionalOnProperty(
        prefix = "hexacore.security.persistence",
        name = "type",
        havingValue = "JPA",
        matchIfMissing = false
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
    @Import({
        com.dx.hexacore.security.auth.adapter.outbound.persistence.AuthenticationPersistenceConfiguration.class,
        com.dx.hexacore.security.session.adapter.outbound.persistence.SessionPersistenceConfiguration.class,
        com.dx.hexacore.security.session.adapter.outbound.event.SessionEventPublisherConfiguration.class,
        com.dx.hexacore.security.auth.adapter.outbound.event.AuthenticationEventConfiguration.class
    })
    public static class JpaPersistenceConfiguration {
        // JPA 관련 추가 설정이 필요한 경우 여기에 추가
    }
    
    /**
     * 메모리 기반 영속성 어댑터 설정 (JPA가 없는 환경에서 기본 제공)
     * 
     * JPA가 클래스패스에 없을 때 자동으로 활성화되며,
     * 개발 및 테스트 환경에서 별도의 데이터베이스 설정 없이
     * security-starter를 사용할 수 있도록 합니다.
     */
    @Configuration
    @Order(3)  // JPA가 없을 때 사용
    @ConditionalOnMissingClass("jakarta.persistence.EntityManager")
    @Import({
        com.dx.hexacore.security.auth.adapter.outbound.memory.InMemoryAuthenticationConfiguration.class,
        com.dx.hexacore.security.session.adapter.outbound.memory.InMemorySessionConfiguration.class,
        com.dx.hexacore.security.session.adapter.outbound.event.SessionEventPublisherConfiguration.class,
        com.dx.hexacore.security.auth.adapter.outbound.event.AuthenticationEventConfiguration.class
    })
    public static class InMemoryPersistenceConfiguration {
        // 메모리 기반 어댑터들과 기본 이벤트 퍼블리셔가 자동으로 Import됨
    }
    
    /**
     * 기본 영속성 어댑터 설정 (Memory)
     * 
     * persistence.type이 MEMORY로 설정되었거나 설정되지 않은 경우
     * InMemory 어댑터를 활성화합니다.
     */
    @Configuration
    @Order(2)  // MEMORY 타입을 기본으로 사용
    @ConditionalOnProperty(
        prefix = "hexacore.security.persistence",
        name = "type",
        havingValue = "MEMORY",
        matchIfMissing = true
    )
    @Import({
        com.dx.hexacore.security.auth.adapter.outbound.memory.InMemoryAuthenticationConfiguration.class,
        com.dx.hexacore.security.session.adapter.outbound.memory.InMemorySessionConfiguration.class,
        com.dx.hexacore.security.session.adapter.outbound.event.SessionEventPublisherConfiguration.class,
        com.dx.hexacore.security.auth.adapter.outbound.event.AuthenticationEventConfiguration.class
    })
    public static class DefaultPersistenceConfiguration {
        // JPA가 있어도 기본적으로 InMemory 사용 (명시적으로 JPA를 선택하지 않는 한)
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