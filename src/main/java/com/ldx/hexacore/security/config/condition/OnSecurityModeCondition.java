package com.ldx.hexacore.security.config.condition;

import com.ldx.hexacore.security.config.properties.SecurityStarterProperties;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

/**
 * Security Mode 조건 평가 클래스
 * 
 * <p>현재 설정된 Mode와 요구되는 Mode를 비교하여 Bean 활성화 여부를 결정합니다.
 * 
 * @since 1.0.0
 */
public class OnSecurityModeCondition extends SpringBootCondition {
    
    private static final String PROPERTY_PREFIX = "hexacore.security";
    private static final String MODE_PROPERTY = "security-starter.mode";
    
    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment environment = context.getEnvironment();
        ConditionMessage.Builder message = ConditionMessage.forCondition("@ConditionalOnSecurityMode");
        
        // 어노테이션에서 요구되는 Mode 추출
        Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionalOnSecurityMode.class.getName());
        if (attributes == null) {
            return ConditionOutcome.noMatch(message.because("no ConditionalOnSecurityMode annotation found"));
        }
        
        ConditionalOnSecurityMode.Mode requiredMode = (ConditionalOnSecurityMode.Mode) attributes.get("value");
        
        // 현재 설정된 Mode 확인
        String currentModeStr = environment.getProperty(MODE_PROPERTY, "TRADITIONAL").toUpperCase();
        SecurityStarterProperties.Mode currentMode;
        
        try {
            currentMode = SecurityStarterProperties.Mode.valueOf(currentModeStr);
        } catch (IllegalArgumentException e) {
            return ConditionOutcome.noMatch(
                message.because("invalid mode value: " + currentModeStr)
            );
        }
        
        // Mode 비교
        boolean matches = requiredMode.name().equals(currentMode.name());
        
        if (matches) {
            return ConditionOutcome.match(
                message.because("current mode " + currentMode + " matches required mode " + requiredMode)
            );
        } else {
            return ConditionOutcome.noMatch(
                message.because("current mode " + currentMode + " does not match required mode " + requiredMode)
            );
        }
    }
}