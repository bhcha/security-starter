package com.dx.hexacore.security.session.application.query.port.in;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("GetFailedAttemptsQuery 테스트")
class GetFailedAttemptsQueryTest {

    @Test
    @DisplayName("유효한 매개변수로 쿼리를 생성할 수 있다")
    void shouldCreateQueryWithValidParameters() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        LocalDateTime from = LocalDateTime.now().minusHours(1);
        LocalDateTime to = LocalDateTime.now();
        int limit = 10;

        // When
        GetFailedAttemptsQuery query = new GetFailedAttemptsQuery(sessionId, userId, from, to, limit);

        // Then
        assertThat(query.sessionId()).isEqualTo(sessionId);
        assertThat(query.userId()).isEqualTo(userId);
        assertThat(query.from()).isEqualTo(from);
        assertThat(query.to()).isEqualTo(to);
        assertThat(query.limit()).isEqualTo(limit);
    }

    @Test
    @DisplayName("사용자 ID 없이 쿼리를 생성할 수 있다")
    void shouldCreateQueryWithoutUserId() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = null;
        LocalDateTime from = LocalDateTime.now().minusHours(1);
        LocalDateTime to = LocalDateTime.now();
        int limit = 10;

        // When
        GetFailedAttemptsQuery query = new GetFailedAttemptsQuery(sessionId, userId, from, to, limit);

        // Then
        assertThat(query.sessionId()).isEqualTo(sessionId);
        assertThat(query.userId()).isNull();
        assertThat(query.from()).isEqualTo(from);
        assertThat(query.to()).isEqualTo(to);
        assertThat(query.limit()).isEqualTo(limit);
    }

    @Test
    @DisplayName("null 세션 ID로 쿼리 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenSessionIdIsNull() {
        // Given
        String sessionId = null;
        String userId = "testUser123";
        LocalDateTime from = LocalDateTime.now().minusHours(1);
        LocalDateTime to = LocalDateTime.now();
        int limit = 10;

        // When & Then
        assertThatThrownBy(() -> new GetFailedAttemptsQuery(sessionId, userId, from, to, limit))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("SessionId cannot be null or empty");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    @DisplayName("빈 세션 ID로 쿼리 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenSessionIdIsEmpty(String sessionId) {
        // Given
        String userId = "testUser123";
        LocalDateTime from = LocalDateTime.now().minusHours(1);
        LocalDateTime to = LocalDateTime.now();
        int limit = 10;

        // When & Then
        assertThatThrownBy(() -> new GetFailedAttemptsQuery(sessionId, userId, from, to, limit))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("SessionId cannot be null or empty");
    }

    @Test
    @DisplayName("시작 시간이 종료 시간보다 늦을 때 예외가 발생한다")
    void shouldThrowExceptionWhenFromIsAfterTo() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to = LocalDateTime.now().minusHours(1); // from보다 이전
        int limit = 10;

        // When & Then
        assertThatThrownBy(() -> new GetFailedAttemptsQuery(sessionId, userId, from, to, limit))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("From time cannot be after to time");
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -10, Integer.MIN_VALUE})
    @DisplayName("음수 제한 개수로 쿼리 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenLimitIsNegative(int limit) {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        LocalDateTime from = LocalDateTime.now().minusHours(1);
        LocalDateTime to = LocalDateTime.now();

        // When & Then
        assertThatThrownBy(() -> new GetFailedAttemptsQuery(sessionId, userId, from, to, limit))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Limit cannot be negative");
    }

    @Test
    @DisplayName("제한 개수 0으로 쿼리를 생성할 수 있다")
    void shouldCreateQueryWithZeroLimit() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        LocalDateTime from = LocalDateTime.now().minusHours(1);
        LocalDateTime to = LocalDateTime.now();
        int limit = 0;

        // When
        GetFailedAttemptsQuery query = new GetFailedAttemptsQuery(sessionId, userId, from, to, limit);

        // Then
        assertThat(query.limit()).isEqualTo(0);
    }

    @Test
    @DisplayName("최대 제한 개수로 쿼리를 생성할 수 있다")
    void shouldCreateQueryWithMaxLimit() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        LocalDateTime from = LocalDateTime.now().minusHours(1);
        LocalDateTime to = LocalDateTime.now();
        int limit = 1000;

        // When
        GetFailedAttemptsQuery query = new GetFailedAttemptsQuery(sessionId, userId, from, to, limit);

        // Then
        assertThat(query.limit()).isEqualTo(1000);
    }

    @Test
    @DisplayName("null 시작 시간으로 쿼리 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenFromIsNull() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        LocalDateTime from = null;
        LocalDateTime to = LocalDateTime.now();
        int limit = 10;

        // When & Then
        assertThatThrownBy(() -> new GetFailedAttemptsQuery(sessionId, userId, from, to, limit))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("From time cannot be null");
    }

    @Test
    @DisplayName("null 종료 시간으로 쿼리 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenToIsNull() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        LocalDateTime from = LocalDateTime.now().minusHours(1);
        LocalDateTime to = null;
        int limit = 10;

        // When & Then
        assertThatThrownBy(() -> new GetFailedAttemptsQuery(sessionId, userId, from, to, limit))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("To time cannot be null");
    }

    @Test
    @DisplayName("시작 시간과 종료 시간이 같을 때 쿼리를 생성할 수 있다")
    void shouldCreateQueryWhenFromEqualsTo() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        LocalDateTime sameTime = LocalDateTime.now();
        int limit = 10;

        // When
        GetFailedAttemptsQuery query = new GetFailedAttemptsQuery(sessionId, userId, sameTime, sameTime, limit);

        // Then
        assertThat(query.from()).isEqualTo(sameTime);
        assertThat(query.to()).isEqualTo(sameTime);
    }

    @Test
    @DisplayName("동등성 비교가 올바르게 동작한다")
    void shouldCompareEquality() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        LocalDateTime from = LocalDateTime.of(2024, 1, 1, 12, 0);
        LocalDateTime to = LocalDateTime.of(2024, 1, 1, 13, 0);
        int limit = 10;

        GetFailedAttemptsQuery query1 = new GetFailedAttemptsQuery(sessionId, userId, from, to, limit);
        GetFailedAttemptsQuery query2 = new GetFailedAttemptsQuery(sessionId, userId, from, to, limit);

        // When & Then
        assertThat(query1).isEqualTo(query2);
        assertThat(query1.hashCode()).isEqualTo(query2.hashCode());
    }

    @Test
    @DisplayName("서로 다른 값을 가진 쿼리는 동등하지 않다")
    void shouldNotBeEqualWithDifferentValues() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId1 = "testUser123";
        String userId2 = "testUser456";
        LocalDateTime from = LocalDateTime.now().minusHours(1);
        LocalDateTime to = LocalDateTime.now();
        int limit = 10;

        GetFailedAttemptsQuery query1 = new GetFailedAttemptsQuery(sessionId, userId1, from, to, limit);
        GetFailedAttemptsQuery query2 = new GetFailedAttemptsQuery(sessionId, userId2, from, to, limit);

        // When & Then
        assertThat(query1).isNotEqualTo(query2);
    }

    @Test
    @DisplayName("toString이 적절한 정보를 반환한다")
    void shouldReturnAppropriateToString() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        LocalDateTime from = LocalDateTime.now().minusHours(1);
        LocalDateTime to = LocalDateTime.now();
        int limit = 10;

        GetFailedAttemptsQuery query = new GetFailedAttemptsQuery(sessionId, userId, from, to, limit);

        // When
        String result = query.toString();

        // Then
        assertThat(result).contains("GetFailedAttemptsQuery");
        assertThat(result).contains(sessionId);
        assertThat(result).contains(userId);
        assertThat(result).contains(String.valueOf(limit));
    }

    @Test
    @DisplayName("빈 사용자 ID로 쿼리를 생성할 수 있다")
    void shouldCreateQueryWithEmptyUserId() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "";
        LocalDateTime from = LocalDateTime.now().minusHours(1);
        LocalDateTime to = LocalDateTime.now();
        int limit = 10;

        // When
        GetFailedAttemptsQuery query = new GetFailedAttemptsQuery(sessionId, userId, from, to, limit);

        // Then
        assertThat(query.sessionId()).isEqualTo(sessionId);
        assertThat(query.userId()).isEqualTo("");
    }

    @Test
    @DisplayName("매우 큰 제한 개수로 쿼리를 생성할 수 있다")
    void shouldCreateQueryWithVeryLargeLimit() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";
        LocalDateTime from = LocalDateTime.now().minusHours(1);
        LocalDateTime to = LocalDateTime.now();
        int limit = Integer.MAX_VALUE;

        // When
        GetFailedAttemptsQuery query = new GetFailedAttemptsQuery(sessionId, userId, from, to, limit);

        // Then
        assertThat(query.limit()).isEqualTo(Integer.MAX_VALUE);
    }
}