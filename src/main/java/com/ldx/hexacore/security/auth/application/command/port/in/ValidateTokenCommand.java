package com.ldx.hexacore.security.auth.application.command.port.in;

import com.ldx.hexacore.security.util.ValidationMessages;
import java.util.Objects;

/**
 * 토큰 검증을 위한 명령 객체.
 * 액세스 토큰의 유효성을 검증하기 위한 요청을 나타냅니다.
 * 
 * @since 1.0.0
 */
public class ValidateTokenCommand {
    
    private final String accessToken;
    
    /**
     * 토큰 검증 명령을 생성합니다.
     * 
     * @param accessToken 검증할 액세스 토큰 (null, 빈 문자열, 공백 불가)
     * @throws IllegalArgumentException accessToken이 null이거나 빈 값인 경우
     */
    public ValidateTokenCommand(String accessToken) {
        this.accessToken = validateAccessToken(accessToken);
    }
    
    /**
     * 정적 팩토리 메서드로 토큰 검증 명령을 생성합니다.
     * 
     * @param accessToken 검증할 액세스 토큰
     * @return 새로운 ValidateTokenCommand 인스턴스
     */
    public static ValidateTokenCommand of(String accessToken) {
        return new ValidateTokenCommand(accessToken);
    }
    
    private String validateAccessToken(String accessToken) {
        if (accessToken == null) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNull("accessToken"));
        }
        if (accessToken.trim().isEmpty()) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNullOrEmpty("accessToken"));
        }
        return accessToken;
    }
    
    /**
     * 액세스 토큰을 반환합니다.
     * 
     * @return 액세스 토큰
     */
    public String getAccessToken() {
        return accessToken;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidateTokenCommand that = (ValidateTokenCommand) o;
        return Objects.equals(accessToken, that.accessToken);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(accessToken);
    }
    
    @Override
    public String toString() {
        return "ValidateTokenCommand{" +
                "accessToken='[PROTECTED]'" +
                '}';
    }
}