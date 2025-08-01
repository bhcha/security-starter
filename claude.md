## 🎯 개요

DDD 헥사고날 아키텍처 구현 지침서

## 🚫 절대 규칙

1. **각 Phase에서 지정된 참조 문서만 사용** - 다른 문서 참조 금지
2. **구현 프로세스 절대 준수**: 구현계획수립 → 테스트시나리오 작성 → 테스트코드 작성 → 구현 → 리뷰(테스트리뷰+코딩표준리뷰)
3. **레이어별 명세서 작성 필수**: 각 레이어 완료 시 `/docs/spec/` 디렉토리에 명세서 작성, 다음 레이어는 코드가 아닌 명세서만 참조
4. **애그리거트 단위 구현**: 하나의 애그리거트를 Domain → Application → Adapter 순으로 완전히 구현 후 다음 애그리거트 진행
5. **작업 순서 추론 금지**: 문서에 제시된 단계를 순서대로 따르며, 임의로 순서를 변경하거나 추론하지 않음
6. **🚫 하위 모듈 참조 절대 금지**: 현재 프로젝트(security-starter) 개발 시 하위 모듈(`security-auth-starter/`) 내부 코드를 절대 참조하지 않음

## 🏛️ 헥사고날 아키텍처 준수 원칙

### 📐 절대 원칙
1. **Port 인터페이스만 public**: Use Case 인터페이스, Repository 인터페이스만 공개
2. **모든 구현체는 package-private**: Adapter, Use Case 구현체 모두 숨김  
3. **Domain 객체는 public**: Command, Result, Event, Value Object는 공개
4. **Auto-Configuration만 public**: 라이브러리 조립 로직만 공개

### 🔒 구현 규칙
- **Use Case 구현체**: `class XxxUseCaseImpl` (package-private)
- **Adapter 구현체**: `class XxxAdapter` (package-private)
- **Configuration**: `class XxxConfig` (package-private, Auto-Configuration 제외)
- **Port 인터페이스**: `public interface XxxUseCase`, `public interface XxxRepository`

### ✅ 올바른 예시
```java
// ✅ Public Port 인터페이스
public interface AuthenticationUseCase {
    AuthenticationResult authenticate(AuthenticateCommand command);
}

// ✅ Package-private 구현체
@Component
class AuthenticateUseCaseImpl implements AuthenticationUseCase {
    // 구현 내용
}

// ✅ Public Domain 객체
public class AuthenticateCommand {
    // 커맨드 내용
}
```

### ❌ 잘못된 예시
```java
// ❌ 구현체를 public으로 노출
@Component
public class AuthenticateUseCaseImpl implements AuthenticationUseCase {
    // 헥사고날 아키텍처 위반
}
```

### 🎯 준수 체크리스트
- [ ] Use Case 구현체가 package-private인가?
- [ ] Adapter 구현체가 package-private인가?
- [ ] Port 인터페이스가 public인가?
- [ ] Domain 객체가 public인가?
- [ ] 사용자가 구현체에 직접 접근할 수 없는가?

## 🚨 작업 시작 전 필수 확인사항
사용자가 "다음 작업", "작업 시작" 등의 요청을 할 때:

1. **MUST**: 먼저 `/docs/plan/tracker.md` 확인하여 현재 진행 상황 파악
2. **MUST**: 완료된 Phase와 현재 진행해야 할 Phase 식별
3. **MUST**: 현재 애그리거트와 레이어 확인
4. 그 후 해당 Phase의 지침 따라 진행

## 📋 작업 진행 규칙
1. **단계별 독립 실행**: 각 Phase는 독립적으로 수행
2. **진행 상태 기록**: 모든 작업은 tracker.md에 기록
3. **완료 기준 충족**: 명시된 완료 기준을 모두 충족해야 다음 Phase로 진행
4. **구현 계획서 참조**: Phase 1에서 작성한 `/docs/plan/implementation-plan.md`를 참조하여 진행

## 🏗️ 멀티 모듈 구조 및 개발 범위

### 프로젝트 구조
```
security-starter/                   # 루트 프로젝트 (현재 개발 대상)
├── src/main/java/                   # ✅ 개발 대상: 핵심 보안 라이브러리
│   └── com/dx/hexacore/security/    # 도메인, 애플리케이션, 어댑터 레이어
├── docs/                            # ✅ 참조 가능: 개발 문서
├── claude.md                        # ✅ 참조 가능: 개발 지침
└── security-auth-starter/           # ❌ 참조 금지: 하위 모듈
    ├── src/main/java/               # ❌ 절대 참조 금지
    ├── build.gradle                 # ❌ 절대 참조 금지
    └── docs/                        # ❌ 절대 참조 금지
```

### 🚫 하위 모듈 참조 금지 세부 규칙

1. **절대 참조 금지 대상**:
   - `security-auth-starter/` 디렉토리 하위 모든 파일
   - 하위 모듈의 소스 코드, 설정 파일, 문서
   - 하위 모듈의 패키지 구조나 클래스명

2. **개발 집중 대상**:
   - `src/main/java/com/dx/hexacore/security/` 패키지만 개발
   - 루트 프로젝트의 `docs/` 디렉토리 문서만 참조
   - 루트 프로젝트의 `claude.md` 지침만 준수

3. **이유**:
   - 하위 모듈은 현재 개발 중인 라이브러리를 사용하는 별도 프로젝트
   - 핵심 라이브러리 개발에 집중하여 책임 분리
   - 하위 모듈 참조 시 순환 의존성 및 설계 혼란 방지

## 📁 문서 폴더 구조
```
/docs/
├── plan/
│   ├── implementation-plan.md
│   ├── tracker.md
│   └── {애그리거트명소문자}/            # 애그리거트별 폴더
│       └── {애그리거트명}-{phase명}-plan.md
├── testscenario/
│   └── {애그리거트명소문자}/            # 애그리거트별 폴더
│       └── {phase명}-scenarios.md
├── review/
│   └── {애그리거트명소문자}/            # 애그리거트별 폴더
│       ├── {애그리거트명}-{phase명}-test-review.md
│       └── {애그리거트명}-{phase명}-coding-review.md
├── spec/
│   └── {애그리거트명소문자}-{레이어명}-spec.md
└── aggregate_spec/
    └── {애그리거트명소문자}-interface.md
```

---

## Phase 1: 도메인 모델 분석 및 구현 계획 수립

### 🎯 목표

제공된 도메인 모델 문서를 분석하고 애그리거트별 구현 계획을 수립한다.

### 📚 참조 문서

- 도메인모델: `/docs/domainModel/{domain-model}.md`
- 프로젝트 컨텍스트: `/docs/1-context.md`

### ✅ 수행 작업

1. **도메인 모델 분석**
    
    - Bounded Context 식별
    - Aggregate 경계 확인
    - 도메인 이벤트 목록 작성
    - 불변 규칙(Invariants) 정리
2. **구현 순서 결정**
    
    - 의존성이 없거나 적은 애그리거트부터 시작
    - 애그리거트 간 의존성 관계 파악
    - 구현 순서 결정
3. **구현 계획서 작성**
    
    - 파일명: `implementation-plan.md`
    - 위치: `/docs/plan/`
    - 애그리거트별 상세 구현 계획 포함

### 📄 구현 계획서 템플릿

```markdown
# 구현 계획서

## 1. 도메인 모델 분석 결과
### Bounded Context
- [Context명]: 설명

### Aggregate 목록
1. [애그리거트명]: 핵심 책임
2. [애그리거트명]: 핵심 책임

### 의존성 관계
- [애그리거트 A] → [애그리거트 B]: 의존성 설명

## 2. 구현 순서
1차: [애그리거트명] - 의존성 없음
2차: [애그리거트명] - 1차 애그리거트에 의존

## 3. 애그리거트별 상세 계획

### [1차 애그리거트명] (예: Authentication)
#### Domain Layer (Phase 2-4)
**패키지**: `com.dx.hexacore.security.{애그리거트명소문자}.domain`
- Value Objects: [VO1], [VO2]
- Entities: [Entity1]
- Aggregate Root: [AggregateRoot]
- Domain Events: [Event1], [Event2]
- Domain Services: [Service1] (필요시)

#### Application Layer (Phase 5-6)
**패키지**: `com.dx.hexacore.security.{애그리거트명소문자}.application`
- Commands: [Command1], [Command2]
- Queries: [Query1], [Query2]
- Use Cases: [UseCase1], [UseCase2]

#### Adapter Layer (Phase 7-8)
**패키지**: `com.dx.hexacore.security.{애그리거트명소문자}.adapter`
- Inbound: REST API, Event Listener
- Outbound: JPA Persistence, Event Publisher

### [2차 애그리거트명] (예: Session)
**동일한 서브도메인 구조 적용**
```

### ✅ 완료 기준

- [ ] 도메인 모델 분석 완료
- [ ] 애그리거트별 구현 순서 결정
- [ ] `/docs/plan/implementation-plan.md` 작성 완료
- [ ] tracker.md에 전체 계획 기록

---

## ⚠️ 명세서 참조 원칙
- 상위 레이어 구현 시 하위 레이어의 코드를 직접 보지 않음
- 오직 명세서에 정의된 인터페이스와 계약만 참조
- 구현 세부사항이 필요한 경우 명세서를 보완하여 해결
- **완료된 애그리거트 참조 시**: `/docs/aggregate_spec/[애그리거트명소문자]-interface.md` 문서만 참조

## 애그리거트별 구현 (Phase 2-8 반복)

> ⚠️ **중요**:
> 
> - `/docs/plan/implementation-plan.md`에 정의된 순서대로 애그리거트 구현
> - 각 Phase에서는 `/docs/plan/implementation-plan.md`의 해당 애그리거트 섹션만 참조
> - 작업 순서를 임의로 변경하거나 추론하지 않음

### 🔄 애그리거트 구현 플로우

```
`/docs/plan/implementation-plan.md` 참조
↓
현재 차수의 애그리거트 확인
↓
Domain Layer (Phase 2-4)
↓
Application Layer (Phase 5-6)
↓
Adapter Layer (Phase 7-8)
↓
다음 차수 애그리거트로 이동
```

---

## 도메인 레이어 구현 (Phase 2-4)

### Phase 2: [애그리거트명] Value Objects 구현

### 🎯 목표

`/docs/plan/implementation-plan.md`에 정의된 현재 애그리거트의 Value Object를 구현합니다.

### 📚 참조 문서

- `/docs/plan/implementation-plan.md` (현재 애그리거트의 Domain Layer 섹션)
- `/docs/guide/2-coding-standards.md`
- `/docs/guide/3-tdd-workflow.md`
- `/docs/guide/3-1-domain-tdd.md`
- `/docs/guide/4-1-domain-templates.md`
- **의존하는 애그리거트의 인터페이스 문서** (`/docs/aggregate_spec/[의존애그리거트명]-interface.md`) ※ 해당 애그리거트가 완료된 경우만

### 🏗️ 패키지 구조
현재 애그리거트의 도메인 레이어 구현 시 다음 패키지 구조를 따라야 합니다:
```
com.dx.hexacore.security.{애그리거트명소문자}.domain/
├── vo/              # Value Objects
├── event/           # Domain Events  
├── service/         # Domain Services
└── {AggregateRoot}.java
```

### 🔄 구현 프로세스

1. **구현 계획 수립**
    
    - 파일명: `/docs/plan/{애그리거트명소문자}/{애그리거트명}-{phase명}-plan.md`
    - 구현 목표, 범위, 컴포넌트 목록, 완료 기준 명시
    - `/docs/plan/implementation-plan.md`에서 현재 애그리거트의 컴포넌트 목록 확인
    - 각 컴포넌트의 속성 및 검증 규칙 정의

2. **테스트 시나리오 작성**
    
    - 파일명: `/docs/testscenario/{애그리거트명소문자}/{phase명}-scenarios.md`
    - 각 컴포넌트별 테스트 시나리오 문서화
    - 정상/예외 케이스 정의

3. **테스트 코드 작성**
    
    - 시나리오 기반 테스트 코드 작성
    - 최소 5개 테스트 케이스
    - TDD 사이클 적용

4. **구현**
    
    - 테스트 주도 개발
    - 불변성, 정적 팩토리 메서드, 자가 검증, equals/hashCode 구현
    - 코딩 표준 준수

5. **리뷰**
    
    - **테스트 리뷰**: `/docs/review/{애그리거트명소문자}/{애그리거트명}-{phase명}-test-review.md`
      - 테스트 실행 결과 요약, 커버리지 분석, 실패한 테스트 및 해결 과정, 테스트 품질 평가
    - **코딩 표준 리뷰**: `/docs/review/{애그리거트명소문자}/{애그리거트명}-{phase명}-coding-review.md`
      - 코딩 표준 체크리스트, DDD 패턴 적용 검토, 코드 품질 지표, 리팩토링 필요 사항
    - 테스트 통과 확인
    - 코딩 표준 준수 확인

### ✅ 완료 기준

- [ ] `/docs/plan/implementation-plan.md`에 명시된 모든 VO 구현
- [ ] 테스트 커버리지 80% 이상
- [ ] tracker.md 업데이트

---

### Phase 3: [애그리거트명] Entities & Aggregates 구현

### 🎯 목표

`/docs/plan/implementation-plan.md`에 정의된 현재 애그리거트의 Entity와 Aggregate Root를 구현합니다.

### 📚 참조 문서

- `/docs/plan/implementation-plan.md` (현재 애그리거트의 Domain Layer 섹션)
- `/docs/guide/2-coding-standards.md`
- `/docs/guide/3-tdd-workflow.md`
- `/docs/guide/3-1-domain-tdd.md`
- `/docs/guide/4-1-domain-templates.md`
- **의존하는 애그리거트의 인터페이스 문서** (`/docs/aggregate_spec/[의존애그리거트명]-interface.md`) ※ 해당 애그리거트가 완료된 경우만

### 🔄 구현 프로세스

(계획수립 → 테스트시나리오 → 테스트코드 → 구현 → 리뷰)

### ✅ 완료 기준

- [ ] `/docs/plan/implementation-plan.md`에 명시된 Entity/Aggregate 구현
- [ ] 불변 규칙 테스트 통과
- [ ] tracker.md 업데이트

---

### Phase 4: [애그리거트명] Events & Services 구현

### 🎯 목표

`/docs/plan/implementation-plan.md`에 정의된 현재 애그리거트의 Domain Event와 Domain Service를 구현합니다.

### 📚 참조 문서

- `/docs/plan/implementation-plan.md` (현재 애그리거트의 Domain Layer 섹션)
- `/docs/guide/2-coding-standards.md`
- `/docs/guide/3-tdd-workflow.md`
- `/docs/guide/3-1-domain-tdd.md`
- `/docs/guide/4-1-domain-templates.md`
- **의존하는 애그리거트의 인터페이스 문서** (`/docs/aggregate_spec/[의존애그리거트명]-interface.md`) ※ 해당 애그리거트가 완료된 경우만

### 🔄 구현 프로세스

(계획수립 → 테스트시나리오 → 테스트코드 → 구현 → 리뷰)

### ✅ 완료 기준

- [ ] `/docs/plan/implementation-plan.md`에 명시된 Domain Event 구현
- [ ] Domain Service 구현 (있는 경우)
- [ ] **[애그리거트명] 도메인 레이어 명세서 작성** (`/docs/spec/[애그리거트명소문자]-domain-layer-spec.md`) ⚠️

---

## Application 레이어 구현 (Phase 5-6)

### Phase 5: [애그리거트명] Command Side 구현

### 🎯 목표

`/docs/plan/implementation-plan.md`에 정의된 현재 애그리거트의 Command Side를 구현합니다.

### 📚 참조 문서

- `/docs/plan/implementation-plan.md` (현재 애그리거트의 Application Layer 섹션)
- **[애그리거트명] 도메인 레이어 명세서** (`/docs/spec/[애그리거트명소문자]-domain-layer-spec.md`)
- `/docs/guide/2-coding-standards.md`
- `/docs/guide/3-2-application-tdd.md`
- `/docs/guide/4-2-application-templates.md`
- **의존하는 애그리거트의 인터페이스 문서** (`/docs/aggregate_spec/[의존애그리거트명]-interface.md`) ※ 해당 애그리거트가 완료된 경우만

### 🏗️ 패키지 구조
현재 애그리거트의 애플리케이션 레이어 구현 시 다음 패키지 구조를 따라야 합니다:
```
com.dx.hexacore.security.{애그리거트명소문자}.application/
├── command/
│   ├── handler/         # Use Case 구현체
│   ├── port/in/        # Commands, Results, Use Case 인터페이스
│   └── port/out/       # Repository, External Service 인터페이스
├── query/
│   ├── handler/        # Query Handler 구현체
│   ├── port/in/        # Queries, Responses
│   └── port/out/       # Query Repository 인터페이스
├── projection/         # Query용 Projection 객체
└── exception/          # Application 예외
```

### 🔄 구현 프로세스

(계획수립 → 테스트시나리오 → 테스트코드 → 구현 → 리뷰)

### ✅ 완료 기준

- [ ] `/docs/plan/implementation-plan.md`에 명시된 Command/UseCase 구현
- [ ] tracker.md 업데이트

---

### Phase 6: [애그리거트명] Query Side 구현

### 🎯 목표

`/docs/plan/implementation-plan.md`에 정의된 현재 애그리거트의 Query Side를 구현합니다.

### 📚 참조 문서

- `/docs/plan/implementation-plan.md` (현재 애그리거트의 Application Layer 섹션)
- **[애그리거트명] 도메인 레이어 명세서** (`/docs/spec/[애그리거트명소문자]-domain-layer-spec.md`)
- `/docs/guide/2-coding-standards.md`
- `/docs/guide/3-2-application-tdd.md`
- `/docs/guide/4-2-application-templates.md`
- **의존하는 애그리거트의 인터페이스 문서** (`/docs/aggregate_spec/[의존애그리거트명]-interface.md`) ※ 해당 애그리거트가 완료된 경우만

### 🔄 구현 프로세스

(계획수립 → 테스트시나리오 → 테스트코드 → 구현 → 리뷰)

### ✅ 완료 기준

- [ ] `/docs/plan/implementation-plan.md`에 명시된 Query/Projection 구현
- [ ] **[애그리거트명] Application 레이어 명세서 작성** (`/docs/spec/[애그리거트명소문자]-application-layer-spec.md`) ⚠️

---

## Adapter 레이어 구현 (Phase 7-8)

### Phase 7: [애그리거트명] Inbound Adapter 구현

### 🎯 목표

`/docs/plan/implementation-plan.md`에 정의된 현재 애그리거트의 Inbound Adapter를 구현합니다.

### 📚 참조 문서

- `/docs/plan/implementation-plan.md` (현재 애그리거트의 Adapter Layer 섹션)
- **[애그리거트명] Application 레이어 명세서** (`/docs/spec/[애그리거트명소문자]-application-layer-spec.md`)
- `/docs/guide/3-3-adapter-tdd.md`
- `/docs/guide/4-3-adapter-templates.md`
- **의존하는 애그리거트의 인터페이스 문서** (`/docs/aggregate_spec/[의존애그리거트명]-interface.md`) ※ 해당 애그리거트가 완료된 경우만

### 🏗️ 패키지 구조
현재 애그리거트의 어댑터 레이어 구현 시 다음 패키지 구조를 따라야 합니다:
```
com.dx.hexacore.security.{애그리거트명소문자}.adapter/
├── inbound/
│   ├── web/            # REST Controllers
│   │   └── dto/        # Request/Response DTOs
│   ├── event/          # Event Listeners
│   ├── filter/         # Security Filters
│   └── config/         # Inbound Configuration
└── outbound/
    ├── persistence/    # JPA Adapters
    │   ├── entity/     # JPA Entities
    │   └── repository/ # JPA Repositories
    ├── external/       # External Service Adapters
    │   └── dto/        # External DTOs
    ├── event/          # Event Publishers
    └── cache/          # Cache Adapters
```

### 🔄 구현 프로세스

(계획수립 → 테스트시나리오 → 테스트코드 → 구현 → 리뷰)

### ✅ 완료 기준

- [ ] `/docs/plan/implementation-plan.md`에 명시된 Inbound Adapter 구현
- [ ] 통합 테스트 작성

---

### Phase 8: [애그리거트명] Outbound Adapter 구현

### 🎯 목표

`/docs/plan/implementation-plan.md`에 정의된 현재 애그리거트의 Outbound Adapter를 구현합니다.

### 📚 참조 문서

- `/docs/plan/implementation-plan.md` (현재 애그리거트의 Adapter Layer 섹션)
- **[애그리거트명] Application 레이어 명세서** (`/docs/spec/[애그리거트명소문자]-application-layer-spec.md`)
- `/docs/guide/4-3-adapter-templates.md`
- **의존하는 애그리거트의 인터페이스 문서** (`/docs/aggregate_spec/[의존애그리거트명]-interface.md`) ※ 해당 애그리거트가 완료된 경우만

### 🔄 구현 프로세스

1. **구현 계획 수립** ~ 5. **리뷰** (동일한 프로세스)

6. **애그리거트 인터페이스 문서 작성**
   - 파일명: `/docs/aggregate_spec/[애그리거트명소문자]-interface.md`
   - 다른 애그리거트가 참조할 수 있는 공개 인터페이스 명세
   - 구현 세부사항은 제외하고 사용 방법만 포함

### ✅ 완료 기준

- [ ] `/docs/plan/implementation-plan.md`에 명시된 Outbound Adapter 구현
- [ ] **[애그리거트명] Adapter 레이어 명세서 작성** (`/docs/spec/[애그리거트명소문자]-adapter-layer-spec.md`) ⚠️
- [ ] **[애그리거트명] 애그리거트 인터페이스 문서 작성** (`/docs/aggregate_spec/[애그리거트명소문자]-interface.md`) ⚠️
- [ ] **[애그리거트명] 전체 구현 완료** ✅

> `/docs/plan/implementation-plan.md`의 다음 애그리거트가 있다면 Phase 2로 돌아가서 반복

---

## 통합 및 마무리 (Phase 9-10)

### Phase 9: 통합 및 설정

### 🎯 목표

모든 애그리거트를 통합하고 전체 시스템 설정을 완료합니다.

### 📚 참조 문서

- `/docs/plan/implementation-plan.md`
- **모든 애그리거트별 레이어 명세서** (`/docs/spec/` 디렉토리 참조)
- **모든 애그리거트 인터페이스 문서** (`/docs/aggregate_spec/` 디렉토리 참조)
- `docs/1-context.md`

### ✅ 완료 기준

- [ ] 모든 설정 완료
- [ ] 통합 테스트 전체 실행

---

### Phase 10: 최종 검증 및 문서화

### 🎯 목표

전체 시스템을 검증하고 필요한 문서를 작성합니다.

### ✅ 완료 기준

- [ ] 품질 검증 (테스트 커버리지 80% 이상)
- [ ] 문서화 완료
- [ ] 배포 준비 완료

---

## 📝 애그리거트별 명세서 작성 가이드

> **중요**: 모든 명세서는 `/docs/spec/` 디렉토리에 저장합니다.
> - 파일명 규칙: `[애그리거트명소문자]-[레이어명]-spec.md`
> - 예: `authentication-domain-layer-spec.md`

### [애그리거트명] 도메인 레이어 명세서 (Phase 4 완료 시)

```markdown
## [애그리거트명] 도메인 레이어 명세서
### Value Objects
- [VO명]: 속성, 검증 규칙, 주요 메서드
### Entities
- [Entity명]: 식별자, 속성, 비즈니스 메서드
### Aggregate Root
- [Aggregate명]: 불변 규칙, 도메인 이벤트
### Domain Events
- [Event명]: 발생 조건, 포함 데이터
### Domain Services
- [Service명]: 책임, 메서드 시그니처
```

### [애그리거트명] Application 레이어 명세서 (Phase 6 완료 시)

```markdown
## [애그리거트명] Application 레이어 명세서
### Commands
- [Command명]: 필드, 검증 규칙
### Use Cases
- [UseCase명]: 입력/출력, 흐름
### Ports
- Inbound: [Port명] - 메서드 시그니처
- Outbound: [Port명] - 메서드 시그니처
### Queries
- [Query명]: 파라미터, 반환 타입
```

### [애그리거트명] Adapter 레이어 명세서 (Phase 8 완료 시)

```markdown
## [애그리거트명] Adapter 레이어 명세서
### Inbound Adapters
- [Adapter명]: 엔드포인트, DTO
### Outbound Adapters
- [Adapter명]: 구현된 Port, 매핑 규칙
```

---

## 📝 애그리거트 인터페이스 문서 작성 가이드

> **중요**: Phase 8 완료 시 `/docs/aggregate_spec/` 디렉토리에 작성
> - 파일명 규칙: `[애그리거트명소문자]-interface.md`
> - 다른 애그리거트 개발 시 이 문서만 참조
> - 코드나 기존 명세서를 보지 않고 개발 가능하도록 작성

### 애그리거트 인터페이스 문서 템플릿

```markdown
# [애그리거트명] Aggregate Interface Specification

## 1. Aggregate Overview
- **Bounded Context**: [컨텍스트명]
- **Aggregate Root**: [클래스명]
- **핵심 책임**: [애그리거트의 주요 책임]
- **패키지 구조**: com.dx.hexacore.security.[애그리거트명소문자]

## 2. Public Domain Interface

### Aggregate Root
```java
public class [AggregateRoot] {
    // 생성자/팩토리 메서드
    public static [AggregateRoot] create(...) { }
    
    // 주요 비즈니스 메서드
    public void doSomething(...) { }
    
    // 조회 메서드
    public [Type] getSomething() { }
}
```

### Value Objects (외부 참조 가능한 것만)
```java
public class [ValueObject] {
    public static [ValueObject] of(...) { }
    // 주요 메서드 시그니처
}
```

### Domain Events
```java
public record [DomainEvent](
    String aggregateId,
    // 이벤트 데이터
) implements DomainEvent { }
```

## 3. Application Services Interface

### Commands & Handlers
```java
// Command
public record [Command](/* fields */) { }

// Handler Interface
public interface [CommandHandler] {
    void handle([Command] command);
}
```

### Queries & Results
```java
// Query
public record [Query](/* parameters */) { }

// Result
public record [QueryResult](/* fields */) { }

// Handler Interface
public interface [QueryHandler] {
    [QueryResult] handle([Query] query);
}
```

### Port Interfaces
```java
// Inbound Port (Application Service)
public interface [UseCase] {
    void execute([Input] input);
}

// Outbound Port (Repository/Gateway)
public interface [Repository] {
    [Aggregate] findById([Id] id);
    void save([Aggregate] aggregate);
}
```

## 4. REST API Endpoints
```yaml
# Command Endpoints
POST   /api/[aggregate-plural]          # Create
PUT    /api/[aggregate-plural]/{id}     # Update
DELETE /api/[aggregate-plural]/{id}     # Delete

# Query Endpoints  
GET    /api/[aggregate-plural]/{id}     # Get by ID
GET    /api/[aggregate-plural]          # List/Search
```

## 5. Integration Points

### Events Published
- `[EventName]`: 발생 조건, 구독자가 필요한 데이터

### Events Subscribed
- `[ExternalEventName]`: 처리 방식

### External Dependencies
- `[AggregateB]Repository`: [용도]
- `[ExternalService]`: [용도]

## 6. Usage Examples

### Creating Aggregate
```java
// Via Application Service
var command = new Create[Aggregate]Command(...);
[useCase].execute(command);

// Via REST API
POST /api/[aggregates]
{
    "field1": "value1",
    "field2": "value2"
}
```

### Querying Aggregate
```java
// Via Query Handler
var query = new Get[Aggregate]Query(id);
var result = queryHandler.handle(query);

// Via REST API
GET /api/[aggregates]/{id}
```

## 7. Important Constraints
- [불변 규칙 1]
- [불변 규칙 2]
- [동시성 제약사항]
- [성능 고려사항]
```