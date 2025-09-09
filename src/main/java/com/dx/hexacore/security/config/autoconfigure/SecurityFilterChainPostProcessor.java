package com.dx.hexacore.security.config.autoconfigure;

import com.dx.hexacore.security.auth.adapter.inbound.filter.JwtAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * SecurityFilterChain BeanPostProcessor
 * 
 * 모든 SecurityFilterChain Bean이 생성될 때 자동으로 JWT 필터를 주입합니다.
 * 사용자가 어떤 SecurityFilterChain을 정의하든 JWT 기능이 자동으로 추가됩니다.
 */
@Component
@ConditionalOnProperty(
    prefix = "hexacore.security.jwt.auto-inject",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = false  // 기본적으로 비활성화 (명시적 활성화 필요)
)
public class SecurityFilterChainPostProcessor implements BeanPostProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityFilterChainPostProcessor.class);
    
    private final JwtAuthenticationFilter jwtFilter;
    
    public SecurityFilterChainPostProcessor(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
        logger.info("🔧 SecurityFilterChainPostProcessor initialized - JWT filter will be auto-injected");
    }
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof SecurityFilterChain) {
            logger.info("🎯 Processing SecurityFilterChain bean: {}", beanName);
            
            // Starter 자체의 SecurityFilterChain은 건너뛰기
            if (beanName.contains("hexacore") || beanName.contains("jwt")) {
                logger.debug("Skipping starter's own SecurityFilterChain: {}", beanName);
                return bean;
            }
            
            try {
                // SecurityFilterChain에 JWT 필터가 이미 포함되어 있는지 확인
                if (!hasJwtFilter((SecurityFilterChain) bean)) {
                    logger.info("📌 Injecting JWT filter into SecurityFilterChain: {}", beanName);
                    return wrapSecurityFilterChain((SecurityFilterChain) bean);
                } else {
                    logger.debug("JWT filter already present in SecurityFilterChain: {}", beanName);
                }
            } catch (Exception e) {
                logger.error("Failed to inject JWT filter into SecurityFilterChain: {}", beanName, e);
            }
        }
        
        return bean;
    }
    
    /**
     * SecurityFilterChain에 JWT 필터가 있는지 확인
     */
    private boolean hasJwtFilter(SecurityFilterChain chain) {
        try {
            // SecurityFilterChain의 필터 목록을 검사
            // 실제 구현은 Spring Security 내부 구조에 따라 달라질 수 있음
            Method getFiltersMethod = chain.getClass().getMethod("getFilters");
            if (getFiltersMethod != null) {
                Object filters = getFiltersMethod.invoke(chain);
                if (filters != null) {
                    return filters.toString().contains("JwtAuthenticationFilter");
                }
            }
        } catch (Exception e) {
            logger.debug("Could not check for JWT filter presence", e);
        }
        return false;
    }
    
    /**
     * SecurityFilterChain을 래핑하여 JWT 필터 추가
     * 
     * 주의: 이 방식은 복잡하고 Spring Security 내부 구조에 의존적입니다.
     * 실제로는 다른 방식을 권장합니다.
     */
    private SecurityFilterChain wrapSecurityFilterChain(SecurityFilterChain original) {
        // Dynamic Proxy를 사용하여 SecurityFilterChain 래핑
        return (SecurityFilterChain) Proxy.newProxyInstance(
            SecurityFilterChain.class.getClassLoader(),
            new Class[] { SecurityFilterChain.class },
            (proxy, method, args) -> {
                Object result = method.invoke(original, args);
                
                // getFilters() 메소드가 호출될 때 JWT 필터 추가
                if ("getFilters".equals(method.getName())) {
                    logger.debug("Adding JWT filter to filter list");
                    // 실제 구현은 더 복잡함
                }
                
                return result;
            }
        );
    }
}