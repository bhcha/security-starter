# 🔧 Security Starter 영속성 레이어 제거 리팩토링 계획

## 📋 개요
Security Starter를 순수한 인프라스트럭처 레이어 라이브러리로 전환하기 위해 불필요한 영속성 관련 코드를 제거하고 아키텍처를 단순화합니다.

## 🎯 목표
1. **도메인 레이어 순수성 확보**: JPA 의존성 완전 제거
2. **불필요한 복잡성 제거**: Repository 패턴, 영속성 어댑터 제거
3. **본질적 기능에 집중**: 토큰 검증과 설정 관리만 유지
4. **Zero Configuration 원칙 강화**: 데이터베이스 설정 없이 즉시 사용 가능

## 🏗️ 현재 vs 목표 아키텍처

### 현재 아키텍처 (불필요하게 복잡)
```
Domain Layer
├── Value Objects (@Embeddable 포함 ❌)
├── Entities (영속성 고려)
└── Domain Events (저장 이벤트)

Application Layer
├── Use Cases
├── Port In/Out
└── Repository Interfaces (불필요)

Adapter Layer
├── JPA Adapters (불필요)
├── InMemory Adapters (불필요)
├── Token Providers (유지)
└── REST Controllers (유지)
```

### 목표 아키텍처 (단순하고 명확)
```
Domain Layer
├── Value Objects (순수 Java)
└── Validation Rules

Application Layer
├── Token Services (검증/발급)
└── Security Filters

Infrastructure Layer
├── Token Providers (Keycloak/JWT/NoOp)
├── REST Controllers (옵션)
└── Configuration
```

## 📝 Phase별 작업 계획

### Phase 1: 도메인 레이어 정리 (최우선)
**목표**: 도메인 객체의 순수성 확보

#### 1.1 JPA 어노테이션 제거
- [ ] `Token.java`에서 `@Embeddable` 제거
- [ ] `Credentials.java`에서 `@Embeddable` 제거
- [ ] `AuthenticationStatus.java`에서 `@Embeddable` 제거
- [ ] 각 VO의 `protected` 기본 생성자 제거
- [ ] `jakarta.persistence` import 문 제거

#### 1.2 도메인 이벤트 정리
- [ ] 영속성 관련 이벤트 제거 또는 용도 재정의
- [ ] 이벤트를 로깅/모니터링 용도로만 사용

### Phase 2: Repository 인터페이스 제거
**목표**: 불필요한 추상화 계층 제거

#### 2.1 Application Layer Port 정리
- [ ] `AuthenticationRepository` 인터페이스 제거
- [ ] `AuthenticationSessionRepository` 인터페이스 제거
- [ ] `LoadAuthenticationQueryPort` 제거
- [ ] `LoadTokenInfoQueryPort` 제거
- [ ] `LoadSessionStatusQueryPort` 제거
- [ ] `LoadFailedAttemptsQueryPort` 제거

#### 2.2 Use Case 단순화
- [ ] Repository 의존성을 제거한 Use Case 재구현
- [ ] TokenProvider만 사용하도록 수정
- [ ] 상태 저장 로직을 상태 검증 로직으로 변경

### Phase 3: Adapter Layer 정리
**목표**: 영속성 관련 어댑터 완전 제거

#### 3.1 JPA 관련 제거
- [ ] `/persistence/entity/` 디렉토리 전체 제거
  - `AuthenticationJpaEntity.java`
  - `TokenEntity.java`
  - `SessionJpaEntity.java`
  - `AuthenticationAttemptJpaEntity.java`
- [ ] `/persistence/repository/` 디렉토리 제거
- [ ] `/persistence/` 어댑터 클래스 제거
  - `AuthenticationJpaAdapter.java`
  - `SessionJpaAdapter.java`
  - Mapper 클래스들

#### 3.2 InMemory 어댑터 제거
- [ ] `/memory/` 디렉토리 전체 제거
  - `InMemoryAuthenticationCommandAdapter.java`
  - `InMemoryAuthenticationQueryAdapter.java`
  - `InMemorySessionAdapter.java`
  - Configuration 클래스들

### Phase 4: 설정 및 의존성 정리
**목표**: 프로젝트 설정 단순화

#### 4.1 Gradle 의존성 정리
- [ ] `spring-boot-starter-data-jpa` 의존성 제거
- [ ] H2 Database 의존성 제거
- [ ] Hibernate 관련 의존성 제거

#### 4.2 설정 파일 정리
- [ ] `application.yml`에서 datasource 설정 제거
- [ ] JPA/Hibernate 설정 제거
- [ ] 영속성 관련 프로파일 제거

#### 4.3 AutoConfiguration 정리
- [ ] `PersistenceAutoConfiguration.java` 제거 또는 전면 재작성
- [ ] JPA 조건부 설정 로직 제거
- [ ] 단순한 TokenProvider 설정만 유지

### Phase 5: 새로운 구조 구현
**목표**: 단순하고 명확한 보안 인프라 제공

#### 5.1 핵심 서비스 구현
```java
// 새로운 TokenService (Repository 없음)
@Service
public class TokenService {
    private final TokenProvider tokenProvider;

    public Token issueToken(String username, String password) {
        // 검증 후 즉시 토큰 발급
        Credentials credentials = Credentials.of(username, password);
        return tokenProvider.issueToken(credentials);
    }

    public boolean validateToken(String token) {
        // 토큰 자체 검증 (저장소 조회 없음)
        return tokenProvider.validateToken(token).isValid();
    }
}
```

#### 5.2 Security Filter 단순화
- [ ] 토큰 검증 필터를 순수 검증 로직으로 수정
- [ ] 세션 관리를 토큰 기반으로 전환
- [ ] 상태 저장 없이 stateless 처리

### Phase 6: 테스트 코드 수정
**목표**: 새로운 구조에 맞는 테스트

#### 6.1 단위 테스트 수정
- [ ] Repository 관련 테스트 제거
- [ ] 영속성 관련 테스트 제거
- [ ] 순수 검증 로직 테스트로 대체

#### 6.2 통합 테스트 수정
- [ ] `@DataJpaTest` 어노테이션 제거
- [ ] `@SpringBootTest`를 경량화
- [ ] 데이터베이스 없이 동작 확인

### Phase 7: 문서화 및 마무리
**목표**: 변경사항 문서화 및 검증

#### 7.1 문서 업데이트
- [ ] README.md 업데이트 (새로운 아키텍처 설명)
- [ ] 설정 가이드 수정
- [ ] API 문서 업데이트

#### 7.2 최종 검증
- [ ] 모든 JPA 의존성 제거 확인
- [ ] Zero Configuration 동작 확인
- [ ] 메모리 사용량 감소 확인
- [ ] 시작 시간 단축 확인

## 🚀 실행 전략

### 우선순위
1. **긴급**: Phase 1 (도메인 레이어 JPA 제거)
2. **높음**: Phase 2-3 (Repository 및 Adapter 제거)
3. **중간**: Phase 4-5 (새 구조 구현)
4. **낮음**: Phase 6-7 (테스트 및 문서화)

### 리스크 관리
- 각 Phase별 별도 브랜치에서 작업
- Phase 완료 시 통합 테스트 수행
- 기존 기능 손상 없음 확인

### 예상 효과
- **코드량 감소**: 약 40-50% 코드 제거 예상
- **복잡도 감소**: 레이어 단순화로 유지보수성 향상
- **성능 향상**: 불필요한 영속성 작업 제거
- **의존성 감소**: JPA, H2 등 무거운 의존성 제거

## 📊 성공 지표
- [ ] JPA 의존성 완전 제거
- [ ] 애플리케이션 시작 시간 50% 단축
- [ ] 메모리 사용량 30% 감소
- [ ] 코드 라인 수 40% 감소
- [ ] Zero Configuration으로 즉시 사용 가능

## 🔄 롤백 계획
만약 문제 발생 시:
1. Git 이전 커밋으로 복원
2. 제거한 코드를 별도 아카이브 브랜치에 보관
3. 단계적 제거로 위험 최소화

---

**작성일**: 2024-01-13
**작성자**: Security Starter Team
**상태**: 계획 수립 완료