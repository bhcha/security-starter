package com.dx.hexacore.security.session.application.command.handler;

import com.dx.hexacore.security.session.application.command.port.in.UnlockAccountCommand;
import com.dx.hexacore.security.session.application.exception.SessionNotFoundException;
import com.dx.hexacore.security.session.application.command.port.out.AuthenticationSessionRepository;
import com.dx.hexacore.security.session.application.command.port.in.UnlockAccountResult;
import com.dx.hexacore.security.session.application.command.port.in.UnlockAccountUseCase;
import com.dx.hexacore.security.session.domain.AuthenticationSession;
import com.dx.hexacore.security.session.domain.vo.SessionId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 계정 잠금 해제 유스케이스 구현체
 * 
 * 특정 사용자의 계정 잠금을 명시적으로 해제합니다.
 */
@Service
@Transactional
class UnlockAccountUseCaseImpl implements UnlockAccountUseCase {
    
    private final AuthenticationSessionRepository sessionRepository;
    
    public UnlockAccountUseCaseImpl(AuthenticationSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }
    
    @Override
    public UnlockAccountResult execute(UnlockAccountCommand command) {
        // 1. 세션 조회
        SessionId sessionId = SessionId.of(command.sessionId());
        AuthenticationSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(command.sessionId()));
        
        // 2. 현재 잠금 상태 확인
        boolean wasLocked = session.isCurrentlyLocked();
        
        // 3. 계정 잠금 해제 (도메인 로직 실행)
        session.unlockAccount();
        
        // 4. 세션 저장
        sessionRepository.save(session);
        
        // 5. 결과 생성 및 반환
        LocalDateTime unlockedAt = LocalDateTime.now();
        
        return new UnlockAccountResult(
                command.sessionId(),
                command.userId(),
                wasLocked,
                true,  // 항상 성공 (도메인 로직에서 예외 처리)
                unlockedAt
        );
    }
}