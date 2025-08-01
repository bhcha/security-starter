package com.dx.hexacore.security.auth.application.query.port.out;

import com.dx.hexacore.security.auth.application.projection.TokenInfoProjection;
import com.dx.hexacore.security.auth.application.exception.TokenNotFoundException;

import java.util.Optional;

/**
 * 토큰 정보 조회 포트.
 * 
 * @since 1.0.0
 */
public interface LoadTokenInfoQueryPort {

    /**
     * 토큰으로 토큰 정보를 조회합니다.
     * 
     * @param token 조회할 토큰
     * @return 조회된 토큰 정보 Projection
     */
    Optional<TokenInfoProjection> loadByToken(String token);

    /**
     * 토큰으로 토큰 정보를 조회합니다. 없으면 예외 발생.
     * 
     * @param token 조회할 토큰
     * @return 조회된 토큰 정보 Projection
     * @throws TokenNotFoundException 토큰 정보를 찾을 수 없는 경우
     */
    default TokenInfoProjection loadByTokenOrThrow(String token) {
        return loadByToken(token)
            .orElseThrow(() -> new TokenNotFoundException(token));
    }
}