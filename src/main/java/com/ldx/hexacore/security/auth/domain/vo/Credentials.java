package com.ldx.hexacore.security.auth.domain.vo;

import com.ldx.hexacore.security.util.ValidationUtils;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 인증을 위한 사용자 자격증명 값 객체.
 * 
 * <p>사용자명과 비밀번호를 포함한 불변 객체로, 인증 과정에서 사용됩니다.
 * 
 * <p>제약사항:
 * <ul>
 *   <li>Username: 3-50자, 영문/숫자/언더스코어만 허용</li>
 *   <li>Password: 최소 8자 이상</li>
 * </ul>
 * 
 * @since 1.0.0
 */
public final class Credentials {
    
    private static final Pattern VALID_USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");
    // 길이 상수들은 SecurityConstants에서 주입받도록 변경
    
    private final String username;
    private final String password;

    // private 생성자 - 상수 주입 버전
    private Credentials(String username, String password, 
                       int minUsernameLength, int maxUsernameLength, int minPasswordLength) {
        ValidationUtils.requireNonNullOrEmpty(username, "Username");
        ValidationUtils.requireInRange(username.length(), minUsernameLength, maxUsernameLength, "Username length");
        ValidationUtils.requireValidPattern(username, VALID_USERNAME_PATTERN, "Username");
        ValidationUtils.requireNonNullOrEmpty(password, "Password");
        ValidationUtils.requireMinLength(password, minPasswordLength, "Password");
        this.username = username;
        this.password = password;
    }
    
    // private 생성자 - 기본 상수 버전 (deprecated)
    private Credentials(String username, String password, boolean deprecated) {
        ValidationUtils.requireNonNullOrEmpty(username, "Username");
        ValidationUtils.requireInRange(username.length(), 3, 50, "Username length");
        ValidationUtils.requireValidPattern(username, VALID_USERNAME_PATTERN, "Username");
        ValidationUtils.requireNonNullOrEmpty(password, "Password");
        ValidationUtils.requireMinLength(password, 8, "Password");
        this.username = username;
        this.password = password;
    }
    
    // ===== 정적 팩토리 메서드 =====
    
    /**
     * 사용자명과 비밀번호로부터 Credentials를 생성합니다.
     * 
     * @param username 사용자명
     * @param password 비밀번호
     * @param minUsernameLength 최소 사용자명 길이
     * @param maxUsernameLength 최대 사용자명 길이
     * @param minPasswordLength 최소 비밀번호 길이
     * @return 생성된 Credentials
     * @throws IllegalArgumentException 유효하지 않은 값인 경우
     */
    public static Credentials of(String username, String password,
                               int minUsernameLength, int maxUsernameLength, int minPasswordLength) {
        return new Credentials(username, password, minUsernameLength, maxUsernameLength, minPasswordLength);
    }

    /**
     * 기본 제약사항으로 사용자명과 비밀번호로부터 Credentials를 생성합니다.
     * 
     * @deprecated 외부에서 상수값을 주입받는 {@link #of(String, String, int, int, int)} 사용을 권장합니다.
     * @param username 사용자명
     * @param password 비밀번호
     * @return 생성된 Credentials
     * @throws IllegalArgumentException 유효하지 않은 값인 경우
     */
    @Deprecated
    public static Credentials of(String username, String password) {
        return new Credentials(username, password, true);
    }
    
    // ===== 유틸리티 메서드 =====
    
    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }
    
    
    // ===== Object 메서드 =====
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Credentials that = (Credentials) o;
        return Objects.equals(username, that.username) && 
               Objects.equals(password, that.password);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(username, password);
    }
    
    @Override
    public String toString() {
        return "Credentials{username='" + username + "', password='***'}";
    }
}