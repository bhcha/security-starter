# AuthenticationSession Aggregate Phase 3 구현 계획서

## 🎯 구현 목표

AuthenticationSession 애그리거트의 Entity와 Aggregate Root를 구현합니다.

## 📋 구현 범위

### Entity 구현
- **AuthenticationAttempt**: 인증 시도 기록을 나타내는 Entity

### Aggregate Root 구현  
- **AuthenticationSession**: 세션별 인증 시도 관리 및 계정 잠금 정책 적용

### Domain Event 구현
- **AccountLocked**: 계정 잠금 시 발생하는 도메인 이벤트

## 📊 컴포넌트 상세 설계

### 1. AuthenticationAttempt Entity

#### 속성
- `attemptId`: 시도 식별자 (Long, Identity)
- `userId`: 사용자 식별자 (String)
- `attemptedAt`: 시도 시각 (LocalDateTime)
- `isSuccessful`: 성공/실패 여부 (boolean)
- `clientIp`: 클라이언트 IP (ClientIp VO)
- `riskLevel`: 위험도 (RiskLevel VO)

#### 비즈니스 메서드
- `isWithinTimeWindow(LocalDateTime windowStart)`: 지정된 시간 윈도우 내 시도인지 확인
- `isFromSameSource(ClientIp ip)`: 동일한 소스에서의 시도인지 확인
- `calculateRiskScore()`: 시도의 위험 점수 계산

### 2. AuthenticationSession Aggregate Root

#### 속성
- `sessionId`: 세션 식별자 (SessionId VO)
- `userId`: 사용자 식별자 (String)
- `clientIp`: 클라이언트 IP (ClientIp VO)
- `attempts`: 인증 시도 목록 (List<AuthenticationAttempt>)
- `isLocked`: 잠금 상태 (boolean)
- `lockedUntil`: 잠금 해제 시각 (LocalDateTime)
- `createdAt`: 생성 시각 (LocalDateTime)
- `lastActivityAt`: 마지막 활동 시각 (LocalDateTime)

#### 정책 상수
- `MAX_FAILED_ATTEMPTS`: 최대 실패 허용 횟수 (5회)
- `LOCKOUT_DURATION_MINUTES`: 잠금 지속 시간 (30분)
- `TIME_WINDOW_MINUTES`: 실패 집계 시간 윈도우 (15분)

#### 비즈니스 메서드
- `recordAttempt(userId, clientIp, isSuccessful, riskLevel)`: 인증 시도 기록
- `shouldLockAccount()`: 계정 잠금 필요 여부 판단
- `lockAccount()`: 계정 잠금 실행
- `unlockAccount()`: 계정 잠금 해제
- `isCurrentlyLocked()`: 현재 잠금 상태 확인
- `getFailedAttemptsInWindow()`: 시간 윈도우 내 실패 횟수 조회
- `updateLastActivity()`: 마지막 활동 시각 갱신

#### 불변 규칙 (Invariants)
1. 세션 ID는 null이 될 수 없음
2. 사용자 ID는 null이거나 빈 문자열이 될 수 없음
3. 잠금 상태일 때 `lockedUntil`은 미래 시각이어야 함
4. 시도 목록은 시간 순으로 정렬되어야 함
5. 성공한 시도 후에는 실패 카운터가 리셋되어야 함

### 3. AccountLocked Domain Event

#### 속성
- `sessionId`: 세션 식별자 (String)
- `userId`: 사용자 식별자 (String)
- `clientIp`: 클라이언트 IP (String)
- `lockedUntil`: 잠금 해제 시각 (LocalDateTime)
- `failedAttemptCount`: 실패 시도 횟수 (int)
- `occurredAt`: 이벤트 발생 시각 (LocalDateTime)

## ✅ 완료 기준

- [ ] AuthenticationAttempt Entity 구현 완료
- [ ] AuthenticationSession Aggregate Root 구현 완료  
- [ ] AccountLocked Domain Event 구현 완료
- [ ] 모든 비즈니스 규칙 및 불변 조건 구현
- [ ] 단위 테스트 작성 및 통과 (최소 15개 테스트 케이스)
- [ ] 테스트 커버리지 85% 이상
- [ ] 코딩 표준 준수 확인

## 📝 구현 순서

1. AuthenticationAttempt Entity 구현
2. AccountLocked Domain Event 구현
3. AuthenticationSession Aggregate Root 구현
4. 통합 테스트 작성
5. 불변 규칙 검증 테스트 작성