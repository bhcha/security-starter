# AuthenticationSession Aggregate - Phase 2: Value Objects 테스트 시나리오

## 개요
AuthenticationSession 애그리거트의 Value Object들에 대한 테스트 시나리오를 정의합니다.

## 테스트 대상 Value Objects
1. SessionId
2. ClientIp
3. RiskLevel
4. IpType (enum)
5. RiskCategory (enum)

---

## 1. SessionId 테스트 시나리오

### 1.1 정상 시나리오

#### TC001: UUID로 SessionId 생성
**목적**: 유효한 UUID로 SessionId를 생성할 수 있다
**입력**: `UUID.randomUUID()`
**기대결과**: 
- SessionId 객체 생성 성공
- getValue()가 입력 UUID와 동일

#### TC002: 문자열로 SessionId 생성
**목적**: 유효한 UUID 문자열로 SessionId를 생성할 수 있다
**입력**: `"550e8400-e29b-41d4-a716-446655440000"`
**기대결과**:
- SessionId 객체 생성 성공
- getValue()가 해당 UUID와 동일

#### TC003: 새로운 SessionId 생성
**목적**: generate() 메서드로 새로운 SessionId를 생성할 수 있다
**입력**: `SessionId.generate()`
**기대결과**:
- SessionId 객체 생성 성공
- 매번 다른 UUID 생성
- getValue()가 null이 아님

#### TC004: SessionId toString 테스트
**목적**: toString()이 UUID 문자열을 반환한다
**입력**: 임의의 SessionId
**기대결과**: `toString() == getValue().toString()`

### 1.2 예외 시나리오

#### TC005: null UUID로 생성
**입력**: `SessionId.of((UUID) null)`
**기대결과**: `IllegalArgumentException("SessionId UUID cannot be null")`

#### TC006: null 문자열로 생성
**입력**: `SessionId.of((String) null)`
**기대결과**: `IllegalArgumentException("SessionId string cannot be null or empty")`

#### TC007: 빈 문자열로 생성
**입력**: `SessionId.of("")`
**기대결과**: `IllegalArgumentException("SessionId string cannot be null or empty")`

#### TC008: 공백 문자열로 생성
**입력**: `SessionId.of("   ")`
**기대결과**: `IllegalArgumentException("SessionId string cannot be null or empty")`

#### TC009: 잘못된 UUID 형식으로 생성
**입력**: `SessionId.of("invalid-uuid-format")`
**기대결과**: `IllegalArgumentException("Invalid UUID format: invalid-uuid-format")`

### 1.3 동등성 테스트

#### TC010: SessionId 동등성 검증
**목적**: 같은 UUID를 가진 SessionId는 동등하다
**입력**: 동일한 UUID로 생성된 두 SessionId
**기대결과**: `equals()` true, `hashCode()` 동일

---

## 2. ClientIp 테스트 시나리오

### 2.1 정상 시나리오

#### TC011: 유효한 IPv4 주소로 생성
**입력**: `"192.168.0.1"`
**기대결과**:
- ClientIp 객체 생성 성공
- `getIpAddress() == "192.168.0.1"`
- `getType() == IpType.IPv4`
- `isIPv4() == true`
- `isIPv6() == false`

#### TC012: 유효한 IPv6 주소로 생성
**입력**: `"2001:0db8:85a3:0000:0000:8a2e:0370:7334"`
**기대결과**:
- ClientIp 객체 생성 성공
- `getType() == IpType.IPv6`
- `isIPv4() == false`
- `isIPv6() == true`

#### TC013: 로컬호스트 IPv4 테스트
**입력**: `"127.0.0.1"`
**기대결과**:
- `isLocalhost() == true`
- `getType() == IpType.IPv4`

#### TC014: 로컬호스트 IPv6 테스트
**입력**: `"::1"`
**기대결과**:
- `isLocalhost() == true`
- `getType() == IpType.IPv6`

#### TC015: 사설 IP 주소 테스트
**입력**: `["10.0.0.1", "172.16.0.1", "192.168.1.1"]`
**기대결과**: 모두 `isIPv4() == true`

#### TC016: 공인 IP 주소 테스트
**입력**: `["8.8.8.8", "1.1.1.1", "208.67.222.222"]`
**기대결과**: 모두 `isIPv4() == true`

### 2.2 예외 시나리오

#### TC017: null IP 주소로 생성
**입력**: `ClientIp.of(null)`
**기대결과**: `IllegalArgumentException("IP address cannot be null or empty")`

#### TC018: 빈 IP 주소로 생성
**입력**: `ClientIp.of("")`
**기대결과**: `IllegalArgumentException("IP address cannot be null or empty")`

#### TC019: 공백만 있는 IP 주소로 생성
**입력**: `ClientIp.of("   ")`
**기대결과**: `IllegalArgumentException("IP address cannot be null or empty")`

#### TC020: 앞뒤 공백이 있는 IP 주소로 생성
**입력**: `ClientIp.of(" 192.168.0.1 ")`
**기대결과**: `IllegalArgumentException("Invalid IP address format:  192.168.0.1 ")`

#### TC021: 잘못된 IP 주소 형식 - 범위 초과
**입력**: `ClientIp.of("999.999.999.999")`
**기대결과**: `IllegalArgumentException("Invalid IP address format: 999.999.999.999")`

#### TC022: 잘못된 IP 주소 형식 - 문자 포함
**입력**: `ClientIp.of("192.168.0.abc")`
**기대결과**: `IllegalArgumentException("Invalid IP address format: 192.168.0.abc")`

### 2.3 동등성 테스트

#### TC023: ClientIp 동등성 검증
**목적**: 같은 IP 주소를 가진 ClientIp는 동등하다
**입력**: 동일한 IP로 생성된 두 ClientIp
**기대결과**: `equals()` true, `hashCode()` 동일

---

## 3. RiskLevel 테스트 시나리오

### 3.1 정상 시나리오

#### TC024: 낮은 위험도 생성
**입력**: `RiskLevel.low("Normal login attempt")`
**기대결과**:
- `getScore() == 0`
- `getCategory() == RiskCategory.LOW`
- `getReason() == "Normal login attempt"`
- `isLow() == true`, 나머지 false

#### TC025: 중간 위험도 생성
**입력**: `RiskLevel.medium("Failed login attempt")`
**기대결과**:
- `getScore() == 38`
- `getCategory() == RiskCategory.MEDIUM`
- `isMedium() == true`, 나머지 false

#### TC026: 높은 위험도 생성
**입력**: `RiskLevel.high("Multiple failed attempts")`
**기대결과**:
- `getScore() == 63`
- `getCategory() == RiskCategory.HIGH`
- `isHigh() == true`, 나머지 false

#### TC027: 치명적 위험도 생성
**입력**: `RiskLevel.critical("Brute force attack detected")`
**기대결과**:
- `getScore() == 88`
- `getCategory() == RiskCategory.CRITICAL`
- `isCritical() == true`, 나머지 false

#### TC028: 커스텀 점수로 생성
**입력**: `RiskLevel.of(60, "Custom risk assessment")`
**기대결과**:
- `getScore() == 60`
- `getCategory() == RiskCategory.MEDIUM`
- `getReason() == "Custom risk assessment"`

#### TC029: 카테고리 경계값 테스트
**목적**: 점수별 카테고리 자동 분류 검증
**테스트 케이스**:
- `RiskLevel.of(10, "Low")` → `RiskCategory.LOW`
- `RiskLevel.of(25, "Low")` → `RiskCategory.LOW`
- `RiskLevel.of(26, "Medium")` → `RiskCategory.MEDIUM`
- `RiskLevel.of(50, "Medium")` → `RiskCategory.MEDIUM`
- `RiskLevel.of(51, "High")` → `RiskCategory.HIGH`
- `RiskLevel.of(75, "High")` → `RiskCategory.HIGH`
- `RiskLevel.of(76, "Critical")` → `RiskCategory.CRITICAL`
- `RiskLevel.of(100, "Critical")` → `RiskCategory.CRITICAL`

#### TC030: 이유 문자열 trim 테스트
**입력**: `RiskLevel.low("  Normal login  ")`
**기대결과**: `getReason() == "Normal login"`

### 3.2 예외 시나리오

#### TC031: null 이유로 생성
**입력**: `RiskLevel.low(null)`
**기대결과**: `IllegalArgumentException("Risk reason cannot be null or empty")`

#### TC032: 빈 이유로 생성
**입력**: `RiskLevel.medium("")`
**기대결과**: `IllegalArgumentException("Risk reason cannot be null or empty")`

#### TC033: 공백만 있는 이유로 생성
**입력**: `RiskLevel.high("   ")`
**기대결과**: `IllegalArgumentException("Risk reason cannot be null or empty")`

#### TC034: 음수 점수로 생성
**입력**: `RiskLevel.of(-1, "Invalid")`
**기대결과**: `IllegalArgumentException("Risk score must be between 0 and 100")`

#### TC035: 100 초과 점수로 생성
**입력**: `RiskLevel.of(101, "Invalid")`
**기대결과**: `IllegalArgumentException("Risk score must be between 0 and 100")`

#### TC036: 경계값 점수 테스트
**입력**: `RiskLevel.of(0, "Minimum")`, `RiskLevel.of(100, "Maximum")`  
**기대결과**: 예외 없이 정상 생성

### 3.3 동등성 및 비교 테스트

#### TC037: RiskLevel 동등성 검증
**목적**: 같은 점수와 이유를 가진 RiskLevel은 동등하다
**입력**: 동일한 값으로 생성된 두 RiskLevel
**기대결과**: `equals()` true, `hashCode()` 동일

#### TC038: 위험도 비교 테스트
**목적**: 위험도별 점수 순서 검증
**기대결과**: `low < medium < high < critical`

---

## 4. IpType 테스트 시나리오

### 4.1 열거형 값 테스트

#### TC039: IPv4 타입 값 확인
**기대결과**: `IpType.IPv4.name() == "IPv4"`

#### TC040: IPv6 타입 값 확인
**기대결과**: `IpType.IPv6.name() == "IPv6"`

#### TC041: 열거형 값 개수 확인
**기대결과**: `IpType.values().length == 2`

#### TC042: valueOf 테스트
**입력**: `IpType.valueOf("IPv4")`, `IpType.valueOf("IPv6")`
**기대결과**: 해당 열거형 상수 반환

#### TC043: 잘못된 valueOf 테스트
**입력**: `IpType.valueOf("IPv5")`
**기대결과**: `IllegalArgumentException`

#### TC044: ordinal 테스트
**기대결과**: `IPv4.ordinal() == 0`, `IPv6.ordinal() == 1`

---

## 5. RiskCategory 테스트 시나리오

### 5.1 열거형 값 테스트

#### TC045: 모든 카테고리 값 확인
**기대결과**: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL` 존재

#### TC046: 열거형 값 개수 확인
**기대결과**: `RiskCategory.values().length == 4`

#### TC047: valueOf 테스트
**입력**: `RiskCategory.valueOf("LOW")` 등
**기대결과**: 해당 열거형 상수 반환

#### TC048: ordinal 순서 테스트
**기대결과**: `LOW(0) < MEDIUM(1) < HIGH(2) < CRITICAL(3)`

#### TC049: compareTo 테스트
**목적**: 심각도 순서대로 비교 가능
**기대결과**: `LOW.compareTo(CRITICAL) < 0`

---

## 테스트 완료 기준

1. **커버리지**: 모든 public 메서드 100% 커버리지
2. **시나리오**: 정상/예외/경계값 시나리오 모두 포함
3. **TDD**: 테스트 먼저 작성 후 구현 검증
4. **문서화**: 각 테스트의 목적과 기대결과 명확히 기술
5. **자동화**: 모든 테스트가 자동 실행 가능하고 반복 가능

## 품질 지표

- **테스트 케이스 수**: 49개
- **예상 커버리지**: 100%
- **예외 시나리오 비율**: 35%
- **비즈니스 로직 검증**: 모든 VO의 핵심 규칙 검증