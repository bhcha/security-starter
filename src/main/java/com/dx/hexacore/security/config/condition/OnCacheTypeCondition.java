package com.dx.hexacore.security.config.condition;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;

/**
 * 캐시 타입 기반 조건 검사 클래스입니다.
 * 
 * 다음 조건들을 검사합니다:
 * 1. 필요한 캐시 구현 클래스가 클래스패스에 존재하는지
 * 2. hexacore.security.cache.type 속성값이 일치하는지
 * 3. 캐시가 활성화되어 있는지
 */
public class OnCacheTypeCondition extends SpringBootCondition {
    
    private static final String CACHE_TYPE_PROPERTY = "hexacore.security.cache.type";
    private static final String CACHE_ENABLED_PROPERTY = "hexacore.security.cache.enabled";
    private static final String DEFAULT_CACHE_TYPE = "caffeine";
    private static final Map<String, String> CACHE_TYPE_CLASSES = new HashMap<>();
    
    static {
        CACHE_TYPE_CLASSES.put("caffeine", "com.github.benmanes.caffeine.cache.Caffeine");
        CACHE_TYPE_CLASSES.put("redis", "org.springframework.data.redis.core.RedisTemplate");
    }
    
    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        ConditionMessage.Builder message = ConditionMessage.forCondition("OnCacheType");
        
        // Get the cache type from annotation
        MultiValueMap<String, Object> attributes = metadata.getAllAnnotationAttributes(
            ConditionalOnCacheType.class.getName());
        
        if (attributes == null || attributes.isEmpty()) {
            return ConditionOutcome.noMatch(message.because("no @ConditionalOnCacheType annotation found"));
        }
        
        String expectedCacheType = (String) attributes.getFirst("value");
        if (expectedCacheType == null) {
            return ConditionOutcome.noMatch(message.because("@ConditionalOnCacheType value is null"));
        }
        
        // Check if the required class is present
        String requiredClass = CACHE_TYPE_CLASSES.get(expectedCacheType.toLowerCase());
        if (requiredClass == null) {
            return ConditionOutcome.noMatch(
                message.because("Unknown cache type: " + expectedCacheType)
            );
        }
        
        ClassLoader classLoader = context.getClassLoader();
        if (!ClassUtils.isPresent(requiredClass, classLoader)) {
            return ConditionOutcome.noMatch(
                message.didNotFind("required class").items(requiredClass)
            );
        }
        
        // Check configured cache type
        String configuredCacheType = context.getEnvironment().getProperty(CACHE_TYPE_PROPERTY, DEFAULT_CACHE_TYPE);
        if (!expectedCacheType.equalsIgnoreCase(configuredCacheType)) {
            return ConditionOutcome.noMatch(
                message.because("hexacore.security.cache.type is '" + configuredCacheType + 
                              "' but condition requires '" + expectedCacheType + "'")
            );
        }
        
        // Check if cache is enabled
        boolean cacheEnabled = context.getEnvironment().getProperty(CACHE_ENABLED_PROPERTY, Boolean.class, true);
        if (!cacheEnabled) {
            return ConditionOutcome.noMatch(
                message.because("hexacore.security.cache.enabled is false")
            );
        }
        
        return ConditionOutcome.match(
            message.found("cache type").items(expectedCacheType)
        );
    }
}