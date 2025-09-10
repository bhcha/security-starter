package com.ldx.hexacore.security.session.application.command.port.in;

import java.time.LocalDateTime;

/**
 * 인증 시도 기록 결과
 * 
 * 인증 시도 기록 처리 결과를 담는 불변 객체입니다.
 * 계정 잠금 상태와 잠금 해제 시각 정보를 포함합니다.
 */
public record RecordAttemptResult(
    String sessionId,           // 세션 ID
    String userId,              // 사용자 ID
    boolean isSuccessful,       // 인증 성공 여부
    boolean isAccountLocked,    // 계정 잠금 여부
    LocalDateTime lockedUntil,  // 잠금 해제 시각 (잠긴 경우)
    LocalDateTime recordedAt    // 기록 시각
) {
    /**
     * 성공적인 인증 시도 결과 생성
     */
    public static RecordAttemptResult success(String sessionId, String userId, LocalDateTime recordedAt) {
        return new RecordAttemptResult(sessionId, userId, true, false, null, recordedAt);
    }
    
    /**
     * 실패한 인증 시도 결과 생성 (잠금 없음)
     */
    public static RecordAttemptResult failure(String sessionId, String userId, LocalDateTime recordedAt) {
        return new RecordAttemptResult(sessionId, userId, false, false, null, recordedAt);
    }
    
    /**
     * 계정 잠금이 발생한 인증 시도 결과 생성
     */
    public static RecordAttemptResult failureWithLock(String sessionId, String userId, 
                                                     LocalDateTime lockedUntil, LocalDateTime recordedAt) {
        return new RecordAttemptResult(sessionId, userId, false, true, lockedUntil, recordedAt);
    }
}