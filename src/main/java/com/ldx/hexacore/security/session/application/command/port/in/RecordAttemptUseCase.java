package com.ldx.hexacore.security.session.application.command.port.in;

import com.ldx.hexacore.security.session.application.exception.SessionNotFoundException;

/**
 * 인증 시도 기록 유스케이스
 * 
 * 사용자의 인증 시도를 세션에 기록하고, 계정 잠금 정책을 적용합니다.
 */
public interface RecordAttemptUseCase {
    
    /**
     * 인증 시도 기록 실행
     * 
     * @param command 인증 시도 기록 명령
     * @return 기록 결과 (계정 잠금 상태 포함)
     * @throws SessionNotFoundException 세션을 찾을 수 없는 경우
     */
    RecordAttemptResult execute(RecordAuthenticationAttemptCommand command);
}