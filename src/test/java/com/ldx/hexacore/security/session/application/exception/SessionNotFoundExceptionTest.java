package com.ldx.hexacore.security.session.application.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SessionNotFoundException 테스트")
class SessionNotFoundExceptionTest {

    @Test
    @DisplayName("세션 ID와 함께 예외를 생성할 수 있다")
    void shouldCreateExceptionWithSessionId() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";

        // When
        SessionNotFoundException exception = new SessionNotFoundException(sessionId);

        // Then
        assertThat(exception.getMessage()).isEqualTo("Session not found: " + sessionId);
        assertThat(exception.getSessionId()).isEqualTo(sessionId);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("기본 메시지로 예외를 생성할 수 있다")
    void shouldCreateExceptionWithDefaultMessage() {
        // Given
        String message = "Session not found";

        // When
        SessionNotFoundException exception = new SessionNotFoundException(message, true);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getSessionId()).isNull();
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("메시지와 원인과 함께 예외를 생성할 수 있다")
    void shouldCreateExceptionWithMessageAndCause() {
        // Given
        String message = "Session retrieval failed";
        Throwable cause = new IllegalStateException("Database connection failed");

        // When
        SessionNotFoundException exception = new SessionNotFoundException(message, cause);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getSessionId()).isNull();
    }

    @Test
    @DisplayName("세션 ID, 메시지, 원인과 함께 예외를 생성할 수 있다")
    void shouldCreateExceptionWithSessionIdMessageAndCause() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String message = "Session retrieval failed";
        Throwable cause = new IllegalStateException("Database connection failed");

        // When
        SessionNotFoundException exception = new SessionNotFoundException(sessionId, message, cause);

        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getSessionId()).isEqualTo(sessionId);
    }

    @Test
    @DisplayName("null 세션 ID로 예외를 생성할 수 있다")
    void shouldCreateExceptionWithNullSessionId() {
        // Given
        String sessionId = null;

        // When
        SessionNotFoundException exception = new SessionNotFoundException(sessionId);

        // Then
        assertThat(exception.getMessage()).isEqualTo("Session not found: null");
        assertThat(exception.getSessionId()).isNull();
    }

    @Test
    @DisplayName("빈 세션 ID로 예외를 생성할 수 있다")
    void shouldCreateExceptionWithEmptySessionId() {
        // Given
        String sessionId = "";

        // When
        SessionNotFoundException exception = new SessionNotFoundException(sessionId);

        // Then
        assertThat(exception.getMessage()).isEqualTo("Session not found: ");
        assertThat(exception.getSessionId()).isEqualTo("");
    }

    @Test
    @DisplayName("null 메시지로 예외를 생성할 수 있다")
    void shouldCreateExceptionWithNullMessage() {
        // Given
        String message = null;

        // When
        SessionNotFoundException exception = new SessionNotFoundException(message, true);

        // Then
        assertThat(exception.getMessage()).isNull();
        assertThat(exception.getSessionId()).isNull();
    }

    @Test
    @DisplayName("예외 체인이 올바르게 동작한다")
    void shouldMaintainExceptionChain() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        RuntimeException rootCause = new RuntimeException("Root cause");
        IllegalStateException intermediateCause = new IllegalStateException("Intermediate cause", rootCause);

        // When
        SessionNotFoundException exception = new SessionNotFoundException(sessionId, "Final message", intermediateCause);

        // Then
        assertThat(exception.getCause()).isEqualTo(intermediateCause);
        assertThat(exception.getCause().getCause()).isEqualTo(rootCause);
    }

    @Test
    @DisplayName("스택 트레이스가 정상적으로 생성된다")
    void shouldHaveProperStackTrace() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";

        // When
        SessionNotFoundException exception = new SessionNotFoundException(sessionId);

        // Then
        assertThat(exception.getStackTrace()).isNotNull();
        assertThat(exception.getStackTrace()).isNotEmpty();
        assertThat(exception.getStackTrace()[0].getClassName())
                .contains("SessionNotFoundExceptionTest");
    }

    @Test
    @DisplayName("예외 정보가 toString에 포함된다")
    void shouldIncludeExceptionInfoInToString() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String message = "Session retrieval failed";

        // When
        SessionNotFoundException exception = new SessionNotFoundException(sessionId, message, null);

        // Then
        String toString = exception.toString();
        assertThat(toString).contains("SessionNotFoundException");
        assertThat(toString).contains(message);
    }

    @Test
    @DisplayName("원인 없는 예외의 getCause는 null을 반환한다")
    void shouldReturnNullForCauseWhenNoCause() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";

        // When
        SessionNotFoundException exception = new SessionNotFoundException(sessionId);

        // Then
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("긴 세션 ID로 예외를 생성할 수 있다")
    void shouldCreateExceptionWithLongSessionId() {
        // Given
        String sessionId = "very-long-session-id-with-multiple-parts-and-special-characters-123456789-abcdef";

        // When
        SessionNotFoundException exception = new SessionNotFoundException(sessionId);

        // Then
        assertThat(exception.getMessage()).isEqualTo("Session not found: " + sessionId);
        assertThat(exception.getSessionId()).isEqualTo(sessionId);
    }

    @Test
    @DisplayName("특수 문자가 포함된 세션 ID로 예외를 생성할 수 있다")
    void shouldCreateExceptionWithSpecialCharacterSessionId() {
        // Given
        String sessionId = "session@#$%^&*()_+-={}[]|\\:;\"'<>?,./";

        // When
        SessionNotFoundException exception = new SessionNotFoundException(sessionId);

        // Then
        assertThat(exception.getMessage()).isEqualTo("Session not found: " + sessionId);
        assertThat(exception.getSessionId()).isEqualTo(sessionId);
    }
}