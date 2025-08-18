# Security Starter

Enterprise-grade Spring Boot Security Starter with DDD Hexagonal Architecture

[![Version](https://img.shields.io/badge/version-1.0.2-blue.svg)](https://github.com/your-org/security-starter/releases)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-green.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://openjdk.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 🎯 What is this?

Security-Starter는 Spring Boot 애플리케이션을 위한 엔터프라이즈급 보안 라이브러리입니다. DDD 헥사고날 아키텍처를 기반으로 하여 확장 가능하고 유지보수하기 쉬운 보안 솔루션을 제공합니다.

## ✨ Key Features

### 🔐 Authentication & Authorization
- **JWT/Keycloak 지원**: 유연한 토큰 기반 인증 with 다중 프로바이더
- **자동 토큰 검증**: Spring Security Filter 자동 등록
- **토큰 관리**: 액세스/리프레시 토큰 자동 관리

### 🛡️ Session Management  
- **지능형 계정 잠금**: 실패 시도 추적 및 자동 잠금
- **보안 정책 설정**: 최대 시도 횟수, 잠금 시간, 시도 윈도우 설정
- **실시간 모니터링**: 인증 시도 및 보안 이벤트 추적

### 🏗️ Architecture
- **DDD 헥사고날 아키텍처**: 비즈니스 로직과 기술 구현 분리
- **CQRS 패턴**: Command와 Query 분리로 확장성 향상
- **이벤트 기반**: 도메인 이벤트를 통한 느슨한 결합

### 🔧 Developer Experience
- **자동 설정**: Spring Boot Auto Configuration
- **설정 검증**: 시작 시 설정 자동 검증 및 친화적 에러 메시지
- **타입 안전성**: 완전한 Java 17+ 및 Spring Boot 3.5+ 지원

Check the [📖 GUIDE](GUIDE.md) for detailed setup and usage instructions.


## 🤝 Contribution

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License. For details, refer to the [LICENSE](LICENSE) file.

## 📞 Support
- Inquiries: oneforall88@gmail.com

## 🚀 Quick Start

### 1. Add Dependency

```gradle
dependencies {
    implementation 'com.dx:security-starter:1.0.2'
}
```

### 2. Basic Configuration

```yaml
hexacore:
  security:
    enabled: true
    token-provider:
      provider: jwt
      jwt:
        secret: ${JWT_SECRET:your-secret-key}
    session:
      enabled: true
      lockout:
        max-attempts: 5
        lockout-duration-minutes: 30
```

### 3. Use in Your Code

```java
@RestController
public class AuthController {
    
    private final AuthenticationUseCase authenticationUseCase;
    private final SessionManagementUseCase sessionManagementUseCase;
    
    @PostMapping("/login")
    public ResponseEntity<AuthResult> login(@RequestBody LoginRequest request) {
        var command = new AuthenticateCommand(request.username(), request.password());
        var result = authenticationUseCase.authenticate(command);
        return ResponseEntity.ok(result);
    }
}
```

## 🔄 Version History

### v1.0.2 (2025-08-02)
- **개발자 경험 대폭 개선**
- ✅ 설정 검증 강화 (JWT/Keycloak 프로덕션 보안 검증)
- ✅ 개발자 친화적 에러 메시지 및 해결방안 제시
- ✅ 전역 설정 검증자 구현
- ✅ 모든 테스트 통과 (812개 테스트)
- 🐛 sessionManagementUseCase Bean 누락 수정

### v1.0.1 (2025-08-02)
- 🐛 Spring Security 의존성 문제 해결
- 🐛 Bean 이름 일관성 수정
- 🐛 테스트 실패 문제 해결

### v1.0.0 (2025-08-02)
- 🎉 Initial release
- JWT and Keycloak authentication support
- Session management with lockout policies
- DDD Hexagonal Architecture implementation
- CQRS pattern with event sourcing
- Multi-storage support (JPA, in-memory)