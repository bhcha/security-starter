package com.dx.hexacore.security.session.application.query.handler;

import com.dx.hexacore.security.session.application.query.port.out.LoadFailedAttemptsQueryPort;
import com.dx.hexacore.security.session.application.query.port.out.LoadSessionStatusQueryPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Session Query Handler 설정 클래스
 * 같은 패키지에 있는 package-private 구현체들을 Bean으로 등록합니다.
 */
@Configuration
public class SessionQueryConfiguration {

    // Query Handler
    @Bean
    @ConditionalOnMissingBean
    public SessionQueryHandler sessionQueryHandler(
            LoadSessionStatusQueryPort loadSessionStatusQueryPort,
            LoadFailedAttemptsQueryPort loadFailedAttemptsQueryPort) {
        return new SessionQueryHandler(loadSessionStatusQueryPort, loadFailedAttemptsQueryPort);
    }
}