package com.ldx.hexacore.security.session.application.query.port.in;

import java.time.LocalDateTime;

/**
 * 세션 상태 응답
 * 
 * 세션 상태 조회 결과를 담는 불변 응답 객체입니다.
 */
public record SessionStatusResponse(
    String sessionId,           // 세션 ID
    String primaryUserId,       // 주 사용자 ID
    String primaryClientIp,     // 주 클라이언트 IP
    boolean isLocked,           // 잠금 상태
    LocalDateTime lockedUntil,  // 잠금 해제 시각
    LocalDateTime createdAt,    // 생성 시각
    LocalDateTime lastActivityAt, // 마지막 활동 시각
    int totalAttempts,          // 총 인증 시도 횟수
    int failedAttempts          // 실패한 인증 시도 횟수
) {
    /**
     * 활성 세션 응답 생성
     */
    public static SessionStatusResponse active(String sessionId, String primaryUserId, String primaryClientIp,
                                             LocalDateTime createdAt, LocalDateTime lastActivityAt,
                                             int totalAttempts, int failedAttempts) {
        return new SessionStatusResponse(sessionId, primaryUserId, primaryClientIp, false, null,
                createdAt, lastActivityAt, totalAttempts, failedAttempts);
    }
    
    /**
     * 잠긴 세션 응답 생성
     */
    public static SessionStatusResponse locked(String sessionId, String primaryUserId, String primaryClientIp,
                                             LocalDateTime lockedUntil, LocalDateTime createdAt, LocalDateTime lastActivityAt,
                                             int totalAttempts, int failedAttempts) {
        return new SessionStatusResponse(sessionId, primaryUserId, primaryClientIp, true, lockedUntil,
                createdAt, lastActivityAt, totalAttempts, failedAttempts);
    }
}