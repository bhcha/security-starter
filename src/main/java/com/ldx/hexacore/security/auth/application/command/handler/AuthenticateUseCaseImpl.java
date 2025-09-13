package com.ldx.hexacore.security.auth.application.command.handler;

import com.ldx.hexacore.security.auth.application.command.port.in.AuthenticateCommand;
import com.ldx.hexacore.security.auth.application.command.port.in.AuthenticationResult;
import com.ldx.hexacore.security.auth.application.command.port.in.AuthenticationUseCase;
import com.ldx.hexacore.security.auth.application.command.port.out.EventPublisher;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenProviderException;
import com.ldx.hexacore.security.auth.domain.vo.Credentials;
import com.ldx.hexacore.security.auth.domain.vo.Token;
import com.ldx.hexacore.security.auth.domain.event.AuthenticationAttempted;
import com.ldx.hexacore.security.auth.domain.event.AuthenticationSucceeded;
import com.ldx.hexacore.security.auth.domain.event.AuthenticationFailed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;

import java.util.Objects;

/**
 * 인증 사용 사례 구현체.
 * 사용자 인증 처리를 담당하는 애플리케이션 서비스입니다.
 * 
 * @since 1.0.0
 */
class AuthenticateUseCaseImpl implements AuthenticationUseCase {

    private static final Logger log = LoggerFactory.getLogger(AuthenticateUseCaseImpl.class);

    private final TokenProvider tokenProvider;
    private final EventPublisher eventPublisher;
    
    /**
     * 인증 사용 사례를 생성합니다.
     *
     * @param tokenProvider 토큰 제공자
     * @param eventPublisher 이벤트 발행자
     */
    public AuthenticateUseCaseImpl(
        TokenProvider tokenProvider,
        EventPublisher eventPublisher
    ) {
        this.tokenProvider = Objects.requireNonNull(tokenProvider, "tokenProvider cannot be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher cannot be null");
    }
    
    @Override
    public AuthenticationResult authenticate(AuthenticateCommand command) {
        Objects.requireNonNull(command, "command cannot be null");

        String username = command.getUsername();
        Credentials credentials = Credentials.of(username, command.getPassword());
        UUID authenticationId = UUID.randomUUID();

        // 인증 시도 이벤트 발행
        eventPublisher.publish(AuthenticationAttempted.of(
            authenticationId,
            username,
            java.time.LocalDateTime.now()
        ));

        try {
            // 토큰 제공자를 통한 인증
            log.debug("Attempting authentication for user: {}", username);
            Token token = tokenProvider.issueToken(credentials);

            // 인증 성공 이벤트 발행
            eventPublisher.publish(AuthenticationSucceeded.of(
                authenticationId,
                token,
                java.time.LocalDateTime.now()
            ));

            log.info("Authentication successful for user: {}", username);
            return AuthenticationResult.success(username, token);

        } catch (TokenProviderException e) {
            // 인증 실패 처리
            String failureReason = "Authentication failed: " + e.getMessage();

            // 인증 실패 이벤트 발행
            eventPublisher.publish(AuthenticationFailed.of(
                authenticationId,
                failureReason,
                java.time.LocalDateTime.now()
            ));

            log.warn("Authentication failed for user: {} - {}", username, failureReason);
            return AuthenticationResult.failure(username, failureReason);
        }
    }
}