package com.dx.hexacore.security.auth.domain.service;

import com.dx.hexacore.security.auth.domain.Authentication;
import com.dx.hexacore.security.auth.domain.vo.Credentials;
import com.dx.hexacore.security.auth.domain.vo.Token;
import com.dx.hexacore.security.auth.domain.vo.AuthenticationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SessionPolicy 테스트")
class SessionPolicyTest {

    private SessionPolicy sessionPolicy;

    @BeforeEach
    void setUp() {
        sessionPolicy = new SessionPolicy();
    }

    @Test
    @DisplayName("성공 상태이고 유효한 토큰을 가진 세션이 유효하다")
    void shouldValidateSuccessfulSessionWithValidToken() {
        // Given
        Credentials credentials = Credentials.of("testuser", "password123");
        Authentication authentication = Authentication.attemptAuthentication(credentials);
        Token token = Token.of("access-token", "refresh-token", 3600);
        authentication.markAsSuccessful(token);

        // When
        boolean isValid = sessionPolicy.validateSession(authentication);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("PENDING 상태의 세션은 유효하지 않다")
    void shouldNotValidatePendingSession() {
        // Given
        Credentials credentials = Credentials.of("testuser", "password123");
        Authentication authentication = Authentication.attemptAuthentication(credentials);

        // When
        boolean isValid = sessionPolicy.validateSession(authentication);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("FAILED 상태의 세션은 유효하지 않다")
    void shouldNotValidateFailedSession() {
        // Given
        Credentials credentials = Credentials.of("testuser", "password123");
        Authentication authentication = Authentication.attemptAuthentication(credentials);
        authentication.markAsFailed("Invalid credentials");

        // When
        boolean isValid = sessionPolicy.validateSession(authentication);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("성공 상태이지만 만료된 토큰을 가진 세션은 유효하지 않다")
    void shouldNotValidateSessionWithExpiredToken() {
        // Given
        Credentials credentials = Credentials.of("testuser", "password123");
        Authentication authentication = Authentication.attemptAuthentication(credentials);
        Token token = Token.of("access-token", "refresh-token", 3600);
        authentication.markAsSuccessful(token);
        authentication.expireToken();

        // When
        boolean isValid = sessionPolicy.validateSession(authentication);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("null 인증 객체에 대해 false를 반환한다")
    void shouldReturnFalseForNullAuthentication() {
        // When
        boolean isValid = sessionPolicy.validateSession(null);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("세션 만료 시간이 초과된 경우 유효하지 않다")
    void shouldNotValidateExpiredSession() {
        // Given
        Credentials credentials = Credentials.of("testuser", "password123");
        LocalDateTime pastTime = LocalDateTime.now().minusHours(25); // 25시간 전
        Authentication authentication = Authentication.of(
            java.util.UUID.randomUUID(),
            credentials,
            AuthenticationStatus.pending(),
            pastTime
        );
        Token token = Token.of("access-token", "refresh-token", 3600);
        authentication.markAsSuccessful(token);

        // When
        boolean isValid = sessionPolicy.validateSession(authentication);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("최근 세션은 유효하다")
    void shouldValidateRecentSession() {
        // Given
        Credentials credentials = Credentials.of("testuser", "password123");
        LocalDateTime recentTime = LocalDateTime.now().minusMinutes(30); // 30분 전
        Authentication authentication = Authentication.of(
            java.util.UUID.randomUUID(),
            credentials,
            AuthenticationStatus.pending(),
            recentTime
        );
        Token token = Token.of("access-token", "refresh-token", 3600);
        authentication.markAsSuccessful(token);

        // When
        boolean isValid = sessionPolicy.validateSession(authentication);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("세션 정책 검증은 stateless하게 동작한다")
    void shouldOperateStateless() {
        // Given
        Credentials credentials1 = Credentials.of("user1", "password1");
        Credentials credentials2 = Credentials.of("user2", "password2");
        
        Authentication auth1 = Authentication.attemptAuthentication(credentials1);
        Authentication auth2 = Authentication.attemptAuthentication(credentials2);
        
        Token token = Token.of("access-token", "refresh-token", 3600);
        auth1.markAsSuccessful(token);
        auth2.markAsSuccessful(token);

        // When
        boolean isValid1 = sessionPolicy.validateSession(auth1);
        boolean isValid2 = sessionPolicy.validateSession(auth2);

        // Then
        assertThat(isValid1).isTrue();
        assertThat(isValid2).isTrue();
        
        // 이전 검증이 다음 검증에 영향을 주지 않음을 확인
        boolean isValid1Again = sessionPolicy.validateSession(auth1);
        assertThat(isValid1Again).isTrue();
    }

    @Test
    @DisplayName("동일한 세션에 대해 일관된 검증 결과를 반환한다")
    void shouldReturnConsistentValidationResults() {
        // Given
        Credentials credentials = Credentials.of("testuser", "password123");
        Authentication authentication = Authentication.attemptAuthentication(credentials);
        Token token = Token.of("access-token", "refresh-token", 3600);
        authentication.markAsSuccessful(token);

        // When
        boolean result1 = sessionPolicy.validateSession(authentication);
        boolean result2 = sessionPolicy.validateSession(authentication);
        boolean result3 = sessionPolicy.validateSession(authentication);

        // Then
        assertThat(result1).isEqualTo(result2).isEqualTo(result3);
        assertThat(result1).isTrue();
    }
}