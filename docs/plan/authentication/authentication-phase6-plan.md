# Authentication 애그리거트 Phase 6: Query Side 구현 계획

## 🎯 구현 목표

Authentication 애그리거트의 Application Layer Query Side를 구현합니다.
- Query 객체 및 Use Case 인터페이스 구현
- Query Handler 구현
- Projection 정의
- 조회 포트 인터페이스 정의

## 📚 참조 문서

- `/docs/plan/implementation-plan.md` (Authentication 애그리거트의 Application Layer 섹션)
- `/docs/spec/authentication-domain-layer-spec.md` (도메인 레이어 명세서)
- `/docs/guide/2-coding-standards.md`
- `/docs/guide/3-2-application-tdd.md`
- `/docs/guide/4-2-application-templates.md`

## 🔍 구현 범위

### implementation-plan.md에서 정의된 Queries
1. **GetAuthenticationQuery**: 인증 정보 조회
2. **GetTokenInfoQuery**: 토큰 정보 조회

### 구현할 컴포넌트

#### 1. Query 객체들
- `GetAuthenticationQuery`: 인증 ID 기반 조회
- `GetTokenInfoQuery`: 토큰 기반 조회

#### 2. Use Case 인터페이스들
- `GetAuthenticationUseCase`: 인증 정보 조회 인터페이스
- `GetTokenInfoUseCase`: 토큰 정보 조회 인터페이스

#### 3. Query Handler 구현체들
- `AuthenticationQueryHandler`: 인증 조회 처리기

#### 4. Projection 클래스들
- `AuthenticationProjection`: 인증 정보 조회용 프로젝션
- `TokenInfoProjection`: 토큰 정보 조회용 프로젝션

#### 5. Outbound Port 인터페이스들
- `LoadAuthenticationQueryPort`: 인증 조회 포트
- `LoadTokenInfoQueryPort`: 토큰 조회 포트

#### 6. Response DTO들
- `AuthenticationResponse`: 인증 정보 응답
- `TokenInfoResponse`: 토큰 정보 응답

## 🎯 상세 설계

### GetAuthenticationQuery 설계
```java
public class GetAuthenticationQuery {
    private final String authenticationId;
    // 인증 ID로 Authentication 애그리거트 정보 조회
}
```

### GetTokenInfoQuery 설계
```java
public class GetTokenInfoQuery {
    private final String token;
    // 토큰으로 토큰 관련 정보 조회 (유효성, 만료시간 등)
}
```

### Projection 설계
- **AuthenticationProjection**: 인증 상태, 시도 시간, 성공/실패 정보
- **TokenInfoProjection**: 토큰 유효성, 만료 시간, 갱신 가능 여부

## ✅ 완료 기준

- [ ] 모든 Query 객체 구현 및 테스트
- [ ] Use Case 인터페이스 정의
- [ ] Query Handler 구현 및 테스트
- [ ] Projection 클래스 구현
- [ ] Outbound Port 인터페이스 정의
- [ ] Response DTO 구현
- [ ] 테스트 커버리지 80% 이상
- [ ] 코딩 표준 준수
- [ ] tracker.md 업데이트

## 📁 파일 구조

```
src/main/java/com/dx/hexacore/security/application/
├── query/
│   ├── port/
│   │   ├── in/
│   │   │   ├── GetAuthenticationQuery.java
│   │   │   ├── GetAuthenticationUseCase.java
│   │   │   ├── GetTokenInfoQuery.java
│   │   │   ├── GetTokenInfoUseCase.java
│   │   │   ├── AuthenticationResponse.java
│   │   │   └── TokenInfoResponse.java
│   │   └── out/
│   │       ├── LoadAuthenticationQueryPort.java
│   │       └── LoadTokenInfoQueryPort.java
│   ├── handler/
│   │   └── AuthenticationQueryHandler.java
│   └── projection/
│       ├── AuthenticationProjection.java
│       └── TokenInfoProjection.java
```

## 📝 구현 순서

1. **Query 객체 구현**: GetAuthenticationQuery, GetTokenInfoQuery
2. **Use Case 인터페이스 정의**: GetAuthenticationUseCase, GetTokenInfoUseCase  
3. **Response DTO 구현**: AuthenticationResponse, TokenInfoResponse
4. **Projection 클래스 구현**: AuthenticationProjection, TokenInfoProjection
5. **Outbound Port 인터페이스 정의**: LoadAuthenticationQueryPort, LoadTokenInfoQueryPort
6. **Query Handler 구현**: AuthenticationQueryHandler

## 🧪 테스트 계획

### 필수 테스트 시나리오
1. **정상 조회 케이스**
   - 유효한 인증 ID로 조회 성공
   - 유효한 토큰으로 정보 조회 성공

2. **예외 케이스**
   - 존재하지 않는 인증 ID 조회
   - 유효하지 않은 토큰 조회
   - null/빈 값 입력

3. **경계값 테스트**
   - 만료된 토큰 처리
   - 비활성화된 인증 처리

### 테스트 커버리지 목표
- Query Handler: 85% 이상
- Query 객체: 90% 이상
- Projection: 80% 이상