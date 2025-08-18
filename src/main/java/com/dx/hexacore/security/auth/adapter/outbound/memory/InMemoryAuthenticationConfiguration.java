package com.dx.hexacore.security.auth.adapter.outbound.memory;

import com.dx.hexacore.security.auth.application.command.port.out.AuthenticationRepository;
import com.dx.hexacore.security.auth.application.query.port.out.LoadAuthenticationQueryPort;
import com.dx.hexacore.security.auth.application.query.port.out.LoadTokenInfoQueryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 메모리 기반 인증 어댑터 설정 클래스.
 * JPA가 없는 환경에서 기본 구현체를 제공합니다.
 * 
 * @since 1.0.0
 */
@Configuration
public class InMemoryAuthenticationConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryAuthenticationConfiguration.class);

    /**
     * 하나의 공유 query 어댑터 인스턴스를 생성합니다.
     */
    @Bean("inMemoryAuthenticationQueryAdapter")
    @ConditionalOnMissingBean(name = "inMemoryAuthenticationQueryAdapter")
    public InMemoryAuthenticationQueryAdapter inMemoryAuthenticationQueryAdapter() {
        logger.info("Creating shared in-memory authentication query adapter (JPA not available)");
        return new InMemoryAuthenticationQueryAdapter();
    }

    /**
     * 하나의 공유 command 어댑터 인스턴스를 생성합니다.
     */
    @Bean("inMemoryAuthenticationCommandAdapter")
    @ConditionalOnMissingBean(name = "inMemoryAuthenticationCommandAdapter")
    public InMemoryAuthenticationCommandAdapter inMemoryAuthenticationCommandAdapter() {
        logger.info("Creating shared in-memory authentication command adapter (JPA not available)");
        return new InMemoryAuthenticationCommandAdapter();
    }

    @Bean
    @ConditionalOnMissingBean(AuthenticationRepository.class)
    public AuthenticationRepository authenticationRepository(InMemoryAuthenticationCommandAdapter adapter) {
        return adapter;
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(LoadAuthenticationQueryPort.class)
    public LoadAuthenticationQueryPort loadAuthenticationQueryPort(InMemoryAuthenticationQueryAdapter adapter) {
        return adapter;
    }

    @Bean  
    @ConditionalOnMissingBean(LoadTokenInfoQueryPort.class)
    public LoadTokenInfoQueryPort loadTokenInfoQueryPort(InMemoryAuthenticationQueryAdapter adapter) {
        return adapter;
    }
}