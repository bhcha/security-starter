package com.ldx.hexacore.security.auth.domain;

import com.ldx.hexacore.security.auth.domain.event.AuthenticationAttempted;
import com.ldx.hexacore.security.auth.domain.event.AuthenticationSucceeded;
import com.ldx.hexacore.security.auth.domain.event.AuthenticationFailed;
import com.ldx.hexacore.security.auth.domain.event.TokenExpired;
import com.ldx.hexacore.security.auth.domain.vo.AuthenticationStatus;
import com.ldx.hexacore.security.auth.domain.vo.Credentials;
import com.ldx.hexacore.security.auth.domain.vo.Token;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Authentication 애그리거트 루트 테스트")
class AuthenticationTest {

    @Test
    @DisplayName("유효한 자격증명으로 인증을 시도할 수 있다")
    void shouldAttemptAuthenticationWithValidCredentials() {
        // Given
        Credentials credentials = Credentials.of("testuser", "password123");

        // When
        Authentication authentication = Authentication.attemptAuthentication(credentials);

        // Then
        assertThat(authentication).isNotNull();
        assertThat(authentication.getId()).isNotNull();
        assertThat(authentication.getStatus().isPending()).isTrue();
        assertThat(authentication.getCredentials()).isEqualTo(credentials);
        assertThat(authentication.getToken()).isNull();
        assertThat(authentication.getAttemptTime()).isNotNull();
        
        // Domain Events 확인
        assertThat(authentication.getDomainEvents()).hasSize(1);
        assertThat(authentication.getDomainEvents().get(0)).isInstanceOf(AuthenticationAttempted.class);
    }

    @Test
    @DisplayName("null 자격증명으로 인증 시도 시 예외가 발생한다")
    void shouldThrowExceptionWhenCredentialsIsNull() {
        // When & Then
        assertThatThrownBy(() -> Authentication.attemptAuthentication(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Credentials cannot be null");
    }

    @Test
    @DisplayName("PENDING 상태에서 인증을 성공으로 처리할 수 있다")
    void shouldMarkAsSuccessfulFromPendingState() {
        // Given
        Credentials credentials = Credentials.of("testuser", "password123");
        Authentication authentication = Authentication.attemptAuthentication(credentials);
        Token token = Token.of("access-token-123", "refresh-token-456", 3600);
        authentication.clearDomainEvents(); // 기존 이벤트 클리어

        // When
        authentication.markAsSuccessful(token);

        // Then
        assertThat(authentication.getStatus().isSuccess()).isTrue();
        assertThat(authentication.getToken()).isEqualTo(token);
        assertThat(authentication.getSuccessTime()).isNotNull();
        
        // Domain Events 확인
        assertThat(authentication.getDomainEvents()).hasSize(1);
        assertThat(authentication.getDomainEvents().get(0)).isInstanceOf(AuthenticationSucceeded.class);
    }

    @Test
    @DisplayName("SUCCESS 상태에서 다시 성공 처리 시 예외가 발생한다")
    void shouldThrowExceptionWhenMarkingSuccessfulFromSuccessState() {
        // Given
        Credentials credentials = Credentials.of("testuser", "password123");
        Authentication authentication = Authentication.attemptAuthentication(credentials);
        Token token = Token.of("access-token-123", "refresh-token-456", 3600);
        authentication.markAsSuccessful(token);

        // When & Then
        assertThatThrownBy(() -> authentication.markAsSuccessful(token))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot mark as successful: authentication is not in PENDING state");
    }

    @Test
    @DisplayName("FAILED 상태에서 성공 처리 시 예외가 발생한다")
    void shouldThrowExceptionWhenMarkingSuccessfulFromFailedState() {
        // Given
        Credentials credentials = Credentials.of("testuser", "password123");
        Authentication authentication = Authentication.attemptAuthentication(credentials);
        authentication.markAsFailed("Invalid credentials");

        Token token = Token.of("access-token-123", "refresh-token-456", 3600);

        // When & Then
        assertThatThrownBy(() -> authentication.markAsSuccessful(token))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot mark as successful: authentication is not in PENDING state");
    }

    @Test
    @DisplayName("null 토큰으로 성공 처리 시 예외가 발생한다")
    void shouldThrowExceptionWhenMarkingSuccessfulWithNullToken() {
        // Given
        Credentials credentials = Credentials.of("testuser", "password123");
        Authentication authentication = Authentication.attemptAuthentication(credentials);

        // When & Then
        assertThatThrownBy(() -> authentication.markAsSuccessful(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Token cannot be null");
    }

    @Test
    @DisplayName("PENDING 상태에서 인증을 실패로 처리할 수 있다")
    void shouldMarkAsFailedFromPendingState() {
        // Given
        Credentials credentials = Credentials.of("testuser", "password123");
        Authentication authentication = Authentication.attemptAuthentication(credentials);
        String reason = "Invalid credentials";
        authentication.clearDomainEvents(); // 기존 이벤트 클리어

        // When
        authentication.markAsFailed(reason);

        // Then
        assertThat(authentication.getStatus().isFailed()).isTrue();
        assertThat(authentication.getFailureReason()).isEqualTo(reason);
        assertThat(authentication.getFailureTime()).isNotNull();
        
        // Domain Events 확인
        assertThat(authentication.getDomainEvents()).hasSize(1);
        assertThat(authentication.getDomainEvents().get(0)).isInstanceOf(AuthenticationFailed.class);
    }

    @Test
    @DisplayName("SUCCESS 상태에서 실패 처리 시 예외가 발생한다")
    void shouldThrowExceptionWhenMarkingFailedFromSuccessState() {
        // Given
        Credentials credentials = Credentials.of("testuser", "password123");
        Authentication authentication = Authentication.attemptAuthentication(credentials);
        Token token = Token.of("access-token-123", "refresh-token-456", 3600);
        authentication.markAsSuccessful(token);

        // When & Then
        assertThatThrownBy(() -> authentication.markAsFailed("Some reason"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot mark as failed: authentication is not in PENDING state");
    }

    @Test
    @DisplayName("null 또는 빈 사유로 실패 처리 시 예외가 발생한다")
    void shouldThrowExceptionWhenMarkingFailedWithNullOrEmptyReason() {
        // Given
        Credentials credentials = Credentials.of("testuser", "password123");
        Authentication authentication = Authentication.attemptAuthentication(credentials);

        // When & Then
        assertThatThrownBy(() -> authentication.markAsFailed(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Failure reason cannot be null or empty");

        assertThatThrownBy(() -> authentication.markAsFailed(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Failure reason cannot be null or empty");

        assertThatThrownBy(() -> authentication.markAsFailed("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Failure reason cannot be null or empty");
    }

    @Test
    @DisplayName("SUCCESS 상태에서 토큰을 만료시킬 수 있다")
    void shouldExpireTokenFromSuccessState() {
        // Given
        Credentials credentials = Credentials.of("testuser", "password123");
        Authentication authentication = Authentication.attemptAuthentication(credentials);
        Token token = Token.of("access-token-123", "refresh-token-456", 3600);
        authentication.markAsSuccessful(token);
        authentication.clearDomainEvents(); // 기존 이벤트 클리어

        // When
        authentication.expireToken();

        // Then
        assertThat(authentication.getToken().isExpired()).isTrue();
        assertThat(authentication.getTokenExpiredTime()).isNotNull();
        
        // Domain Events 확인
        assertThat(authentication.getDomainEvents()).hasSize(1);
        assertThat(authentication.getDomainEvents().get(0)).isInstanceOf(TokenExpired.class);
    }

    @Test
    @DisplayName("토큰이 없는 상태에서 만료 시도 시 예외가 발생한다")
    void shouldThrowExceptionWhenExpiringTokenWithoutToken() {
        // Given
        Credentials credentials = Credentials.of("testuser", "password123");
        Authentication authentication = Authentication.attemptAuthentication(credentials);

        // When & Then
        assertThatThrownBy(() -> authentication.expireToken())
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot expire token: no token present");
    }

    @Test
    @DisplayName("이미 만료된 토큰을 다시 만료 시도 시 예외가 발생한다")
    void shouldThrowExceptionWhenExpiringAlreadyExpiredToken() {
        // Given
        Credentials credentials = Credentials.of("testuser", "password123");
        Authentication authentication = Authentication.attemptAuthentication(credentials);
        Token token = Token.of("access-token-123", "refresh-token-456", 3600);
        authentication.markAsSuccessful(token);
        authentication.expireToken();

        // When & Then
        assertThatThrownBy(() -> authentication.expireToken())
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot expire token: token is already expired");
    }

    @Test
    @DisplayName("유효한 토큰이 있는 경우 토큰 유효성 검증이 true를 반환한다")
    void shouldReturnTrueWhenTokenIsValid() {
        // Given
        Credentials credentials = Credentials.of("testuser", "password123");
        Authentication authentication = Authentication.attemptAuthentication(credentials);
        Token token = Token.of("access-token-123", "refresh-token-456", 3600);
        authentication.markAsSuccessful(token);

        // When
        boolean isValid = authentication.isTokenValid();

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("만료된 토큰이 있는 경우 토큰 유효성 검증이 false를 반환한다")
    void shouldReturnFalseWhenTokenIsExpired() {
        // Given
        Credentials credentials = Credentials.of("testuser", "password123");
        Authentication authentication = Authentication.attemptAuthentication(credentials);
        Token token = Token.of("access-token-123", "refresh-token-456", 3600);
        authentication.markAsSuccessful(token);
        authentication.expireToken();

        // When
        boolean isValid = authentication.isTokenValid();

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("토큰이 없는 경우 토큰 유효성 검증이 false를 반환한다")
    void shouldReturnFalseWhenNoToken() {
        // Given
        Credentials credentials = Credentials.of("testuser", "password123");
        Authentication authentication = Authentication.attemptAuthentication(credentials);

        // When
        boolean isValid = authentication.isTokenValid();

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("같은 데이터로 생성된 두 Authentication은 동일하다")
    void shouldBeEqualWhenCreatedWithSameData() {
        // Given
        Credentials credentials = Credentials.of("testuser", "password123");
        UUID id = UUID.randomUUID();
        LocalDateTime attemptTime = LocalDateTime.now();

        // When
        Authentication auth1 = Authentication.of(id, credentials, AuthenticationStatus.pending(), attemptTime);
        Authentication auth2 = Authentication.of(id, credentials, AuthenticationStatus.pending(), attemptTime);

        // Then
        assertThat(auth1).isEqualTo(auth2);
        assertThat(auth1.hashCode()).isEqualTo(auth2.hashCode());
    }

    @Test
    @DisplayName("다른 ID로 생성된 두 Authentication은 다르다")
    void shouldNotBeEqualWhenCreatedWithDifferentId() {
        // Given
        Credentials credentials = Credentials.of("testuser", "password123");
        LocalDateTime attemptTime = LocalDateTime.now();

        // When
        Authentication auth1 = Authentication.of(UUID.randomUUID(), credentials, AuthenticationStatus.pending(), attemptTime);
        Authentication auth2 = Authentication.of(UUID.randomUUID(), credentials, AuthenticationStatus.pending(), attemptTime);

        // Then
        assertThat(auth1).isNotEqualTo(auth2);
    }

    @Test
    @DisplayName("toString 메서드가 올바른 정보를 포함한다")
    void shouldIncludeCorrectInformationInToString() {
        // Given
        Credentials credentials = Credentials.of("testuser", "password123");
        Authentication authentication = Authentication.attemptAuthentication(credentials);

        // When
        String toString = authentication.toString();

        // Then
        assertThat(toString).contains("Authentication");
        assertThat(toString).contains(authentication.getId().toString());
        assertThat(toString).contains("testuser");
        assertThat(toString).contains("PENDING");
    }

    @Test
    @DisplayName("SUCCESS 상태에서 토큰을 업데이트할 수 있다")
    void shouldUpdateTokenFromSuccessState() {
        // Given
        Credentials credentials = Credentials.of("testuser", "password123");
        Authentication authentication = Authentication.attemptAuthentication(credentials);
        Token originalToken = Token.of("old-access-token", "old-refresh-token", 3600);
        authentication.markAsSuccessful(originalToken);
        authentication.clearDomainEvents();

        Token newToken = Token.of("new-access-token", "new-refresh-token", 7200);

        // When
        authentication.updateToken(newToken);

        // Then
        assertThat(authentication.getToken()).isEqualTo(newToken);
        assertThat(authentication.getSuccessTime()).isNotNull();
        
        // Domain Events 확인
        assertThat(authentication.getDomainEvents()).hasSize(1);
        assertThat(authentication.getDomainEvents().get(0)).isInstanceOf(AuthenticationSucceeded.class);
    }

    @Test
    @DisplayName("PENDING 상태에서 토큰 업데이트 시 예외가 발생한다")
    void shouldThrowExceptionWhenUpdatingTokenFromPendingState() {
        // Given
        Credentials credentials = Credentials.of("testuser", "password123");
        Authentication authentication = Authentication.attemptAuthentication(credentials);
        Token newToken = Token.of("new-access-token", "new-refresh-token", 7200);

        // When & Then
        assertThatThrownBy(() -> authentication.updateToken(newToken))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot update token: authentication is not in SUCCESS state");
    }

    @Test
    @DisplayName("FAILED 상태에서 토큰 업데이트 시 예외가 발생한다")
    void shouldThrowExceptionWhenUpdatingTokenFromFailedState() {
        // Given
        Credentials credentials = Credentials.of("testuser", "password123");
        Authentication authentication = Authentication.attemptAuthentication(credentials);
        authentication.markAsFailed("Invalid credentials");
        Token newToken = Token.of("new-access-token", "new-refresh-token", 7200);

        // When & Then
        assertThatThrownBy(() -> authentication.updateToken(newToken))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot update token: authentication is not in SUCCESS state");
    }

    @Test
    @DisplayName("null 토큰으로 업데이트 시 예외가 발생한다")
    void shouldThrowExceptionWhenUpdatingWithNullToken() {
        // Given
        Credentials credentials = Credentials.of("testuser", "password123");
        Authentication authentication = Authentication.attemptAuthentication(credentials);
        Token originalToken = Token.of("old-access-token", "old-refresh-token", 3600);
        authentication.markAsSuccessful(originalToken);

        // When & Then
        assertThatThrownBy(() -> authentication.updateToken(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Token cannot be null");
    }
}