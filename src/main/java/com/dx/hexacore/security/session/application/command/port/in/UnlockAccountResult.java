package com.dx.hexacore.security.session.application.command.port.in;

import java.time.LocalDateTime;

/**
 * 계정 잠금 해제 결과
 * 
 * 계정 잠금 해제 처리 결과를 담는 불변 객체입니다.
 */
public record UnlockAccountResult(
    String sessionId,           // 세션 ID
    String userId,              // 사용자 ID
    boolean wasLocked,          // 해제 전 잠금 상태
    boolean unlockSuccessful,   // 해제 성공 여부
    LocalDateTime unlockedAt    // 해제 시각
) {
    /**
     * 성공적인 해제 결과 생성
     */
    public static UnlockAccountResult success(String sessionId, String userId, 
                                            boolean wasLocked, LocalDateTime unlockedAt) {
        return new UnlockAccountResult(sessionId, userId, wasLocked, true, unlockedAt);
    }
    
    /**
     * 해제 실패 결과 생성
     */
    public static UnlockAccountResult failure(String sessionId, String userId, boolean wasLocked) {
        return new UnlockAccountResult(sessionId, userId, wasLocked, false, null);
    }
}