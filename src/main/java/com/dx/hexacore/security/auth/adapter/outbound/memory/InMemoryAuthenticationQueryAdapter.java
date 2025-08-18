package com.dx.hexacore.security.auth.adapter.outbound.memory;

import com.dx.hexacore.security.auth.application.query.port.out.LoadAuthenticationQueryPort;
import com.dx.hexacore.security.auth.application.query.port.out.LoadTokenInfoQueryPort;
import com.dx.hexacore.security.auth.application.projection.AuthenticationProjection;
import com.dx.hexacore.security.auth.application.projection.TokenInfoProjection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 메모리 기반 인증 조회 어댑터.
 * JPA가 없는 환경에서 기본 구현체로 사용됩니다.
 * 
 * @since 1.0.0
 */
class InMemoryAuthenticationQueryAdapter implements LoadAuthenticationQueryPort, LoadTokenInfoQueryPort {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryAuthenticationQueryAdapter.class);
    
    private final Map<String, AuthenticationProjection> authenticationStore = new ConcurrentHashMap<>();
    private final Map<String, TokenInfoProjection> tokenStore = new ConcurrentHashMap<>();

    @Override
    public Optional<AuthenticationProjection> loadById(String authenticationId) {
        logger.debug("Loading authentication by ID: {} (using in-memory adapter)", authenticationId);
        return Optional.ofNullable(authenticationStore.get(authenticationId));
    }

    @Override
    public Optional<TokenInfoProjection> loadByToken(String token) {
        logger.debug("Loading token info by token: {} (using in-memory adapter)", token);
        return Optional.ofNullable(tokenStore.get(token));
    }
    
    /**
     * 테스트나 개발용으로 인증 정보를 추가합니다.
     */
    public void addAuthentication(String id, AuthenticationProjection projection) {
        authenticationStore.put(id, projection);
    }
    
    /**
     * 테스트나 개발용으로 토큰 정보를 추가합니다.
     */
    public void addToken(String token, TokenInfoProjection projection) {
        tokenStore.put(token, projection);
    }
    
    /**
     * 저장된 데이터를 모두 삭제합니다.
     */
    public void clear() {
        authenticationStore.clear();
        tokenStore.clear();
    }
}