package com.dx.hexacore.security.session.application.query.port.out;

import com.dx.hexacore.security.session.application.projection.FailedAttemptProjection;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 실패한 인증 시도 조회 쿼리 포트
 * 
 * 실패한 인증 시도 조회를 위한 아웃바운드 포트입니다.
 */
public interface LoadFailedAttemptsQueryPort {
    
    /**
     * 실패한 인증 시도 목록 조회
     * 
     * @param sessionId 세션 ID
     * @param from 조회 시작 시간
     * @param to 조회 종료 시간
     * @param limit 조회 제한 개수
     * @return 실패한 인증 시도 프로젝션 목록
     */
    List<FailedAttemptProjection> loadFailedAttempts(String sessionId, 
                                                    LocalDateTime from, LocalDateTime to, int limit);
    
    /**
     * 사용자별 실패한 인증 시도 목록 조회
     * 
     * @param sessionId 세션 ID
     * @param userId 사용자 ID
     * @param from 조회 시작 시간
     * @param to 조회 종료 시간
     * @param limit 조회 제한 개수
     * @return 해당 사용자의 실패한 인증 시도 프로젝션 목록
     */
    List<FailedAttemptProjection> loadFailedAttemptsByUser(String sessionId, String userId,
                                                          LocalDateTime from, LocalDateTime to, int limit);
    
    /**
     * 실패한 인증 시도 총 개수 조회
     * 
     * @param sessionId 세션 ID
     * @param from 조회 시작 시간
     * @param to 조회 종료 시간
     * @return 실패한 인증 시도 총 개수
     */
    int countFailedAttempts(String sessionId, LocalDateTime from, LocalDateTime to);
    
    /**
     * 사용자별 실패한 인증 시도 총 개수 조회
     * 
     * @param sessionId 세션 ID
     * @param userId 사용자 ID
     * @param from 조회 시작 시간
     * @param to 조회 종료 시간
     * @return 해당 사용자의 실패한 인증 시도 총 개수
     */
    int countFailedAttemptsByUser(String sessionId, String userId, LocalDateTime from, LocalDateTime to);
}