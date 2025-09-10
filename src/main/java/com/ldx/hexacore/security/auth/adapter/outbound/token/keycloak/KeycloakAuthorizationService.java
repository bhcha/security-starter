package com.ldx.hexacore.security.auth.adapter.outbound.token.keycloak;

import com.sd.KeycloakClient.client.auth.sync.KeycloakAuthClient;
import com.sd.KeycloakClient.config.ClientConfiguration;
import com.sd.KeycloakClient.dto.KeycloakResponse;
import com.sd.KeycloakClient.dto.auth.KeycloakAuthorizationResult;
import com.sd.KeycloakClient.factory.KeycloakClient;
import lombok.extern.slf4j.Slf4j;

/**
 * 팀 Keycloak Client 기반 Authorization Service
 * 
 * <p>팀에서 개발한 keycloak-client 라이브러리를 사용하여 리소스 권한 검증을 수행합니다.</p>
 * <p>하드코딩 없이 Keycloak의 authorization 엔드포인트를 직접 호출하여 권한을 검증합니다.</p>
 */
@Slf4j
public class KeycloakAuthorizationService {
    
    private final KeycloakAuthClient keycloakAuthClient;
    private final String realm;
    private final String clientId;
    
    public KeycloakAuthorizationService(KeycloakProperties properties) {
        this.realm = properties.getRealm();
        this.clientId = properties.getClientId();
        
        try {
            // 팀 Keycloak Client 라이브러리 초기화
            ClientConfiguration config = ClientConfiguration.builder()
                    .baseUrl(properties.getServerUrl())
                    .realmName(properties.getRealm())
                    .clientId(properties.getClientId())
                    .clientSecret(properties.getClientSecret())
                    .build();
                    
            KeycloakClient keycloakClient = new KeycloakClient(config);
            this.keycloakAuthClient = keycloakClient.auth();
                    
            log.info("🔧 팀 Keycloak Client Authorization Service 초기화됨");
            log.info("Server URL: {}", properties.getServerUrl());
            log.info("Realm: {}", realm);
            log.info("Client ID: {}", clientId);
        } catch (Exception e) {
            log.error("❌ 팀 Keycloak Client 초기화 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Keycloak Authorization Service 초기화 실패", e);
        }
    }
    
    /**
     * 팀 Keycloak Client를 사용하여 authorization을 수행합니다.
     * 
     * <p>하드코딩 없이 Keycloak authorization 엔드포인트에 직접 요청하여 권한을 검증합니다.</p>
     * 
     * @param accessToken 사용자 액세스 토큰
     * @param requestUri 요청 URI
     * @param httpMethod HTTP 메서드
     * @return 권한이 허용되면 true, 거부되면 false
     */
    public boolean checkAuthorization(String accessToken, String requestUri, String httpMethod) {
        try {
            log.info("🔍 팀 Keycloak Client로 authorization 체크: {} {}", httpMethod, requestUri);
            
            // 팀 라이브러리의 authorization 함수 호출
            KeycloakResponse<KeycloakAuthorizationResult> response = 
                keycloakAuthClient.authorization(accessToken, requestUri, httpMethod);
            
            if (response == null || response.getBody().isEmpty()) {
                log.warn("❌ Authorization 응답이 비어있음: {} {}", httpMethod, requestUri);
                return false;
            }
            
            KeycloakAuthorizationResult result = response.getBody().get();
            boolean granted = result.isGranted();
            
            log.info("🎯 Authorization 결과: {} {} -> {}", 
                httpMethod, requestUri, granted ? "✅ 허용" : "❌ 거부");
            
            return granted;
            
        } catch (Exception e) {
            log.error("❌ 팀 Keycloak Client authorization 체크 중 오류: {} {}", 
                requestUri, e.getMessage(), e);
            return false; // 오류 시 기본적으로 거부
        }
    }
    
    /**
     * 디버깅을 위한 서비스 정보 출력
     */
    public void logServiceInfo() {
        log.info("📋 팀 Keycloak Client Authorization Service 정보:");
        log.info("  - Realm: {}", realm);
        log.info("  - Client ID: {}", clientId);
        log.info("  - 사용 라이브러리: io.github.l-dxd:keycloak-client:0.0.17");
        log.info("  - 완전 하드코딩 제거 - Keycloak이 직접 엔드포인트 권한 검증");
    }
}