package com.dx.hexacore.security.session.application.command.port.in;

import com.dx.hexacore.security.util.ValidationMessages;

/**
 * 계정 잠금 해제 명령
 * 
 * 특정 세션의 특정 사용자 계정 잠금을 해제하기 위한 명령 객체입니다.
 */
public record UnlockAccountCommand(
    String sessionId,    // 세션 ID
    String userId        // 잠금 해제할 사용자 ID
) {
    
    public UnlockAccountCommand {
        validateSessionId(sessionId);
        validateUserId(userId);
    }
    
    private void validateSessionId(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNullOrEmpty("SessionId"));
        }
    }
    
    private void validateUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNullOrEmpty("UserId"));
        }
    }
}