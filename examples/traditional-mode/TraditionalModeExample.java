package com.example.traditional;

import com.ldx.hexacore.security.auth.application.command.port.in.*;
import com.ldx.hexacore.security.auth.domain.Authentication;
import com.ldx.hexacore.security.auth.domain.vo.Credentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

/**
 * Traditional Mode 예제
 * 
 * 빠른 개발을 위한 간단한 아키텍처 예제입니다.
 * Controller에서 Domain 객체를 직접 사용할 수 있습니다.
 */
@SpringBootApplication
public class TraditionalModeExample {
    
    public static void main(String[] args) {
        SpringApplication.run(TraditionalModeExample.class, args);
    }
}

/**
 * Traditional Mode에서의 Controller 구현
 * Domain 객체를 직접 사용하는 간단한 구조
 */
@RestController
@RequestMapping("/api/auth")
class TraditionalAuthController {
    
    @Autowired
    private AuthenticationUseCase authenticationUseCase;
    
    @Autowired
    private TokenManagementUseCase tokenManagementUseCase;
    
    /**
     * 로그인 엔드포인트
     * Traditional Mode에서는 간단하게 구현 가능
     */
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        // Domain 객체 직접 생성 및 사용
        var command = new AuthenticateCommand(
            request.username(), 
            request.password()
        );
        
        var result = authenticationUseCase.authenticate(command);
        
        // 간단한 응답 생성
        return new LoginResponse(
            result.isSuccess(),
            result.getToken(),
            result.getRefreshToken(),
            result.getMessage()
        );
    }
    
    /**
     * 토큰 검증 엔드포인트
     */
    @PostMapping("/validate")
    public TokenValidationResponse validateToken(@RequestBody TokenRequest request) {
        var command = new ValidateTokenCommand(request.token());
        var result = tokenManagementUseCase.validateToken(command);
        
        return new TokenValidationResponse(
            result.isValid(),
            result.getUserId(),
            result.getTokenType()
        );
    }
    
    /**
     * 토큰 갱신 엔드포인트
     */
    @PostMapping("/refresh")
    public RefreshResponse refreshToken(@RequestBody RefreshRequest request) {
        var command = new RefreshTokenCommand(request.refreshToken());
        var result = tokenManagementUseCase.refreshToken(command);
        
        return new RefreshResponse(
            result.getNewAccessToken(),
            result.getNewRefreshToken()
        );
    }
}

// Request/Response DTOs (Traditional Mode에서는 간단하게 정의)

record LoginRequest(String username, String password) {}

record LoginResponse(
    boolean success,
    String accessToken,
    String refreshToken,
    String message
) {}

record TokenRequest(String token) {}

record TokenValidationResponse(
    boolean valid,
    String userId,
    String tokenType
) {}

record RefreshRequest(String refreshToken) {}

record RefreshResponse(
    String accessToken,
    String refreshToken
) {}

/**
 * Traditional Mode 설정 예제
 * application.yml
 */
class TraditionalModeConfig {
    /*
    hexacore:
      security:
        # Traditional Mode가 기본값이므로 생략 가능
        mode: TRADITIONAL
        
        # 모든 기능이 기본적으로 활성화됨
        # 필요시 개별 기능 비활성화 가능
        rateLimitToggle:
          enabled: true    # Rate Limiting 활성화
        
        # 간단한 개발을 위한 설정
        tokenProvider:
          provider: spring_jwt
          jwt:
            secret: ${JWT_SECRET:dev-secret-key}
            accessTokenExpiry: PT2H    # 개발 중에는 긴 만료 시간
        
        # 느슨한 세션 정책
        session:
          lockout:
            max-attempts: 10    # 개발 중에는 많은 시도 허용
            lockout-duration-minutes: 5
    */
}