# Security Starter - 실제 설정 옵션 문서

## 개요

이 문서는 security-starter에서 실제로 구현된 모든 설정 옵션을 상세히 설명합니다. 모든 설정은 `application.yml` 또는 `application.properties` 파일에서 관리할 수 있습니다.

## 🏗️ 메인 설정 (SecurityStarterProperties)

### 기본 설정
```yaml
security-starter:
  # 전체 스타터 활성화 여부 (기본값: true, Zero Configuration)
  enabled: true

  # 아키텍처 모드 (TRADITIONAL | HEXAGONAL)
  mode: TRADITIONAL
```

#### Mode 설명
- **TRADITIONAL**: 모든 레이어에서 security-starter 자유 사용
- **HEXAGONAL**: Domain Layer에서 사용 제한, Application Layer 이상에서만 사용

## 🎛️ Feature Toggle 설정

각 기능을 개별적으로 제어할 수 있는 토글 시스템입니다:

```yaml
security-starter:
  # 인증 기능 토글
  authentication-toggle:
    enabled: true  # 기본값: true

  # 세션 관리 토글
  session-toggle:
    enabled: true  # 기본값: true

  # JWT 토큰 토글
  jwt-toggle:
    enabled: true  # 기본값: true

  # Rate Limiting 토글
  rate-limit-toggle:
    enabled: false  # 기본값: false

  # IP 제한 토글
  ip-restriction-toggle:
    enabled: false  # 기본값: false

  # 보안 헤더 토글
  headers-toggle:
    enabled: true  # 기본값: true
```

## 🔐 Token Provider 설정

### 토큰 제공자 선택
```yaml
security-starter:
  token-provider:
    # 토큰 제공자 타입 (jwt | keycloak)
    provider: jwt  # 기본값: jwt
```

### JWT Token Provider 설정
```yaml
security-starter:
  token-provider:
    jwt:
      # JWT 기능 활성화 여부
      enabled: true  # 기본값: true

      # JWT 서명 키 (최소 32자, 프로덕션에서는 512자 권장)
      secret: "default-jwt-secret-for-development-change-this-in-production"

      # 액세스 토큰 만료 시간 (초, 최소: 300, 최대: 86400)
      access-token-expiration: 3600  # 기본값: 1시간

      # 리프레시 토큰 만료 시간 (초, 최소: 3600, 최대: 2592000)
      refresh-token-expiration: 604800  # 기본값: 7일

      # 토큰 발급자
      issuer: "security-starter"  # 기본값: security-starter

      # 제외할 경로 목록
      excluded-paths:
        - "/public/**"
        - "/health"

      # 서명 알고리즘 (HS256 | HS384 | HS512 | RS256 | RS384 | RS512)
      algorithm: "HS256"  # 기본값: HS256

      # Authorization 헤더의 토큰 접두사
      token-prefix: "Bearer "  # 기본값: "Bearer "

      # JWT 토큰을 담을 헤더 이름
      header-name: "Authorization"  # 기본값: "Authorization"
```

#### JWT 설정 검증 규칙
1. **프로덕션 환경 Secret 검증**: 기본값 문자열 사용 금지
2. **토큰 만료시간 관계**: 리프레시 토큰 > 액세스 토큰
3. **Secret 최소 길이**: 32자 이상 (256bit 보안)

### Keycloak Token Provider 설정
```yaml
security-starter:
  token-provider:
    keycloak:
      # Keycloak 기능 활성화 여부
      enabled: true  # 기본값: true

      # Keycloak 서버 URL (프로덕션에서는 HTTPS 필수)
      server-url: "https://keycloak.example.com"

      # Realm 이름 (1-100자)
      realm: "your-realm"

      # Client ID (1-100자)
      client-id: "your-client-id"

      # Client Secret (최대 500자, Public Client인 경우 선택사항)
      client-secret: "your-client-secret"

      # Public Client 여부 (Client Secret 불필요)
      public-client: false  # 기본값: false

      # OAuth2 Scopes
      scopes: "openid profile email"  # 기본값

      # Grant Type (password | authorization_code | client_credentials)
      grant-type: "password"  # 기본값: password
```

#### Keycloak 설정 검증 규칙
1. **프로덕션 HTTPS**: localhost가 아닌 프로덕션 환경에서 HTTPS 필수
2. **필수 설정 완성도**: enabled=true시 모든 필수 필드 검증
3. **Grant Type 유효성**: 지원하는 Grant Type만 허용

## 🔒 JWT 통합 전략 설정

```yaml
security-starter:
  jwt:
    # JWT 기능 활성화
    enabled: true  # 기본값: true

    # JWT 통합 전략
    strategy: "security-integration"  # 기본값
    # 옵션:
    # - security-integration: SecurityFilterChain과 통합
    # - servlet-filter: 독립적인 ServletFilter로 동작
    # - manual: Bean만 제공, 사용자가 완전 제어

    # ServletFilter 전략 사용 시 우선순위 (-100 ~ 100)
    filter-order: 50  # 기본값: 50

    # 자동 주입 활성화 (deprecated)
    auto-inject: false
```

## 🛡️ 인증 필터 설정

```yaml
security-starter:
  filter:
    # 인증 필터 활성화 여부
    enabled: true  # 기본값: true

    # 인증 제외 경로 (Ant Pattern 지원)
    exclude-paths:
      - "/public/**"
      - "/actuator/health"
      - "/swagger-ui/**"
      - "/v3/api-docs/**"
```

## 📊 세션 관리 설정

```yaml
security-starter:
  session:
    # 세션 관리 활성화 여부
    enabled: true  # 기본값: true

    # 세션 이벤트 처리
    event:
      enabled: true  # 기본값: true

    # 계정 잠금 정책
    lockout:
      # 최대 실패 허용 횟수
      max-attempts: 5  # 기본값: 5

      # 잠금 지속 시간 (분)
      lockout-duration-minutes: 30  # 기본값: 30분

      # 실패 카운트 추적 윈도우 (분)
      attempt-window-minutes: 15  # 기본값: 15분
```

## 💾 캐시 설정

```yaml
security-starter:
  cache:
    # 캐시 활성화 여부
    enabled: true  # 기본값: true

    # 캐시 타입 (caffeine | redis)
    type: "caffeine"  # 기본값: caffeine

    # Caffeine 캐시 설정
    caffeine:
      # 최대 캐시 크기
      maximum-size: 10000  # 기본값: 10000

      # TTL (초)
      expire-after-write-seconds: 900  # 기본값: 15분

      # 통계 수집 여부
      record-stats: true  # 기본값: true
```

## 🚦 Rate Limiting 설정

```yaml
security-starter:
  rate-limit:
    # Rate Limiting 활성화 여부
    enabled: false  # 기본값: false

    # 기본 제한 횟수 (시간 윈도우당)
    default-limit: 100  # 기본값: 100

    # 시간 윈도우 (초)
    time-window: 60  # 기본값: 60초

    # IP별 제한 (미설정시 default-limit 사용)
    per-ip-limit: 50

    # 사용자별 제한 (미설정시 default-limit 사용)
    per-user-limit: 200

    # 엔드포인트별 제한 (미설정시 default-limit 사용)
    per-endpoint-limit: 1000

    # Rate Limiting 전략
    strategy: SLIDING_WINDOW  # 기본값
    # 옵션: SLIDING_WINDOW | FIXED_WINDOW | TOKEN_BUCKET

    # 분산 Rate Limiting (Redis 사용)
    distributed: false  # 기본값: false

    # 저장소 키 접두사
    key-prefix: "rate_limit:"  # 기본값
```

## 🌐 IP 제한 설정

```yaml
security-starter:
  ip-restriction:
    # IP 제한 활성화 여부
    enabled: false  # 기본값: false

    # IP 제한 모드 (WHITELIST | BLACKLIST)
    mode: WHITELIST  # 기본값: WHITELIST

    # 허용 IP 목록 (WHITELIST 모드용, CIDR 지원)
    allowed-ips:
      - "192.168.1.0/24"
      - "10.0.0.1"
      - "203.0.113.0/24"

    # 차단 IP 목록 (BLACKLIST 모드용, CIDR 지원)
    blocked-ips:
      - "192.168.100.0/24"
      - "172.16.0.1"

    # 로컬호스트 자동 허용
    allow-localhost: true  # 기본값: true

    # 프록시 헤더 확인 (신뢰할 수 있는 프록시에서만 사용)
    check-forwarded-header: false  # 기본값: false

    # 클라이언트 IP 헤더 이름
    client-ip-header: "X-Real-IP"  # 기본값: X-Real-IP

    # IP 캐시 설정
    cache-size: 10000  # 기본값: 10000
    cache-ttl: 3600    # 기본값: 1시간
```

## 🛡️ 보안 헤더 설정

```yaml
security-starter:
  headers:
    # 보안 헤더 활성화
    enabled: true  # 기본값: true

    # X-Frame-Options (DENY | SAMEORIGIN | ALLOW-FROM)
    frame-options: "DENY"  # 기본값: DENY

    # X-Content-Type-Options
    content-type-options: "nosniff"  # 기본값: nosniff

    # X-XSS-Protection
    xss-protection: "1; mode=block"  # 기본값

    # Strict-Transport-Security
    hsts: "max-age=31536000; includeSubDomains"  # 기본값

    # Content-Security-Policy
    content-security-policy: "default-src 'self'"  # 기본값

    # Referrer-Policy
    referrer-policy: "no-referrer-when-downgrade"  # 기본값

    # Feature-Policy
    feature-policy: "geolocation 'none'; microphone 'none'; camera 'none'"

    # HSTS 헤더 활성화
    hsts-enabled: true  # 기본값: true

    # CSP 헤더 활성화
    csp-enabled: true  # 기본값: true
```

## ⚙️ 보안 상수 설정 (SecurityConstants)

```yaml
security-starter:
  constants:
    # 세션 관련 상수
    session:
      time-window-minutes: 15        # 실패 추적 시간 윈도우
      timeout-hours: 24             # 세션 타임아웃
      max-failed-attempts: 5        # 최대 실패 횟수
      lockout-duration-minutes: 30  # 계정 잠금 시간

    # 토큰 관련 상수
    token:
      min-expires-in: 1        # 최소 만료시간 (초)
      max-expires-in: 86400    # 최대 만료시간 (초, 24시간)

    # 입력값 검증 상수
    validation:
      min-username-length: 3              # 최소 사용자명 길이
      max-username-length: 50             # 최대 사용자명 길이
      min-password-length: 8              # 최소 비밀번호 길이
      min-jwt-secret-length: 32           # 최소 JWT Secret 길이
      recommended-jwt-secret-length: 64   # 권장 JWT Secret 길이

    # 로깅 및 보안 감지 상수
    logging:
      suspicious-activity-threshold: 5                    # 의심활동 임계값
      suspicious-activity-time-window-minutes: 5          # 의심활동 감지 윈도우
      max-log-message-length: 50                         # 로그 메시지 최대 길이
      top-stats-limit: 5                                 # 통계 상위 항목 수
```

## 📋 완전한 설정 예제

### 개발 환경 설정
```yaml
security-starter:
  enabled: true
  mode: TRADITIONAL

  # 기본 기능만 활성화
  authentication-toggle:
    enabled: true
  session-toggle:
    enabled: true
  jwt-toggle:
    enabled: true
  rate-limit-toggle:
    enabled: false
  ip-restriction-toggle:
    enabled: false
  headers-toggle:
    enabled: true

  # JWT 개발 설정
  token-provider:
    provider: jwt
    jwt:
      enabled: true
      secret: "dev-secret-key-minimum-32-characters-long"
      access-token-expiration: 3600
      refresh-token-expiration: 604800
      issuer: "dev-security-starter"

  # 관대한 필터 설정
  filter:
    enabled: true
    exclude-paths:
      - "/public/**"
      - "/actuator/**"
      - "/swagger-ui/**"
      - "/v3/api-docs/**"
      - "/h2-console/**"

  # 기본 세션 관리
  session:
    enabled: true
    lockout:
      max-attempts: 10  # 개발 환경에서는 관대하게
      lockout-duration-minutes: 5
```

### 프로덕션 환경 설정
```yaml
security-starter:
  enabled: true
  mode: HEXAGONAL

  # 모든 보안 기능 활성화
  authentication-toggle:
    enabled: true
  session-toggle:
    enabled: true
  jwt-toggle:
    enabled: true
  rate-limit-toggle:
    enabled: true
  ip-restriction-toggle:
    enabled: true
  headers-toggle:
    enabled: true

  # 강력한 JWT 설정
  token-provider:
    provider: jwt
    jwt:
      enabled: true
      secret: "${JWT_SECRET}"  # 환경변수에서 주입
      access-token-expiration: 900    # 15분
      refresh-token-expiration: 86400 # 24시간
      issuer: "prod-security-starter"
      algorithm: "HS512"

  # 엄격한 필터 설정
  filter:
    enabled: true
    exclude-paths:
      - "/actuator/health"  # 최소한만 제외

  # 엄격한 세션 관리
  session:
    enabled: true
    lockout:
      max-attempts: 3
      lockout-duration-minutes: 60
      attempt-window-minutes: 10

  # Rate Limiting 활성화
  rate-limit:
    enabled: true
    default-limit: 60
    time-window: 60
    per-ip-limit: 30
    per-user-limit: 100
    strategy: SLIDING_WINDOW
    distributed: true

  # IP 제한 활성화
  ip-restriction:
    enabled: true
    mode: WHITELIST
    allowed-ips:
      - "10.0.0.0/8"      # 내부 네트워크
      - "172.16.0.0/12"   # 사설 네트워크
      - "192.168.0.0/16"  # 로컬 네트워크
    allow-localhost: false
    check-forwarded-header: true
    client-ip-header: "X-Forwarded-For"

  # 강력한 보안 헤더
  headers:
    enabled: true
    frame-options: "DENY"
    content-security-policy: "default-src 'self'; script-src 'self' 'unsafe-inline'"
    hsts: "max-age=31536000; includeSubDomains; preload"
```

### Keycloak 연동 설정
```yaml
security-starter:
  enabled: true

  # Keycloak 토큰 제공자
  token-provider:
    provider: keycloak
    keycloak:
      enabled: true
      server-url: "https://keycloak.company.com"
      realm: "company-realm"
      client-id: "spring-boot-app"
      client-secret: "${KEYCLOAK_CLIENT_SECRET}"
      grant-type: "authorization_code"
      scopes: "openid profile email roles"
      public-client: false

  # JWT 통합 전략
  jwt:
    enabled: true
    strategy: "security-integration"

  # Keycloak 친화적 필터 설정
  filter:
    enabled: true
    exclude-paths:
      - "/oauth2/**"
      - "/login/oauth2/**"
```

## 🔍 설정 디버깅

### 로깅 활성화
```yaml
logging:
  level:
    com.ldx.hexacore.security: DEBUG
    com.ldx.hexacore.security.config: TRACE
```

### 설정 확인 방법
1. 애플리케이션 시작 로그에서 설정 상태 확인
2. `/actuator/configprops` 엔드포인트로 실제 적용된 설정 확인
3. `/actuator/health` 엔드포인트로 보안 컴포넌트 상태 확인

이 설정 문서는 security-starter의 모든 실제 구현된 설정 옵션을 포함하며, 각 설정의 기본값과 검증 규칙을 명시하여 개발자가 안전하고 효과적으로 보안 기능을 구성할 수 있도록 합니다.