package com.dx.hexacore.security.logging;

import com.dx.hexacore.security.auth.application.command.port.out.TokenValidationContext;
import com.dx.hexacore.security.auth.application.command.port.out.TokenValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Security 요청 처리 과정을 상세히 로깅하는 컴포넌트
 * 
 * 각 요청의 전체 흐름을 추적하고 성능, 보안 이벤트를 기록합니다.
 */
@Component
public class SecurityRequestLogger {
    
    private static final Logger logger = LoggerFactory.getLogger("SECURITY.REQUEST");
    private static final Logger performanceLogger = LoggerFactory.getLogger("SECURITY.PERFORMANCE");
    private static final Logger authLogger = LoggerFactory.getLogger("SECURITY.AUTH");
    
    /**
     * 요청 시작 로깅
     */
    public String logRequestStart(HttpServletRequest request) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("requestId", requestId);
        
        if (logger.isDebugEnabled()) {
            logger.debug("╔════════════════════════════════════════════════════════════════");
            logger.debug("║ 🔍 REQUEST START [{}]", requestId);
            logger.debug("╟────────────────────────────────────────────────────────────────");
            logger.debug("║ Method: {} {}", request.getMethod(), request.getRequestURI());
            logger.debug("║ Remote IP: {}", getClientIp(request));
            logger.debug("║ User-Agent: {}", request.getHeader("User-Agent"));
            logger.debug("║ Auth Header: {}", 
                request.getHeader("Authorization") != null ? "Present" : "Missing");
            logger.debug("╚════════════════════════════════════════════════════════════════");
        }
        
        return requestId;
    }
    
    /**
     * 토큰 추출 로깅
     */
    public void logTokenExtraction(String token, boolean found) {
        if (logger.isDebugEnabled()) {
            if (found) {
                logger.debug("🎫 Token extracted: {}...{}", 
                    token.substring(0, Math.min(20, token.length())),
                    token.length() > 20 ? " (" + token.length() + " chars)" : "");
            } else {
                logger.debug("⚠️  No Bearer token found in request");
            }
        }
    }
    
    /**
     * 토큰 검증 컨텍스트 로깅
     */
    public void logValidationContext(TokenValidationContext context) {
        if (!logger.isInfoEnabled()) return;
        
        logger.info("┌─── 🔐 Token Validation Context ───────────────────────────────");
        logger.info("│ Request URI: {}", context.getRequestUri());
        logger.info("│ HTTP Method: {}", context.getHttpMethod());
        logger.info("│ Resource Check: {}", 
            context.isCheckResourcePermission() ? "✅ ENABLED" : "❌ DISABLED");
        
        if (context.isCheckResourcePermission()) {
            logger.info("│ ⚡ Keycloak UMA authorization will be performed");
        }
        
        logger.info("└────────────────────────────────────────────────────────────────");
    }
    
    /**
     * 토큰 검증 결과 로깅
     */
    public void logValidationResult(TokenValidationResult result, long durationMs) {
        if (result.valid()) {
            authLogger.info("✅ AUTH SUCCESS | User: {} | Duration: {}ms", 
                result.username(), durationMs);
            
            if (logger.isDebugEnabled()) {
                logger.debug("┌─── ✅ Token Validation SUCCESS ────────────────────────────");
                logger.debug("│ Username: {}", result.username());
                logger.debug("│ User ID: {}", result.userId());
                logger.debug("│ Duration: {}ms", durationMs);
                
                if (result.claims() != null && !result.claims().isEmpty()) {
                    logger.debug("│ Claims:");
                    result.claims().forEach((key, value) -> 
                        logger.debug("│   ├─ {}: {}", key, value));
                }
                
                logger.debug("└────────────────────────────────────────────────────────────────");
            }
        } else {
            authLogger.warn("❌ AUTH FAILED | Duration: {}ms | Reason: {}", 
                durationMs, 
                result.claims() != null ? result.claims().get("error") : "Unknown");
            
            logger.info("┌─── ❌ Token Validation FAILED ─────────────────────────────");
            logger.info("│ Duration: {}ms", durationMs);
            
            if (result.claims() != null) {
                Boolean resourceDenied = (Boolean) result.claims().get("resource_permission_denied");
                if (Boolean.TRUE.equals(resourceDenied)) {
                    logger.info("│ Reason: Resource Permission Denied");
                    logger.info("│ Requested URI: {}", result.claims().get("requested_uri"));
                    logger.info("│ Requested Method: {}", result.claims().get("requested_method"));
                } else {
                    logger.info("│ Reason: {}", result.claims().get("error"));
                }
            }
            
            logger.info("└────────────────────────────────────────────────────────────────");
        }
    }
    
    /**
     * UMA 권한 체크 로깅
     */
    public void logUMACheck(String uri, String method, boolean granted, long durationMs) {
        if (granted) {
            logger.info("🔓 UMA GRANTED | {} {} | {}ms", method, uri, durationMs);
        } else {
            logger.warn("🔒 UMA DENIED | {} {} | {}ms", method, uri, durationMs);
        }
    }
    
    /**
     * 요청 완료 로깅
     */
    public void logRequestComplete(String requestId, boolean success, long totalDurationMs) {
        if (performanceLogger.isInfoEnabled()) {
            performanceLogger.info("REQUEST_COMPLETE | ID: {} | Success: {} | Duration: {}ms", 
                requestId, success, totalDurationMs);
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("╔════════════════════════════════════════════════════════════════");
            logger.debug("║ {} REQUEST COMPLETE [{}]", 
                success ? "✅" : "❌", requestId);
            logger.debug("║ Total Duration: {}ms", totalDurationMs);
            logger.debug("╚════════════════════════════════════════════════════════════════");
        }
        
        MDC.remove("requestId");
    }
    
    /**
     * 에러 로깅
     */
    public void logError(String message, Exception e) {
        logger.error("❌ ERROR: {} - {}", message, e.getMessage());
        if (logger.isDebugEnabled()) {
            logger.debug("Stack trace:", e);
        }
    }
    
    /**
     * 보안 이벤트 로깅
     */
    public void logSecurityEvent(String eventType, String details) {
        authLogger.warn("🚨 SECURITY_EVENT | Type: {} | Details: {}", eventType, details);
    }
    
    /**
     * 성능 경고 로깅
     */
    public void logPerformanceWarning(String operation, long durationMs, long thresholdMs) {
        if (durationMs > thresholdMs) {
            performanceLogger.warn("⚠️  SLOW_OPERATION | {} took {}ms (threshold: {}ms)", 
                operation, durationMs, thresholdMs);
        }
    }
    
    /**
     * 통계 로깅 (주기적으로 호출)
     */
    public void logStatistics(long totalRequests, long successCount, long failureCount, 
                             double avgResponseTime) {
        logger.info("╔════════════════════════════════════════════════════════════════");
        logger.info("║ 📊 SECURITY STATISTICS");
        logger.info("╟────────────────────────────────────────────────────────────────");
        logger.info("║ Total Requests: {}", totalRequests);
        logger.info("║ Success: {} ({:.1f}%)", successCount, 
            (successCount * 100.0 / totalRequests));
        logger.info("║ Failures: {} ({:.1f}%)", failureCount, 
            (failureCount * 100.0 / totalRequests));
        logger.info("║ Avg Response Time: {:.2f}ms", avgResponseTime);
        logger.info("╚════════════════════════════════════════════════════════════════");
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}