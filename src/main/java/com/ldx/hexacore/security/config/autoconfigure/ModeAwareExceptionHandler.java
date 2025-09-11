package com.ldx.hexacore.security.config.autoconfigure;

import com.ldx.hexacore.security.auth.application.exception.AuthenticationException;
import com.ldx.hexacore.security.auth.application.exception.TokenException;
import com.ldx.hexacore.security.config.properties.SecurityStarterProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Mode별 차별화된 Exception Handler
 * 
 * <p>Traditional/Hexagonal 모드에 따라 다른 응답 형식을 제공합니다.
 * 
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ModeAwareExceptionHandler {
    
    private final SecurityStarterProperties properties;
    
    /**
     * Authentication 예외 처리
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuthenticationException(
            AuthenticationException ex, 
            WebRequest request) {
        
        log.error("Authentication error: {}", ex.getMessage());
        
        switch (properties.getMode()) {
            case TRADITIONAL:
                return handleTraditionalMode(ex, request);
            case HEXAGONAL:
                return handleHexagonalMode(ex, request);
            default:
                return handleTraditionalMode(ex, request);
        }
    }
    
    /**
     * Token 예외 처리
     */
    @ExceptionHandler(TokenException.class)
    public ResponseEntity<?> handleTokenException(
            TokenException ex,
            WebRequest request) {
        
        log.error("Token error: {}", ex.getMessage());
        
        switch (properties.getMode()) {
            case TRADITIONAL:
                return handleTraditionalTokenError(ex, request);
            case HEXAGONAL:
                return handleHexagonalTokenError(ex, request);
            default:
                return handleTraditionalTokenError(ex, request);
        }
    }
    
    /**
     * Traditional 모드 - 간단한 에러 응답
     */
    private ResponseEntity<?> handleTraditionalMode(
            AuthenticationException ex,
            WebRequest request) {
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Authentication Failed");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("timestamp", Instant.now().toString());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse);
    }
    
    /**
     * Hexagonal 모드 - RFC 7807 Problem Details 형식
     */
    private ResponseEntity<?> handleHexagonalMode(
            AuthenticationException ex,
            WebRequest request) {
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage()
        );
        
        problemDetail.setType(URI.create("https://hexacore.security/problems/authentication-failed"));
        problemDetail.setTitle("Authentication Failed");
        problemDetail.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));
        
        // 추가 정보
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("mode", "HEXAGONAL");
        
        // Domain Event 정보 추가 (Hexagonal 특화)
        if (ex.getCause() != null) {
            problemDetail.setProperty("domainEvent", "AuthenticationFailed");
            problemDetail.setProperty("aggregateId", extractAggregateId(ex));
        }
        
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(problemDetail);
    }
    
    /**
     * Traditional 모드 - Token 에러 간단 응답
     */
    private ResponseEntity<?> handleTraditionalTokenError(
            TokenException ex,
            WebRequest request) {
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Token Error");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("timestamp", Instant.now().toString());
        
        HttpStatus status = determineTokenErrorStatus(ex);
        
        return ResponseEntity
                .status(status)
                .body(errorResponse);
    }
    
    /**
     * Hexagonal 모드 - Token 에러 상세 응답
     */
    private ResponseEntity<?> handleHexagonalTokenError(
            TokenException ex,
            WebRequest request) {
        
        HttpStatus status = determineTokenErrorStatus(ex);
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                status,
                ex.getMessage()
        );
        
        problemDetail.setType(URI.create("https://hexacore.security/problems/token-error"));
        problemDetail.setTitle("Token Processing Error");
        problemDetail.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));
        
        // Hexagonal 특화 정보
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("mode", "HEXAGONAL");
        problemDetail.setProperty("tokenType", extractTokenType(ex));
        
        // Port 정보 추가
        problemDetail.setProperty("port", "TokenManagementUseCase");
        problemDetail.setProperty("adapter", "JwtAdapter");
        
        return ResponseEntity
                .status(status)
                .body(problemDetail);
    }
    
    /**
     * Token 에러 상태 코드 결정
     */
    private HttpStatus determineTokenErrorStatus(TokenException ex) {
        String message = ex.getMessage().toLowerCase();
        
        if (message.contains("expired")) {
            return HttpStatus.UNAUTHORIZED;
        } else if (message.contains("invalid")) {
            return HttpStatus.BAD_REQUEST;
        } else if (message.contains("not found")) {
            return HttpStatus.NOT_FOUND;
        }
        
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
    
    /**
     * Aggregate ID 추출 (Hexagonal 모드용)
     */
    private String extractAggregateId(Exception ex) {
        // 실제 구현에서는 Exception에서 Aggregate ID를 추출
        return "auth-" + System.currentTimeMillis();
    }
    
    /**
     * Token Type 추출
     */
    private String extractTokenType(TokenException ex) {
        String message = ex.getMessage();
        
        if (message.contains("access")) {
            return "ACCESS_TOKEN";
        } else if (message.contains("refresh")) {
            return "REFRESH_TOKEN";
        }
        
        return "UNKNOWN";
    }
}