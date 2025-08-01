# Authentication Phase 5: Command Side 테스트 시나리오

## Commands 테스트 시나리오

### AuthenticateCommand
#### 정상 케이스
1. **유효한 사용자명과 비밀번호로 Command 생성**
   - Given: 유효한 username과 password
   - When: AuthenticateCommand 생성
   - Then: 성공적으로 생성됨

#### 예외 케이스
2. **null username으로 Command 생성**
   - Given: null username
   - When: AuthenticateCommand 생성 시도
   - Then: IllegalArgumentException 발생

3. **빈 username으로 Command 생성**
   - Given: 빈 문자열 username
   - When: AuthenticateCommand 생성 시도
   - Then: IllegalArgumentException 발생

4. **null password로 Command 생성**
   - Given: null password
   - When: AuthenticateCommand 생성 시도
   - Then: IllegalArgumentException 발생

5. **빈 password로 Command 생성**
   - Given: 빈 문자열 password
   - When: AuthenticateCommand 생성 시도
   - Then: IllegalArgumentException 발생

### ValidateTokenCommand
#### 정상 케이스
6. **유효한 토큰으로 Command 생성**
   - Given: 유효한 accessToken
   - When: ValidateTokenCommand 생성
   - Then: 성공적으로 생성됨

#### 예외 케이스
7. **null 토큰으로 Command 생성**
   - Given: null accessToken
   - When: ValidateTokenCommand 생성 시도
   - Then: IllegalArgumentException 발생

8. **빈 토큰으로 Command 생성**
   - Given: 빈 문자열 accessToken
   - When: ValidateTokenCommand 생성 시도
   - Then: IllegalArgumentException 발생

### RefreshTokenCommand
#### 정상 케이스
9. **유효한 리프레시 토큰으로 Command 생성**
   - Given: 유효한 refreshToken
   - When: RefreshTokenCommand 생성
   - Then: 성공적으로 생성됨

#### 예외 케이스
10. **null 리프레시 토큰으로 Command 생성**
    - Given: null refreshToken
    - When: RefreshTokenCommand 생성 시도
    - Then: IllegalArgumentException 발생

## Use Cases 테스트 시나리오

### AuthenticateUseCase
#### 정상 케이스
11. **유효한 인증정보로 인증 성공**
    - Given: 유효한 AuthenticateCommand
    - When: AuthenticateUseCase 실행
    - Then: 성공 결과와 토큰 반환

12. **외부 인증 제공자 연동 성공**
    - Given: 유효한 인증정보와 작동하는 외부 제공자
    - When: 인증 시도
    - Then: 외부 제공자에서 토큰 발급받아 반환

#### 예외 케이스
13. **잘못된 인증정보로 인증 실패**
    - Given: 잘못된 AuthenticateCommand
    - When: AuthenticateUseCase 실행
    - Then: 실패 결과 반환

14. **외부 인증 제공자 연동 실패**
    - Given: 외부 제공자 장애 상황
    - When: 인증 시도
    - Then: 인증 실패 결과 반환

15. **저장소 저장 실패**
    - Given: 저장소 장애 상황
    - When: 인증 성공 후 저장 시도
    - Then: 적절한 예외 처리

### ValidateTokenUseCase
#### 정상 케이스
16. **유효한 토큰 검증 성공**
    - Given: 유효한 ValidateTokenCommand
    - When: ValidateTokenUseCase 실행
    - Then: 유효성 확인 결과 반환

17. **만료되지 않은 토큰 검증**
    - Given: 만료되지 않은 토큰
    - When: 토큰 검증
    - Then: 유효함으로 판정

#### 예외 케이스
18. **만료된 토큰 검증**
    - Given: 만료된 토큰
    - When: ValidateTokenUseCase 실행
    - Then: 무효함으로 판정

19. **존재하지 않는 토큰 검증**
    - Given: 저장소에 없는 토큰
    - When: 토큰 검증
    - Then: 무효함으로 판정

20. **저장소 조회 실패**
    - Given: 저장소 장애 상황
    - When: 토큰 조회 시도
    - Then: 적절한 예외 처리

### RefreshTokenUseCase
#### 정상 케이스
21. **유효한 리프레시 토큰으로 새 토큰 발급**
    - Given: 유효한 RefreshTokenCommand
    - When: RefreshTokenUseCase 실행
    - Then: 새로운 accessToken 반환

22. **리프레시 토큰 기반 토큰 갱신**
    - Given: 만료된 accessToken과 유효한 refreshToken
    - When: 토큰 갱신 시도
    - Then: 새 토큰 발급 성공

#### 예외 케이스
23. **만료된 리프레시 토큰으로 갱신 시도**
    - Given: 만료된 refreshToken
    - When: RefreshTokenUseCase 실행
    - Then: 토큰 갱신 실패

24. **존재하지 않는 리프레시 토큰**
    - Given: 저장소에 없는 refreshToken
    - When: 토큰 갱신 시도
    - Then: 토큰 갱신 실패

25. **외부 제공자 토큰 갱신 실패**
    - Given: 외부 제공자 장애
    - When: 토큰 갱신 시도
    - Then: 적절한 예외 처리

## 통합 테스트 시나리오

### End-to-End 플로우
26. **전체 인증 플로우 테스트**
    - Given: 사용자 인증정보
    - When: 인증 → 토큰 검증 → 토큰 갱신 순서로 실행
    - Then: 각 단계가 정상적으로 동작

27. **도메인 이벤트 발행 확인**
    - Given: 인증 관련 작업 수행
    - When: Use Case 실행
    - Then: 적절한 도메인 이벤트 발행됨

28. **트랜잭션 롤백 테스트**
    - Given: 중간 단계에서 예외 발생
    - When: Use Case 실행
    - Then: 트랜잭션 롤백되어 일관성 유지

## 성능 테스트 시나리오

29. **동시 인증 요청 처리**
    - Given: 여러 사용자의 동시 인증 요청
    - When: 동시에 AuthenticateUseCase 실행
    - Then: 모든 요청이 정상 처리됨

30. **대량 토큰 검증 요청**
    - Given: 대량의 토큰 검증 요청
    - When: ValidateTokenUseCase 반복 실행
    - Then: 성능 저하 없이 처리됨