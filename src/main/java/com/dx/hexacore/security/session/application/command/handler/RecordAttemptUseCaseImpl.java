package com.dx.hexacore.security.session.application.command.handler;

import com.dx.hexacore.security.session.application.command.port.in.RecordAuthenticationAttemptCommand;
import com.dx.hexacore.security.session.application.exception.SessionNotFoundException;
import com.dx.hexacore.security.session.application.command.port.out.AuthenticationSessionRepository;
import com.dx.hexacore.security.session.application.command.port.out.SessionEventPublisher;
import com.dx.hexacore.security.session.application.command.port.in.RecordAttemptResult;
import com.dx.hexacore.security.session.application.command.port.in.RecordAttemptUseCase;
import com.dx.hexacore.security.session.domain.AuthenticationSession;
import com.dx.hexacore.security.session.domain.vo.ClientIp;
import com.dx.hexacore.security.session.domain.vo.RiskLevel;
import com.dx.hexacore.security.session.domain.vo.SessionId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 인증 시도 기록 유스케이스 구현체
 * 
 * 사용자의 인증 시도를 세션에 기록하고, 계정 잠금 정책을 적용합니다.
 */
@Service
@Transactional
class RecordAttemptUseCaseImpl implements RecordAttemptUseCase {
    
    private final AuthenticationSessionRepository sessionRepository;
    private final SessionEventPublisher eventPublisher;
    
    public RecordAttemptUseCaseImpl(AuthenticationSessionRepository sessionRepository,
                                   SessionEventPublisher eventPublisher) {
        this.sessionRepository = sessionRepository;
        this.eventPublisher = eventPublisher;
    }
    
    @Override
    public RecordAttemptResult execute(RecordAuthenticationAttemptCommand command) {
        // 1. 세션 조회
        SessionId sessionId = SessionId.of(command.sessionId());
        AuthenticationSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(command.sessionId()));
        
        // 2. 도메인 객체 생성
        ClientIp clientIp = ClientIp.of(command.clientIp());
        RiskLevel riskLevel = RiskLevel.of(command.riskScore(), command.riskReason());
        
        // 3. 인증 시도 기록 (도메인 로직 실행)
        session.recordAttempt(command.userId(), clientIp, command.isSuccessful(), riskLevel);
        
        // 4. 세션 저장
        sessionRepository.save(session);
        
        // 5. 도메인 이벤트 발행
        eventPublisher.publishAll(session.getDomainEvents());
        
        // 6. 결과 생성 및 반환
        LocalDateTime recordedAt = LocalDateTime.now();
        boolean isAccountLocked = session.isCurrentlyLocked();
        LocalDateTime lockedUntil = session.getLockedUntil();
        
        return new RecordAttemptResult(
                command.sessionId(),
                command.userId(),
                command.isSuccessful(),
                isAccountLocked,
                lockedUntil,
                recordedAt
        );
    }
}