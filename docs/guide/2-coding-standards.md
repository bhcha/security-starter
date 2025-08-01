## 🚫 금지 사항

### 코드 레벨
- `@Autowired` 필드 주입 (생성자 주입만 허용)
- `public` 필드 (테스트 제외)
- `null` 반환 (Optional 사용)
- checked exception (도메인 예외 제외)

### 아키텍처 레벨
- 계층 간 직접 참조
- 순환 의존성
- 도메인 로직의 외부 유출
- 기술 종속적 도메인 모델

### 테스트
- 우회코드 작성 절대금지
- 축소테스트 절대금지

## 🎯 핵심 원칙

### 도메인 주도 설계 원칙
1. **Rich Domain Model**: 비즈니스 로직은 도메인 객체 내부에 위치
2. **정적 팩토리 메서드**: 객체 생성의 의도를 명확히 표현
3. **불변성**: 가능한 모든 객체를 불변으로 설계
4. **자가 검증**: 객체는 생성 시점에 스스로를 검증

## 📏 코딩 표준

### 명명 규칙
| 구분 | 규칙 | 예시 |
|------|------|------|
| 패키지 | 소문자, 단수형 | `domain.member` |
| 클래스 | PascalCase | `MemberService` |
| 인터페이스 | 명사/형용사 | `MemberRepository` |
| 메서드 | camelCase, 동사 | `findByEmail()` |
| 상수 | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |

### 파일 구성 순서
1. package 선언
2. import 문 (정적 import 먼저)
3. 클래스/인터페이스 선언
4. 상수 (static final)
5. 필드 (private final 우선)
6. 생성자 (private 권장)
7. 정적 팩토리 메서드 (객체 생성의 주요 방법)
8. 비즈니스 메서드
9. private 헬퍼 메서드
10. equals/hashCode/toString

### 도메인 모델 패턴
- **Rich Domain Model**: 비즈니스 로직은 반드시 도메인 객체 내부에 구현
- **정적 팩토리 메서드**: 생성자 대신 의미 있는 이름의 정적 팩토리 메서드 사용
- **불변 객체**: 가능한 모든 도메인 객체는 불변으로 설계
- **자가 검증**: 도메인 객체는 생성 시점에 자신의 유효성 검증

## 📐 정적 팩토리 메서드 패턴

### 기본 규칙
```java
// ✅ 올바른 정적 팩토리 메서드 사용
public class ClientId {
    private final String value;
    
    // private 생성자 - 외부에서 직접 생성 불가
    private ClientId(String value) {
        validateId(value);
        this.value = value;
    }
    
    // 의미 있는 이름의 정적 팩토리 메서드
    public static ClientId of(String value) {
        return new ClientId(value);
    }
    
    public static ClientId generate() {
        return new ClientId(UUID.randomUUID().toString());
    }
    
    public static ClientId fromExternalSystem(String externalId) {
        return new ClientId("EXT_" + externalId);
    }
    
    private static void validateId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Client ID cannot be empty");
        }
        if (!value.matches("^[A-Za-z0-9_-]+$")) {
            throw new IllegalArgumentException("Client ID contains invalid characters");
        }
    }
}

// ❌ 잘못된 예시 - public 생성자
public class ClientId {
    private String value;
    
    public ClientId(String value) {  // public 생성자 노출
        this.value = value;
    }
    
    public void setValue(String value) {  // 가변성
        this.value = value;
    }
}
```

### 정적 팩토리 메서드 명명 규칙

| 메서드명 | 사용 시기 | 예시 |
|---------|----------|------|
| `of` | 단순한 타입 변환 | `ClientId.of("CLIENT_123")` |
| `from` | 타입 변환 + 약간의 처리 | `ClientSecret.from(rawPassword)` |
| `create` | 새 인스턴스 생성 | `AuthenticationSession.create(clientId)` |
| `generate` | 자동 생성 | `SessionId.generate()` |
| `reconstitute` | 영속성에서 복원 | `ClientCredentials.reconstitute(...)` |
| `parse` | 문자열 파싱 | `IpAddress.parse("192.168.1.1")` |
| `valueOf` | enum과 유사한 변환 | `ClientStatus.valueOf("ACTIVE")` |

## 🏛️ 도메인 모델 패턴

### 애그리게이트 루트
```java
@Entity
@AggregateRoot
public class ClientCredentials {
    @Id
    private final ClientId id;
    private final ClientSecret secret;
    private ClientStatus status;
    private int failedAttempts;
    private final List<DomainEvent> events = new ArrayList<>();
    
    // private 생성자
    private ClientCredentials(ClientId id, ClientSecret secret) {
        this.id = requireNonNull(id, "Client ID is required");
        this.secret = requireNonNull(secret, "Client secret is required");
        this.status = ClientStatus.ACTIVE;
        this.failedAttempts = 0;
    }
    
    // 정적 팩토리 메서드 - 새 클라이언트 생성
    public static ClientCredentials createNew(String clientId, String rawSecret) {
        ClientCredentials credentials = new ClientCredentials(
            ClientId.of(clientId),
            ClientSecret.create(rawSecret)
        );
        credentials.addEvent(new ClientCredentialsCreated(
            credentials.id,
            Instant.now()
        ));
        return credentials;
    }
    
    // 정적 팩토리 메서드 - DB에서 복원
    public static ClientCredentials reconstitute(
        ClientId id,
        ClientSecret secret,
        ClientStatus status,
        int failedAttempts
    ) {
        ClientCredentials credentials = new ClientCredentials(id, secret);
        credentials.status = status;
        credentials.failedAttempts = failedAttempts;
        return credentials;
    }
    
    // 비즈니스 메서드 - 행위를 표현하는 이름
    public AuthenticationResult authenticate(String providedSecret) {
        if (isLocked()) {
            addEvent(new ClientAuthenticationBlocked(id, Instant.now()));
            return AuthenticationResult.blocked();
        }
        
        if (!secret.matches(providedSecret)) {
            return handleFailedAuthentication();
        }
        
        return handleSuccessfulAuthentication();
    }
    
    public void rotateSecret(String newSecret) {
        if (isLocked()) {
            throw new ClientLockedException(id);
        }
        
        ClientSecret oldSecret = this.secret;
        this.secret = ClientSecret.create(newSecret);
        
        addEvent(new ClientSecretRotated(
            id,
            oldSecret.getMasked(),
            this.secret.getMasked(),
            Instant.now()
        ));
    }
    
    public void lock(String reason) {
        if (isLocked()) {
            return;  // 이미 잠김
        }
        
        this.status = ClientStatus.LOCKED;
        addEvent(new ClientAccountLocked(id, reason, Instant.now()));
    }
    
    public void unlock(String adminId) {
        if (!isLocked()) {
            return;  // 이미 활성화됨
        }
        
        this.status = ClientStatus.ACTIVE;
        this.failedAttempts = 0;
        addEvent(new ClientAccountUnlocked(id, adminId, Instant.now()));
    }
    
    // Private 헬퍼 메서드
    private boolean isLocked() {
        return status == ClientStatus.LOCKED;
    }
    
    private AuthenticationResult handleFailedAuthentication() {
        failedAttempts++;
        addEvent(new ClientAuthenticationFailed(id, failedAttempts, Instant.now()));
        
        if (failedAttempts >= 5) {
            lock("Too many failed attempts");
        }
        
        return AuthenticationResult.failed(5 - failedAttempts);
    }
    
    private AuthenticationResult handleSuccessfulAuthentication() {
        failedAttempts = 0;
        SessionId sessionId = SessionId.generate();
        
        addEvent(new ClientAuthenticationSucceeded(
            id,
            sessionId,
            Instant.now()
        ));
        
        return AuthenticationResult.succeeded(sessionId);
    }
    
    private void addEvent(DomainEvent event) {
        events.add(event);
    }
    
    // 이벤트 수집 메서드
    public List<DomainEvent> collectEvents() {
        List<DomainEvent> collectedEvents = new ArrayList<>(events);
        events.clear();
        return collectedEvents;
    }
}
```

### 값 객체 (Value Object)
```java
public final class ClientSecret {
    private final String encryptedValue;
    private final String salt;
    
    // private 생성자
    private ClientSecret(String encryptedValue, String salt) {
        this.encryptedValue = requireNonNull(encryptedValue);
        this.salt = requireNonNull(salt);
    }
    
    // 정적 팩토리 메서드 - 새 비밀번호 생성
    public static ClientSecret create(String rawSecret) {
        validateRawSecret(rawSecret);
        String salt = generateSalt();
        String encrypted = encrypt(rawSecret, salt);
        return new ClientSecret(encrypted, salt);
    }
    
    // 정적 팩토리 메서드 - DB에서 복원
    public static ClientSecret fromEncrypted(String encryptedValue, String salt) {
        return new ClientSecret(encryptedValue, salt);
    }
    
    // 비즈니스 메서드
    public boolean matches(String rawSecret) {
        if (rawSecret == null) {
            return false;
        }
        return encrypt(rawSecret, salt).equals(encryptedValue);
    }
    
    public String getMasked() {
        return "****" + encryptedValue.substring(encryptedValue.length() - 4);
    }
    
    // 검증 로직
    private static void validateRawSecret(String rawSecret) {
        if (rawSecret == null || rawSecret.length() < 32) {
            throw new WeakSecretException("Secret must be at least 32 characters");
        }
        if (!rawSecret.matches(".*[A-Z].*") || 
            !rawSecret.matches(".*[a-z].*") || 
            !rawSecret.matches(".*[0-9].*") ||
            !rawSecret.matches(".*[!@#$%^&*()].*")) {
            throw new WeakSecretException(
                "Secret must contain uppercase, lowercase, number, and special character"
            );
        }
    }
    
    // equals, hashCode 필수
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientSecret that = (ClientSecret) o;
        return encryptedValue.equals(that.encryptedValue) && 
               salt.equals(that.salt);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(encryptedValue, salt);
    }
}
```

### 도메인 서비스
```java
// 도메인 서비스는 여러 애그리게이트에 걸친 비즈니스 로직을 처리
@DomainService
public class TokenGenerationService {
    private final TokenSigner tokenSigner;
    
    public TokenGenerationService(TokenSigner tokenSigner) {
        this.tokenSigner = requireNonNull(tokenSigner);
    }
    
    public AccessToken generateToken(
        ClientCredentials client,
        AuthenticationSession session
    ) {
        // 여러 애그리게이트의 정보를 조합
        TokenClaims claims = TokenClaims.builder()
            .clientId(client.getId())
            .sessionId(session.getId())
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
            .build();
            
        String signedToken = tokenSigner.sign(claims);
        
        return AccessToken.of(signedToken);
    }
}
```

## 📝 명명 규칙

### 클래스/인터페이스 명명
```java
// ✅ 올바른 명명
public class ClientCredentials { }      // 명사, 도메인 용어
public interface SaveClientPort { }     // 동사 + 명사
public class AuthenticationResult { }   // 결과를 나타내는 명사

// ❌ 잘못된 명명
public class ClientCredentialsManager { }  // Manager, Helper 등 모호한 접미사
public interface IClientRepository { }     // I 접두사 사용
public class ProcessAuth { }              // 동사로 시작
```

### 메서드 명명
```java
// ✅ 올바른 명명
public AuthenticationResult authenticate(String secret) { }
public void lock(String reason) { }
public boolean isLocked() { }
public Optional<Client> findById(ClientId id) { }

// ❌ 잘못된 명명
public void processAuthentication() { }  // 모호한 process
public boolean checkLocked() { }         // check 대신 is 사용
public Client getClientById() { }        // null 가능한데 get 사용
```

### 상수 명명
```java
// ✅ 올바른 상수
public static final int MAX_FAILED_ATTEMPTS = 5;
public static final Duration TOKEN_EXPIRATION = Duration.ofHours(1);
private static final Pattern CLIENT_ID_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+$");

// ❌ 잘못된 상수
public static final int max_failed_attempts = 5;  // 소문자
public static int MAX_ATTEMPTS = 5;                // final 누락
```

## 🧪 테스트 코드 표준

### 테스트 클래스 구조
```java
class ClientCredentialsTest {
    
    // 테스트 대상
    private ClientCredentials client;
    
    @BeforeEach
    void setUp() {
        // Given - 공통 설정
        client = ClientCredentials.createNew("CLIENT_001", "ValidSecret123!@#");
    }
    
    @Nested
    @DisplayName("인증 처리")
    class Authentication {
        
        @Test
        @DisplayName("올바른 비밀번호로 인증 시 성공한다")
        void shouldSucceedWithCorrectSecret() {
            // When
            AuthenticationResult result = client.authenticate("ValidSecret123!@#");
            
            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getSessionId()).isNotNull();
        }
        
        @Test
        @DisplayName("잘못된 비밀번호로 인증 시 실패한다")
        void shouldFailWithWrongSecret() {
            // When
            AuthenticationResult result = client.authenticate("WrongSecret");
            
            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getRemainingAttempts()).isEqualTo(4);
        }
        
        @Test
        @DisplayName("5회 실패 시 계정이 잠긴다")
        void shouldLockAfterFiveFailures() {
            // Given
            IntStream.range(0, 4).forEach(i -> 
                client.authenticate("WrongSecret")
            );
            
            // When
            AuthenticationResult result = client.authenticate("WrongSecret");
            
            // Then
            assertThat(result.isBlocked()).isTrue();
            assertThat(client.collectEvents())
                .anySatisfy(event -> {
                    assertThat(event).isInstanceOf(ClientAccountLocked.class);
                    assertThat(((ClientAccountLocked) event).getReason())
                        .isEqualTo("Too many failed attempts");
                });
        }
    }
    
    @Nested
    @DisplayName("비밀번호 교체")
    class SecretRotation {
        
        @Test
        @DisplayName("활성 상태에서 비밀번호를 교체할 수 있다")
        void shouldRotateSecretWhenActive() {
            // Given
            String newSecret = "NewValidSecret456!@#";
            
            // When
            client.rotateSecret(newSecret);
            
            // Then
            AuthenticationResult result = client.authenticate(newSecret);
            assertThat(result.isSuccess()).isTrue();
        }
        
        @Test
        @DisplayName("잠긴 상태에서는 비밀번호를 교체할 수 없다")
        void shouldNotRotateSecretWhenLocked() {
            // Given
            client.lock("Security breach");
            
            // When & Then
            assertThatThrownBy(() -> client.rotateSecret("NewSecret"))
                .isInstanceOf(ClientLockedException.class)
                .hasMessageContaining("CLIENT_001");
        }
    }
}
```

### 테스트 명명 규칙
```java
// 메서드명: should + 예상결과 + 조건
@Test
void shouldReturnEmptyWhenNoDataExists() { }

@Test
void shouldThrowExceptionWhenInvalidInput() { }

// DisplayName: 한글로 명확한 시나리오 설명
@DisplayName("유효하지 않은 클라이언트 ID로 생성 시 예외가 발생한다")
```

## 🎨 코드 포맷팅

### 들여쓰기와 중괄호
```java
// ✅ 올바른 스타일
public class ClientService {
    private final ClientRepository repository;
    
    public ClientService(ClientRepository repository) {
        this.repository = repository;
    }
    
    public Optional<Client> findClient(ClientId id) {
        if (id == null) {
            return Optional.empty();
        }
        
        return repository.findById(id)
            .filter(Client::isActive)
            .map(this::enrichClient);
    }
}

// ❌ 잘못된 스타일
public class ClientService
{  // 중괄호 위치
    private final ClientRepository repository;
    
    public Optional<Client> findClient(ClientId id)
    {
        if(id==null) return Optional.empty();  // 공백 부족, 한 줄 처리
    }
}
```

### 라인 길이와 메서드 체이닝
```java
// ✅ 올바른 체이닝
return clients.stream()
    .filter(Client::isActive)
    .filter(client -> client.hasRole(Role.ADMIN))
    .map(Client::getId)
    .collect(Collectors.toList());

// ✅ 올바른 빌더 패턴
TokenClaims claims = TokenClaims.builder()
    .clientId(client.getId())
    .sessionId(session.getId())
    .issuedAt(Instant.now())
    .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
    .build();
```

## 📚 Import 규칙

### Import 순서
```java
// 1. Java 표준 라이브러리
import java.time.Instant;
import java.util.List;
import java.util.Optional;

// 2. 써드파티 라이브러리
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// 3. 프로젝트 내부 패키지
import com.company.security.domain.auth.ClientCredentials;
import com.company.security.domain.auth.event.ClientAuthenticationSucceeded;

// 4. 정적 import
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
```

### Import 규칙
- 와일드카드 import 금지 (`import java.util.*`)
- 사용하지 않는 import 제거
- 정적 import는 테스트와 유틸리티 메서드에만 제한적 사용

## 🔒 불변성과 방어적 복사

```java
public class AccessPolicy {
    private final Set<IpRange> allowedRanges;
    private final Set<ClientId> blacklistedClients;
    
    private AccessPolicy(Set<IpRange> allowedRanges, Set<ClientId> blacklistedClients) {
        // 방어적 복사
        this.allowedRanges = Set.copyOf(allowedRanges);
        this.blacklistedClients = Set.copyOf(blacklistedClients);
    }
    
    public static AccessPolicy create(
        Collection<IpRange> allowedRanges,
        Collection<ClientId> blacklistedClients
    ) {
        return new AccessPolicy(
            new HashSet<>(allowedRanges),
            new HashSet<>(blacklistedClients)
        );
    }
    
    // 불변 컬렉션 반환
    public Set<IpRange> getAllowedRanges() {
        return allowedRanges;  // 이미 불변
    }
    
    // 새 인스턴스 반환하는 수정 메서드
    public AccessPolicy addAllowedRange(IpRange range) {
        Set<IpRange> newRanges = new HashSet<>(allowedRanges);
        newRanges.add(range);
        return new AccessPolicy(newRanges, blacklistedClients);
    }
}
```

## 📋 JavaDoc 작성 규칙

```java
/**
 * 클라이언트 자격증명을 관리하는 애그리게이트 루트.
 * 
 * <p>이 클래스는 클라이언트의 인증 정보와 상태를 관리하며,
 * 인증 시도, 비밀번호 교체, 계정 잠금/해제 등의 비즈니스 로직을 포함합니다.
 * 
 * <p>주요 비즈니스 규칙:
 * <ul>
 *   <li>5회 연속 인증 실패 시 자동으로 계정이 잠깁니다</li>
 *   <li>잠긴 계정은 관리자만 해제할 수 있습니다</li>
 *   <li>비밀번호는 최소 32자 이상이어야 합니다</li>
 * </ul>
 * 
 * @see AuthenticationResult
 * @see ClientSecret
 * @since 1.0.0
 */
@Entity
@AggregateRoot
public class ClientCredentials {
    
    /**
     * 새로운 클라이언트 자격증명을 생성합니다.
     * 
     * @param clientId 클라이언트 식별자
     * @param rawSecret 암호화되지 않은 비밀번호 (최소 32자)
     * @return 생성된 클라이언트 자격증명
     * @throws IllegalArgumentException clientId가 null이거나 비어있는 경우
     * @throws WeakSecretException 비밀번호가 보안 요구사항을 충족하지 않는 경우
     */
    public static ClientCredentials createNew(String clientId, String rawSecret) {
        // 구현
    }
}
```

## 🚨 예외 처리 규칙

```java
// ✅ 도메인 예외 - checked exception
public class ClientLockedException extends DomainException {
    private final ClientId clientId;
    
    public ClientLockedException(ClientId clientId) {
        super(String.format("Client %s is locked", clientId.getValue()));
        this.clientId = clientId;
    }
    
    public ClientId getClientId() {
        return clientId;
    }
}

// ✅ 기술적 예외 - unchecked exception
public class DatabaseConnectionException extends InfrastructureException {
    public DatabaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}

// 예외 처리 패턴
public class AuthenticationCommandHandler {
    
    public TokenResponse handle(AuthenticateCommand command) {
        try {
            // 도메인 로직
            ClientCredentials client = loadClient(command.getClientId());
            AuthenticationResult result = client.authenticate(command.getSecret());
            
            // 결과 처리
            if (result.isSuccess()) {
                saveSession(result.getSession());
                publishEvents(client.collectEvents());
                return TokenResponse.success(result.getToken());
            }
            
            return TokenResponse.failed(result.getMessage());
            
        } catch (ClientNotFoundException e) {
            // 도메인 예외는 명시적으로 처리
            return TokenResponse.notFound();
        } catch (DatabaseConnectionException e) {
            // 기술적 예외는 상위로 전파
            throw new ServiceUnavailableException("Authentication service unavailable", e);
        }
    }
}
```

## 💡 Best Practices

### 1. Early Return 패턴
```java
// ✅ Early return으로 가독성 향상
public Optional<Client> findActiveClient(ClientId id) {
    if (id == null) {
        return Optional.empty();
    }
    
    Optional<Client> client = repository.findById(id);
    if (client.isEmpty()) {
        return Optional.empty();
    }
    
    if (!client.get().isActive()) {
        return Optional.empty();
    }
    
    return client;
}

// ❌ 중첩된 if문
public Optional<Client> findActiveClient(ClientId id) {
    if (id != null) {
        Optional<Client> client = repository.findById(id);
        if (client.isPresent()) {
            if (client.get().isActive()) {
                return client;
            }
        }
    }
    return Optional.empty();
}
```

### 2. Null 대신 Optional 사용
```java
// ✅ Optional 사용
public Optional<AuthenticationSession> findActiveSession(SessionId id) {
    return repository.findById(id)
        .filter(AuthenticationSession::isActive);
}

// ❌ Null 반환
public AuthenticationSession findActiveSession(SessionId id) {
    AuthenticationSession session = repository.findById(id);
    if (session != null && session.isActive()) {
        return session;
    }
    return null;  // 금지
}
```

### 3. 스트림 API 활용
```java
// ✅ 스트림으로 간결하게 표현
public List<ClientId> findExpiredClients() {
    return clients.stream()
        .filter(client -> client.getLastActivityTime().isBefore(cutoffTime))
        .filter(client -> !client.isPremium())
        .map(Client::getId)
        .collect(Collectors.toList());
}

// ❌ 전통적인 루프
public List<ClientId> findExpiredClients() {
    List<ClientId> result = new ArrayList<>();
    for (Client client : clients) {
        if (client.getLastActivityTime().isBefore(cutoffTime)) {
            if (!client.isPremium()) {
                result.add(client.getId());
            }
        }
    }
    return result;
}
```

## 🔧 리팩토링 체크리스트

- [ ] 모든 public 클래스와 메서드에 JavaDoc이 있는가?
- [ ] 생성자 대신 정적 팩토리 메서드를 사용하는가?
- [ ] 도메인 로직이 도메인 객체 내부에 있는가?
- [ ] 가변 상태를 최소화했는가?
- [ ] null 대신 Optional을 사용하는가?
- [ ] 메서드가 20줄을 넘지 않는가?
- [ ] 순환 복잡도가 10 미만인가?
- [ ] 테스트 커버리지가 80% 이상인가?
- [ ] 중복 코드가 없는가?
- [ ] 명명이 의도를 명확히 표현하는가?