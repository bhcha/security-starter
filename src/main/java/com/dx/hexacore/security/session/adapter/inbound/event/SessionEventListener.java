package com.dx.hexacore.security.session.adapter.inbound.event;

import com.dx.hexacore.security.session.application.command.port.in.RecordAuthenticationAttemptCommand;
import com.dx.hexacore.security.session.application.command.port.in.RecordAttemptUseCase;
import com.dx.hexacore.security.session.application.command.port.in.CheckLockoutUseCase;
import com.dx.hexacore.security.auth.domain.event.AuthenticationAttempted;
import com.dx.hexacore.security.auth.domain.event.AuthenticationSucceeded;
import com.dx.hexacore.security.auth.domain.event.AuthenticationFailed;
import com.dx.hexacore.security.session.domain.event.AccountLocked;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

/**
 * 세션 관련 이벤트를 수신하여 처리하는 Event Listener
 * 
 * Authentication 애그리거트로부터 발생하는 이벤트를 수신하여
 * AuthenticationSession 애그리거트의 Use Case를 호출합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean({RecordAttemptUseCase.class, CheckLockoutUseCase.class})
class SessionEventListener {
    
    private final RecordAttemptUseCase recordAttemptUseCase;
    private final CheckLockoutUseCase checkLockoutUseCase;
    
    /**
     * 인증 시도 이벤트 처리
     * 
     * @param event AuthenticationAttempted 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAuthenticationAttempted(AuthenticationAttempted event) {
        log.debug("Handling authentication attempted event for aggregate: {}", event.getAuthenticationId());
        
        // null 체크
        if (event.getUsername() == null) {
            log.warn("Received authentication attempted event with null username for aggregate: {}", 
                     event.getAuthenticationId());
            return;
        }
        
        try {
            RecordAuthenticationAttemptCommand command = new RecordAuthenticationAttemptCommand(
                event.getAuthenticationId().toString(),
                event.getUsername(),
                extractClientIp(event), // 실제 구현에서는 event에서 IP를 추출하거나 다른 방법 사용
                false, // attempting 상태는 성공/실패 판단 전
                calculateRiskScore(event),
                "Authentication attempt"
            );
            
            recordAttemptUseCase.execute(command);
            
        } catch (Exception e) {
            log.error("Error handling authentication attempted event for aggregate: {}", 
                     event.getAuthenticationId(), e);
        }
    }
    
    /**
     * 인증 성공 이벤트 처리
     * 
     * @param event AuthenticationSucceeded 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAuthenticationSucceeded(AuthenticationSucceeded event) {
        log.debug("Handling authentication succeeded event for aggregate: {}", event.getAuthenticationId());
        
        try {
            // 성공 시에는 username을 token에서 추출해야 할 수도 있음
            String username = extractUsernameFromToken(event);
            
            RecordAuthenticationAttemptCommand command = new RecordAuthenticationAttemptCommand(
                event.getAuthenticationId().toString(),
                username,
                "0.0.0.0", // 실제 구현에서는 적절한 IP 추출
                true, // 성공
                0, // 성공 시 위험도 0
                "Authentication successful"
            );
            
            recordAttemptUseCase.execute(command);
            
        } catch (Exception e) {
            log.error("Error handling authentication succeeded event for aggregate: {}", 
                     event.getAuthenticationId(), e);
        }
    }
    
    /**
     * 인증 실패 이벤트 처리
     * 
     * @param event AuthenticationFailed 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAuthenticationFailed(AuthenticationFailed event) {
        log.debug("Handling authentication failed event for aggregate: {}", event.getAuthenticationId());
        
        try {
            // 실패 시에는 aggregate ID로부터 username을 추출해야 할 수도 있음
            String username = extractUsernameFromAuthenticationId(event.getAuthenticationId().toString());
            
            RecordAuthenticationAttemptCommand command = new RecordAuthenticationAttemptCommand(
                event.getAuthenticationId().toString(),
                username,
                "0.0.0.0", // 실제 구현에서는 적절한 IP 추출
                false, // 실패
                calculateRiskScoreForFailure(event.getReason()),
                event.getReason()
            );
            
            recordAttemptUseCase.execute(command);
            
            // 실패 후 잠금 상태 확인
            checkLockoutUseCase.execute(event.getAuthenticationId().toString(), username);
            
        } catch (Exception e) {
            log.error("Error handling authentication failed event for aggregate: {}", 
                     event.getAuthenticationId(), e);
        }
    }
    
    /**
     * 계정 잠금 이벤트 처리
     * 
     * @param event AccountLocked 이벤트
     */
    @EventListener
    public void handleAccountLocked(AccountLocked event) {
        log.warn("Account locked for user: {} in session: {} until: {}", 
                event.userId(), event.sessionId(), event.lockedUntil());
        
        // 추가적인 알림 처리가 필요한 경우 여기에 구현
        // 예: 관리자 알림, 사용자 이메일 발송 등
    }
    
    /**
     * 이벤트로부터 클라이언트 IP 추출
     * 
     * 실제 구현에서는 이벤트에 IP 정보가 포함되거나
     * SecurityContext, HttpServletRequest 등에서 추출
     */
    private String extractClientIp(AuthenticationAttempted event) {
        // TODO: 실제 구현 필요
        return "0.0.0.0";
    }
    
    /**
     * 위험도 점수 계산
     */
    private int calculateRiskScore(AuthenticationAttempted event) {
        // TODO: 실제 위험도 계산 로직 구현
        // 예: IP 위치, 시간대, 이전 실패 횟수 등 고려
        return 50;
    }
    
    /**
     * 실패 사유에 따른 위험도 점수 계산
     */
    private int calculateRiskScoreForFailure(String reason) {
        // TODO: 실패 사유에 따른 위험도 차등 적용
        return 75;
    }
    
    /**
     * 토큰으로부터 username 추출
     */
    private String extractUsernameFromToken(AuthenticationSucceeded event) {
        // TODO: 실제 구현에서는 JWT 토큰 파싱하여 username 추출
        return "unknown";
    }
    
    /**
     * Aggregate ID로부터 username 추출
     */
    private String extractUsernameFromAuthenticationId(String aggregateId) {
        // TODO: 실제 구현에서는 repository를 통해 조회하거나 캠시 활용
        return "unknown";
    }
}