# Authentication Aggregate Phase 7 테스트 시나리오

## 개요
Authentication 애그리거트의 Inbound Adapter 구현을 위한 테스트 시나리오입니다.
JWT Authentication Filter와 Event Listener의 모든 기능을 검증합니다.

## 테스트 환경 설정
- Spring Boot Test
- Spring Security Test
- MockBean (의존성 모킹)
- TestRestTemplate (통합 테스트)

---

## 1. JWT Authentication Filter 테스트 시나리오

### 1.1 토큰 추출 테스트

#### 시나리오 1-1: Authorization 헤더에서 Bearer 토큰 추출 성공
- **Given**: Authorization 헤더에 "Bearer valid-jwt-token"
- **When**: 보호된 엔드포인트 요청
- **Then**: 
  - 토큰 성공적으로 추출
  - 토큰 검증 프로세스 진행

#### 시나리오 1-2: Authorization 헤더 없음
- **Given**: Authorization 헤더가 없는 요청
- **When**: 보호된 엔드포인트 요청
- **Then**: 
  - 필터 체인 계속 진행 (인증 없이)
  - 보호된 리소스는 접근 거부

#### 시나리오 1-3: Bearer 접두사 없는 토큰
- **Given**: Authorization 헤더에 "valid-jwt-token" (Bearer 없음)
- **When**: 보호된 엔드포인트 요청
- **Then**: 
  - 토큰 형식 오류로 처리
  - 401 Unauthorized 응답

#### 시나리오 1-4: 빈 Bearer 토큰
- **Given**: Authorization 헤더에 "Bearer " (토큰 없음)
- **When**: 보호된 엔드포인트 요청
- **Then**: 
  - 토큰 없음으로 처리
  - 401 Unauthorized 응답

---

### 1.2 토큰 검증 테스트

#### 시나리오 2-1: 유효한 토큰 검증 성공
- **Given**: 유효한 JWT 토큰
- **When**: ValidateTokenUseCase 호출
- **Then**: 
  - 토큰 검증 성공
  - SecurityContext에 Authentication 설정
  - 요청 처리 계속

#### 시나리오 2-2: 만료된 토큰 검증
- **Given**: 만료된 JWT 토큰
- **When**: ValidateTokenUseCase 호출
- **Then**: 
  - 토큰 검증 실패
  - 401 Unauthorized 응답
  - 적절한 에러 메시지

#### 시나리오 2-3: 서명이 잘못된 토큰
- **Given**: 서명이 유효하지 않은 JWT 토큰
- **When**: ValidateTokenUseCase 호출
- **Then**: 
  - 토큰 검증 실패
  - 401 Unauthorized 응답
  - 보안 이벤트 로깅

#### 시나리오 2-4: 형식이 잘못된 토큰
- **Given**: JWT 형식이 아닌 문자열
- **When**: ValidateTokenUseCase 호출
- **Then**: 
  - 토큰 파싱 실패
  - 400 Bad Request 응답
  - 에러 상세 정보 제공

---

### 1.3 Spring Security Context 설정 테스트

#### 시나리오 3-1: 인증 정보 설정 성공
- **Given**: 유효한 토큰으로 검증 완료
- **When**: SecurityContext 설정
- **Then**: 
  - Authentication 객체 생성
  - SecurityContextHolder에 설정
  - 후속 필터에서 인증 정보 사용 가능

#### 시나리오 3-2: 인증 정보 전파 확인
- **Given**: SecurityContext에 인증 정보 설정됨
- **When**: 컨트롤러 메서드 호출
- **Then**: 
  - @AuthenticationPrincipal로 사용자 정보 접근 가능
  - 권한 기반 접근 제어 동작

#### 시나리오 3-3: 요청 완료 후 Context 정리
- **Given**: 요청 처리 완료
- **When**: 필터 체인 종료
- **Then**: 
  - SecurityContext 초기화
  - 메모리 누수 방지

---

### 1.4 제외 경로 테스트

#### 시나리오 4-1: 제외 경로 요청
- **Given**: /actuator/health 요청
- **When**: 필터 체인 실행
- **Then**: 
  - JWT 필터 건너뛰기
  - 인증 없이 접근 허용

#### 시나리오 4-2: 커스텀 제외 경로
- **Given**: 설정된 제외 경로 요청
- **When**: 필터 체인 실행
- **Then**: 
  - JWT 필터 건너뛰기
  - 정상 응답

---

## 2. Event Listener 테스트 시나리오

### 2.1 외부 인증 이벤트 수신 처리

#### 시나리오 6-1: 외부 시스템 인증 성공 이벤트 수신
- **Given**: 외부 시스템에서 인증 성공 이벤트 발생
- **When**: 이벤트 리스너가 이벤트 수신
- **Then**: 
  - 이벤트 데이터 파싱 성공
  - Application Layer로 적절한 Command 전달
  - 처리 결과 로깅

#### 시나리오 6-2: 외부 시스템 인증 실패 이벤트 수신
- **Given**: 외부 시스템에서 인증 실패 이벤트 발생
- **When**: 이벤트 리스너가 이벤트 수신
- **Then**: 
  - 실패 이벤트 적절히 처리
  - 실패 원인 분석 및 기록
  - 필요시 알림 발송

#### 시나리오 6-3: 잘못된 형식의 이벤트 수신
- **Given**: 잘못된 형식의 이벤트 메시지
- **When**: 이벤트 리스너가 이벤트 수신
- **Then**: 
  - 파싱 오류 감지
  - 오류 로깅 및 데드레터 큐 전송
  - 시스템 안정성 유지

#### 시나리오 6-4: 이벤트 처리 중 예외 발생
- **Given**: 이벤트 처리 중 시스템 오류 발생
- **When**: 이벤트 리스너에서 예외 발생
- **Then**: 
  - 예외 처리 및 로깅
  - 재시도 메커니즘 동작
  - 최대 재시도 후 데드레터 큐 전송

---

## 3. 보안 테스트 시나리오

### 3.1 입력값 보안 검증

#### 시나리오 7-1: SQL Injection 시도
- **Given**: SQL Injection 공격 패턴이 포함된 입력값
- **When**: API 요청 전송
- **Then**: 
  - 입력값 검증으로 차단
  - 안전한 처리 확인
  - 공격 시도 로깅

#### 시나리오 7-2: XSS 공격 시도
- **Given**: XSS 스크립트가 포함된 입력값
- **When**: API 요청 전송
- **Then**: 
  - 입력값 sanitization 확인
  - 안전한 응답 생성
  - 스크립트 실행 방지

#### 시나리오 7-3: 과도하게 긴 입력값
- **Given**: 매우 긴 문자열 입력값
- **When**: API 요청 전송
- **Then**: 
  - 길이 제한 검증 동작
  - 적절한 오류 응답
  - 시스템 안정성 유지

---

## 4. 성능 테스트 시나리오

### 4.1 응답 시간 테스트

#### 시나리오 8-1: 인증 API 응답 시간 측정
- **Given**: 정상적인 인증 요청
- **When**: 1000회 연속 요청 실행
- **Then**: 
  - 평균 응답 시간 500ms 이하
  - 95% 요청이 1초 이하
  - 메모리 누수 없음

#### 시나리오 8-2: 동시 요청 처리 능력
- **Given**: 100개의 동시 인증 요청
- **When**: 동시 요청 실행
- **Then**: 
  - 모든 요청 정상 처리
  - 데이터 정합성 유지
  - 에러율 1% 이하

---

## 5. 통합 테스트 시나리오

### 5.1 전체 플로우 테스트

#### 시나리오 9-1: 인증부터 토큰 검증까지 전체 플로우
- **Given**: 전체 시스템이 구동된 상태
- **When**: 인증 → 토큰 검증 → 토큰 갱신 순차 실행
- **Then**: 
  - 모든 단계 정상 완료
  - 데이터 일관성 유지
  - 이벤트 발행 확인

#### 시나리오 9-2: 실패 시나리오 전체 플로우
- **Given**: 전체 시스템이 구동된 상태
- **When**: 잘못된 인증 → 토큰 검증 실패 시나리오 실행
- **Then**: 
  - 적절한 오류 처리
  - 보안 이벤트 기록
  - 시스템 안정성 유지

---

## 6. API 문서화 테스트 시나리오

### 6.1 OpenAPI 문서 생성 검증

#### 시나리오 10-1: Swagger UI 접근 및 문서 확인
- **Given**: 애플리케이션이 구동된 상태
- **When**: Swagger UI에 접근
- **Then**: 
  - 모든 엔드포인트 문서화 확인
  - Request/Response 스키마 정의 확인
  - 예제 요청/응답 확인

#### 시나리오 10-2: API 스펙 JSON 유효성 검증
- **Given**: OpenAPI 스펙이 생성된 상태
- **When**: API 스펙 JSON 검증
- **Then**: 
  - 유효한 OpenAPI 3.0 스펙
  - 모든 엔드포인트 포함
  - 완전한 스키마 정의

---

## 테스트 데이터 준비

### 유효한 테스트 데이터
```json
{
  "validUser": {
    "username": "testuser",
    "password": "password123!"
  },
  "validToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "validRefreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 무효한 테스트 데이터
```json
{
  "invalidUser": {
    "username": "",
    "password": "short"
  },
  "expiredToken": "expired.jwt.token",
  "malformedToken": "invalid-token-format"
}
```

## 예상 테스트 케이스 수량
- **컨트롤러 테스트**: 25개
- **이벤트 리스너 테스트**: 10개  
- **보안 테스트**: 8개
- **성능 테스트**: 5개
- **통합 테스트**: 10개
- **API 문서 테스트**: 2개

**총 예상 테스트 케이스**: 60개