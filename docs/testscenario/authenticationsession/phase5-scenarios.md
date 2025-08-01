# AuthenticationSession Aggregate - Phase 5 테스트 시나리오

## 1. 테스트 시나리오 개요
AuthenticationSession 애그리거트의 Application Layer Command Side 컴포넌트들에 대한 테스트 시나리오를 정의합니다.

## 2. Commands 테스트 시나리오

### 2.1 RecordAuthenticationAttemptCommand 테스트 시나리오

#### 2.1.1 정상 케이스
**시나리오 1**: 유효한 성공적인 인증 시도 명령 생성
- **Given**: 유효한 세션 ID, 사용자 ID, 클라이언트 IP, 성공 상태, 낮은 위험도
- **When**: RecordAuthenticationAttemptCommand 생성
- **Then**: 명령 객체가 정상적으로 생성됨

**시나리오 2**: 유효한 실패한 인증 시도 명령 생성
- **Given**: 유효한 세션 ID, 사용자 ID, 클라이언트 IP, 실패 상태, 높은 위험도
- **When**: RecordAuthenticationAttemptCommand 생성
- **Then**: 명령 객체가 정상적으로 생성됨

#### 2.1.2 예외 케이스
**시나리오 3**: null 세션 ID로 명령 생성 시도
- **Given**: null 세션 ID
- **When**: RecordAuthenticationAttemptCommand 생성
- **Then**: IllegalArgumentException 발생

**시나리오 4**: 빈 사용자 ID로 명령 생성 시도
- **Given**: 빈 문자열 사용자 ID
- **When**: RecordAuthenticationAttemptCommand 생성
- **Then**: IllegalArgumentException 발생

**시나리오 5**: 유효하지 않은 IP 주소로 명령 생성 시도
- **Given**: 유효하지 않은 IP 주소 형식
- **When**: RecordAuthenticationAttemptCommand 생성
- **Then**: IllegalArgumentException 발생

**시나리오 6**: 위험도 점수 범위 초과로 명령 생성 시도
- **Given**: 100을 초과하는 위험도 점수
- **When**: RecordAuthenticationAttemptCommand 생성
- **Then**: IllegalArgumentException 발생

**시나리오 7**: null 위험도 이유로 명령 생성 시도
- **Given**: null 위험도 이유
- **When**: RecordAuthenticationAttemptCommand 생성
- **Then**: IllegalArgumentException 발생

#### 2.1.3 경계값 테스트
**시나리오 8**: 최소 위험도 점수(0)로 명령 생성
- **Given**: 위험도 점수 0
- **When**: RecordAuthenticationAttemptCommand 생성
- **Then**: 명령 객체가 정상적으로 생성됨

**시나리오 9**: 최대 위험도 점수(100)로 명령 생성
- **Given**: 위험도 점수 100
- **When**: RecordAuthenticationAttemptCommand 생성
- **Then**: 명령 객체가 정상적으로 생성됨

### 2.2 UnlockAccountCommand 테스트 시나리오

#### 2.2.1 정상 케이스
**시나리오 10**: 유효한 계정 잠금 해제 명령 생성
- **Given**: 유효한 세션 ID, 사용자 ID
- **When**: UnlockAccountCommand 생성
- **Then**: 명령 객체가 정상적으로 생성됨

#### 2.2.2 예외 케이스
**시나리오 11**: null 세션 ID로 명령 생성 시도
- **Given**: null 세션 ID
- **When**: UnlockAccountCommand 생성
- **Then**: IllegalArgumentException 발생

**시나리오 12**: 빈 사용자 ID로 명령 생성 시도
- **Given**: 빈 문자열 사용자 ID
- **When**: UnlockAccountCommand 생성
- **Then**: IllegalArgumentException 발생

## 3. Use Cases 테스트 시나리오

### 3.1 RecordAttemptUseCase 테스트 시나리오

#### 3.1.1 정상 케이스
**시나리오 13**: 성공적인 인증 시도 기록
- **Given**: 존재하는 세션, 유효한 인증 시도 명령
- **When**: 성공적인 인증 시도 기록
- **Then**: 시도가 세션에 기록되고 계정이 잠금되지 않음

**시나리오 14**: 실패한 인증 시도 기록 (잠금 임계치 미달)
- **Given**: 존재하는 세션, 4회 이하의 실패 기록
- **When**: 실패한 인증 시도 기록
- **Then**: 시도가 기록되고 계정이 잠금되지 않음

**시나리오 15**: 실패한 인증 시도 기록으로 계정 잠금 발생
- **Given**: 존재하는 세션, 이미 4회 실패 기록 존재
- **When**: 5번째 실패한 인증 시도 기록
- **Then**: 계정이 잠금되고 AccountLocked 이벤트 발행

**시나리오 16**: 잠긴 계정에 성공적인 인증 시도 기록
- **Given**: 계정이 잠긴 상태의 세션, 성공적인 인증 시도
- **When**: 성공적인 인증 시도 기록
- **Then**: 계정 잠금이 해제되고 시도가 기록됨

#### 3.1.2 예외 케이스
**시나리오 17**: 존재하지 않는 세션에 시도 기록
- **Given**: 존재하지 않는 세션 ID
- **When**: 인증 시도 기록 요청
- **Then**: SessionNotFoundException 발생

**시나리오 18**: Repository 조회 실패
- **Given**: Repository에서 예외 발생
- **When**: 인증 시도 기록 요청
- **Then**: 원본 예외가 전파됨

#### 3.1.3 통합 시나리오
**시나리오 19**: 시간 윈도우 밖의 실패 시도는 계정 잠금에 영향 없음
- **Given**: 15분 이전의 실패 기록 4회, 현재 시점의 실패 시도
- **When**: 새로운 실패 시도 기록
- **Then**: 계정이 잠금되지 않음

**시나리오 20**: 다른 사용자의 시도는 계정 잠금에 영향 없음
- **Given**: userA의 4회 실패 기록, userB의 실패 시도
- **When**: userB의 실패 시도 기록
- **Then**: userB의 계정이 잠금되지 않음

### 3.2 CheckLockoutUseCase 테스트 시나리오

#### 3.2.1 정상 케이스
**시나리오 21**: 정상 계정의 잠금 상태 확인
- **Given**: 잠금되지 않은 계정
- **When**: 계정 잠금 상태 확인
- **Then**: 잠금되지 않음 결과 반환

**시나리오 22**: 잠긴 계정의 잠금 상태 확인
- **Given**: 잠긴 계정
- **When**: 계정 잠금 상태 확인
- **Then**: 잠금됨 결과와 해제 시각 반환

**시나리오 23**: 시간 경과로 자동 해제된 계정 확인
- **Given**: 잠금 시간이 경과한 계정
- **When**: 계정 잠금 상태 확인
- **Then**: 잠금되지 않음 결과 반환

#### 3.2.2 예외 케이스
**시나리오 24**: 존재하지 않는 세션의 잠금 상태 확인
- **Given**: 존재하지 않는 세션 ID
- **When**: 계정 잠금 상태 확인
- **Then**: SessionNotFoundException 발생

### 3.3 UnlockAccountUseCase 테스트 시나리오

#### 3.3.1 정상 케이스
**시나리오 25**: 잠긴 계정의 명시적 해제
- **Given**: 잠긴 계정
- **When**: 계정 잠금 해제 요청
- **Then**: 계정 잠금이 해제되고 성공 결과 반환

**시나리오 26**: 이미 해제된 계정의 해제 요청
- **Given**: 잠금되지 않은 계정
- **When**: 계정 잠금 해제 요청
- **Then**: 성공 결과 반환 (중복 요청 허용)

#### 3.3.2 예외 케이스
**시나리오 27**: 존재하지 않는 세션의 계정 해제
- **Given**: 존재하지 않는 세션 ID
- **When**: 계정 잠금 해제 요청
- **Then**: SessionNotFoundException 발생

**시나리오 28**: Repository 저장 실패
- **Given**: Repository 저장 시 예외 발생
- **When**: 계정 잠금 해제 요청
- **Then**: 원본 예외가 전파됨

## 4. Result Objects 테스트 시나리오

### 4.1 RecordAttemptResult 테스트 시나리오

#### 4.1.1 정상 케이스
**시나리오 29**: 성공적인 시도 기록 결과 생성
- **Given**: 성공 상태, 잠금되지 않은 계정, 낮은 위험도
- **When**: RecordAttemptResult 생성
- **Then**: 결과 객체가 정상적으로 생성됨

**시나리오 30**: 계정 잠금 발생 결과 생성
- **Given**: 실패 상태, 잠긴 계정, 잠금 해제 시각
- **When**: RecordAttemptResult 생성
- **Then**: 계정 잠금 정보가 포함된 결과 생성

#### 4.1.2 불변성 테스트
**시나리오 31**: 결과 객체의 불변성 확인
- **Given**: 생성된 RecordAttemptResult 객체
- **When**: 객체 내부 상태 접근
- **Then**: 모든 필드가 불변임

### 4.2 LockoutCheckResult 테스트 시나리오

#### 4.2.1 정상 케이스
**시나리오 32**: 잠금되지 않은 상태 결과 생성
- **Given**: 잠금되지 않은 계정 정보
- **When**: LockoutCheckResult 생성
- **Then**: 잠금되지 않음 결과 생성

**시나리오 33**: 잠긴 상태 결과 생성
- **Given**: 잠긴 계정 정보, 해제 시각
- **When**: LockoutCheckResult 생성
- **Then**: 잠금 정보가 포함된 결과 생성

### 4.3 UnlockAccountResult 테스트 시나리오

#### 4.3.1 정상 케이스
**시나리오 34**: 성공적인 해제 결과 생성
- **Given**: 해제 성공 정보
- **When**: UnlockAccountResult 생성
- **Then**: 성공 결과 객체 생성

## 5. Exception Classes 테스트 시나리오

### 5.1 SessionNotFoundException 테스트 시나리오

**시나리오 35**: 세션 ID와 함께 예외 생성
- **Given**: 존재하지 않는 세션 ID
- **When**: SessionNotFoundException 생성
- **Then**: 세션 ID 정보가 포함된 메시지와 함께 예외 생성

**시나리오 36**: 기본 메시지로 예외 생성
- **Given**: 기본 메시지
- **When**: SessionNotFoundException 생성
- **Then**: 기본 메시지와 함께 예외 생성

### 5.2 SessionValidationException 테스트 시나리오

**시나리오 37**: 검증 실패 정보와 함께 예외 생성
- **Given**: 검증 실패 상세 정보
- **When**: SessionValidationException 생성
- **Then**: 검증 실패 정보가 포함된 예외 생성

### 5.3 UnlockNotAllowedException 테스트 시나리오

**시나리오 38**: 해제 불가 이유와 함께 예외 생성
- **Given**: 해제 불가 이유
- **When**: UnlockNotAllowedException 생성
- **Then**: 이유 정보가 포함된 예외 생성

## 6. 통합 테스트 시나리오

### 6.1 Repository 연동 테스트

**시나리오 39**: Repository 조회 및 저장 연동
- **Given**: Mock Repository, 존재하는 세션
- **When**: Use Case 실행
- **Then**: Repository 메서드가 올바르게 호출됨

**시나리오 40**: Repository 예외 처리 연동
- **Given**: Repository에서 예외 발생
- **When**: Use Case 실행
- **Then**: 적절한 예외가 전파됨

### 6.2 EventPublisher 연동 테스트

**시나리오 41**: 도메인 이벤트 발행 연동
- **Given**: Mock EventPublisher, 계정 잠금 발생 상황
- **When**: RecordAttemptUseCase 실행
- **Then**: AccountLocked 이벤트가 발행됨

**시나리오 42**: 이벤트 발행 실패 처리
- **Given**: EventPublisher에서 예외 발생
- **When**: Use Case 실행
- **Then**: 적절한 예외 처리 또는 로깅

## 7. 성능 테스트 시나리오

**시나리오 43**: 대량 인증 시도 처리 성능
- **Given**: 1000개의 동시 인증 시도
- **When**: RecordAttemptUseCase 병렬 실행
- **Then**: 응답 시간이 허용 범위 내에 있음

**시나리오 44**: 메모리 사용량 테스트
- **Given**: 장시간 실행되는 세션
- **When**: 지속적인 Use Case 호출
- **Then**: 메모리 누수가 발생하지 않음

## 8. Mock 객체 테스트 전략

### 8.1 Repository Mock
- `findBySessionId()`: 다양한 시나리오별 반환값 설정
- `save()`: 호출 검증 및 반환값 설정
- 예외 상황 시뮬레이션

### 8.2 EventPublisher Mock
- `publish()`: 이벤트 발행 호출 검증
- `publishAll()`: 다중 이벤트 발행 검증
- 발행 실패 시나리오 시뮬레이션

## 9. 테스트 데이터 전략

### 9.1 고정 테스트 데이터
- 표준 세션 ID: `550e8400-e29b-41d4-a716-446655440000`
- 표준 사용자 ID: `testUser123`
- 표준 클라이언트 IP: `192.168.1.100`

### 9.2 동적 테스트 데이터
- 현재 시간 기반 타임스탬프
- 랜덤 UUID 생성
- 다양한 위험도 점수 범위

이 테스트 시나리오는 AuthenticationSession 애그리거트의 Application Layer Command Side의 모든 컴포넌트를 철저히 검증하기 위해 작성되었습니다.