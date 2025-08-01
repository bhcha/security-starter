# 구현 계획서

## 🎯 프로젝트 목표
이 프로젝트는 **Spring Boot Starter**로 제공될 보안 인프라스트럭처 레이어입니다.
- 독립 실행 애플리케이션이 아닌 **라이브러리 형태**로 제공
- 다른 프로젝트에서 의존성 추가만으로 보안 기능 활성화
- 주로 **필터**, **이벤트 리스너**, **자동 설정**으로 구성

## 1. 도메인 모델 분석 결과

### Bounded Context
- **Client Authentication Context**: 사용자 인증 및 JWT 토큰 발급 관리 (Keycloak 통합)
- **Access Control Context**: IP 제한, Rate Limiting, 토큰 검증을 통한 API 접근 제어
- **Security Monitoring Context**: 보안 이벤트 추적, 위험도 평가, 감사 로깅
- **Alert Management Context**: 보안 알림 발송, 재시도 정책, 알림 채널 관리
- **Identity Provider Integration Context**: Keycloak과의 통합, 토큰 발급/검증 프록시

### Aggregate 목록
1. **Authentication**: 인증 처리 및 토큰 발급
2. **AuthenticationSession**: 인증 세션 및 실패 관리
3. **AccessPolicy**: IP 제한 및 접근 정책 관리
4. **RateLimitBucket**: Rate Limiting 처리
5. **SecurityEvent**: 보안 이벤트 및 위험도 평가
6. **SecurityAlert**: 보안 알림 관리

### 의존성 관계
- AuthenticationSession → Authentication: 인증 정보 참조
- AccessPolicy → Authentication: 토큰 검증 시 인증 정보 필요
- RateLimitBucket → AccessPolicy: 정책 기반 제한 적용
- SecurityEvent → Authentication/AccessPolicy: 보안 이벤트 발생 원인
- SecurityAlert → SecurityEvent: 이벤트 기반 알림 발송

## 2. 구현 순서
1차: **Authentication** - 가장 기본이 되는 인증 애그리거트
2차: **AuthenticationSession** - Authentication에 의존
3차: **AccessPolicy** - 독립적으로 구현 가능
4차: **RateLimitBucket** - AccessPolicy와 연관
5차: **SecurityEvent** - 다른 애그리거트들의 이벤트 수집
6차: **SecurityAlert** - SecurityEvent 기반 알림

## 3. 애그리거트별 상세 계획

### Authentication (1차 애그리거트)
#### Domain Layer (Phase 2-4)
- Value Objects:
  - Credentials (username, password)
  - Token (accessToken, refreshToken, expiresIn)
  - AuthenticationStatus (PENDING, SUCCESS, FAILED)
- Entities: 없음
- Aggregate Root: Authentication
- Domain Events:
  - AuthenticationAttempted
  - AuthenticationSucceeded
  - AuthenticationFailed
  - TokenExpired
- Domain Services:
  - AuthenticationDomainService
  - JwtPolicy
  - SessionPolicy

#### Application Layer (Phase 5-6)
- Commands:
  - AuthenticateCommand
  - ValidateTokenCommand
- Queries:
  - GetAuthenticationQuery
  - GetTokenInfoQuery
- Use Cases:
  - AuthenticateUseCase
  - ValidateTokenUseCase
  - RefreshTokenUseCase

#### Adapter Layer (Phase 7-8)
- Inbound:
  - Security Filter (JwtAuthenticationFilter) - JWT 토큰 검증 필터
  - Event Listener (AuthenticationEventListener) - 외부 인증 이벤트 수신
  - ⚠️ REST API는 제공하지 않음 (사용자가 직접 구현)
- Outbound:
  - JPA Persistence (AuthenticationJpaAdapter) - 조건부 설정
  - Event Publisher (AuthenticationEventPublisher)
  - Keycloak Integration (KeycloakAdapter) - 조건부 설정

### AuthenticationSession (2차 애그리거트)
#### Domain Layer (Phase 2-4)
- Value Objects:
  - SessionId
  - ClientIp
  - RiskLevel
- Entities:
  - AuthenticationAttempt
- Aggregate Root: AuthenticationSession
- Domain Events:
  - AccountLocked
- Domain Services: 없음

#### Application Layer (Phase 5-6)
- Commands:
  - RecordAuthenticationAttemptCommand
  - UnlockAccountCommand
- Queries:
  - GetSessionStatusQuery
  - GetFailedAttemptsQuery
- Use Cases:
  - RecordAttemptUseCase
  - CheckLockoutUseCase
  - UnlockAccountUseCase

#### Adapter Layer (Phase 7-8)
- Inbound:
  - Event Listener (SessionEventListener) - 세션 관련 이벤트 처리
  - ⚠️ 별도의 REST API 제공하지 않음
- Outbound:
  - JPA Persistence (SessionJpaAdapter) - 조건부 설정
  - Cache Adapter (SessionCacheAdapter) - 조건부 설정 (Caffeine/Redis)

### AccessPolicy (3차 애그리거트)
#### Domain Layer (Phase 2-4)
- Value Objects:
  - PolicyId
  - IpRange
  - TimeWindow
  - ValidationResult
- Entities:
  - IpRestriction
  - RateLimitRule
- Aggregate Root: AccessPolicy
- Domain Events:
  - AccessGranted
  - AccessDenied
- Domain Services:
  - IpMatchingService

#### Application Layer (Phase 5-6)
- Commands:
  - CreatePolicyCommand
  - UpdatePolicyCommand
  - ValidateAccessCommand
- Queries:
  - GetPolicyQuery
  - GetActivePoliciesQuery
- Use Cases:
  - CreatePolicyUseCase
  - ValidateAccessUseCase
  - UpdatePolicyUseCase

#### Adapter Layer (Phase 7-8)
- Inbound:
  - Security Filter (IpRestrictionFilter) - IP 기반 접근 제어 필터
  - Event Listener (PolicyEventListener) - 정책 변경 이벤트 처리
  - ⚠️ REST API는 관리 기능이므로 사용자가 필요시 구현
- Outbound:
  - JPA Persistence (PolicyJpaAdapter) - 조건부 설정
  - Cache Adapter (PolicyCacheAdapter) - 조건부 설정

### RateLimitBucket (4차 애그리거트)
#### Domain Layer (Phase 2-4)
- Value Objects:
  - BucketKey
  - ConsumptionResult
- Entities: 없음
- Aggregate Root: RateLimitBucket
- Domain Events:
  - RateLimitExceeded
- Domain Services: 없음

#### Application Layer (Phase 5-6)
- Commands:
  - ConsumeTokenCommand
  - RefillBucketCommand
- Queries:
  - GetBucketStatusQuery
  - GetAvailableTokensQuery
- Use Cases:
  - ConsumeTokenUseCase
  - CheckLimitUseCase
  - RefillBucketUseCase

#### Adapter Layer (Phase 7-8)
- Inbound:
  - Security Filter (RateLimitFilter) - API 호출 빈도 제한 필터
  - ⚠️ REST API 제공하지 않음
- Outbound:
  - Cache Adapter (BucketCacheAdapter) - 필수 (Redis/Caffeine)
  - Persistence Adapter (BucketPersistenceAdapter) - 선택적

### SecurityEvent (5차 애그리거트)
#### Domain Layer (Phase 2-4)
- Value Objects:
  - EventId
  - SecurityEventType
  - ClientIdentifier
  - EventMetadata
- Entities:
  - RiskAssessment
  - RiskFactor
- Aggregate Root: SecurityEvent
- Domain Events:
  - RiskLevelEvaluated
  - SecurityAlertTriggered
- Domain Services:
  - RiskEvaluationService

#### Application Layer (Phase 5-6)
- Commands:
  - RecordSecurityEventCommand
  - EvaluateRiskCommand
- Queries:
  - GetSecurityEventsQuery
  - GetRiskAssessmentQuery
  - GetAuditLogQuery
- Use Cases:
  - RecordEventUseCase
  - EvaluateRiskUseCase
  - GenerateAuditReportUseCase

#### Adapter Layer (Phase 7-8)
- Inbound:
  - Event Listener (SecurityEventListener) - 보안 이벤트 수집
  - ⚠️ REST API는 감사 기능이므로 사용자가 필요시 구현
- Outbound:
  - JPA Persistence (EventJpaAdapter) - 조건부 설정
  - Metrics Publisher (MetricsAdapter) - 조건부 설정
  - OpenTelemetry Adapter - 조건부 설정

### SecurityAlert (6차 애그리거트)
#### Domain Layer (Phase 2-4)
- Value Objects:
  - AlertId
  - AlertType
  - AlertPriority
  - Recipient
  - AlertMessage
- Entities:
  - DeliveryAttempt
- Aggregate Root: SecurityAlert
- Domain Events:
  - AlertSent
  - AlertFailed
  - AlertRetryScheduled
  - AlertEscalated
- Domain Services: 없음

#### Application Layer (Phase 5-6)
- Commands:
  - CreateAlertCommand
  - SendAlertCommand
  - RetryAlertCommand
- Queries:
  - GetAlertStatusQuery
  - GetPendingAlertsQuery
- Use Cases:
  - CreateAlertUseCase
  - SendAlertUseCase
  - RetryFailedAlertsUseCase

#### Adapter Layer (Phase 7-8)
- Inbound:
  - Event Listener (AlertEventListener) - 알림 트리거 이벤트 수신
  - ⚠️ REST API는 관리 기능이므로 사용자가 필요시 구현
- Outbound:
  - JPA Persistence (AlertJpaAdapter) - 조건부 설정
  - Email Adapter (SmtpAdapter) - 조건부 설정
  - Slack Adapter (SlackAdapter) - 조건부 설정  
  - Webhook Adapter (WebhookAdapter) - 조건부 설정

## 4. Spring Boot Starter 구현 가이드라인

### 4.1 Adapter Layer 구현 원칙
- **Inbound Adapters**:
  - **필터(Filter)**: 보안 관련 횡단 관심사 처리 (인증, 인가, Rate Limiting 등)
  - **이벤트 리스너**: 외부 시스템 이벤트 수신 및 처리
  - **REST API 제외**: 사용자가 필요에 따라 직접 구현
  
- **Outbound Adapters**:
  - **조건부 설정**: 데이터베이스, 캐시, 외부 시스템별 자동 설정
  - **인터페이스 제공**: Port 인터페이스만 제공, 구현체는 조건부 Bean

### 4.2 Auto Configuration 구조
```
security-auth-starter/
├── autoconfigure/
│   ├── SecurityAuthAutoConfiguration.java          # 메인 설정
│   ├── DomainAutoConfiguration.java               # Domain Layer Bean
│   ├── ApplicationAutoConfiguration.java          # Application Layer Bean
│   ├── SecurityFilterAutoConfiguration.java       # 보안 필터 체인
│   └── AdapterAutoConfiguration.java              # 조건부 어댑터 설정
└── properties/
    └── SecurityAuthProperties.java                # 설정 프로퍼티

```

### 4.3 필터 체인 구성
1. **JwtAuthenticationFilter**: JWT 토큰 검증
2. **RateLimitFilter**: API 호출 빈도 제한
3. **IpRestrictionFilter**: IP 기반 접근 제어
4. **SecurityEventFilter**: 보안 이벤트 수집

### 4.4 조건부 Bean 등록 전략
- **@ConditionalOnProperty**: 기능별 활성화/비활성화
- **@ConditionalOnClass**: 필요 라이브러리 존재 여부
- **@ConditionalOnBean**: 필수 Bean 존재 여부
- **@ConditionalOnMissingBean**: 커스텀 구현 허용