# Authentication Phase 8: Outbound Adapter 구현 계획

## 🎯 목표
Authentication 애그리거트의 Outbound Adapter를 구현하여 외부 시스템과의 통합을 완성합니다.

## 📋 구현 범위

### 1. JPA Persistence Adapter (AuthenticationJpaAdapter)
- **책임**: Authentication 애그리거트의 영속성 관리
- **조건부 설정**: JPA가 있을 때만 활성화 (@ConditionalOnClass)
- **구현할 Port**: AuthenticationRepository, LoadAuthenticationQueryPort, LoadTokenInfoQueryPort

### 2. Event Publisher (AuthenticationEventPublisher)
- **책임**: 도메인 이벤트를 외부로 발행
- **이벤트 버스**: Spring ApplicationEventPublisher 기본 사용
- **구현할 Port**: EventPublisher

### 3. Keycloak Integration (KeycloakAdapter)
- **책임**: Keycloak과의 인증 연동
- **조건부 설정**: Keycloak 라이브러리가 있을 때만 활성화
- **구현할 Port**: ExternalAuthProvider

## 🔧 컴포넌트 목록

### JPA Persistence 구성요소
1. **Entity 클래스**
   - AuthenticationJpaEntity
   - TokenJpaEntity (임베디드)
   
2. **Repository 인터페이스**
   - AuthenticationJpaRepository (Spring Data JPA)
   
3. **Adapter 구현체**
   - AuthenticationJpaAdapter

4. **매퍼 클래스**
   - AuthenticationJpaMapper

### Event Publisher 구성요소
1. **Publisher 구현체**
   - SpringEventPublisher
   
2. **이벤트 래퍼**
   - DomainEventWrapper

### Keycloak Integration 구성요소
1. **Adapter 구현체**
   - KeycloakAuthenticationAdapter
   
2. **설정 클래스**
   - KeycloakConfig
   - KeycloakProperties

3. **DTO 클래스**
   - KeycloakTokenResponse
   - KeycloakErrorResponse

## 📝 구현 순서
1. JPA Persistence Adapter
2. Event Publisher
3. Keycloak Integration

## ✅ 완료 기준
- [ ] 모든 Outbound Port 구현체 완성
- [ ] 각 어댑터별 단위 테스트 작성 (80% 커버리지)
- [ ] 통합 테스트 작성
- [ ] 조건부 Bean 등록 설정 완료
- [ ] Authentication Adapter 레이어 명세서 작성
- [ ] Authentication 애그리거트 인터페이스 문서 작성

## 🏗️ 아키텍처 원칙
1. **헥사고날 아키텍처**: Port와 Adapter의 명확한 분리
2. **조건부 설정**: 필요한 라이브러리가 있을 때만 활성화
3. **인터페이스 기반**: 모든 외부 연동은 Port 인터페이스를 통해
4. **테스트 가능성**: 모든 어댑터는 독립적으로 테스트 가능

## 📊 예상 산출물
- 구현 파일: 약 15개
- 테스트 파일: 약 10개
- 테스트 케이스: 약 50개
- 문서: 2개 (명세서, 인터페이스 문서)