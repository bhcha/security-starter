package com.dx.hexacore.security.auth.application.query.handler;

import com.dx.hexacore.security.auth.application.exception.ValidationException;
import com.dx.hexacore.security.auth.application.exception.AuthenticationNotFoundException;
import com.dx.hexacore.security.auth.application.exception.TokenNotFoundException;
import com.dx.hexacore.security.auth.application.query.port.in.*;
import com.dx.hexacore.security.auth.application.query.port.out.LoadAuthenticationQueryPort;
import com.dx.hexacore.security.auth.application.query.port.out.LoadTokenInfoQueryPort;
import com.dx.hexacore.security.auth.application.projection.AuthenticationProjection;
import com.dx.hexacore.security.auth.application.projection.TokenInfoProjection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import java.util.Objects;

/**
 * 인증 조회 처리기.
 * 
 * @since 1.0.0
 */
@Service
@Transactional(readOnly = true)
class AuthenticationQueryHandler implements GetAuthenticationUseCase, GetTokenInfoUseCase {

    private final LoadAuthenticationQueryPort loadAuthenticationQueryPort;
    private final LoadTokenInfoQueryPort loadTokenInfoQueryPort;

    public AuthenticationQueryHandler(
        LoadAuthenticationQueryPort loadAuthenticationQueryPort,
        LoadTokenInfoQueryPort loadTokenInfoQueryPort
    ) {
        this.loadAuthenticationQueryPort = Objects.requireNonNull(loadAuthenticationQueryPort);
        this.loadTokenInfoQueryPort = Objects.requireNonNull(loadTokenInfoQueryPort);
    }

    @Override
    public AuthenticationResponse getAuthentication(@Valid GetAuthenticationQuery query) {
        // Null 체크
        if (query == null) {
            throw new ValidationException("Query cannot be null");
        }
        
        // 인증 정보 조회
        AuthenticationProjection projection = loadAuthenticationQueryPort
            .loadById(query.getAuthenticationId())
            .orElseThrow(() -> new AuthenticationNotFoundException(query.getAuthenticationId()));

        // Projection을 Response로 변환
        return mapToAuthenticationResponse(projection);
    }

    @Override
    public TokenInfoResponse getTokenInfo(@Valid GetTokenInfoQuery query) {
        // Null 체크
        if (query == null) {
            throw new ValidationException("Query cannot be null");
        }
        
        // 토큰 정보 조회
        TokenInfoProjection projection = loadTokenInfoQueryPort
            .loadByToken(query.getToken())
            .orElseThrow(() -> new TokenNotFoundException(query.getToken()));

        // Projection을 Response로 변환
        return mapToTokenInfoResponse(projection);
    }

    /**
     * AuthenticationProjection을 AuthenticationResponse로 변환합니다.
     * 
     * @param projection 변환할 Projection
     * @return 변환된 Response
     */
    private AuthenticationResponse mapToAuthenticationResponse(AuthenticationProjection projection) {
        return AuthenticationResponse.builder()
            .id(projection.getId())
            .username(projection.getUsername())
            .status(projection.getStatus())
            .attemptTime(projection.getAttemptTime())
            .successTime(projection.getSuccessTime())
            .failureTime(projection.getFailureTime())
            .failureReason(projection.getFailureReason())
            .accessToken(projection.getAccessToken())
            .refreshToken(projection.getRefreshToken())
            .tokenExpiresIn(projection.getTokenExpiresIn())
            .build();
    }

    /**
     * TokenInfoProjection을 TokenInfoResponse로 변환합니다.
     * 
     * @param projection 변환할 Projection
     * @return 변환된 Response
     */
    private TokenInfoResponse mapToTokenInfoResponse(TokenInfoProjection projection) {
        return TokenInfoResponse.builder()
            .token(projection.getToken())
            .isValid(projection.isValid())
            .issuedAt(projection.getIssuedAt())
            .expiresAt(projection.getExpiresAt())
            .canRefresh(projection.canRefresh())
            .tokenType(projection.getTokenType())
            .scope(projection.getScope())
            .authenticationId(projection.getAuthenticationId())
            .build();
    }
}