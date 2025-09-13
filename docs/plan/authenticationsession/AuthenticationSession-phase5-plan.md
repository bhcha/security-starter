# AuthenticationSession Aggregate - Phase 5 구현 계획서

## 1. 구현 목표
AuthenticationSession 애그리거트의 Application Layer Command Side를 구현합니다.

## 2. 구현 범위
`/docs/plan/implementation-plan.md`에 명시된 AuthenticationSession 애그리거트의 Application Layer Command Side 컴포넌트들을 구현합니다.

## 3. 구현 컴포넌트 목록

### 3.1 Commands
- **RecordAuthenticationAttemptCommand**: 인증 시도 기록 명령
- **UnlockAccountCommand**: 계정 잠금 해제 명령

### 3.2 Use Cases
- **RecordAttemptUseCase**: 인증 시도 기록 유스케이스
- **CheckLockoutUseCase**: 계정 잠금 상태 확인 유스케이스
- **UnlockAccountUseCase**: 계정 잠금 해제 유스케이스

### 3.3 Result Objects
- **RecordAttemptResult**: 인증 시도 기록 결과
- **LockoutCheckResult**: 계정 잠금 상태 확인 결과
- **UnlockAccountResult**: 계정 잠금 해제 결과

### 3.4 Outbound Ports
- **AuthenticationSessionRepository**: 세션 저장소 포트
- **SessionEventPublisher**: 세션 이벤트 발행 포트

### 3.5 Use Case 구현체
- **RecordAttemptUseCaseImpl**: 인증 시도 기록 유스케이스 구현체 
- **CheckLockoutUseCaseImpl**: 계정 잠금 상태 확인 유스케이스 구현체
- **UnlockAccountUseCaseImpl**: 계정 잠금 해제 유스케이스 구현체

### 3.6 예외 클래스
- **SessionNotFoundException**: 세션을 찾을 수 없는 경우
- **SessionValidationException**: 세션 검증 실패 시
- **UnlockNotAllowedException**: 잠금 해제가 허용되지 않는 경우

## 4. 구현 상세

### 4.1 Commands 구현 계획

#### RecordAuthenticationAttemptCommand
```java
public record RecordAuthenticationAttemptCommand(
    String sessionId,           // 세션 ID
    String userId,              // 사용자 ID  
    String clientIp,            // 클라이언트 IP
    boolean isSuccessful,       // 성공 여부
    int riskScore,              // 위험도 점수
    String riskReason           // 위험도 이유
) {
    // 검증 로직 구현 예정
}
```

#### UnlockAccountCommand
```java
public record UnlockAccountCommand(
    String sessionId,           // 세션 ID
    String userId               // 잠금 해제할 사용자 ID
) {
    // 검증 로직 구현 예정
}
```

### 4.2 Use Cases 구현 계획

#### RecordAttemptUseCase
- **책임**: 인증 시도를 세션에 기록하고 잠금 상태를 확인
- **흐름**: 
  1. 세션 조회
  2. 인증 시도 기록
  3. 계정 잠금 상태 확인
  4. 도메인 이벤트 발행
  5. 결과 반환

#### CheckLockoutUseCase
- **책임**: 특정 사용자의 계정 잠금 상태 확인
- **흐름**:
  1. 세션 조회
  2. 사용자 잠금 상태 확인
  3. 결과 반환

#### UnlockAccountUseCase
- **책임**: 계정 잠금을 명시적으로 해제
- **흐름**:
  1. 세션 조회
  2. 계정 잠금 해제
  3. 세션 저장
  4. 결과 반환

### 4.3 Outbound Ports 구현 계획

#### AuthenticationSessionRepository
```java
public interface AuthenticationSessionRepository {
    Optional<AuthenticationSession> findBySessionId(SessionId sessionId);
    AuthenticationSession save(AuthenticationSession session);
    void delete(SessionId sessionId);
}
```

#### SessionEventPublisher
```java
public interface SessionEventPublisher {
    void publish(DomainEvent event);
    void publishAll(List<DomainEvent> events);
}
```

## 5. 패키지 구조
```
com.ldx.hexacore.security.application.session/
├── command/
│   ├── RecordAuthenticationAttemptCommand.java
│   └── UnlockAccountCommand.java
├── usecase/
│   ├── RecordAttemptUseCase.java
│   ├── CheckLockoutUseCase.java
│   ├── UnlockAccountUseCase.java
│   ├── RecordAttemptUseCaseImpl.java
│   ├── CheckLockoutUseCaseImpl.java
│   └── UnlockAccountUseCaseImpl.java
├── result/
│   ├── RecordAttemptResult.java
│   ├── LockoutCheckResult.java
│   └── UnlockAccountResult.java
├── port/
│   ├── AuthenticationSessionRepository.java
│   └── SessionEventPublisher.java
└── exception/
    ├── SessionNotFoundException.java
    ├── SessionValidationException.java
    └── UnlockNotAllowedException.java
```

## 6. 참조 문서
- `/docs/spec/authenticationsession-domain-layer-spec.md` - 도메인 레이어 명세서
- `/docs/guide/2-coding-standards.md` - 코딩 표준
- `/docs/guide/3-2-application-tdd.md` - Application Layer TDD 가이드
- `/docs/guide/4-2-application-templates.md` - Application Layer 템플릿

## 7. 완료 기준
- [ ] 모든 Command 클래스 구현 완료
- [ ] 모든 Use Case 인터페이스 및 구현체 완료
- [ ] 모든 Result 클래스 구현 완료
- [ ] 모든 Outbound Port 인터페이스 구현 완료
- [ ] 예외 클래스 구현 완료
- [ ] 단위 테스트 작성 및 통과 (커버리지 80% 이상)
- [ ] 테스트 리뷰 완료
- [ ] 코딩 표준 리뷰 완료

## 8. 제약사항 및 주의사항
- Domain Layer의 AuthenticationSession Aggregate만 직접 참조 가능
- 도메인 레이어 명세서에 정의된 인터페이스만 사용
- CQRS 패턴 준수: Command Side는 상태 변경에만 집중
- 트랜잭션 처리는 Use Case 레벨에서 관리
- 도메인 이벤트는 트랜잭션 커밋 후 발행

## 9. 테스트 전략
- **단위 테스트**: 각 Use Case의 개별 동작 검증
- **통합 테스트**: Repository와 EventPublisher 연동 검증
- **예외 테스트**: 다양한 예외 상황 처리 검증
- **Mock 테스트**: 외부 의존성 격리 테스트