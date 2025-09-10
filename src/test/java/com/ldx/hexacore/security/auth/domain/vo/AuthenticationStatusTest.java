package com.ldx.hexacore.security.auth.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * AuthenticationStatus Value Object 테스트.
 */
@DisplayName("AuthenticationStatus 값 객체")
class AuthenticationStatusTest {

    @Test
    @DisplayName("PENDING 상태로 AuthenticationStatus 생성에 성공한다")
    void shouldCreateAuthenticationStatusWhenPendingStatus() {
        // When
        AuthenticationStatus status = AuthenticationStatus.pending();

        // Then
        assertThat(status).isNotNull();
        assertThat(status.isPending()).isTrue();
        assertThat(status.isSuccess()).isFalse();
        assertThat(status.isFailed()).isFalse();
    }

    @Test
    @DisplayName("SUCCESS 상태로 AuthenticationStatus 생성에 성공한다")
    void shouldCreateAuthenticationStatusWhenSuccessStatus() {
        // When
        AuthenticationStatus status = AuthenticationStatus.success();

        // Then
        assertThat(status).isNotNull();
        assertThat(status.isPending()).isFalse();
        assertThat(status.isSuccess()).isTrue();
        assertThat(status.isFailed()).isFalse();
    }

    @Test
    @DisplayName("FAILED 상태로 AuthenticationStatus 생성에 성공한다")
    void shouldCreateAuthenticationStatusWhenFailedStatus() {
        // When
        AuthenticationStatus status = AuthenticationStatus.failed();

        // Then
        assertThat(status).isNotNull();
        assertThat(status.isPending()).isFalse();
        assertThat(status.isSuccess()).isFalse();
        assertThat(status.isFailed()).isTrue();
    }

    @Test
    @DisplayName("동일한 상태값으로 생성된 AuthenticationStatus는 equals true를 반환한다")
    void shouldReturnTrueWhenSameStatus() {
        // Given
        AuthenticationStatus status1 = AuthenticationStatus.pending();
        AuthenticationStatus status2 = AuthenticationStatus.pending();

        // When & Then
        assertThat(status1).isEqualTo(status2);
    }

    @Test
    @DisplayName("다른 상태값으로 생성된 AuthenticationStatus는 equals false를 반환한다")
    void shouldReturnFalseWhenDifferentStatus() {
        // Given
        AuthenticationStatus pendingStatus = AuthenticationStatus.pending();
        AuthenticationStatus successStatus = AuthenticationStatus.success();

        // When & Then
        assertThat(pendingStatus).isNotEqualTo(successStatus);
    }

    @Test
    @DisplayName("동일한 상태값으로 생성된 AuthenticationStatus는 동일한 hashCode를 가진다")
    void shouldHaveSameHashCodeWhenSameStatus() {
        // Given
        AuthenticationStatus status1 = AuthenticationStatus.pending();
        AuthenticationStatus status2 = AuthenticationStatus.pending();

        // When & Then
        assertThat(status1.hashCode()).isEqualTo(status2.hashCode());
    }

    @Test
    @DisplayName("isPending() 메서드가 PENDING 상태에서만 true를 반환한다")
    void shouldReturnTrueOnlyWhenPending() {
        // Given
        AuthenticationStatus pendingStatus = AuthenticationStatus.pending();
        AuthenticationStatus successStatus = AuthenticationStatus.success();
        AuthenticationStatus failedStatus = AuthenticationStatus.failed();

        // When & Then
        assertThat(pendingStatus.isPending()).isTrue();
        assertThat(successStatus.isPending()).isFalse();
        assertThat(failedStatus.isPending()).isFalse();
    }

    @Test
    @DisplayName("isSuccess() 메서드가 SUCCESS 상태에서만 true를 반환한다")
    void shouldReturnTrueOnlyWhenSuccess() {
        // Given
        AuthenticationStatus pendingStatus = AuthenticationStatus.pending();
        AuthenticationStatus successStatus = AuthenticationStatus.success();
        AuthenticationStatus failedStatus = AuthenticationStatus.failed();

        // When & Then
        assertThat(pendingStatus.isSuccess()).isFalse();
        assertThat(successStatus.isSuccess()).isTrue();
        assertThat(failedStatus.isSuccess()).isFalse();
    }

    @Test
    @DisplayName("isFailed() 메서드가 FAILED 상태에서만 true를 반환한다")
    void shouldReturnTrueOnlyWhenFailed() {
        // Given
        AuthenticationStatus pendingStatus = AuthenticationStatus.pending();
        AuthenticationStatus successStatus = AuthenticationStatus.success();
        AuthenticationStatus failedStatus = AuthenticationStatus.failed();

        // When & Then
        assertThat(pendingStatus.isFailed()).isFalse();
        assertThat(successStatus.isFailed()).isFalse();
        assertThat(failedStatus.isFailed()).isTrue();
    }

    @Test
    @DisplayName("상태값을 문자열로 변환할 수 있다")
    void shouldConvertStatusToString() {
        // Given
        AuthenticationStatus pendingStatus = AuthenticationStatus.pending();
        AuthenticationStatus successStatus = AuthenticationStatus.success();
        AuthenticationStatus failedStatus = AuthenticationStatus.failed();

        // When & Then
        assertThat(pendingStatus.toString()).isEqualTo("PENDING");
        assertThat(successStatus.toString()).isEqualTo("SUCCESS");
        assertThat(failedStatus.toString()).isEqualTo("FAILED");
    }

    @Test
    @DisplayName("문자열로부터 AuthenticationStatus를 생성할 수 있다")
    void shouldCreateFromString() {
        // When
        AuthenticationStatus pendingStatus = AuthenticationStatus.of("PENDING");
        AuthenticationStatus successStatus = AuthenticationStatus.of("SUCCESS");
        AuthenticationStatus failedStatus = AuthenticationStatus.of("FAILED");

        // Then
        assertThat(pendingStatus.isPending()).isTrue();
        assertThat(successStatus.isSuccess()).isTrue();
        assertThat(failedStatus.isFailed()).isTrue();
    }

    @Test
    @DisplayName("null 상태로 생성 시 IllegalArgumentException이 발생한다")
    void shouldThrowExceptionWhenStatusIsNull() {
        // When & Then
        assertThatThrownBy(() -> AuthenticationStatus.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Status cannot be empty");
    }

    @Test
    @DisplayName("잘못된 상태값으로 생성 시 IllegalArgumentException이 발생한다")
    void shouldThrowExceptionWhenInvalidStatus() {
        // When & Then
        assertThatThrownBy(() -> AuthenticationStatus.of("INVALID"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid authentication status");
    }
}