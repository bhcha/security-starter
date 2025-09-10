package com.ldx.hexacore.security.auth.adapter.inbound.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationEntryPoint 테스트")
class JwtAuthenticationEntryPointTest {

    private JwtAuthenticationEntryPoint entryPoint;
    private ObjectMapper objectMapper;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        entryPoint = new JwtAuthenticationEntryPoint(objectMapper);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("인증 실패 시 401 응답 반환")
    void commence_ReturnsUnauthorizedResponse() throws Exception {
        // Given
        request.setMethod("GET");
        request.setRequestURI("/api/protected");
        AuthenticationException authException = new InsufficientAuthenticationException("Authentication required");

        // When
        entryPoint.commence(request, response, authException);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getContentType()).isEqualTo("application/json;charset=UTF-8");
        assertThat(response.getCharacterEncoding()).isEqualTo("UTF-8");
    }

    @Test
    @DisplayName("에러 응답에 필수 필드 포함")
    void commence_ResponseContainsRequiredFields() throws Exception {
        // Given
        request.setMethod("POST");
        request.setRequestURI("/api/users");
        AuthenticationException authException = new BadCredentialsException("Invalid credentials");

        // When
        entryPoint.commence(request, response, authException);

        // Then
        String jsonResponse = response.getContentAsString();
        assertThat(jsonResponse).contains("\"success\":false");
        assertThat(jsonResponse).contains("\"message\":\"Authentication required\"");
        assertThat(jsonResponse).contains("\"timestamp\"");
        assertThat(jsonResponse).contains("\"status\":401");
        assertThat(jsonResponse).contains("\"path\":\"/api/users\"");
    }

    @Test
    @DisplayName("AuthenticationException이 null일 때도 정상 처리")
    void commence_HandlesNullException() throws Exception {
        // Given
        request.setMethod("GET");
        request.setRequestURI("/api/data");

        // When
        entryPoint.commence(request, response, null);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        String jsonResponse = response.getContentAsString();
        assertThat(jsonResponse).contains("\"success\":false");
        assertThat(jsonResponse).contains("\"message\":\"Authentication required\"");
    }

    @Test
    @DisplayName("다양한 인증 예외 처리")
    void commence_HandlesDifferentAuthenticationExceptions() throws Exception {
        // Given
        request.setMethod("DELETE");
        request.setRequestURI("/api/resource/123");
        AuthenticationException authException = new BadCredentialsException("Bad credentials");

        // When
        entryPoint.commence(request, response, authException);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        String jsonResponse = response.getContentAsString();
        assertThat(jsonResponse).contains("\"message\":\"Authentication required\"");
        // 메시지는 항상 "Authentication required"로 통일되어 있음
    }

    @Test
    @DisplayName("응답 형식이 올바른 JSON인지 검증")
    void commence_ValidJsonResponse() throws Exception {
        // Given
        request.setMethod("PUT");
        request.setRequestURI("/api/update");
        AuthenticationException authException = new InsufficientAuthenticationException("Need auth");

        // When
        entryPoint.commence(request, response, authException);

        // Then
        String jsonResponse = response.getContentAsString();
        // JSON 파싱이 정상적으로 되는지 확인
        objectMapper.readTree(jsonResponse);
        
        // JSON 구조 검증
        assertThat(jsonResponse).startsWith("{");
        assertThat(jsonResponse).endsWith("}");
        assertThat(jsonResponse).contains("\"success\":false");
    }

    @Test
    @DisplayName("특수 문자가 포함된 경로 처리")
    void commence_HandlesSpecialCharactersInPath() throws Exception {
        // Given
        request.setMethod("GET");
        request.setRequestURI("/api/data?query=test&value=123");
        AuthenticationException authException = new InsufficientAuthenticationException("Auth needed");

        // When
        entryPoint.commence(request, response, authException);

        // Then
        String jsonResponse = response.getContentAsString();
        assertThat(jsonResponse).contains("\"path\":\"/api/data?query=test&value=123\"");
    }

    @Test
    @DisplayName("HTTP 메서드 정보가 로그에만 포함되고 응답에는 포함되지 않음")
    void commence_HttpMethodNotInResponse() throws Exception {
        // Given
        request.setMethod("PATCH");
        request.setRequestURI("/api/patch");
        AuthenticationException authException = new InsufficientAuthenticationException("Auth required");

        // When
        entryPoint.commence(request, response, authException);

        // Then
        String jsonResponse = response.getContentAsString();
        // HTTP 메서드는 응답에 포함되지 않음
        assertThat(jsonResponse).doesNotContain("PATCH");
        assertThat(jsonResponse).doesNotContain("\"method\"");
    }
}