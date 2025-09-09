package com.dx.hexacore.security.auth.adapter.inbound.filter;

import com.dx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.dx.hexacore.security.auth.application.command.port.out.TokenValidationContext;
import com.dx.hexacore.security.auth.application.command.port.out.TokenValidationResult;
import com.dx.hexacore.security.auth.application.exception.ValidationException;
import com.dx.hexacore.security.auth.adapter.inbound.config.SecurityProperties;
import com.dx.hexacore.security.logging.SecurityRequestLogger;
import com.dx.hexacore.security.logging.SecurityEventLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT 인증 필터
 * HTTP 요청에서 JWT 토큰을 추출하여 검증하고 Spring Security Context에 인증 정보를 설정합니다.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    
    private final TokenProvider tokenProvider;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher;
    private final List<String> excludeUrlPatterns;
    private final SecurityProperties securityProperties;
    private final SecurityRequestLogger requestLogger;
    private final SecurityEventLogger eventLogger;

    public JwtAuthenticationFilter(
            TokenProvider tokenProvider,
            ObjectMapper objectMapper,
            List<String> excludeUrlPatterns,
            SecurityProperties securityProperties,
            SecurityRequestLogger requestLogger,
            SecurityEventLogger eventLogger) {
        this.tokenProvider = tokenProvider;
        this.objectMapper = objectMapper;
        this.pathMatcher = new AntPathMatcher();
        // 기본 제외 경로는 설정에서 관리 - 하드코딩 제거
        this.excludeUrlPatterns = excludeUrlPatterns != null ? excludeUrlPatterns : Collections.emptyList();
        this.securityProperties = securityProperties;
        this.requestLogger = requestLogger;
        this.eventLogger = eventLogger;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        String method = request.getMethod();
        long startTime = System.currentTimeMillis();
        
        // 요청 시작 로깅
        String requestId = requestLogger.logRequestStart(request);

        try {
            String token = extractToken(request);
            
            if (StringUtils.hasText(token)) {
                requestLogger.logTokenExtraction(token, true);
                processToken(token, request);
            } else {
                requestLogger.logTokenExtraction(null, false);
            }
            
            filterChain.doFilter(request, response);
            
            // 요청 완료 로깅
            long duration = System.currentTimeMillis() - startTime;
            requestLogger.logRequestComplete(requestId, true, duration);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            requestLogger.logError("JWT authentication failed", e);
            requestLogger.logRequestComplete(requestId, false, duration);
            
            // 보안 이벤트 로깅
            eventLogger.logAuthenticationFailure(
                e.getMessage(),
                getClientIpAddress(request),
                request.getHeader("User-Agent")
            );
            
            handleAuthenticationFailure(response, e);
            return; // 인증 실패 시 필터 체인을 계속하지 않음
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return excludeUrlPatterns.stream()
            .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * HTTP 요청에서 JWT 토큰을 추출합니다.
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            String token = bearerToken.substring(BEARER_PREFIX.length());
            logger.debug("JWT token extracted from Authorization header");
            return token;
        }
        
        return null;
    }

    /**
     * 토큰을 검증하고 인증 정보를 설정합니다.
     */
    private void processToken(String token, HttpServletRequest request) {
        try {
            // 요청 컨텍스트 정보 수집
            TokenValidationContext context = buildValidationContext(request);
            
            // 컨텍스트 로깅
            requestLogger.logValidationContext(context);
            
            // 컨텍스트와 함께 토큰 검증
            long validationStart = System.currentTimeMillis();
            TokenValidationResult result = tokenProvider.validateTokenWithContext(token, context);
            long validationDuration = System.currentTimeMillis() - validationStart;
            
            // 검증 결과 로깅
            requestLogger.logValidationResult(result, validationDuration);
            
            if (result.valid()) {
                // 인증 성공 - SecurityContext에 인증 정보 설정
                String defaultRole = securityProperties.getAuthentication().getDefaultRole();
                JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                    token,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority(defaultRole))
                );
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                // 인증 성공 이벤트 로깅
                eventLogger.logAuthenticationSuccess(
                    result.username(),
                    getClientIpAddress(request),
                    request.getHeader("User-Agent")
                );
                
                // 리소스 접근 로깅
                if (result.claims() != null && Boolean.TRUE.equals(result.claims().get("resource_permission_granted"))) {
                    eventLogger.logResourceAccessGranted(
                        result.username(),
                        request.getRequestURI(),
                        request.getMethod()
                    );
                }
            } else {
                // 리소스 접근 거부 로깅
                if (result.claims() != null && Boolean.TRUE.equals(result.claims().get("resource_permission_denied"))) {
                    eventLogger.logResourceAccessDenied(
                        result.username() != null ? result.username() : "unknown",
                        request.getRequestURI(),
                        request.getMethod()
                    );
                }
                
                String reason = result.claims() != null ? 
                    (String) result.claims().get("error") : "Unknown reason";
                throw new JwtAuthenticationException("Token validation failed: " + reason);
            }
            
        } catch (JwtAuthenticationException e) {
            // Re-throw JWT authentication exceptions to preserve the original error message
            throw e;
        } catch (Exception e) {
            logger.error("Token validation error: {}", e.getMessage());
            // For unexpected errors, preserve the original error message
            throw new JwtAuthenticationException(e.getMessage() != null ? e.getMessage() : "Invalid token format", e);
        }
    }

    /**
     * 요청으로부터 검증 컨텍스트를 구성합니다.
     */
    private TokenValidationContext buildValidationContext(HttpServletRequest request) {
        // 설정에서 리소스 권한 체크 활성화 여부 확인
        boolean checkResourcePermission = securityProperties.getAuthentication().isCheckResourcePermission();
        
        TokenValidationContext.TokenValidationContextBuilder builder = TokenValidationContext.builder()
                .requestUri(request.getRequestURI())
                .httpMethod(request.getMethod())
                .checkResourcePermission(checkResourcePermission);
        
        // 클라이언트 IP 추가
        String clientIp = getClientIpAddress(request);
        if (clientIp != null) {
            builder.clientIp(clientIp);
        }
        
        // User-Agent 추가
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null) {
            builder.userAgent(userAgent);
        }
        
        // 로깅
        if (checkResourcePermission) {
            logger.debug("Building validation context with resource permission check for: {} {}", 
                request.getMethod(), request.getRequestURI());
        }
        
        return builder.build();
    }
    
    /**
     * 클라이언트 IP 주소를 추출합니다.
     */
    private String getClientIpAddress(HttpServletRequest request) {
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
    
    /**
     * 인증 실패 시 에러 응답을 생성합니다.
     */
    private void handleAuthenticationFailure(HttpServletResponse response, Exception e) 
            throws IOException {
        
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        SecurityProperties.Authentication.ErrorResponse errorConfig = 
            securityProperties.getAuthentication().getErrorResponse();
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", e.getMessage() != null ? e.getMessage() : errorConfig.getDefaultMessage());
        
        if (errorConfig.isIncludeTimestamp()) {
            errorResponse.put("timestamp", LocalDateTime.now().toString());
        }
        
        if (errorConfig.isIncludeStatus()) {
            errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
        }
        
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }

    /**
     * JWT 인증 예외
     */
    public static class JwtAuthenticationException extends RuntimeException {
        public JwtAuthenticationException(String message) {
            super(message);
        }
        
        public JwtAuthenticationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

/**
 * JWT 기반 인증 토큰
 */
class JwtAuthenticationToken extends UsernamePasswordAuthenticationToken {
    
    private final String token;
    
    public JwtAuthenticationToken(String token, Object credentials, 
            List<SimpleGrantedAuthority> authorities) {
        super(token, credentials, authorities);
        this.token = token;
    }
    
    public String getToken() {
        return token;
    }
    
    @Override
    public Object getPrincipal() {
        return token;
    }
}