# AuthenticationSession 애그리거트 Phase 4 테스트 시나리오

## 1. Domain Events 테스트 시나리오

### 1.1 AccountLocked 이벤트 테스트 시나리오

#### 정상 시나리오
1. **유효한 매개변수로 AccountLocked 이벤트 생성**
   - Given: 유효한 sessionId, userId, clientIp, lockedUntil, failedAttemptCount, occurredAt
   - When: AccountLocked.of() 메서드 호출
   - Then: 이벤트가 성공적으로 생성됨

2. **이벤트 타입 확인**
   - Given: 생성된 AccountLocked 이벤트
   - When: eventType() 메서드 호출
   - Then: "AccountLocked" 반환

3. **애그리거트 ID 확인**
   - Given: 생성된 AccountLocked 이벤트
   - When: aggregateId() 메서드 호출
   - Then: sessionId와 동일한 값 반환

4. **이벤트 속성 접근자 테스트**
   - Given: 생성된 AccountLocked 이벤트
   - When: 각 속성 접근자 메서드 호출
   - Then: 생성 시 전달한 값들과 정확히 일치

#### 예외 시나리오
5. **null sessionId로 이벤트 생성 시도**
   - Given: sessionId가 null인 매개변수
   - When: AccountLocked.of() 메서드 호출
   - Then: IllegalArgumentException 발생 ("Session ID cannot be null or empty")

6. **빈 문자열 sessionId로 이벤트 생성 시도**
   - Given: sessionId가 빈 문자열인 매개변수
   - When: AccountLocked.of() 메서드 호출
   - Then: IllegalArgumentException 발생

7. **null userId로 이벤트 생성 시도**
   - Given: userId가 null인 매개변수
   - When: AccountLocked.of() 메서드 호출
   - Then: IllegalArgumentException 발생 ("User ID cannot be null or empty")

8. **null clientIp로 이벤트 생성 시도**
   - Given: clientIp가 null인 매개변수
   - When: AccountLocked.of() 메서드 호출
   - Then: IllegalArgumentException 발생 ("Client IP cannot be null or empty")

9. **null lockedUntil로 이벤트 생성 시도**
   - Given: lockedUntil이 null인 매개변수
   - When: AccountLocked.of() 메서드 호출
   - Then: IllegalArgumentException 발생 ("Locked until time cannot be null")

10. **null occurredAt로 이벤트 생성 시도**
    - Given: occurredAt이 null인 매개변수
    - When: AccountLocked.of() 메서드 호출
    - Then: IllegalArgumentException 발생 ("Occurred time cannot be null")

11. **음수 failedAttemptCount로 이벤트 생성 시도**
    - Given: failedAttemptCount가 0 이하인 매개변수
    - When: AccountLocked.of() 메서드 호출
    - Then: IllegalArgumentException 발생 ("Failed attempt count must be positive")

12. **lockedUntil이 occurredAt보다 이전 시점인 경우**
    - Given: lockedUntil이 occurredAt보다 이전인 매개변수
    - When: AccountLocked.of() 메서드 호출
    - Then: IllegalArgumentException 발생 ("Locked until time must be in the future")

#### 동등성 및 직렬화 테스트
13. **같은 데이터로 생성된 이벤트 동등성 테스트**
    - Given: 동일한 데이터로 생성된 두 AccountLocked 이벤트
    - When: equals() 메서드 호출
    - Then: true 반환

14. **다른 데이터로 생성된 이벤트 동등성 테스트**
    - Given: 다른 데이터로 생성된 두 AccountLocked 이벤트
    - When: equals() 메서드 호출
    - Then: false 반환

15. **JSON 직렬화/역직렬화 테스트**
    - Given: 생성된 AccountLocked 이벤트
    - When: JSON으로 직렬화 후 역직렬화
    - Then: 원본 이벤트와 동일한 데이터 유지

## 2. Domain Services 테스트 시나리오
- Domain Services 없음 (구현 계획서에 따라)

## 3. 통합 테스트 시나리오

### 3.1 Aggregate에서 이벤트 발행 테스트
16. **AuthenticationSession에서 AccountLocked 이벤트 발행**
    - Given: 실패 횟수가 임계치에 도달한 AuthenticationSession
    - When: recordFailedAttempt() 메서드 호출
    - Then: AccountLocked 이벤트가 도메인 이벤트 목록에 추가됨

17. **이벤트 발행 후 애그리거트 상태 확인**
    - Given: AccountLocked 이벤트를 발행한 AuthenticationSession
    - When: 애그리거트 상태 조회
    - Then: 잠금 상태가 정확히 설정됨

## 4. 예상 테스트 케이스 수
- AccountLocked 이벤트: 15개 (정상 4개 + 예외 8개 + 동등성/직렬화 3개)
- 통합 테스트: 2개
- **총 17개 테스트 케이스**

## 5. 테스트 커버리지 목표
- AccountLocked 이벤트: 100%
- 전체 도메인 레이어: 95% 이상 유지