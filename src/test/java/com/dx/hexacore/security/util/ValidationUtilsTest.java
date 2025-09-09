package com.dx.hexacore.security.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.*;

/**
 * ValidationUtils 단위 테스트
 */
@DisplayName("ValidationUtils 유틸리티 클래스")
class ValidationUtilsTest {

    @Test
    @DisplayName("ValidationUtils 인스턴스 생성을 시도하면 예외가 발생한다")
    void shouldThrowExceptionWhenTryingToInstantiate() {
        // When & Then
        assertThatThrownBy(() -> {
            // 리플렉션을 사용해 private 생성자에 접근
            var constructor = ValidationUtils.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        })
        .hasRootCauseInstanceOf(UnsupportedOperationException.class)
        .hasRootCauseMessage("Utility class cannot be instantiated");
    }

    // ===========================================
    // requireNonNullOrEmpty 테스트
    // ===========================================

    @Test
    @DisplayName("requireNonNullOrEmpty - 유효한 문자열로 성공한다")
    void requireNonNullOrEmpty_ShouldSucceedWithValidString() {
        // Given
        String validValue = "test";
        String fieldName = "testField";

        // When & Then (예외가 발생하지 않아야 함)
        assertThatNoException().isThrownBy(() -> 
            ValidationUtils.requireNonNullOrEmpty(validValue, fieldName)
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n", "\r\n"})
    @DisplayName("requireNonNullOrEmpty - null/empty/blank 문자열에 대해 예외가 발생한다")
    void requireNonNullOrEmpty_ShouldThrowExceptionForInvalidStrings(String invalidValue) {
        // Given
        String fieldName = "testField";

        // When & Then
        assertThatThrownBy(() -> ValidationUtils.requireNonNullOrEmpty(invalidValue, fieldName))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("testField cannot be null or empty");
    }

    // ===========================================
    // requireNonNull 테스트
    // ===========================================

    @Test
    @DisplayName("requireNonNull - 유효한 객체로 성공한다")
    void requireNonNull_ShouldSucceedWithValidObject() {
        // Given
        Object validValue = new Object();
        String fieldName = "testField";

        // When & Then
        assertThatNoException().isThrownBy(() -> 
            ValidationUtils.requireNonNull(validValue, fieldName)
        );
    }

    @Test
    @DisplayName("requireNonNull - null 객체에 대해 예외가 발생한다")
    void requireNonNull_ShouldThrowExceptionForNullObject() {
        // Given
        Object nullValue = null;
        String fieldName = "testField";

        // When & Then
        assertThatThrownBy(() -> ValidationUtils.requireNonNull(nullValue, fieldName))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("testField cannot be null");
    }

    // ===========================================
    // requireInRange (int) 테스트
    // ===========================================

    @Test
    @DisplayName("requireInRange(int) - 범위 내 값으로 성공한다")
    void requireInRange_Int_ShouldSucceedWithValueInRange() {
        // Given
        int value = 5;
        int min = 1;
        int max = 10;
        String fieldName = "testField";

        // When & Then
        assertThatNoException().isThrownBy(() -> 
            ValidationUtils.requireInRange(value, min, max, fieldName)
        );
    }

    @Test
    @DisplayName("requireInRange(int) - 경계값(최솟값)으로 성공한다")
    void requireInRange_Int_ShouldSucceedWithMinValue() {
        // Given
        int value = 1;
        int min = 1;
        int max = 10;
        String fieldName = "testField";

        // When & Then
        assertThatNoException().isThrownBy(() -> 
            ValidationUtils.requireInRange(value, min, max, fieldName)
        );
    }

    @Test
    @DisplayName("requireInRange(int) - 경계값(최댓값)으로 성공한다")
    void requireInRange_Int_ShouldSucceedWithMaxValue() {
        // Given
        int value = 10;
        int min = 1;
        int max = 10;
        String fieldName = "testField";

        // When & Then
        assertThatNoException().isThrownBy(() -> 
            ValidationUtils.requireInRange(value, min, max, fieldName)
        );
    }

    @Test
    @DisplayName("requireInRange(int) - 최솟값보다 작으면 예외가 발생한다")
    void requireInRange_Int_ShouldThrowExceptionWhenBelowMin() {
        // Given
        int value = 0;
        int min = 1;
        int max = 10;
        String fieldName = "testField";

        // When & Then
        assertThatThrownBy(() -> ValidationUtils.requireInRange(value, min, max, fieldName))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("testField must be between 1 and 10");
    }

    @Test
    @DisplayName("requireInRange(int) - 최댓값보다 크면 예외가 발생한다")
    void requireInRange_Int_ShouldThrowExceptionWhenAboveMax() {
        // Given
        int value = 11;
        int min = 1;
        int max = 10;
        String fieldName = "testField";

        // When & Then
        assertThatThrownBy(() -> ValidationUtils.requireInRange(value, min, max, fieldName))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("testField must be between 1 and 10");
    }

    // ===========================================
    // requireInRange (long) 테스트
    // ===========================================

    @Test
    @DisplayName("requireInRange(long) - 범위 내 값으로 성공한다")
    void requireInRange_Long_ShouldSucceedWithValueInRange() {
        // Given
        long value = 5000L;
        long min = 1000L;
        long max = 10000L;
        String fieldName = "testField";

        // When & Then
        assertThatNoException().isThrownBy(() -> 
            ValidationUtils.requireInRange(value, min, max, fieldName)
        );
    }

    @Test
    @DisplayName("requireInRange(long) - 최댓값보다 크면 예외가 발생한다")
    void requireInRange_Long_ShouldThrowExceptionWhenAboveMax() {
        // Given
        long value = 10001L;
        long min = 1000L;
        long max = 10000L;
        String fieldName = "testField";

        // When & Then
        assertThatThrownBy(() -> ValidationUtils.requireInRange(value, min, max, fieldName))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("testField must be between 1000 and 10000");
    }

    // ===========================================
    // requirePositive 테스트
    // ===========================================

    @Test
    @DisplayName("requirePositive - 양수로 성공한다")
    void requirePositive_ShouldSucceedWithPositiveValue() {
        // Given
        int value = 1;
        String fieldName = "testField";

        // When & Then
        assertThatNoException().isThrownBy(() -> 
            ValidationUtils.requirePositive(value, fieldName)
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    @DisplayName("requirePositive - 0이하 값에 대해 예외가 발생한다")
    void requirePositive_ShouldThrowExceptionForNonPositiveValues(int value) {
        // Given
        String fieldName = "testField";

        // When & Then
        assertThatThrownBy(() -> ValidationUtils.requirePositive(value, fieldName))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("testField must be positive");
    }

    // ===========================================
    // requireValidUUID 테스트
    // ===========================================

    @Test
    @DisplayName("requireValidUUID - 유효한 UUID로 성공한다")
    void requireValidUUID_ShouldSucceedWithValidUUID() {
        // Given
        String validUuid = "550e8400-e29b-41d4-a716-446655440000";
        String fieldName = "sessionId";

        // When
        UUID result = ValidationUtils.requireValidUUID(validUuid, fieldName);

        // Then
        assertThat(result).isEqualTo(UUID.fromString(validUuid));
    }

    @Test
    @DisplayName("requireValidUUID - 공백이 포함된 유효한 UUID로 성공한다")
    void requireValidUUID_ShouldSucceedWithValidUUIDWithWhitespace() {
        // Given
        String validUuidWithSpace = "  550e8400-e29b-41d4-a716-446655440000  ";
        String fieldName = "sessionId";

        // When
        UUID result = ValidationUtils.requireValidUUID(validUuidWithSpace, fieldName);

        // Then
        assertThat(result).isEqualTo(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    @DisplayName("requireValidUUID - null/empty/blank 문자열에 대해 예외가 발생한다")
    void requireValidUUID_ShouldThrowExceptionForNullOrEmpty(String invalidValue) {
        // Given
        String fieldName = "sessionId";

        // When & Then
        assertThatThrownBy(() -> ValidationUtils.requireValidUUID(invalidValue, fieldName))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("sessionId cannot be null or empty");
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-uuid", "123", "550e8400-e29b-41d4-a716"})
    @DisplayName("requireValidUUID - 잘못된 UUID 형식에 대해 예외가 발생한다")
    void requireValidUUID_ShouldThrowExceptionForInvalidFormat(String invalidUuid) {
        // Given
        String fieldName = "sessionId";

        // When & Then
        assertThatThrownBy(() -> ValidationUtils.requireValidUUID(invalidUuid, fieldName))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid sessionId format: " + invalidUuid);
    }

    // ===========================================
    // requireValidIpAddress 테스트
    // ===========================================

    @ParameterizedTest
    @ValueSource(strings = {"192.168.1.1", "127.0.0.1", "10.0.0.1"})
    @DisplayName("requireValidIpAddress - 유효한 IPv4 주소로 성공한다")
    void requireValidIpAddress_ShouldSucceedWithValidIPv4(String validIp) {
        // Given
        String fieldName = "clientIp";

        // When
        String result = ValidationUtils.requireValidIpAddress(validIp, fieldName);

        // Then
        assertThat(result).isEqualTo(validIp);
    }

    @Test
    @DisplayName("requireValidIpAddress - 유효한 IPv6 주소로 성공한다")
    void requireValidIpAddress_ShouldSucceedWithValidIPv6() {
        // Given
        String validIp = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
        String fieldName = "clientIp";

        // When
        String result = ValidationUtils.requireValidIpAddress(validIp, fieldName);

        // Then
        assertThat(result).isEqualTo(validIp);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    @DisplayName("requireValidIpAddress - null/empty/blank에 대해 예외가 발생한다")
    void requireValidIpAddress_ShouldThrowExceptionForNullOrEmpty(String invalidValue) {
        // Given
        String fieldName = "clientIp";

        // When & Then
        assertThatThrownBy(() -> ValidationUtils.requireValidIpAddress(invalidValue, fieldName))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("clientIp cannot be null or empty");
    }

    @Test
    @DisplayName("requireValidIpAddress - 공백이 포함된 IP에 대해 예외가 발생한다")
    void requireValidIpAddress_ShouldThrowExceptionForIPWithWhitespace() {
        // Given
        String invalidIp = " 192.168.1.1 ";
        String fieldName = "clientIp";

        // When & Then
        assertThatThrownBy(() -> ValidationUtils.requireValidIpAddress(invalidIp, fieldName))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid clientIp format: " + invalidIp);
    }

    @ParameterizedTest
    @ValueSource(strings = {"999.999.999.999", "invalid-ip", "192.168.1.1.1", "300.168.1.1"})
    @DisplayName("requireValidIpAddress - 잘못된 IP 형식에 대해 예외가 발생한다")
    void requireValidIpAddress_ShouldThrowExceptionForInvalidFormat(String invalidIp) {
        // Given
        String fieldName = "clientIp";

        // When & Then
        assertThatThrownBy(() -> ValidationUtils.requireValidIpAddress(invalidIp, fieldName))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid clientIp format: " + invalidIp);
    }

    // ===========================================
    // requireValidPattern 테스트
    // ===========================================

    @Test
    @DisplayName("requireValidPattern - 유효한 패턴 매치로 성공한다")
    void requireValidPattern_ShouldSucceedWithValidPattern() {
        // Given
        String value = "user123";
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9_]+$");
        String fieldName = "username";

        // When & Then
        assertThatNoException().isThrownBy(() -> 
            ValidationUtils.requireValidPattern(value, pattern, fieldName)
        );
    }

    @Test
    @DisplayName("requireValidPattern - 패턴이 매치하지 않으면 예외가 발생한다")
    void requireValidPattern_ShouldThrowExceptionWhenPatternDoesNotMatch() {
        // Given
        String value = "user@domain";
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9_]+$");
        String fieldName = "username";

        // When & Then
        assertThatThrownBy(() -> ValidationUtils.requireValidPattern(value, pattern, fieldName))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("username contains invalid characters");
    }

    @Test
    @DisplayName("requireValidPattern - null 패턴에 대해 예외가 발생한다")
    void requireValidPattern_ShouldThrowExceptionForNullPattern() {
        // Given
        String value = "user123";
        Pattern pattern = null;
        String fieldName = "username";

        // When & Then
        assertThatThrownBy(() -> ValidationUtils.requireValidPattern(value, pattern, fieldName))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("pattern cannot be null");
    }

    // ===========================================
    // requireValidLength 테스트
    // ===========================================

    @Test
    @DisplayName("requireValidLength - 유효한 길이로 성공한다")
    void requireValidLength_ShouldSucceedWithValidLength() {
        // Given
        String value = "username";
        int minLength = 3;
        int maxLength = 50;
        String fieldName = "username";

        // When & Then
        assertThatNoException().isThrownBy(() -> 
            ValidationUtils.requireValidLength(value, minLength, maxLength, fieldName)
        );
    }

    @Test
    @DisplayName("requireValidLength - 최소 길이보다 짧으면 예외가 발생한다")
    void requireValidLength_ShouldThrowExceptionWhenTooShort() {
        // Given
        String value = "ab";
        int minLength = 3;
        int maxLength = 50;
        String fieldName = "username";

        // When & Then
        assertThatThrownBy(() -> ValidationUtils.requireValidLength(value, minLength, maxLength, fieldName))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("username must be between 3 and 50 characters");
    }

    @Test
    @DisplayName("requireValidLength - 최대 길이보다 길면 예외가 발생한다")
    void requireValidLength_ShouldThrowExceptionWhenTooLong() {
        // Given
        String value = "a".repeat(51);
        int minLength = 3;
        int maxLength = 50;
        String fieldName = "username";

        // When & Then
        assertThatThrownBy(() -> ValidationUtils.requireValidLength(value, minLength, maxLength, fieldName))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("username must be between 3 and 50 characters");
    }

    // ===========================================
    // requireMinLength 테스트
    // ===========================================

    @Test
    @DisplayName("requireMinLength - 최소 길이 이상으로 성공한다")
    void requireMinLength_ShouldSucceedWithValidLength() {
        // Given
        String value = "password123";
        int minLength = 8;
        String fieldName = "password";

        // When & Then
        assertThatNoException().isThrownBy(() -> 
            ValidationUtils.requireMinLength(value, minLength, fieldName)
        );
    }

    @Test
    @DisplayName("requireMinLength - 최소 길이보다 짧으면 예외가 발생한다")
    void requireMinLength_ShouldThrowExceptionWhenTooShort() {
        // Given
        String value = "1234567";
        int minLength = 8;
        String fieldName = "password";

        // When & Then
        assertThatThrownBy(() -> ValidationUtils.requireMinLength(value, minLength, fieldName))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("password must be at least 8 characters");
    }

    @Test
    @DisplayName("requireMinLength - 정확히 최소 길이로 성공한다")
    void requireMinLength_ShouldSucceedWithExactMinLength() {
        // Given
        String value = "12345678";
        int minLength = 8;
        String fieldName = "password";

        // When & Then
        assertThatNoException().isThrownBy(() -> 
            ValidationUtils.requireMinLength(value, minLength, fieldName)
        );
    }
}