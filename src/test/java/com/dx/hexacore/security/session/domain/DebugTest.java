package com.dx.hexacore.security.session.domain;

import com.dx.hexacore.security.session.domain.vo.ClientIp;
import com.dx.hexacore.security.session.domain.vo.RiskLevel;
import com.dx.hexacore.security.session.domain.vo.SessionId;
import org.junit.jupiter.api.Test;

public class DebugTest {
    
    @Test
    void debugFailedAttemptsCount() {
        // Given
        AuthenticationSession session = AuthenticationSession.create(
            SessionId.generate(), "user123", ClientIp.of("192.168.1.100")
        );
        
        System.out.println("Initial failed attempts: " + session.getFailedAttemptsInWindow());
        
        // 4번의 실패 시도 추가
        for (int i = 0; i < 4; i++) {
            session.recordAttempt("user123", ClientIp.of("192.168.1.100"), 
                                false, RiskLevel.low("Attempt " + (i + 1)));
            System.out.println("After attempt " + (i + 1) + ": " + session.getFailedAttemptsInWindow());
            System.out.println("Should lock: " + session.shouldLockAccount());
            System.out.println("Is locked: " + session.isCurrentlyLocked());
            System.out.println("---");
        }
    }
}