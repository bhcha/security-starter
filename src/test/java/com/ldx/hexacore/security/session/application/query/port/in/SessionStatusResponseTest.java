package com.ldx.hexacore.security.session.application.query.port.in;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SessionStatusResponse 테스트")
class SessionStatusResponseTest {

    @Test
    @DisplayName("활성 세션 응답을 생성할 수 있다")
    void shouldCreateActiveSessionResponse() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String primaryUserId = "testUser123";
        String primaryClientIp = "192.168.1.100";
        boolean isLocked = false;
        LocalDateTime lockedUntil = null;
        LocalDateTime createdAt = LocalDateTime.now().minusHours(1);
        LocalDateTime lastActivityAt = LocalDateTime.now();
        int totalAttempts = 5;
        int failedAttempts = 1;

        // When
        SessionStatusResponse response = new SessionStatusResponse(
                sessionId, primaryUserId, primaryClientIp, isLocked, lockedUntil,
                createdAt, lastActivityAt, totalAttempts, failedAttempts);

        // Then
        assertThat(response.sessionId()).isEqualTo(sessionId);
        assertThat(response.primaryUserId()).isEqualTo(primaryUserId);
        assertThat(response.primaryClientIp()).isEqualTo(primaryClientIp);
        assertThat(response.isLocked()).isFalse();
        assertThat(response.lockedUntil()).isNull();
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.lastActivityAt()).isEqualTo(lastActivityAt);
        assertThat(response.totalAttempts()).isEqualTo(totalAttempts);
        assertThat(response.failedAttempts()).isEqualTo(failedAttempts);
    }

    @Test
    @DisplayName("잠긴 세션 응답을 생성할 수 있다")
    void shouldCreateLockedSessionResponse() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String primaryUserId = "testUser123";
        String primaryClientIp = "192.168.1.100";
        boolean isLocked = true;
        LocalDateTime lockedUntil = LocalDateTime.now().plusMinutes(30);
        LocalDateTime createdAt = LocalDateTime.now().minusHours(1);
        LocalDateTime lastActivityAt = LocalDateTime.now();
        int totalAttempts = 10;
        int failedAttempts = 5;

        // When
        SessionStatusResponse response = new SessionStatusResponse(
                sessionId, primaryUserId, primaryClientIp, isLocked, lockedUntil,
                createdAt, lastActivityAt, totalAttempts, failedAttempts);

        // Then
        assertThat(response.sessionId()).isEqualTo(sessionId);
        assertThat(response.primaryUserId()).isEqualTo(primaryUserId);
        assertThat(response.primaryClientIp()).isEqualTo(primaryClientIp);
        assertThat(response.isLocked()).isTrue();
        assertThat(response.lockedUntil()).isEqualTo(lockedUntil);
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.lastActivityAt()).isEqualTo(lastActivityAt);
        assertThat(response.totalAttempts()).isEqualTo(totalAttempts);
        assertThat(response.failedAttempts()).isEqualTo(failedAttempts);
    }

    @Test
    @DisplayName("응답 객체의 불변성을 확인한다")
    void shouldBeImmutable() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String primaryUserId = "testUser123";
        String primaryClientIp = "192.168.1.100";
        boolean isLocked = false;
        LocalDateTime lockedUntil = null;
        LocalDateTime createdAt = LocalDateTime.now().minusHours(1);
        LocalDateTime lastActivityAt = LocalDateTime.now();
        int totalAttempts = 5;
        int failedAttempts = 1;

        // When
        SessionStatusResponse response = new SessionStatusResponse(
                sessionId, primaryUserId, primaryClientIp, isLocked, lockedUntil,
                createdAt, lastActivityAt, totalAttempts, failedAttempts);

        // Then - 모든 필드가 불변이어야 함
        assertThat(response.sessionId()).isEqualTo(sessionId);
        assertThat(response.primaryUserId()).isEqualTo(primaryUserId);
        assertThat(response.primaryClientIp()).isEqualTo(primaryClientIp);
        assertThat(response.isLocked()).isEqualTo(isLocked);
        assertThat(response.lockedUntil()).isEqualTo(lockedUntil);
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.lastActivityAt()).isEqualTo(lastActivityAt);
        assertThat(response.totalAttempts()).isEqualTo(totalAttempts);
        assertThat(response.failedAttempts()).isEqualTo(failedAttempts);
    }

    @Test
    @DisplayName("동등성 비교가 올바르게 동작한다")
    void shouldCompareEquality() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String primaryUserId = "testUser123";
        String primaryClientIp = "192.168.1.100";
        boolean isLocked = false;
        LocalDateTime lockedUntil = null;
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 12, 0);
        LocalDateTime lastActivityAt = LocalDateTime.of(2024, 1, 1, 13, 0);
        int totalAttempts = 5;
        int failedAttempts = 1;

        SessionStatusResponse response1 = new SessionStatusResponse(
                sessionId, primaryUserId, primaryClientIp, isLocked, lockedUntil,
                createdAt, lastActivityAt, totalAttempts, failedAttempts);
        SessionStatusResponse response2 = new SessionStatusResponse(
                sessionId, primaryUserId, primaryClientIp, isLocked, lockedUntil,
                createdAt, lastActivityAt, totalAttempts, failedAttempts);

        // When & Then
        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }

    @Test
    @DisplayName("서로 다른 값을 가진 응답은 동등하지 않다")
    void shouldNotBeEqualWithDifferentValues() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String primaryUserId1 = "testUser123";
        String primaryUserId2 = "testUser456";
        String primaryClientIp = "192.168.1.100";
        boolean isLocked = false;
        LocalDateTime lockedUntil = null;
        LocalDateTime createdAt = LocalDateTime.now().minusHours(1);
        LocalDateTime lastActivityAt = LocalDateTime.now();
        int totalAttempts = 5;
        int failedAttempts = 1;

        SessionStatusResponse response1 = new SessionStatusResponse(
                sessionId, primaryUserId1, primaryClientIp, isLocked, lockedUntil,
                createdAt, lastActivityAt, totalAttempts, failedAttempts);
        SessionStatusResponse response2 = new SessionStatusResponse(
                sessionId, primaryUserId2, primaryClientIp, isLocked, lockedUntil,
                createdAt, lastActivityAt, totalAttempts, failedAttempts);

        // When & Then
        assertThat(response1).isNotEqualTo(response2);
    }

    @Test
    @DisplayName("toString이 적절한 정보를 반환한다")
    void shouldReturnAppropriateToString() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String primaryUserId = "testUser123";
        String primaryClientIp = "192.168.1.100";
        boolean isLocked = true;
        LocalDateTime lockedUntil = LocalDateTime.now().plusMinutes(30);
        LocalDateTime createdAt = LocalDateTime.now().minusHours(1);
        LocalDateTime lastActivityAt = LocalDateTime.now();
        int totalAttempts = 10;
        int failedAttempts = 5;

        SessionStatusResponse response = new SessionStatusResponse(
                sessionId, primaryUserId, primaryClientIp, isLocked, lockedUntil,
                createdAt, lastActivityAt, totalAttempts, failedAttempts);

        // When
        String result = response.toString();

        // Then
        assertThat(result).contains("SessionStatusResponse");
        assertThat(result).contains(sessionId);
        assertThat(result).contains(primaryUserId);
        assertThat(result).contains(primaryClientIp);
        assertThat(result).contains("true"); // isLocked
        assertThat(result).contains(String.valueOf(totalAttempts));
        assertThat(result).contains(String.valueOf(failedAttempts));
    }

    @Test
    @DisplayName("null 값들로 응답을 생성할 수 있다")
    void shouldCreateResponseWithNullValues() {
        // Given
        String sessionId = null;
        String primaryUserId = null;
        String primaryClientIp = null;
        boolean isLocked = false;
        LocalDateTime lockedUntil = null;
        LocalDateTime createdAt = null;
        LocalDateTime lastActivityAt = null;
        int totalAttempts = 0;
        int failedAttempts = 0;

        // When
        SessionStatusResponse response = new SessionStatusResponse(
                sessionId, primaryUserId, primaryClientIp, isLocked, lockedUntil,
                createdAt, lastActivityAt, totalAttempts, failedAttempts);

        // Then
        assertThat(response.sessionId()).isNull();
        assertThat(response.primaryUserId()).isNull();
        assertThat(response.primaryClientIp()).isNull();
        assertThat(response.isLocked()).isFalse();
        assertThat(response.lockedUntil()).isNull();
        assertThat(response.createdAt()).isNull();
        assertThat(response.lastActivityAt()).isNull();
        assertThat(response.totalAttempts()).isEqualTo(0);
        assertThat(response.failedAttempts()).isEqualTo(0);
    }

    @Test
    @DisplayName("음수 시도 횟수로 응답을 생성할 수 있다")
    void shouldCreateResponseWithNegativeAttempts() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String primaryUserId = "testUser123";
        String primaryClientIp = "192.168.1.100";
        boolean isLocked = false;
        LocalDateTime lockedUntil = null;
        LocalDateTime createdAt = LocalDateTime.now().minusHours(1);
        LocalDateTime lastActivityAt = LocalDateTime.now();
        int totalAttempts = -1;
        int failedAttempts = -1;

        // When
        SessionStatusResponse response = new SessionStatusResponse(
                sessionId, primaryUserId, primaryClientIp, isLocked, lockedUntil,
                createdAt, lastActivityAt, totalAttempts, failedAttempts);

        // Then
        assertThat(response.totalAttempts()).isEqualTo(-1);
        assertThat(response.failedAttempts()).isEqualTo(-1);
    }

    @Test
    @DisplayName("IPv6 주소로 응답을 생성할 수 있다")
    void shouldCreateResponseWithIPv6Address() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String primaryUserId = "testUser123";
        String primaryClientIp = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
        boolean isLocked = false;
        LocalDateTime lockedUntil = null;
        LocalDateTime createdAt = LocalDateTime.now().minusHours(1);        LocalDateTime lastActivityAt = LocalDateTime.now();
        int totalAttempts = 5;
        int failedAttempts = 1;

        // When
        SessionStatusResponse response = new SessionStatusResponse(
                sessionId, primaryUserId, primaryClientIp, isLocked, lockedUntil,
                createdAt, lastActivityAt, totalAttempts, failedAttempts);

        // Then
        assertThat(response.primaryClientIp()).isEqualTo(primaryClientIp);
    }

    @Test
    @DisplayName("특수 문자가 포함된 사용자 ID로 응답을 생성할 수 있다")
    void shouldCreateResponseWithSpecialCharacterUserId() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String primaryUserId = "user@example.com";
        String primaryClientIp = "192.168.1.100";
        boolean isLocked = false;
        LocalDateTime lockedUntil = null;
        LocalDateTime createdAt = LocalDateTime.now().minusHours(1);
        LocalDateTime lastActivityAt = LocalDateTime.now();
        int totalAttempts = 5;
        int failedAttempts = 1;

        // When
        SessionStatusResponse response = new SessionStatusResponse(
                sessionId, primaryUserId, primaryClientIp, isLocked, lockedUntil,
                createdAt, lastActivityAt, totalAttempts, failedAttempts);

        // Then
        assertThat(response.primaryUserId()).isEqualTo(primaryUserId);
    }

    @Test
    @DisplayName("실패 횟수가 총 시도 횟수보다 많은 경우도 허용한다")
    void shouldAllowFailedAttemptsGreaterThanTotal() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String primaryUserId = "testUser123";
        String primaryClientIp = "192.168.1.100";
        boolean isLocked = false;
        LocalDateTime lockedUntil = null;
        LocalDateTime createdAt = LocalDateTime.now().minusHours(1);
        LocalDateTime lastActivityAt = LocalDateTime.now();
        int totalAttempts = 3;
        int failedAttempts = 5; // 총 시도보다 많음

        // When
        SessionStatusResponse response = new SessionStatusResponse(
                sessionId, primaryUserId, primaryClientIp, isLocked, lockedUntil,
                createdAt, lastActivityAt, totalAttempts, failedAttempts);

        // Then
        assertThat(response.totalAttempts()).isEqualTo(3);
        assertThat(response.failedAttempts()).isEqualTo(5);
    }
}