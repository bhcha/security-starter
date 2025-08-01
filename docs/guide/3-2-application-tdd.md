# Application Layer TDD 구현 필수 지침

## 🎯 Application Layer 핵심 원칙
```
1. 비즈니스 흐름 조정만 담당 (Orchestration)
2. 도메인 로직은 도메인 객체에 위임
3. 트랜잭션 경계 설정
4. 포트를 통한 외부 통신
```

## 📋 구현 순서 (절대 준수)

### 1단계: Use Case 식별 및 테스트 시나리오 작성
```markdown
위치: /testscenario/application/{usecase}/테스트목록.md

예시:
/testscenario/application/AuthenticateUseCase/테스트목록.md
- [ ] 유효한 자격증명으로 인증 성공
- [ ] 존재하지 않는 클라이언트 인증 실패
- [ ] 잘못된 비밀번호로 인증 실패
- [ ] 잠긴 계정으로 인증 시도
- [ ] 인증 성공 시 세션 저장
- [ ] 인증 시도 이벤트 발행
- [ ] 5회 실패 시 계정 잠금 이벤트 발행
```

### 2단계: 구현 순서
```
1. Command/Query 객체 → 2. Use Case 인터페이스 → 3. Port 인터페이스 → 4. Handler 구현
```

## 🔴 Command Handler 테스트 작성

### 필수 테스트 구조
```java
@ExtendWith(MockitoExtension.class)
class AuthenticationCommandHandlerTest {
    
    @Mock private LoadClientCredentialsPort loadPort;
    @Mock private SaveAuthenticationSessionPort savePort;
    @Mock private PublishDomainEventPort eventPort;
    
    @InjectMocks private AuthenticationCommandHandler handler;
    
    @Test
    @DisplayName("유효한 자격증명으로 인증 성공 시 토큰을 반환한다")
    void shouldReturnTokenWhenAuthenticationSucceeds() {
        // Given - Mock 설정
        var command = new AuthenticateCommand("client123", "secret");
        var credentials = ClientCredentials.createNew("client123", "secret");
        var session = AuthenticationSession.create(/* ... */);
        
        when(loadPort.loadByIdOrThrow(any())).thenReturn(credentials);
        
        // When - 실행
        var response = handler.authenticate(command);
        
        // Then - 검증
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getToken()).isNotNull();
        
        // 상호작용 검증
        verify(savePort).save(any(AuthenticationSession.class));
        verify(eventPort).publishAll(anyList());
    }
}
```

### Command Handler 테스트 체크리스트
```
필수 시나리오:
□ Happy Path (정상 흐름)
□ 도메인 예외 처리
□ 외부 시스템 실패
□ 트랜잭션 롤백
□ 이벤트 발행 검증

Mock 검증:
□ Port 호출 횟수
□ Port 호출 파라미터
□ 호출 순서 (필요시)
```

## 🟢 Command Handler 구현 패턴

### 표준 구현 구조
```java
@Component
@Transactional
public class AuthenticationCommandHandler implements AuthenticateUseCase {
    
    // 의존성 주입 (Port만)
    private final LoadClientCredentialsPort loadPort;
    private final SaveAuthenticationSessionPort savePort;
    private final PublishDomainEventPort eventPort;
    
    @Override
    public AuthenticateResponse authenticate(AuthenticateCommand command) {
        // 1. 전제 조건 검증 (Command 유효성)
        validateCommand(command);
        
        // 2. 도메인 객체 로드
        ClientCredentials credentials = loadPort
            .loadByIdOrThrow(ClientId.of(command.getClientId()));
        
        // 3. 도메인 로직 실행 (위임)
        AuthenticationResult result = credentials
            .authenticate(command.getSecret());
        
        // 4. 결과에 따른 처리
        if (result.isSuccess()) {
            // 세션 저장
            AuthenticationSession session = AuthenticationSession.create(
                credentials.getId(),
                result.getSessionId(),
                result.getToken()
            );
            savePort.save(session);
        }
        
        // 5. 이벤트 발행
        eventPort.publishAll(credentials.collectEvents());
        
        // 6. 응답 반환
        return AuthenticateResponse.from(result);
    }
}
```

### 구현 체크리스트
```
□ @Transactional 적용
□ Port 인터페이스만 의존
□ 도메인 로직은 도메인에 위임
□ 예외는 그대로 전파 (또는 변환)
□ 이벤트 수집 및 발행
```

## 🔵 Query Handler 테스트 작성

### Query Handler 테스트 구조
```java
@ExtendWith(MockitoExtension.class)
class SecurityMetricsQueryHandlerTest {
    
    @Mock private LoadSecurityMetricsPort loadPort;
    @Mock private SecurityMetricsMapper mapper;
    
    @InjectMocks private SecurityMetricsQueryHandler handler;
    
    @Test
    @DisplayName("기간별 보안 메트릭을 조회한다")
    void shouldReturnSecurityMetricsByPeriod() {
        // Given
        var query = GetSecurityMetricsQuery.builder()
            .fromDate(LocalDate.now().minusDays(7))
            .toDate(LocalDate.now())
            .build();
            
        var projections = List.of(
            new SecurityMetricsProjection(/* ... */)
        );
        
        when(loadPort.loadByPeriod(any(), any())).thenReturn(projections);
        
        // When
        var response = handler.getMetrics(query);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getMetrics()).hasSize(1);
    }
}
```

### Query Handler 테스트 체크리스트
```
필수 시나리오:
□ 정상 조회
□ 빈 결과 처리
□ 페이징 처리
□ 정렬 처리
□ 필터링 조합

성능 관련:
□ N+1 쿼리 방지
□ 페이지 크기 제한
□ 타임아웃 처리
```

## 📊 Application Layer 커버리지 기준

### 필수 커버리지
```
전체: 85% 이상

상세:
- Command Handler: 90% 이상
- Query Handler: 80% 이상  
- Event Handler: 85% 이상
- Application Service: 85% 이상
```

### 제외 가능 항목
```
- 단순 DTO (Getter/Setter만)
- Mapper 인터페이스 (자동 생성)
- 단순 설정 클래스
```

## 🚫 Application Layer 안티패턴

### 절대 금지
```java
// ❌ 도메인 로직 구현
public class AuthenticationCommandHandler {
    public AuthenticateResponse authenticate(command) {
        // 잘못됨: 도메인 로직을 Handler에 구현
        if (failedAttempts >= 5) {
            status = "LOCKED";
        }
    }
}

// ❌ 직접 Repository 접근
@Autowired
private ClientRepository repository;  // Port를 통해야 함

// ❌ 도메인 객체 노출
public ClientCredentials getClient() {  // DTO로 변환해야 함
    return credentials;
}
```

### 올바른 패턴
```java
// ✅ 도메인에 위임
AuthenticationResult result = credentials.authenticate(secret);

// ✅ Port를 통한 접근
ClientCredentials client = loadPort.loadById(id);

// ✅ DTO 변환
return ClientResponse.from(client);
```

## 🔧 Event Handler 구현

### Event Handler 테스트
```java
@Test
@DisplayName("인증 성공 이벤트 발생 시 프로젝션을 업데이트한다")
void shouldUpdateProjectionOnAuthenticationSucceeded() {
    // Given
    var event = new AuthenticationSucceeded(
        "client123",
        SessionId.generate(),
        Instant.now()
    );
    
    // When
    handler.on(event);
    
    // Then
    verify(projectionPort).updateLastLogin(
        eq("client123"),
        any(Instant.class)
    );
}
```

### Event Handler 구현
```java
@Component
public class AuthenticationProjectionEventHandler {
    
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(AuthenticationSucceeded event) {
        projectionPort.updateLastLogin(
            event.getClientId(),
            event.getOccurredAt()
        );
    }
}
```

## ✅ Application Layer 완료 체크리스트

### 각 Use Case별
```
□ Command/Query 객체 검증
□ Handler 구현 및 테스트
□ 모든 시나리오 커버
□ Port 인터페이스 정의
□ 트랜잭션 경계 설정
```

### 전체 완료 시
```
□ 커버리지 85% 이상
□ 모든 도메인 이벤트 처리
□ 예외 처리 전략 일관성
□ 응답 DTO 변환 완료
```