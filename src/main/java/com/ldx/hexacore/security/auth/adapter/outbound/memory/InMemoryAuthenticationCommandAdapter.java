package com.ldx.hexacore.security.auth.adapter.outbound.memory;

import com.ldx.hexacore.security.auth.application.command.port.out.AuthenticationRepository;
import com.ldx.hexacore.security.auth.domain.Authentication;
import com.ldx.hexacore.security.auth.domain.vo.AuthenticationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 메모리 기반 인증 명령 어댑터.
 * JPA가 없는 환경에서 기본 구현체로 사용됩니다.
 * 
 * @since 1.0.0
 */
class InMemoryAuthenticationCommandAdapter implements AuthenticationRepository {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryAuthenticationCommandAdapter.class);
    
    private final Map<UUID, Authentication> authenticationStore = new ConcurrentHashMap<>();

    @Override
    public Authentication save(Authentication authentication) {
        logger.debug("Saving authentication with ID: {} (using in-memory adapter)", authentication.getId());
        authenticationStore.put(authentication.getId(), authentication);
        return authentication;
    }

    @Override
    public Optional<Authentication> findById(UUID id) {
        logger.debug("Finding authentication by ID: {} (using in-memory adapter)", id);
        return Optional.ofNullable(authenticationStore.get(id));
    }

    @Override
    public Optional<Authentication> findLatestSuccessfulByUsername(String username) {
        logger.debug("Finding latest successful authentication by username: {} (using in-memory adapter)", username);
        return authenticationStore.values().stream()
                .filter(auth -> auth.getCredentials().getUsername().equals(username))
                .filter(auth -> auth.getStatus().equals(AuthenticationStatus.success()))
                .max((a1, a2) -> a1.getSuccessTime().compareTo(a2.getSuccessTime()));
    }

    @Override
    public Optional<Authentication> findByAccessToken(String accessToken) {
        logger.debug("Finding authentication by access token (using in-memory adapter)");
        return authenticationStore.values().stream()
                .filter(auth -> auth.getToken() != null)
                .filter(auth -> auth.getToken().getAccessToken().equals(accessToken))
                .findFirst();
    }

    @Override
    public Optional<Authentication> findByRefreshToken(String refreshToken) {
        logger.debug("Finding authentication by refresh token (using in-memory adapter)");
        return authenticationStore.values().stream()
                .filter(auth -> auth.getToken() != null)
                .filter(auth -> auth.getToken().getRefreshToken().equals(refreshToken))
                .findFirst();
    }
    
    /**
     * 저장된 데이터를 모두 삭제합니다.
     */
    public void clear() {
        authenticationStore.clear();
    }
    
    /**
     * 저장된 데이터 개수를 반환합니다.
     */
    public int size() {
        return authenticationStore.size();
    }
}