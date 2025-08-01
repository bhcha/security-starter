package com.dx.hexacore.security.auth.adapter.inbound.filter;

import com.dx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.dx.hexacore.security.auth.application.command.port.out.TokenValidationResult;
import com.dx.hexacore.security.auth.application.exception.ValidationException;
import com.dx.hexacore.security.auth.adapter.inbound.config.SecurityProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    
    private final TokenProvider tokenProvider;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher;
    private final List<String> excludeUrlPatterns;
    private final SecurityProperties securityProperties;

    public JwtAuthenticationFilter(
            TokenProvider tokenProvider,
            ObjectMapper objectMapper,
            List<String> excludeUrlPatterns,
            SecurityProperties securityProperties) {
        this.tokenProvider = tokenProvider;
        this.objectMapper = objectMapper;
        this.pathMatcher = new AntPathMatcher();
        this.excludeUrlPatterns = excludeUrlPatterns != null ? excludeUrlPatterns : 
            List.of("/actuator/health", "/error");
        this.securityProperties = securityProperties;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        logger.debug("Processing JWT authentication for path: {}", path);

        try {
            String token = extractToken(request);
            
            if (StringUtils.hasText(token)) {
                processToken(token);
            }
            
            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            logger.error("JWT authentication failed: {}", e.getMessage());
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
    private void processToken(String token) {
        try {
            TokenValidationResult result = tokenProvider.validateToken(token);
            
            if (result.valid()) {
                // 인증 성공 - SecurityContext에 인증 정보 설정
                String defaultRole = securityProperties.getAuthentication().getDefaultRole();
                JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                    token,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority(defaultRole))
                );
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("JWT authentication successful for user: {}", result.username());
            } else {
                String reason = result.claims() != null ? 
                    (String) result.claims().get("error") : "Unknown reason";
                logger.warn("JWT token validation failed: {}", reason);
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