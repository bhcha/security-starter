package com.dx.hexacore.security.auth.application.query.handler;

import com.dx.hexacore.security.auth.application.query.handler.AuthenticationQueryHandler;
import com.dx.hexacore.security.auth.application.query.port.in.GetAuthenticationUseCase;
import com.dx.hexacore.security.auth.application.query.port.in.GetTokenInfoUseCase;
import com.dx.hexacore.security.auth.application.query.port.out.LoadAuthenticationQueryPort;
import com.dx.hexacore.security.auth.application.query.port.out.LoadTokenInfoQueryPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Authentication Query Use Case 설정 클래스
 * 같은 패키지에 있는 package-private 구현체들을 Bean으로 등록합니다.
 */
@Configuration
public class AuthenticationQueryConfiguration {

    // Application Services - Query Side
    @Bean
    @ConditionalOnMissingBean
    public GetAuthenticationUseCase getAuthenticationUseCase(LoadAuthenticationQueryPort loadAuthenticationQueryPort) {
        return new AuthenticationQueryHandler(loadAuthenticationQueryPort, null);
    }

    @Bean
    @ConditionalOnMissingBean
    public GetTokenInfoUseCase getTokenInfoUseCase(LoadTokenInfoQueryPort loadTokenInfoQueryPort) {
        return new AuthenticationQueryHandler(null, loadTokenInfoQueryPort);
    }
}