# Authentication 도메인 레이어 명세서

## 개요
Authentication 애그리거트의 도메인 레이어 구현 완료 명세서입니다. Phase 2-4를 통해 Value Objects, Entities & Aggregates, Events & Services가 모두 구현되었습니다.

## Value Objects

### Credentials
- **속성**: username (String), password (String)
- **검증 규칙**: 
  - username: null/empty/blank 불가, 길이 1-100자
  - password: null/empty/blank 불가, 길이 8-100자
- **주요 메서드**: 
  - `static Credentials of(String username, String password)`
  - `String getUsername()`
  - `String getPassword()`

### Token
- **속성**: accessToken (String), refreshToken (String), expiresIn (long), expired (boolean)
- **검증 규칙**:
  - accessToken: null/empty/blank 불가
  - refreshToken: null/empty/blank 불가
  - expiresIn: 1-86400초 (최대 24시간)
- **주요 메서드**:
  - `static Token of(String accessToken, String refreshToken, long expiresIn)`
  - `boolean isExpired()`
  - `void expire()`

### AuthenticationStatus
- **속성**: status (Status enum: PENDING, SUCCESS, FAILED)
- **검증 규칙**: status null 불가
- **주요 메서드**:
  - `static AuthenticationStatus pending()`
  - `static AuthenticationStatus success()`
  - `static AuthenticationStatus failed()`
  - `boolean isPending()`, `boolean isSuccess()`, `boolean isFailed()`

## Entities
해당 없음 - Authentication 애그리거트에는 별도 Entity가 없음

## Aggregate Root

### Authentication
- **식별자**: UUID id
- **속성**: 
  - credentials (Credentials)
  - status (AuthenticationStatus)
  - token (Token, nullable)
  - attemptTime (LocalDateTime)
  - successTime (LocalDateTime, nullable)
  - failureTime (LocalDateTime, nullable)
  - failureReason (String, nullable)
  - tokenExpiredTime (LocalDateTime, nullable)

- **불변 규칙**:
  1. PENDING 상태에서만 SUCCESS/FAILED로 전이 가능
  2. SUCCESS/FAILED 상태에서 다른 상태로 변경 불가
  3. SUCCESS 상태에서만 유효한 Token 보유
  4. Token 없이 만료 처리 불가
  5. 이미 만료된 Token 재만료 불가

- **비즈니스 메서드**:
  - `static Authentication attemptAuthentication(Credentials credentials)`: 인증 시도
  - `void markAsSuccessful(Token token)`: 인증 성공 처리
  - `void markAsFailed(String reason)`: 인증 실패 처리
  - `void expireToken()`: 토큰 만료 처리
  - `boolean isTokenValid()`: 토큰 유효성 검증

## Domain Events

### AuthenticationAttempted
- **발생 조건**: 인증 시도 시
- **포함 데이터**: authenticationId, username, attemptTime
- **목적**: 인증 시도 추적 및 모니터링

### AuthenticationSucceeded
- **발생 조건**: 인증 성공 시
- **포함 데이터**: authenticationId, token, successTime
- **목적**: 성공한 인증 이벤트 추적

### AuthenticationFailed
- **발생 조건**: 인증 실패 시
- **포함 데이터**: authenticationId, reason, failureTime
- **목적**: 실패한 인증 이벤트 추적 및 보안 모니터링

### TokenExpired
- **발생 조건**: 토큰 만료 처리 시
- **포함 데이터**: authenticationId, expiredToken, expiredTime
- **목적**: 토큰 만료 이벤트 추적

## Domain Services

### AuthenticationDomainService
- **책임**: 인증 처리 도메인 로직
- **메서드 시그니처**: `Authentication authenticate(Credentials credentials)`
- **역할**: Credentials를 받아 Authentication 애그리거트 생성

### JwtPolicy
- **책임**: JWT 토큰 정책 검증
- **메서드 시그니처**: `boolean validate(Token token)`
- **역할**: 토큰 유효성 정책 검증 (만료 여부 등)

### SessionPolicy
- **책임**: 세션 정책 검증
- **메서드 시그니처**: `boolean validateSession(Authentication authentication)`
- **역할**: 세션 유효성 검증 (상태, 만료 시간, 토큰 유효성)
- **정책**: 24시간 세션 타임아웃

## 구현 특징

### 설계 원칙 준수
- **불변성**: 모든 Value Object는 불변
- **캡슐화**: 내부 상태는 비즈니스 메서드를 통해서만 변경
- **자가 검증**: 각 객체는 생성 시 자체 검증
- **도메인 이벤트**: 중요한 비즈니스 이벤트 발행

### 테스트 커버리지
- **총 테스트 케이스**: 85개 이상
- **테스트 범위**: 
  - Value Objects: 51개 테스트
  - Domain Events: 31개 테스트  
  - Aggregate Root: 13개 이상 테스트
  - Domain Services: 23개 테스트
- **테스트 성공률**: 100%
- **테스트 유형**: 단위 테스트, 불변 규칙 테스트, 도메인 이벤트 테스트, 정책 검증 테스트

### 코딩 표준 준수
- DDD 패턴 적용
- 정적 팩토리 메서드 사용
- equals/hashCode 구현
- 방어적 복사 적용
- 명확한 예외 메시지

## 다음 단계
Authentication 애그리거트의 도메인 레이어 구현이 완료되었습니다. 다음은 **Phase 5: Command Side 구현**으로 Application 레이어 개발을 진행합니다.

## 파일 위치 참조
- **도메인 패키지**: `/src/main/java/com/dx/hexacore/security/domain/auth/`
- **테스트 패키지**: `/src/test/java/com/dx/hexacore/security/domain/auth/`
- **문서**: `/docs/plan/`, `/docs/testscenario/`, `/docs/review/`