package com.ldx.hexacore.security.config.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;

/**
 * Adapter Layer 자동 설정 클래스입니다.
 * 
 * Inbound/Outbound Adapter의 조건부 활성화를 관리합니다.
 */
@AutoConfiguration
@ConditionalOnProperty(
    prefix = "security-starter",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
@Import({
    PersistenceAutoConfiguration.class,
    CacheAutoConfiguration.class,
    SecurityFilterAutoConfiguration.class
})
public class AdapterAutoConfiguration {

    /**
     * Adapter Layer의 개별 설정은 Import된 Configuration 클래스에서 처리합니다.
     * - PersistenceAutoConfiguration: JPA, MongoDB 영속성 어댑터
     * - CacheAutoConfiguration: Caffeine, Redis 캐시 어댑터  
     * - SecurityFilterAutoConfiguration: Security Filter 설정
     */
}