# Authentication Phase 5: Command Side 구현 계획

## 구현 목표
Authentication 애그리거트의 Application Layer Command Side를 구현합니다.
- Commands: AuthenticateCommand, ValidateTokenCommand
- Use Cases: AuthenticateUseCase, ValidateTokenUseCase, RefreshTokenUseCase
- Inbound/Outbound Ports 정의

## 구현 범위

### Commands
1. **AuthenticateCommand**
   - 필드: username, password
   - 검증: 필수 값 검증

2. **ValidateTokenCommand**
   - 필드: accessToken
   - 검증: 토큰 형식 검증

### Use Cases
1. **AuthenticateUseCase**
   - 입력: AuthenticateCommand
   - 출력: AuthenticationResult (성공/실패 정보)
   - 흐름: 인증 시도 → 도메인 서비스 호출 → 결과 반환

2. **ValidateTokenUseCase**
   - 입력: ValidateTokenCommand
   - 출력: TokenValidationResult
   - 흐름: 토큰 조회 → 정책 검증 → 결과 반환

3. **RefreshTokenUseCase**
   - 입력: RefreshTokenCommand
   - 출력: Token
   - 흐름: 리프레시 토큰 검증 → 새 토큰 발급

### Ports
#### Inbound Ports
- AuthenticationUseCase: 인증 관련 사용 사례 인터페이스
- TokenManagementUseCase: 토큰 관리 사용 사례 인터페이스

#### Outbound Ports
- AuthenticationRepository: 인증 정보 저장소
- TokenRepository: 토큰 저장소
- EventPublisher: 도메인 이벤트 발행
- ExternalAuthProvider: 외부 인증 제공자 (Keycloak)

## 완료 기준
- [ ] 모든 Commands 구현 및 테스트
- [ ] 모든 Use Cases 구현 및 테스트
- [ ] Inbound/Outbound Ports 정의
- [ ] 테스트 커버리지 80% 이상
- [ ] TDD 사이클 준수