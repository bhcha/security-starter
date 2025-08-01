package com.dx.hexacore.security.session.application.projection;

import com.dx.hexacore.security.session.application.query.port.in.SessionStatusResponse;
import com.dx.hexacore.security.session.domain.AuthenticationSession;

import java.time.LocalDateTime;

/**
 * 세션 상태 프로젝션
 * 
 * AuthenticationSession 도메인 객체를 읽기 최적화된 형태로 변환하는 프로젝션 클래스입니다.
 */
public record SessionStatusProjection(
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
     * AuthenticationSession으로부터 프로젝션 생성
     */
    public static SessionStatusProjection from(AuthenticationSession session) {
        return new SessionStatusProjection(
                session.getSessionId().getValue().toString(),
                session.getUserId(),
                session.getClientIp().getIpAddress(),
                session.isCurrentlyLocked(),
                session.getLockedUntil(),
                session.getCreatedAt(),
                session.getLastActivityAt(),
                session.getAttempts().size(),
                (int) session.getAttempts().stream()
                        .filter(attempt -> !attempt.isSuccessful())
                        .count()
        );
    }
    
    /**
     * SessionStatusResponse로 변환
     */
    public SessionStatusResponse toResponse() {
        return new SessionStatusResponse(
                sessionId, primaryUserId, primaryClientIp, isLocked, lockedUntil,
                createdAt, lastActivityAt, totalAttempts, failedAttempts
        );
    }
}