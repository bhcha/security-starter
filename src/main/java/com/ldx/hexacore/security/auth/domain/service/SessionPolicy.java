package com.ldx.hexacore.security.auth.domain.service;

import com.ldx.hexacore.security.auth.domain.Authentication;

import java.time.LocalDateTime;

public class SessionPolicy {
    
    private static final int SESSION_TIMEOUT_HOURS = 24;

    public boolean validateSession(Authentication authentication) {
        if (authentication == null) {
            return false;
        }

        return isSessionActive(authentication) && 
               isSessionNotExpired(authentication) &&
               authentication.isTokenValid();
    }

    private boolean isSessionActive(Authentication authentication) {
        return authentication.getStatus().isSuccess();
    }

    private boolean isSessionNotExpired(Authentication authentication) {
        LocalDateTime attemptTime = authentication.getAttemptTime();
        LocalDateTime expirationTime = attemptTime.plusHours(SESSION_TIMEOUT_HOURS);
        return LocalDateTime.now().isBefore(expirationTime);
    }
}