package com.dx.hexacore.security.session.application.query.port.in;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("GetSessionStatusQuery 테스트")
class GetSessionStatusQueryTest {

    @Test
    @DisplayName("유효한 세션 ID로 쿼리를 생성할 수 있다")
    void shouldCreateQueryWithValidSessionId() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";

        // When
        GetSessionStatusQuery query = new GetSessionStatusQuery(sessionId, null);

        // Then
        assertThat(query.sessionId()).isEqualTo(sessionId);
        assertThat(query.userId()).isNull();
    }

    @Test
    @DisplayName("세션 ID와 사용자 ID로 쿼리를 생성할 수 있다")
    void shouldCreateQueryWithSessionIdAndUserId() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";

        // When
        GetSessionStatusQuery query = new GetSessionStatusQuery(sessionId, userId);

        // Then
        assertThat(query.sessionId()).isEqualTo(sessionId);
        assertThat(query.userId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("null 세션 ID로 쿼리 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenSessionIdIsNull() {
        // Given
        String sessionId = null;
        String userId = "testUser123";

        // When & Then
        assertThatThrownBy(() -> new GetSessionStatusQuery(sessionId, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("SessionId cannot be null or empty");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    @DisplayName("빈 세션 ID로 쿼리 생성 시 예외가 발생한다")
    void shouldThrowExceptionWhenSessionIdIsEmpty(String sessionId) {
        // Given
        String userId = "testUser123";

        // When & Then
        assertThatThrownBy(() -> new GetSessionStatusQuery(sessionId, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("SessionId cannot be null or empty");
    }

    @Test
    @DisplayName("빈 사용자 ID는 허용된다")
    void shouldAllowEmptyUserId() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "";

        // When
        GetSessionStatusQuery query = new GetSessionStatusQuery(sessionId, userId);

        // Then
        assertThat(query.sessionId()).isEqualTo(sessionId);
        assertThat(query.userId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("동등성 비교가 올바르게 동작한다")
    void shouldCompareEquality() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";

        GetSessionStatusQuery query1 = new GetSessionStatusQuery(sessionId, userId);
        GetSessionStatusQuery query2 = new GetSessionStatusQuery(sessionId, userId);

        // When & Then
        assertThat(query1).isEqualTo(query2);
        assertThat(query1.hashCode()).isEqualTo(query2.hashCode());
    }

    @Test
    @DisplayName("서로 다른 값을 가진 쿼리는 동등하지 않다")
    void shouldNotBeEqualWithDifferentValues() {
        // Given
        String sessionId1 = "550e8400-e29b-41d4-a716-446655440000";
        String sessionId2 = "550e8400-e29b-41d4-a716-446655440001";
        String userId = "testUser123";

        GetSessionStatusQuery query1 = new GetSessionStatusQuery(sessionId1, userId);
        GetSessionStatusQuery query2 = new GetSessionStatusQuery(sessionId2, userId);

        // When & Then
        assertThat(query1).isNotEqualTo(query2);
    }

    @Test
    @DisplayName("toString이 적절한 정보를 반환한다")
    void shouldReturnAppropriateToString() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "testUser123";

        GetSessionStatusQuery query = new GetSessionStatusQuery(sessionId, userId);

        // When
        String result = query.toString();

        // Then
        assertThat(result).contains("GetSessionStatusQuery");
        assertThat(result).contains(sessionId);
        assertThat(result).contains(userId);
    }

    @Test
    @DisplayName("특수 문자가 포함된 세션 ID로 쿼리를 생성할 수 있다")
    void shouldCreateQueryWithSpecialCharacterSessionId() {
        // Given
        String sessionId = "session-123_abc@domain.com";
        String userId = "testUser123";

        // When
        GetSessionStatusQuery query = new GetSessionStatusQuery(sessionId, userId);

        // Then
        assertThat(query.sessionId()).isEqualTo(sessionId);
        assertThat(query.userId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("특수 문자가 포함된 사용자 ID로 쿼리를 생성할 수 있다")
    void shouldCreateQueryWithSpecialCharacterUserId() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = "user@example.com";

        // When
        GetSessionStatusQuery query = new GetSessionStatusQuery(sessionId, userId);

        // Then
        assertThat(query.sessionId()).isEqualTo(sessionId);
        assertThat(query.userId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("긴 세션 ID로 쿼리를 생성할 수 있다")
    void shouldCreateQueryWithLongSessionId() {
        // Given
        String sessionId = "very-long-session-id-with-multiple-parts-and-special-characters-123456789-abcdef";
        String userId = "testUser123";

        // When
        GetSessionStatusQuery query = new GetSessionStatusQuery(sessionId, userId);

        // Then
        assertThat(query.sessionId()).isEqualTo(sessionId);
        assertThat(query.userId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("null 사용자 ID로 쿼리를 생성할 수 있다")
    void shouldCreateQueryWithNullUserId() {
        // Given
        String sessionId = "550e8400-e29b-41d4-a716-446655440000";
        String userId = null;

        // When
        GetSessionStatusQuery query = new GetSessionStatusQuery(sessionId, userId);

        // Then
        assertThat(query.sessionId()).isEqualTo(sessionId);
        assertThat(query.userId()).isNull();
    }
}