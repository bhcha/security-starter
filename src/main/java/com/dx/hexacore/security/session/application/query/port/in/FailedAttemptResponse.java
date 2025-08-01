package com.dx.hexacore.security.session.application.query.port.in;

import java.time.LocalDateTime;

/**
 * 실패한 인증 시도 응답
 * 
 * 실패한 인증 시도 정보를 담는 불변 응답 객체입니다.
 */
public record FailedAttemptResponse(
    String userId,              // 사용자 ID
    String clientIp,            // 클라이언트 IP
    int riskScore,              // 위험도 점수
    String riskReason,          // 위험도 이유
    LocalDateTime attemptedAt   // 시도 시각
) {
    /**
     * 정적 팩토리 메서드
     */
    public static FailedAttemptResponse of(String userId, String clientIp, int riskScore, 
                                         String riskReason, LocalDateTime attemptedAt) {
        return new FailedAttemptResponse(userId, clientIp, riskScore, riskReason, attemptedAt);
    }
}