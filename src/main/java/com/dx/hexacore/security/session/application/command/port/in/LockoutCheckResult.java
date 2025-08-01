package com.dx.hexacore.security.session.application.command.port.in;

import java.time.LocalDateTime;

/**
 * 계정 잠금 상태 확인 결과
 * 
 * 특정 사용자의 계정 잠금 상태 확인 결과를 담는 불변 객체입니다.
 */
public record LockoutCheckResult(
    String sessionId,           // 세션 ID
    String userId,              // 사용자 ID
    boolean isLocked,           // 잠금 여부
    LocalDateTime lockedUntil,  // 잠금 해제 시각 (잠긴 경우)
    LocalDateTime checkedAt     // 확인 시각
) {
    /**
     * 잠금되지 않은 상태 결과 생성
     */
    public static LockoutCheckResult notLocked(String sessionId, String userId, LocalDateTime checkedAt) {
        return new LockoutCheckResult(sessionId, userId, false, null, checkedAt);
    }
    
    /**
     * 잠긴 상태 결과 생성
     */
    public static LockoutCheckResult locked(String sessionId, String userId, 
                                          LocalDateTime lockedUntil, LocalDateTime checkedAt) {
        return new LockoutCheckResult(sessionId, userId, true, lockedUntil, checkedAt);
    }
}