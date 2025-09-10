package com.ldx.hexacore.security.session.application.projection;

import com.ldx.hexacore.security.session.application.query.port.in.FailedAttemptResponse;
import com.ldx.hexacore.security.session.domain.AuthenticationAttempt;

import java.time.LocalDateTime;

/**
 * 실패한 인증 시도 프로젝션
 * 
 * AuthenticationAttempt 도메인 객체를 읽기 최적화된 형태로 변환하는 프로젝션 클래스입니다.
 */
public record FailedAttemptProjection(
    String userId,              // 사용자 ID
    String clientIp,            // 클라이언트 IP
    int riskScore,              // 위험도 점수
    String riskReason,          // 위험도 이유
    LocalDateTime attemptedAt   // 시도 시각
) {
    
    /**
     * 실패한 AuthenticationAttempt으로부터 프로젝션 생성
     */
    public static FailedAttemptProjection from(AuthenticationAttempt attempt) {
        if (attempt.isSuccessful()) {
            throw new IllegalArgumentException("Cannot create projection from successful attempt");
        }
        
        return new FailedAttemptProjection(
                attempt.getUserId(),
                attempt.getClientIp().getIpAddress(),
                attempt.getRiskLevel().getScore(),
                attempt.getRiskLevel().getReason(),
                attempt.getAttemptedAt()
        );
    }
    
    /**
     * FailedAttemptResponse로 변환
     */
    public FailedAttemptResponse toResponse() {
        return new FailedAttemptResponse(userId, clientIp, riskScore, riskReason, attemptedAt);
    }
}