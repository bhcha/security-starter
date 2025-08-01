package com.dx.hexacore.security.config.support;

import com.dx.hexacore.security.config.properties.HexacoreSecurityProperties;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Hexacore Security의 상태를 확인하는 Health Indicator입니다.
 * 
 * Spring Boot Actuator를 통해 /actuator/health에서 확인할 수 있습니다.
 */
public class SecurityAuthHealthIndicator implements HealthIndicator {

    private final HexacoreSecurityProperties properties;

    public SecurityAuthHealthIndicator() {
        this.properties = null; // Optional dependency
    }

    @Autowired(required = false)
    public void setProperties(HexacoreSecurityProperties properties) {
        // Properties injection for detailed health info
    }

    @Override
    public Health health() {
        Health.Builder builder = Health.up();

        // 기본 상태 정보
        builder.withDetail("status", "active")
               .withDetail("component", "security-starter");

        // Properties가 주입되었다면 상세 정보 추가
        if (properties != null) {
            builder.withDetail("enabled", properties.getEnabled())
                   .withDetail("tokenProvider", properties.getTokenProvider().getProvider())
                   .withDetail("filterEnabled", properties.getFilter().getEnabled())
                   .withDetail("sessionEnabled", properties.getSession().getEnabled())
                   .withDetail("cacheEnabled", properties.getCache().getEnabled())
                   .withDetail("cacheType", properties.getCache().getType())
                   .withDetail("rateLimitEnabled", properties.getRateLimit().getEnabled())
                   .withDetail("ipRestrictionEnabled", properties.getIpRestriction().getEnabled())
                   .withDetail("headersEnabled", properties.getHeaders().getEnabled());
        }

        return builder.build();
    }
}