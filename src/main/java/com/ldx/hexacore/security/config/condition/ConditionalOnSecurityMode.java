package com.ldx.hexacore.security.config.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * Security Mode 기반 조건부 활성화 어노테이션
 * 
 * <p>설정된 Mode(Traditional/Hexagonal)에 따라 Bean을 조건부로 활성화합니다.
 * 
 * @since 1.0.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnSecurityModeCondition.class)
public @interface ConditionalOnSecurityMode {
    
    /**
     * 활성화할 Mode
     * 
     * @return Traditional 또는 Hexagonal
     */
    Mode value();
    
    /**
     * Security Mode 열거형
     */
    enum Mode {
        TRADITIONAL,
        HEXAGONAL
    }
}