package com.dx.hexacore.security.session.application.command.port.out;

import com.dx.hexacore.security.session.domain.AuthenticationSession;
import com.dx.hexacore.security.session.domain.vo.SessionId;

import java.util.Optional;

/**
 * 인증 세션 저장소 포트
 * 
 * AuthenticationSession 애그리거트의 영속성을 담당하는 아웃바운드 포트입니다.
 */
public interface AuthenticationSessionRepository {
    
    /**
     * 세션 ID로 인증 세션 조회
     * 
     * @param sessionId 조회할 세션 ID
     * @return 조회된 인증 세션 (없으면 Optional.empty())
     */
    Optional<AuthenticationSession> findBySessionId(SessionId sessionId);
    
    /**
     * 인증 세션 저장
     * 
     * @param session 저장할 인증 세션
     * @return 저장된 인증 세션
     */
    AuthenticationSession save(AuthenticationSession session);
    
    /**
     * 인증 세션 삭제
     * 
     * @param sessionId 삭제할 세션 ID
     */
    void delete(SessionId sessionId);
}