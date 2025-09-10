package com.ldx.hexacore.security.auth.adapter.inbound.filter;

import com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenValidationResult;
import com.ldx.hexacore.security.auth.adapter.inbound.config.SecurityProperties;
import com.ldx.hexacore.security.logging.SecurityRequestLogger;
import com.ldx.hexacore.security.logging.SecurityEventLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter 테스트")
class JwtAuthenticationFilterTest {

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityProperties securityProperties;
    
    @Mock
    private SecurityRequestLogger securityRequestLogger;
    
    @Mock
    private SecurityEventLogger securityEventLogger;

    private JwtAuthenticationFilter filter;
    private ObjectMapper objectMapper;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        
        // SecurityProperties Mock 설정 (lenient 모드)
        SecurityProperties.Authentication auth = mock(SecurityProperties.Authentication.class);
        SecurityProperties.Authentication.ErrorResponse errorResponse = 
            mock(SecurityProperties.Authentication.ErrorResponse.class);
        
        lenient().when(securityProperties.getAuthentication()).thenReturn(auth);
        lenient().when(auth.getDefaultRole()).thenReturn("ROLE_USER");
        lenient().when(auth.getErrorResponse()).thenReturn(errorResponse);
        lenient().when(errorResponse.getDefaultMessage()).thenReturn("Authentication failed");
        lenient().when(errorResponse.isIncludeStatus()).thenReturn(true);
        lenient().when(errorResponse.isIncludeTimestamp()).thenReturn(true);
        
        filter = new JwtAuthenticationFilter(
            tokenProvider,
            objectMapper,
            List.of("/actuator/health", "/error", "/public/**"),
            securityProperties,
            securityRequestLogger,
            securityEventLogger
        );
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("토큰 추출 테스트")
    class TokenExtractionTest {

        @Test
        @DisplayName("Authorization 헤더에서 Bearer 토큰 추출 성공")
        void extractToken_WithValidBearerToken() throws Exception {
            // Given
            String token = "valid-jwt-token";
            request.addHeader("Authorization", "Bearer " + token);
            request.setRequestURI("/api/protected");

            TokenValidationResult validResult = TokenValidationResult.valid("user-123", "testuser", Set.of("ROLE_USER"), null);
            given(tokenProvider.validateTokenWithContext(eq(token), any()))
                .willReturn(validResult);

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            verify(tokenProvider).validateTokenWithContext(eq(token), any());
            verify(filterChain).doFilter(request, response);
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        }

        @Test
        @DisplayName("Authorization 헤더 없음")
        void extractToken_NoAuthorizationHeader() throws Exception {
            // Given
            request.setRequestURI("/api/protected");

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            verify(tokenProvider, never()).validateTokenWithContext(anyString(), any());
            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("Bearer 접두사 없는 토큰")
        void extractToken_NoBearerPrefix() throws Exception {
            // Given
            request.addHeader("Authorization", "invalid-token-format");
            request.setRequestURI("/api/protected");

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            verify(tokenProvider, never()).validateTokenWithContext(anyString(), any());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("빈 Bearer 토큰")
        void extractToken_EmptyBearerToken() throws Exception {
            // Given
            request.addHeader("Authorization", "Bearer ");
            request.setRequestURI("/api/protected");

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            verify(tokenProvider, never()).validateTokenWithContext(anyString(), any());
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("토큰 검증 테스트")
    class TokenValidationTest {

        @Test
        @DisplayName("유효한 토큰 검증 성공")
        void validateToken_Success() throws Exception {
            // Given
            String token = "valid-jwt-token";
            request.addHeader("Authorization", "Bearer " + token);
            request.setRequestURI("/api/protected");

            TokenValidationResult validResult = TokenValidationResult.valid("user-123", "testuser", Set.of("ROLE_USER"), null);
            given(tokenProvider.validateTokenWithContext(eq(token), any()))
                .willReturn(validResult);

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain).doFilter(request, response);
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
            
            // SecurityContext 확인은 실제 구현에서 테스트
            // (SecurityContextHolder는 static이라 mock 테스트가 어려움)
        }

        @Test
        @DisplayName("만료된 토큰 검증")
        void validateToken_ExpiredToken() throws Exception {
            // Given
            String token = "expired-jwt-token";
            request.addHeader("Authorization", "Bearer " + token);
            request.setRequestURI("/api/protected");

            TokenValidationResult invalidResult = TokenValidationResult.invalid("Token expired");
            given(tokenProvider.validateTokenWithContext(eq(token), any()))
                .willReturn(invalidResult);

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain, never()).doFilter(request, response);
            assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
            assertThat(response.getContentAsString()).contains("Token validation failed");
        }

        @Test
        @DisplayName("서명이 잘못된 토큰")
        void validateToken_InvalidSignature() throws Exception {
            // Given
            String token = "invalid-signature-token";
            request.addHeader("Authorization", "Bearer " + token);
            request.setRequestURI("/api/protected");

            TokenValidationResult invalidResult = TokenValidationResult.invalid("Invalid signature");
            given(tokenProvider.validateTokenWithContext(eq(token), any()))
                .willReturn(invalidResult);

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain, never()).doFilter(request, response);
            assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        }

        @Test
        @DisplayName("형식이 잘못된 토큰")
        void validateToken_MalformedToken() throws Exception {
            // Given
            String token = "malformed-token";
            request.addHeader("Authorization", "Bearer " + token);
            request.setRequestURI("/api/protected");

            given(tokenProvider.validateTokenWithContext(eq(token), any()))
                .willReturn(TokenValidationResult.invalid("Invalid token format"));

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain, never()).doFilter(request, response);
            assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
            assertThat(response.getContentAsString()).contains("Invalid token format");
        }
    }

    @Nested
    @DisplayName("제외 경로 테스트")
    class ExcludePathTest {

        @Test
        @DisplayName("제외 경로 요청 - /actuator/health")
        void excludePath_ActuatorHealth() throws Exception {
            // Given
            request.setRequestURI("/actuator/health");

            // When
            boolean shouldNotFilter = filter.shouldNotFilter(request);

            // Then
            assertThat(shouldNotFilter).isTrue();
        }

        @Test
        @DisplayName("제외 경로 요청 - /error")
        void excludePath_Error() throws Exception {
            // Given
            request.setRequestURI("/error");

            // When
            boolean shouldNotFilter = filter.shouldNotFilter(request);

            // Then
            assertThat(shouldNotFilter).isTrue();
        }

        @Test
        @DisplayName("커스텀 제외 경로 - /public/**")
        void excludePath_CustomPublic() throws Exception {
            // Given
            request.setRequestURI("/public/login");

            // When
            boolean shouldNotFilter = filter.shouldNotFilter(request);

            // Then
            assertThat(shouldNotFilter).isTrue();
        }

        @Test
        @DisplayName("보호된 경로는 필터 적용")
        void protectedPath_ShouldFilter() throws Exception {
            // Given
            request.setRequestURI("/api/protected");

            // When
            boolean shouldNotFilter = filter.shouldNotFilter(request);

            // Then
            assertThat(shouldNotFilter).isFalse();
        }
    }

    @Nested
    @DisplayName("에러 응답 테스트")
    class ErrorResponseTest {

        @Test
        @DisplayName("인증 실패 시 JSON 에러 응답")
        void authenticationFailure_JsonResponse() throws Exception {
            // Given
            String token = "invalid-token";
            request.addHeader("Authorization", "Bearer " + token);
            request.setRequestURI("/api/protected");

            TokenValidationResult invalidResult = TokenValidationResult.invalid("Invalid token");
            given(tokenProvider.validateTokenWithContext(eq(token), any()))
                .willReturn(invalidResult);

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
            assertThat(response.getContentType()).isEqualTo("application/json;charset=UTF-8");
            assertThat(response.getCharacterEncoding()).isEqualTo("UTF-8");
            
            String jsonResponse = response.getContentAsString();
            assertThat(jsonResponse).contains("\"success\":false");
            assertThat(jsonResponse).contains("\"status\":401");
            assertThat(jsonResponse).contains("\"message\"");
            assertThat(jsonResponse).contains("\"timestamp\"");
        }

        @Test
        @DisplayName("예외 발생 시 적절한 에러 메시지")
        void exception_AppropriateErrorMessage() throws Exception {
            // Given
            String token = "error-token";
            request.addHeader("Authorization", "Bearer " + token);
            request.setRequestURI("/api/protected");

            given(tokenProvider.validateTokenWithContext(eq(token), any()))
                .willThrow(new RuntimeException("Unexpected error"));

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
            assertThat(response.getContentAsString()).contains("Unexpected error");
        }
    }

    @Nested
    @DisplayName("SecurityContext 정리 테스트")
    class SecurityContextCleanupTest {

        @Test
        @DisplayName("요청 완료 후 SecurityContext 정리")
        void cleanupSecurityContext_AfterRequest() throws Exception {
            // Given
            request.setRequestURI("/api/protected");

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("예외 발생 시에도 SecurityContext 정리")
        void cleanupSecurityContext_OnException() throws Exception {
            // Given
            String token = "error-token";
            request.addHeader("Authorization", "Bearer " + token);
            request.setRequestURI("/api/protected");

            given(tokenProvider.validateTokenWithContext(eq(token), any()))
                .willThrow(new RuntimeException("Error"));

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }
}