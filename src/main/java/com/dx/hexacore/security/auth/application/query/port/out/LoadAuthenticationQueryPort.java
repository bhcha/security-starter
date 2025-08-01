package com.dx.hexacore.security.auth.application.query.port.out;

import com.dx.hexacore.security.auth.application.projection.AuthenticationProjection;
import com.dx.hexacore.security.auth.application.exception.AuthenticationNotFoundException;

import java.util.Optional;

/**
 * 인증 정보 조회 포트.
 * 
 * @since 1.0.0
 */
public interface LoadAuthenticationQueryPort {

    /**
     * ID로 인증 정보를 조회합니다.
     * 
     * @param authenticationId 조회할 인증 ID
     * @return 조회된 인증 정보 Projection
     */
    Optional<AuthenticationProjection> loadById(String authenticationId);

    /**
     * ID로 인증 정보를 조회합니다. 없으면 예외 발생.
     * 
     * @param authenticationId 조회할 인증 ID
     * @return 조회된 인증 정보 Projection
     * @throws AuthenticationNotFoundException 인증 정보를 찾을 수 없는 경우
     */
    default AuthenticationProjection loadByIdOrThrow(String authenticationId) {
        return loadById(authenticationId)
            .orElseThrow(() -> new AuthenticationNotFoundException(authenticationId));
    }
}