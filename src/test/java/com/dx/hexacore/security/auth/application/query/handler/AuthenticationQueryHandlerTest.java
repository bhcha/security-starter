package com.dx.hexacore.security.auth.application.query.handler;

import com.dx.hexacore.security.auth.application.exception.AuthenticationNotFoundException;
import com.dx.hexacore.security.auth.application.exception.TokenNotFoundException;
import com.dx.hexacore.security.auth.application.exception.ValidationException;
import com.dx.hexacore.security.auth.application.query.port.in.AuthenticationResponse;
import com.dx.hexacore.security.auth.application.query.port.in.GetAuthenticationQuery;
import com.dx.hexacore.security.auth.application.query.port.in.GetTokenInfoQuery;
import com.dx.hexacore.security.auth.application.query.port.in.TokenInfoResponse;
import com.dx.hexacore.security.auth.application.query.port.out.LoadAuthenticationQueryPort;
import com.dx.hexacore.security.auth.application.query.port.out.LoadTokenInfoQueryPort;
import com.dx.hexacore.security.auth.application.projection.AuthenticationProjection;
import com.dx.hexacore.security.auth.application.projection.TokenInfoProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationQueryHandler 테스트")
class AuthenticationQueryHandlerTest {

    @Mock
    private LoadAuthenticationQueryPort loadAuthenticationQueryPort;

    @Mock
    private LoadTokenInfoQueryPort loadTokenInfoQueryPort;

    @InjectMocks
    private AuthenticationQueryHandler handler;

    @BeforeEach
    void setUp() {
        // Mock 초기화는 @ExtendWith(MockitoExtension.class)에 의해 자동 처리됨
    }

    // GetAuthentication Use Case 테스트

    @Test
    @DisplayName("존재하는 인증 ID로 조회 시 Authentication 정보 반환")
    void shouldReturnAuthenticationWhenIdExists() {
        // Given
        String authenticationId = "auth-123";
        GetAuthenticationQuery query = GetAuthenticationQuery.of(authenticationId);
        
        AuthenticationProjection projection = AuthenticationProjection.builder()
            .id(authenticationId)
            .username("testuser")
            .status("SUCCESS")
            .attemptTime(LocalDateTime.now().minusMinutes(5))
            .successTime(LocalDateTime.now())
            .build();
            
        when(loadAuthenticationQueryPort.loadById(authenticationId))
            .thenReturn(Optional.of(projection));

        // When
        AuthenticationResponse response = handler.getAuthentication(query);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(authenticationId);
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        verify(loadAuthenticationQueryPort).loadById(authenticationId);
    }

    @Test
    @DisplayName("PENDING 상태 인증 조회 시 올바른 응답 반환")
    void shouldReturnPendingAuthenticationCorrectly() {
        // Given
        String authenticationId = "auth-pending";
        GetAuthenticationQuery query = GetAuthenticationQuery.of(authenticationId);
        
        AuthenticationProjection projection = AuthenticationProjection.builder()
            .id(authenticationId)
            .username("pendinguser")
            .status("PENDING")
            .attemptTime(LocalDateTime.now())
            .build();
            
        when(loadAuthenticationQueryPort.loadById(authenticationId))
            .thenReturn(Optional.of(projection));

        // When
        AuthenticationResponse response = handler.getAuthentication(query);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getSuccessTime()).isNull();
        assertThat(response.getFailureTime()).isNull();
    }

    @Test
    @DisplayName("SUCCESS 상태 인증 조회 시 토큰 정보 포함하여 반환")
    void shouldReturnSuccessAuthenticationWithTokenInfo() {
        // Given
        String authenticationId = "auth-success";
        GetAuthenticationQuery query = GetAuthenticationQuery.of(authenticationId);
        
        AuthenticationProjection projection = AuthenticationProjection.builder()
            .id(authenticationId)
            .username("successuser")
            .status("SUCCESS")
            .attemptTime(LocalDateTime.now().minusMinutes(10))
            .successTime(LocalDateTime.now().minusMinutes(5))
            .accessToken("access-token-123")
            .refreshToken("refresh-token-123")
            .tokenExpiresIn(3600L)
            .build();
            
        when(loadAuthenticationQueryPort.loadById(authenticationId))
            .thenReturn(Optional.of(projection));

        // When
        AuthenticationResponse response = handler.getAuthentication(query);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getAccessToken()).isEqualTo("access-token-123");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token-123");
        assertThat(response.getTokenExpiresIn()).isEqualTo(3600L);
    }

    @Test
    @DisplayName("FAILED 상태 인증 조회 시 실패 이유 포함하여 반환")
    void shouldReturnFailedAuthenticationWithReason() {
        // Given
        String authenticationId = "auth-failed";
        GetAuthenticationQuery query = GetAuthenticationQuery.of(authenticationId);
        
        AuthenticationProjection projection = AuthenticationProjection.builder()
            .id(authenticationId)
            .username("faileduser")
            .status("FAILED")
            .attemptTime(LocalDateTime.now().minusMinutes(10))
            .failureTime(LocalDateTime.now().minusMinutes(5))
            .failureReason("Invalid credentials")
            .build();
            
        when(loadAuthenticationQueryPort.loadById(authenticationId))
            .thenReturn(Optional.of(projection));

        // When
        AuthenticationResponse response = handler.getAuthentication(query);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("FAILED");
        assertThat(response.getFailureReason()).isEqualTo("Invalid credentials");
        assertThat(response.getFailureTime()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 인증 ID 조회 시 AuthenticationNotFoundException 발생")
    void shouldThrowAuthenticationNotFoundExceptionWhenIdNotExists() {
        // Given
        String authenticationId = "non-existent-auth";
        GetAuthenticationQuery query = GetAuthenticationQuery.of(authenticationId);
        
        when(loadAuthenticationQueryPort.loadById(authenticationId))
            .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> handler.getAuthentication(query))
            .isInstanceOf(AuthenticationNotFoundException.class)
            .hasMessageContaining(authenticationId);
    }

    @Test
    @DisplayName("null Query 입력 시 ValidationException 발생")
    void shouldThrowValidationExceptionWhenQueryIsNull() {
        // Given
        GetAuthenticationQuery query = null;

        // When & Then
        assertThatThrownBy(() -> handler.getAuthentication(query))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Query cannot be null");
    }

    @Test
    @DisplayName("LoadAuthenticationQueryPort 올바른 파라미터로 호출 확인")
    void shouldCallLoadAuthenticationQueryPortWithCorrectParameters() {
        // Given
        String authenticationId = "auth-123";
        GetAuthenticationQuery query = GetAuthenticationQuery.of(authenticationId);
        
        AuthenticationProjection projection = AuthenticationProjection.builder()
            .id(authenticationId)
            .username("testuser")
            .status("SUCCESS")
            .attemptTime(LocalDateTime.now())
            .build();
            
        when(loadAuthenticationQueryPort.loadById(authenticationId))
            .thenReturn(Optional.of(projection));

        // When
        handler.getAuthentication(query);

        // Then
        verify(loadAuthenticationQueryPort, times(1)).loadById(authenticationId);
        verifyNoMoreInteractions(loadAuthenticationQueryPort);
    }

    // GetTokenInfo Use Case 테스트

    @Test
    @DisplayName("유효한 토큰으로 조회 시 토큰 정보 반환")
    void shouldReturnTokenInfoWhenTokenIsValid() {
        // Given
        String token = "valid-access-token";
        GetTokenInfoQuery query = GetTokenInfoQuery.of(token);
        
        TokenInfoProjection projection = TokenInfoProjection.builder()
            .token(token)
            .isValid(true)
            .expiresAt(LocalDateTime.now().plusHours(1))
            .issuedAt(LocalDateTime.now().minusHours(1))
            .authenticationId("auth-123")
            .build();
            
        when(loadTokenInfoQueryPort.loadByToken(token))
            .thenReturn(Optional.of(projection));

        // When
        TokenInfoResponse response = handler.getTokenInfo(query);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo(token);
        assertThat(response.isValid()).isTrue();
        assertThat(response.getAuthenticationId()).isEqualTo("auth-123");
    }

    @Test
    @DisplayName("만료되지 않은 토큰 조회 시 isValid=true 반환")
    void shouldReturnValidTrueForNonExpiredToken() {
        // Given
        String token = "non-expired-token";
        GetTokenInfoQuery query = GetTokenInfoQuery.of(token);
        
        TokenInfoProjection projection = TokenInfoProjection.builder()
            .token(token)
            .isValid(true)
            .expiresAt(LocalDateTime.now().plusHours(2))
            .issuedAt(LocalDateTime.now().minusMinutes(30))
            .build();
            
        when(loadTokenInfoQueryPort.loadByToken(token))
            .thenReturn(Optional.of(projection));

        // When
        TokenInfoResponse response = handler.getTokenInfo(query);

        // Then
        assertThat(response.isValid()).isTrue();
        assertThat(response.getExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("만료된 토큰 조회 시 isValid=false 반환")
    void shouldReturnValidFalseForExpiredToken() {
        // Given
        String token = "expired-token";
        GetTokenInfoQuery query = GetTokenInfoQuery.of(token);
        
        TokenInfoProjection projection = TokenInfoProjection.builder()
            .token(token)
            .isValid(false)
            .expiresAt(LocalDateTime.now().minusHours(1))
            .issuedAt(LocalDateTime.now().minusHours(2))
            .build();
            
        when(loadTokenInfoQueryPort.loadByToken(token))
            .thenReturn(Optional.of(projection));

        // When
        TokenInfoResponse response = handler.getTokenInfo(query);

        // Then
        assertThat(response.isValid()).isFalse();
        assertThat(response.getExpiresAt()).isBefore(LocalDateTime.now());
    }

    @Test
    @DisplayName("토큰 만료 시간 정보 올바르게 반환")
    void shouldReturnCorrectExpirationTime() {
        // Given
        String token = "token-with-expiration";
        GetTokenInfoQuery query = GetTokenInfoQuery.of(token);
        LocalDateTime expectedExpiration = LocalDateTime.now().plusMinutes(30);
        
        TokenInfoProjection projection = TokenInfoProjection.builder()
            .token(token)
            .isValid(true)
            .expiresAt(expectedExpiration)
            .issuedAt(LocalDateTime.now())
            .build();
            
        when(loadTokenInfoQueryPort.loadByToken(token))
            .thenReturn(Optional.of(projection));

        // When
        TokenInfoResponse response = handler.getTokenInfo(query);

        // Then
        assertThat(response.getExpiresAt()).isEqualTo(expectedExpiration);
    }

    @Test
    @DisplayName("존재하지 않는 토큰 조회 시 TokenNotFoundException 발생")
    void shouldThrowTokenNotFoundExceptionWhenTokenNotExists() {
        // Given
        String token = "non-existent-token";
        GetTokenInfoQuery query = GetTokenInfoQuery.of(token);
        
        when(loadTokenInfoQueryPort.loadByToken(token))
            .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> handler.getTokenInfo(query))
            .isInstanceOf(TokenNotFoundException.class)
            .hasMessageContaining("Token not found");
    }

    @Test
    @DisplayName("null TokenInfo Query 입력 시 ValidationException 발생")
    void shouldThrowValidationExceptionWhenTokenQueryIsNull() {
        // Given
        GetTokenInfoQuery query = null;

        // When & Then
        assertThatThrownBy(() -> handler.getTokenInfo(query))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Query cannot be null");
    }

    @Test
    @DisplayName("LoadTokenInfoQueryPort 올바른 파라미터로 호출 확인")
    void shouldCallLoadTokenInfoQueryPortWithCorrectParameters() {
        // Given
        String token = "test-token";
        GetTokenInfoQuery query = GetTokenInfoQuery.of(token);
        
        TokenInfoProjection projection = TokenInfoProjection.builder()
            .token(token)
            .isValid(true)
            .expiresAt(LocalDateTime.now().plusHours(1))
            .build();
            
        when(loadTokenInfoQueryPort.loadByToken(token))
            .thenReturn(Optional.of(projection));

        // When
        handler.getTokenInfo(query);

        // Then
        verify(loadTokenInfoQueryPort, times(1)).loadByToken(token);
        verifyNoMoreInteractions(loadTokenInfoQueryPort);
    }
}