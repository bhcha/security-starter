package com.dx.hexacore.security.session.application.command.port.in;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("LockoutCheckResult 테스트")
class LockoutCheckResultTest {

    @Test
    @DisplayName("잠금되지 않은 상태 결과를 생성할 수 있다")
    void shouldCreateNotLockedResult() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        boolean isLocked = false;
        LocalDateTime checkedAt = LocalDateTime.now();

        // When
        LockoutCheckResult result = new LockoutCheckResult(
                sessionId, userId, isLocked, null, checkedAt);

        // Then
        assertThat(result.sessionId()).isEqualTo(sessionId);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.isLocked()).isFalse();
        assertThat(result.lockedUntil()).isNull();
        assertThat(result.checkedAt()).isEqualTo(checkedAt);
    }

    @Test
    @DisplayName("잠긴 상태 결과를 생성할 수 있다")
    void shouldCreateLockedResult() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        boolean isLocked = true;
        LocalDateTime lockedUntil = LocalDateTime.now().plusMinutes(30);
        LocalDateTime checkedAt = LocalDateTime.now();

        // When
        LockoutCheckResult result = new LockoutCheckResult(
                sessionId, userId, isLocked, lockedUntil, checkedAt);

        // Then
        assertThat(result.sessionId()).isEqualTo(sessionId);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.isLocked()).isTrue();
        assertThat(result.lockedUntil()).isEqualTo(lockedUntil);
        assertThat(result.checkedAt()).isEqualTo(checkedAt);
    }

    @Test
    @DisplayName("결과 객체의 불변성을 확인한다")
    void shouldBeImmutable() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        boolean isLocked = true;
        LocalDateTime lockedUntil = LocalDateTime.now().plusMinutes(30);
        LocalDateTime checkedAt = LocalDateTime.now();

        // When
        LockoutCheckResult result = new LockoutCheckResult(
                sessionId, userId, isLocked, lockedUntil, checkedAt);

        // Then - 모든 필드가 불변이어야 함
        assertThat(result.sessionId()).isEqualTo(sessionId);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.isLocked()).isEqualTo(isLocked);
        assertThat(result.lockedUntil()).isEqualTo(lockedUntil);
        assertThat(result.checkedAt()).isEqualTo(checkedAt);
    }

    @Test
    @DisplayName("동등성 비교가 올바르게 동작한다")
    void shouldCompareEquality() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        boolean isLocked = true;
        LocalDateTime lockedUntil = LocalDateTime.of(2024, 1, 1, 12, 30, 0);
        LocalDateTime checkedAt = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

        LockoutCheckResult result1 = new LockoutCheckResult(
                sessionId, userId, isLocked, lockedUntil, checkedAt);
        LockoutCheckResult result2 = new LockoutCheckResult(
                sessionId, userId, isLocked, lockedUntil, checkedAt);

        // When & Then
        assertThat(result1).isEqualTo(result2);
        assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
    }

    @Test
    @DisplayName("서로 다른 값을 가진 결과는 동등하지 않다")
    void shouldNotBeEqualWithDifferentValues() {
        // Given
        String sessionId1 = "550e8400-e29b-41d4-a716-446655440000";
        String sessionId2 = "550e8400-e29b-41d4-a716-446655440001";
        String userId = "testUser123";
        boolean isLocked = true;
        LocalDateTime lockedUntil = LocalDateTime.now().plusMinutes(30);
        LocalDateTime checkedAt = LocalDateTime.now();

        LockoutCheckResult result1 = new LockoutCheckResult(
                sessionId1, userId, isLocked, lockedUntil, checkedAt);
        LockoutCheckResult result2 = new LockoutCheckResult(
                sessionId2, userId, isLocked, lockedUntil, checkedAt);

        // When & Then
        assertThat(result1).isNotEqualTo(result2);
    }

    @Test
    @DisplayName("toString이 적절한 정보를 반환한다")
    void shouldReturnAppropriateToString() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        boolean isLocked = true;
        LocalDateTime lockedUntil = LocalDateTime.now().plusMinutes(30);
        LocalDateTime checkedAt = LocalDateTime.now();

        LockoutCheckResult result = new LockoutCheckResult(
                sessionId, userId, isLocked, lockedUntil, checkedAt);

        // When
        String stringResult = result.toString();

        // Then
        assertThat(stringResult).contains("LockoutCheckResult");
        assertThat(stringResult).contains(sessionId);
        assertThat(stringResult).contains(userId);
        assertThat(stringResult).contains("true"); // isLocked
        assertThat(stringResult).contains("lockedUntil");
        assertThat(stringResult).contains("checkedAt");
    }

    @Test
    @DisplayName("잠금되지 않은 상태의 toString이 올바르게 동작한다")
    void shouldReturnAppropriateToStringWhenNotLocked() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        boolean isLocked = false;
        LocalDateTime checkedAt = LocalDateTime.now();

        LockoutCheckResult result = new LockoutCheckResult(
                sessionId, userId, isLocked, null, checkedAt);

        // When
        String stringResult = result.toString();

        // Then
        assertThat(stringResult).contains("LockoutCheckResult");
        assertThat(stringResult).contains(sessionId);
        assertThat(stringResult).contains(userId);
        assertThat(stringResult).contains("false"); // isLocked
        assertThat(stringResult).contains("checkedAt");
    }

    @Test
    @DisplayName("null 세션 ID로 결과를 생성할 수 있다")
    void shouldCreateResultWithNullSessionId() {
        // Given
        String sessionId = null;
        String userId = "testUser123";
        boolean isLocked = false;
        LocalDateTime checkedAt = LocalDateTime.now();

        // When
        LockoutCheckResult result = new LockoutCheckResult(
                sessionId, userId, isLocked, null, checkedAt);

        // Then
        assertThat(result.sessionId()).isNull();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.isLocked()).isFalse();
    }

    @Test
    @DisplayName("null 사용자 ID로 결과를 생성할 수 있다")
    void shouldCreateResultWithNullUserId() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = null;
        boolean isLocked = false;
        LocalDateTime checkedAt = LocalDateTime.now();

        // When
        LockoutCheckResult result = new LockoutCheckResult(
                sessionId, userId, isLocked, null, checkedAt);

        // Then
        assertThat(result.sessionId()).isEqualTo(sessionId);
        assertThat(result.userId()).isNull();
        assertThat(result.isLocked()).isFalse();
    }

    @Test
    @DisplayName("null 확인 시각으로 결과를 생성할 수 있다")
    void shouldCreateResultWithNullCheckedAt() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        boolean isLocked = false;
        LocalDateTime checkedAt = null;

        // When
        LockoutCheckResult result = new LockoutCheckResult(
                sessionId, userId, isLocked, null, checkedAt);

        // Then
        assertThat(result.sessionId()).isEqualTo(sessionId);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.checkedAt()).isNull();
    }

    @Test
    @DisplayName("잠긴 상태이지만 lockedUntil이 null인 경우를 처리할 수 있다")
    void shouldHandleLockedStateWithNullLockedUntil() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        boolean isLocked = true;
        LocalDateTime lockedUntil = null;
        LocalDateTime checkedAt = LocalDateTime.now();

        // When
        LockoutCheckResult result = new LockoutCheckResult(
                sessionId, userId, isLocked, lockedUntil, checkedAt);

        // Then
        assertThat(result.sessionId()).isEqualTo(sessionId);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.isLocked()).isTrue();
        assertThat(result.lockedUntil()).isNull();
        assertThat(result.checkedAt()).isEqualTo(checkedAt);
    }

    @Test
    @DisplayName("과거 시점의 lockedUntil로 결과를 생성할 수 있다")
    void shouldCreateResultWithPastLockedUntil() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        boolean isLocked = false;
        LocalDateTime lockedUntil = LocalDateTime.now().minusMinutes(5); // 5분 전
        LocalDateTime checkedAt = LocalDateTime.now();

        // When
        LockoutCheckResult result = new LockoutCheckResult(
                sessionId, userId, isLocked, lockedUntil, checkedAt);

        // Then
        assertThat(result.sessionId()).isEqualTo(sessionId);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.isLocked()).isFalse();
        assertThat(result.lockedUntil()).isEqualTo(lockedUntil);
        assertThat(result.checkedAt()).isEqualTo(checkedAt);
    }
}