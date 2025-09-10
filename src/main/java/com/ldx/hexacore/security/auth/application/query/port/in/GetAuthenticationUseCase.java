package com.ldx.hexacore.security.auth.application.query.port.in;

import com.ldx.hexacore.security.auth.application.exception.AuthenticationNotFoundException;
import com.ldx.hexacore.security.auth.application.exception.ValidationException;

/**
 * 인증 정보 조회 Use Case.
 * 
 * @since 1.0.0
 */
public interface GetAuthenticationUseCase {

    /**
     * 인증 ID로 인증 정보를 조회합니다.
     * 
     * @param query 인증 조회 Query
     * @return 조회된 인증 정보
     * @throws AuthenticationNotFoundException 인증 정보를 찾을 수 없는 경우
     * @throws ValidationException 입력값이 유효하지 않은 경우
     */
    AuthenticationResponse getAuthentication(GetAuthenticationQuery query);
}