package com.dx.hexacore.security.session.application.query.port.in;

import com.dx.hexacore.security.util.ValidationMessages;

/**
 * 세션 상태 조회 쿼리
 * 
 * 특정 세션의 현재 상태를 조회하기 위한 쿼리 객체입니다.
 * 사용자 ID가 포함된 경우 해당 사용자 관련 정보를 필터링할 수 있습니다.
 */
public record GetSessionStatusQuery(
    String sessionId,    // 조회할 세션 ID
    String userId        // 조회할 사용자 ID (optional)
) {
    
    public GetSessionStatusQuery {
        validateSessionId(sessionId);
    }
    
    private void validateSessionId(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNullOrEmpty("SessionId"));
        }
    }
}