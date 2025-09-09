package com.dx.hexacore.security.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 공통 검증 유틸리티 클래스.
 * 
 * <p>Value Object와 Command 객체에서 사용되는 공통 검증 로직을 제공합니다.
 * 모든 메서드는 static이며, 검증 실패 시 명확한 메시지와 함께 IllegalArgumentException을 발생시킵니다.
 * 
 * <p>검증 실패 시 예외 메시지는 일관된 형식을 따릅니다:
 * <ul>
 *   <li>Null/Empty: "{fieldName} cannot be null or empty"</li>
 *   <li>범위: "{fieldName} must be between {min} and {max}"</li>
 *   <li>형식: "Invalid {fieldName} format: {value}"</li>
 * </ul>
 * 
 * @author Claude Code Assistant
 * @since 1.0.0
 */
public final class ValidationUtils {
    
    private ValidationUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * 문자열이 null이 아니고 공백이 아님을 검증합니다.
     * 
     * @param value 검증할 문자열
     * @param fieldName 필드명 (예외 메시지용)
     * @throws IllegalArgumentException 문자열이 null이거나 공백인 경우
     */
    public static void requireNonNullOrEmpty(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNullOrEmpty(fieldName));
        }
    }
    
    /**
     * 객체가 null이 아님을 검증합니다.
     * 
     * @param value 검증할 객체
     * @param fieldName 필드명 (예외 메시지용)
     * @throws IllegalArgumentException 객체가 null인 경우
     */
    public static void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNull(fieldName));
        }
    }
    
    /**
     * 정수 값이 지정된 범위 내에 있는지 검증합니다.
     * 
     * @param value 검증할 값
     * @param min 최솟값 (포함)
     * @param max 최댓값 (포함)
     * @param fieldName 필드명 (예외 메시지용)
     * @throws IllegalArgumentException 값이 범위를 벗어난 경우
     */
    public static void requireInRange(int value, int min, int max, String fieldName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(ValidationMessages.mustBeBetween(fieldName, min, max));
        }
    }
    
    /**
     * long 값이 지정된 범위 내에 있는지 검증합니다.
     * 
     * @param value 검증할 값
     * @param min 최솟값 (포함)
     * @param max 최댓값 (포함)
     * @param fieldName 필드명 (예외 메시지용)
     * @throws IllegalArgumentException 값이 범위를 벗어난 경우
     */
    public static void requireInRange(long value, long min, long max, String fieldName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(ValidationMessages.mustBeBetween(fieldName, min, max));
        }
    }
    
    /**
     * 값이 양수인지 검증합니다.
     * 
     * @param value 검증할 값
     * @param fieldName 필드명 (예외 메시지용)
     * @throws IllegalArgumentException 값이 0 이하인 경우
     */
    public static void requirePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(ValidationMessages.mustBePositive(fieldName));
        }
    }
    
    /**
     * 문자열이 유효한 UUID 형식인지 검증합니다.
     * 
     * @param value 검증할 UUID 문자열
     * @param fieldName 필드명 (예외 메시지용)
     * @return 파싱된 UUID 객체
     * @throws IllegalArgumentException UUID 형식이 올바르지 않은 경우
     */
    public static UUID requireValidUUID(String value, String fieldName) {
        requireNonNullOrEmpty(value, fieldName);
        
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(ValidationMessages.invalidFormat(fieldName, value));
        }
    }
    
    /**
     * 문자열이 유효한 IP 주소 형식인지 검증합니다.
     * IPv4와 IPv6를 모두 지원합니다.
     * 
     * @param ipAddress 검증할 IP 주소
     * @param fieldName 필드명 (예외 메시지용)
     * @return 정규화된 IP 주소 문자열
     * @throws IllegalArgumentException IP 주소 형식이 올바르지 않은 경우
     */
    public static String requireValidIpAddress(String ipAddress, String fieldName) {
        requireNonNullOrEmpty(ipAddress, fieldName);
        
        // 공백 제거하여 정확한 검증
        String trimmedIp = ipAddress.trim();
        if (!trimmedIp.equals(ipAddress)) {
            throw new IllegalArgumentException(ValidationMessages.invalidFormat(fieldName, ipAddress));
        }
        
        try {
            InetAddress.getByName(trimmedIp);
            return trimmedIp;
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException(ValidationMessages.invalidFormat(fieldName, ipAddress));
        }
    }
    
    /**
     * 문자열이 지정된 정규식 패턴과 일치하는지 검증합니다.
     * 
     * @param value 검증할 문자열
     * @param pattern 정규식 패턴
     * @param fieldName 필드명 (예외 메시지용)
     * @throws IllegalArgumentException 문자열이 패턴과 일치하지 않는 경우
     */
    public static void requireValidPattern(String value, Pattern pattern, String fieldName) {
        requireNonNullOrEmpty(value, fieldName);
        requireNonNull(pattern, "pattern");
        
        if (!pattern.matcher(value).matches()) {
            throw new IllegalArgumentException(ValidationMessages.containsInvalidCharacters(fieldName));
        }
    }
    
    /**
     * 문자열이 지정된 길이 범위 내에 있는지 검증합니다.
     * 
     * @param value 검증할 문자열
     * @param minLength 최소 길이 (포함)
     * @param maxLength 최대 길이 (포함)
     * @param fieldName 필드명 (예외 메시지용)
     * @throws IllegalArgumentException 길이가 범위를 벗어난 경우
     */
    public static void requireValidLength(String value, int minLength, int maxLength, String fieldName) {
        requireNonNullOrEmpty(value, fieldName);
        
        int length = value.length();
        if (length < minLength || length > maxLength) {
            throw new IllegalArgumentException(
                ValidationMessages.mustBeBetweenCharacters(fieldName, minLength, maxLength)
            );
        }
    }
    
    /**
     * 문자열이 최소 길이 이상인지 검증합니다.
     * 
     * @param value 검증할 문자열
     * @param minLength 최소 길이 (포함)
     * @param fieldName 필드명 (예외 메시지용)
     * @throws IllegalArgumentException 길이가 최소 길이보다 짧은 경우
     */
    public static void requireMinLength(String value, int minLength, String fieldName) {
        requireNonNullOrEmpty(value, fieldName);
        
        if (value.length() < minLength) {
            throw new IllegalArgumentException(
                ValidationMessages.mustBeAtLeastCharacters(fieldName, minLength)
            );
        }
    }
}