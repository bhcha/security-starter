package com.dx.hexacore.security.auth.application.query.port.in;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("GetAuthenticationQuery 테스트")
class GetAuthenticationQueryTest {

    @Test
    @DisplayName("유효한 인증 ID로 Query 객체 생성 성공")
    void shouldCreateQueryWithValidAuthenticationId() {
        // Given
        String authenticationId = "auth-123";

        // When
        GetAuthenticationQuery query = GetAuthenticationQuery.of(authenticationId);

        // Then
        assertThat(query).isNotNull();
        assertThat(query.getAuthenticationId()).isEqualTo(authenticationId);
    }

    @Test
    @DisplayName("정적 팩토리 메서드를 통한 Query 생성 성공")
    void shouldCreateQueryUsingStaticFactoryMethod() {
        // Given
        String authenticationId = "auth-456";

        // When
        GetAuthenticationQuery query = GetAuthenticationQuery.of(authenticationId);

        // Then
        assertThat(query).isNotNull();
        assertThat(query.getAuthenticationId()).isEqualTo(authenticationId);
    }

    @Test
    @DisplayName("Builder 패턴을 통한 Query 생성 성공")
    void shouldCreateQueryUsingBuilder() {
        // Given
        String authenticationId = "auth-789";

        // When
        GetAuthenticationQuery query = GetAuthenticationQuery.builder()
            .authenticationId(authenticationId)
            .build();

        // Then
        assertThat(query).isNotNull();
        assertThat(query.getAuthenticationId()).isEqualTo(authenticationId);
    }

    @Test
    @DisplayName("null 인증 ID로 Query 생성 시 예외 발생")
    void shouldThrowExceptionWhenAuthenticationIdIsNull() {
        // Given
        String authenticationId = null;

        // When & Then
        assertThatThrownBy(() -> GetAuthenticationQuery.of(authenticationId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Authentication ID cannot be null");
    }

    @Test
    @DisplayName("빈 문자열 인증 ID로 Query 생성 시 예외 발생")
    void shouldThrowExceptionWhenAuthenticationIdIsEmpty() {
        // Given
        String authenticationId = "";

        // When & Then
        assertThatThrownBy(() -> GetAuthenticationQuery.of(authenticationId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Authentication ID cannot be empty");
    }

    @Test
    @DisplayName("공백만 있는 인증 ID로 Query 생성 시 예외 발생")
    void shouldThrowExceptionWhenAuthenticationIdIsBlank() {
        // Given
        String authenticationId = "   ";

        // When & Then
        assertThatThrownBy(() -> GetAuthenticationQuery.of(authenticationId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Authentication ID cannot be blank");
    }

    @Test
    @DisplayName("equals/hashCode 올바른 동작 확인")
    void shouldHaveCorrectEqualsAndHashCode() {
        // Given
        String authenticationId = "auth-123";
        GetAuthenticationQuery query1 = GetAuthenticationQuery.of(authenticationId);
        GetAuthenticationQuery query2 = GetAuthenticationQuery.of(authenticationId);
        GetAuthenticationQuery query3 = GetAuthenticationQuery.of("auth-456");

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
        String authenticationId = "auth-123";
        GetAuthenticationQuery query = GetAuthenticationQuery.of(authenticationId);

        // When
        String result = query.toString();

        // Then
        assertThat(result).contains("GetAuthenticationQuery");
        assertThat(result).contains(authenticationId);
    }

    @Test
    @DisplayName("Getter 메서드 올바른 값 반환 확인")
    void shouldReturnCorrectValuesFromGetters() {
        // Given
        String authenticationId = "auth-123";
        GetAuthenticationQuery query = GetAuthenticationQuery.of(authenticationId);

        // When & Then
        assertThat(query.getAuthenticationId()).isEqualTo(authenticationId);
        assertThat(query.getAuthenticationId()).isNotNull();
        assertThat(query.getAuthenticationId()).isNotEmpty();
    }
}