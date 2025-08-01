package com.dx.hexacore.security.config.autoconfigure;

import com.dx.hexacore.security.config.support.SecurityAuthHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot Actuator 통합을 위한 자동 설정 클래스입니다.
 * 
 * Hexacore Security 상태를 Health Check 엔드포인트에서 확인할 수 있도록 합니다.
 */
@AutoConfiguration
@ConditionalOnClass(HealthIndicator.class)
@ConditionalOnProperty(
    prefix = "hexacore.security",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class ActuatorAutoConfiguration {

    /**
     * Hexacore Security 상태를 확인하는 Health Indicator를 등록합니다.
     * 
     * /actuator/health 엔드포인트에서 hexacoreSecurity 항목으로 확인 가능합니다.
     */
    @Bean
    @ConditionalOnEnabledHealthIndicator("hexacoreSecurity")
    public SecurityAuthHealthIndicator securityAuthHealthIndicator() {
        return new SecurityAuthHealthIndicator();
    }
}