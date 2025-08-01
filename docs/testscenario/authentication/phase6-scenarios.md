# Authentication 애그리거트 Phase 6: Query Side 테스트 시나리오

## 🎯 테스트 목표

Authentication 애그리거트의 Query Side 구현에 대한 포괄적인 테스트 시나리오를 정의합니다.

## 📋 테스트 대상 컴포넌트

1. **Query 객체**: GetAuthenticationQuery, GetTokenInfoQuery
2. **Use Case 인터페이스**: GetAuthenticationUseCase, GetTokenInfoUseCase
3. **Query Handler**: AuthenticationQueryHandler
4. **Response DTO**: AuthenticationResponse, TokenInfoResponse
5. **Projection**: AuthenticationProjection, TokenInfoProjection

## 🔍 GetAuthenticationQuery 테스트 시나리오

### 정상 케이스
- [ ] **시나리오 1**: 유효한 인증 ID로 Query 객체 생성 성공
- [ ] **시나리오 2**: 정적 팩토리 메서드를 통한 Query 생성 성공
- [ ] **시나리오 3**: Builder 패턴을 통한 Query 생성 성공

### 예외 케이스
- [ ] **시나리오 4**: null 인증 ID로 Query 생성 시 예외 발생
- [ ] **시나리오 5**: 빈 문자열 인증 ID로 Query 생성 시 예외 발생
- [ ] **시나리오 6**: 공백만 있는 인증 ID로 Query 생성 시 예외 발생

### 검증 케이스
- [ ] **시나리오 7**: equals/hashCode 올바른 동작 확인
- [ ] **시나리오 8**: toString 메서드 올바른 출력 확인
- [ ] **시나리오 9**: Getter 메서드 올바른 값 반환 확인

## 🔍 GetTokenInfoQuery 테스트 시나리오

### 정상 케이스
- [ ] **시나리오 10**: 유효한 토큰으로 Query 객체 생성 성공
- [ ] **시나리오 11**: JWT 형식 토큰으로 Query 생성 성공
- [ ] **시나리오 12**: 긴 토큰 문자열로 Query 생성 성공

### 예외 케이스  
- [ ] **시나리오 13**: null 토큰으로 Query 생성 시 예외 발생
- [ ] **시나리오 14**: 빈 문자열 토큰으로 Query 생성 시 예외 발생
- [ ] **시나리오 15**: 공백만 있는 토큰으로 Query 생성 시 예외 발생

### 검증 케이스
- [ ] **시나리오 16**: equals/hashCode 올바른 동작 확인
- [ ] **시나리오 17**: toString 메서드 올바른 출력 확인
- [ ] **시나리오 18**: Getter 메서드 올바른 값 반환 확인

## 🏗️ AuthenticationQueryHandler 테스트 시나리오

### GetAuthentication Use Case 테스트

#### 정상 케이스
- [ ] **시나리오 19**: 존재하는 인증 ID로 조회 시 Authentication 정보 반환
- [ ] **시나리오 20**: PENDING 상태 인증 조회 시 올바른 응답 반환
- [ ] **시나리오 21**: SUCCESS 상태 인증 조회 시 토큰 정보 포함하여 반환
- [ ] **시나리오 22**: FAILED 상태 인증 조회 시 실패 이유 포함하여 반환

#### 예외 케이스
- [ ] **시나리오 23**: 존재하지 않는 인증 ID 조회 시 AuthenticationNotFoundException 발생
- [ ] **시나리오 24**: null Query 입력 시 ValidationException 발생
- [ ] **시나리오 25**: 데이터베이스 연결 실패 시 적절한 예외 처리

#### 상호작용 검증
- [ ] **시나리오 26**: LoadAuthenticationQueryPort 올바른 파라미터로 호출 확인
- [ ] **시나리오 27**: Projection → Response 매핑 올바르게 수행 확인
- [ ] **시나리오 28**: 트랜잭션 읽기 전용으로 실행 확인

### GetTokenInfo Use Case 테스트

#### 정상 케이스
- [ ] **시나리오 29**: 유효한 토큰으로 조회 시 토큰 정보 반환
- [ ] **시나리오 30**: 만료되지 않은 토큰 조회 시 isValid=true 반환
- [ ] **시나리오 31**: 만료된 토큰 조회 시 isValid=false 반환
- [ ] **시나리오 32**: 토큰 만료 시간 정보 올바르게 반환

#### 예외 케이스
- [ ] **시나리오 33**: 존재하지 않는 토큰 조회 시 TokenNotFoundException 발생
- [ ] **시나리오 34**: 손상된 토큰 조회 시 InvalidTokenException 발생
- [ ] **시나리오 35**: null Query 입력 시 ValidationException 발생

#### 상호작용 검증
- [ ] **시나리오 36**: LoadTokenInfoQueryPort 올바른 파라미터로 호출 확인
- [ ] **시나리오 37**: 토큰 유효성 검증 로직 올바르게 수행 확인
- [ ] **시나리오 38**: 응답 매핑 올바르게 수행 확인

## 📊 Projection 테스트 시나리오

### AuthenticationProjection 테스트
- [ ] **시나리오 39**: 모든 필드가 올바르게 설정된 Projection 생성
- [ ] **시나리오 40**: Builder 패턴을 통한 Projection 생성 성공
- [ ] **시나리오 41**: null 필드 처리 확인
- [ ] **시나리오 42**: 날짜 필드 올바른 형식으로 설정 확인

### TokenInfoProjection 테스트
- [ ] **시나리오 43**: 토큰 유효성 상태 올바르게 설정 확인
- [ ] **시나리오 44**: 만료 시간 올바르게 설정 확인
- [ ] **시나리오 45**: 갱신 가능 여부 올바르게 계산 확인
- [ ] **시나리오 46**: null 토큰 처리 확인

## 📨 Response DTO 테스트 시나리오

### AuthenticationResponse 테스트
- [ ] **시나리오 47**: SUCCESS 상태 응답 생성 확인
- [ ] **시나리오 48**: FAILED 상태 응답 생성 확인
- [ ] **시나리오 49**: PENDING 상태 응답 생성 확인
- [ ] **시나리오 50**: 토큰 정보 포함 응답 생성 확인
- [ ] **시나리오 51**: 실패 이유 포함 응답 생성 확인

### TokenInfoResponse 테스트
- [ ] **시나리오 52**: 유효한 토큰 응답 생성 확인
- [ ] **시나리오 53**: 만료된 토큰 응답 생성 확인
- [ ] **시나리오 54**: 토큰 메타데이터 포함 응답 생성 확인
- [ ] **시나리오 55**: 갱신 가능 토큰 응답 생성 확인

## 🔗 통합 테스트 시나리오

### 전체 플로우 테스트
- [ ] **시나리오 56**: 인증 생성 → 조회 전체 플로우 테스트
- [ ] **시나리오 57**: 토큰 발급 → 토큰 정보 조회 전체 플로우 테스트
- [ ] **시나리오 58**: 인증 실패 → 실패 정보 조회 전체 플로우 테스트
- [ ] **시나리오 59**: 토큰 만료 → 만료 토큰 조회 전체 플로우 테스트

### 동시성 테스트
- [ ] **시나리오 60**: 동시 조회 요청 처리 확인
- [ ] **시나리오 61**: 조회 중 인증 상태 변경 시 일관성 확인

## ⚡ 성능 테스트 시나리오

### 응답 시간 테스트
- [ ] **시나리오 62**: 단일 인증 조회 응답 시간 100ms 이하 확인
- [ ] **시나리오 63**: 토큰 정보 조회 응답 시간 50ms 이하 확인
- [ ] **시나리오 64**: 대량 조회 요청 시 성능 저하 없음 확인

### 메모리 사용량 테스트
- [ ] **시나리오 65**: 조회 결과 캐싱 메모리 사용량 적정선 유지 확인
- [ ] **시나리오 66**: 대용량 응답 데이터 메모리 누수 없음 확인

## 📝 테스트 데이터 준비

### 테스트용 Authentication 데이터
```java
// PENDING 상태 인증
Authentication pendingAuth = Authentication.attemptAuthentication(
    Credentials.of("testuser", "password123")
);

// SUCCESS 상태 인증  
Authentication successAuth = Authentication.attemptAuthentication(
    Credentials.of("testuser", "password123")
);
successAuth.markAsSuccessful(
    Token.of("access-token", "refresh-token", 3600)
);

// FAILED 상태 인증
Authentication failedAuth = Authentication.attemptAuthentication(
    Credentials.of("testuser", "wrongpassword")
);
failedAuth.markAsFailed("Invalid credentials");
```

### 테스트용 Token 데이터
```java
// 유효한 토큰
Token validToken = Token.of("valid-access-token", "valid-refresh-token", 3600);

// 만료된 토큰
Token expiredToken = Token.of("expired-access-token", "expired-refresh-token", 1);
Thread.sleep(1000); // 1초 대기하여 만료시킴
```

## ✅ 테스트 완료 기준

- [ ] 모든 시나리오 테스트 케이스 작성 완료
- [ ] 테스트 커버리지 85% 이상 달성
- [ ] 모든 테스트 케이스 통과
- [ ] 성능 기준 충족
- [ ] 예외 처리 완전성 확인
- [ ] 코딩 표준 준수 확인

## 📊 예상 테스트 케이스 수량

- **Query 객체 테스트**: 18개
- **Query Handler 테스트**: 20개  
- **Projection 테스트**: 8개
- **Response DTO 테스트**: 9개
- **통합 테스트**: 6개
- **성능 테스트**: 5개

**총 예상 테스트 케이스**: 66개