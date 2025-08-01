package com.hexa.hr.controller;

import com.dx.hexacore.security.auth.application.command.port.in.AuthenticateCommand;
import com.dx.hexacore.security.auth.application.command.port.in.AuthenticationResult;
import com.dx.hexacore.security.auth.application.command.port.in.AuthenticationUseCase;
import com.dx.hexacore.security.auth.application.command.port.in.TokenManagementUseCase;
import com.dx.hexacore.security.auth.application.command.port.in.TokenValidationResult;
import com.dx.hexacore.security.auth.application.command.port.in.ValidateTokenCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

/**
 * hexa-hr 프로젝트용 인증 컨트롤러
 * 
 * security-starter 라이브러리를 사용하여 JWT 기반 인증을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class HexaHrAuthController {
    
    private final AuthenticationUseCase authenticationUseCase;
    private final TokenManagementUseCase tokenManagementUseCase;
    
    /**
     * 사용자 로그인
     * 
     * @param request 로그인 요청 (username, password)
     * @return JWT 토큰을 포함한 인증 결과
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            log.info("Login attempt for user: {}", request.getUsername());
            
            AuthenticateCommand command = new AuthenticateCommand(
                request.getUsername(), 
                request.getPassword()
            );
            
            AuthenticationResult result = authenticationUseCase.authenticate(command);
            
            if (result.isSuccess()) {
                log.info("Login successful for user: {}", request.getUsername());
                
                return ResponseEntity.ok(LoginResponse.builder()
                    .success(true)
                    .username(result.getUsername())
                    .accessToken(result.getToken().map(token -> token.getAccessToken()).orElse(null))
                    .refreshToken(result.getToken().map(token -> token.getRefreshToken()).orElse(null))
                    .expiresIn(result.getToken().map(token -> token.getExpiresIn()).orElse(0))
                    .message("Login successful")
                    .build());
            } else {
                log.warn("Login failed for user: {} - {}", 
                    request.getUsername(), 
                    result.getFailureReason().orElse("Unknown reason"));
                    
                return ResponseEntity.badRequest().body(LoginResponse.builder()
                    .success(false)
                    .message(result.getFailureReason().orElse("Authentication failed"))
                    .build());
            }
            
        } catch (Exception e) {
            log.error("Login error for user: {}", request.getUsername(), e);
            
            return ResponseEntity.internalServerError().body(LoginResponse.builder()
                .success(false)
                .message("Internal server error")
                .build());
        }
    }
    
    /**
     * JWT 토큰 검증
     * 
     * @param request 토큰 검증 요청
     * @return 토큰 유효성 검증 결과
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@Valid @RequestBody TokenValidationRequest request) {
        try {
            ValidateTokenCommand command = new ValidateTokenCommand(request.getToken());
            TokenValidationResult result = tokenManagementUseCase.validateToken(command);
            
            if (result.isValid()) {
                return ResponseEntity.ok(TokenValidationResponse.builder()
                    .valid(true)
                    .username(result.getUsername())
                    .message("Token is valid")
                    .build());
            } else {
                return ResponseEntity.badRequest().body(TokenValidationResponse.builder()
                    .valid(false)
                    .message(result.getInvalidReason().orElse("Token is invalid"))
                    .build());
            }
            
        } catch (Exception e) {
            log.error("Token validation error", e);
            
            return ResponseEntity.internalServerError().body(TokenValidationResponse.builder()
                .valid(false)
                .message("Internal server error")
                .build());
        }
    }
    
    /**
     * 서비스 상태 확인
     * 
     * @return 서비스 상태 정보
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "hexa-hr-auth",
            "timestamp", System.currentTimeMillis(),
            "version", "1.0.0"
        ));
    }
    
    // DTO 클래스들
    
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class LoginRequest {
        @jakarta.validation.constraints.NotBlank(message = "Username is required")
        private String username;
        
        @jakarta.validation.constraints.NotBlank(message = "Password is required")
        private String password;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class LoginResponse {
        private boolean success;
        private String username;
        private String accessToken;
        private String refreshToken;
        private int expiresIn;
        private String message;
    }
    
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TokenValidationRequest {
        @jakarta.validation.constraints.NotBlank(message = "Token is required")
        private String token;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TokenValidationResponse {
        private boolean valid;
        private String username;
        private String message;
    }
}