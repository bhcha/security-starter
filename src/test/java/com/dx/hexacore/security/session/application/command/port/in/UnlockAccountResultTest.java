package com.dx.hexacore.security.session.application.command.port.in;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UnlockAccountResult 테스트")
class UnlockAccountResultTest {

    @Test
    @DisplayName("성공적인 해제 결과를 생성할 수 있다")
    void shouldCreateSuccessfulUnlockResult() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        boolean wasLocked = true;
        boolean unlockSuccessful = true;
        LocalDateTime unlockedAt = LocalDateTime.now();

        // When
        UnlockAccountResult result = new UnlockAccountResult(
                sessionId, userId, wasLocked, unlockSuccessful, unlockedAt);

        // Then
        assertThat(result.sessionId()).isEqualTo(sessionId);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.wasLocked()).isTrue();
        assertThat(result.unlockSuccessful()).isTrue();
        assertThat(result.unlockedAt()).isEqualTo(unlockedAt);
    }

    @Test
    @DisplayName("이미 해제된 계정의 해제 시도 결과를 생성할 수 있다")
    void shouldCreateAlreadyUnlockedResult() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        boolean wasLocked = false;
        boolean unlockSuccessful = true;
        LocalDateTime unlockedAt = LocalDateTime.now();

        // When
        UnlockAccountResult result = new UnlockAccountResult(
                sessionId, userId, wasLocked, unlockSuccessful, unlockedAt);

        // Then
        assertThat(result.sessionId()).isEqualTo(sessionId);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.wasLocked()).isFalse();
        assertThat(result.unlockSuccessful()).isTrue();
        assertThat(result.unlockedAt()).isEqualTo(unlockedAt);
    }

    @Test
    @DisplayName("해제 실패 결과를 생성할 수 있다")
    void shouldCreateFailedUnlockResult() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        boolean wasLocked = true;
        boolean unlockSuccessful = false;
        LocalDateTime unlockedAt = null;

        // When
        UnlockAccountResult result = new UnlockAccountResult(
                sessionId, userId, wasLocked, unlockSuccessful, unlockedAt);

        // Then
        assertThat(result.sessionId()).isEqualTo(sessionId);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.wasLocked()).isTrue();
        assertThat(result.unlockSuccessful()).isFalse();
        assertThat(result.unlockedAt()).isNull();
    }

    @Test
    @DisplayName("결과 객체의 불변성을 확인한다")
    void shouldBeImmutable() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        boolean wasLocked = true;
        boolean unlockSuccessful = true;
        LocalDateTime unlockedAt = LocalDateTime.now();

        // When
        UnlockAccountResult result = new UnlockAccountResult(
                sessionId, userId, wasLocked, unlockSuccessful, unlockedAt);

        // Then - 모든 필드가 불변이어야 함
        assertThat(result.sessionId()).isEqualTo(sessionId);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.wasLocked()).isEqualTo(wasLocked);
        assertThat(result.unlockSuccessful()).isEqualTo(unlockSuccessful);
        assertThat(result.unlockedAt()).isEqualTo(unlockedAt);
    }

    @Test
    @DisplayName("동등성 비교가 올바르게 동작한다")
    void shouldCompareEquality() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        boolean wasLocked = true;
        boolean unlockSuccessful = true;
        LocalDateTime unlockedAt = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

        UnlockAccountResult result1 = new UnlockAccountResult(
                sessionId, userId, wasLocked, unlockSuccessful, unlockedAt);
        UnlockAccountResult result2 = new UnlockAccountResult(
                sessionId, userId, wasLocked, unlockSuccessful, unlockedAt);

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
        boolean wasLocked = true;
        boolean unlockSuccessful = true;
        LocalDateTime unlockedAt = LocalDateTime.now();

        UnlockAccountResult result1 = new UnlockAccountResult(
                sessionId, userId1, wasLocked, unlockSuccessful, unlockedAt);
        UnlockAccountResult result2 = new UnlockAccountResult(
                sessionId, userId2, wasLocked, unlockSuccessful, unlockedAt);

        // When & Then
        assertThat(result1).isNotEqualTo(result2);
    }

    @Test
    @DisplayName("toString이 적절한 정보를 반환한다")
    void shouldReturnAppropriateToString() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        boolean wasLocked = true;
        boolean unlockSuccessful = true;
        LocalDateTime unlockedAt = LocalDateTime.now();

        UnlockAccountResult result = new UnlockAccountResult(
                sessionId, userId, wasLocked, unlockSuccessful, unlockedAt);

        // When
        String stringResult = result.toString();

        // Then
        assertThat(stringResult).contains("UnlockAccountResult");
        assertThat(stringResult).contains(sessionId);
        assertThat(stringResult).contains(userId);
        assertThat(stringResult).contains("true"); // wasLocked
        assertThat(stringResult).contains("true"); // unlockSuccessful
        assertThat(stringResult).contains("unlockedAt");
    }

    @Test
    @DisplayName("해제 실패 시의 toString이 올바르게 동작한다")
    void shouldReturnAppropriateToStringWhenUnlockFailed() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        boolean wasLocked = true;
        boolean unlockSuccessful = false;
        LocalDateTime unlockedAt = null;

        UnlockAccountResult result = new UnlockAccountResult(
                sessionId, userId, wasLocked, unlockSuccessful, unlockedAt);

        // When
        String stringResult = result.toString();

        // Then
        assertThat(stringResult).contains("UnlockAccountResult");
        assertThat(stringResult).contains(sessionId);
        assertThat(stringResult).contains(userId);
        assertThat(stringResult).contains("true");  // wasLocked
        assertThat(stringResult).contains("false"); // unlockSuccessful
    }

    @Test
    @DisplayName("null 세션 ID로 결과를 생성할 수 있다")
    void shouldCreateResultWithNullSessionId() {
        // Given
        String sessionId = null;
        String userId = "testUser123";
        boolean wasLocked = true;
        boolean unlockSuccessful = true;
        LocalDateTime unlockedAt = LocalDateTime.now();

        // When
        UnlockAccountResult result = new UnlockAccountResult(
                sessionId, userId, wasLocked, unlockSuccessful, unlockedAt);

        // Then
        assertThat(result.sessionId()).isNull();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.unlockSuccessful()).isTrue();
    }

    @Test
    @DisplayName("null 사용자 ID로 결과를 생성할 수 있다")
    void shouldCreateResultWithNullUserId() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = null;
        boolean wasLocked = true;
        boolean unlockSuccessful = true;
        LocalDateTime unlockedAt = LocalDateTime.now();

        // When
        UnlockAccountResult result = new UnlockAccountResult(
                sessionId, userId, wasLocked, unlockSuccessful, unlockedAt);

        // Then
        assertThat(result.sessionId()).isEqualTo(sessionId);
        assertThat(result.userId()).isNull();
        assertThat(result.unlockSuccessful()).isTrue();
    }

    @Test
    @DisplayName("null 해제 시각으로 결과를 생성할 수 있다")
    void shouldCreateResultWithNullUnlockedAt() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        boolean wasLocked = true;
        boolean unlockSuccessful = false;
        LocalDateTime unlockedAt = null;

        // When
        UnlockAccountResult result = new UnlockAccountResult(
                sessionId, userId, wasLocked, unlockSuccessful, unlockedAt);

        // Then
        assertThat(result.sessionId()).isEqualTo(sessionId);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.unlockSuccessful()).isFalse();
        assertThat(result.unlockedAt()).isNull();
    }

    @Test
    @DisplayName("다양한 불린 조합으로 결과를 생성할 수 있다")
    void shouldCreateResultsWithVariousBooleanCombinations() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        LocalDateTime unlockedAt = LocalDateTime.now();

        // When & Then - 모든 불린 조합 테스트
        UnlockAccountResult result1 = new UnlockAccountResult(
                sessionId, userId, true, true, unlockedAt);
        assertThat(result1.wasLocked()).isTrue();
        assertThat(result1.unlockSuccessful()).isTrue();

        UnlockAccountResult result2 = new UnlockAccountResult(
                sessionId, userId, true, false, null);
        assertThat(result2.wasLocked()).isTrue();
        assertThat(result2.unlockSuccessful()).isFalse();

        UnlockAccountResult result3 = new UnlockAccountResult(
                sessionId, userId, false, true, unlockedAt);
        assertThat(result3.wasLocked()).isFalse();
        assertThat(result3.unlockSuccessful()).isTrue();

        UnlockAccountResult result4 = new UnlockAccountResult(
                sessionId, userId, false, false, null);
        assertThat(result4.wasLocked()).isFalse();
        assertThat(result4.unlockSuccessful()).isFalse();
    }
}