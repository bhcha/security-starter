package com.dx.hexacore.security.session.application.query.port.in;

import java.time.LocalDateTime;

/**
 * 실패한 인증 시도 목록 조회 쿼리
 * 
 * 특정 세션의 실패한 인증 시도 목록을 시간 범위와 제한 개수로 조회하기 위한 쿼리 객체입니다.
 */
public record GetFailedAttemptsQuery(
    String sessionId,           // 세션 ID
    String userId,              // 사용자 ID (optional)
    LocalDateTime from,         // 조회 시작 시간
    LocalDateTime to,           // 조회 종료 시간
    int limit                   // 조회 제한 개수
) {
    
    public GetFailedAttemptsQuery {
        validateSessionId(sessionId);
        validateTimeRange(from, to);
        validateLimit(limit);
    }
    
    private void validateSessionId(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
    }
    
    private void validateTimeRange(LocalDateTime from, LocalDateTime to) {
        if (from == null) {
            throw new IllegalArgumentException("From time cannot be null");
        }
        if (to == null) {
            throw new IllegalArgumentException("To time cannot be null");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("From time cannot be after to time");
        }
    }
    
    private void validateLimit(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Limit cannot be negative");
        }
    }
}