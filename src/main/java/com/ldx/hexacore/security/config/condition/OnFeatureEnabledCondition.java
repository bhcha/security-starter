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
 * Feature Toggle 조건 평가 클래스
 * 
 * <p>SecurityStarterProperties의 FeatureToggle 설정을 평가하여 Bean 활성화 여부를 결정합니다.
 * 
 * @since 1.0.0
 */
public class OnFeatureEnabledCondition extends SpringBootCondition {
    
    private static final String PROPERTY_PREFIX = "security-starter";
    
    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment environment = context.getEnvironment();
        ConditionMessage.Builder message = ConditionMessage.forCondition("@ConditionalOnFeatureEnabled");
        
        // 어노테이션에서 Feature 추출
        Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionalOnFeatureEnabled.class.getName());
        if (attributes == null) {
            return ConditionOutcome.noMatch(message.because("no ConditionalOnFeatureEnabled annotation found"));
        }
        
        ConditionalOnFeatureEnabled.Feature feature = (ConditionalOnFeatureEnabled.Feature) attributes.get("value");
        
        // 전체 스타터가 비활성화된 경우 체크
        if (!isStarterEnabled(environment)) {
            return ConditionOutcome.noMatch(
                message.because("Hexacore Security is disabled (security-starter.enabled=false)")
            );
        }
        
        // Feature별 활성화 상태 체크
        boolean isEnabled = isFeatureEnabled(environment, feature);
        
        if (isEnabled) {
            return ConditionOutcome.match(
                message.because("Feature " + feature.name() + " is enabled")
            );
        } else {
            return ConditionOutcome.noMatch(
                message.because("Feature " + feature.name() + " is disabled")
            );
        }
    }
    
    /**
     * 스타터 전체 활성화 여부 확인
     */
    private boolean isStarterEnabled(Environment environment) {
        String enabled = environment.getProperty(PROPERTY_PREFIX + ".enabled", "true");
        return Boolean.parseBoolean(enabled);
    }
    
    /**
     * 개별 Feature 활성화 여부 확인
     */
    private boolean isFeatureEnabled(Environment environment, ConditionalOnFeatureEnabled.Feature feature) {
        // Properties 바인딩 시도
        try {
            Binder binder = Binder.get(environment);
            SecurityStarterProperties properties = binder.bind(
                PROPERTY_PREFIX, 
                Bindable.of(SecurityStarterProperties.class)
            ).orElseGet(SecurityStarterProperties::new);
            
            // Feature별 활성화 상태 확인
            switch (feature) {
                case AUTHENTICATION:
                    return properties.isAuthenticationEnabled();
                case SESSION:
                    return properties.isSessionEnabled();
                case JWT:
                    return properties.isJwtEnabled();
                case RATE_LIMIT:
                    return properties.isRateLimitEnabled();
                case IP_RESTRICTION:
                    return properties.isIpRestrictionEnabled();
                case HEADERS:
                    return properties.isHeadersEnabled();
                default:
                    return false;
            }
        } catch (Exception e) {
            // 바인딩 실패 시 직접 프로퍼티 확인
            String propertyPath = getPropertyPath(feature);
            String enabled = environment.getProperty(propertyPath, "false");
            
            // 일부 기능은 기본값이 true
            if (feature == ConditionalOnFeatureEnabled.Feature.AUTHENTICATION ||
                feature == ConditionalOnFeatureEnabled.Feature.SESSION ||
                feature == ConditionalOnFeatureEnabled.Feature.JWT ||
                feature == ConditionalOnFeatureEnabled.Feature.HEADERS) {
                enabled = environment.getProperty(propertyPath, "true");
            }
            
            return Boolean.parseBoolean(enabled);
        }
    }
    
    /**
     * Feature에 대한 프로퍼티 경로 생성
     */
    private String getPropertyPath(ConditionalOnFeatureEnabled.Feature feature) {
        // propertyName을 kebab-case로 변환
        String kebabCase = feature.getPropertyName()
            .replaceAll("([a-z])([A-Z])", "$1-$2")
            .toLowerCase();
        return PROPERTY_PREFIX + "." + kebabCase + "-toggle.enabled";
    }
}