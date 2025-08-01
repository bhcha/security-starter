# AuthenticationSession Aggregate - Phase 6 테스트 시나리오

## 1. 테스트 시나리오 개요
AuthenticationSession 애그리거트의 Application Layer Query Side 컴포넌트들에 대한 테스트 시나리오를 정의합니다.

## 2. Queries 테스트 시나리오

### 2.1 GetSessionStatusQuery 테스트 시나리오

#### 2.1.1 정상 케이스
**시나리오 1**: 유효한 세션 ID로 쿼리 생성
- **Given**: 유효한 세션 ID
- **When**: GetSessionStatusQuery 생성
- **Then**: 쿼리 객체가 정상적으로 생성됨

**시나리오 2**: 세션 ID와 사용자 ID로 쿼리 생성
- **Given**: 유효한 세션 ID, 사용자 ID
- **When**: GetSessionStatusQuery 생성
- **Then**: 쿼리 객체가 정상적으로 생성됨

#### 2.1.2 예외 케이스
**시나리오 3**: null 세션 ID로 쿼리 생성 시도
- **Given**: null 세션 ID
- **When**: GetSessionStatusQuery 생성
- **Then**: IllegalArgumentException 발생

**시나리오 4**: 빈 세션 ID로 쿼리 생성 시도
- **Given**: 빈 문자열 세션 ID
- **When**: GetSessionStatusQuery 생성
- **Then**: IllegalArgumentException 발생

### 2.2 GetFailedAttemptsQuery 테스트 시나리오

#### 2.2.1 정상 케이스
**시나리오 5**: 유효한 매개변수로 쿼리 생성
- **Given**: 유효한 세션 ID, 시간 범위, 제한 개수
- **When**: GetFailedAttemptsQuery 생성
- **Then**: 쿼리 객체가 정상적으로 생성됨

**시나리오 6**: 사용자 ID 포함하여 쿼리 생성
- **Given**: 세션 ID, 사용자 ID, 시간 범위, 제한 개수
- **When**: GetFailedAttemptsQuery 생성
- **Then**: 쿼리 객체가 정상적으로 생성됨

#### 2.2.2 예외 케이스
**시나리오 7**: null 세션 ID로 쿼리 생성 시도
- **Given**: null 세션 ID
- **When**: GetFailedAttemptsQuery 생성
- **Then**: IllegalArgumentException 발생

**시나리오 8**: 시작 시간이 종료 시간보다 늦을 때
- **Given**: from이 to보다 나중인 시간
- **When**: GetFailedAttemptsQuery 생성
- **Then**: IllegalArgumentException 발생

**시나리오 9**: 음수 제한 개수로 쿼리 생성 시도
- **Given**: 음수 limit 값
- **When**: GetFailedAttemptsQuery 생성
- **Then**: IllegalArgumentException 발생

#### 2.2.3 경계값 테스트
**시나리오 10**: 제한 개수 0으로 쿼리 생성
- **Given**: limit = 0
- **When**: GetFailedAttemptsQuery 생성
- **Then**: 쿼리 객체가 정상적으로 생성됨

**시나리오 11**: 최대 제한 개수로 쿼리 생성
- **Given**: limit = 1000
- **When**: GetFailedAttemptsQuery 생성
- **Then**: 쿼리 객체가 정상적으로 생성됨

## 3. Query Handler 테스트 시나리오

### 3.1 SessionQueryHandler - GetSessionStatusQuery 처리

#### 3.1.1 정상 케이스
**시나리오 12**: 존재하는 세션의 상태 조회
- **Given**: 존재하는 세션 ID로 쿼리
- **When**: SessionQueryHandler.handle() 호출
- **Then**: SessionStatusResponse 반환

**시나리오 13**: 잠긴 세션의 상태 조회
- **Given**: 잠긴 세션 ID로 쿼리
- **When**: SessionQueryHandler.handle() 호출
- **Then**: 잠금 정보가 포함된 SessionStatusResponse 반환

**시나리오 14**: 활성 세션의 상태 조회
- **Given**: 활성 세션 ID로 쿼리
- **When**: SessionQueryHandler.handle() 호출
- **Then**: 활성 상태 정보가 포함된 SessionStatusResponse 반환

#### 3.1.2 예외 케이스
**시나리오 15**: 존재하지 않는 세션 조회
- **Given**: 존재하지 않는 세션 ID로 쿼리
- **When**: SessionQueryHandler.handle() 호출
- **Then**: SessionNotFoundException 발생

**시나리오 16**: Query Port 조회 실패
- **Given**: Query Port에서 예외 발생
- **When**: SessionQueryHandler.handle() 호출
- **Then**: SessionQueryException 발생

#### 3.1.3 성능 테스트
**시나리오 17**: 대량 데이터가 있는 세션 조회
- **Given**: 수천 개의 인증 시도가 있는 세션
- **When**: SessionQueryHandler.handle() 호출
- **Then**: 응답 시간이 허용 범위 내에 있음

### 3.2 SessionQueryHandler - GetFailedAttemptsQuery 처리

#### 3.2.1 정상 케이스
**시나리오 18**: 실패한 인증 시도 목록 조회
- **Given**: 실패 시도가 있는 세션으로 쿼리
- **When**: SessionQueryHandler.handle() 호출
- **Then**: FailedAttemptsResponse 반환

**시나리오 19**: 특정 사용자의 실패 시도 조회
- **Given**: 사용자 ID가 포함된 쿼리
- **When**: SessionQueryHandler.handle() 호출
- **Then**: 해당 사용자의 실패 시도만 반환

**시나리오 20**: 시간 범위 필터링된 실패 시도 조회
- **Given**: 특정 시간 범위로 제한된 쿼리
- **When**: SessionQueryHandler.handle() 호출
- **Then**: 시간 범위 내의 실패 시도만 반환

**시나리오 21**: 제한 개수가 적용된 실패 시도 조회
- **Given**: limit=5로 제한된 쿼리
- **When**: SessionQueryHandler.handle() 호출
- **Then**: 최대 5개의 실패 시도만 반환

#### 3.2.2 예외 케이스
**시나리오 22**: 존재하지 않는 세션의 실패 시도 조회
- **Given**: 존재하지 않는 세션 ID로 쿼리
- **When**: SessionQueryHandler.handle() 호출
- **Then**: 빈 목록 반환 (예외 발생하지 않음)

**시나리오 23**: Query Port 조회 실패
- **Given**: Query Port에서 예외 발생
- **When**: SessionQueryHandler.handle() 호출
- **Then**: SessionQueryException 발생

#### 3.2.3 경계값 테스트
**시나리오 24**: 빈 결과 조회
- **Given**: 실패 시도가 없는 세션으로 쿼리
- **When**: SessionQueryHandler.handle() 호출
- **Then**: 빈 목록이 포함된 FailedAttemptsResponse 반환

**시나리오 25**: 제한 개수보다 적은 데이터 조회
- **Given**: limit=10이지만 실제 데이터는 3개인 상황
- **When**: SessionQueryHandler.handle() 호출
- **Then**: 3개 데이터가 포함된 FailedAttemptsResponse 반환

## 4. Response Objects 테스트 시나리오

### 4.1 SessionStatusResponse 테스트 시나리오

#### 4.1.1 정상 케이스
**시나리오 26**: 활성 세션 응답 생성
- **Given**: 활성 세션 데이터
- **When**: SessionStatusResponse 생성
- **Then**: 활성 상태 정보가 포함된 응답 생성

**시나리오 27**: 잠긴 세션 응답 생성
- **Given**: 잠긴 세션 데이터
- **When**: SessionStatusResponse 생성
- **Then**: 잠금 정보가 포함된 응답 생성

#### 4.1.2 불변성 테스트
**시나리오 28**: 응답 객체의 불변성 확인
- **Given**: 생성된 SessionStatusResponse 객체
- **When**: 객체 내부 상태 접근
- **Then**: 모든 필드가 불변임

#### 4.1.3 정적 팩토리 메서드
**시나리오 29**: Projection으로부터 응답 생성
- **Given**: SessionStatusProjection 객체
- **When**: SessionStatusResponse.from() 호출
- **Then**: 프로젝션 데이터가 매핑된 응답 생성

### 4.2 FailedAttemptsResponse 테스트 시나리오

#### 4.2.1 정상 케이스
**시나리오 30**: 다중 실패 시도 응답 생성
- **Given**: 여러 실패 시도 데이터
- **When**: FailedAttemptsResponse 생성
- **Then**: 모든 실패 시도가 포함된 응답 생성

**시나리오 31**: 빈 실패 시도 응답 생성
- **Given**: 빈 실패 시도 목록
- **When**: FailedAttemptsResponse 생성
- **Then**: 빈 목록이 포함된 응답 생성

#### 4.2.2 정적 팩토리 메서드
**시나리오 32**: Projection 목록으로부터 응답 생성
- **Given**: FailedAttemptProjection 목록
- **When**: FailedAttemptsResponse.from() 호출
- **Then**: 프로젝션 데이터가 매핑된 응답 생성

## 5. Projection Classes 테스트 시나리오

### 5.1 SessionStatusProjection 테스트 시나리오

#### 5.1.1 매핑 테스트
**시나리오 33**: AuthenticationSession으로부터 프로젝션 생성
- **Given**: AuthenticationSession 애그리거트
- **When**: SessionStatusProjection.from() 호출
- **Then**: 도메인 데이터가 정확히 매핑된 프로젝션 생성

**시나리오 34**: 계산된 값 포함 프로젝션 생성
- **Given**: 여러 인증 시도가 있는 세션
- **When**: SessionStatusProjection.from() 호출
- **Then**: 총 시도 수, 실패 수 등이 계산되어 포함됨

#### 5.1.2 성능 최적화
**시나리오 35**: 지연 로딩 없는 프로젝션 생성
- **Given**: 대량 데이터가 있는 세션
- **When**: SessionStatusProjection.from() 호출
- **Then**: 필요한 데이터만 즉시 로딩됨

### 5.2 FailedAttemptProjection 테스트 시나리오

#### 5.2.1 매핑 테스트
**시나리오 36**: AuthenticationAttempt으로부터 프로젝션 생성
- **Given**: 실패한 AuthenticationAttempt
- **When**: FailedAttemptProjection.from() 호출
- **Then**: 실패 시도 데이터가 정확히 매핑된 프로젝션 생성

**시나리오 37**: 위험도 정보 포함 프로젝션 생성
- **Given**: 높은 위험도의 실패 시도
- **When**: FailedAttemptProjection.from() 호출
- **Then**: 위험도 점수와 이유가 포함된 프로젝션 생성

## 6. Query Ports 테스트 시나리오

### 6.1 LoadSessionStatusQueryPort 테스트 시나리오

**시나리오 38**: 세션 상태 로딩 성공
- **Given**: 존재하는 세션 ID
- **When**: loadSessionStatus() 호출
- **Then**: SessionStatusProjection 반환

**시나리오 39**: 존재하지 않는 세션 로딩
- **Given**: 존재하지 않는 세션 ID
- **When**: loadSessionStatus() 호출
- **Then**: Optional.empty() 반환

**시나리오 40**: 사용자별 세션 상태 로딩
- **Given**: 세션 ID와 사용자 ID
- **When**: loadSessionStatusByUser() 호출
- **Then**: 해당 사용자 관련 정보가 필터링된 프로젝션 반환

### 6.2 LoadFailedAttemptsQueryPort 테스트 시나리오

**시나리오 41**: 실패 시도 목록 로딩
- **Given**: 세션 ID, 시간 범위, 제한 개수
- **When**: loadFailedAttempts() 호출
- **Then**: FailedAttemptProjection 목록 반환

**시나리오 42**: 사용자별 실패 시도 로딩
- **Given**: 세션 ID, 사용자 ID, 시간 범위
- **When**: loadFailedAttemptsByUser() 호출
- **Then**: 해당 사용자의 실패 시도만 반환

**시나리오 43**: 실패 시도 개수 조회
- **Given**: 세션 ID, 시간 범위
- **When**: countFailedAttempts() 호출
- **Then**: 실패 시도 총 개수 반환

## 7. Exception 테스트 시나리오

### 7.1 SessionQueryException 테스트 시나리오

**시나리오 44**: 쿼리 처리 실패 예외 생성
- **Given**: 쿼리 처리 실패 상황
- **When**: SessionQueryException 생성
- **Then**: 실패 정보가 포함된 예외 생성

**시나리오 45**: 원인과 함께 예외 생성
- **Given**: 원본 예외와 메시지
- **When**: SessionQueryException 생성
- **Then**: 예외 체인이 포함된 예외 생성

## 8. 통합 테스트 시나리오

### 8.1 Query Handler와 Port 연동

**시나리오 46**: 전체 쿼리 플로우 테스트
- **Given**: Mock Query Port, 쿼리 객체
- **When**: QueryHandler에서 전체 처리 흐름 실행
- **Then**: 포트 호출, 프로젝션 매핑, 응답 생성이 순서대로 실행됨

**시나리오 47**: 예외 전파 테스트
- **Given**: Port에서 예외 발생
- **When**: QueryHandler 실행
- **Then**: 적절한 애플리케이션 예외로 변환되어 전파됨

## 9. 성능 테스트 시나리오

**시나리오 48**: 대량 데이터 조회 성능
- **Given**: 10,000개의 인증 시도가 있는 세션
- **When**: 실패 시도 목록 조회
- **Then**: 응답 시간이 100ms 이내

**시나리오 49**: 동시 쿼리 처리 성능
- **Given**: 100개의 동시 쿼리 요청
- **When**: 동시 처리 실행
- **Then**: 모든 요청이 정상 처리되고 응답 시간이 일정 범위 내

## 10. 캐싱 테스트 시나리오

**시나리오 50**: 반복 조회 시 캐싱 효과
- **Given**: 동일한 세션 상태를 연속 조회
- **When**: 두 번째 조회 실행
- **Then**: 캐시에서 응답하여 빠른 응답 시간

이 테스트 시나리오는 AuthenticationSession 애그리거트의 Application Layer Query Side의 모든 컴포넌트를 철저히 검증하기 위해 작성되었습니다.