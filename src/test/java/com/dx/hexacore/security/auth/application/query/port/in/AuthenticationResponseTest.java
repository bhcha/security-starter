package com.dx.hexacore.security.auth.application.query.port.in;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AuthenticationResponse 테스트")
class AuthenticationResponseTest {

    @Test
    @DisplayName("SUCCESS 상태 응답 생성 확인")
    void shouldCreateSuccessResponse() {
        // Given
        String id = "auth-success";
        String username = "successuser";
        LocalDateTime attemptTime = LocalDateTime.now().minusMinutes(10);
        LocalDateTime successTime = LocalDateTime.now().minusMinutes(5);
        String accessToken = "access-token-123";
        String refreshToken = "refresh-token-123";
        Long tokenExpiresIn = 3600L;

        // When
        AuthenticationResponse response = AuthenticationResponse.builder()
            .id(id)
            .username(username)
            .status("SUCCESS")
            .attemptTime(attemptTime)
            .successTime(successTime)
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenExpiresIn(tokenExpiresIn)
            .build();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getUsername()).isEqualTo(username);
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getAttemptTime()).isEqualTo(attemptTime);
        assertThat(response.getSuccessTime()).isEqualTo(successTime);
        assertThat(response.getAccessToken()).isEqualTo(accessToken);
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(response.getTokenExpiresIn()).isEqualTo(tokenExpiresIn);
        assertThat(response.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("FAILED 상태 응답 생성 확인")
    void shouldCreateFailedResponse() {
        // Given
        String id = "auth-failed";
        String username = "faileduser";
        LocalDateTime attemptTime = LocalDateTime.now().minusMinutes(10);
        LocalDateTime failureTime = LocalDateTime.now().minusMinutes(5);
        String failureReason = "Invalid credentials";

        // When
        AuthenticationResponse response = AuthenticationResponse.builder()
            .id(id)
            .username(username)
            .status("FAILED")
            .attemptTime(attemptTime)
            .failureTime(failureTime)
            .failureReason(failureReason)
            .build();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getUsername()).isEqualTo(username);
        assertThat(response.getStatus()).isEqualTo("FAILED");
        assertThat(response.getFailureTime()).isEqualTo(failureTime);
        assertThat(response.getFailureReason()).isEqualTo(failureReason);
        assertThat(response.getSuccessTime()).isNull();
        assertThat(response.getAccessToken()).isNull();
        assertThat(response.isSuccess()).isFalse();
    }

    @Test
    @DisplayName("PENDING 상태 응답 생성 확인")
    void shouldCreatePendingResponse() {
        // Given
        String id = "auth-pending";
        String username = "pendinguser";
        LocalDateTime attemptTime = LocalDateTime.now();

        // When
        AuthenticationResponse response = AuthenticationResponse.builder()
            .id(id)
            .username(username)
            .status("PENDING")
            .attemptTime(attemptTime)
            .build();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getUsername()).isEqualTo(username);
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getAttemptTime()).isEqualTo(attemptTime);
        assertThat(response.getSuccessTime()).isNull();
        assertThat(response.getFailureTime()).isNull();
        assertThat(response.getFailureReason()).isNull();
        assertThat(response.getAccessToken()).isNull();
        assertThat(response.getRefreshToken()).isNull();
        assertThat(response.isPending()).isTrue();
        assertThat(response.isSuccess()).isFalse();
    }

    @Test
    @DisplayName("토큰 정보 포함 응답 생성 확인")
    void shouldCreateResponseWithTokenInfo() {
        // Given
        String accessToken = "detailed-access-token";
        String refreshToken = "detailed-refresh-token";
        Long tokenExpiresIn = 7200L;

        // When
        AuthenticationResponse response = AuthenticationResponse.builder()
            .id("auth-with-token")
            .username("tokenuser")
            .status("SUCCESS")
            .attemptTime(LocalDateTime.now())
            .successTime(LocalDateTime.now())
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenExpiresIn(tokenExpiresIn)
            .build();

        // Then
        assertThat(response.getAccessToken()).isEqualTo(accessToken);
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(response.getTokenExpiresIn()).isEqualTo(tokenExpiresIn);
        assertThat(response.hasToken()).isTrue();
    }

    @Test
    @DisplayName("실패 이유 포함 응답 생성 확인")
    void shouldCreateResponseWithFailureReason() {
        // Given
        String failureReason = "Account is locked due to multiple failed attempts";

        // When
        AuthenticationResponse response = AuthenticationResponse.builder()
            .id("auth-with-reason")
            .username("reasonuser")
            .status("FAILED")
            .attemptTime(LocalDateTime.now())
            .failureTime(LocalDateTime.now())
            .failureReason(failureReason)
            .build();

        // Then
        assertThat(response.getFailureReason()).isEqualTo(failureReason);
        assertThat(response.hasFailureReason()).isTrue();
        assertThat(response.isFailed()).isTrue();
    }

    @Test
    @DisplayName("정적 팩토리 메서드 - success() 테스트")
    void shouldCreateSuccessResponseUsingStaticFactory() {
        // Given
        String id = "auth-static-success";
        String username = "staticuser";
        String accessToken = "static-access-token";

        // When
        AuthenticationResponse response = AuthenticationResponse.success(
            id, username, accessToken, "static-refresh-token", 3600L
        );

        // Then
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getUsername()).isEqualTo(username);
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getAccessToken()).isEqualTo(accessToken);
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getAttemptTime()).isNotNull();
        assertThat(response.getSuccessTime()).isNotNull();
    }

    @Test
    @DisplayName("정적 팩토리 메서드 - failed() 테스트")
    void shouldCreateFailedResponseUsingStaticFactory() {
        // Given
        String id = "auth-static-failed";
        String username = "staticfaileduser";
        String reason = "Static failure reason";

        // When
        AuthenticationResponse response = AuthenticationResponse.failed(
            id, username, reason
        );

        // Then
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getUsername()).isEqualTo(username);
        assertThat(response.getStatus()).isEqualTo("FAILED");
        assertThat(response.getFailureReason()).isEqualTo(reason);
        assertThat(response.isFailed()).isTrue();
        assertThat(response.getAttemptTime()).isNotNull();
        assertThat(response.getFailureTime()).isNotNull();
    }

    @Test
    @DisplayName("정적 팩토리 메서드 - pending() 테스트")
    void shouldCreatePendingResponseUsingStaticFactory() {
        // Given
        String id = "auth-static-pending";
        String username = "staticpendinguser";

        // When
        AuthenticationResponse response = AuthenticationResponse.pending(id, username);

        // Then
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getUsername()).isEqualTo(username);
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.isPending()).isTrue();
        assertThat(response.getAttemptTime()).isNotNull();
    }

    @Test
    @DisplayName("상태 검사 메서드들 올바른 동작 확인")
    void shouldHaveCorrectStatusMethods() {
        // Given
        AuthenticationResponse successResponse = AuthenticationResponse.success(
            "auth-1", "user1", "token", "refresh", 3600L);
        AuthenticationResponse failedResponse = AuthenticationResponse.failed(
            "auth-2", "user2", "reason");
        AuthenticationResponse pendingResponse = AuthenticationResponse.pending(
            "auth-3", "user3");

        // When & Then
        assertThat(successResponse.isSuccess()).isTrue();
        assertThat(successResponse.isFailed()).isFalse();
        assertThat(successResponse.isPending()).isFalse();

        assertThat(failedResponse.isSuccess()).isFalse();
        assertThat(failedResponse.isFailed()).isTrue();
        assertThat(failedResponse.isPending()).isFalse();

        assertThat(pendingResponse.isSuccess()).isFalse();
        assertThat(pendingResponse.isFailed()).isFalse();
        assertThat(pendingResponse.isPending()).isTrue();
    }

    @Test
    @DisplayName("토큰 및 실패 이유 존재 여부 검사 메서드 확인")
    void shouldHaveCorrectHasTokenAndHasFailureReasonMethods() {
        // Given
        AuthenticationResponse withToken = AuthenticationResponse.success(
            "auth-token", "user", "token", "refresh", 3600L);
        AuthenticationResponse withoutToken = AuthenticationResponse.pending("auth-no-token", "user");
        AuthenticationResponse withReason = AuthenticationResponse.failed("auth-reason", "user", "reason");
        AuthenticationResponse withoutReason = AuthenticationResponse.pending("auth-no-reason", "user");

        // When & Then
        assertThat(withToken.hasToken()).isTrue();
        assertThat(withoutToken.hasToken()).isFalse();
        assertThat(withReason.hasFailureReason()).isTrue();
        assertThat(withoutReason.hasFailureReason()).isFalse();
    }
}