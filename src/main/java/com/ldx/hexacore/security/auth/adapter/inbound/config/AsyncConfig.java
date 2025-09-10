package com.ldx.hexacore.security.auth.adapter.inbound.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 비동기 처리 설정
 * Event Listener의 비동기 처리를 위한 설정입니다.
 */
@Configuration
@EnableAsync
class AsyncConfig {

    private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);

    @Bean(name = "hexacoreSecurityTaskExecutor")
    @ConditionalOnMissingBean(name = "taskExecutor")
    public Executor hexacoreSecurityTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("AuthEventAsync-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        
        logger.info("Async task executor initialized with core pool size: {}, max pool size: {}", 
            executor.getCorePoolSize(), executor.getMaxPoolSize());
        
        return executor;
    }

}