package com.dx.hexacore.security.config.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * 캐시 타입 조건부 Bean 등록을 위한 어노테이션입니다.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnCacheTypeCondition.class)
public @interface ConditionalOnCacheType {
    
    /**
     * 매칭할 캐시 타입
     */
    String value();
}