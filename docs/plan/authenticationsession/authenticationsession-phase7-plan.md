# AuthenticationSession Phase 7: Inbound Adapter 구현 계획

## 1. 구현 목표

AuthenticationSession 애그리거트의 Inbound Adapter를 구현하여 외부로부터의 이벤트를 수신하고 처리합니다.

## 2. 구현 범위

### Event Listener
- SessionEventListener: 세션 관련 이벤트 처리
  - Authentication 애그리거트로부터의 이벤트 수신
  - 인증 시도 기록 및 계정 잠금 상태 관리
  - 이벤트 타입별 처리 전략

### 주요 이벤트 처리
1. AuthenticationAttempted: 인증 시도 기록
2. AuthenticationSucceeded: 성공 시 리셋
3. AuthenticationFailed: 실패 집계 및 잠금 체크

## 3. 컴포넌트 목록

### Event Listener
1. **SessionEventListener**
   - 역할: Spring Event를 수신하여 Application Service 호출
   - 기능:
     - @EventListener/@TransactionalEventListener 활용
     - 이벤트 타입별 분기 처리
     - 적절한 Command 생성 및 Use Case 호출

### Configuration
2. **SessionEventConfiguration**
   - 역할: Event Listener 자동 설정
   - 기능:
     - @EnableEventProcessing 설정
     - @ConditionalOnProperty 조건부 활성화
     - 트랜잭션 전파 설정

## 4. 기술 스택
- Spring Events
- @TransactionalEventListener
- @ConditionalOnProperty
- Spring Boot Auto Configuration

## 5. 완료 기준
- [ ] SessionEventListener 구현 및 테스트
- [ ] 이벤트별 처리 로직 구현
- [ ] 트랜잭션 경계 설정
- [ ] 조건부 활성화 설정
- [ ] 통합 테스트 작성
- [ ] 모든 테스트 통과
- [ ] 테스트 커버리지 80% 이상