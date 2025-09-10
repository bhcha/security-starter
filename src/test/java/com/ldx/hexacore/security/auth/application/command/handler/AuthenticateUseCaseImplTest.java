package com.ldx.hexacore.security.auth.application.command.handler;

import com.ldx.hexacore.security.auth.application.command.port.in.AuthenticateCommand;
import com.ldx.hexacore.security.auth.application.command.port.in.AuthenticationResult;
import com.ldx.hexacore.security.auth.application.command.port.out.AuthenticationRepository;
import com.ldx.hexacore.security.auth.application.command.port.out.EventPublisher;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenProviderException;
import com.ldx.hexacore.security.auth.domain.Authentication;
import com.ldx.hexacore.security.auth.domain.event.DomainEvent;
import com.ldx.hexacore.security.auth.domain.vo.Credentials;
import com.ldx.hexacore.security.auth.domain.vo.Token;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.atLeast;

@DisplayName("AuthenticateUseCaseImpl 테스트")
class AuthenticateUseCaseImplTest {

    @Mock
    private AuthenticationRepository authenticationRepository;
    
    @Mock
    private TokenProvider tokenProvider;
    
    @Mock
    private EventPublisher eventPublisher;

    private AuthenticateUseCaseImpl authenticateUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authenticateUseCase = new AuthenticateUseCaseImpl(
            authenticationRepository,
            tokenProvider,
            eventPublisher
        );
    }

    @Test
    @DisplayName("유효한 인증정보로 인증에 성공한다")
    void shouldAuthenticateSuccessfullyWithValidCredentials() {
        // given
        String username = "testuser";
        String password = "testpassword123";
        AuthenticateCommand command = new AuthenticateCommand(username, password);
        
        Token expectedToken = Token.of("access.token", "refresh.token", 3600);
        Authentication savedAuth = mock(Authentication.class);
        
        when(tokenProvider.issueToken(any(Credentials.class))).thenReturn(expectedToken);
        when(authenticationRepository.save(any(Authentication.class))).thenReturn(savedAuth);

        // when
        AuthenticationResult result = authenticateUseCase.authenticate(command);

        // then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getToken()).isPresent();
        assertThat(result.getToken().get()).isEqualTo(expectedToken);
        
        verify(tokenProvider).issueToken(any(Credentials.class));
        verify(authenticationRepository).save(any(Authentication.class));
        verify(eventPublisher, atLeast(1)).publish(any(DomainEvent.class)); // 도메인 이벤트들
    }

    @Test
    @DisplayName("잘못된 인증정보로 인증에 실패한다")
    void shouldFailAuthenticationWithInvalidCredentials() {
        // given
        String username = "testuser";
        String password = "wrongpassword";
        AuthenticateCommand command = new AuthenticateCommand(username, password);
        
        when(tokenProvider.issueToken(any(Credentials.class)))
            .thenThrow(TokenProviderException.invalidCredentials("test-provider"));

        // when
        AuthenticationResult result = authenticateUseCase.authenticate(command);

        // then
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getToken()).isEmpty();
        assertThat(result.getFailureReason()).isPresent();
        assertThat(result.getFailureReason().get()).contains("Invalid credentials");
        
        verify(tokenProvider).issueToken(any(Credentials.class));
        verify(authenticationRepository).save(any(Authentication.class));
        verify(eventPublisher, atLeast(1)).publish(any(DomainEvent.class)); // 도메인 이벤트들
    }

    @Test
    @DisplayName("외부 인증 제공자 연동 실패 시 인증이 실패한다")
    void shouldFailAuthenticationWhenExternalProviderFails() {
        // given
        AuthenticateCommand command = new AuthenticateCommand("testuser", "testpassword123");
        
        when(tokenProvider.issueToken(any(Credentials.class)))
            .thenThrow(TokenProviderException.providerUnavailable("test-provider", new RuntimeException("Provider unavailable")));

        // when
        AuthenticationResult result = authenticateUseCase.authenticate(command);

        // then
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getFailureReason()).isPresent();
        assertThat(result.getFailureReason().get()).contains("Token provider is unavailable");
        
        verify(tokenProvider).issueToken(any(Credentials.class));
        verify(authenticationRepository).save(any(Authentication.class));
        verify(eventPublisher, atLeast(1)).publish(any(DomainEvent.class)); // 도메인 이벤트들
    }

    @Test
    @DisplayName("저장소 저장 실패 시 적절한 예외가 발생한다")
    void shouldHandleRepositorySaveFailure() {
        // given
        AuthenticateCommand command = new AuthenticateCommand("testuser", "testpassword123");
        Token token = Token.of("access.token", "refresh.token", 3600);
        
        when(tokenProvider.issueToken(any(Credentials.class))).thenReturn(token);
        when(authenticationRepository.save(any(Authentication.class)))
            .thenThrow(new RuntimeException("Database connection failed"));

        // when & then
        assertThatThrownBy(() -> authenticateUseCase.authenticate(command))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Database connection failed");
        
        verify(tokenProvider).issueToken(any(Credentials.class));
        verify(authenticationRepository).save(any(Authentication.class));
        verify(eventPublisher, atLeast(1)).publish(any(DomainEvent.class)); // 최소한 시도 이벤트
    }

    @Test
    @DisplayName("null command로 호출 시 예외가 발생한다")
    void shouldThrowExceptionWhenCommandIsNull() {
        // when & then
        assertThatThrownBy(() -> authenticateUseCase.authenticate(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("command cannot be null");
        
        verifyNoInteractions(tokenProvider, authenticationRepository, eventPublisher);
    }

    @Test
    @DisplayName("성공한 인증은 도메인 이벤트를 발행한다")
    void shouldPublishDomainEventsOnSuccessfulAuthentication() {
        // given
        AuthenticateCommand command = new AuthenticateCommand("testuser", "testpassword123");
        Token token = Token.of("access.token", "refresh.token", 3600);
        Authentication savedAuth = mock(Authentication.class);
        
        when(tokenProvider.issueToken(any(Credentials.class))).thenReturn(token);
        when(authenticationRepository.save(any(Authentication.class))).thenReturn(savedAuth);

        // when
        authenticateUseCase.authenticate(command);

        // then
        verify(eventPublisher, atLeast(1)).publish(any(DomainEvent.class)); // 도메인 이벤트들 발행됨
    }

    @Test
    @DisplayName("실패한 인증은 실패 이벤트를 발행한다")
    void shouldPublishFailureEventOnFailedAuthentication() {
        // given
        AuthenticateCommand command = new AuthenticateCommand("testuser", "wrongpassword");
        
        when(tokenProvider.issueToken(any(Credentials.class)))
            .thenThrow(TokenProviderException.invalidCredentials("test-provider"));

        // when
        authenticateUseCase.authenticate(command);

        // then
        verify(eventPublisher, atLeast(1)).publish(any(DomainEvent.class)); // 도메인 이벤트들 발행됨
    }
}