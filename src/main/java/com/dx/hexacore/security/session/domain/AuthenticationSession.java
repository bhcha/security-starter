package com.dx.hexacore.security.session.domain;

import com.dx.hexacore.security.auth.domain.AggregateRoot;
import com.dx.hexacore.security.session.domain.event.AccountLocked;
import com.dx.hexacore.security.session.domain.vo.ClientIp;
import com.dx.hexacore.security.session.domain.vo.RiskLevel;
import com.dx.hexacore.security.session.domain.vo.SessionId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 인증 세션 애그리거트 루트
 */
public class AuthenticationSession extends AggregateRoot {
    
    // 정책 상수
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;
    private static final int TIME_WINDOW_MINUTES = 15;
    
    private final SessionId sessionId;
    private final String userId;
    private final ClientIp clientIp;
    private final List<AuthenticationAttempt> attempts;
    private boolean isLocked;
    private LocalDateTime lockedUntil;
    private final LocalDateTime createdAt;
    private LocalDateTime lastActivityAt;

    private AuthenticationSession(SessionId sessionId, String userId, ClientIp clientIp) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.clientIp = clientIp;
        this.attempts = new ArrayList<>();
        this.isLocked = false;
        this.lockedUntil = null;
        this.createdAt = LocalDateTime.now();
        this.lastActivityAt = LocalDateTime.now();
    }

    /**
     * AuthenticationSession 생성 팩토리 메서드
     */
    public static AuthenticationSession create(SessionId sessionId, String userId, ClientIp clientIp) {
        validateParameters(sessionId, userId, clientIp);
        return new AuthenticationSession(sessionId, userId, clientIp);
    }

    private static void validateParameters(SessionId sessionId, String userId, ClientIp clientIp) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (clientIp == null) {
            throw new IllegalArgumentException("Client IP cannot be null");
        }
    }

    /**
     * 인증 시도를 기록
     */
    public void recordAttempt(String userId, ClientIp clientIp, boolean isSuccessful, RiskLevel riskLevel) {
        recordAttemptAtSpecificTime(userId, clientIp, isSuccessful, riskLevel, LocalDateTime.now());
    }

    /**
     * 특정 시각에 인증 시도를 기록 (테스트용)
     */
    public void recordAttemptAtSpecificTime(String userId, ClientIp clientIp, boolean isSuccessful, 
                                          RiskLevel riskLevel, LocalDateTime attemptTime) {
        AuthenticationAttempt attempt = AuthenticationAttempt.create(userId, attemptTime, isSuccessful, clientIp, riskLevel);
        attempts.add(attempt);
        
        // 시간 순으로 정렬
        attempts.sort((a, b) -> a.getAttemptedAt().compareTo(b.getAttemptedAt()));
        
        updateLastActivity();
        
        // 성공한 경우 잠금 해제
        if (isSuccessful && isLocked) {
            unlockAccount();
        }
        
        // 실패한 경우 잠금 필요 여부 확인
        if (!isSuccessful && shouldLockAccount()) {
            lockAccount();
        }
    }

    /**
     * 계정 잠금 필요 여부 판단
     */
    public boolean shouldLockAccount() {
        if (isCurrentlyLocked()) {
            return false; // 이미 잠금 상태
        }
        
        return getFailedAttemptsInWindow() >= MAX_FAILED_ATTEMPTS;
    }

    /**
     * 계정 잠금 실행
     */
    public void lockAccount() {
        if (isCurrentlyLocked()) {
            return; // 이미 잠금 상태
        }
        
        this.isLocked = true;
        this.lockedUntil = LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES);
        
        // 도메인 이벤트 발행
        AccountLocked event = AccountLocked.of(
            sessionId.getValue().toString(),
            userId,
            clientIp.getIpAddress(),
            lockedUntil,
            getFailedAttemptsInWindow(),
            LocalDateTime.now()
        );
        
        addDomainEvent(event);
        updateLastActivity();
    }

    /**
     * 계정 잠금 해제
     */
    public void unlockAccount() {
        this.isLocked = false;
        this.lockedUntil = null;
        updateLastActivity();
    }

    /**
     * 현재 잠금 상태 확인
     */
    public boolean isCurrentlyLocked() {
        if (!isLocked) {
            return false;
        }
        
        if (lockedUntil != null && LocalDateTime.now().isAfter(lockedUntil)) {
            // 잠금 시간이 만료되면 자동으로 해제
            unlockAccount();
            return false;
        }
        
        return true;
    }

    /**
     * 시간 윈도우 내 실패 횟수 조회
     */
    public int getFailedAttemptsInWindow() {
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(TIME_WINDOW_MINUTES);
        
        // 마지막 성공한 시도 이후의 실패만 카운트
        LocalDateTime lastSuccessTime = getLastSuccessfulAttemptTime();
        if (lastSuccessTime != null && lastSuccessTime.isAfter(windowStart)) {
            windowStart = lastSuccessTime;
        }
        
        final LocalDateTime finalWindowStart = windowStart;
        
        return (int) attempts.stream()
            .filter(attempt -> !attempt.isSuccessful())
            .filter(attempt -> attempt.isWithinTimeWindow(finalWindowStart))
            .count();
    }
    
    private LocalDateTime getLastSuccessfulAttemptTime() {
        return attempts.stream()
            .filter(AuthenticationAttempt::isSuccessful)
            .map(AuthenticationAttempt::getAttemptedAt)
            .max(LocalDateTime::compareTo)
            .orElse(null);
    }

    /**
     * 마지막 활동 시각 갱신
     */
    public void updateLastActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }

    /**
     * 잠금 상태 강제 설정 (테스트용)
     */
    public void forceSetLockStatus(boolean isLocked, LocalDateTime lockedUntil) {
        this.isLocked = isLocked;
        this.lockedUntil = lockedUntil;
    }

    // Getters
    public SessionId getSessionId() {
        return sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public ClientIp getClientIp() {
        return clientIp;
    }

    public List<AuthenticationAttempt> getAttempts() {
        return new ArrayList<>(attempts);
    }

    public boolean isLocked() {
        return isLocked;
    }

    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    @Override
    public String toString() {
        return "AuthenticationSession{" +
               "sessionId=" + sessionId +
               ", userId='" + userId + '\'' +
               ", clientIp=" + clientIp +
               ", attemptsCount=" + attempts.size() +
               ", isLocked=" + isLocked +
               ", lockedUntil=" + lockedUntil +
               ", createdAt=" + createdAt +
               ", lastActivityAt=" + lastActivityAt +
               '}';
    }
}