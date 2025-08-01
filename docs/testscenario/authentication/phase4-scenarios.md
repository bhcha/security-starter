# Authentication Aggregate - Phase 4: Events & Services 테스트 시나리오

## Domain Events 테스트 시나리오

### 1. AuthenticationAttempted 이벤트
#### 정상 케이스
- **시나리오**: 유효한 인증 시도 이벤트 생성
- **Given**: username="testuser", clientIp="192.168.1.1", timestamp=현재시간, attemptId=UUID
- **When**: AuthenticationAttempted 이벤트 생성
- **Then**: 모든 필드가 올바르게 설정됨

#### 예외 케이스
- **시나리오**: null username으로 이벤트 생성 시도
- **Given**: username=null, 기타 유효한 값
- **When**: AuthenticationAttempted 이벤트 생성
- **Then**: IllegalArgumentException 발생

- **시나리오**: null clientIp로 이벤트 생성 시도
- **Given**: clientIp=null, 기타 유효한 값
- **When**: AuthenticationAttempted 이벤트 생성
- **Then**: IllegalArgumentException 발생

### 2. AuthenticationSucceeded 이벤트
#### 정상 케이스
- **시나리오**: 성공한 인증 이벤트 생성
- **Given**: username="testuser", clientIp="192.168.1.1", token=유효한토큰, sessionId=UUID
- **When**: AuthenticationSucceeded 이벤트 생성
- **Then**: 모든 필드가 올바르게 설정됨

#### 예외 케이스
- **시나리오**: null token으로 이벤트 생성 시도
- **Given**: token=null, 기타 유효한 값
- **When**: AuthenticationSucceeded 이벤트 생성
- **Then**: IllegalArgumentException 발생

### 3. AuthenticationFailed 이벤트
#### 정상 케이스
- **시나리오**: 실패한 인증 이벤트 생성
- **Given**: username="testuser", clientIp="192.168.1.1", reason="Invalid password", attemptCount=3
- **When**: AuthenticationFailed 이벤트 생성
- **Then**: 모든 필드가 올바르게 설정됨

#### 예외 케이스
- **시나리오**: 음수 attemptCount로 이벤트 생성 시도
- **Given**: attemptCount=-1, 기타 유효한 값
- **When**: AuthenticationFailed 이벤트 생성
- **Then**: IllegalArgumentException 발생

### 4. TokenExpired 이벤트
#### 정상 케이스
- **시나리오**: 토큰 만료 이벤트 생성
- **Given**: username="testuser", tokenId=UUID, expirationTime=과거시간
- **When**: TokenExpired 이벤트 생성
- **Then**: 모든 필드가 올바르게 설정됨

#### 예외 케이스
- **시나리오**: null tokenId로 이벤트 생성 시도
- **Given**: tokenId=null, 기타 유효한 값
- **When**: TokenExpired 이벤트 생성
- **Then**: IllegalArgumentException 발생

## Domain Services 테스트 시나리오

### 1. AuthenticationDomainService
#### authenticate() 메서드
##### 정상 케이스
- **시나리오**: 유효한 자격증명으로 인증
- **Given**: credentials=유효한자격증명, clientIp="192.168.1.1"
- **When**: authenticate() 호출
- **Then**: Authentication 객체 반환, AuthenticationAttempted 이벤트 발생

- **시나리오**: 유효한 자격증명으로 인증 성공
- **Given**: credentials=유효한자격증명, 외부 인증 성공
- **When**: authenticate() 호출
- **Then**: AuthenticationSucceeded 이벤트 발생, 토큰 포함

##### 예외 케이스
- **시나리오**: 잘못된 자격증명으로 인증 시도
- **Given**: credentials=잘못된자격증명
- **When**: authenticate() 호출
- **Then**: AuthenticationFailed 이벤트 발생, 실패 사유 포함

- **시나리오**: null 자격증명으로 인증 시도
- **Given**: credentials=null
- **When**: authenticate() 호출
- **Then**: IllegalArgumentException 발생

#### validateCredentials() 메서드
##### 정상 케이스
- **시나리오**: 유효한 자격증명 검증
- **Given**: credentials=유효한자격증명
- **When**: validateCredentials() 호출
- **Then**: true 반환

##### 예외 케이스
- **시나리오**: 잘못된 자격증명 검증
- **Given**: credentials=잘못된자격증명
- **When**: validateCredentials() 호출
- **Then**: false 반환

#### generateAuthenticationId() 메서드
##### 정상 케이스
- **시나리오**: 인증 ID 생성
- **Given**: 메서드 호출 준비
- **When**: generateAuthenticationId() 호출
- **Then**: 유효한 UUID 반환, 중복되지 않음

### 2. JwtPolicy
#### validateTokenStructure() 메서드
##### 정상 케이스
- **시나리오**: 유효한 JWT 토큰 구조 검증
- **Given**: token=유효한JWT토큰
- **When**: validateTokenStructure() 호출
- **Then**: true 반환

##### 예외 케이스
- **시나리오**: 잘못된 JWT 토큰 구조 검증
- **Given**: token=잘못된형식토큰
- **When**: validateTokenStructure() 호출
- **Then**: false 반환

- **시나리오**: null 토큰 검증
- **Given**: token=null
- **When**: validateTokenStructure() 호출
- **Then**: false 반환

#### isTokenExpired() 메서드
##### 정상 케이스
- **시나리오**: 만료되지 않은 토큰 검증
- **Given**: token=유효한토큰, 현재시간 < 만료시간
- **When**: isTokenExpired() 호출
- **Then**: false 반환

- **시나리오**: 만료된 토큰 검증
- **Given**: token=만료된토큰, 현재시간 > 만료시간
- **When**: isTokenExpired() 호출
- **Then**: true 반환

#### extractClaims() 메서드
##### 정상 케이스
- **시나리오**: 토큰에서 클레임 추출
- **Given**: token=유효한JWT토큰
- **When**: extractClaims() 호출
- **Then**: Map<String, Object> 반환, 예상 클레임 포함

##### 예외 케이스
- **시나리오**: 잘못된 토큰에서 클레임 추출 시도
- **Given**: token=잘못된토큰
- **When**: extractClaims() 호출
- **Then**: 빈 Map 반환 또는 예외 발생

### 3. SessionPolicy
#### generateSessionId() 메서드
##### 정상 케이스
- **시나리오**: 세션 ID 생성
- **Given**: 메서드 호출 준비
- **When**: generateSessionId() 호출
- **Then**: 유효한 세션 ID 반환, 중복되지 않음

#### validateSessionTimeout() 메서드
##### 정상 케이스
- **시나리오**: 세션 타임아웃 유효성 검증
- **Given**: sessionTimeout=30(분)
- **When**: validateSessionTimeout() 호출
- **Then**: true 반환

##### 예외 케이스
- **시나리오**: 음수 세션 타임아웃 검증
- **Given**: sessionTimeout=-10
- **When**: validateSessionTimeout() 호출
- **Then**: false 반환

- **시나리오**: 0 세션 타임아웃 검증
- **Given**: sessionTimeout=0
- **When**: validateSessionTimeout() 호출
- **Then**: false 반환

#### isSessionValid() 메서드
##### 정상 케이스
- **시나리오**: 유효한 세션 검증
- **Given**: sessionId=유효한세션ID, lastAccessTime=최근시간
- **When**: isSessionValid() 호출
- **Then**: true 반환

##### 예외 케이스
- **시나리오**: 만료된 세션 검증
- **Given**: sessionId=유효한세션ID, lastAccessTime=오래된시간
- **When**: isSessionValid() 호출
- **Then**: false 반환

- **시나리오**: null 세션 ID 검증
- **Given**: sessionId=null
- **When**: isSessionValid() 호출
- **Then**: false 반환

## 통합 시나리오

### 1. 완전한 인증 플로우
- **시나리오**: 전체 인증 과정 테스트
- **Given**: 유효한 자격증명과 정책들
- **When**: AuthenticationDomainService.authenticate() 호출
- **Then**: 
  1. AuthenticationAttempted 이벤트 발생
  2. JwtPolicy로 토큰 검증
  3. SessionPolicy로 세션 생성
  4. AuthenticationSucceeded 이벤트 발생
  5. 유효한 Authentication 객체 반환

### 2. 인증 실패 및 재시도 플로우
- **시나리오**: 인증 실패 후 재시도 과정
- **Given**: 잘못된 자격증명, 이후 올바른 자격증명
- **When**: 연속된 인증 시도
- **Then**:
  1. 첫 번째: AuthenticationFailed 이벤트 발생
  2. 두 번째: AuthenticationSucceeded 이벤트 발생