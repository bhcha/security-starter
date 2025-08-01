# AuthenticationSession Aggregate - Phase 3: Entities & Aggregates 테스트 시나리오

## 개요
AuthenticationSession 애그리거트의 Entity와 Aggregate Root에 대한 테스트 시나리오를 정의합니다.

## 테스트 대상
1. AuthenticationAttempt (Entity)
2. AuthenticationSession (Aggregate Root)
3. AccountLocked (Domain Event)

---

## 1. AuthenticationAttempt Entity 테스트 시나리오

### 1.1 생성 및 기본 기능 테스트

#### TC001: 성공적인 AuthenticationAttempt 생성
**목적**: 유효한 파라미터로 AuthenticationAttempt를 생성할 수 있다
**입력**:
- userId: "user123"
- attemptedAt: 현재 시간
- isSuccessful: true
- clientIp: ClientIp.of("192.168.1.100")
- riskLevel: RiskLevel.low("Normal login attempt")

**기대결과**:
- AuthenticationAttempt 객체 생성 성공
- 모든 getter 메서드가 입력값과 동일한 값 반환

#### TC002: 실패한 인증 시도 생성
**목적**: 실패한 인증 시도를 정확히 기록할 수 있다
**입력**: isSuccessful: false, 나머지는 TC001과 동일
**기대결과**: `isSuccessful() == false`

#### TC003: 높은 위험도 인증 시도 생성
**목적**: 다양한 위험도의 인증 시도를 생성할 수 있다
**입력**: riskLevel: RiskLevel.high("Suspicious activity")
**기대결과**: `getRiskLevel().isHigh() == true`

### 1.2 예외 시나리오

#### TC004: null userId로 생성 시도
**입력**: userId: null
**기대결과**: `IllegalArgumentException("User ID cannot be null or empty")`

#### TC005: 빈 userId로 생성 시도
**입력**: userId: ""
**기대결과**: `IllegalArgumentException("User ID cannot be null or empty")`

#### TC006: 공백만 있는 userId로 생성 시도
**입력**: userId: "   "
**기대결과**: `IllegalArgumentException("User ID cannot be null or empty")`

#### TC007: null attemptedAt으로 생성 시도
**입력**: attemptedAt: null
**기대결과**: `IllegalArgumentException("Attempted time cannot be null")`

#### TC008: null clientIp로 생성 시도
**입력**: clientIp: null
**기대결과**: `IllegalArgumentException("Client IP cannot be null")`

#### TC009: null riskLevel로 생성 시도
**입력**: riskLevel: null
**기대결과**: `IllegalArgumentException("Risk level cannot be null")`

### 1.3 비즈니스 로직 테스트

#### TC010: 시간 윈도우 내 시도 여부 확인 - 윈도우 내
**목적**: 특정 시간 윈도우 내의 시도인지 정확히 판단한다
**설정**:
- attemptTime: 현재 시간
- windowStart: 현재 시간 - 5분
**기대결과**: `isWithinTimeWindow(windowStart) == true`

#### TC011: 시간 윈도우 내 시도 여부 확인 - 윈도우 밖
**설정**:
- attemptTime: 현재 시간 - 10분
- windowStart: 현재 시간 - 5분
**기대결과**: `isWithinTimeWindow(windowStart) == false`

#### TC012: 시간 윈도우 시작과 동일한 시간
**설정**: attemptTime == windowStart
**기대결과**: `isWithinTimeWindow(windowStart) == true`

#### TC013: 시간 윈도우 확인 시 null 파라미터
**입력**: windowStart: null
**기대결과**: `IllegalArgumentException("Window start time cannot be null")`

#### TC014: 동일한 소스에서의 시도 여부 확인 - 동일한 IP
**설정**: 
- 생성 시 clientIp: "192.168.1.100"
- 비교 대상: ClientIp.of("192.168.1.100")
**기대결과**: `isFromSameSource(sameIp) == true`

#### TC015: 동일한 소스에서의 시도 여부 확인 - 다른 IP
**설정**:
- 생성 시 clientIp: "192.168.1.100"
- 비교 대상: ClientIp.of("192.168.1.200")
**기대결과**: `isFromSameSource(differentIp) == false`

#### TC016: 동일한 소스 확인 시 null 파라미터
**입력**: otherIp: null
**기대결과**: `IllegalArgumentException("Other IP cannot be null")`

#### TC017: 위험 점수 계산 - 성공한 시도
**목적**: 성공한 시도의 위험 점수가 기본 점수와 동일하다
**설정**: isSuccessful: true, riskLevel: RiskLevel.medium("Normal activity")
**기대결과**: `calculateRiskScore() == riskLevel.getScore()`

#### TC018: 위험 점수 계산 - 실패한 시도
**목적**: 실패한 시도는 페널티 점수가 추가된다
**설정**: isSuccessful: false, riskLevel: RiskLevel.medium("Failed login")
**기대결과**: `calculateRiskScore() > riskLevel.getScore()`

---

## 2. AuthenticationSession Aggregate Root 테스트 시나리오

### 2.1 생성 및 기본 기능 테스트

#### TC019: AuthenticationSession 생성
**목적**: 유효한 파라미터로 AuthenticationSession을 생성할 수 있다
**입력**:
- sessionId: SessionId.generate()
- userId: "user123"
**기대결과**:
- AuthenticationSession 객체 생성 성공
- `isLocked() == false`
- `getLockedUntil() == null`
- `getAttempts().isEmpty() == true`

#### TC020: 인증 시도 기록 - 성공한 시도
**목적**: 성공한 인증 시도를 기록할 수 있다
**입력**:
- userId: "user123"
- clientIp: ClientIp.of("192.168.1.100")
- isSuccessful: true
- riskLevel: RiskLevel.low("Normal login")
**기대결과**:
- `getAttempts().size() == 1`
- 기록된 시도의 모든 정보가 입력값과 일치

#### TC021: 인증 시도 기록 - 실패한 시도
**목적**: 실패한 인증 시도를 기록할 수 있다
**입력**: isSuccessful: false, 나머지는 TC020과 동일
**기대결과**:
- `getAttempts().size() == 1`
- `getAttempts().get(0).isSuccessful() == false`

#### TC022: 여러 인증 시도 기록
**목적**: 여러 번의 인증 시도를 순차적으로 기록할 수 있다
**설정**: 3번의 서로 다른 인증 시도 기록
**기대결과**:
- `getAttempts().size() == 3`
- 모든 시도가 시간 순서대로 기록됨

### 2.2 계정 잠금 로직 테스트

#### TC023: 계정 잠금 정책 - 5회 실패 시 잠금
**목적**: 15분 내 5회 실패 시 계정이 잠긴다
**설정**: 15분 내에 5회 연속 실패한 인증 시도 기록
**기대결과**:
- `isLocked() == true`
- `getLockedUntil()`이 현재 시간 + 30분
- AccountLocked 도메인 이벤트 발행

#### TC024: 계정 잠금 정책 - 시간 윈도우 밖 실패는 제외
**목적**: 15분 시간 윈도우 밖의 실패는 집계에서 제외된다
**설정**:
- 20분 전: 3회 실패
- 10분 전: 2회 실패 (총 5회지만 윈도우 내는 2회)
**기대결과**: `isLocked() == false`

#### TC025: 계정 잠금 정책 - 성공 시 실패 카운트 리셋
**목적**: 성공한 인증 시도 시 실패 카운트가 리셋된다
**설정**:
- 3회 실패
- 1회 성공
- 4회 실패 (리셋 후이므로 총 4회)
**기대결과**: `isLocked() == false`

#### TC026: 계정 잠금 해제 - 시간 경과 후
**목적**: 잠금 시간이 지나면 자동으로 잠금이 해제된다
**설정**:
- 계정 잠금 상태로 설정
- lockedUntil을 과거 시간으로 설정
**기대결과**: `isCurrentlyLocked() == false`

#### TC027: 잠긴 계정에 추가 시도 시 예외
**목적**: 잠긴 계정에 대한 추가 인증 시도는 거부된다
**설정**: 계정을 잠금 상태로 설정 후 인증 시도
**기대결과**: `IllegalStateException("Account is currently locked")`

#### TC028: 시간 윈도우 내 실패 횟수 집계
**목적**: 지정된 시간 윈도우 내의 실패 횟수를 정확히 집계한다
**설정**:
- 20분 전: 2회 실패 (윈도우 밖)
- 10분 전: 2회 실패 (윈도우 내)
- 5분 전: 1회 실패 (윈도우 내)
**기대결과**: `getFailedAttemptsInWindow() == 3`

### 2.3 도메인 이벤트 테스트

#### TC029: AccountLocked 이벤트 발행
**목적**: 계정 잠금 시 AccountLocked 이벤트가 발행된다
**설정**: 5회 실패로 계정 잠금 유발
**기대결과**:
- `getDomainEvents().size() == 1`
- 이벤트 타입이 AccountLocked
- 이벤트에 sessionId, userId, clientIp, lockedUntil, failedAttemptCount 포함

#### TC030: 이벤트 데이터 정확성 검증
**목적**: 발행된 이벤트의 데이터가 정확하다
**설정**: 계정 잠금 유발
**기대결과**:
- 이벤트의 sessionId가 세션의 ID와 일치
- 이벤트의 userId가 사용자 ID와 일치
- 이벤트의 failedAttemptCount == 5

### 2.4 예외 시나리오

#### TC031: null sessionId로 생성 시도
**입력**: sessionId: null
**기대결과**: `IllegalArgumentException("Session ID cannot be null")`

#### TC032: null userId로 생성 시도
**입력**: userId: null
**기대결과**: `IllegalArgumentException("User ID cannot be null or empty")`

#### TC033: 빈 userId로 생성 시도
**입력**: userId: ""
**기대결과**: `IllegalArgumentException("User ID cannot be null or empty")`

#### TC034: 인증 시도 기록 시 null 파라미터들
**테스트 케이스**:
- null userId: `IllegalArgumentException("User ID cannot be null or empty")`
- null clientIp: `IllegalArgumentException("Client IP cannot be null")`
- null riskLevel: `IllegalArgumentException("Risk level cannot be null")`

---

## 3. AccountLocked Domain Event 테스트 시나리오

### 3.1 이벤트 생성 테스트

#### TC035: AccountLocked 이벤트 생성
**목적**: 유효한 파라미터로 AccountLocked 이벤트를 생성할 수 있다
**입력**:
- sessionId: "550e8400-e29b-41d4-a716-446655440000"
- userId: "user123"
- clientIp: "192.168.1.100"
- lockedUntil: 현재 시간 + 30분
- failedAttemptCount: 5
- occurredAt: 현재 시간

**기대결과**:
- AccountLocked 객체 생성 성공
- 모든 getter가 입력값과 일치

#### TC036: 이벤트 JSON 직렬화/역직렬화
**목적**: 이벤트가 JSON으로 정확히 직렬화/역직렬화된다
**설정**: AccountLocked 이벤트 생성 후 JSON 변환
**기대결과**:
- JSON 직렬화 성공
- 역직렬화 후 모든 필드값이 원본과 동일

### 3.2 이벤트 검증 테스트

#### TC037: null sessionId로 이벤트 생성
**입력**: sessionId: null
**기대결과**: `IllegalArgumentException("Session ID cannot be null or empty")`

#### TC038: 빈 sessionId로 이벤트 생성
**입력**: sessionId: ""
**기대결과**: `IllegalArgumentException("Session ID cannot be null or empty")`

#### TC039: null userId로 이벤트 생성
**입력**: userId: null
**기대결과**: `IllegalArgumentException("User ID cannot be null or empty")`

#### TC040: null clientIp로 이벤트 생성
**입력**: clientIp: null
**기대결과**: `IllegalArgumentException("Client IP cannot be null or empty")`

#### TC041: null lockedUntil로 이벤트 생성
**입력**: lockedUntil: null
**기대결과**: `IllegalArgumentException("Locked until time cannot be null")`

#### TC042: 음수 failedAttemptCount로 이벤트 생성
**입력**: failedAttemptCount: -1
**기대결과**: `IllegalArgumentException("Failed attempt count must be positive")`

#### TC043: null occurredAt으로 이벤트 생성
**입력**: occurredAt: null
**기대결과**: `IllegalArgumentException("Occurred time cannot be null")`

---

## 통합 테스트 시나리오

### TC044: 완전한 계정 잠금 플로우 테스트
**목적**: 전체 계정 잠금 프로세스가 정상 동작한다
**시나리오**:
1. AuthenticationSession 생성
2. 4회 실패한 인증 시도 기록
3. 5번째 실패 시도로 계정 잠금 유발
4. AccountLocked 도메인 이벤트 확인
5. 추가 인증 시도 시 예외 발생 확인

**기대결과**: 모든 단계가 예상대로 동작

### TC045: 시간 윈도우 경계 테스트
**목적**: 15분 시간 윈도우 경계에서 정확히 동작한다
**시나리오**:
1. 정확히 15분 전에 실패 시도 기록
2. 현재 시간에 4회 추가 실패 시도
3. 계정 잠금 여부 확인

**기대결과**: 15분 전 시도는 제외되어 잠금되지 않음

---

## 테스트 완료 기준

1. **기능 커버리지**: 모든 public 메서드 100% 커버리지
2. **비즈니스 로직**: 계정 잠금 정책의 모든 경우의 수 검증
3. **도메인 이벤트**: 이벤트 발행 및 데이터 정확성 검증
4. **예외 처리**: 모든 입력 검증 로직 테스트
5. **통합 시나리오**: 실제 사용 흐름과 유사한 복합 시나리오 검증

## 품질 지표

- **테스트 케이스 수**: 45개
- **예상 커버리지**: 100%
- **비즈니스 로직 시나리오**: 계정 잠금 정책의 모든 경우
- **도메인 이벤트 검증**: 발행 조건 및 데이터 정확성
- **통합 테스트**: 실제 업무 흐름 시뮬레이션

## 정책 상수 검증

- **MAX_FAILED_ATTEMPTS**: 5회
- **LOCKOUT_DURATION_MINUTES**: 30분
- **TIME_WINDOW_MINUTES**: 15분

이 정책 상수들이 비즈니스 요구사항과 일치하는지 확인 필요