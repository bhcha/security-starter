package com.ldx.hexacore.security.session.application.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UnlockNotAllowedException 테스트")
class UnlockNotAllowedExceptionTest {

    @Test
    @DisplayName("해제 불가 이유와 함께 예외를 생성할 수 있다")
    void shouldCreateExceptionWithUnlockDenialReason() {
        // Given
        String reason = "Account unlock not allowed: insufficient permissions";

        // When
        UnlockNotAllowedException exception = new UnlockNotAllowedException(reason);

        // Then
        assertThat(exception.getMessage()).isEqualTo(reason);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("메시지와 원인과 함께 예외를 생성할 수 있다")
    void shouldCreateExceptionWithMessageAndCause() {
        // Given
        String message = "Unlock operation denied";
        Throwable cause = new SecurityException("Access denied by security policy");

        // When
        UnlockNotAllowedException exception = new UnlockNotAllowedException(message, cause);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("원인만으로 예외를 생성할 수 있다")
    void shouldCreateExceptionWithCauseOnly() {
        // Given
        Throwable cause = new SecurityException("Security policy violation");

        // When
        UnlockNotAllowedException exception = new UnlockNotAllowedException(cause);

        // Then
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getMessage()).isEqualTo(cause.toString());
    }

    @Test
    @DisplayName("기본 생성자로 예외를 생성할 수 있다")
    void shouldCreateExceptionWithDefaultConstructor() {
        // When
        UnlockNotAllowedException exception = new UnlockNotAllowedException();

        // Then
        assertThat(exception.getMessage()).isNull();
        assertThat(exception.getCause()).isNull();
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("사용자 ID와 함께 예외를 생성할 수 있다")
    void shouldCreateExceptionWithUserId() {
        // Given
        String userId = "testUser123";
        String message = "Unlock not allowed for user: " + userId;

        // When
        UnlockNotAllowedException exception = new UnlockNotAllowedException(message);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getMessage()).contains(userId);
    }

    @Test
    @DisplayName("시간 기반 제한 이유로 예외를 생성할 수 있다")
    void shouldCreateExceptionWithTimeBasedRestriction() {
        // Given
        String message = "Unlock not allowed: minimum lock duration not elapsed (remaining: 25 minutes)";

        // When
        UnlockNotAllowedException exception = new UnlockNotAllowedException(message);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getMessage()).contains("25 minutes");
    }

    @Test
    @DisplayName("권한 부족 이유로 예외를 생성할 수 있다")
    void shouldCreateExceptionWithInsufficientPermissions() {
        // Given
        String message = "Unlock not allowed: user lacks ADMIN_UNLOCK permission";

        // When
        UnlockNotAllowedException exception = new UnlockNotAllowedException(message);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getMessage()).contains("ADMIN_UNLOCK");
    }

    @Test
    @DisplayName("정책 위반 이유로 예외를 생성할 수 있다")
    void shouldCreateExceptionWithPolicyViolation() {
        // Given
        String message = "Unlock not allowed: violates security policy LOCK_001 (suspicious activity detected)";

        // When
        UnlockNotAllowedException exception = new UnlockNotAllowedException(message);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getMessage()).contains("LOCK_001");
        assertThat(exception.getMessage()).contains("suspicious activity");
    }

    @Test
    @DisplayName("null 메시지로 예외를 생성할 수 있다")
    void shouldCreateExceptionWithNullMessage() {
        // Given
        String message = null;

        // When
        UnlockNotAllowedException exception = new UnlockNotAllowedException(message);

        // Then
        assertThat(exception.getMessage()).isNull();
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("빈 메시지로 예외를 생성할 수 있다")
    void shouldCreateExceptionWithEmptyMessage() {
        // Given
        String message = "";

        // When
        UnlockNotAllowedException exception = new UnlockNotAllowedException(message);

        // Then
        assertThat(exception.getMessage()).isEqualTo("");
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("null 원인과 함께 예외를 생성할 수 있다")
    void shouldCreateExceptionWithNullCause() {
        // Given
        String message = "Unlock denied";
        Throwable cause = null;

        // When
        UnlockNotAllowedException exception = new UnlockNotAllowedException(message, cause);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("예외 체인이 올바르게 동작한다")
    void shouldMaintainExceptionChain() {
        // Given
        RuntimeException rootCause = new RuntimeException("Root access control error");
        SecurityException intermediateCause = new SecurityException("Security check failed", rootCause);
        String message = "Unlock operation not allowed";

        // When
        UnlockNotAllowedException exception = new UnlockNotAllowedException(message, intermediateCause);

        // Then
        assertThat(exception.getCause()).isEqualTo(intermediateCause);
        assertThat(exception.getCause().getCause()).isEqualTo(rootCause);
    }

    @Test
    @DisplayName("스택 트레이스가 정상적으로 생성된다")
    void shouldHaveProperStackTrace() {
        // Given
        String message = "Unlock not allowed";

        // When
        UnlockNotAllowedException exception = new UnlockNotAllowedException(message);

        // Then
        assertThat(exception.getStackTrace()).isNotNull();
        assertThat(exception.getStackTrace()).isNotEmpty();
        assertThat(exception.getStackTrace()[0].getClassName())
                .contains("UnlockNotAllowedExceptionTest");
    }

    @Test
    @DisplayName("예외 정보가 toString에 포함된다")
    void shouldIncludeExceptionInfoInToString() {
        // Given
        String message = "Account unlock denied due to security policy";

        // When
        UnlockNotAllowedException exception = new UnlockNotAllowedException(message);

        // Then
        String toString = exception.toString();
        assertThat(toString).contains("UnlockNotAllowedException");
        assertThat(toString).contains(message);
    }

    @Test
    @DisplayName("복합적인 해제 불가 이유로 예외를 생성할 수 있다")
    void shouldCreateExceptionWithComplexDenialReason() {
        // Given
        String message = "Unlock not allowed: [1] Insufficient permissions (requires ADMIN role), " +
                "[2] Time restriction (minimum 30 minutes not elapsed), " +
                "[3] Policy violation (max 3 unlocks per day exceeded)";

        // When
        UnlockNotAllowedException exception = new UnlockNotAllowedException(message);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getMessage()).contains("Insufficient permissions");
        assertThat(exception.getMessage()).contains("Time restriction");
        assertThat(exception.getMessage()).contains("Policy violation");
    }

    @Test
    @DisplayName("특수 문자가 포함된 메시지로 예외를 생성할 수 있다")
    void shouldCreateExceptionWithSpecialCharacterMessage() {
        // Given
        String message = "Unlock denied: user@#$%^&*()_+-={}[]|\\:;\"'<>?,./";

        // When
        UnlockNotAllowedException exception = new UnlockNotAllowedException(message);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("JSON 형태의 해제 불가 정보로 예외를 생성할 수 있다")
    void shouldCreateExceptionWithJsonDenialInfo() {
        // Given
        String message = "Unlock denied: {\"reason\":\"policy_violation\",\"policy\":\"MAX_UNLOCK_ATTEMPTS\",\"current\":5,\"limit\":3}";

        // When
        UnlockNotAllowedException exception = new UnlockNotAllowedException(message);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getMessage()).contains("policy_violation");
        assertThat(exception.getMessage()).contains("MAX_UNLOCK_ATTEMPTS");
        assertThat(exception.getMessage()).contains("\"current\":5");
        assertThat(exception.getMessage()).contains("\"limit\":3");
    }

    @Test
    @DisplayName("중첩된 해제 불가 예외를 원인으로 하는 예외를 생성할 수 있다")
    void shouldCreateExceptionWithNestedUnlockException() {
        // Given
        UnlockNotAllowedException nestedCause = new UnlockNotAllowedException("Nested unlock denial");
        String message = "Parent unlock operation failed";

        // When
        UnlockNotAllowedException exception = new UnlockNotAllowedException(message, nestedCause);

        // Then
        assertThat(exception.getCause()).isEqualTo(nestedCause);
        assertThat(exception.getCause()).isInstanceOf(UnlockNotAllowedException.class);
        assertThat(exception.getCause().getMessage()).isEqualTo("Nested unlock denial");
    }
}