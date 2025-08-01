# Authentication Phase 8: Outbound Adapter 테스트 시나리오

## 1. JPA Persistence Adapter 테스트 시나리오

### 1.1 AuthenticationJpaAdapter - Save 시나리오
**목적**: Authentication 애그리거트를 영속화하는 기능 검증

#### 정상 케이스
1. **새로운 Authentication 저장**
   - Given: 유효한 Authentication 애그리거트
   - When: save() 메서드 호출
   - Then: DB에 저장되고 저장된 엔티티 반환

2. **기존 Authentication 업데이트**
   - Given: 이미 저장된 Authentication의 변경된 상태
   - When: save() 메서드 호출
   - Then: DB에 업데이트되고 변경사항 반영

#### 예외 케이스
3. **null Authentication 저장 시도**
   - Given: null Authentication
   - When: save() 메서드 호출
   - Then: IllegalArgumentException 발생

4. **DB 연결 실패**
   - Given: DB 연결 불가 상황
   - When: save() 메서드 호출
   - Then: DataAccessException 발생

### 1.2 AuthenticationJpaAdapter - FindById 시나리오
**목적**: ID로 Authentication 조회 기능 검증

#### 정상 케이스
5. **존재하는 ID로 조회**
   - Given: DB에 저장된 Authentication ID
   - When: findById() 메서드 호출
   - Then: Optional.of(Authentication) 반환

6. **존재하지 않는 ID로 조회**
   - Given: DB에 없는 Authentication ID
   - When: findById() 메서드 호출
   - Then: Optional.empty() 반환

#### 예외 케이스
7. **null ID로 조회**
   - Given: null ID
   - When: findById() 메서드 호출
   - Then: IllegalArgumentException 발생

### 1.3 AuthenticationJpaAdapter - Query Port 시나리오
**목적**: 읽기 전용 쿼리 포트 기능 검증

#### 정상 케이스
8. **ID로 AuthenticationProjection 조회**
   - Given: 저장된 Authentication ID
   - When: loadById() 메서드 호출
   - Then: Optional.of(AuthenticationProjection) 반환

9. **토큰으로 TokenInfoProjection 조회**
   - Given: 유효한 토큰 문자열
   - When: loadByToken() 메서드 호출
   - Then: Optional.of(TokenInfoProjection) 반환

#### 예외 케이스
10. **만료된 토큰으로 조회**
    - Given: 만료된 토큰
    - When: loadByToken() 메서드 호출
    - Then: TokenInfoProjection의 isValid가 false

### 1.4 JPA Entity 매핑 시나리오
**목적**: Domain 객체와 JPA Entity 간 변환 검증

#### 정상 케이스
11. **Domain to Entity 변환**
    - Given: 완전한 Authentication 도메인 객체
    - When: toEntity() 메서드 호출
    - Then: 모든 필드가 정확히 매핑된 JPA Entity 반환

12. **Entity to Domain 변환**
    - Given: DB에서 조회한 JPA Entity
    - When: toDomain() 메서드 호출
    - Then: 동일한 상태의 Authentication 도메인 객체 반환

## 2. Event Publisher 테스트 시나리오

### 2.1 SpringEventPublisher - 단일 이벤트 발행
**목적**: 도메인 이벤트를 Spring 이벤트로 발행하는 기능 검증

#### 정상 케이스
13. **AuthenticationSucceeded 이벤트 발행**
    - Given: AuthenticationSucceeded 도메인 이벤트
    - When: publish() 메서드 호출
    - Then: Spring ApplicationEvent로 변환되어 발행됨

14. **AuthenticationFailed 이벤트 발행**
    - Given: AuthenticationFailed 도메인 이벤트
    - When: publish() 메서드 호출
    - Then: Spring ApplicationEvent로 변환되어 발행됨

#### 예외 케이스
15. **null 이벤트 발행 시도**
    - Given: null 이벤트
    - When: publish() 메서드 호출
    - Then: IllegalArgumentException 발생

### 2.2 SpringEventPublisher - 다중 이벤트 발행
**목적**: 여러 도메인 이벤트를 한 번에 발행하는 기능 검증

#### 정상 케이스
16. **여러 이벤트 동시 발행**
    - Given: 3개의 서로 다른 도메인 이벤트 리스트
    - When: publishAll() 메서드 호출
    - Then: 모든 이벤트가 순서대로 발행됨

17. **빈 리스트 발행**
    - Given: 빈 이벤트 리스트
    - When: publishAll() 메서드 호출
    - Then: 아무 이벤트도 발행되지 않음 (정상 처리)

#### 예외 케이스
18. **리스트 내 null 이벤트 포함**
    - Given: null을 포함한 이벤트 리스트
    - When: publishAll() 메서드 호출
    - Then: IllegalArgumentException 발생

### 2.3 이벤트 리스너 통합 시나리오
**목적**: 발행된 이벤트가 리스너에 전달되는지 검증

#### 정상 케이스
19. **이벤트 리스너 수신 확인**
    - Given: 테스트용 이벤트 리스너 등록
    - When: 도메인 이벤트 발행
    - Then: 리스너가 이벤트를 수신하고 처리

20. **비동기 이벤트 처리**
    - Given: @Async 리스너 설정
    - When: 도메인 이벤트 발행
    - Then: 별도 스레드에서 이벤트 처리됨

## 3. Keycloak Integration 테스트 시나리오

### 3.1 KeycloakAuthenticationAdapter - 인증
**목적**: Keycloak을 통한 사용자 인증 기능 검증

#### 정상 케이스
21. **유효한 자격증명으로 인증**
    - Given: 올바른 username/password
    - When: authenticate() 메서드 호출
    - Then: ExternalAuthResult.success()와 토큰 반환

22. **리프레시 토큰으로 갱신**
    - Given: 유효한 리프레시 토큰
    - When: refreshToken() 메서드 호출
    - Then: 새로운 액세스 토큰 반환

#### 예외 케이스
23. **잘못된 자격증명으로 인증**
    - Given: 틀린 username/password
    - When: authenticate() 메서드 호출
    - Then: ExternalAuthResult.failed() 반환

24. **Keycloak 서버 다운**
    - Given: Keycloak 연결 불가
    - When: authenticate() 메서드 호출
    - Then: ExternalAuthException 발생

### 3.2 KeycloakAuthenticationAdapter - 토큰 검증
**목적**: Keycloak 토큰 검증 기능 확인

#### 정상 케이스
25. **유효한 토큰 검증**
    - Given: Keycloak에서 발급한 유효한 토큰
    - When: validateToken() 메서드 호출
    - Then: TokenValidationResult.valid() 반환

26. **토큰 정보 조회**
    - Given: 유효한 액세스 토큰
    - When: introspectToken() 메서드 호출
    - Then: 토큰의 상세 정보 반환

#### 예외 케이스
27. **만료된 토큰 검증**
    - Given: 만료된 토큰
    - When: validateToken() 메서드 호출
    - Then: TokenValidationResult.expired() 반환

28. **변조된 토큰 검증**
    - Given: 서명이 잘못된 토큰
    - When: validateToken() 메서드 호출
    - Then: TokenValidationResult.invalid() 반환

### 3.3 Keycloak 설정 시나리오
**목적**: Keycloak 연동 설정의 조건부 활성화 검증

#### 정상 케이스
29. **Keycloak 라이브러리 존재 시 활성화**
    - Given: Keycloak 라이브러리가 클래스패스에 존재
    - When: 애플리케이션 시작
    - Then: KeycloakAuthenticationAdapter Bean 생성

30. **설정 프로퍼티 바인딩**
    - Given: application.yml의 Keycloak 설정
    - When: 애플리케이션 시작
    - Then: KeycloakProperties에 값이 바인딩됨

#### 예외 케이스
31. **Keycloak 라이브러리 없을 때**
    - Given: Keycloak 라이브러리 없음
    - When: 애플리케이션 시작
    - Then: KeycloakAuthenticationAdapter Bean 생성 안 됨

32. **필수 설정 누락**
    - Given: realm 설정 누락
    - When: 애플리케이션 시작
    - Then: BeanCreationException 발생

## 4. 통합 테스트 시나리오

### 4.1 전체 플로우 통합 테스트
**목적**: 모든 Outbound Adapter가 함께 동작하는지 검증

#### 정상 케이스
33. **인증 → 저장 → 이벤트 발행 플로우**
    - Given: 전체 시스템 구성
    - When: 인증 Use Case 실행
    - Then: Keycloak 인증 → JPA 저장 → 이벤트 발행 성공

34. **토큰 검증 → 조회 플로우**
    - Given: 저장된 인증 정보
    - When: 토큰 검증 Use Case 실행
    - Then: Keycloak 검증 → JPA 조회 성공

### 4.2 트랜잭션 시나리오
**목적**: 트랜잭션 경계와 롤백 동작 검증

#### 정상 케이스
35. **성공적인 트랜잭션 커밋**
    - Given: 정상적인 인증 플로우
    - When: 모든 단계 성공
    - Then: DB에 데이터 영속화

#### 예외 케이스
36. **중간 실패 시 롤백**
    - Given: 이벤트 발행 중 예외 발생
    - When: 트랜잭션 내에서 예외
    - Then: DB 변경사항 롤백됨

## 테스트 데이터 준비

### Mock 객체
- MockAuthenticationRepository
- MockEventPublisher
- MockKeycloakClient

### 테스트 픽스처
- 유효한 Authentication 객체
- 다양한 상태의 Token 객체
- 테스트용 Credentials

### 테스트 환경 설정
- @DataJpaTest: JPA 레이어 테스트
- @SpringBootTest: 통합 테스트
- WireMock: Keycloak API 모킹