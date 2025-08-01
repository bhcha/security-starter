package com.dx.hexacore.security.session.application.query.port.in;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 실패한 인증 시도 목록 응답
 * 
 * 실패한 인증 시도 목록 조회 결과를 담는 불변 응답 객체입니다.
 */
public record FailedAttemptsResponse(
    String sessionId,                       // 세션 ID
    List<FailedAttemptResponse> attempts,   // 실패한 시도 목록
    int totalCount,                         // 전체 개수
    LocalDateTime queriedAt                 // 쿼리 실행 시각
) {
    /**
     * 정적 팩토리 메서드
     */
    public static FailedAttemptsResponse of(String sessionId, List<FailedAttemptResponse> attempts, 
                                          int totalCount, LocalDateTime queriedAt) {
        return new FailedAttemptsResponse(sessionId, attempts, totalCount, queriedAt);
    }
    
    /**
     * 빈 응답 생성
     */
    public static FailedAttemptsResponse empty(String sessionId, LocalDateTime queriedAt) {
        return new FailedAttemptsResponse(sessionId, List.of(), 0, queriedAt);
    }
}