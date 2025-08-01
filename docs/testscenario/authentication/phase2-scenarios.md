# Authentication Value Objects 테스트 시나리오

## Credentials 테스트 시나리오

### 정상 케이스
- [ ] 유효한 username과 password로 Credentials 생성 성공
- [ ] username 최소 길이(3자)로 생성 성공
- [ ] username 최대 길이(50자)로 생성 성공
- [ ] password 최소 길이(8자)로 생성 성공
- [ ] username에 영문, 숫자, 언더스코어 조합으로 생성 성공
- [ ] 동일한 값으로 생성된 Credentials는 equals true
- [ ] 다른 값으로 생성된 Credentials는 equals false
- [ ] 동일한 값으로 생성된 Credentials는 hashCode 동일
- [ ] Credentials 객체는 불변성 보장

### 예외 케이스
- [ ] username이 null일 때 IllegalArgumentException 발생
- [ ] username이 empty일 때 IllegalArgumentException 발생
- [ ] username이 2자 이하일 때 IllegalArgumentException 발생
- [ ] username이 51자 이상일 때 IllegalArgumentException 발생
- [ ] username에 특수문자 포함 시 IllegalArgumentException 발생
- [ ] password가 null일 때 IllegalArgumentException 발생
- [ ] password가 empty일 때 IllegalArgumentException 발생
- [ ] password가 7자 이하일 때 IllegalArgumentException 발생

## Token 테스트 시나리오

### 정상 케이스
- [ ] 유효한 accessToken, refreshToken, expiresIn으로 Token 생성 성공
- [ ] expiresIn 최소값(1초)으로 생성 성공
- [ ] expiresIn 최대값(86400초)으로 생성 성공
- [ ] 동일한 값으로 생성된 Token은 equals true
- [ ] 다른 값으로 생성된 Token은 equals false
- [ ] 동일한 값으로 생성된 Token은 hashCode 동일
- [ ] Token 객체는 불변성 보장
- [ ] JWT 형식의 accessToken으로 생성 성공

### 예외 케이스
- [ ] accessToken이 null일 때 IllegalArgumentException 발생
- [ ] accessToken이 empty일 때 IllegalArgumentException 발생
- [ ] refreshToken이 null일 때 IllegalArgumentException 발생
- [ ] refreshToken이 empty일 때 IllegalArgumentException 발생
- [ ] expiresIn이 0일 때 IllegalArgumentException 발생
- [ ] expiresIn이 음수일 때 IllegalArgumentException 발생
- [ ] expiresIn이 86401초 이상일 때 IllegalArgumentException 발생

## AuthenticationStatus 테스트 시나리오

### 정상 케이스
- [ ] PENDING 상태로 AuthenticationStatus 생성 성공
- [ ] SUCCESS 상태로 AuthenticationStatus 생성 성공
- [ ] FAILED 상태로 AuthenticationStatus 생성 성공
- [ ] 동일한 상태값으로 생성된 AuthenticationStatus는 equals true
- [ ] 다른 상태값으로 생성된 AuthenticationStatus는 equals false
- [ ] 동일한 상태값으로 생성된 AuthenticationStatus는 hashCode 동일
- [ ] AuthenticationStatus 객체는 불변성 보장
- [ ] isPending() 메서드가 PENDING 상태에서만 true 반환
- [ ] isSuccess() 메서드가 SUCCESS 상태에서만 true 반환
- [ ] isFailed() 메서드가 FAILED 상태에서만 true 반환

### 예외 케이스
- [ ] null 상태로 생성 시 IllegalArgumentException 발생