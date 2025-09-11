package com.ldx.hexacore.security.config.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * Feature Toggle 기반 조건부 활성화 어노테이션
 * 
 * <p>SecurityStarterProperties의 FeatureToggle 설정을 기반으로 Bean을 활성화합니다.
 * 
 * @since 1.0.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnFeatureEnabledCondition.class)
public @interface ConditionalOnFeatureEnabled {
    
    /**
     * 체크할 Feature 이름
     * 
     * @return authentication, session, jwt, rateLimit, ipRestriction 등
     */
    Feature value();
    
    /**
     * Feature 열거형
     */
    enum Feature {
        AUTHENTICATION("authentication"),
        SESSION("session"),
        JWT("jwt"),
        RATE_LIMIT("rateLimit"),
        IP_RESTRICTION("ipRestriction"),
        HEADERS("headers");
        
        private final String propertyName;
        
        Feature(String propertyName) {
            this.propertyName = propertyName;
        }
        
        public String getPropertyName() {
            return propertyName;
        }
    }
}