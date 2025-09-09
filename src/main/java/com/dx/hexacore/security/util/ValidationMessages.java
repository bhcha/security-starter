package com.dx.hexacore.security.util;

/**
 * 검증 실패 시 사용되는 표준 메시지 상수 클래스.
 * 
 * <p>모든 검증 관련 예외 메시지를 일관된 형식으로 제공합니다.
 * 메시지 템플릿은 String.format()을 통해 동적 값을 삽입할 수 있습니다.
 * 
 * <p>메시지 카테고리:
 * <ul>
 *   <li>NULL_EMPTY: null 또는 빈 값 관련 메시지</li>
 *   <li>RANGE: 범위 검증 관련 메시지</li>
 *   <li>FORMAT: 형식 검증 관련 메시지</li>
 *   <li>BUSINESS: 비즈니스 규칙 관련 메시지</li>
 * </ul>
 * 
 * @author Claude Code Assistant
 * @since 1.0.0
 */
public final class ValidationMessages {
    
    private ValidationMessages() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    // ===== NULL/EMPTY 관련 메시지 =====
    
    /**
     * 필드가 null 또는 empty인 경우 사용되는 메시지 템플릿.
     * 사용법: String.format(CANNOT_BE_NULL_OR_EMPTY, fieldName)
     */
    public static final String CANNOT_BE_NULL_OR_EMPTY = "%s cannot be null or empty";
    
    /**
     * 필드가 null인 경우 사용되는 메시지 템플릿.
     * 사용법: String.format(CANNOT_BE_NULL, fieldName)
     */
    public static final String CANNOT_BE_NULL = "%s cannot be null";
    
    /**
     * 필드가 빈 값인 경우 사용되는 메시지 템플릿.
     * 사용법: String.format(CANNOT_BE_EMPTY, fieldName)
     */
    public static final String CANNOT_BE_EMPTY = "%s cannot be empty";
    
    /**
     * 필드가 공백만 있는 경우 사용되는 메시지 템플릿.
     * 사용법: String.format(CANNOT_BE_BLANK, fieldName)
     */
    public static final String CANNOT_BE_BLANK = "%s cannot be blank";
    
    // ===== 범위 관련 메시지 =====
    
    /**
     * 숫자가 지정된 범위를 벗어난 경우 사용되는 메시지 템플릿.
     * 사용법: String.format(MUST_BE_BETWEEN_NUMERIC, fieldName, min, max)
     */
    public static final String MUST_BE_BETWEEN_NUMERIC = "%s must be between %d and %d";
    
    /**
     * 문자열 길이가 지정된 범위를 벗어난 경우 사용되는 메시지 템플릿.
     * 사용법: String.format(MUST_BE_BETWEEN_CHARACTERS, fieldName, minLength, maxLength)
     */
    public static final String MUST_BE_BETWEEN_CHARACTERS = "%s must be between %d and %d characters";
    
    /**
     * 문자열이 최소 길이보다 짧은 경우 사용되는 메시지 템플릿.
     * 사용법: String.format(MUST_BE_AT_LEAST_CHARACTERS, fieldName, minLength)
     */
    public static final String MUST_BE_AT_LEAST_CHARACTERS = "%s must be at least %d characters";
    
    /**
     * 숫자가 양수가 아닌 경우 사용되는 메시지 템플릿.
     * 사용법: String.format(MUST_BE_POSITIVE, fieldName)
     */
    public static final String MUST_BE_POSITIVE = "%s must be positive";
    
    // ===== 형식 관련 메시지 =====
    
    /**
     * 값의 형식이 유효하지 않은 경우 사용되는 메시지 템플릿.
     * 사용법: String.format(INVALID_FORMAT_WITH_VALUE, fieldName, value)
     */
    public static final String INVALID_FORMAT_WITH_VALUE = "Invalid %s format: %s";
    
    /**
     * 값의 형식이 유효하지 않은 경우 사용되는 간단한 메시지 템플릿.
     * 사용법: String.format(INVALID_FORMAT, fieldName)
     */
    public static final String INVALID_FORMAT = "Invalid %s format";
    
    /**
     * 유효하지 않은 문자가 포함된 경우 사용되는 메시지 템플릿.
     * 사용법: String.format(CONTAINS_INVALID_CHARACTERS, fieldName)
     */
    public static final String CONTAINS_INVALID_CHARACTERS = "%s contains invalid characters";
    
    // ===== 비즈니스 규칙 관련 메시지 =====
    
    /**
     * 시간 범위가 유효하지 않은 경우 사용되는 메시지 템플릿.
     * 사용법: TIME_RANGE_INVALID
     */
    public static final String TIME_RANGE_INVALID = "From time cannot be after to time";
    
    /**
     * 시간이 미래여야 하는 경우 사용되는 메시지 템플릿.
     * 사용법: String.format(MUST_BE_IN_FUTURE, fieldName)
     */
    public static final String MUST_BE_IN_FUTURE = "%s must be in the future";
    
    /**
     * 중복될 수 없는 값이 중복된 경우 사용되는 메시지 템플릿.
     * 사용법: String.format(ALREADY_EXISTS, fieldName, value)
     */
    public static final String ALREADY_EXISTS = "%s '%s' already exists";
    
    // ===== 유틸리티 메서드 =====
    
    /**
     * null 또는 empty 검증 실패 메시지를 생성합니다.
     * 
     * @param fieldName 필드명
     * @return 포맷된 에러 메시지
     */
    public static String cannotBeNullOrEmpty(String fieldName) {
        return String.format(CANNOT_BE_NULL_OR_EMPTY, fieldName);
    }
    
    /**
     * null 검증 실패 메시지를 생성합니다.
     * 
     * @param fieldName 필드명
     * @return 포맷된 에러 메시지
     */
    public static String cannotBeNull(String fieldName) {
        return String.format(CANNOT_BE_NULL, fieldName);
    }
    
    /**
     * empty 검증 실패 메시지를 생성합니다.
     * 
     * @param fieldName 필드명
     * @return 포맷된 에러 메시지
     */
    public static String cannotBeEmpty(String fieldName) {
        return String.format(CANNOT_BE_EMPTY, fieldName);
    }
    
    /**
     * blank 검증 실패 메시지를 생성합니다.
     * 
     * @param fieldName 필드명
     * @return 포맷된 에러 메시지
     */
    public static String cannotBeBlank(String fieldName) {
        return String.format(CANNOT_BE_BLANK, fieldName);
    }
    
    /**
     * 숫자 범위 검증 실패 메시지를 생성합니다.
     * 
     * @param fieldName 필드명
     * @param min 최솟값
     * @param max 최댓값
     * @return 포맷된 에러 메시지
     */
    public static String mustBeBetween(String fieldName, long min, long max) {
        return String.format(MUST_BE_BETWEEN_NUMERIC, fieldName, min, max);
    }
    
    /**
     * 문자열 길이 범위 검증 실패 메시지를 생성합니다.
     * 
     * @param fieldName 필드명
     * @param minLength 최소 길이
     * @param maxLength 최대 길이
     * @return 포맷된 에러 메시지
     */
    public static String mustBeBetweenCharacters(String fieldName, int minLength, int maxLength) {
        return String.format(MUST_BE_BETWEEN_CHARACTERS, fieldName, minLength, maxLength);
    }
    
    /**
     * 최소 길이 검증 실패 메시지를 생성합니다.
     * 
     * @param fieldName 필드명
     * @param minLength 최소 길이
     * @return 포맷된 에러 메시지
     */
    public static String mustBeAtLeastCharacters(String fieldName, int minLength) {
        return String.format(MUST_BE_AT_LEAST_CHARACTERS, fieldName, minLength);
    }
    
    /**
     * 양수 검증 실패 메시지를 생성합니다.
     * 
     * @param fieldName 필드명
     * @return 포맷된 에러 메시지
     */
    public static String mustBePositive(String fieldName) {
        return String.format(MUST_BE_POSITIVE, fieldName);
    }
    
    /**
     * 형식 검증 실패 메시지를 생성합니다 (값 포함).
     * 
     * @param fieldName 필드명
     * @param value 유효하지 않은 값
     * @return 포맷된 에러 메시지
     */
    public static String invalidFormat(String fieldName, Object value) {
        return String.format(INVALID_FORMAT_WITH_VALUE, fieldName, value);
    }
    
    /**
     * 형식 검증 실패 메시지를 생성합니다 (값 미포함).
     * 
     * @param fieldName 필드명
     * @return 포맷된 에러 메시지
     */
    public static String invalidFormat(String fieldName) {
        return String.format(INVALID_FORMAT, fieldName);
    }
    
    /**
     * 유효하지 않은 문자 포함 메시지를 생성합니다.
     * 
     * @param fieldName 필드명
     * @return 포맷된 에러 메시지
     */
    public static String containsInvalidCharacters(String fieldName) {
        return String.format(CONTAINS_INVALID_CHARACTERS, fieldName);
    }
    
    /**
     * 미래 시간 검증 실패 메시지를 생성합니다.
     * 
     * @param fieldName 필드명
     * @return 포맷된 에러 메시지
     */
    public static String mustBeInFuture(String fieldName) {
        return String.format(MUST_BE_IN_FUTURE, fieldName);
    }
    
    /**
     * 중복 존재 메시지를 생성합니다.
     * 
     * @param fieldName 필드명
     * @param value 중복된 값
     * @return 포맷된 에러 메시지
     */
    public static String alreadyExists(String fieldName, Object value) {
        return String.format(ALREADY_EXISTS, fieldName, value);
    }
}