# AuthenticationSession 애그리거트 Phase 4 구현 계획

## 1. 구현 목표
AuthenticationSession 애그리거트의 Domain Events와 Domain Services를 구현합니다.

## 2. 구현 범위
- **Domain Events**: AccountLocked (이미 Phase 3에서 구현됨)
- **Domain Services**: 없음 (구현 계획서에 따라)

## 3. 컴포넌트 목록
### Domain Events
- [x] **AccountLocked**: 계정 잠금 시 발생하는 도메인 이벤트
  - 이미 구현 완료: `event.com.ldx.hexacore.security.session.domain.AccountLocked`
  - 속성: sessionId, userId, clientIp, lockedUntil, failedAttemptCount
  - 검증 규칙: 모든 필수 필드 검증, 시간 순서 검증

### Domain Services
- 없음 (구현 계획서에 Domain Services 없음으로 명시)

## 4. 완료 기준
- [x] AccountLocked 이벤트 구현 (Phase 3에서 완료)
- [x] 테스트 코드 작성 및 통과 (Phase 3에서 완료)
- [ ] 도메인 레이어 명세서 작성
- [ ] tracker.md 업데이트

## 5. 특이사항
- Domain Events는 Phase 3에서 이미 완전히 구현됨
- Domain Services는 구현 계획서에 "없음"으로 명시되어 있음
- Phase 4는 실질적으로 문서화 작업이 주요 업무