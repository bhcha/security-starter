# Security Starter - 실제 구현 기능 문서

## 개요

security-starter는 Spring Boot 애플리케이션에 포괄적인 보안 기능을 제공하는 스타터 라이브러리입니다. 헥사고날 아키텍처를 기반으로 구현되었으며, JWT 인증, Keycloak 통합, 세션 관리, Rate Limiting, IP 제한 등 다양한 보안 기능을 지원합니다.

## 🏗️ 아키텍처 모드

### Traditional Mode (기본값)
- 모든 레이어에서 security-starter 자유 사용
- 전통적인 MVC 아키텍처 지원

### Hexagonal Mode
- Domain Layer에서 security-starter 사용 제한
- Application Layer 이상에서만 사용 권장
- 순수한 도메인 로직 보장

## 🔐 핵심 보안 기능

### 1. JWT 인증 시스템
- **Spring JWT Token Provider**: JJWT 라이브러리 기반 JWT 토큰 발급/검증
- **토큰 타입**: Access Token + Refresh Token 지원
- **알고리즘**: HS256, HS384, HS512, RS256, RS384, RS512 지원
- **토큰 검증**: 서명, 만료시간, 발급자, Audience 검증
- **컨텍스트 기반 검증**: 요청 URI, HTTP 메소드 기반 권한 체크

### 2. Keycloak 통합
- **Keycloak Token Provider**: Keycloak 서버와의 OAuth2 인증 통합
- **Grant Types**: password, authorization_code, client_credentials 지원
- **Token Introspection**: Keycloak 서버를 통한 토큰 검증
- **Public/Confidential Client**: 모든 클라이언트 타입 지원
- **프로덕션 환경 보안**: HTTPS 강제, 기본 설정 금지

### 3. JWT 인증 필터
- **OncePerRequestFilter**: 요청당 한 번만 실행되는 JWT 필터
- **토큰 추출**: Authorization Bearer 헤더에서 자동 추출
- **경로 제외**: 설정 가능한 제외 경로 패턴 (Ant Pattern)
- **Spring Security 통합**: SecurityContext에 인증 정보 자동 설정
- **에러 처리**: 상세한 JWT 인증 실패 처리

### 4. 세션 관리
- **Authentication Session**: 사용자별 인증 세션 추적
- **Account Lockout**: 실패 횟수 기반 계정 잠금 (기본: 5회)
- **Time Window**: 실패 시도 추적 시간 윈도우 (기본: 15분)
- **Auto Unlock**: 자동 계정 잠금 해제 (기본: 30분)
- **Session Events**: 인증 성공/실패 이벤트 처리

### 5. Rate Limiting (선택적)
- **Strategy**: SLIDING_WINDOW, FIXED_WINDOW, TOKEN_BUCKET
- **Multi-level Limiting**: Per-IP, Per-User, Per-Endpoint 제한
- **Distributed Support**: Redis 기반 분산 Rate Limiting
- **Configurable Time Window**: 유연한 시간 윈도우 설정

### 6. IP 제한 (선택적)
- **Whitelist/Blacklist Mode**: 허용/차단 IP 목록 관리
- **CIDR Notation**: IP 범위 지정 지원 (예: 192.168.0.0/16)
- **Proxy Support**: X-Forwarded-For, X-Real-IP 헤더 지원
- **Localhost Exception**: 로컬호스트 자동 허용 옵션
- **IP Caching**: 성능 최적화를 위한 IP 캐싱

### 7. 보안 헤더 (기본 활성화)
- **X-Frame-Options**: 클릭재킹 방지
- **X-Content-Type-Options**: MIME 타입 스니핑 방지
- **X-XSS-Protection**: XSS 공격 방지
- **Strict-Transport-Security**: HTTPS 강제
- **Content-Security-Policy**: CSP 정책 적용
- **Referrer-Policy**: 리퍼러 정책 설정

## 🏛️ 헥사고날 아키텍처 구조

### Domain Layer
```
com.ldx.hexacore.security.auth.domain/
├── vo/                    # Value Objects
│   ├── Credentials.java   # 사용자 인증 자격증명
│   ├── Token.java         # JWT 토큰 정보
│   └── AuthenticationStatus.java
├── event/                 # Domain Events
│   ├── AuthenticationSucceeded.java
│   ├── AuthenticationFailed.java
│   └── TokenExpired.java
├── service/               # Domain Services
│   ├── AuthenticationDomainService.java
│   └── JwtPolicy.java
└── Authentication.java    # Aggregate Root
```

### Application Layer
```
com.ldx.hexacore.security.auth.application/
├── command/
│   ├── handler/           # Use Case 구현체
│   │   ├── AuthenticateUseCaseImpl.java
│   │   └── TokenManagementUseCaseImpl.java
│   ├── port/in/          # Inbound Ports
│   │   ├── AuthenticationUseCase.java
│   │   ├── AuthenticateCommand.java
│   │   └── AuthenticationResult.java
│   └── port/out/         # Outbound Ports
│       ├── TokenProvider.java
│       ├── ExternalAuthProvider.java
│       └── EventPublisher.java
└── exception/            # Application Exceptions
```

### Adapter Layer
```
com.ldx.hexacore.security.auth.adapter/
├── inbound/
│   ├── filter/           # Security Filters
│   │   ├── JwtAuthenticationFilter.java
│   │   └── SecurityFilterConfig.java
│   ├── event/            # Event Listeners
│   │   └── AuthenticationEventListener.java
│   └── config/           # Inbound Configuration
└── outbound/
    ├── token/            # Token Providers
    │   ├── jwt/          # Spring JWT Implementation
    │   ├── keycloak/     # Keycloak Integration
    │   └── noop/         # No-Op Implementation
    ├── external/         # External Services
    │   └── KeycloakAuthenticationAdapter.java
    └── event/            # Event Publishers
        └── SpringEventPublisher.java
```

## ⚙️ 자동 설정 (Auto Configuration)

### SecurityStarterAutoConfiguration
메인 자동 설정 클래스로 다음 설정들을 Import합니다:

- **TraditionalModeConfiguration**: 전통적 모드 설정
- **HexagonalModeConfiguration**: 헥사고날 모드 설정
- **TokenProviderAutoConfiguration**: 토큰 제공자 자동 설정
- **SecurityFilterAutoConfiguration**: 보안 필터 자동 설정
- **ApplicationLayerAutoConfiguration**: 애플리케이션 레이어 설정
- **SupportBeansAutoConfiguration**: 지원 Bean 설정
- **LoggingBeansAutoConfiguration**: 로깅 Bean 설정

### 조건부 Bean 생성
- `@ConditionalOnProperty`: security-starter.enabled=true (기본값)
- `@ConditionalOnClass`: 필요한 클래스 존재 여부 확인
- `@ConditionalOnMissingBean`: 사용자 정의 Bean 우선 적용

## 🎛️ Feature Toggle 시스템

각 기능을 개별적으로 활성화/비활성화할 수 있습니다:

```yaml
security-starter:
  enabled: true  # 전체 스타터 활성화

  # 기능별 토글
  authentication-toggle:
    enabled: true    # 인증 기능
  session-toggle:
    enabled: true    # 세션 관리
  jwt-toggle:
    enabled: true    # JWT 토큰
  rate-limit-toggle:
    enabled: false   # Rate Limiting (기본 OFF)
  ip-restriction-toggle:
    enabled: false   # IP 제한 (기본 OFF)
  headers-toggle:
    enabled: true    # 보안 헤더 (기본 ON)
```

## 🔄 토큰 제공자 전략

### 1. Spring JWT Token Provider (기본값)
```yaml
security-starter:
  token-provider:
    provider: jwt
    jwt:
      enabled: true
      secret: "your-secret-key-at-least-32-chars"
      access-token-expiration: 3600      # 1시간
      refresh-token-expiration: 604800   # 7일
      issuer: "security-starter"
```

### 2. Keycloak Token Provider
```yaml
security-starter:
  token-provider:
    provider: keycloak
    keycloak:
      enabled: true
      server-url: "https://keycloak.example.com"
      realm: "your-realm"
      client-id: "your-client-id"
      client-secret: "your-client-secret"
      grant-type: "password"
```

### 3. No-Op Token Provider (Fallback)
JWT 라이브러리가 없을 때 자동으로 사용되는 더미 구현체

## 📊 로깅 및 모니터링

### Security Event Logger
- **인증 성공/실패 로깅**
- **리소스 접근 승인/거부 로깅**
- **토큰 만료 이벤트 로깅**
- **의심스러운 활동 감지**

### Security Request Logger
- **요청 시작/완료 로깅**
- **토큰 추출 로깅**
- **검증 컨텍스트 로깅**
- **검증 결과 및 소요시간 로깅**

### Suspicious Activity Tracker
- **실패 횟수 임계값 감지** (기본: 5회)
- **시간 윈도우 기반 추적** (기본: 5분)
- **클라이언트 IP별 추적**
- **자동 보안 알림**

## 🔧 설정 검증

### Startup Validator
애플리케이션 시작 시 보안 설정을 검증합니다:
- JWT Secret 강도 검증
- Keycloak 연결 테스트
- 프로덕션 환경 보안 설정 검증

### Health Indicator
Spring Actuator와 통합하여 보안 컴포넌트 상태를 모니터링합니다.

### Failure Analyzer
설정 오류 발생 시 상세한 해결 방안을 제공합니다.

## 🚀 Zero Configuration 원칙

별도 설정 없이도 기본 보안 기능이 즉시 동작합니다:
- JWT 인증 자동 활성화
- 기본 보안 헤더 적용
- 개발용 기본 설정 제공
- 프로덕션 환경 자동 감지

## 🔒 보안 모범 사례

### 프로덕션 환경 필수 설정
1. **강력한 JWT Secret**: 최소 64자 이상의 랜덤 문자열
2. **HTTPS 사용**: Keycloak 연동 시 HTTPS 필수
3. **토큰 만료시간**: Access Token 1시간, Refresh Token 7일 권장
4. **Rate Limiting**: 활성화하여 브루트포스 공격 방지
5. **IP 제한**: 필요시 Whitelist 모드로 접근 제한

### 개발 환경 편의 기능
- 기본 JWT Secret 제공 (프로덕션에서 자동 경고)
- localhost 자동 허용
- 상세한 디버그 로깅
- 설정 오류 시 친화적인 오류 메시지

## 📈 성능 최적화

### 캐싱 전략
- **Caffeine Cache**: 기본 로컬 캐싱
- **Redis Cache**: 분산 환경 캐싱 지원
- **IP Validation Cache**: IP 검증 결과 캐싱
- **Token Validation Cache**: 토큰 검증 결과 캐싱

### 필터 성능
- **Path Exclusion**: 불필요한 경로는 필터 제외
- **Once Per Request**: 중복 필터링 방지
- **빠른 실패**: 조기 검증 실패로 리소스 절약

## 🔄 확장성

### Custom Token Provider
```java
@Component
public class CustomTokenProvider implements TokenProvider {
    // 사용자 정의 토큰 제공자 구현
}
```

### Custom Authentication Filter
```java
@Component
public class CustomAuthFilter extends JwtAuthenticationFilter {
    // 사용자 정의 인증 필터 확장
}
```

### Event Listener
```java
@EventListener
public void handleAuthenticationSuccess(AuthenticationSucceeded event) {
    // 인증 성공 이벤트 처리
}
```

## 🛠️ 통합 가이드

### Spring Security 통합
- SecurityFilterChain과 자동 통합
- 기존 Spring Security 설정과 호환
- 사용자 정의 필터 체인 지원

### Spring Boot Actuator 통합
- Health Check 엔드포인트 제공
- 보안 메트릭 수집
- 실시간 보안 상태 모니터링

### 다른 스타터와의 호환성
- web-starter와 완전 호환
- database 관련 스타터와 연동 가능
- 마이크로서비스 아키텍처 지원

이 보안 스타터는 Spring Boot의 Auto Configuration 원칙을 따르며, 개발자가 최소한의 설정으로 강력한 보안 기능을 사용할 수 있도록 설계되었습니다.