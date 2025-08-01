package com.dx.hexacore.security.session.adapter.inbound.event;

import com.dx.hexacore.security.session.application.command.port.in.RecordAttemptUseCase;
import com.dx.hexacore.security.session.application.command.port.in.CheckLockoutUseCase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Session Event Listener 자동 설정
 * 
 * hexacore.session.event.enabled 속성을 통해 활성화/비활성화 제어
 * 기본값: true
 */
@Configuration
@EnableTransactionManagement
@ConditionalOnProperty(
    prefix = "hexacore.session.event",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true // 기본값 true
)
class SessionEventConfiguration {
    
    /**
     * SessionEventListener Bean 등록
     * 
     * @param recordAttemptUseCase 인증 시도 기록 Use Case
     * @param checkLockoutUseCase 잠금 상태 확인 Use Case
     * @return SessionEventListener
     */
    @Bean
    public SessionEventListener sessionEventListener(
            RecordAttemptUseCase recordAttemptUseCase,
            CheckLockoutUseCase checkLockoutUseCase) {
        return new SessionEventListener(recordAttemptUseCase, checkLockoutUseCase);
    }
}