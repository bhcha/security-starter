package com.ldx.hexacore.security.auth.application.command.handler;

import com.ldx.hexacore.security.auth.application.command.port.in.RefreshTokenCommand;
import com.ldx.hexacore.security.auth.application.command.port.in.TokenRefreshException;
import com.ldx.hexacore.security.auth.application.command.port.in.TokenValidationResult;
import com.ldx.hexacore.security.auth.application.command.port.in.ValidateTokenCommand;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenProviderException;
import com.ldx.hexacore.security.auth.domain.vo.Token;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("TokenManagementUseCaseImpl 테스트")
class TokenManagementUseCaseImplTest {

    @Mock
    private TokenProvider tokenProvider;

    private TokenManagementUseCaseImpl tokenManagementUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tokenManagementUseCase = new TokenManagementUseCaseImpl(tokenProvider);
    }

    @Test
    @DisplayName("유효한 토큰을 성공적으로 검증한다")
    void shouldValidateTokenSuccessfully() {
        // given
        String accessToken = "valid.access.token";
        ValidateTokenCommand command = new ValidateTokenCommand(accessToken);

        com.ldx.hexacore.security.auth.application.command.port.out.TokenValidationResult providerResult =
            com.ldx.hexacore.security.auth.application.command.port.out.TokenValidationResult.valid(
                "user123",
                "testuser",
                Set.of("USER"),
                Instant.now().plusSeconds(3600)
            );

        when(tokenProvider.validateToken(accessToken)).thenReturn(providerResult);

        // when
        TokenValidationResult result = tokenManagementUseCase.validateToken(command);

        // then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getAccessToken()).isEqualTo(accessToken);
        assertThat(result.getInvalidReason()).isEmpty();

        verify(tokenProvider).validateToken(accessToken);
    }

    @Test
    @DisplayName("만료된 토큰은 무효로 판정한다")
    void shouldInvalidateExpiredToken() {
        // given
        String accessToken = "expired.token";
        ValidateTokenCommand command = new ValidateTokenCommand(accessToken);

        com.ldx.hexacore.security.auth.application.command.port.out.TokenValidationResult providerResult =
            com.ldx.hexacore.security.auth.application.command.port.out.TokenValidationResult.invalid("Token expired");

        when(tokenProvider.validateToken(accessToken)).thenReturn(providerResult);

        // when
        TokenValidationResult result = tokenManagementUseCase.validateToken(command);

        // then
        assertThat(result.isInvalid()).isTrue();
        assertThat(result.getAccessToken()).isEqualTo(accessToken);
        assertThat(result.getInvalidReason()).isPresent();
        assertThat(result.getInvalidReason().get()).contains("Token expired");

        verify(tokenProvider).validateToken(accessToken);
    }

    @Test
    @DisplayName("TokenProvider 예외 발생 시 무효로 처리한다")
    void shouldHandleTokenProviderException() {
        // given
        String accessToken = "problematic.token";
        ValidateTokenCommand command = new ValidateTokenCommand(accessToken);

        when(tokenProvider.validateToken(accessToken))
            .thenThrow(TokenProviderException.tokenValidationFailed("test-provider", new RuntimeException("Validation error")));

        // when
        TokenValidationResult result = tokenManagementUseCase.validateToken(command);

        // then
        assertThat(result.isInvalid()).isTrue();
        assertThat(result.getInvalidReason()).isPresent();
        assertThat(result.getInvalidReason().get()).contains("Token validation failed");

        verify(tokenProvider).validateToken(accessToken);
    }

    @Test
    @DisplayName("리프레시 토큰으로 새로운 토큰을 발급한다")
    void shouldRefreshTokenSuccessfully() {
        // given
        String refreshToken = "valid.refresh.token";
        RefreshTokenCommand command = new RefreshTokenCommand(refreshToken);

        Token newToken = Token.of("new.access.token", "new.refresh.token", 3600);

        when(tokenProvider.refreshToken(refreshToken)).thenReturn(newToken);

        // when
        Token result = tokenManagementUseCase.refreshToken(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("new.access.token");
        assertThat(result.getRefreshToken()).isEqualTo("new.refresh.token");

        verify(tokenProvider).refreshToken(refreshToken);
    }

    @Test
    @DisplayName("유효하지 않은 리프레시 토큰으로 갱신 시 예외가 발생한다")
    void shouldThrowExceptionWhenRefreshTokenIsInvalid() {
        // given
        String refreshToken = "invalid.refresh.token";
        RefreshTokenCommand command = new RefreshTokenCommand(refreshToken);

        when(tokenProvider.refreshToken(refreshToken))
            .thenThrow(TokenProviderException.tokenRefreshFailed("test-provider", new RuntimeException("Invalid refresh token")));

        // when & then
        assertThatThrownBy(() -> tokenManagementUseCase.refreshToken(command))
            .isInstanceOf(TokenRefreshException.class)
            .hasMessageContaining("Failed to refresh token");

        verify(tokenProvider).refreshToken(refreshToken);
    }

    @Test
    @DisplayName("null ValidateTokenCommand로 호출 시 예외가 발생한다")
    void shouldThrowExceptionWhenValidateCommandIsNull() {
        // when & then
        assertThatThrownBy(() -> tokenManagementUseCase.validateToken(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("command cannot be null");

        verifyNoInteractions(tokenProvider);
    }

    @Test
    @DisplayName("null RefreshTokenCommand로 호출 시 예외가 발생한다")
    void shouldThrowExceptionWhenRefreshCommandIsNull() {
        // when & then
        assertThatThrownBy(() -> tokenManagementUseCase.refreshToken(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("command cannot be null");

        verifyNoInteractions(tokenProvider);
    }

    @Test
    @DisplayName("토큰 제공자가 사용 불가능할 때 적절한 처리를 한다")
    void shouldHandleProviderUnavailable() {
        // given
        String accessToken = "some.token";
        ValidateTokenCommand command = new ValidateTokenCommand(accessToken);

        when(tokenProvider.validateToken(accessToken))
            .thenThrow(TokenProviderException.providerUnavailable("test-provider", new RuntimeException("Service down")));

        // when
        TokenValidationResult result = tokenManagementUseCase.validateToken(command);

        // then
        assertThat(result.isInvalid()).isTrue();
        assertThat(result.getInvalidReason()).isPresent();
        assertThat(result.getInvalidReason().get()).contains("Token validation failed");

        verify(tokenProvider).validateToken(accessToken);
    }
}