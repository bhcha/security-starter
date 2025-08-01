## 🎯 프로젝트 개요

### 프로젝트 메타 정보
```yaml
project:
  name: security-starter
  version: 1.0.0
  java: 21
  spring-boot: 3.5.x
  architecture: Hexagonal + CQRS
  
# 개발 규칙
rules:
  - "모든 public 메서드는 JavaDoc 필수"
  - "테스트 커버리지 80% 이상"
  - "순환 복잡도 10 미만"
  - "메서드 라인 수 20줄 이내"
  
# 브랜치 전략
git:
  main: 운영 배포 브랜치
  develop: 개발 통합 브랜치
  feature/*: 기능 개발 브랜치
  hotfix/*: 긴급 수정 브랜치
```

## 🚀 통합 기술 스택

| 계층                | 주요 기술                                                  | 용도                | 패키지 위치        |
| ----------------- | -------------------------------------------------------- | ----------------- |---------------|
| **Domain**        | Pure Java, Java Records                                  | 도메인 모델            | security-auth |
| **Security**      | Spring Security 6.x, Keycloak(SSO/OAuth2), JWT(Token 인증) | 인증/인가             | security-auth |
| **Persistence**   | Spring Data JPA 3.x, Hibernate 6.x, MySQL 8.x, H2(테스트용)  | 데이터 저장            | security-auth |
| **Caching**       | Caffeine                                                 | 성능 최적화            | security-auth |
| **Rate Limiting** | Bucket4j                                                 | API 제한            | security-auth |
| **Observability** | Micrometer, OpenTelemetry, Jaeger, Spring Boot Actuator  | 모니터링              | security-auth |
| **Testing**       | JUnit 5, Mockito, TestContainers, Spring Boot Test       | 테스트               | 각 모듈          |
| **Build/실행**      | Gradle 8.x, Spring Boot 3.4.x(최신)                        | 빌드/실행             | 루트 프로젝트       |
| **Mapping**       | MapStruct 1.5.x                                          | DTO-Entity 매핑     | security-auth |
| **Logging**       | SLF4J + Logback, MDC (Mapped Diagnostic Context)         | 구조화된 로깅, 추적 ID 관리 | security-auth |
| **Documentation** | Spring REST Docs, Ascii Docs                             | API 문서화           | security-auth |

## 🔧 프로젝트 표준 라이브러리


### 도메인 요구사항 (security-auth - 79개)
1. **인증(auth)**: 클라이언트 자격증명, 인증 세션, 토큰 생성
2. **접근제어(access)**: 접근 정책, Rate Limiting, IP 제한
3. **모니터링(monitoring)**: 보안 이벤트, 위험도 평가
4. **알림(alert)**: 보안 알림, 알림 실패 처리


## 🏛️ 아키텍처 원칙

### Hexagonal Architecture + CQRS
```
┌─────────────────────────────────────────────────────┐
│                    Adapters (In)                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │
│  │ Web Command │  │ Web Query   │  │Event Stream │  │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  │
│         │                 │                 │       │
│  ┌──────▼────────────────▼─────────────────▼──────┐ │
│  │              Application Layer                 │ │
│  │  ┌─────────────┐            ┌────────────────┐ │ │
│  │  │Command Side │            │ Query Side     │ │ │
│  │  │  Handlers   │            │  Handlers      │ │ │
│  │  └──────┬──────┘            └────────┬───────┘ │ │
│  │         │                             │        │ │
│  │  ┌──────▼─────────────────────────────▼──────┐ │ │
│  │  │              Domain Layer                 │ │ │
│  │  │  Aggregates, Events, Services, VOs        │ │ │
│  │  └───────────────────────────────────────────┘ │ │
│  └────────────────────────────────────────────────┘ │
│                                                     │
│                    Adapters (Out)                   │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │
│  │ Write Model │  │ Read Model  │  │  External   │  │
│  │ Persistence │  │ Persistence │  │  Services   │  │
│  └─────────────┘  └─────────────┘  └─────────────┘  │
└─────────────────────────────────────────────────────┘

                 Infrastructure (Starters)
┌─────────────────────────────────────────────────────┐
│  ┌─────────────┐  ┌─────────────────────────────┐   │
│  │ web-starter │  │ security-auth-starter       │   │
│  └─────────────┘  └─────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
```