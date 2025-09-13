# Authentication Adapter 레이어 명세서

## 개요
Authentication Aggregate의 Adapter 레이어는 Application 레이어의 Port 인터페이스를 구현하여 외부 시스템과의 통합을 담당합니다.

## Inbound Adapters

### Event Listener
**위치**: `com.ldx.hexacore.security.auth.adapter.inbound.event`

#### AuthenticationEventListener
- **책임**: 인증 관련 이벤트 수신 및 처리
- **구독 이벤트**: AuthenticationAttempted, AuthenticationSucceeded, AuthenticationFailed

## Outbound Adapters

### JPA Persistence Adapter
**위치**: `com.ldx.hexacore.security.auth.adapter.outbound.persistence`

#### AuthenticationJpaAdapter
- **구현 Port**: AuthenticationRepository, LoadAuthenticationQueryPort, LoadTokenInfoQueryPort
- **책임**: Authentication Aggregate의 영속성 관리

##### 주요 기능
- Authentication 저장/조회
- Token 정보로 Authentication 조회
- 트랜잭션 관리

#### Entity 구조
```java
@Entity
@Table(name = "authentication")
public class AuthenticationJpaEntity {
    @Id private String id;
    private String username;
    @Enumerated(EnumType.STRING) private AuthenticationStatusEntity status;
    @Embedded private TokenEntity token;
    private LocalDateTime attemptTime;
    private LocalDateTime successTime;
    @Version private Long version;
}
```

### Event Publisher
**위치**: `com.ldx.hexacore.security.auth.adapter.outbound.event`

#### SpringEventPublisher
- **구현 Port**: EventPublisher
- **책임**: 도메인 이벤트를 Spring ApplicationEvent로 발행

##### 발행 이벤트
- AuthenticationAttempted
- AuthenticationSucceeded
- AuthenticationFailed
- TokenRefreshed
- SessionTerminated

### External Authentication Provider
**위치**: `com.ldx.hexacore.security.auth.adapter.outbound.external`

#### KeycloakAuthenticationAdapter
- **구현 Port**: ExternalAuthProvider
- **책임**: Keycloak을 통한 외부 인증 처리

##### 주요 기능
- 사용자 인증
- 토큰 갱신
- 토큰 검증
- 세션 종료

##### 설정
```yaml
security:
  auth:
    keycloak:
      enabled: true
      server-url: ${KEYCLOAK_AUTH_SERVER_URL}
      realm: ${KEYCLOAK_REALM}
      client-id: ${KEYCLOAK_CLIENT}
      client-secret: ${KEYCLOAK_CLIENT_SECRET}
```

## 설정 및 활성화

### 조건부 Bean 등록
```java
@ConditionalOnProperty(name = "security.auth.keycloak.enabled", havingValue = "true")
public class KeycloakAuthenticationAdapter { }

@ConditionalOnProperty(name = "security.persistence.enabled", havingValue = "true", matchIfMissing = true)
public class AuthenticationJpaAdapter { }
```

### 통합 포인트
- **데이터베이스**: JPA/Hibernate를 통한 RDBMS 연동
- **이벤트 버스**: Spring ApplicationEventPublisher
- **외부 인증**: Keycloak OAuth2/OIDC

## 예외 처리

도메인 예외는 라이브러리를 사용하는 애플리케이션에서 처리해야 합니다.

### 발생 가능한 예외
- `InvalidCredentialsException`: 잘못된 인증 정보
- `TokenExpiredException`: 만료된 토큰
- `InvalidTokenException`: 유효하지 않은 토큰
- `SessionNotFoundException`: 세션을 찾을 수 없음
- `ExternalAuthException`: 외부 인증 서비스 오류

## 보안 고려사항
- 민감한 정보 로깅 방지 (토큰, 비밀번호 마스킹)
- 토큰 저장 시 암호화 고려
- SQL Injection 방지 (JPA 사용)

## 성능 최적화
- JPA 읽기 전용 트랜잭션 사용
- 연결 풀 설정 최적화
- 캐싱 전략 (추후 구현 예정)
- 비동기 이벤트 처리 (추후 구현 예정)