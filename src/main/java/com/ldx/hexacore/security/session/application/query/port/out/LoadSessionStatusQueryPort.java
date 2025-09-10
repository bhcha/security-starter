package com.ldx.hexacore.security.session.application.query.port.out;

import com.ldx.hexacore.security.session.application.projection.SessionStatusProjection;

import java.util.Optional;

/**
 * 세션 상태 조회 쿼리 포트
 * 
 * 세션 상태 조회를 위한 아웃바운드 포트입니다.
 */
public interface LoadSessionStatusQueryPort {
    
    /**
     * 세션 상태 조회
     * 
     * @param sessionId 조회할 세션 ID
     * @return 세션 상태 프로젝션 (없으면 Optional.empty())
     */
    Optional<SessionStatusProjection> loadSessionStatus(String sessionId);
    
    /**
     * 사용자별 세션 상태 조회
     * 
     * @param sessionId 조회할 세션 ID
     * @param userId 조회할 사용자 ID
     * @return 사용자 관련 정보가 필터링된 세션 상태 프로젝션
     */
    Optional<SessionStatusProjection> loadSessionStatusByUser(String sessionId, String userId);
}