package com.dx.hexacore.security.auth.application.command.port.in;

import java.util.Objects;

/**
 * 사용자 인증을 위한 명령 객체.
 * 사용자명과 비밀번호를 포함하여 인증 요청을 나타냅니다.
 * 
 * @since 1.0.0
 */
public class AuthenticateCommand {
    
    private final String username;
    private final String password;
    
    /**
     * 인증 명령을 생성합니다.
     * 
     * @param username 사용자명 (null, 빈 문자열, 공백 불가)
     * @param password 비밀번호 (null, 빈 문자열, 공백 불가)
     * @throws IllegalArgumentException username 또는 password가 null이거나 빈 값인 경우
     */
    public AuthenticateCommand(String username, String password) {
        this.username = validateUsername(username);
        this.password = validatePassword(password);
    }
    
    /**
     * 정적 팩토리 메서드로 인증 명령을 생성합니다.
     * 
     * @param username 사용자명
     * @param password 비밀번호
     * @return 새로운 AuthenticateCommand 인스턴스
     */
    public static AuthenticateCommand of(String username, String password) {
        return new AuthenticateCommand(username, password);
    }
    
    private String validateUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("username cannot be null");
        }
        if (username.trim().isEmpty()) {
            throw new IllegalArgumentException("username cannot be empty or blank");
        }
        return username;
    }
    
    private String validatePassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("password cannot be null");
        }
        if (password.trim().isEmpty()) {
            throw new IllegalArgumentException("password cannot be empty or blank");
        }
        return password;
    }
    
    /**
     * 사용자명을 반환합니다.
     * 
     * @return 사용자명
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * 비밀번호를 반환합니다.
     * 
     * @return 비밀번호
     */
    public String getPassword() {
        return password;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthenticateCommand that = (AuthenticateCommand) o;
        return Objects.equals(username, that.username) && 
               Objects.equals(password, that.password);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(username, password);
    }
    
    @Override
    public String toString() {
        return "AuthenticateCommand{" +
                "username='" + username + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}