# DDD + TDD 구현 필수 지침

## 🎯 핵심 원칙
```
1. 테스트가 설계를 주도한다 (Test-Driven Design)
2. 도메인 모델이 비즈니스 규칙을 강제한다 (Domain-Driven)
3. RED → GREEN → REFACTOR 사이클을 절대 위반하지 않는다
```

## 📋 구현 순서 (절대 준수)

### 1단계: 테스트 시나리오 작성
```markdown
위치: /docs/testscenario/{context}/{aggregate}/authentication-vo-scenarios.md

예시:
/docs/testscenario/auth/Authentication/authentication-vo-scenarios.md
- [ ] 유효한 Credentials로 Authentication 생성
- [ ] Username 없이 생성 시 예외 발생
- [ ] Password 없이 생성 시 예외 발생
- [ ] 인증 성공 시 토큰 발급
- [ ] 인증 성공 시에만 토큰 존재
- [ ] 실패한 인증은 토큰 없음
```

### 2단계: Value Object부터 구현
```
순서: Value Object → Entity → Aggregate → Domain Service → Event

이유:
- Value Object는 의존성이 없음
- 불변성과 자가 검증으로 안정적 기반 제공
- Aggregate가 Value Object를 사용
```

## 🔴 RED - 테스트 작성 규칙

### 테스트 구조 (필수)
```java
@Test
@DisplayName("한글로 명확한 시나리오 설명")
void should동작When조건() {
    // Given - 전제 조건
    // When - 실행
    // Then - 검증 (assertThat 사용)
}
```

### 테스트 작성 체크리스트
```
□ 한 번에 하나의 테스트만 작성
□ 컴파일 에러 또는 실패 확인
□ 테스트 이름이 시나리오를 명확히 설명
□ Given-When-Then 구조 사용
```

## 🟢 GREEN - 최소 구현 규칙

### 구현 단계
```
1. 컴파일 통과 (null 반환 가능)
2. 하드코딩으로 테스트 통과
3. 일반화 (필요시)
```

### 구현 체크리스트
```
□ 테스트를 통과하는 최소 코드만 작성
□ 다른 기능 추가 금지
□ 설계 개선 금지 (리팩토링 단계에서)
```

## 🔵 REFACTOR - 리팩토링 규칙

### 리팩토링 시점
```
- 모든 테스트가 GREEN일 때만
- 중복 코드 발견 시
- 코드 냄새 감지 시
```

### 리팩토링 체크리스트
```
□ 모든 테스트 통과 확인
□ 구조적 변경만 수행
□ 기능 변경 금지
□ 커밋 전 테스트 재실행
```

## 📊 DDD 도메인 모델별 필수 테스트

### Value Object (100% 커버리지)
```java
필수 테스트:
1. 정상 생성 - of(), from(), create()
2. null/empty 검증
3. 경계값 검증 (최소/최대)
4. 형식 검증 (패턴, 길이)
5. equals/hashCode
6. 불변성

예시:
- Credentials.of(valid) → 성공
- Credentials.of(null) → IllegalArgumentException
- Credentials.of(empty) → IllegalArgumentException
- 동일 값 → equals true
- 다른 값 → equals false
```

### Aggregate (95% 이상)
```java
필수 테스트:
1. 생성 시나리오 (모든 팩토리 메서드)
2. 모든 불변 규칙(invariants)
3. 상태 전이
4. 도메인 이벤트 발생
5. 비즈니스 메서드

예시 (Authentication):
- createNew() → 불변규칙 검증
- "Username과 Password는 필수" → 없으면 예외
- "인증 성공 시에만 토큰 발급" → 성공시 토큰 존재
- "실패한 인증은 토큰 없음" → 실패시 토큰 null
- authenticate() → AuthenticationAttempted 이벤트 발생
```

### Domain Event (100%)
```java
필수 테스트:
1. 필수 필드 검증
2. 생성 시점 데이터 불변성
3. getAggregateId() 정상 동작
4. getEventType() 정상 반환
```

## 🚫 절대 금지 사항


## 🔧 커밋 규칙

### 커밋 타이밍
```
1. 각 TDD 사이클 완료 시
2. 구조적 변경 완료 시
3. 기능적 변경 완료 시
```

### 커밋 메시지
```
test: [도메인] 테스트 시나리오 추가
feat: [도메인] 기능 구현
refactor: [도메인] 구조 개선
```

## 📈 품질 지표

### 필수 달성 기준
```
커버리지:
- Value Object: 100%
- Aggregate: 95% 이상
- Domain Service: 90% 이상
- Domain Event: 100%

테스트:
- 모든 불변 규칙 테스트
- 모든 경계 조건 테스트
- 모든 예외 상황 테스트
```

### 테스트 실행
```bash
# 매 구현 후
./gradlew test

# 커버리지 확인
./gradlew test jacocoTestReport

# 특정 테스트만
./gradlew test --tests "CredentialsTest"
```

## ✅ 구현 완료 체크리스트

### 각 클래스별
```
□ 테스트 목록 100% 완료
□ 커버리지 기준 충족
□ 모든 불변 규칙 구현
□ 정적 팩토리 메서드 사용
□ equals/hashCode 구현 (VO)
□ 도메인 이벤트 발생 (Aggregate)
```

### Phase 완료 시
```
□ 모든 도메인 모델 구현
□ 통합 테스트 작성
□ 문서화 완료
□ tracker.md 업데이트
```

이 지침을 따르면 AI는 일관된 품질의 DDD + TDD 구현을 제공할 수 있습니다.