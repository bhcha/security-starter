package com.dx.hexacore.security.session.application.exception;

import com.dx.hexacore.security.session.application.exception.SessionValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SessionValidationException 테스트")
class SessionValidationExceptionTest {

    @Test
    @DisplayName("검증 실패 정보와 함께 예외를 생성할 수 있다")
    void shouldCreateExceptionWithValidationFailureInfo() {
        // Given
        String message = "Session validation failed: invalid state";

        // When
        SessionValidationException exception = new SessionValidationException(message);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("메시지와 원인과 함께 예외를 생성할 수 있다")
    void shouldCreateExceptionWithMessageAndCause() {
        // Given
        String message = "Session validation failed";
        Throwable cause = new IllegalArgumentException("Invalid session data");

        // When
        SessionValidationException exception = new SessionValidationException(message, cause);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("원인만으로 예외를 생성할 수 있다")
    void shouldCreateExceptionWithCauseOnly() {
        // Given
        Throwable cause = new IllegalArgumentException("Invalid session data");

        // When
        SessionValidationException exception = new SessionValidationException(cause);

        // Then
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getMessage()).isEqualTo(cause.toString());
    }

    @Test
    @DisplayName("기본 생성자로 예외를 생성할 수 있다")
    void shouldCreateExceptionWithDefaultConstructor() {
        // When
        SessionValidationException exception = new SessionValidationException();

        // Then
        assertThat(exception.getMessage()).isNull();
        assertThat(exception.getCause()).isNull();
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("null 메시지로 예외를 생성할 수 있다")
    void shouldCreateExceptionWithNullMessage() {
        // Given
        String message = null;

        // When
        SessionValidationException exception = new SessionValidationException(message);

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
        SessionValidationException exception = new SessionValidationException(message);

        // Then
        assertThat(exception.getMessage()).isEqualTo("");
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("null 원인과 함께 예외를 생성할 수 있다")
    void shouldCreateExceptionWithNullCause() {
        // Given
        String message = "Validation failed";
        Throwable cause = null;

        // When
        SessionValidationException exception = new SessionValidationException(message, cause);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("예외 체인이 올바르게 동작한다")
    void shouldMaintainExceptionChain() {
        // Given
        RuntimeException rootCause = new RuntimeException("Root validation error");
        IllegalArgumentException intermediateCause = new IllegalArgumentException("Field validation error", rootCause);
        String message = "Session validation failed";

        // When
        SessionValidationException exception = new SessionValidationException(message, intermediateCause);

        // Then
        assertThat(exception.getCause()).isEqualTo(intermediateCause);
        assertThat(exception.getCause().getCause()).isEqualTo(rootCause);
    }

    @Test
    @DisplayName("스택 트레이스가 정상적으로 생성된다")
    void shouldHaveProperStackTrace() {
        // Given
        String message = "Session validation failed";

        // When
        SessionValidationException exception = new SessionValidationException(message);

        // Then
        assertThat(exception.getStackTrace()).isNotNull();
        assertThat(exception.getStackTrace()).isNotEmpty();
        assertThat(exception.getStackTrace()[0].getClassName())
                .contains("SessionValidationExceptionTest");
    }

    @Test
    @DisplayName("예외 정보가 toString에 포함된다")
    void shouldIncludeExceptionInfoInToString() {
        // Given
        String message = "Session state is invalid";

        // When
        SessionValidationException exception = new SessionValidationException(message);

        // Then
        String toString = exception.toString();
        assertThat(toString).contains("SessionValidationException");
        assertThat(toString).contains(message);
    }

    @Test
    @DisplayName("여러 검증 실패 정보를 포함한 메시지로 예외를 생성할 수 있다")
    void shouldCreateExceptionWithMultipleValidationFailures() {
        // Given
        String message = "Multiple validation failures: " +
                "[1] Session ID is invalid, " +
                "[2] User ID is missing, " +
                "[3] Client IP is malformed";

        // When
        SessionValidationException exception = new SessionValidationException(message);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getMessage()).contains("Session ID is invalid");
        assertThat(exception.getMessage()).contains("User ID is missing");
        assertThat(exception.getMessage()).contains("Client IP is malformed");
    }

    @Test
    @DisplayName("특수 문자가 포함된 메시지로 예외를 생성할 수 있다")
    void shouldCreateExceptionWithSpecialCharacterMessage() {
        // Given
        String message = "Validation failed: session@#$%^&*()_+-={}[]|\\:;\"'<>?,./";

        // When
        SessionValidationException exception = new SessionValidationException(message);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("JSON 형태의 검증 실패 정보로 예외를 생성할 수 있다")
    void shouldCreateExceptionWithJsonValidationInfo() {
        // Given
        String message = "Validation failed: {\"field\":\"sessionId\",\"error\":\"invalid format\",\"value\":\"abc123\"}";

        // When
        SessionValidationException exception = new SessionValidationException(message);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getMessage()).contains("sessionId");
        assertThat(exception.getMessage()).contains("invalid format");
        assertThat(exception.getMessage()).contains("abc123");
    }

    @Test
    @DisplayName("긴 검증 실패 메시지로 예외를 생성할 수 있다")
    void shouldCreateExceptionWithLongValidationMessage() {
        // Given
        StringBuilder messageBuilder = new StringBuilder("Session validation failed due to multiple issues: ");
        for (int i = 1; i <= 10; i++) {
            messageBuilder.append("Issue ").append(i).append(": Some validation error description. ");
        }
        String message = messageBuilder.toString();

        // When
        SessionValidationException exception = new SessionValidationException(message);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getMessage().length()).isGreaterThan(300);
    }

    @Test
    @DisplayName("중첩된 검증 예외를 원인으로 하는 예외를 생성할 수 있다")
    void shouldCreateExceptionWithNestedValidationException() {
        // Given
        SessionValidationException nestedCause = new SessionValidationException("Nested validation error");
        String message = "Parent validation failed";

        // When
        SessionValidationException exception = new SessionValidationException(message, nestedCause);

        // Then
        assertThat(exception.getCause()).isEqualTo(nestedCause);
        assertThat(exception.getCause()).isInstanceOf(SessionValidationException.class);
        assertThat(exception.getCause().getMessage()).isEqualTo("Nested validation error");
    }
}