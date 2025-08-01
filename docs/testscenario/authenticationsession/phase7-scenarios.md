# AuthenticationSession Phase 7: Inbound Adapter 테스트 시나리오

## 1. SessionEventListener 테스트 시나리오

### 1.1 AuthenticationAttempted 이벤트 처리

#### 정상 케이스
1. **인증 시도 이벤트 수신 및 처리**
   - Given: AuthenticationAttempted 이벤트가 발생하고
   - When: EventListener가 이벤트를 수신하면
   - Then: RecordAttemptCommand를 생성하고 RecordAttemptUseCase를 호출한다

2. **유효한 사용자 ID와 IP로 인증 시도 기록**
   - Given: userId="user123", clientIp="192.168.1.1"인 이벤트가 발생하고
   - When: EventListener가 처리하면
   - Then: 해당 정보로 RecordAttemptCommand가 생성된다

3. **트랜잭션 커밋 후 이벤트 처리**
   - Given: @TransactionalEventListener(phase = AFTER_COMMIT) 설정이 적용되고
   - When: 트랜잭션이 커밋되면
   - Then: 이벤트 처리가 실행된다

#### 예외 케이스
4. **null 사용자 ID 처리**
   - Given: userId가 null인 이벤트가 발생하고
   - When: EventListener가 처리하려고 하면
   - Then: 로그를 기록하고 처리를 건너뛴다

5. **잘못된 IP 형식 처리**
   - Given: clientIp가 "invalid-ip"인 이벤트가 발생하고
   - When: EventListener가 처리하려고 하면
   - Then: 로그를 기록하고 처리를 건너뛴다

6. **UseCase 예외 발생 시 처리**
   - Given: RecordAttemptUseCase가 예외를 발생시키고
   - When: EventListener가 호출하면
   - Then: 예외를 로깅하고 정상적으로 종료한다

### 1.2 AuthenticationSucceeded 이벤트 처리

#### 정상 케이스
7. **인증 성공 이벤트 수신 및 처리**
   - Given: AuthenticationSucceeded 이벤트가 발생하고
   - When: EventListener가 이벤트를 수신하면
   - Then: RecordAttemptCommand를 생성하고 RecordAttemptUseCase를 호출한다

8. **성공 플래그가 true로 설정**
   - Given: 인증 성공 이벤트가 발생하고
   - When: RecordAttemptCommand를 생성하면
   - Then: success 필드가 true로 설정된다

9. **성공 시 실패 횟수 리셋**
   - Given: 사용자가 인증에 성공하고
   - When: 이벤트가 처리되면
   - Then: 해당 사용자의 실패 횟수가 리셋된다

### 1.3 AuthenticationFailed 이벤트 처리

#### 정상 케이스
10. **인증 실패 이벤트 수신 및 처리**
    - Given: AuthenticationFailed 이벤트가 발생하고
    - When: EventListener가 이벤트를 수신하면
    - Then: RecordAttemptCommand를 생성하고 RecordAttemptUseCase를 호출한다

11. **실패 플래그가 false로 설정**
    - Given: 인증 실패 이벤트가 발생하고
    - When: RecordAttemptCommand를 생성하면
    - Then: success 필드가 false로 설정된다

12. **실패 후 잠금 상태 확인**
    - Given: 인증 실패 이벤트가 처리되고
    - When: CheckLockoutUseCase를 호출하면
    - Then: 계정 잠금 상태를 확인한다

13. **5회 실패 시 계정 잠금**
    - Given: 동일 사용자가 5번째 실패하고
    - When: 이벤트가 처리되면
    - Then: AccountLocked 이벤트가 발생한다

### 1.4 AccountLocked 이벤트 처리

#### 정상 케이스
14. **계정 잠금 이벤트 수신**
    - Given: AccountLocked 이벤트가 발생하고
    - When: EventListener가 이벤트를 수신하면
    - Then: 잠금 정보를 로깅한다

15. **잠금 알림 발송**
    - Given: 계정이 잠겨지고
    - When: AccountLocked 이벤트가 처리되면
    - Then: 관리자에게 알림을 발송한다

## 2. SessionEventConfiguration 테스트 시나리오

### 2.1 자동 설정

#### 정상 케이스
16. **Event Listener Bean 등록**
    - Given: 스프링 컨텍스트가 로드되고
    - When: @Configuration이 적용되면
    - Then: SessionEventListener Bean이 등록된다

17. **조건부 활성화 - 활성화된 경우**
    - Given: hexacore.session.event.enabled=true로 설정되고
    - When: 애플리케이션이 시작되면
    - Then: SessionEventListener가 활성화된다

18. **조건부 활성화 - 비활성화된 경우**
    - Given: hexacore.session.event.enabled=false로 설정되고
    - When: 애플리케이션이 시작되면
    - Then: SessionEventListener가 등록되지 않는다

19. **기본값 적용**
    - Given: 설정이 없고
    - When: 애플리케이션이 시작되면
    - Then: 기본값(true)이 적용되어 활성화된다

### 2.2 트랜잭션 설정

#### 정상 케이스
20. **트랜잭션 커밋 후 이벤트 처리**
    - Given: @TransactionalEventListener가 적용되고
    - When: 트랜잭션이 커밋되면
    - Then: 이벤트가 처리된다

21. **트랜잭션 롤백 시 이벤트 미처리**
    - Given: @TransactionalEventListener가 적용되고
    - When: 트랜잭션이 롤백되면
    - Then: 이벤트가 처리되지 않는다

## 3. 통합 테스트 시나리오

### 3.1 End-to-End 테스트

#### 정상 케이스
22. **인증 성공 흐름**
    - Given: Authentication 애그리거트에서 AuthenticationSucceeded 이벤트가 발생하고
    - When: SessionEventListener가 수신하면
    - Then: 세션 정보가 저장되고 실패 횟수가 리셋된다

23. **인증 실패 및 잠금 흐름**
    - Given: 5번의 연속된 AuthenticationFailed 이벤트가 발생하고
    - When: SessionEventListener가 모두 처리하면
    - Then: 계정이 잠기고 AccountLocked 이벤트가 발생한다

24. **동시 이벤트 처리**
    - Given: 여러 사용자의 이벤트가 동시에 발생하고
    - When: SessionEventListener가 처리하면
    - Then: 모든 이벤트가 정상적으로 처리된다

25. **예외 복구 테스트**
    - Given: UseCase에서 간헐적 예외가 발생하고
    - When: EventListener가 재시도하면
    - Then: 결국 성공적으로 처리된다