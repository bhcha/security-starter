package com.ldx.hexacore.security.session.adapter.outbound.memory;

import com.ldx.hexacore.security.session.application.command.port.out.AuthenticationSessionRepository;
import com.ldx.hexacore.security.session.application.query.port.out.LoadFailedAttemptsQueryPort;
import com.ldx.hexacore.security.session.application.query.port.out.LoadSessionStatusQueryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 메모리 기반 세션 어댑터 설정 클래스.
 * JPA가 없는 환경에서 기본 구현체를 제공합니다.
 * 
 * @since 1.0.0
 */
@Configuration
public class InMemorySessionConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(InMemorySessionConfiguration.class);

    /**
     * 하나의 공유 세션 어댑터 인스턴스를 생성합니다.
     */
    @Bean("inMemorySessionAdapter")
    @ConditionalOnMissingBean(name = "inMemorySessionAdapter")
    public InMemorySessionAdapter inMemorySessionAdapter() {
        logger.info("Creating shared in-memory session adapter (JPA not available)");
        return new InMemorySessionAdapter();
    }

    @Bean
    @ConditionalOnMissingBean(AuthenticationSessionRepository.class)
    public AuthenticationSessionRepository authenticationSessionRepository(InMemorySessionAdapter adapter) {
        return adapter;
    }

    @Bean  
    @ConditionalOnMissingBean(LoadSessionStatusQueryPort.class)
    public LoadSessionStatusQueryPort loadSessionStatusQueryPort(InMemorySessionAdapter adapter) {
        return adapter;
    }

    @Bean  
    @ConditionalOnMissingBean(LoadFailedAttemptsQueryPort.class)
    public LoadFailedAttemptsQueryPort loadFailedAttemptsQueryPort(InMemorySessionAdapter adapter) {
        return adapter;
    }
}