package com.ldx.hexacore.security.config.condition;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * 단순화된 조건부 어노테이션 모음
 * 
 * <p>자주 사용되는 조건을 간단한 어노테이션으로 제공합니다.
 * 
 * @since 1.0.0
 */
public class SimplifiedConditions {
    
    /**
     * Authentication 기능이 활성화되었을 때
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @ConditionalOnFeatureEnabled(ConditionalOnFeatureEnabled.Feature.AUTHENTICATION)
    public @interface ConditionalOnAuthentication {
    }
    
    /**
     * Session 기능이 활성화되었을 때
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @ConditionalOnFeatureEnabled(ConditionalOnFeatureEnabled.Feature.SESSION)
    public @interface ConditionalOnSession {
    }
    
    /**
     * JWT 기능이 활성화되었을 때
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @ConditionalOnFeatureEnabled(ConditionalOnFeatureEnabled.Feature.JWT)
    public @interface ConditionalOnJwt {
    }
    
    /**
     * Rate Limit 기능이 활성화되었을 때
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @ConditionalOnFeatureEnabled(ConditionalOnFeatureEnabled.Feature.RATE_LIMIT)
    public @interface ConditionalOnRateLimit {
    }
    
    /**
     * IP Restriction 기능이 활성화되었을 때
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @ConditionalOnFeatureEnabled(ConditionalOnFeatureEnabled.Feature.IP_RESTRICTION)
    public @interface ConditionalOnIpRestriction {
    }
    
    /**
     * Traditional 모드일 때
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @ConditionalOnSecurityMode(ConditionalOnSecurityMode.Mode.TRADITIONAL)
    public @interface ConditionalOnTraditionalMode {
    }
    
    /**
     * Hexagonal 모드일 때
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @ConditionalOnSecurityMode(ConditionalOnSecurityMode.Mode.HEXAGONAL)
    public @interface ConditionalOnHexagonalMode {
    }
    
    /**
     * JPA Persistence가 활성화되었을 때
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @ConditionalOnProperty(
        prefix = "security-starter.persistence",
        name = "type",
        havingValue = "jpa"
    )
    public @interface ConditionalOnJpaPersistence {
    }
    
    /**
     * MongoDB Persistence가 활성화되었을 때
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @ConditionalOnProperty(
        prefix = "security-starter.persistence",
        name = "type",
        havingValue = "mongodb"
    )
    public @interface ConditionalOnMongoDbPersistence {
    }
    
    /**
     * Keycloak Provider가 활성화되었을 때
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @ConditionalOnProperty(
        prefix = "security-starter.tokenProvider",
        name = "provider",
        havingValue = "keycloak"
    )
    public @interface ConditionalOnKeycloakProvider {
    }
    
    /**
     * Spring JWT Provider가 활성화되었을 때
     */
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @ConditionalOnProperty(
        prefix = "security-starter.tokenProvider",
        name = "provider",
        havingValue = "spring_jwt"
    )
    public @interface ConditionalOnSpringJwtProvider {
    }
}