package com.ldx.hexacore.security.session.application.command.port.in;

import com.ldx.hexacore.security.session.application.exception.SessionNotFoundException;

/**
 * 계정 잠금 상태 확인 유스케이스
 * 
 * 특정 사용자의 계정 잠금 상태를 확인합니다.
 */
public interface CheckLockoutUseCase {
    
    /**
     * 계정 잠금 상태 확인 실행
     * 
     * @param sessionId 확인할 세션 ID
     * @param userId 확인할 사용자 ID
     * @return 잠금 상태 확인 결과
     * @throws SessionNotFoundException 세션을 찾을 수 없는 경우
     */
    LockoutCheckResult execute(String sessionId, String userId);
}