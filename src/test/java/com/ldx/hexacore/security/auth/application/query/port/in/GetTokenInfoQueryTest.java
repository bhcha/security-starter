package com.ldx.hexacore.security.auth.application.query.port.in;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("GetTokenInfoQuery 테스트")
class GetTokenInfoQueryTest {

    @Test
    @DisplayName("유효한 토큰으로 Query 객체 생성 성공")
    void shouldCreateQueryWithValidToken() {
        // Given
        String token = "valid-access-token";

        // When
        GetTokenInfoQuery query = GetTokenInfoQuery.of(token);

        // Then
        assertThat(query).isNotNull();
        assertThat(query.getToken()).isEqualTo(token);
    }

    @Test
    @DisplayName("JWT 형식 토큰으로 Query 생성 성공")
    void shouldCreateQueryWithJwtToken() {
        // Given
        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

        // When
        GetTokenInfoQuery query = GetTokenInfoQuery.of(jwtToken);

        // Then
        assertThat(query).isNotNull();
        assertThat(query.getToken()).isEqualTo(jwtToken);
    }

    @Test
    @DisplayName("긴 토큰 문자열로 Query 생성 성공")
    void shouldCreateQueryWithLongToken() {
        // Given
        String longToken = "a".repeat(1000); // 1000자 토큰

        // When
        GetTokenInfoQuery query = GetTokenInfoQuery.of(longToken);

        // Then
        assertThat(query).isNotNull();
        assertThat(query.getToken()).isEqualTo(longToken);
    }

    @Test
    @DisplayName("null 토큰으로 Query 생성 시 예외 발생")
    void shouldThrowExceptionWhenTokenIsNull() {
        // Given
        String token = null;

        // When & Then
        assertThatThrownBy(() -> GetTokenInfoQuery.of(token))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Token cannot be null");
    }

    @Test
    @DisplayName("빈 문자열 토큰으로 Query 생성 시 예외 발생")
    void shouldThrowExceptionWhenTokenIsEmpty() {
        // Given
        String token = "";

        // When & Then
        assertThatThrownBy(() -> GetTokenInfoQuery.of(token))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Token cannot be empty");
    }

    @Test
    @DisplayName("공백만 있는 토큰으로 Query 생성 시 예외 발생")
    void shouldThrowExceptionWhenTokenIsBlank() {
        // Given
        String token = "   ";

        // When & Then
        assertThatThrownBy(() -> GetTokenInfoQuery.of(token))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Token cannot be blank");
    }

    @Test
    @DisplayName("equals/hashCode 올바른 동작 확인")
    void shouldHaveCorrectEqualsAndHashCode() {
        // Given
        String token = "valid-token";
        GetTokenInfoQuery query1 = GetTokenInfoQuery.of(token);
        GetTokenInfoQuery query2 = GetTokenInfoQuery.of(token);
        GetTokenInfoQuery query3 = GetTokenInfoQuery.of("different-token");

        // When & Then
        assertThat(query1).isEqualTo(query2);
        assertThat(query1).isNotEqualTo(query3);
        assertThat(query1.hashCode()).isEqualTo(query2.hashCode());
        assertThat(query1.hashCode()).isNotEqualTo(query3.hashCode());
    }

    @Test
    @DisplayName("toString 메서드 올바른 출력 확인")
    void shouldHaveCorrectToString() {
        // Given
        String token = "test-token";
        GetTokenInfoQuery query = GetTokenInfoQuery.of(token);

        // When
        String result = query.toString();

        // Then
        assertThat(result).contains("GetTokenInfoQuery");
        assertThat(result).contains("test-token");
    }

    @Test
    @DisplayName("Getter 메서드 올바른 값 반환 확인")
    void shouldReturnCorrectValuesFromGetters() {
        // Given
        String token = "test-token";
        GetTokenInfoQuery query = GetTokenInfoQuery.of(token);

        // When & Then
        assertThat(query.getToken()).isEqualTo(token);
        assertThat(query.getToken()).isNotNull();
        assertThat(query.getToken()).isNotEmpty();
    }
}