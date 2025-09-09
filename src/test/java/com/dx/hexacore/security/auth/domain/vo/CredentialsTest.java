package com.dx.hexacore.security.auth.domain.vo;

import org.junit.jupiter.api.BeforeEach;
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

    private com.dx.hexacore.security.config.SecurityConstants securityConstants;
    
    @BeforeEach
    void setUp() {
        securityConstants = new com.dx.hexacore.security.config.SecurityConstants();
    }

    @Test
    @DisplayName("유효한 username과 password로 Credentials 생성에 성공한다")
    void shouldCreateCredentialsWhenValidUsernameAndPassword() {
        // Given
        String username = "testuser";
        String password = "password123";

        // When
        Credentials credentials = Credentials.of(username, password,
                                                securityConstants.getValidation().getMinUsernameLength(),
                                                securityConstants.getValidation().getMaxUsernameLength(),
                                                securityConstants.getValidation().getMinPasswordLength());

        // Then
        assertThat(credentials).isNotNull();
        assertThat(credentials.getUsername()).isEqualTo(username);
        assertThat(credentials.getPassword()).isEqualTo(password);
    }

    @Test
    @DisplayName("username 최소 길이로 생성에 성공한다")
    void shouldCreateCredentialsWhenUsernameMinLength() {
        // Given
        String username = "a".repeat(securityConstants.getValidation().getMinUsernameLength());
        String password = "password123";

        // When
        Credentials credentials = Credentials.of(username, password,
                                                securityConstants.getValidation().getMinUsernameLength(),
                                                securityConstants.getValidation().getMaxUsernameLength(),
                                                securityConstants.getValidation().getMinPasswordLength());

        // Then
        assertThat(credentials).isNotNull();
        assertThat(credentials.getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("username 최대 길이로 생성에 성공한다")
    void shouldCreateCredentialsWhenUsernameMaxLength() {
        // Given
        String username = "a".repeat(securityConstants.getValidation().getMaxUsernameLength());
        String password = "password123";

        // When
        Credentials credentials = Credentials.of(username, password,
                                                securityConstants.getValidation().getMinUsernameLength(),
                                                securityConstants.getValidation().getMaxUsernameLength(),
                                                securityConstants.getValidation().getMinPasswordLength());

        // Then
        assertThat(credentials).isNotNull();
        assertThat(credentials.getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("password 최소 길이로 생성에 성공한다")
    void shouldCreateCredentialsWhenPasswordMinLength() {
        // Given
        String username = "testuser";
        String password = "1".repeat(securityConstants.getValidation().getMinPasswordLength());

        // When
        Credentials credentials = Credentials.of(username, password,
                                                securityConstants.getValidation().getMinUsernameLength(),
                                                securityConstants.getValidation().getMaxUsernameLength(),
                                                securityConstants.getValidation().getMinPasswordLength());

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
        Credentials credentials = Credentials.of(username, password,
                                                securityConstants.getValidation().getMinUsernameLength(),
                                                securityConstants.getValidation().getMaxUsernameLength(),
                                                securityConstants.getValidation().getMinPasswordLength());

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
        Credentials credentials1 = Credentials.of(username, password,
                                                securityConstants.getValidation().getMinUsernameLength(),
                                                securityConstants.getValidation().getMaxUsernameLength(),
                                                securityConstants.getValidation().getMinPasswordLength());
        Credentials credentials2 = Credentials.of(username, password,
                                                securityConstants.getValidation().getMinUsernameLength(),
                                                securityConstants.getValidation().getMaxUsernameLength(),
                                                securityConstants.getValidation().getMinPasswordLength());

        // When & Then
        assertThat(credentials1).isEqualTo(credentials2);
    }

    @Test
    @DisplayName("다른 값으로 생성된 Credentials는 equals false를 반환한다")
    void shouldReturnFalseWhenDifferentCredentials() {
        // Given
        Credentials credentials1 = Credentials.of("user1", "password123",
                                                 securityConstants.getValidation().getMinUsernameLength(),
                                                 securityConstants.getValidation().getMaxUsernameLength(),
                                                 securityConstants.getValidation().getMinPasswordLength());
        Credentials credentials2 = Credentials.of("user2", "password123",
                                                 securityConstants.getValidation().getMinUsernameLength(),
                                                 securityConstants.getValidation().getMaxUsernameLength(),
                                                 securityConstants.getValidation().getMinPasswordLength());

        // When & Then
        assertThat(credentials1).isNotEqualTo(credentials2);
    }

    @Test
    @DisplayName("동일한 값으로 생성된 Credentials는 동일한 hashCode를 가진다")
    void shouldHaveSameHashCodeWhenSameCredentials() {
        // Given
        String username = "testuser";
        String password = "password123";
        Credentials credentials1 = Credentials.of(username, password,
                                                securityConstants.getValidation().getMinUsernameLength(),
                                                securityConstants.getValidation().getMaxUsernameLength(),
                                                securityConstants.getValidation().getMinPasswordLength());
        Credentials credentials2 = Credentials.of(username, password,
                                                securityConstants.getValidation().getMinUsernameLength(),
                                                securityConstants.getValidation().getMaxUsernameLength(),
                                                securityConstants.getValidation().getMinPasswordLength());

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
        assertThatThrownBy(() -> Credentials.of(username, password,
                                               securityConstants.getValidation().getMinUsernameLength(),
                                               securityConstants.getValidation().getMaxUsernameLength(),
                                               securityConstants.getValidation().getMinPasswordLength()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username cannot be null or empty");
    }

    @Test
    @DisplayName("username이 empty일 때 IllegalArgumentException이 발생한다")
    void shouldThrowExceptionWhenUsernameIsEmpty() {
        // Given
        String username = "";
        String password = "password123";

        // When & Then
        assertThatThrownBy(() -> Credentials.of(username, password,
                                               securityConstants.getValidation().getMinUsernameLength(),
                                               securityConstants.getValidation().getMaxUsernameLength(),
                                               securityConstants.getValidation().getMinPasswordLength()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username cannot be null or empty");
    }

    @Test
    @DisplayName("username이 최소 길이보다 짧을 때 IllegalArgumentException이 발생한다")
    void shouldThrowExceptionWhenUsernameIsTooShort() {
        // Given
        String username = "a".repeat(securityConstants.getValidation().getMinUsernameLength() - 1);
        String password = "password123";

        // When & Then
        assertThatThrownBy(() -> Credentials.of(username, password,
                                               securityConstants.getValidation().getMinUsernameLength(),
                                               securityConstants.getValidation().getMaxUsernameLength(),
                                               securityConstants.getValidation().getMinPasswordLength()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(String.format("Username length must be between %d and %d",
                                     securityConstants.getValidation().getMinUsernameLength(),
                                     securityConstants.getValidation().getMaxUsernameLength()));
    }

    @Test
    @DisplayName("username이 최대 길이보다 길 때 IllegalArgumentException이 발생한다")
    void shouldThrowExceptionWhenUsernameIsTooLong() {
        // Given
        String username = "a".repeat(securityConstants.getValidation().getMaxUsernameLength() + 1);
        String password = "password123";

        // When & Then
        assertThatThrownBy(() -> Credentials.of(username, password,
                                               securityConstants.getValidation().getMinUsernameLength(),
                                               securityConstants.getValidation().getMaxUsernameLength(),
                                               securityConstants.getValidation().getMinPasswordLength()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(String.format("Username length must be between %d and %d",
                                     securityConstants.getValidation().getMinUsernameLength(),
                                     securityConstants.getValidation().getMaxUsernameLength()));
    }

    @ParameterizedTest
    @DisplayName("username에 특수문자가 포함될 때 IllegalArgumentException이 발생한다")
    @ValueSource(strings = {"user@name", "user-name", "user.name", "user name", "user#name"})
    void shouldThrowExceptionWhenUsernameContainsSpecialCharacters(String username) {
        // Given
        String password = "password123";

        // When & Then
        assertThatThrownBy(() -> Credentials.of(username, password,
                                               securityConstants.getValidation().getMinUsernameLength(),
                                               securityConstants.getValidation().getMaxUsernameLength(),
                                               securityConstants.getValidation().getMinPasswordLength()))
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
        assertThatThrownBy(() -> Credentials.of(username, password,
                                               securityConstants.getValidation().getMinUsernameLength(),
                                               securityConstants.getValidation().getMaxUsernameLength(),
                                               securityConstants.getValidation().getMinPasswordLength()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password cannot be null or empty");
    }

    @Test
    @DisplayName("password가 empty일 때 IllegalArgumentException이 발생한다")
    void shouldThrowExceptionWhenPasswordIsEmpty() {
        // Given
        String username = "testuser";
        String password = "";

        // When & Then
        assertThatThrownBy(() -> Credentials.of(username, password,
                                               securityConstants.getValidation().getMinUsernameLength(),
                                               securityConstants.getValidation().getMaxUsernameLength(),
                                               securityConstants.getValidation().getMinPasswordLength()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password cannot be null or empty");
    }

    @Test
    @DisplayName("password가 최소 길이보다 짧을 때 IllegalArgumentException이 발생한다")
    void shouldThrowExceptionWhenPasswordIsTooShort() {
        // Given
        String username = "testuser";
        String password = "1".repeat(securityConstants.getValidation().getMinPasswordLength() - 1);

        // When & Then
        assertThatThrownBy(() -> Credentials.of(username, password,
                                               securityConstants.getValidation().getMinUsernameLength(),
                                               securityConstants.getValidation().getMaxUsernameLength(),
                                               securityConstants.getValidation().getMinPasswordLength()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(String.format("Password must be at least %d characters",
                                     securityConstants.getValidation().getMinPasswordLength()));
    }
}