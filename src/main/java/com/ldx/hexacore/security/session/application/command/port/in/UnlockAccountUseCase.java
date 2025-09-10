package com.ldx.hexacore.security.session.application.command.port.in;

import com.ldx.hexacore.security.session.application.exception.SessionNotFoundException;
import com.ldx.hexacore.security.session.application.exception.UnlockNotAllowedException;

/**
 * 계정 잠금 해제 유스케이스
 * 
 * 특정 사용자의 계정 잠금을 명시적으로 해제합니다.
 */
public interface UnlockAccountUseCase {
    
    /**
     * 계정 잠금 해제 실행
     * 
     * @param command 계정 잠금 해제 명령
     * @return 해제 결과
     * @throws SessionNotFoundException 세션을 찾을 수 없는 경우
     * @throws UnlockNotAllowedException 해제가 허용되지 않는 경우
     */
    UnlockAccountResult execute(UnlockAccountCommand command);
}