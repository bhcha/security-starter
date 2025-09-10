package com.ldx.hexacore.security.auth.application.query.port.in;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TokenInfoResponse 테스트")
class TokenInfoResponseTest {

    @Test
    @DisplayName("유효한 토큰 응답 생성 확인")
    void shouldCreateValidTokenResponse() {
        // Given
        String token = "valid-token-123";
        boolean isValid = true;
        LocalDateTime issuedAt = LocalDateTime.now().minusMinutes(30);
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);
        String authenticationId = "auth-123";

        // When
        TokenInfoResponse response = TokenInfoResponse.builder()
            .token(token)
            .isValid(isValid)
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .authenticationId(authenticationId)
            .build();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo(token);
        assertThat(response.isValid()).isTrue();
        assertThat(response.getIssuedAt()).isEqualTo(issuedAt);
        assertThat(response.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(response.getAuthenticationId()).isEqualTo(authenticationId);
    }

    @Test
    @DisplayName("만료된 토큰 응답 생성 확인")
    void shouldCreateExpiredTokenResponse() {
        // Given
        String expiredToken = "expired-token-456";
        boolean isValid = false;
        LocalDateTime issuedAt = LocalDateTime.now().minusHours(2);
        LocalDateTime expiresAt = LocalDateTime.now().minusHours(1);

        // When
        TokenInfoResponse response = TokenInfoResponse.builder()
            .token(expiredToken)
            .isValid(isValid)
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .build();

        // Then
        assertThat(response.getToken()).isEqualTo(expiredToken);
        assertThat(response.isValid()).isFalse();
        assertThat(response.getExpiresAt()).isBefore(LocalDateTime.now());
        assertThat(response.isExpired()).isTrue();
    }

    @Test
    @DisplayName("토큰 메타데이터 포함 응답 생성 확인")
    void shouldCreateResponseWithMetadata() {
        // Given
        String token = "metadata-token";
        String tokenType = "Bearer";
        String scope = "read write delete";
        boolean canRefresh = true;

        // When
        TokenInfoResponse response = TokenInfoResponse.builder()
            .token(token)
            .isValid(true)
            .tokenType(tokenType)
            .scope(scope)
            .canRefresh(canRefresh)
            .issuedAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusHours(1))
            .build();

        // Then
        assertThat(response.getToken()).isEqualTo(token);
        assertThat(response.getTokenType()).isEqualTo(tokenType);
        assertThat(response.getScope()).isEqualTo(scope);
        assertThat(response.canRefresh()).isTrue();
    }

    @Test
    @DisplayName("갱신 가능 토큰 응답 생성 확인")
    void shouldCreateRefreshableTokenResponse() {
        // Given
        String refreshableToken = "refreshable-token";
        boolean canRefresh = true;

        // When
        TokenInfoResponse response = TokenInfoResponse.builder()
            .token(refreshableToken)
            .isValid(true)
            .canRefresh(canRefresh)
            .issuedAt(LocalDateTime.now().minusMinutes(30))
            .expiresAt(LocalDateTime.now().plusMinutes(30))
            .build();

        // Then
        assertThat(response.getToken()).isEqualTo(refreshableToken);
        assertThat(response.canRefresh()).isTrue();
        assertThat(response.isRefreshable()).isTrue();
    }

    @Test
    @DisplayName("정적 팩토리 메서드 - valid() 테스트")
    void shouldCreateValidResponseUsingStaticFactory() {
        // Given
        String token = "static-valid-token";
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(2);
        String authenticationId = "auth-static";

        // When
        TokenInfoResponse response = TokenInfoResponse.valid(
            token, expiresAt, authenticationId
        );

        // Then
        assertThat(response.getToken()).isEqualTo(token);
        assertThat(response.isValid()).isTrue();
        assertThat(response.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(response.getAuthenticationId()).isEqualTo(authenticationId);
        assertThat(response.getIssuedAt()).isNotNull();
    }

    @Test
    @DisplayName("정적 팩토리 메서드 - expired() 테스트")
    void shouldCreateExpiredResponseUsingStaticFactory() {
        // Given
        String expiredToken = "static-expired-token";
        LocalDateTime expiredAt = LocalDateTime.now().minusHours(1);

        // When
        TokenInfoResponse response = TokenInfoResponse.expired(expiredToken, expiredAt);

        // Then
        assertThat(response.getToken()).isEqualTo(expiredToken);
        assertThat(response.isValid()).isFalse();
        assertThat(response.getExpiresAt()).isEqualTo(expiredAt);
        assertThat(response.isExpired()).isTrue();
    }

    @Test
    @DisplayName("정적 팩토리 메서드 - invalid() 테스트")
    void shouldCreateInvalidResponseUsingStaticFactory() {
        // Given
        String invalidToken = "static-invalid-token";
        String reason = "Token signature verification failed";

        // When
        TokenInfoResponse response = TokenInfoResponse.invalid(invalidToken, reason);

        // Then
        assertThat(response.getToken()).isEqualTo(invalidToken);
        assertThat(response.isValid()).isFalse();
        assertThat(response.getInvalidReason()).isEqualTo(reason);
        assertThat(response.hasInvalidReason()).isTrue();
    }

    @Test
    @DisplayName("유효성 검사 메서드들 올바른 동작 확인")
    void shouldHaveCorrectValidityMethods() {
        // Given
        TokenInfoResponse validToken = TokenInfoResponse.valid(
            "valid", LocalDateTime.now().plusHours(1), "auth-1");
        TokenInfoResponse expiredToken = TokenInfoResponse.expired(
            "expired", LocalDateTime.now().minusHours(1));
        TokenInfoResponse invalidToken = TokenInfoResponse.invalid(
            "invalid", "Invalid signature");

        // When & Then
        assertThat(validToken.isValid()).isTrue();
        assertThat(validToken.isExpired()).isFalse();
        assertThat(validToken.isActive()).isTrue();

        assertThat(expiredToken.isValid()).isFalse();
        assertThat(expiredToken.isExpired()).isTrue();
        assertThat(expiredToken.isActive()).isFalse();

        assertThat(invalidToken.isValid()).isFalse();
        assertThat(invalidToken.isActive()).isFalse();
    }

    @Test
    @DisplayName("갱신 가능 여부 검사 메서드 확인")
    void shouldHaveCorrectRefreshabilityMethods() {
        // Given
        TokenInfoResponse refreshableToken = TokenInfoResponse.builder()
            .token("refreshable")
            .isValid(true)
            .canRefresh(true)
            .issuedAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusHours(1))
            .build();

        TokenInfoResponse nonRefreshableToken = TokenInfoResponse.builder()
            .token("non-refreshable")
            .isValid(false)
            .canRefresh(false)
            .issuedAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().minusHours(1))
            .build();

        // When & Then
        assertThat(refreshableToken.canRefresh()).isTrue();
        assertThat(refreshableToken.isRefreshable()).isTrue();

        assertThat(nonRefreshableToken.canRefresh()).isFalse();
        assertThat(nonRefreshableToken.isRefreshable()).isFalse();
    }

    @Test
    @DisplayName("토큰 만료까지 남은 시간 계산 확인")
    void shouldCalculateTimeUntilExpiration() {
        // Given
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        TokenInfoResponse response = TokenInfoResponse.valid("token", expiresAt, "auth");

        // When
        long minutesUntilExpiration = response.getMinutesUntilExpiration();

        // Then
        assertThat(minutesUntilExpiration).isBetween(29L, 31L); // 약간의 여유를 둠
    }

    @Test
    @DisplayName("Builder 패턴 필수 필드 검증")
    void shouldValidateRequiredFieldsInBuilder() {
        // Given & When & Then - 최소 필수 필드로 생성 가능
        assertThatCode(() -> 
            TokenInfoResponse.builder()
                .token("minimal-token")
                .isValid(true)
                .build()
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("완전한 토큰 정보 응답 생성")
    void shouldCreateCompleteTokenInfoResponse() {
        // Given
        String token = "complete-token-info";
        boolean isValid = true;
        LocalDateTime issuedAt = LocalDateTime.now().minusMinutes(30);
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);
        boolean canRefresh = true;
        String tokenType = "Bearer";
        String scope = "read write admin";
        String authenticationId = "auth-complete";

        // When
        TokenInfoResponse response = TokenInfoResponse.builder()
            .token(token)
            .isValid(isValid)
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .canRefresh(canRefresh)
            .tokenType(tokenType)
            .scope(scope)
            .authenticationId(authenticationId)
            .build();

        // Then
        assertThat(response.getToken()).isEqualTo(token);
        assertThat(response.isValid()).isEqualTo(isValid);
        assertThat(response.getIssuedAt()).isEqualTo(issuedAt);
        assertThat(response.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(response.canRefresh()).isEqualTo(canRefresh);
        assertThat(response.getTokenType()).isEqualTo(tokenType);
        assertThat(response.getScope()).isEqualTo(scope);
        assertThat(response.getAuthenticationId()).isEqualTo(authenticationId);
        assertThat(response.isActive()).isTrue();
        assertThat(response.isRefreshable()).isTrue();
    }
}