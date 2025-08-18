package com.dx.hexacore.security.session.application.command.handler;

import com.dx.hexacore.security.session.application.command.port.in.*;
import org.springframework.stereotype.Service;

/**
 * Session Management Use Case Implementation - Facade pattern
 * 
 * This implementation delegates to individual use case implementations
 * providing a unified interface for session management operations.
 */
@Service
class SessionManagementUseCaseImpl implements SessionManagementUseCase {
    
    private final CheckLockoutUseCase checkLockoutUseCase;
    private final RecordAttemptUseCase recordAttemptUseCase;
    private final UnlockAccountUseCase unlockAccountUseCase;
    
    public SessionManagementUseCaseImpl(
            CheckLockoutUseCase checkLockoutUseCase,
            RecordAttemptUseCase recordAttemptUseCase,
            UnlockAccountUseCase unlockAccountUseCase) {
        this.checkLockoutUseCase = checkLockoutUseCase;
        this.recordAttemptUseCase = recordAttemptUseCase;
        this.unlockAccountUseCase = unlockAccountUseCase;
    }
    
    @Override
    public LockoutCheckResult checkLockout(String sessionId, String userId) {
        return checkLockoutUseCase.execute(sessionId, userId);
    }
    
    @Override
    public RecordAttemptResult recordAttempt(RecordAuthenticationAttemptCommand command) {
        return recordAttemptUseCase.execute(command);
    }
    
    @Override
    public UnlockAccountResult unlockAccount(UnlockAccountCommand command) {
        return unlockAccountUseCase.execute(command);
    }
}