package com.dx.hexacore.security.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

/**
 * ValidationMessages 클래스 테스트.
 */
@DisplayName("ValidationMessages 유틸리티")
class ValidationMessagesTest {

    @Test
    @DisplayName("클래스를 인스턴스화할 수 없어야 한다")
    void shouldNotBeInstantiable() {
        // When & Then
        assertThatThrownBy(() -> {
            var constructor = ValidationMessages.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        })
        .hasRootCauseInstanceOf(UnsupportedOperationException.class)
        .hasRootCauseMessage("Utility class cannot be instantiated");
    }

    // ===== NULL/EMPTY 관련 메시지 테스트 =====

    @ParameterizedTest
    @CsvSource({
        "Username, 'Username cannot be null or empty'",
        "Password, 'Password cannot be null or empty'",
        "Email, 'Email cannot be null or empty'"
    })
    @DisplayName("cannotBeNullOrEmpty 메서드는 올바른 메시지를 생성한다")
    void shouldGenerateCannotBeNullOrEmptyMessage(String fieldName, String expectedMessage) {
        // When
        String actualMessage = ValidationMessages.cannotBeNullOrEmpty(fieldName);

        // Then
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "SessionId, 'SessionId cannot be null'",
        "ClientIp, 'ClientIp cannot be null'",
        "Token, 'Token cannot be null'"
    })
    @DisplayName("cannotBeNull 메서드는 올바른 메시지를 생성한다")
    void shouldGenerateCannotBeNullMessage(String fieldName, String expectedMessage) {
        // When
        String actualMessage = ValidationMessages.cannotBeNull(fieldName);

        // Then
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    // ===== 범위 관련 메시지 테스트 =====

    @ParameterizedTest
    @CsvSource({
        "Age, 18, 65, 'Age must be between 18 and 65'",
        "Score, 0, 100, 'Score must be between 0 and 100'",
        "Priority, 1, 10, 'Priority must be between 1 and 10'"
    })
    @DisplayName("mustBeBetween 메서드는 올바른 숫자 범위 메시지를 생성한다")
    void shouldGenerateMustBeBetweenMessage(String fieldName, long min, long max, String expectedMessage) {
        // When
        String actualMessage = ValidationMessages.mustBeBetween(fieldName, min, max);

        // Then
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "Username, 3, 50, 'Username must be between 3 and 50 characters'",
        "Password, 8, 128, 'Password must be between 8 and 128 characters'",
        "Title, 1, 255, 'Title must be between 1 and 255 characters'"
    })
    @DisplayName("mustBeBetweenCharacters 메서드는 올바른 길이 범위 메시지를 생성한다")
    void shouldGenerateMustBeBetweenCharactersMessage(String fieldName, int minLength, int maxLength, String expectedMessage) {
        // When
        String actualMessage = ValidationMessages.mustBeBetweenCharacters(fieldName, minLength, maxLength);

        // Then
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "Password, 8, 'Password must be at least 8 characters'",
        "Description, 10, 'Description must be at least 10 characters'",
        "Comment, 5, 'Comment must be at least 5 characters'"
    })
    @DisplayName("mustBeAtLeastCharacters 메서드는 올바른 최소 길이 메시지를 생성한다")
    void shouldGenerateMustBeAtLeastCharactersMessage(String fieldName, int minLength, String expectedMessage) {
        // When
        String actualMessage = ValidationMessages.mustBeAtLeastCharacters(fieldName, minLength);

        // Then
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "Count, 'Count must be positive'",
        "Amount, 'Amount must be positive'",
        "Quantity, 'Quantity must be positive'"
    })
    @DisplayName("mustBePositive 메서드는 올바른 양수 메시지를 생성한다")
    void shouldGenerateMustBePositiveMessage(String fieldName, String expectedMessage) {
        // When
        String actualMessage = ValidationMessages.mustBePositive(fieldName);

        // Then
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    // ===== 형식 관련 메시지 테스트 =====

    @ParameterizedTest
    @CsvSource({
        "Email, 'invalid-email', 'Invalid Email format: invalid-email'",
        "UUID, '123-456', 'Invalid UUID format: 123-456'",
        "IP address, '999.999.999.999', 'Invalid IP address format: 999.999.999.999'"
    })
    @DisplayName("invalidFormat 메서드는 올바른 형식 오류 메시지를 생성한다 (값 포함)")
    void shouldGenerateInvalidFormatMessageWithValue(String fieldName, String value, String expectedMessage) {
        // When
        String actualMessage = ValidationMessages.invalidFormat(fieldName, value);

        // Then
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "Email, 'Invalid Email format'",
        "UUID, 'Invalid UUID format'",
        "Phone, 'Invalid Phone format'"
    })
    @DisplayName("invalidFormat 메서드는 올바른 형식 오류 메시지를 생성한다 (값 미포함)")
    void shouldGenerateInvalidFormatMessageWithoutValue(String fieldName, String expectedMessage) {
        // When
        String actualMessage = ValidationMessages.invalidFormat(fieldName);

        // Then
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "Username, 'Username contains invalid characters'",
        "Password, 'Password contains invalid characters'",
        "Filename, 'Filename contains invalid characters'"
    })
    @DisplayName("containsInvalidCharacters 메서드는 올바른 메시지를 생성한다")
    void shouldGenerateContainsInvalidCharactersMessage(String fieldName, String expectedMessage) {
        // When
        String actualMessage = ValidationMessages.containsInvalidCharacters(fieldName);

        // Then
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    // ===== 비즈니스 규칙 관련 메시지 테스트 =====

    @Test
    @DisplayName("TIME_RANGE_INVALID 상수는 올바른 값을 가진다")
    void shouldHaveCorrectTimeRangeInvalidMessage() {
        // When
        String message = ValidationMessages.TIME_RANGE_INVALID;

        // Then
        assertThat(message).isEqualTo("From time cannot be after to time");
    }

    @ParameterizedTest
    @CsvSource({
        "Expiry date, 'Expiry date must be in the future'",
        "Start time, 'Start time must be in the future'",
        "Deadline, 'Deadline must be in the future'"
    })
    @DisplayName("mustBeInFuture 메서드는 올바른 미래 시간 메시지를 생성한다")
    void shouldGenerateMustBeInFutureMessage(String fieldName, String expectedMessage) {
        // When
        String actualMessage = ValidationMessages.mustBeInFuture(fieldName);

        // Then
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    @Test
    @DisplayName("alreadyExists 메서드는 올바른 중복 존재 메시지를 생성한다")
    void shouldGenerateAlreadyExistsMessage() {
        // When
        String usernameMessage = ValidationMessages.alreadyExists("Username", "testuser");
        String emailMessage = ValidationMessages.alreadyExists("Email", "test@example.com");
        String idMessage = ValidationMessages.alreadyExists("ID", "12345");

        // Then
        assertThat(usernameMessage).isEqualTo("Username 'testuser' already exists");
        assertThat(emailMessage).isEqualTo("Email 'test@example.com' already exists");
        assertThat(idMessage).isEqualTo("ID '12345' already exists");
    }

    // ===== 메시지 상수 테스트 =====

    @Test
    @DisplayName("모든 메시지 상수는 null이 아니어야 한다")
    void shouldHaveNonNullMessageConstants() {
        // When & Then
        assertThat(ValidationMessages.CANNOT_BE_NULL_OR_EMPTY).isNotNull().isNotEmpty();
        assertThat(ValidationMessages.CANNOT_BE_NULL).isNotNull().isNotEmpty();
        assertThat(ValidationMessages.MUST_BE_BETWEEN_NUMERIC).isNotNull().isNotEmpty();
        assertThat(ValidationMessages.MUST_BE_BETWEEN_CHARACTERS).isNotNull().isNotEmpty();
        assertThat(ValidationMessages.MUST_BE_AT_LEAST_CHARACTERS).isNotNull().isNotEmpty();
        assertThat(ValidationMessages.MUST_BE_POSITIVE).isNotNull().isNotEmpty();
        assertThat(ValidationMessages.INVALID_FORMAT_WITH_VALUE).isNotNull().isNotEmpty();
        assertThat(ValidationMessages.INVALID_FORMAT).isNotNull().isNotEmpty();
        assertThat(ValidationMessages.CONTAINS_INVALID_CHARACTERS).isNotNull().isNotEmpty();
        assertThat(ValidationMessages.TIME_RANGE_INVALID).isNotNull().isNotEmpty();
        assertThat(ValidationMessages.MUST_BE_IN_FUTURE).isNotNull().isNotEmpty();
        assertThat(ValidationMessages.ALREADY_EXISTS).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("메시지 템플릿은 올바른 포맷 지시자를 포함해야 한다")
    void shouldHaveCorrectFormatSpecifiers() {
        // When & Then
        assertThat(ValidationMessages.CANNOT_BE_NULL_OR_EMPTY).contains("%s");
        assertThat(ValidationMessages.CANNOT_BE_NULL).contains("%s");
        assertThat(ValidationMessages.MUST_BE_BETWEEN_NUMERIC).contains("%s", "%d", "%d");
        assertThat(ValidationMessages.MUST_BE_BETWEEN_CHARACTERS).contains("%s", "%d", "%d");
        assertThat(ValidationMessages.MUST_BE_AT_LEAST_CHARACTERS).contains("%s", "%d");
        assertThat(ValidationMessages.MUST_BE_POSITIVE).contains("%s");
        assertThat(ValidationMessages.INVALID_FORMAT_WITH_VALUE).contains("%s", "%s");
        assertThat(ValidationMessages.INVALID_FORMAT).contains("%s");
        assertThat(ValidationMessages.CONTAINS_INVALID_CHARACTERS).contains("%s");
        assertThat(ValidationMessages.MUST_BE_IN_FUTURE).contains("%s");
        assertThat(ValidationMessages.ALREADY_EXISTS).contains("%s", "%s");
    }

    @ParameterizedTest
    @CsvSource({
        "Username, 'Username cannot be empty'",
        "Password, 'Password cannot be empty'",
        "Email, 'Email cannot be empty'"
    })
    @DisplayName("cannotBeEmpty 메서드는 올바른 메시지를 생성한다")
    void shouldGenerateCannotBeEmptyMessage(String fieldName, String expectedMessage) {
        // When
        String actualMessage = ValidationMessages.cannotBeEmpty(fieldName);

        // Then
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    @ParameterizedTest
    @CsvSource({
        "Username, 'Username cannot be blank'",
        "Password, 'Password cannot be blank'",
        "Email, 'Email cannot be blank'"
    })
    @DisplayName("cannotBeBlank 메서드는 올바른 메시지를 생성한다")
    void shouldGenerateCannotBeBlankMessage(String fieldName, String expectedMessage) {
        // When
        String actualMessage = ValidationMessages.cannotBeBlank(fieldName);

        // Then
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    @Test
    @DisplayName("유틸리티 메서드는 null 입력에 대해 적절히 처리해야 한다")
    void shouldHandleNullInputsGracefully() {
        // When & Then - null 필드명에 대해서는 String.format이 "null"로 처리
        assertThat(ValidationMessages.cannotBeNullOrEmpty(null)).isEqualTo("null cannot be null or empty");
        assertThat(ValidationMessages.cannotBeNull(null)).isEqualTo("null cannot be null");
        assertThat(ValidationMessages.cannotBeEmpty(null)).isEqualTo("null cannot be empty");
        assertThat(ValidationMessages.cannotBeBlank(null)).isEqualTo("null cannot be blank");
        assertThat(ValidationMessages.mustBePositive(null)).isEqualTo("null must be positive");
        assertThat(ValidationMessages.containsInvalidCharacters(null)).isEqualTo("null contains invalid characters");
        assertThat(ValidationMessages.invalidFormat(null)).isEqualTo("Invalid null format");
        assertThat(ValidationMessages.invalidFormat(null, "value")).isEqualTo("Invalid null format: value");
        assertThat(ValidationMessages.mustBeInFuture(null)).isEqualTo("null must be in the future");
        assertThat(ValidationMessages.alreadyExists(null, "value")).isEqualTo("null 'value' already exists");
    }
}