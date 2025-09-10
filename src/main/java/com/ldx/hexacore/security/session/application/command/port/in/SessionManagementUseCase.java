package com.ldx.hexacore.security.session.application.command.port.in;

/**
 * Session Management Use Case - Facade interface
 * 
 * This interface provides a unified entry point for all session management operations.
 * It combines the functionality of CheckLockoutUseCase, RecordAttemptUseCase, and UnlockAccountUseCase.
 */
public interface SessionManagementUseCase {
    
    /**
     * Check if account is locked
     * 
     * @param sessionId the session ID
     * @param userId the user ID to check
     * @return lockout check result
     */
    LockoutCheckResult checkLockout(String sessionId, String userId);
    
    /**
     * Record authentication attempt
     * 
     * @param command the record attempt command
     * @return record attempt result
     */
    RecordAttemptResult recordAttempt(RecordAuthenticationAttemptCommand command);
    
    /**
     * Unlock account
     * 
     * @param command the unlock account command
     * @return unlock account result
     */
    UnlockAccountResult unlockAccount(UnlockAccountCommand command);
}