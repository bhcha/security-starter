package com.ldx.hexacore.security.session.application.command.handler;

import com.ldx.hexacore.security.session.application.exception.SessionNotFoundException;
import com.ldx.hexacore.security.session.application.command.port.out.AuthenticationSessionRepository;
import com.ldx.hexacore.security.session.application.command.port.in.LockoutCheckResult;
import com.ldx.hexacore.security.session.application.command.port.in.CheckLockoutUseCase;
import com.ldx.hexacore.security.session.domain.AuthenticationSession;
import com.ldx.hexacore.security.session.domain.vo.SessionId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 계정 잠금 상태 확인 유스케이스 구현체
 * 
 * 특정 사용자의 계정 잠금 상태를 확인합니다.
 */
@Service
@Transactional(readOnly = true)
class CheckLockoutUseCaseImpl implements CheckLockoutUseCase {
    
    private final AuthenticationSessionRepository sessionRepository;
    
    public CheckLockoutUseCaseImpl(AuthenticationSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }
    
    @Override
    public LockoutCheckResult execute(String sessionId, String userId) {
        // 1. 세션 조회
        SessionId sessionIdObj = SessionId.of(sessionId);
        AuthenticationSession session = sessionRepository.findBySessionId(sessionIdObj)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));
        
        // 2. 계정 잠금 상태 확인
        boolean isLocked = session.isCurrentlyLocked();
        LocalDateTime lockedUntil = session.getLockedUntil();
        LocalDateTime checkedAt = LocalDateTime.now();
        
        // 3. 결과 생성 및 반환
        return new LockoutCheckResult(
                sessionId,
                userId,
                isLocked,
                lockedUntil,
                checkedAt
        );
    }
}