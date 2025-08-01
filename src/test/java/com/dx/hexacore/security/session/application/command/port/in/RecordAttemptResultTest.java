package com.dx.hexacore.security.session.application.command.port.in;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RecordAttemptResult 테스트")
class RecordAttemptResultTest {

    @Test
    @DisplayName("성공적인 시도 기록 결과를 생성할 수 있다")
    void shouldCreateSuccessfulAttemptResult() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        boolean isSuccessful = true;
        boolean isAccountLocked = false;
        LocalDateTime recordedAt = LocalDateTime.now();

        // When
        RecordAttemptResult result = new RecordAttemptResult(
                sessionId, userId, isSuccessful, isAccountLocked, null, recordedAt);

        // Then
        assertThat(result.sessionId()).isEqualTo(sessionId);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.isAccountLocked()).isFalse();
        assertThat(result.lockedUntil()).isNull();
        assertThat(result.recordedAt()).isEqualTo(recordedAt);
    }

    @Test
    @DisplayName("계정 잠금이 발생한 결과를 생성할 수 있다")
    void shouldCreateResultWithAccountLocked() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        boolean isSuccessful = false;
        boolean isAccountLocked = true;
        LocalDateTime lockedUntil = LocalDateTime.now().plusMinutes(30);
        LocalDateTime recordedAt = LocalDateTime.now();

        // When
        RecordAttemptResult result = new RecordAttemptResult(
                sessionId, userId, isSuccessful, isAccountLocked, lockedUntil, recordedAt);

        // Then
        assertThat(result.sessionId()).isEqualTo(sessionId);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.isAccountLocked()).isTrue();
        assertThat(result.lockedUntil()).isEqualTo(lockedUntil);
        assertThat(result.recordedAt()).isEqualTo(recordedAt);
    }

    @Test
    @DisplayName("실패했지만 잠금되지 않은 결과를 생성할 수 있다")
    void shouldCreateFailedButNotLockedResult() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        boolean isSuccessful = false;
        boolean isAccountLocked = false;
        LocalDateTime recordedAt = LocalDateTime.now();

        // When
        RecordAttemptResult result = new RecordAttemptResult(
                sessionId, userId, isSuccessful, isAccountLocked, null, recordedAt);

        // Then
        assertThat(result.sessionId()).isEqualTo(sessionId);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.isAccountLocked()).isFalse();
        assertThat(result.lockedUntil()).isNull();
        assertThat(result.recordedAt()).isEqualTo(recordedAt);
    }

    @Test
    @DisplayName("결과 객체의 불변성을 확인한다")
    void shouldBeImmutable() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        boolean isSuccessful = true;
        boolean isAccountLocked = false;
        LocalDateTime recordedAt = LocalDateTime.now();

        // When
        RecordAttemptResult result = new RecordAttemptResult(
                sessionId, userId, isSuccessful, isAccountLocked, null, recordedAt);

        // Then - 모든 필드가 불변이어야 함
        assertThat(result.sessionId()).isEqualTo(sessionId);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.isSuccessful()).isEqualTo(isSuccessful);
        assertThat(result.isAccountLocked()).isEqualTo(isAccountLocked);
        assertThat(result.lockedUntil()).isNull();
        assertThat(result.recordedAt()).isEqualTo(recordedAt);
    }

    @Test
    @DisplayName("동등성 비교가 올바르게 동작한다")
    void shouldCompareEquality() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        boolean isSuccessful = true;
        boolean isAccountLocked = false;
        LocalDateTime recordedAt = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

        RecordAttemptResult result1 = new RecordAttemptResult(
                sessionId, userId, isSuccessful, isAccountLocked, null, recordedAt);
        RecordAttemptResult result2 = new RecordAttemptResult(
                sessionId, userId, isSuccessful, isAccountLocked, null, recordedAt);

        // When & Then
        assertThat(result1).isEqualTo(result2);
        assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
    }

    @Test
    @DisplayName("서로 다른 값을 가진 결과는 동등하지 않다")
    void shouldNotBeEqualWithDifferentValues() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId1 = "testUser123";
        String userId2 = "testUser456";
        boolean isSuccessful = true;
        boolean isAccountLocked = false;
        LocalDateTime recordedAt = LocalDateTime.now();

        RecordAttemptResult result1 = new RecordAttemptResult(
                sessionId, userId1, isSuccessful, isAccountLocked, null, recordedAt);
        RecordAttemptResult result2 = new RecordAttemptResult(
                sessionId, userId2, isSuccessful, isAccountLocked, null, recordedAt);

        // When & Then
        assertThat(result1).isNotEqualTo(result2);
    }

    @Test
    @DisplayName("toString이 적절한 정보를 반환한다")
    void shouldReturnAppropriateToString() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        boolean isSuccessful = true;
        boolean isAccountLocked = false;
        LocalDateTime recordedAt = LocalDateTime.now();

        RecordAttemptResult result = new RecordAttemptResult(
                sessionId, userId, isSuccessful, isAccountLocked, null, recordedAt);

        // When
        String stringResult = result.toString();

        // Then
        assertThat(stringResult).contains("RecordAttemptResult");
        assertThat(stringResult).contains(sessionId);
        assertThat(stringResult).contains(userId);
        assertThat(stringResult).contains(String.valueOf(isSuccessful));
        assertThat(stringResult).contains(String.valueOf(isAccountLocked));
    }

    @Test
    @DisplayName("잠금 해제 시각이 있는 결과의 toString이 올바르게 동작한다")
    void shouldReturnAppropriateToStringWithLockedUntil() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        boolean isSuccessful = false;
        boolean isAccountLocked = true;
        LocalDateTime lockedUntil = LocalDateTime.now().plusMinutes(30);
        LocalDateTime recordedAt = LocalDateTime.now();

        RecordAttemptResult result = new RecordAttemptResult(
                sessionId, userId, isSuccessful, isAccountLocked, lockedUntil, recordedAt);

        // When
        String stringResult = result.toString();

        // Then
        assertThat(stringResult).contains("RecordAttemptResult");
        assertThat(stringResult).contains(sessionId);
        assertThat(stringResult).contains(userId);
        assertThat(stringResult).contains("false"); // isSuccessful
        assertThat(stringResult).contains("true");  // isAccountLocked
        assertThat(stringResult).contains("lockedUntil");
    }

    @Test
    @DisplayName("null 세션 ID로 결과를 생성할 수 있다")
    void shouldCreateResultWithNullSessionId() {
        // Given
        String sessionId = null;
        String userId = "testUser123";
        boolean isSuccessful = true;
        boolean isAccountLocked = false;
        LocalDateTime recordedAt = LocalDateTime.now();

        // When
        RecordAttemptResult result = new RecordAttemptResult(
                sessionId, userId, isSuccessful, isAccountLocked, null, recordedAt);

        // Then
        assertThat(result.sessionId()).isNull();
        assertThat(result.userId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("null 기록 시각으로 결과를 생성할 수 있다")
    void shouldCreateResultWithNullRecordedAt() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        boolean isSuccessful = true;
        boolean isAccountLocked = false;
        LocalDateTime recordedAt = null;

        // When
        RecordAttemptResult result = new RecordAttemptResult(
                sessionId, userId, isSuccessful, isAccountLocked, null, recordedAt);

        // Then
        assertThat(result.sessionId()).isEqualTo(sessionId);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.recordedAt()).isNull();
    }
}