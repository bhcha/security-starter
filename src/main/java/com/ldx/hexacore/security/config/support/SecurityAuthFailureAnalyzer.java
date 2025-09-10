package com.ldx.hexacore.security.config.support;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * Hexacore Security 관련 설정 오류를 분석하는 Failure Analyzer입니다.
 * 
 * 애플리케이션 시작 실패 시 사용자에게 친화적인 오류 메시지를 제공합니다.
 */
public class SecurityAuthFailureAnalyzer extends AbstractFailureAnalyzer<Exception> {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, Exception cause) {
        String causeMessage = cause.getMessage();
        
        if (causeMessage == null) {
            return null;
        }

        // Keycloak 관련 오류 분석
        if (isKeycloakConfigurationError(causeMessage)) {
            return analyzeKeycloakError(causeMessage);
        }

        // JWT 관련 오류 분석
        if (isJwtConfigurationError(causeMessage)) {
            return analyzeJwtError(causeMessage);
        }

        // 캐시 관련 오류 분석
        if (isCacheConfigurationError(causeMessage)) {
            return analyzeCacheError(causeMessage);
        }

        // TokenProvider 관련 오류 분석
        if (isTokenProviderError(causeMessage)) {
            return analyzeTokenProviderError(causeMessage);
        }

        return null;
    }

    private boolean isKeycloakConfigurationError(String message) {
        return message.contains("keycloak") || 
               message.contains("KeycloakAuthenticationAdapter") ||
               message.contains("KeycloakTokenProvider");
    }

    private FailureAnalysis analyzeKeycloakError(String message) {
        return new FailureAnalysis(
            "Keycloak 설정에 문제가 있습니다: " + message,
            "다음 설정을 확인하세요:\n" +
            "- hexacore.security.token-provider.keycloak.server-url\n" +
            "- hexacore.security.token-provider.keycloak.realm\n" +
            "- hexacore.security.token-provider.keycloak.client-id\n" +
            "- hexacore.security.token-provider.keycloak.client-secret\n" +
            "또는 hexacore.security.token-provider.provider=jwt로 변경하세요.",
            null
        );
    }

    private boolean isJwtConfigurationError(String message) {
        return message.contains("jwt") || 
               message.contains("SpringJwtTokenProvider") ||
               message.contains("JwtProperties");
    }

    private FailureAnalysis analyzeJwtError(String message) {
        return new FailureAnalysis(
            "JWT 설정에 문제가 있습니다: " + message,
            "다음 설정을 확인하세요:\n" +
            "- hexacore.security.token-provider.jwt.secret (256bit 이상 필요)\n" +
            "- hexacore.security.token-provider.jwt.issuer\n" +
            "- hexacore.security.token-provider.jwt.access-token-expiration\n" +
            "- hexacore.security.token-provider.jwt.refresh-token-expiration",
            null
        );
    }

    private boolean isCacheConfigurationError(String message) {
        return message.contains("cache") || 
               message.contains("caffeine") ||
               message.contains("SessionCacheAdapter");
    }

    private FailureAnalysis analyzeCacheError(String message) {
        return new FailureAnalysis(
            "캐시 설정에 문제가 있습니다: " + message,
            "다음 설정을 확인하세요:\n" +
            "- hexacore.security.cache.type (caffeine 또는 redis)\n" +
            "- hexacore.security.cache.enabled=true\n" +
            "Caffeine 의존성이 추가되었는지 확인하세요.",
            null
        );
    }

    private boolean isTokenProviderError(String message) {
        return message.contains("TokenProvider") ||
               message.contains("No qualifying bean of type") && 
               message.contains("com.dx.hexacore.security.auth.application.command.port.out.TokenProvider");
    }

    private FailureAnalysis analyzeTokenProviderError(String message) {
        return new FailureAnalysis(
            "TokenProvider Bean을 찾을 수 없습니다: " + message,
            "다음 설정을 확인하세요:\n" +
            "- hexacore.security.token-provider.provider (keycloak 또는 jwt)\n" +
            "- Keycloak 선택 시: hexacore.security.token-provider.keycloak.enabled=true\n" +
            "- JWT 선택 시: hexacore.security.token-provider.jwt.enabled=true\n" +
            "적어도 하나의 TokenProvider가 활성화되어야 합니다.",
            null
        );
    }
}