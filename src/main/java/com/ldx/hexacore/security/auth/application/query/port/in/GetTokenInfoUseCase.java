package com.ldx.hexacore.security.auth.application.query.port.in;

import com.ldx.hexacore.security.auth.application.exception.TokenNotFoundException;
import com.ldx.hexacore.security.auth.application.exception.ValidationException;

/**
 * 토큰 정보 조회 Use Case.
 * 
 * @since 1.0.0
 */
public interface GetTokenInfoUseCase {

    /**
     * 토큰으로 토큰 정보를 조회합니다.
     * 
     * @param query 토큰 정보 조회 Query
     * @return 조회된 토큰 정보
     * @throws TokenNotFoundException 토큰을 찾을 수 없는 경우
     * @throws ValidationException 입력값이 유효하지 않은 경우
     */
    TokenInfoResponse getTokenInfo(GetTokenInfoQuery query);
}