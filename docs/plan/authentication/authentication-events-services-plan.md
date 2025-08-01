# Authentication Aggregate - Phase 4: Events & Services 구현 계획서

## 목표
`/docs/plan/implementation-plan.md`에 정의된 Authentication 애그리거트의 Domain Event와 Domain Service를 구현합니다.

## 구현 범위

### Domain Events (구현 대상)
1. **AuthenticationAttempted**
   - 발생 조건: 인증 시도 시
   - 포함 데이터: username, clientIp, timestamp, attemptId

2. **AuthenticationSucceeded**
   - 발생 조건: 인증 성공 시
   - 포함 데이터: username, clientIp, timestamp, token, sessionId

3. **AuthenticationFailed**
   - 발생 조건: 인증 실패 시
   - 포함 데이터: username, clientIp, timestamp, reason, attemptCount

4. **TokenExpired**
   - 발생 조건: 토큰 만료 시
   - 포함 데이터: username, tokenId, expirationTime, timestamp

### Domain Services (구현 대상)
1. **AuthenticationDomainService**
   - 책임: 인증 처리 및 비즈니스 로직 수행
   - 메서드: authenticate(), validateCredentials(), generateAuthenticationId()

2. **JwtPolicy**
   - 책임: JWT 토큰 정책 관리
   - 메서드: validateTokenStructure(), isTokenExpired(), extractClaims()

3. **SessionPolicy**
   - 책임: 세션 정책 관리
   - 메서드: generateSessionId(), validateSessionTimeout(), isSessionValid()

## 완료 기준
- [ ] 모든 Domain Event 구현 완료
- [ ] 모든 Domain Service 구현 완료
- [ ] 테스트 커버리지 80% 이상 달성
- [ ] 모든 테스트 케이스 통과
- [ ] 코딩 표준 준수 확인
- [ ] Authentication 도메인 레이어 명세서 작성

## 구현 특이사항
- Domain Event는 불변 객체로 구현
- 모든 이벤트는 발생 시간(timestamp) 포함
- Domain Service는 상태를 갖지 않는 무상태 서비스로 구현
- 정책 객체들은 검증 로직에 집중