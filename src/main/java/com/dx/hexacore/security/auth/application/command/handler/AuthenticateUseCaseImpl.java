package com.dx.hexacore.security.auth.application.command.handler;

import com.dx.hexacore.security.auth.application.command.port.in.AuthenticateCommand;
import com.dx.hexacore.security.auth.application.command.port.in.AuthenticationResult;
import com.dx.hexacore.security.auth.application.command.port.in.AuthenticationUseCase;
import com.dx.hexacore.security.auth.application.command.port.out.AuthenticationRepository;
import com.dx.hexacore.security.auth.application.command.port.out.EventPublisher;
import com.dx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.dx.hexacore.security.auth.application.command.port.out.TokenProviderException;
import com.dx.hexacore.security.auth.domain.Authentication;
import com.dx.hexacore.security.auth.domain.vo.Credentials;
import com.dx.hexacore.security.auth.domain.vo.Token;

import java.util.Objects;

/**
 * 인증 사용 사례 구현체.
 * 사용자 인증 처리를 담당하는 애플리케이션 서비스입니다.
 * 
 * @since 1.0.0
 */
class AuthenticateUseCaseImpl implements AuthenticationUseCase {
    
    private final AuthenticationRepository authenticationRepository;
    private final TokenProvider tokenProvider;
    private final EventPublisher eventPublisher;
    
    /**
     * 인증 사용 사례를 생성합니다.
     * 
     * @param authenticationRepository 인증 저장소
     * @param tokenProvider 토큰 제공자
     * @param eventPublisher 이벤트 발행자
     */
    public AuthenticateUseCaseImpl(
        AuthenticationRepository authenticationRepository,
        TokenProvider tokenProvider,
        EventPublisher eventPublisher
    ) {
        this.authenticationRepository = Objects.requireNonNull(authenticationRepository, "authenticationRepository cannot be null");
        this.tokenProvider = Objects.requireNonNull(tokenProvider, "tokenProvider cannot be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher cannot be null");
    }
    
    @Override
    public AuthenticationResult authenticate(AuthenticateCommand command) {
        Objects.requireNonNull(command, "command cannot be null");
        
        // 인증 시도 생성 (도메인에서 이미 AuthenticationAttempted 이벤트 발행)
        Credentials credentials = Credentials.of(command.getUsername(), command.getPassword());
        Authentication authentication = Authentication.attemptAuthentication(credentials);
        
        // 도메인 이벤트 발행
        authentication.getDomainEvents().forEach(eventPublisher::publish);
        authentication.clearDomainEvents();
        
        try {
            // 토큰 제공자를 통한 인증
            Token token = tokenProvider.issueToken(credentials);
            
            // 인증 성공 처리
            authentication.markAsSuccessful(token);
            authenticationRepository.save(authentication);
            
            // 성공 도메인 이벤트 발행
            authentication.getDomainEvents().forEach(eventPublisher::publish);
            authentication.clearDomainEvents();
            
            return AuthenticationResult.success(command.getUsername(), token);
            
        } catch (TokenProviderException e) {
            // 인증 실패 처리
            String failureReason = "Authentication failed: " + e.getMessage();
            authentication.markAsFailed(failureReason);
            authenticationRepository.save(authentication);
            
            // 실패 도메인 이벤트 발행
            authentication.getDomainEvents().forEach(eventPublisher::publish);
            authentication.clearDomainEvents();
            
            return AuthenticationResult.failure(command.getUsername(), failureReason);
        }
    }
}