package com.dx.hexacore.security.auth.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Credentials Value Object 테스트.
 */
@DisplayName("Credentials 값 객체")
class CredentialsTest {

    @Test
    @DisplayName("유효한 username과 password로 Credentials 생성에 성공한다")
    void shouldCreateCredentialsWhenValidUsernameAndPassword() {
        // Given
        String username = "testuser";
        String password = "password123";

        // When
        Credentials credentials = Credentials.of(username, password);

        // Then
        assertThat(credentials).isNotNull();
        assertThat(credentials.getUsername()).isEqualTo(username);
        assertThat(credentials.getPassword()).isEqualTo(password);
    }

    @Test
    @DisplayName("username 최소 길이(3자)로 생성에 성공한다")
    void shouldCreateCredentialsWhenUsernameMinLength() {
        // Given
        String username = "abc";
        String password = "password123";

        // When
        Credentials credentials = Credentials.of(username, password);

        // Then
        assertThat(credentials).isNotNull();
        assertThat(credentials.getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("username 최대 길이(50자)로 생성에 성공한다")
    void shouldCreateCredentialsWhenUsernameMaxLength() {
        // Given
        String username = "a".repeat(50);
        String password = "password123";

        // When
        Credentials credentials = Credentials.of(username, password);

        // Then
        assertThat(credentials).isNotNull();
        assertThat(credentials.getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("password 최소 길이(8자)로 생성에 성공한다")
    void shouldCreateCredentialsWhenPasswordMinLength() {
        // Given
        String username = "testuser";
        String password = "12345678";

        // When
        Credentials credentials = Credentials.of(username, password);

        // Then
        assertThat(credentials).isNotNull();
        assertThat(credentials.getPassword()).isEqualTo(password);
    }

    @ParameterizedTest
    @DisplayName("username에 영문, 숫자, 언더스코어 조합으로 생성에 성공한다")
    @ValueSource(strings = {"user123", "test_user", "user_123", "USER123", "Test_User_123"})
    void shouldCreateCredentialsWhenValidUsernamePattern(String username) {
        // Given
        String password = "password123";

        // When
        Credentials credentials = Credentials.of(username, password);

        // Then
        assertThat(credentials).isNotNull();
        assertThat(credentials.getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("동일한 값으로 생성된 Credentials는 equals true를 반환한다")
    void shouldReturnTrueWhenSameCredentials() {
        // Given
        String username = "testuser";
        String password = "password123";
        Credentials credentials1 = Credentials.of(username, password);
        Credentials credentials2 = Credentials.of(username, password);

        // When & Then
        assertThat(credentials1).isEqualTo(credentials2);
    }

    @Test
    @DisplayName("다른 값으로 생성된 Credentials는 equals false를 반환한다")
    void shouldReturnFalseWhenDifferentCredentials() {
        // Given
        Credentials credentials1 = Credentials.of("user1", "password123");
        Credentials credentials2 = Credentials.of("user2", "password123");

        // When & Then
        assertThat(credentials1).isNotEqualTo(credentials2);
    }

    @Test
    @DisplayName("동일한 값으로 생성된 Credentials는 동일한 hashCode를 가진다")
    void shouldHaveSameHashCodeWhenSameCredentials() {
        // Given
        String username = "testuser";
        String password = "password123";
        Credentials credentials1 = Credentials.of(username, password);
        Credentials credentials2 = Credentials.of(username, password);

        // When & Then
        assertThat(credentials1.hashCode()).isEqualTo(credentials2.hashCode());
    }

    @Test
    @DisplayName("username이 null일 때 IllegalArgumentException이 발생한다")
    void shouldThrowExceptionWhenUsernameIsNull() {
        // Given
        String username = null;
        String password = "password123";

        // When & Then
        assertThatThrownBy(() -> Credentials.of(username, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username cannot be empty");
    }

    @Test
    @DisplayName("username이 empty일 때 IllegalArgumentException이 발생한다")
    void shouldThrowExceptionWhenUsernameIsEmpty() {
        // Given
        String username = "";
        String password = "password123";

        // When & Then
        assertThatThrownBy(() -> Credentials.of(username, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username cannot be empty");
    }

    @Test
    @DisplayName("username이 2자 이하일 때 IllegalArgumentException이 발생한다")
    void shouldThrowExceptionWhenUsernameIsTooShort() {
        // Given
        String username = "ab";
        String password = "password123";

        // When & Then
        assertThatThrownBy(() -> Credentials.of(username, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username must be between 3 and 50 characters");
    }

    @Test
    @DisplayName("username이 51자 이상일 때 IllegalArgumentException이 발생한다")
    void shouldThrowExceptionWhenUsernameIsTooLong() {
        // Given
        String username = "a".repeat(51);
        String password = "password123";

        // When & Then
        assertThatThrownBy(() -> Credentials.of(username, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username must be between 3 and 50 characters");
    }

    @ParameterizedTest
    @DisplayName("username에 특수문자가 포함될 때 IllegalArgumentException이 발생한다")
    @ValueSource(strings = {"user@name", "user-name", "user.name", "user name", "user#name"})
    void shouldThrowExceptionWhenUsernameContainsSpecialCharacters(String username) {
        // Given
        String password = "password123";

        // When & Then
        assertThatThrownBy(() -> Credentials.of(username, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username contains invalid characters");
    }

    @Test
    @DisplayName("password가 null일 때 IllegalArgumentException이 발생한다")
    void shouldThrowExceptionWhenPasswordIsNull() {
        // Given
        String username = "testuser";
        String password = null;

        // When & Then
        assertThatThrownBy(() -> Credentials.of(username, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password cannot be empty");
    }

    @Test
    @DisplayName("password가 empty일 때 IllegalArgumentException이 발생한다")
    void shouldThrowExceptionWhenPasswordIsEmpty() {
        // Given
        String username = "testuser";
        String password = "";

        // When & Then
        assertThatThrownBy(() -> Credentials.of(username, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password cannot be empty");
    }

    @Test
    @DisplayName("password가 7자 이하일 때 IllegalArgumentException이 발생한다")
    void shouldThrowExceptionWhenPasswordIsTooShort() {
        // Given
        String username = "testuser";
        String password = "1234567";

        // When & Then
        assertThatThrownBy(() -> Credentials.of(username, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password must be at least 8 characters");
    }
}