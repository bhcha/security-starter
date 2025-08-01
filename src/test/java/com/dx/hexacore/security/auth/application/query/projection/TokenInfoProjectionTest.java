package com.dx.hexacore.security.auth.application.query.projection;

import com.dx.hexacore.security.auth.application.projection.TokenInfoProjection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TokenInfoProjection 테스트")
class TokenInfoProjectionTest {

    @Test
    @DisplayName("토큰 유효성 상태 올바르게 설정 확인")
    void shouldSetTokenValidityCorrectly() {
        // Given
        String token = "valid-token";
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);

        // When
        TokenInfoProjection projection = TokenInfoProjection.builder()
            .token(token)
            .isValid(true)
            .expiresAt(expiresAt)
            .issuedAt(LocalDateTime.now())
            .build();

        // Then
        assertThat(projection).isNotNull();
        assertThat(projection.getToken()).isEqualTo(token);
        assertThat(projection.isValid()).isTrue();
        assertThat(projection.getExpiresAt()).isEqualTo(expiresAt);
    }

    @Test
    @DisplayName("만료 시간 올바르게 설정 확인")
    void shouldSetExpirationTimeCorrectly() {
        // Given
        String token = "expiring-token";
        LocalDateTime issuedAt = LocalDateTime.now().minusHours(1);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);

        // When
        TokenInfoProjection projection = TokenInfoProjection.builder()
            .token(token)
            .isValid(true)
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .build();

        // Then
        assertThat(projection.getIssuedAt()).isEqualTo(issuedAt);
        assertThat(projection.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(projection.getExpiresAt()).isAfter(projection.getIssuedAt());
    }

    @Test
    @DisplayName("갱신 가능 여부 올바르게 계산 확인")
    void shouldCalculateRefreshabilityCorrectly() {
        // Given - 갱신 가능한 토큰
        TokenInfoProjection refreshableToken = TokenInfoProjection.builder()
            .token("refreshable-token")
            .isValid(true)
            .issuedAt(LocalDateTime.now().minusMinutes(30))
            .expiresAt(LocalDateTime.now().plusMinutes(30))
            .canRefresh(true)
            .build();

        // Given - 갱신 불가능한 토큰
        TokenInfoProjection nonRefreshableToken = TokenInfoProjection.builder()
            .token("non-refreshable-token")
            .isValid(false)
            .issuedAt(LocalDateTime.now().minusHours(2))
            .expiresAt(LocalDateTime.now().minusHours(1))
            .canRefresh(false)
            .build();

        // Then
        assertThat(refreshableToken.canRefresh()).isTrue();
        assertThat(nonRefreshableToken.canRefresh()).isFalse();
    }

    @Test
    @DisplayName("null 토큰 처리 확인")
    void shouldHandleNullToken() {
        // Given & When
        TokenInfoProjection projection = TokenInfoProjection.builder()
            .token(null)
            .isValid(false)
            .issuedAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now())
            .build();

        // Then
        assertThat(projection).isNotNull();
        assertThat(projection.getToken()).isNull();
        assertThat(projection.isValid()).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰 Projection 생성 확인")
    void shouldCreateExpiredTokenProjection() {
        // Given
        String expiredToken = "expired-token";
        LocalDateTime issuedAt = LocalDateTime.now().minusHours(2);
        LocalDateTime expiresAt = LocalDateTime.now().minusHours(1);

        // When
        TokenInfoProjection projection = TokenInfoProjection.builder()
            .token(expiredToken)
            .isValid(false)
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .canRefresh(false)
            .authenticationId("auth-expired")
            .build();

        // Then
        assertThat(projection.getToken()).isEqualTo(expiredToken);
        assertThat(projection.isValid()).isFalse();
        assertThat(projection.canRefresh()).isFalse();
        assertThat(projection.getExpiresAt()).isBefore(LocalDateTime.now());
        assertThat(projection.getAuthenticationId()).isEqualTo("auth-expired");
    }

    @Test
    @DisplayName("유효한 토큰 Projection 생성 확인")
    void shouldCreateValidTokenProjection() {
        // Given
        String validToken = "valid-active-token";
        LocalDateTime issuedAt = LocalDateTime.now().minusMinutes(30);
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);

        // When
        TokenInfoProjection projection = TokenInfoProjection.builder()
            .token(validToken)
            .isValid(true)
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .canRefresh(true)
            .authenticationId("auth-valid")
            .build();

        // Then
        assertThat(projection.getToken()).isEqualTo(validToken);
        assertThat(projection.isValid()).isTrue();
        assertThat(projection.canRefresh()).isTrue();
        assertThat(projection.getExpiresAt()).isAfter(LocalDateTime.now());
        assertThat(projection.getAuthenticationId()).isEqualTo("auth-valid");
    }

    @Test
    @DisplayName("토큰 메타데이터 포함 Projection 생성 확인")
    void shouldCreateProjectionWithMetadata() {
        // Given
        String token = "token-with-metadata";
        String tokenType = "Bearer";
        String scope = "read write";

        // When
        TokenInfoProjection projection = TokenInfoProjection.builder()
            .token(token)
            .isValid(true)
            .issuedAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusHours(1))
            .tokenType(tokenType)
            .scope(scope)
            .authenticationId("auth-metadata")
            .build();

        // Then
        assertThat(projection.getToken()).isEqualTo(token);
        assertThat(projection.getTokenType()).isEqualTo(tokenType);
        assertThat(projection.getScope()).isEqualTo(scope);
        assertThat(projection.getAuthenticationId()).isEqualTo("auth-metadata");
    }

    @Test
    @DisplayName("Builder 패턴 필수 필드 검증")
    void shouldValidateRequiredFieldsInBuilder() {
        // Given & When & Then - 최소 필수 필드로 생성 가능
        assertThatCode(() -> 
            TokenInfoProjection.builder()
                .token("minimal-token")
                .isValid(true)
                .build()
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("모든 필드가 설정된 완전한 Projection 생성")
    void shouldCreateCompleteProjection() {
        // Given
        String token = "complete-token";
        boolean isValid = true;
        LocalDateTime issuedAt = LocalDateTime.now().minusMinutes(30);
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);
        boolean canRefresh = true;
        String tokenType = "Bearer";
        String scope = "read write delete";
        String authenticationId = "auth-complete";

        // When
        TokenInfoProjection projection = TokenInfoProjection.builder()
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
        assertThat(projection.getToken()).isEqualTo(token);
        assertThat(projection.isValid()).isEqualTo(isValid);
        assertThat(projection.getIssuedAt()).isEqualTo(issuedAt);
        assertThat(projection.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(projection.canRefresh()).isEqualTo(canRefresh);
        assertThat(projection.getTokenType()).isEqualTo(tokenType);
        assertThat(projection.getScope()).isEqualTo(scope);
        assertThat(projection.getAuthenticationId()).isEqualTo(authenticationId);
    }
}