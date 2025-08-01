# Adapter Layer TDD 구현 필수 지침

## 🎯 Adapter Layer에서도 TDD 적용
```
Adapter Layer도 RED → GREEN → REFACTOR 사이클을 준수합니다.
외부 기술과의 통합이지만, 테스트가 설계를 주도해야 합니다.
```

## 📋 Adapter Layer TDD 구현 순서

### 1단계: 테스트 시나리오 작성
```markdown
위치: /testscenario/adapter/{type}/{adapter}/테스트목록.md

예시:
/testscenario/adapter/filter/JwtAuthenticationFilter/테스트목록.md
- [ ] 유효한 JWT 토큰 → 인증 성공
- [ ] 만료된 토큰 → 인증 실패
- [ ] 잘못된 형식의 토큰 → 인증 실패
- [ ] 토큰 없음 → 필터 통과
- [ ] Bearer 키워드 없음 → 필터 통과
```

## 🔴 RED - Security Filter 테스트 작성

### Step 1: 첫 번째 실패하는 테스트
```java
class JwtAuthenticationFilterTest {
    
    @Mock
    private TokenManagementUseCase tokenManagementUseCase;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    @Mock
    private FilterChain filterChain;
    
    @Test
    @DisplayName("유효한 JWT 토큰을 가진 요청은 인증된다")
    void shouldAuthenticateValidToken() throws Exception {
        // Given
        String validToken = "valid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        
        // When
        filter.doFilterInternal(request, response, filterChain);
        
        // Then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }
}
```
**컴파일 에러 ✓** - JwtAuthenticationFilter 없음

## 🟢 GREEN - 최소 구현

### Step 2: 컴파일 통과하는 최소 코드
```java
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            var auth = new UsernamePasswordAuthenticationToken("user", null);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        
        filterChain.doFilter(request, response);
    }
}
```
**테스트 통과 ✅**

## 🔵 REFACTOR - 구조 개선

### Step 3: 리팩토링
```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    
    private final TokenManagementUseCase tokenManagementUseCase;
    
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        
        try {
            String token = extractToken(request);
            
            if (token != null) {
                var validationResult = tokenManagementUseCase.validateToken(
                    ValidateTokenCommand.of(token)
                );
                
                if (validationResult.isValid()) {
                    setAuthentication(validationResult, request);
                }
            }
            
            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            log.error("JWT authentication failed", e);
            filterChain.doFilter(request, response);
        }
    }
    
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
```

## 🔴 RED - Persistence Adapter 테스트

### Step 1: 실패하는 테스트 작성
```java
@DataJpaTest
class ClientCredentialsPersistenceAdapterTest {
    
    @Test
    @DisplayName("ClientCredentials를 저장할 수 있다")
    void shouldSaveClientCredentials() {
        // Given
        var adapter = new ClientCredentialsPersistenceAdapter();
        var credentials = ClientCredentials.createNew("client123", "secret");
        
        // When
        adapter.save(credentials);
        
        // Then
        var saved = adapter.loadById(ClientId.of("client123"));
        assertThat(saved).isPresent();
    }
}
```
**컴파일 에러 ✓** - Adapter 클래스 없음

## 🟢 GREEN - 최소 구현

### Step 2: 테스트 통과하는 최소 코드
```java
@Component
public class ClientCredentialsPersistenceAdapter 
    implements SaveClientCredentialsPort, LoadClientCredentialsPort {
    
    private Map<String, ClientCredentials> storage = new HashMap<>();
    
    @Override
    public void save(ClientCredentials credentials) {
        storage.put(credentials.getId().getValue(), credentials);
    }
    
    @Override
    public Optional<ClientCredentials> loadById(ClientId id) {
        return Optional.ofNullable(storage.get(id.getValue()));
    }
}
```
**테스트 통과 ✅**

## 🔵 REFACTOR - JPA 구현으로 변경

### Step 3: 실제 JPA 구현
```java
@Component
@RequiredArgsConstructor
public class ClientCredentialsPersistenceAdapter 
    implements SaveClientCredentialsPort, LoadClientCredentialsPort {
    
    private final ClientCredentialsJpaRepository repository;
    private final ClientCredentialsMapper mapper;
    
    @Override
    @Transactional
    public void save(ClientCredentials credentials) {
        ClientCredentialsEntity entity = mapper.toEntity(credentials);
        repository.save(entity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ClientCredentials> loadById(ClientId id) {
        return repository.findById(id.getValue())
            .map(mapper::toDomain);
    }
}
```

## 📊 각 Adapter 유형별 TDD 적용

### Security Filter TDD
```
1. RED: Filter 테스트로 보안 요구사항 검증
2. GREEN: 최소 인증 로직으로 통과
3. REFACTOR: Use Case 주입, 예외 처리
```

### Persistence Adapter TDD
```
1. RED: @DataJpaTest로 저장/조회 테스트 작성
2. GREEN: 메모리 Map으로 구현
3. REFACTOR: JPA Repository로 교체
```

### External Adapter TDD
```
1. RED: WireMock으로 외부 API 테스트 작성
2. GREEN: 하드코딩된 응답
3. REFACTOR: 실제 HTTP 클라이언트 구현
```

### Security Filter TDD
```
1. RED: MockHttpServletRequest로 필터 테스트
2. GREEN: 최소 인증 로직
3. REFACTOR: 완전한 보안 검증
```

## 🔄 Adapter Layer TDD 리듬

### 각 기능별 사이클
```bash
# 1. 테스트 작성 (RED)
# AuthenticationControllerTest에 새 시나리오 추가

# 2. 실행 - 실패 확인
./gradlew test --tests "AuthenticationControllerTest.shouldReturn400ForInvalidRequest"

# 3. 최소 구현 (GREEN)
# 하드코딩이라도 테스트 통과시킴

# 4. 테스트 재실행 - 통과 확인
./gradlew test

# 5. 리팩토링 (REFACTOR)
# 구조 개선, 중복 제거

# 6. 모든 테스트 확인
./gradlew test

# 7. 커밋
git commit -m "test: Add 400 response test for invalid request"
git commit -m "feat: Handle invalid request with 400 response"
git commit -m "refactor: Extract request validation logic"
```

## 🎯 Adapter별 TDD 체크리스트

### 매 사이클마다
```
□ 실패하는 테스트 먼저 작성
□ 컴파일 에러 또는 테스트 실패 확인
□ 최소 코드로 테스트 통과
□ 모든 테스트 여전히 통과
□ 리팩토링 필요성 검토
□ 구조/기능 변경 분리
```

### Web Adapter 특화
```
□ 각 HTTP 상태 코드별 테스트
□ Request 검증 테스트
□ Response 형식 테스트
□ 예외 처리 테스트
```

### Persistence Adapter 특화
```
□ CRUD 각 작업별 테스트
□ 매핑 로직 테스트
□ 트랜잭션 롤백 테스트
□ 동시성 테스트 (필요시)
```

## 🚫 Adapter Layer TDD 안티패턴

### 잘못된 접근
```java
// ❌ 테스트 없이 전체 구현
@RestController
public class AuthController {
    // 복잡한 전체 구현을 한 번에...
    // 나중에 테스트 작성
}

// ❌ 통합 테스트만 작성
@SpringBootTest  // 전체 컨텍스트 로드
class AuthIntegrationTest {
    // 단위 테스트 없이 통합 테스트만
}

// ❌ 구현 세부사항 테스트
@Test
void shouldCallMapperExactlyOnce() {
    // 내부 구현 방식을 테스트
    verify(mapper, times(1)).toCommand(any());
}
```

### 올바른 접근
```java
// ✅ 행위 중심 테스트
@Test
void shouldReturn200ForValidRequest() {
    // 입력과 출력에 집중
}

// ✅ 단위 테스트 우선
@WebMvcTest  // 해당 컨트롤러만 테스트
class AuthControllerTest {
    // 빠른 피드백
}

// ✅ 점진적 구현
// 1. 단순 응답 → 2. 검증 추가 → 3. Use Case 연결
```

## ✅ Adapter Layer TDD 완료 기준

### 각 Adapter별
```
□ 모든 시나리오 테스트 작성
□ RED → GREEN → REFACTOR 준수
□ 커버리지 70% 이상
□ Mock 활용 적절
□ 통합 테스트 보완
```

### 전체 완료 시
```
□ 모든 Port 구현체 완성
□ 외부 의존성 격리
□ 실패 시나리오 처리
□ 성능 최적화 적용
```