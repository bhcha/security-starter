package com.dx.hexacore.security.auth.application.query.projection;

import com.dx.hexacore.security.auth.application.projection.AuthenticationProjection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AuthenticationProjection 테스트")
class AuthenticationProjectionTest {

    @Test
    @DisplayName("모든 필드가 올바르게 설정된 Projection 생성")
    void shouldCreateProjectionWithAllFields() {
        // Given
        String id = "auth-123";
        String username = "testuser";
        String status = "SUCCESS";
        LocalDateTime attemptTime = LocalDateTime.now().minusMinutes(10);
        LocalDateTime successTime = LocalDateTime.now().minusMinutes(5);
        String accessToken = "access-token-123";
        String refreshToken = "refresh-token-123";
        Long tokenExpiresIn = 3600L;

        // When
        AuthenticationProjection projection = AuthenticationProjection.builder()
            .id(id)
            .username(username)
            .status(status)
            .attemptTime(attemptTime)
            .successTime(successTime)
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenExpiresIn(tokenExpiresIn)
            .build();

        // Then
        assertThat(projection).isNotNull();
        assertThat(projection.getId()).isEqualTo(id);
        assertThat(projection.getUsername()).isEqualTo(username);
        assertThat(projection.getStatus()).isEqualTo(status);
        assertThat(projection.getAttemptTime()).isEqualTo(attemptTime);
        assertThat(projection.getSuccessTime()).isEqualTo(successTime);
        assertThat(projection.getAccessToken()).isEqualTo(accessToken);
        assertThat(projection.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(projection.getTokenExpiresIn()).isEqualTo(tokenExpiresIn);
    }

    @Test
    @DisplayName("Builder 패턴을 통한 Projection 생성 성공")
    void shouldCreateProjectionUsingBuilder() {
        // Given & When
        AuthenticationProjection projection = AuthenticationProjection.builder()
            .id("auth-456")
            .username("builderuser")
            .status("PENDING")
            .attemptTime(LocalDateTime.now())
            .build();

        // Then
        assertThat(projection).isNotNull();
        assertThat(projection.getId()).isEqualTo("auth-456");
        assertThat(projection.getUsername()).isEqualTo("builderuser");
        assertThat(projection.getStatus()).isEqualTo("PENDING");
        assertThat(projection.getAttemptTime()).isNotNull();
    }

    @Test
    @DisplayName("null 필드 처리 확인")
    void shouldHandleNullFields() {
        // Given & When
        AuthenticationProjection projection = AuthenticationProjection.builder()
            .id("auth-789")
            .username("nulltestuser")
            .status("FAILED")
            .attemptTime(LocalDateTime.now())
            .successTime(null)  // null 필드
            .failureTime(LocalDateTime.now())
            .failureReason("Test failure")
            .accessToken(null)  // null 필드
            .refreshToken(null) // null 필드
            .tokenExpiresIn(null) // null 필드
            .build();

        // Then
        assertThat(projection).isNotNull();
        assertThat(projection.getId()).isEqualTo("auth-789");
        assertThat(projection.getSuccessTime()).isNull();
        assertThat(projection.getAccessToken()).isNull();
        assertThat(projection.getRefreshToken()).isNull();
        assertThat(projection.getTokenExpiresIn()).isNull();
        assertThat(projection.getFailureReason()).isEqualTo("Test failure");
    }

    @Test
    @DisplayName("날짜 필드 올바른 형식으로 설정 확인")
    void shouldSetDateFieldsCorrectly() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime attemptTime = now.minusMinutes(15);
        LocalDateTime successTime = now.minusMinutes(10);
        LocalDateTime failureTime = now.minusMinutes(5);

        // When
        AuthenticationProjection projection = AuthenticationProjection.builder()
            .id("auth-date-test")
            .username("dateuser")
            .status("SUCCESS")
            .attemptTime(attemptTime)
            .successTime(successTime)
            .failureTime(failureTime)
            .build();

        // Then
        assertThat(projection.getAttemptTime()).isEqualTo(attemptTime);
        assertThat(projection.getSuccessTime()).isEqualTo(successTime);
        assertThat(projection.getFailureTime()).isEqualTo(failureTime);
        
        // 시간 순서 검증
        assertThat(projection.getAttemptTime()).isBefore(projection.getSuccessTime());
        assertThat(projection.getSuccessTime()).isBefore(projection.getFailureTime());
    }

    @Test
    @DisplayName("PENDING 상태 Projection 생성 확인")
    void shouldCreatePendingProjection() {
        // Given & When
        AuthenticationProjection projection = AuthenticationProjection.builder()
            .id("auth-pending")
            .username("pendinguser")
            .status("PENDING")
            .attemptTime(LocalDateTime.now())
            .build();

        // Then
        assertThat(projection.getStatus()).isEqualTo("PENDING");
        assertThat(projection.getSuccessTime()).isNull();
        assertThat(projection.getFailureTime()).isNull();
        assertThat(projection.getFailureReason()).isNull();
        assertThat(projection.getAccessToken()).isNull();
        assertThat(projection.getRefreshToken()).isNull();
    }

    @Test
    @DisplayName("SUCCESS 상태 Projection 생성 확인")
    void shouldCreateSuccessProjection() {
        // Given & When
        AuthenticationProjection projection = AuthenticationProjection.builder()
            .id("auth-success")
            .username("successuser")
            .status("SUCCESS")
            .attemptTime(LocalDateTime.now().minusMinutes(10))
            .successTime(LocalDateTime.now().minusMinutes(5))
            .accessToken("success-access-token")
            .refreshToken("success-refresh-token")
            .tokenExpiresIn(3600L)
            .build();

        // Then
        assertThat(projection.getStatus()).isEqualTo("SUCCESS");
        assertThat(projection.getSuccessTime()).isNotNull();
        assertThat(projection.getAccessToken()).isNotNull();
        assertThat(projection.getRefreshToken()).isNotNull();
        assertThat(projection.getTokenExpiresIn()).isEqualTo(3600L);
        assertThat(projection.getFailureTime()).isNull();
        assertThat(projection.getFailureReason()).isNull();
    }

    @Test
    @DisplayName("FAILED 상태 Projection 생성 확인")
    void shouldCreateFailedProjection() {
        // Given & When
        AuthenticationProjection projection = AuthenticationProjection.builder()
            .id("auth-failed")
            .username("faileduser")
            .status("FAILED")
            .attemptTime(LocalDateTime.now().minusMinutes(10))
            .failureTime(LocalDateTime.now().minusMinutes(5))
            .failureReason("Invalid credentials")
            .build();

        // Then
        assertThat(projection.getStatus()).isEqualTo("FAILED");
        assertThat(projection.getFailureTime()).isNotNull();
        assertThat(projection.getFailureReason()).isEqualTo("Invalid credentials");
        assertThat(projection.getSuccessTime()).isNull();
        assertThat(projection.getAccessToken()).isNull();
        assertThat(projection.getRefreshToken()).isNull();
    }
}