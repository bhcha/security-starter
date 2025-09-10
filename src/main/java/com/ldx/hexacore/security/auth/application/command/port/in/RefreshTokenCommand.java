package com.ldx.hexacore.security.auth.application.command.port.in;

import com.ldx.hexacore.security.util.ValidationMessages;
import java.util.Objects;

/**
 * 토큰 갱신을 위한 명령 객체.
 * 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받기 위한 요청을 나타냅니다.
 * 
 * @since 1.0.0
 */
public class RefreshTokenCommand {
    
    private final String refreshToken;
    
    /**
     * 토큰 갱신 명령을 생성합니다.
     * 
     * @param refreshToken 리프레시 토큰 (null, 빈 문자열, 공백 불가)
     * @throws IllegalArgumentException refreshToken이 null이거나 빈 값인 경우
     */
    public RefreshTokenCommand(String refreshToken) {
        this.refreshToken = validateRefreshToken(refreshToken);
    }
    
    /**
     * 정적 팩토리 메서드로 토큰 갱신 명령을 생성합니다.
     * 
     * @param refreshToken 리프레시 토큰
     * @return 새로운 RefreshTokenCommand 인스턴스
     */
    public static RefreshTokenCommand of(String refreshToken) {
        return new RefreshTokenCommand(refreshToken);
    }
    
    private String validateRefreshToken(String refreshToken) {
        if (refreshToken == null) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNull("refreshToken"));
        }
        if (refreshToken.trim().isEmpty()) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNullOrEmpty("refreshToken"));
        }
        return refreshToken;
    }
    
    /**
     * 리프레시 토큰을 반환합니다.
     * 
     * @return 리프레시 토큰
     */
    public String getRefreshToken() {
        return refreshToken;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefreshTokenCommand that = (RefreshTokenCommand) o;
        return Objects.equals(refreshToken, that.refreshToken);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(refreshToken);
    }
    
    @Override
    public String toString() {
        return "RefreshTokenCommand{" +
                "refreshToken='[PROTECTED]'" +
                '}';
    }
}