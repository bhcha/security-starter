package com.dx.hexacore.security.auth.application.command.handler;

import com.dx.hexacore.security.auth.application.command.port.in.RefreshTokenCommand;
import com.dx.hexacore.security.auth.application.command.port.in.TokenRefreshException;
import com.dx.hexacore.security.auth.application.command.port.in.TokenValidationResult;
import com.dx.hexacore.security.auth.application.command.port.in.ValidateTokenCommand;
import com.dx.hexacore.security.auth.application.command.port.out.AuthenticationRepository;
import com.dx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.dx.hexacore.security.auth.application.command.port.out.TokenProviderException;
import com.dx.hexacore.security.auth.domain.Authentication;
import com.dx.hexacore.security.auth.domain.service.JwtPolicy;
import com.dx.hexacore.security.auth.domain.service.SessionPolicy;
import com.dx.hexacore.security.auth.domain.vo.Token;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("TokenManagementUseCaseImpl 테스트")
class TokenManagementUseCaseImplTest {

    @Mock
    private AuthenticationRepository authenticationRepository;
    
    @Mock
    private TokenProvider tokenProvider;
    
    @Mock
    private JwtPolicy jwtPolicy;
    
    @Mock
    private SessionPolicy sessionPolicy;

    private TokenManagementUseCaseImpl tokenManagementUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tokenManagementUseCase = new TokenManagementUseCaseImpl(
            authenticationRepository,
            tokenProvider,
            jwtPolicy,
            sessionPolicy
        );
    }

    @Test
    @DisplayName("유효한 토큰을 성공적으로 검증한다")
    void shouldValidateTokenSuccessfully() {
        // given
        String accessToken = "valid.access.token";
        ValidateTokenCommand command = new ValidateTokenCommand(accessToken);
        
        Authentication auth = mock(Authentication.class);
        Token token = Token.of(accessToken, "refresh.token", 3600);
        
        when(authenticationRepository.findByAccessToken(accessToken)).thenReturn(Optional.of(auth));
        when(auth.getToken()).thenReturn(token);
        when(auth.isTokenValid()).thenReturn(true);
        when(jwtPolicy.validate(token)).thenReturn(true);
        when(sessionPolicy.validateSession(auth)).thenReturn(true);

        // when
        TokenValidationResult result = tokenManagementUseCase.validateToken(command);

        // then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getAccessToken()).isEqualTo(accessToken);
        assertThat(result.getInvalidReason()).isEmpty();
        
        verify(authenticationRepository).findByAccessToken(accessToken);
        verify(jwtPolicy).validate(token);
        verify(sessionPolicy).validateSession(auth);
    }

    @Test
    @DisplayName("존재하지 않는 토큰은 무효로 판정한다")
    void shouldInvalidateNonExistentToken() {
        // given
        String accessToken = "non.existent.token";
        ValidateTokenCommand command = new ValidateTokenCommand(accessToken);
        
        when(authenticationRepository.findByAccessToken(accessToken)).thenReturn(Optional.empty());

        // when
        TokenValidationResult result = tokenManagementUseCase.validateToken(command);

        // then
        assertThat(result.isInvalid()).isTrue();
        assertThat(result.getAccessToken()).isEqualTo(accessToken);
        assertThat(result.getInvalidReason()).isPresent();
        assertThat(result.getInvalidReason().get()).isEqualTo("Token not found");
        
        verify(authenticationRepository).findByAccessToken(accessToken);
        verifyNoInteractions(jwtPolicy, sessionPolicy);
    }

    @Test
    @DisplayName("만료된 토큰은 무효로 판정한다")
    void shouldInvalidateExpiredToken() {
        // given
        String accessToken = "expired.access.token";
        ValidateTokenCommand command = new ValidateTokenCommand(accessToken);
        
        Authentication auth = mock(Authentication.class);
        Token token = Token.of(accessToken, "refresh.token", 3600);
        
        when(authenticationRepository.findByAccessToken(accessToken)).thenReturn(Optional.of(auth));
        when(auth.getToken()).thenReturn(token);
        when(auth.isTokenValid()).thenReturn(false);

        // when
        TokenValidationResult result = tokenManagementUseCase.validateToken(command);

        // then
        assertThat(result.isInvalid()).isTrue();
        assertThat(result.getInvalidReason()).isPresent();
        assertThat(result.getInvalidReason().get()).isEqualTo("Token expired");
        
        verify(authenticationRepository).findByAccessToken(accessToken);
        verify(auth).isTokenValid();
        verifyNoInteractions(jwtPolicy, sessionPolicy);
    }

    @Test
    @DisplayName("JWT 정책 위반 토큰은 무효로 판정한다")
    void shouldInvalidateTokenViolatingJwtPolicy() {
        // given
        String accessToken = "policy.violating.token";
        ValidateTokenCommand command = new ValidateTokenCommand(accessToken);
        
        Authentication auth = mock(Authentication.class);
        Token token = Token.of(accessToken, "refresh.token", 3600);
        
        when(authenticationRepository.findByAccessToken(accessToken)).thenReturn(Optional.of(auth));
        when(auth.getToken()).thenReturn(token);
        when(auth.isTokenValid()).thenReturn(true);
        when(jwtPolicy.validate(token)).thenReturn(false);

        // when
        TokenValidationResult result = tokenManagementUseCase.validateToken(command);

        // then
        assertThat(result.isInvalid()).isTrue();
        assertThat(result.getInvalidReason()).isPresent();
        assertThat(result.getInvalidReason().get()).isEqualTo("JWT policy violation");
        
        verify(jwtPolicy).validate(token);
        verifyNoInteractions(sessionPolicy);
    }

    @Test
    @DisplayName("세션 정책 위반 토큰은 무효로 판정한다")
    void shouldInvalidateTokenViolatingSessionPolicy() {
        // given
        String accessToken = "session.violating.token";
        ValidateTokenCommand command = new ValidateTokenCommand(accessToken);
        
        Authentication auth = mock(Authentication.class);
        Token token = Token.of(accessToken, "refresh.token", 3600);
        
        when(authenticationRepository.findByAccessToken(accessToken)).thenReturn(Optional.of(auth));
        when(auth.getToken()).thenReturn(token);
        when(auth.isTokenValid()).thenReturn(true);
        when(jwtPolicy.validate(token)).thenReturn(true);
        when(sessionPolicy.validateSession(auth)).thenReturn(false);

        // when
        TokenValidationResult result = tokenManagementUseCase.validateToken(command);

        // then
        assertThat(result.isInvalid()).isTrue();
        assertThat(result.getInvalidReason()).isPresent();
        assertThat(result.getInvalidReason().get()).isEqualTo("Session policy violation");
        
        verify(sessionPolicy).validateSession(auth);
    }

    @Test
    @DisplayName("유효한 리프레시 토큰으로 새 토큰을 발급한다")
    void shouldRefreshTokenSuccessfully() {
        // given
        String refreshToken = "valid.refresh.token";
        RefreshTokenCommand command = new RefreshTokenCommand(refreshToken);
        
        Authentication auth = mock(Authentication.class);
        Token newToken = Token.of("new.access.token", "new.refresh.token", 3600);
        
        when(authenticationRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.of(auth));
        when(auth.isTokenValid()).thenReturn(true);
        when(tokenProvider.refreshToken(refreshToken)).thenReturn(newToken);
        when(authenticationRepository.save(auth)).thenReturn(auth);

        // when
        Token result = tokenManagementUseCase.refreshToken(command);

        // then
        assertThat(result).isEqualTo(newToken);
        
        verify(authenticationRepository).findByRefreshToken(refreshToken);
        verify(tokenProvider).refreshToken(refreshToken);
        verify(authenticationRepository).save(auth);
    }

    @Test
    @DisplayName("존재하지 않는 리프레시 토큰으로 갱신 시 예외가 발생한다")
    void shouldThrowExceptionWhenRefreshTokenNotFound() {
        // given
        String refreshToken = "non.existent.refresh.token";
        RefreshTokenCommand command = new RefreshTokenCommand(refreshToken);
        
        when(authenticationRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tokenManagementUseCase.refreshToken(command))
            .isInstanceOf(TokenRefreshException.class)
            .hasMessage("Refresh token not found");
        
        verify(authenticationRepository).findByRefreshToken(refreshToken);
        verifyNoInteractions(tokenProvider);
    }

    @Test
    @DisplayName("만료된 리프레시 토큰으로 갱신 시 예외가 발생한다")
    void shouldThrowExceptionWhenRefreshTokenExpired() {
        // given
        String refreshToken = "expired.refresh.token";
        RefreshTokenCommand command = new RefreshTokenCommand(refreshToken);
        
        Authentication auth = mock(Authentication.class);
        
        when(authenticationRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.of(auth));
        when(auth.isTokenValid()).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> tokenManagementUseCase.refreshToken(command))
            .isInstanceOf(TokenRefreshException.class)
            .hasMessage("Refresh token expired");
        
        verify(authenticationRepository).findByRefreshToken(refreshToken);
        verifyNoInteractions(tokenProvider);
    }

    @Test
    @DisplayName("외부 제공자 토큰 갱신 실패 시 예외가 발생한다")
    void shouldThrowExceptionWhenExternalProviderRefreshFails() {
        // given
        String refreshToken = "valid.refresh.token";
        RefreshTokenCommand command = new RefreshTokenCommand(refreshToken);
        
        Authentication auth = mock(Authentication.class);
        
        when(authenticationRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.of(auth));
        when(auth.isTokenValid()).thenReturn(true);
        when(tokenProvider.refreshToken(refreshToken))
            .thenThrow(TokenProviderException.tokenExpired("test-provider"));

        // when & then
        assertThatThrownBy(() -> tokenManagementUseCase.refreshToken(command))
            .isInstanceOf(TokenRefreshException.class)
            .hasMessage("Failed to refresh token")
            .hasCauseInstanceOf(TokenProviderException.class);
        
        verify(tokenProvider).refreshToken(refreshToken);
        verify(authenticationRepository, never()).save(any());
    }

    @Test
    @DisplayName("null command로 호출 시 예외가 발생한다")
    void shouldThrowExceptionWhenValidateTokenCommandIsNull() {
        // when & then
        assertThatThrownBy(() -> tokenManagementUseCase.validateToken(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("command cannot be null");
        
        verifyNoInteractions(authenticationRepository, jwtPolicy, sessionPolicy);
    }

    @Test
    @DisplayName("null command로 토큰 갱신 호출 시 예외가 발생한다")
    void shouldThrowExceptionWhenRefreshTokenCommandIsNull() {
        // when & then
        assertThatThrownBy(() -> tokenManagementUseCase.refreshToken(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("command cannot be null");
        
        verifyNoInteractions(authenticationRepository, tokenProvider);
    }
}