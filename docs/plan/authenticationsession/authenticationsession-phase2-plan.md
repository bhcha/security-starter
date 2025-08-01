# AuthenticationSession Phase 2 구현 계획서

## 🎯 구현 목표
AuthenticationSession 애그리거트의 Value Objects를 구현합니다. 인증 세션과 실패 관리를 위한 핵심 데이터 구조를 구축합니다.

## 📋 구현 범위
- Value Objects 3개 구현
- 각 VO의 속성, 검증 규칙, 주요 메서드 정의
- TDD 사이클로 테스트 우선 개발

## 🔧 구현할 컴포넌트

### 1. SessionId (세션 식별자)
**속성:**
- UUID value: 세션의 고유 식별자

**검증 규칙:**
- null 값 불허
- 빈 UUID 불허

**주요 메서드:**
- static SessionId generate(): 새로운 세션 ID 생성
- static SessionId of(UUID value): UUID로부터 생성
- static SessionId of(String value): 문자열로부터 생성
- UUID getValue(): UUID 값 반환
- String toString(): 문자열 표현

### 2. ClientIp (클라이언트 IP 주소)
**속성:**
- String ipAddress: IP 주소 문자열
- IpType type: IPv4 또는 IPv6 구분

**검증 규칙:**
- null 또는 빈 문자열 불허
- 유효한 IP 주소 형식만 허용 (IPv4/IPv6)
- 로컬호스트 IP도 허용 (개발/테스트 환경)

**주요 메서드:**
- static ClientIp of(String ipAddress): IP 주소로부터 생성
- String getIpAddress(): IP 주소 반환
- IpType getType(): IP 타입 반환 (IPv4/IPv6)
- boolean isIPv4(): IPv4 여부 확인
- boolean isIPv6(): IPv6 여부 확인
- boolean isLocalhost(): 로컬호스트 여부 확인

### 3. RiskLevel (위험도 수준)
**속성:**
- RiskScore score: 위험도 점수 (0-100)
- RiskCategory category: 위험도 범주 (LOW, MEDIUM, HIGH, CRITICAL)
- String reason: 위험도 판단 사유

**검증 규칙:**
- score는 0-100 범위 내
- category는 score와 일치해야 함 (0-25: LOW, 26-50: MEDIUM, 51-75: HIGH, 76-100: CRITICAL)
- reason은 필수 입력

**주요 메서드:**
- static RiskLevel of(int score, String reason): 점수와 사유로 생성
- static RiskLevel low(String reason): 낮은 위험도 생성
- static RiskLevel medium(String reason): 중간 위험도 생성
- static RiskLevel high(String reason): 높은 위험도 생성
- static RiskLevel critical(String reason): 심각한 위험도 생성
- int getScore(): 위험도 점수 반환
- RiskCategory getCategory(): 위험도 범주 반환
- String getReason(): 판단 사유 반환
- boolean isLow(): 낮은 위험도 여부
- boolean isMedium(): 중간 위험도 여부
- boolean isHigh(): 높은 위험도 여부
- boolean isCritical(): 심각한 위험도 여부

## 📁 파일 구조
```
src/main/java/com/dx/hexacore/security/session/domain/
├── SessionId.java
├── ClientIp.java
├── RiskLevel.java
└── IpType.java (enum)
└── RiskCategory.java (enum)

src/test/java/com/dx/hexacore/security/session/domain/
├── SessionIdTest.java
├── ClientIpTest.java
└── RiskLevelTest.java
```

## ✅ 완료 기준
- [ ] SessionId VO 구현 및 테스트 완료 (최소 10개 테스트)
- [ ] ClientIp VO 구현 및 테스트 완료 (최소 15개 테스트)
- [ ] RiskLevel VO 구현 및 테스트 완료 (최소 20개 테스트)
- [ ] 모든 테스트 통과 (100% 성공률)
- [ ] 코딩 표준 준수 확인
- [ ] 테스트 커버리지 80% 이상

## 🔄 구현 순서
1. SessionId 구현 (가장 단순)
2. ClientIp 구현 (IP 검증 로직 포함)
3. RiskLevel 구현 (복잡한 비즈니스 로직)

## 📚 참조 사항
- 기존 Authentication 애그리거트의 Value Object 패턴 참조
- IP 주소 검증에는 Java의 InetAddress 활용
- 위험도 계산 로직은 명확한 규칙 기반으로 구현