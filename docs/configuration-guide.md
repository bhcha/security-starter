# Security Starter 설정 가이드

## 개선 사항 (v2.0+)

### 🎯 주요 개선사항
1. **TokenProvider Bean 중복 정의 문제 해결**
   - 기본값 설정으로 JWT Provider가 기본 활성화
   - 설정이 없어도 애플리케이션 시작 가능
   
2. **설정 프로퍼티 경로 일관성 개선**
   - 다양한 프로퍼티 경로 지원 (aliases)
   - 사용자 친화적인 짧은 경로 제공

3. **제로 설정 지원**
   - 추가 설정 없이도 기본 JWT 인증 작동
   - 개발용 기본 시크릿 제공 (운영 환경에서는 변경 필수)

## 기본 동작 (Zero Configuration)

추가 설정 없이도 다음과 같이 작동합니다:

```yaml
# 설정 없음 - JWT Provider가 기본으로 활성화됨
# hexacore.security.token-provider.provider: jwt (기본값)
# hexacore.security.token-provider.jwt.enabled: true (기본값)
```

## 토큰 제공자 설정

### 1. JWT Provider 사용 (기본값)

#### 최소 설정 (권장)
```yaml
hexacore:
  security:
    token-provider:
      provider: jwt
      jwt:
        secret: "your-production-secret-key-change-this"
```

#### 완전한 설정
```yaml
hexacore:
  security:
    token-provider:
      provider: jwt
      jwt:
        enabled: true
        secret: "your-production-secret-key-change-this"
        access-token-expiration: 3600    # 1시간 (초)
        refresh-token-expiration: 604800 # 7일 (초)
        issuer: "my-app"
        algorithm: "HS256"
        token-prefix: "Bearer "
        header-name: "Authorization"
```

### 2. Keycloak Provider 사용

```yaml
hexacore:
  security:
    token-provider:
      provider: keycloak
      keycloak:
        enabled: true
        server-url: "http://localhost:8080"
        realm: "my-realm"
        client-id: "my-client"
        client-secret: "your-client-secret"
```

## 지원하는 프로퍼티 경로 (Aliases)

사용자 편의를 위해 다양한 프로퍼티 경로를 지원합니다:

### Provider 설정
```yaml
# 모든 경로가 동일하게 작동합니다
hexacore.security.token-provider.provider: jwt
hexa.security.auth.provider: jwt
hexa.security.token.provider: jwt
security.auth.provider: jwt
security.token.provider: jwt
```

### JWT 설정
```yaml
# 표준 경로
hexacore.security.token-provider.jwt.secret: "secret"

# 단축 경로 (aliases)
hexa.security.jwt.secret: "secret"
security.jwt.secret: "secret"
```

### Keycloak 설정
```yaml
# 표준 경로
hexacore.security.token-provider.keycloak.server-url: "http://localhost:8080"

# 단축 경로 (aliases)
hexa.security.keycloak.server-url: "http://localhost:8080"
security.keycloak.server-url: "http://localhost:8080"
```

## 환경별 설정 예제

### 개발 환경 (application-dev.yml)
```yaml
hexacore:
  security:
    token-provider:
      provider: jwt
      jwt:
        secret: "dev-secret-key"
        access-token-expiration: 7200  # 2시간
```

### 운영 환경 (application-prod.yml)
```yaml
hexacore:
  security:
    token-provider:
      provider: jwt
      jwt:
        secret: "${JWT_SECRET:}"  # 환경변수에서 주입
        access-token-expiration: 1800  # 30분
        refresh-token-expiration: 86400  # 1일
```

### Keycloak 연동 환경 (application-keycloak.yml)
```yaml
hexacore:
  security:
    token-provider:
      provider: keycloak
      keycloak:
        server-url: "${KEYCLOAK_SERVER_URL}"
        realm: "${KEYCLOAK_REALM}"
        client-id: "${KEYCLOAK_CLIENT_ID}"
        client-secret: "${KEYCLOAK_CLIENT_SECRET}"
```

## 마이그레이션 가이드

### v1.x에서 v2.x로 업그레이드

#### 1. 기존 설정이 있는 경우
기존 설정은 그대로 작동합니다. 변경 불필요.

#### 2. Bean 중복 오류가 발생했던 경우
```yaml
# 이전: 오류 발생
# 설정 없음 또는 불완전한 설정

# 현재: 자동으로 JWT Provider 활성화
# 추가 설정 불필요, 또는 다음과 같이 명시적 설정
hexacore:
  security:
    token-provider:
      provider: jwt  # 또는 keycloak
```

#### 3. 프로퍼티 경로 단순화 (선택사항)
```yaml
# 이전 (여전히 작동함)
hexacore:
  security:
    token-provider:
      provider: jwt
      jwt:
        secret: "secret"

# 새로운 단축 경로 (선택사항)
hexa:
  security:
    auth:
      provider: jwt
    jwt:
      secret: "secret"
```

## 문제 해결

### NoUniqueBeanDefinitionException 오류
```
해결됨: v2.0+에서는 기본적으로 JWT Provider만 활성화되어 이 오류가 발생하지 않습니다.
```

### JWT secret이 설정되지 않음 오류
```yaml
# 해결 방법 1: 명시적 설정
hexacore.security.token-provider.jwt.secret: "your-secret"

# 해결 방법 2: 단축 경로 사용
hexa.security.jwt.secret: "your-secret"

# 해결 방법 3: 환경변수 사용
JWT_SECRET=your-secret
```

### Provider가 명확하지 않음
```yaml
# 명시적으로 provider 지정
hexacore.security.token-provider.provider: jwt  # 또는 keycloak
```

## 보안 주의사항

### 🚨 운영 환경 필수 변경사항
1. **JWT Secret 변경**: 기본 시크릿은 개발용입니다
2. **토큰 만료 시간 조정**: 보안 요구사항에 맞게 설정
3. **환경변수 사용**: 민감한 정보는 환경변수로 관리

```yaml
# ❌ 운영 환경에서 금지
hexacore.security.token-provider.jwt.secret: "default-jwt-secret-for-development-change-this-in-production"

# ✅ 운영 환경 권장
hexacore.security.token-provider.jwt.secret: "${JWT_SECRET}"
```

## 고급 설정

### 인증 필터 설정
```yaml
hexacore:
  security:
    filter:
      enabled: true
      exclude-paths:
        - "/api/health"
        - "/api/docs/**"
        - "/swagger-ui/**"
```

### 세션 관리 설정
```yaml
hexacore:
  security:
    session:
      enabled: true
      lockout:
        max-attempts: 5
        lockout-duration-minutes: 30
        attempt-window-minutes: 15
```

이 가이드를 통해 Security Starter를 효과적으로 설정하고 사용할 수 있습니다.