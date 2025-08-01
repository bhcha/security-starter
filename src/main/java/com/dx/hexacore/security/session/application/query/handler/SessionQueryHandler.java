package com.dx.hexacore.security.session.application.query.handler;

import com.dx.hexacore.security.session.application.exception.SessionNotFoundException;
import com.dx.hexacore.security.session.application.exception.SessionQueryException;
import com.dx.hexacore.security.session.application.query.port.out.LoadFailedAttemptsQueryPort;
import com.dx.hexacore.security.session.application.query.port.out.LoadSessionStatusQueryPort;
import com.dx.hexacore.security.session.application.projection.FailedAttemptProjection;
import com.dx.hexacore.security.session.application.projection.SessionStatusProjection;
import com.dx.hexacore.security.session.application.query.port.in.GetFailedAttemptsQuery;
import com.dx.hexacore.security.session.application.query.port.in.GetSessionStatusQuery;
import com.dx.hexacore.security.session.application.query.port.in.FailedAttemptResponse;
import com.dx.hexacore.security.session.application.query.port.in.FailedAttemptsResponse;
import com.dx.hexacore.security.session.application.query.port.in.SessionStatusResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 세션 쿼리 핸들러
 * 
 * 세션 관련 모든 쿼리를 처리하는 핸들러입니다.
 */
@Service
@Transactional(readOnly = true)
class SessionQueryHandler {
    
    private final LoadSessionStatusQueryPort loadSessionStatusQueryPort;
    private final LoadFailedAttemptsQueryPort loadFailedAttemptsQueryPort;
    
    public SessionQueryHandler(LoadSessionStatusQueryPort loadSessionStatusQueryPort,
                              LoadFailedAttemptsQueryPort loadFailedAttemptsQueryPort) {
        this.loadSessionStatusQueryPort = loadSessionStatusQueryPort;
        this.loadFailedAttemptsQueryPort = loadFailedAttemptsQueryPort;
    }
    
    /**
     * 세션 상태 조회 처리
     */
    public SessionStatusResponse handle(GetSessionStatusQuery query) {
        try {
            SessionStatusProjection projection = (query.userId() != null) 
                ? loadSessionStatusQueryPort.loadSessionStatusByUser(query.sessionId(), query.userId())
                    .orElseThrow(() -> new SessionNotFoundException(query.sessionId()))
                : loadSessionStatusQueryPort.loadSessionStatus(query.sessionId())
                    .orElseThrow(() -> new SessionNotFoundException(query.sessionId()));
            
            return projection.toResponse();
            
        } catch (SessionNotFoundException e) {
            throw e; // 세션 없음 예외는 그대로 전파
        } catch (Exception e) {
            throw new SessionQueryException("Failed to load session status: " + query.sessionId(), e);
        }
    }
    
    /**
     * 실패한 인증 시도 목록 조회 처리
     */
    public FailedAttemptsResponse handle(GetFailedAttemptsQuery query) {
        try {
            List<FailedAttemptProjection> projections = (query.userId() != null)
                ? loadFailedAttemptsQueryPort.loadFailedAttemptsByUser(
                    query.sessionId(), query.userId(), query.from(), query.to(), query.limit())
                : loadFailedAttemptsQueryPort.loadFailedAttempts(
                    query.sessionId(), query.from(), query.to(), query.limit());
            
            int totalCount = (query.userId() != null)
                ? loadFailedAttemptsQueryPort.countFailedAttemptsByUser(
                    query.sessionId(), query.userId(), query.from(), query.to())
                : loadFailedAttemptsQueryPort.countFailedAttempts(
                    query.sessionId(), query.from(), query.to());
            
            List<FailedAttemptResponse> attempts = projections.stream()
                    .map(FailedAttemptProjection::toResponse)
                    .toList();
            
            return FailedAttemptsResponse.of(query.sessionId(), attempts, totalCount, LocalDateTime.now());
            
        } catch (Exception e) {
            throw new SessionQueryException("Failed to load failed attempts: " + query.sessionId(), e);
        }
    }
}