# AuthenticationSession Application 레이어 명세서

## 개요
AuthenticationSession 애그리거트의 Application Layer는 Command Side(Phase 5)와 Query Side(Phase 6)로 구성되며, 도메인 로직을 조율하고 외부와의 인터페이스를 제공합니다.

## Commands (Command Side - Phase 5)

### CreateSessionCommand
**목적**: 새로운 인증 세션 생성
```java
public record CreateSessionCommand(
    String sessionId,     // 생성할 세션 ID
    String userId,        // 사용자 ID
    String clientIp       // 클라이언트 IP 주소
) {
    // 검증 규칙: sessionId, userId, clientIp는 필수값
}
```

### RecordAttemptCommand  
**목적**: 인증 시도 기록
```java
public record RecordAttemptCommand(
    String sessionId,     // 대상 세션 ID
    String userId,        // 시도한 사용자 ID
    String clientIp,      // 클라이언트 IP
    boolean isSuccessful, // 성공 여부
    int riskScore,        // 위험도 점수 (0-100)
    String riskReason     // 위험도 이유
) {
    // 검증 규칙: 모든 필드 필수, riskScore는 0-100 범위
}
```

### LockSessionCommand
**목적**: 세션 강제 잠금
```java
public record LockSessionCommand(
    String sessionId,           // 잠글 세션 ID
    String reason,              // 잠금 사유
    LocalDateTime lockUntil     // 잠금 해제 시각 (선택적)
) {
    // 검증 규칙: sessionId, reason 필수
}
```

### UnlockSessionCommand
**목적**: 세션 잠금 해제
```java
public record UnlockSessionCommand(
    String sessionId,     // 잠금 해제할 세션 ID
    String reason         // 해제 사유
) {
    // 검증 규칙: sessionId, reason 필수
}
```

## Use Cases (Command Handlers - Phase 5)

### SessionCommandHandler
**책임**: 세션 관련 모든 명령 처리
```java
@Service
@Transactional
public class SessionCommandHandler {
    
    public void handle(CreateSessionCommand command);
    public void handle(RecordAttemptCommand command);
    public void handle(LockSessionCommand command);  
    public void handle(UnlockSessionCommand command);
}
```

**주요 비즈니스 흐름**:
1. **세션 생성**: 유효성 검증 → 도메인 객체 생성 → 저장
2. **시도 기록**: 세션 조회 → 시도 기록 → 잠금 정책 적용 → 이벤트 발행
3. **세션 잠금**: 세션 조회 → 강제 잠금 → 이벤트 발행
4. **잠금 해제**: 세션 조회 → 잠금 해제 → 상태 업데이트

## Queries (Query Side - Phase 6)

### GetSessionStatusQuery
**목적**: 세션 상태 조회
```java
public record GetSessionStatusQuery(
    String sessionId,    // 조회할 세션 ID
    String userId        // 조회할 사용자 ID (optional)
) {
    // 검증 규칙: sessionId 필수
}
```

### GetFailedAttemptsQuery
**목적**: 실패한 인증 시도 목록 조회
```java
public record GetFailedAttemptsQuery(
    String sessionId,           // 세션 ID
    String userId,              // 사용자 ID (선택적)
    LocalDateTime from,         // 조회 시작 시간
    LocalDateTime to,           // 조회 종료 시간
    int limit                   // 조회 제한 개수
) {
    // 검증 규칙: sessionId, from, to 필수, from < to, limit > 0
}
```

## Query Results

### SessionStatusResponse
**목적**: 세션 상태 정보 반환
```java
public record SessionStatusResponse(
    String sessionId,           // 세션 ID
    String primaryUserId,       // 주 사용자 ID
    String primaryClientIp,     // 주 클라이언트 IP
    boolean isLocked,           // 잠금 상태
    LocalDateTime lockedUntil,  // 잠금 해제 시각
    LocalDateTime createdAt,    // 생성 시각
    LocalDateTime lastActivityAt, // 마지막 활동 시각
    int totalAttempts,          // 총 인증 시도 횟수
    int failedAttempts          // 실패한 인증 시도 횟수
)
```

### FailedAttemptsResponse
**목적**: 실패한 인증 시도 목록 반환
```java
public record FailedAttemptsResponse(
    String sessionId,                    // 세션 ID
    List<FailedAttemptResponse> attempts, // 실패 시도 목록
    int totalCount,                      // 전체 실패 시도 수
    LocalDateTime queriedAt              // 조회 시각
)

public record FailedAttemptResponse(
    String userId,              // 사용자 ID
    String clientIp,            // 클라이언트 IP
    int riskScore,              // 위험도 점수
    String riskReason,          // 위험도 이유
    LocalDateTime attemptedAt   // 시도 시각
)
```

## Query Handlers (Phase 6)

### SessionQueryHandler
**책임**: 세션 관련 모든 쿼리 처리
```java
@Service
@Transactional(readOnly = true)
public class SessionQueryHandler {
    
    public SessionStatusResponse handle(GetSessionStatusQuery query);
    public FailedAttemptsResponse handle(GetFailedAttemptsQuery query);
}
```

**주요 쿼리 흐름**:
1. **세션 상태 조회**: 쿼리 검증 → 포트 호출 → 프로젝션 변환 → 응답 반환
2. **실패 시도 조회**: 쿼리 검증 → 목록/개수 조회 → 프로젝션 변환 → 응답 반환

## Projection Classes (Phase 6)

### SessionStatusProjection
**목적**: 세션 상태 읽기 최적화
```java
public record SessionStatusProjection(
    String sessionId, String primaryUserId, String primaryClientIp,
    boolean isLocked, LocalDateTime lockedUntil,
    LocalDateTime createdAt, LocalDateTime lastActivityAt,
    int totalAttempts, int failedAttempts
) {
    public static SessionStatusProjection from(AuthenticationSession session);
    public SessionStatusResponse toResponse();
}
```

### FailedAttemptProjection
**목적**: 실패 시도 읽기 최적화
```java
public record FailedAttemptProjection(
    String userId, String clientIp, int riskScore,
    String riskReason, LocalDateTime attemptedAt
) {
    public static FailedAttemptProjection from(AuthenticationAttempt attempt);
    public FailedAttemptResponse toResponse();
}
```

## Ports

### Inbound Ports (Use Cases)
```java
// Command Side
public interface CreateSessionUseCase {
    void execute(CreateSessionCommand command);
}

public interface RecordAttemptUseCase {
    void execute(RecordAttemptCommand command);
}

// Query Side  
public interface GetSessionStatusUseCase {
    SessionStatusResponse execute(GetSessionStatusQuery query);
}

public interface GetFailedAttemptsUseCase {
    FailedAttemptsResponse execute(GetFailedAttemptsQuery query);
}
```

### Outbound Ports

#### Command Side Ports
```java
public interface SaveSessionPort {
    void save(AuthenticationSession session);
}

public interface LoadSessionPort {
    Optional<AuthenticationSession> loadById(SessionId sessionId);
}

public interface PublishSessionEventPort {
    void publish(DomainEvent event);
}
```

#### Query Side Ports  
```java
public interface LoadSessionStatusQueryPort {
    Optional<SessionStatusProjection> loadSessionStatus(String sessionId);
    Optional<SessionStatusProjection> loadSessionStatusByUser(String sessionId, String userId);
}

public interface LoadFailedAttemptsQueryPort {
    List<FailedAttemptProjection> loadFailedAttempts(String sessionId, 
                                                    LocalDateTime from, LocalDateTime to, int limit);
    List<FailedAttemptProjection> loadFailedAttemptsByUser(String sessionId, String userId,
                                                          LocalDateTime from, LocalDateTime to, int limit);
    int countFailedAttempts(String sessionId, LocalDateTime from, LocalDateTime to);
    int countFailedAttemptsByUser(String sessionId, String userId, 
                                 LocalDateTime from, LocalDateTime to);
}
```

## 예외 처리

### Command Side 예외
```java
public class SessionNotFoundException extends RuntimeException
public class SessionAlreadyExistsException extends RuntimeException  
public class SessionCommandException extends RuntimeException
```

### Query Side 예외
```java
public class SessionNotFoundException extends RuntimeException
public class SessionQueryException extends RuntimeException
```

## 트랜잭션 전략

### Command Side
- `@Transactional` - 읽기/쓰기 트랜잭션
- 도메인 이벤트 발행은 트랜잭션 커밋 후 실행
- 실패 시 롤백으로 데이터 일관성 보장

### Query Side  
- `@Transactional(readOnly = true)` - 읽기 전용 트랜잭션
- 성능 최적화 및 의도 명확화
- 데이터 변경 방지

## 보안 고려사항

### 입력 검증
- 모든 Command/Query 객체에서 필수 파라미터 검증
- IP 주소 형식 검증
- 시간 범위 논리적 유효성 검증

### 정보 노출 방지
- 기술적 예외를 도메인 예외로 래핑하여 내부 정보 보호
- 쿼리 결과에 민감 정보 제외
- 세션 ID 유효성 검증으로 무단 접근 방지

### 성능 및 보안
- 쿼리 결과 제한으로 DoS 공격 방지
- 읽기 전용 트랜잭션으로 데이터 변경 차단
- 실패 시도 조회 시 개인정보 최소화

## 사용 예시

### Command 사용
```java
// 세션 생성
var createCommand = new CreateSessionCommand("session-123", "user456", "192.168.1.100");
sessionCommandHandler.handle(createCommand);

// 인증 시도 기록
var recordCommand = new RecordAttemptCommand("session-123", "user456", "192.168.1.100", 
                                           false, 75, "Suspicious IP");
sessionCommandHandler.handle(recordCommand);
```

### Query 사용
```java
// 세션 상태 조회
var statusQuery = new GetSessionStatusQuery("session-123", null);
SessionStatusResponse status = sessionQueryHandler.handle(statusQuery);

// 실패 시도 조회
var attemptsQuery = new GetFailedAttemptsQuery("session-123", null, 
                                              LocalDateTime.now().minusHours(1), 
                                              LocalDateTime.now(), 10);
FailedAttemptsResponse attempts = sessionQueryHandler.handle(attemptsQuery);
```

## 중요 제약사항

### 비즈니스 규칙
- 세션 ID는 시스템 내에서 유일해야 함
- 실패 시도 5회 초과 시 자동 잠금
- 잠금 시간은 30분 (정책 설정)
- 시간 윈도우는 15분 (연속 실패 판단 기준)

### 기술적 제약
- 쿼리 결과는 최대 1000건으로 제한
- 시간 범위 조회는 최대 30일로 제한
- 동시성 처리를 위한 낙관적 잠금 적용

### 성능 고려사항
- 프로젝션을 통한 읽기 성능 최적화
- 읽기 전용 트랜잭션으로 리소스 사용 최소화
- 대용량 데이터 조회 시 페이징 처리 권장