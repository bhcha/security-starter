package com.example.hexagonal;

import com.ldx.hexacore.security.auth.application.command.port.in.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;

/**
 * Hexagonal Mode 예제
 * 
 * DDD 헥사고날 아키텍처를 엄격하게 준수하는 예제입니다.
 * Port-Adapter 패턴을 통해 레이어 간 의존성을 명확히 분리합니다.
 */
@SpringBootApplication
public class HexagonalModeExample {
    
    public static void main(String[] args) {
        SpringApplication.run(HexagonalModeExample.class, args);
    }
}

/**
 * Hexagonal Mode에서의 REST Adapter
 * Application Layer의 Port를 통해서만 통신
 */
@RestController
@RequestMapping("/api/v1/auth")
class AuthenticationRestAdapter {
    
    // Port 인터페이스를 통한 의존성 주입
    private final AuthenticationUseCase authenticationUseCase;
    private final TokenManagementUseCase tokenManagementUseCase;
    
    // 생성자 주입 (Hexagonal 권장 방식)
    public AuthenticationRestAdapter(
            AuthenticationUseCase authenticationUseCase,
            TokenManagementUseCase tokenManagementUseCase) {
        this.authenticationUseCase = authenticationUseCase;
        this.tokenManagementUseCase = tokenManagementUseCase;
    }
    
    /**
     * 인증 엔드포인트
     * RFC 7807 Problem Details 형식으로 에러 응답
     */
    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody AuthenticationRequest request) {
        try {
            // Application Layer의 Command 사용
            var command = new AuthenticateCommand(
                request.username(), 
                request.password()
            );
            
            // Use Case 실행
            AuthenticationResult result = authenticationUseCase.authenticate(command);
            
            if (result.isSuccess()) {
                // 성공 응답 DTO
                var response = new AuthenticationResponse(
                    result.getToken(),
                    result.getRefreshToken(),
                    Instant.now().plusSeconds(3600).toString(), // 만료 시간
                    "Bearer"
                );
                return ResponseEntity.ok(response);
            } else {
                // 실패 시 Problem Details
                return createProblemResponse(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication Failed",
                    result.getMessage()
                );
            }
        } catch (Exception e) {
            // 예외 시 Problem Details
            return createProblemResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Authentication Error",
                e.getMessage()
            );
        }
    }
    
    /**
     * 토큰 검증 엔드포인트
     */
    @PostMapping("/token/validate")
    public ResponseEntity<?> validateToken(@RequestBody TokenValidationRequest request) {
        try {
            var command = new ValidateTokenCommand(request.token());
            TokenValidationResult result = tokenManagementUseCase.validateToken(command);
            
            if (result.isValid()) {
                var response = new TokenValidationResponse(
                    true,
                    result.getUserId(),
                    result.getTokenType(),
                    result.getRemainingTime()
                );
                return ResponseEntity.ok(response);
            } else {
                return createProblemResponse(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid Token",
                    "The provided token is invalid or expired"
                );
            }
        } catch (Exception e) {
            return createProblemResponse(
                HttpStatus.BAD_REQUEST,
                "Token Validation Error",
                e.getMessage()
            );
        }
    }
    
    /**
     * 토큰 갱신 엔드포인트
     */
    @PostMapping("/token/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody TokenRefreshRequest request) {
        try {
            var command = new RefreshTokenCommand(request.refreshToken());
            RefreshTokenResult result = tokenManagementUseCase.refreshToken(command);
            
            var response = new TokenRefreshResponse(
                result.getNewAccessToken(),
                result.getNewRefreshToken(),
                Instant.now().plusSeconds(3600).toString()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return createProblemResponse(
                HttpStatus.UNAUTHORIZED,
                "Token Refresh Failed",
                e.getMessage()
            );
        }
    }
    
    /**
     * RFC 7807 Problem Details 응답 생성
     * Hexagonal Mode에서는 구조화된 에러 응답 사용
     */
    private ResponseEntity<ProblemDetail> createProblemResponse(
            HttpStatus status, String title, String detail) {
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setType(URI.create("https://api.example.com/problems/" + 
            title.toLowerCase().replace(" ", "-")));
        problemDetail.setTitle(title);
        problemDetail.setInstance(URI.create("/api/v1/auth"));
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("mode", "HEXAGONAL");
        
        return ResponseEntity.status(status).body(problemDetail);
    }
}

// Request DTOs (Inbound Adapter Layer)

record AuthenticationRequest(
    String username,
    String password
) {}

record TokenValidationRequest(
    String token
) {}

record TokenRefreshRequest(
    String refreshToken
) {}

// Response DTOs (Inbound Adapter Layer)

record AuthenticationResponse(
    String accessToken,
    String refreshToken,
    String expiresAt,
    String tokenType
) {}

record TokenValidationResponse(
    boolean valid,
    String userId,
    String tokenType,
    Long remainingTime
) {}

record TokenRefreshResponse(
    String accessToken,
    String refreshToken,
    String expiresAt
) {}

/**
 * Hexagonal Mode 설정 예제
 * application.yml
 */
class HexagonalModeConfig {
    /*
    hexacore:
      security:
        # Hexagonal Mode 명시적 설정
        mode: HEXAGONAL
        
        # 실제 Persistence 사용 (Hexagonal 권장)
        persistence:
          type: jpa
          command-db: security_write   # CQRS Write DB
          query-db: security_read      # CQRS Read DB
        
        # 엄격한 보안 설정
        tokenProvider:
          provider: keycloak    # 엔터프라이즈 환경에서는 Keycloak 권장
          keycloak:
            realm: enterprise
            client-id: security-service
            client-secret: ${KEYCLOAK_SECRET}
            auth-server-url: https://keycloak.example.com
        
        # 엄격한 세션 정책
        session:
          lockout:
            max-attempts: 3      # 엄격한 시도 제한
            lockout-duration-minutes: 60  # 긴 잠금 시간
            attempt-window-minutes: 15
        
        # 추가 보안 기능 활성화
        rateLimitToggle:
          enabled: true
        ipRestrictionToggle:
          enabled: true
        
        # Rate Limiting 설정
        rateLimit:
          requests-per-minute: 60
          burst-capacity: 10
        
        # IP 제한 설정
        ipRestriction:
          mode: WHITELIST
          allowed-ips:
            - 10.0.0.0/8
            - 192.168.0.0/16
    */
}

/**
 * Hexagonal Mode에서의 Custom Use Case 구현 예제
 * Port 인터페이스를 구현하여 비즈니스 로직 커스터마이징
 */
class CustomAuthenticationUseCase implements AuthenticationUseCase {
    
    @Override
    public AuthenticationResult authenticate(AuthenticateCommand command) {
        // 커스텀 인증 로직
        // 예: 추가 검증, 감사 로깅, 외부 시스템 연동 등
        
        // Domain Service 활용
        // Event 발행
        // Repository를 통한 영속화
        
        return AuthenticationResult.success(
            "custom-token",
            "custom-refresh-token"
        );
    }
}