package com.dx.hexacore.security.auth.domain.vo;

import jakarta.persistence.Embeddable;
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
@Embeddable
public final class Credentials {
    
    private static final Pattern VALID_USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 50;
    private static final int MIN_PASSWORD_LENGTH = 8;
    
    private final String username;
    private final String password;
    
    // JPA를 위한 protected 기본 생성자
    protected Credentials() {
        this.username = null;
        this.password = null;
    }
    
    // private 생성자
    private Credentials(String username, String password) {
        validateUsername(username);
        validatePassword(password);
        this.username = username;
        this.password = password;
    }
    
    // ===== 정적 팩토리 메서드 =====
    
    /**
     * 사용자명과 비밀번호로부터 Credentials를 생성합니다.
     * 
     * @param username 사용자명
     * @param password 비밀번호
     * @return 생성된 Credentials
     * @throws IllegalArgumentException 유효하지 않은 값인 경우
     */
    public static Credentials of(String username, String password) {
        return new Credentials(username, password);
    }
    
    // ===== 유틸리티 메서드 =====
    
    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }
    
    // ===== Private 검증 메서드 =====
    
    private static void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        
        if (username.length() < MIN_USERNAME_LENGTH || username.length() > MAX_USERNAME_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Username must be between %d and %d characters", 
                    MIN_USERNAME_LENGTH, MAX_USERNAME_LENGTH)
            );
        }
        
        if (!VALID_USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException(
                "Username contains invalid characters"
            );
        }
    }
    
    private static void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Password must be at least %d characters", MIN_PASSWORD_LENGTH)
            );
        }
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