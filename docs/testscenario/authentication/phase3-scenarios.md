# Authentication Aggregate 테스트 시나리오

## 1. Authentication Aggregate Root 테스트 시나리오

### 1.1 인증 시도 시나리오

#### 시나리오 1.1.1: 유효한 자격증명으로 인증 시도
- **Given**: 유효한 Credentials (username, password)
- **When**: Authentication.attemptAuthentication(credentials) 호출
- **Then**: 
  - AuthenticationAttempted 도메인 이벤트 발생
  - 상태가 PENDING으로 변경

#### 시나리오 1.1.2: null 자격증명으로 인증 시도
- **Given**: null Credentials
- **When**: Authentication.attemptAuthentication(null) 호출
- **Then**: IllegalArgumentException 발생

### 1.2 인증 성공 시나리오

#### 시나리오 1.2.1: 인증 성공 처리
- **Given**: PENDING 상태의 Authentication
- **When**: Authentication.markAsSuccessful(token) 호출
- **Then**:
  - 상태가 SUCCESS로 변경
  - Token이 설정됨
  - AuthenticationSucceeded 도메인 이벤트 발생

#### 시나리오 1.2.2: 이미 완료된 인증에 성공 마킹
- **Given**: SUCCESS 상태의 Authentication
- **When**: Authentication.markAsSuccessful(token) 호출
- **Then**: IllegalStateException 발생

#### 시나리오 1.2.3: null 토큰으로 성공 마킹
- **Given**: PENDING 상태의 Authentication
- **When**: Authentication.markAsSuccessful(null) 호출
- **Then**: IllegalArgumentException 발생

### 1.3 인증 실패 시나리오

#### 시나리오 1.3.1: 인증 실패 처리
- **Given**: PENDING 상태의 Authentication
- **When**: Authentication.markAsFailed(reason) 호출
- **Then**:
  - 상태가 FAILED로 변경
  - 실패 사유가 설정됨
  - AuthenticationFailed 도메인 이벤트 발생

#### 시나리오 1.3.2: 빈 실패 사유로 실패 마킹
- **Given**: PENDING 상태의 Authentication
- **When**: Authentication.markAsFailed("") 호출
- **Then**: IllegalArgumentException 발생

### 1.4 토큰 만료 시나리오

#### 시나리오 1.4.1: 토큰 만료 처리
- **Given**: SUCCESS 상태이며 유효한 Token을 가진 Authentication
- **When**: Authentication.expireToken() 호출
- **Then**:
  - Token이 만료 상태로 변경
  - TokenExpired 도메인 이벤트 발생

#### 시나리오 1.4.2: 토큰이 없는 상태에서 만료 시도
- **Given**: Token이 없는 Authentication
- **When**: Authentication.expireToken() 호출
- **Then**: IllegalStateException 발생

### 1.5 토큰 유효성 검증 시나리오

#### 시나리오 1.5.1: 유효한 토큰 검증
- **Given**: SUCCESS 상태이며 유효한 Token을 가진 Authentication
- **When**: Authentication.isTokenValid() 호출
- **Then**: true 반환

#### 시나리오 1.5.2: 만료된 토큰 검증
- **Given**: 만료된 Token을 가진 Authentication
- **When**: Authentication.isTokenValid() 호출
- **Then**: false 반환

#### 시나리오 1.5.3: 토큰이 없는 상태에서 검증
- **Given**: Token이 없는 Authentication
- **When**: Authentication.isTokenValid() 호출
- **Then**: false 반환

## 2. Domain Events 테스트 시나리오

### 2.1 AuthenticationAttempted 이벤트

#### 시나리오 2.1.1: 이벤트 생성 및 데이터 검증
- **Given**: Authentication ID와 Credentials
- **When**: AuthenticationAttempted 이벤트 생성
- **Then**: 
  - 이벤트 ID가 생성됨
  - Authentication ID가 포함됨
  - 시도 시간이 현재 시간으로 설정됨
  - Username이 포함됨 (비밀번호는 제외)

### 2.2 AuthenticationSucceeded 이벤트

#### 시나리오 2.2.1: 성공 이벤트 생성
- **Given**: Authentication ID와 Token
- **When**: AuthenticationSucceeded 이벤트 생성
- **Then**:
  - Authentication ID가 포함됨
  - 성공 시간이 설정됨
  - Token 정보가 포함됨

### 2.3 AuthenticationFailed 이벤트

#### 시나리오 2.3.1: 실패 이벤트 생성
- **Given**: Authentication ID와 실패 사유
- **When**: AuthenticationFailed 이벤트 생성
- **Then**:
  - Authentication ID가 포함됨
  - 실패 시간이 설정됨
  - 실패 사유가 포함됨

### 2.4 TokenExpired 이벤트

#### 시나리오 2.4.1: 토큰 만료 이벤트 생성
- **Given**: Authentication ID와 Token
- **When**: TokenExpired 이벤트 생성
- **Then**:
  - Authentication ID가 포함됨
  - 만료 시간이 설정됨
  - 만료된 Token 정보가 포함됨

## 3. Domain Services 테스트 시나리오

### 3.1 AuthenticationDomainService

#### 시나리오 3.1.1: 인증 처리 서비스
- **Given**: 유효한 Credentials
- **When**: AuthenticationDomainService.authenticate(credentials) 호출
- **Then**: Authentication 객체가 반환됨

#### 시나리오 3.1.2: 무효한 자격증명 처리
- **Given**: 무효한 Credentials
- **When**: AuthenticationDomainService.authenticate(credentials) 호출
- **Then**: AuthenticationException 발생

### 3.2 JwtPolicy

#### 시나리오 3.2.1: JWT 정책 검증
- **Given**: Token과 정책 규칙
- **When**: JwtPolicy.validate(token) 호출
- **Then**: 정책 준수 여부 반환

#### 시나리오 3.2.2: 만료된 토큰 정책 검증
- **Given**: 만료된 Token
- **When**: JwtPolicy.validate(token) 호출
- **Then**: false 반환

### 3.3 SessionPolicy

#### 시나리오 3.3.1: 세션 정책 검증
- **Given**: Authentication과 세션 정책
- **When**: SessionPolicy.validateSession(authentication) 호출
- **Then**: 세션 유효성 검증 결과 반환

## 4. Aggregate 불변 규칙 테스트

### 4.1 상태 전이 불변 규칙

#### 시나리오 4.1.1: PENDING에서만 SUCCESS/FAILED로 전이 가능
- **Given**: 다양한 상태의 Authentication
- **When**: 상태 변경 시도
- **Then**: PENDING 상태에서만 SUCCESS/FAILED로 변경 가능

#### 시나리오 4.1.2: SUCCESS/FAILED 상태에서 다른 상태로 변경 불가
- **Given**: SUCCESS 또는 FAILED 상태의 Authentication
- **When**: 다른 상태로 변경 시도
- **Then**: IllegalStateException 발생

### 4.2 Token 관련 불변 규칙

#### 시나리오 4.2.1: SUCCESS 상태에서만 유효한 Token 보유
- **Given**: 다양한 상태의 Authentication
- **When**: Token 유효성 확인
- **Then**: SUCCESS 상태에서만 유효한 Token 보유

#### 시나리오 4.2.2: Token 없이 만료 처리 불가
- **Given**: Token이 없는 Authentication
- **When**: 토큰 만료 시도
- **Then**: IllegalStateException 발생

### 4.3 동시성 안전성

#### 시나리오 4.3.1: 동시 상태 변경 시도
- **Given**: PENDING 상태의 Authentication
- **When**: 동시에 성공/실패 처리 시도
- **Then**: 하나의 상태 변경만 성공, 나머지는 실패