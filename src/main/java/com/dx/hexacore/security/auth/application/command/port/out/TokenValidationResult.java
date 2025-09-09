package com.dx.hexacore.security.auth.application.command.port.out;

import com.dx.hexacore.security.util.ValidationMessages;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 토큰 검증 결과를 나타내는 불변 DTO
 * 
 * <p>토큰 검증 과정에서 얻어진 결과를 캡슐화합니다.</p>
 * <p>유효한 토큰의 경우 사용자 정보와 권한 정보를 포함하며,</p>
 * <p>무효한 토큰의 경우 실패 사유를 포함합니다.</p>
 * 
 * @param valid 토큰 유효성
 * @param userId 사용자 ID
 * @param username 사용자명
 * @param authorities 권한 목록
 * @param expiresAt 토큰 만료 시각
 * @param claims 추가 클레임 정보
 */
public record TokenValidationResult(
        boolean valid,
        String userId,
        String username,
        Set<String> authorities,
        Instant expiresAt,
        Map<String, Object> claims
) {
    
    /**
     * 생성자에서 불변성을 보장합니다.
     */
    public TokenValidationResult {
        // 방어적 복사를 통한 불변성 보장
        authorities = authorities != null ? Set.copyOf(authorities) : null;
        claims = claims != null ? Map.copyOf(claims) : null;
    }
    
    /**
     * 유효한 토큰 검증 결과를 생성합니다.
     * 
     * @param userId 사용자 ID (필수)
     * @param username 사용자명 (필수)
     * @param authorities 권한 목록
     * @param expiresAt 토큰 만료 시각
     * @return 유효한 토큰 검증 결과
     * @throws IllegalArgumentException 필수 매개변수가 null이거나 빈 값인 경우
     */
    public static TokenValidationResult valid(String userId, String username, 
                                            Set<String> authorities, Instant expiresAt) {
        validateUserId(userId);
        validateUsername(username);
        
        return new TokenValidationResult(
                true,
                userId,
                username,
                authorities,
                expiresAt,
                Collections.emptyMap()
        );
    }
    
    /**
     * 무효한 토큰 검증 결과를 생성합니다.
     * 
     * @param reason 실패 사유 (필수)
     * @return 무효한 토큰 검증 결과
     * @throws IllegalArgumentException 실패 사유가 null이거나 빈 값인 경우
     */
    public static TokenValidationResult invalid(String reason) {
        validateReason(reason);
        
        Map<String, Object> errorClaims = new HashMap<>();
        errorClaims.put("error", reason);
        
        return new TokenValidationResult(
                false,
                null,
                null,
                null,
                null,
                errorClaims
        );
    }
    
    private static void validateUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNullOrEmpty("UserId"));
        }
    }
    
    private static void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNullOrEmpty("Username"));
        }
    }
    
    private static void validateReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNullOrEmpty("Reason"));
        }
    }
}