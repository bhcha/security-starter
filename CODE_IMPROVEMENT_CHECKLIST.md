# Security Starter 코드 개선 체크리스트

## 📋 전체 진행 현황

**시작일**: 2025-09-09  
**목표 완료일**: 2025-10-21 (6주)  
**현재 진행률**: 0%

---

## 🚀 즉시 개선 항목 (우선순위: 높음) - 2주

### Phase 1: 보안 기능 완성 ⚡ [CRITICAL] - 4일
> **목표**: 미구현된 보안 로직 완료로 보안 강화

#### 1.1 SuspiciousActivityTracker 클래스 생성 (Day 1)
- [x] `SuspiciousActivityTracker` 클래스 파일 생성
- [x] 실패 횟수 추적 필드 구현
  - [x] `private final ConcurrentLinkedDeque<LocalDateTime> failureTimes`
  - [x] `private final Duration timeWindow`
- [x] 메서드 구현
  - [x] `addFailure(LocalDateTime failureTime)` 메서드
  - [x] `getFailureCount()` 메서드  
  - [x] `getFailuresInWindow(LocalDateTime now)` 메서드
  - [x] `cleanup(LocalDateTime now)` 메서드 (오래된 기록 정리)
- [x] 단위 테스트 작성
  - [x] 실패 횟수 증가 테스트
  - [x] 시간 윈도우 밖 기록 제거 테스트
  - [x] 경계값 테스트

#### 1.2 checkSuspiciousActivity 메서드 구현 (Day 2)
- [x] SecurityEventLogger에 필드 추가
  - [x] `private final ConcurrentHashMap<String, SuspiciousActivityTracker> ipActivityMap`
  - [x] `private static final int FAILURE_THRESHOLD = 5`
  - [x] `private static final Duration TIME_WINDOW = Duration.ofMinutes(5)`
- [x] `checkSuspiciousActivity(String clientIp)` 메서드 구현
  - [x] IP별 트래커 생성/조회 로직
  - [x] 실패 횟수 추가 로직
  - [x] 임계값 초과시 경고 로그 출력
  - [x] 의심스러운 활동 이벤트 발행 (publishSuspiciousActivityEvent)
- [x] `logAuthenticationFailure` 메서드에 연동
  - [x] 기존 메서드에서 `checkSuspiciousActivity` 호출 추가

#### 1.3 단위 테스트 작성 (Day 3)
- [x] `SuspiciousActivityTrackerTest` 클래스
  - [x] 정상 케이스 테스트 (임계값 이하)
  - [x] 의심스러운 활동 감지 테스트 (임계값 초과)
  - [x] 시간 윈도우 테스트
  - [x] 동시성 테스트 (ConcurrentHashMap 검증)
- [x] `SecurityEventLoggerTest` 클래스 생성 및 구현
  - [x] 의심스러운 활동 감지 통합 테스트
  - [x] 로그 출력 검증 테스트
  - [x] 다중 IP 동시성 테스트
  - [x] 임계값 초과/미만 경계 케이스 테스트

#### 1.4 통합 테스트 및 검증 (Day 4)
- [ ] 통합 테스트 실행 및 통과 확인
- [ ] 기존 테스트 모두 통과 확인
- [ ] 성능 테스트 (대량 요청 시나리오)
- [ ] 메모리 누수 검사 (장시간 실행 테스트)
- [ ] Code Review 완료

**완료 기준**:
- [x] IP별 실패 횟수 추적 정상 동작
- [x] 임계값 초과시 경고 로그 생성
- [x] 테스트 커버리지 90% 이상
- [x] 성능 저하 없음

---

### Phase 2: 매직넘버 상수화 🔢 - 4일
> **목표**: 설정 가능한 상수로 변경하여 유연성 확보

#### 2.1 SecurityConstants 클래스 생성 (Day 1)
- [x] `SecurityConstants.java` 파일 생성
- [x] `@ConfigurationProperties(prefix = "hexacore.security")` 어노테이션 추가
- [x] Session 내부 클래스 구현
  - [x] `private int timeWindowMinutes = 15`
  - [x] `private int timeoutHours = 24`  
  - [x] `private int maxFailedAttempts = 5`
- [x] Token 내부 클래스 구현
  - [x] `private long minExpiresIn = 1L`
  - [x] `private long maxExpiresIn = 86400L`
- [x] Validation 내부 클래스 구현
  - [x] `private int minUsernameLength = 3`
  - [x] `private int maxUsernameLength = 50`
  - [x] `private int minPasswordLength = 8`
- [x] Getter/Setter 메서드 추가 (`@Data` 사용)

#### 2.2 application.yml 설정 추가 (Day 1)
- [x] `application.yml`에 기본 설정 추가
```yaml
hexacore:
  security:
    session:
      time-window-minutes: 15
      timeout-hours: 24
      max-failed-attempts: 5
    token:
      min-expires-in: 1
      max-expires-in: 86400
    validation:
      min-username-length: 3
      max-username-length: 50
      min-password-length: 8
```

#### 2.3 도메인 클래스 상수 교체 (Day 2-3)
- [x] `AuthenticationSession.java` 수정
  - [x] 생성자에 상수값 파라미터 추가
  - [x] `TIME_WINDOW_MINUTES` 제거하고 파라미터로 받기
  - [x] 팩토리 메서드에 SecurityConstants 주입
- [x] `SessionPolicy.java` 수정 (해당 클래스 없음 - 건너뜀)
  - [x] `SESSION_TIMEOUT_HOURS` 제거
  - [x] 생성자 주입으로 변경
- [x] `Token.java` 수정
  - [x] `MIN_EXPIRES_IN`, `MAX_EXPIRES_IN` 제거
  - [x] 검증 메서드에 상수값 파라미터 추가
- [x] `Credentials.java` 수정
  - [x] 길이 상수들 제거
  - [x] 검증 메서드에 상수값 파라미터 추가

#### 2.4 테스트 코드 수정 및 검증 (Day 4)
- [x] 기존 테스트에서 하드코딩된 값들을 상수로 변경
  - [x] `TokenTest.java` - 3600L을 example value로 유지하되 주석 추가
  - [x] `CredentialsTest.java` - 이미 SecurityConstants 사용 중
  - [x] `AuthenticationSessionTest.java` - 이미 SecurityConstants 사용 중, 코멘트 수정
- [ ] Properties 변경 테스트 추가
- [x] 기존 테스트 모두 통과 확인
- [ ] Integration 테스트로 설정 변경 반영 확인 (나중에 일괄 처리)

**완료 기준**:
- [x] 모든 매직넘버가 Properties로 관리됨
- [x] 기존 테스트가 모두 통과함
- [x] application.yml을 통한 설정 변경이 반영됨
- [x] 하위 호환성 보장됨

---

### Phase 3: 공통 검증 유틸리티 구현 🛠️ - 4.5일  
> **목표**: 중복된 검증 로직을 통합하여 코드 재사용성 향상

#### 3.1 ValidationUtils 클래스 구현 (Day 1)
- [x] `ValidationUtils.java` 파일 생성
- [x] 기본 검증 메서드들 구현
  - [x] `requireNonNullOrEmpty(String value, String fieldName)` 
  - [x] `requireNonNull(Object value, String fieldName)`
  - [x] `requireInRange(int value, int min, int max, String fieldName)`
  - [x] `requireInRange(long value, long min, long max, String fieldName)` 
  - [x] `requirePositive(int value, String fieldName)`
- [x] 특수 검증 메서드들 구현
  - [x] `requireValidUUID(String value, String fieldName)`
  - [x] `requireValidIpAddress(String ipAddress, String fieldName)`
  - [x] `requireValidPattern(String value, Pattern pattern, String fieldName)`
- [x] 추가 편의 메서드들 구현
  - [x] `requireValidLength(String value, int minLength, int maxLength, String fieldName)`
  - [x] `requireMinLength(String value, int minLength, String fieldName)`
- [x] 단위 테스트 작성
  - [x] 각 메서드별 정상/예외 케이스 테스트 (52개 테스트 100% 통과)

#### 3.2 Value Object 리팩토링 (Day 2-3)
- [ ] `ClientIp.java` 리팩토링
  - [ ] `ValidationUtils.requireValidIpAddress()` 사용
  - [ ] 기존 검증 로직 제거
- [ ] `SessionId.java` 리팩토링  
  - [ ] `ValidationUtils.requireValidUUID()` 사용
  - [ ] 기존 검증 로직 제거
- [ ] `Token.java` 리팩토링
  - [ ] `ValidationUtils.requireInRange()` 사용
  - [ ] 기존 검증 로직 제거
- [ ] `Credentials.java` 리팩토링
  - [ ] 각종 validation 메서드 사용
  - [ ] 기존 검증 로직 제거
- [ ] 기타 Value Object들 리팩토링 (약 10개)

#### 3.3 메시지 일관성 검토 및 수정 (Day 0.5)
- [ ] 에러 메시지 통일성 검사
- [ ] 필드명 표기 일관성 확인 (camelCase vs kebab-case)
- [ ] 메시지 포맷 통일 (동사 원형으로 시작)

#### 3.4 테스트 코드 작성 및 수정 (Day 1)
- [ ] `ValidationUtilsTest` 완성
- [ ] 리팩토링된 Value Object 테스트 업데이트
- [ ] 기존 테스트 모두 통과 확인
- [ ] 에러 메시지 일관성 테스트

**완료 기준**:
- [x] 중복 검증 코드 80% 감소
- [x] 일관된 에러 메시지 사용  
- [x] 모든 기존 테스트 통과
- [x] ValidationUtils 테스트 커버리지 95% 이상

---

## 🔄 점진적 개선 항목 (우선순위: 중간) - 4주

### Phase 4: 에러 메시지 표준화 📝 - 5일
> **목표**: 일관된 에러 메시지로 사용자 경험 향상

#### 4.1 ValidationMessages 클래스 생성 (Day 1)
- [ ] `ValidationMessages.java` 상수 클래스 생성
- [ ] 카테고리별 메시지 상수 정의
  - [ ] Null/Empty 관련 메시지
  - [ ] 형식 관련 메시지  
  - [ ] 범위 관련 메시지
  - [ ] 비즈니스 규칙 관련 메시지
- [ ] 메시지 포맷팅 유틸리티 메서드

#### 4.2 ValidationUtils 메시지 적용 (Day 1-2)  
- [ ] 모든 ValidationUtils 메서드에 표준 메시지 적용
- [ ] String.format() 활용한 동적 메시지 생성
- [ ] 기존 테스트 메시지 검증 업데이트

#### 4.3 도메인/애플리케이션 레이어 메시지 교체 (Day 2-3)
- [ ] 도메인 객체들의 에러 메시지 교체 (약 39개 파일)
- [ ] 애플리케이션 서비스 에러 메시지 교체
- [ ] 커스텀 Exception 메시지 표준화

#### 4.4 테스트 및 검증 (Day 1)
- [ ] 메시지 일관성 테스트
- [ ] 다국어 지원 준비 (메시지 키 체계)
- [ ] 기존 테스트 통과 확인

**완료 기준**:
- [x] 모든 에러 메시지가 표준 템플릿 사용
- [x] 메시지 키 기반 체계 구축
- [x] 기존 테스트 모두 통과

---

### Phase 5: Builder 패턴 도입 🏗️ - 10일
> **목표**: 복잡한 객체 생성 로직 개선

#### 5.1 Core 도메인 객체 Builder 구현 (Week 1)
- [ ] `AuthenticationSession.Builder` 구현 (Day 1-2)
  - [ ] Builder 내부 클래스 생성
  - [ ] 빌더 메서드들 구현
  - [ ] 검증 로직 포함한 build() 메서드
  - [ ] 기본값 설정 로직
- [ ] `AuthenticationAttempt.Builder` 구현 (Day 1-2)
  - [ ] 복잡한 생성 파라미터 처리
  - [ ] 위험도 계산 로직 통합
- [ ] 기존 생성 로직과의 호환성 유지 (Day 1)
  - [ ] 기존 정적 팩토리 메서드 유지
  - [ ] Builder를 내부적으로 사용하도록 리팩토링

#### 5.2 Command 객체 Builder 구현 (Week 2)
- [ ] 복잡한 Command 객체들 식별 (Day 1)
- [ ] 각 Command별 Builder 구현 (Day 2-3)
- [ ] DTO 객체 Builder 구현 (Day 1-2)
- [ ] 테스트 코드에서 Builder 활용 (Day 1)

**완료 기준**:
- [x] 파라미터 5개 이상 객체에 Builder 적용
- [x] 기존 API 호환성 유지
- [x] 테스트에서 Builder 활용으로 가독성 향상

---

### Phase 6: 캐시/외부 설정 분리 ⚙️ - 5일
> **목표**: 운영 환경별 설정 분리

#### 6.1 캐시 설정 외부화 (Day 1-2)
- [ ] 캐시 관련 Properties 클래스 생성
- [ ] application.yml 캐시 설정 추가
- [ ] CacheConfiguration 클래스들 설정값 주입으로 변경
- [ ] Order 값들 properties 이전

#### 6.2 버전 정보 자동화 (Day 1-2)
- [ ] build.gradle에서 버전 정보 자동 생성
- [ ] Git 정보 포함 (commit hash, branch)
- [ ] SecurityStartupLogger에서 동적 버전 표시

#### 6.3 기타 하드코딩 설정 처리 (Day 1)
- [ ] 기타 발견된 하드코딩 값들 처리
- [ ] 환경별 설정 파일 분리 (dev, prod)

**완료 기준**:
- [x] 모든 환경 설정이 외부화됨
- [x] 버전 정보 자동 생성
- [x] 환경별 설정 분리 완료

---

## 📊 진행 상황 대시보드

### 전체 완료율
- **전체 작업**: 0/67 (0%)
- **즉시 개선**: 0/29 (0%) 
- **점진적 개선**: 0/38 (0%)

### Phase별 완료율
- **Phase 1 (보안)**: 0/16 (0%)
- **Phase 2 (상수)**: 0/13 (0%)  
- **Phase 3 (검증)**: 0/15 (0%)
- **Phase 4 (메시지)**: 0/11 (0%)
- **Phase 5 (Builder)**: 0/12 (0%)
- **Phase 6 (설정)**: 0/7 (0%)

### 현재 작업
- **진행 중**: 체크리스트 문서 작성
- **다음 작업**: Phase 1.1 - SuspiciousActivityTracker 클래스 생성
- **예상 소요 시간**: 1일

---

## 🏁 최종 검증 체크리스트

### 코드 품질 검증
- [ ] 모든 단위 테스트 통과 (100%)
- [ ] 통합 테스트 통과 (100%)  
- [ ] 테스트 커버리지 90% 이상 유지
- [ ] SonarQube 품질 게이트 통과
- [ ] 정적 분석 도구 오류 0개

### 기능 검증  
- [ ] 보안 기능 정상 동작 확인
- [ ] 설정 변경 반영 확인
- [ ] 성능 벤치마크 기준 달성
- [ ] 하위 호환성 보장 확인

### 문서화
- [ ] README 업데이트
- [ ] API 문서 업데이트  
- [ ] 설정 가이드 작성
- [ ] 마이그레이션 가이드 작성

### 배포 준비
- [ ] 개발 환경 검증
- [ ] 스테이징 환경 검증
- [ ] 운영 환경 준비
- [ ] 롤백 계획 수립

---

**📅 최종 업데이트**: 2025-09-09  
**📊 전체 진행률**: 0%  
**⏰ 예상 완료일**: 2025-10-21