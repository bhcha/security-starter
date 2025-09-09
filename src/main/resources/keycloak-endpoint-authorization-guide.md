# Keycloak 엔드포인트별 권한 제어 가이드

## 개요
이 문서는 security-starter를 사용하는 애플리케이션에서 Keycloak의 UMA 2.0을 활용하여 엔드포인트별 권한을 제어하는 방법을 설명합니다.

## 주요 기능
- Bearer 토큰 인증 시 접근하려는 엔드포인트 정보를 Keycloak에 전달
- Keycloak의 리소스/권한 정책에 따른 접근 제어
- UMA 2.0 프로토콜을 통한 세밀한 권한 관리

## 설정 방법

### 1. application.yml 설정

```yaml
# Keycloak 설정
hexacore:
  security:
    token-provider:
      keycloak:
        enabled: true
        server-url: ${KEYCLOAK_AUTH_SERVER_URL:https://authdev.daewoong.co.kr/}
        realm: ${KEYCLOAK_REALM:backoffice-api}
        client-id: ${KEYCLOAK_CLIENT:identity-api}
        client-secret: ${KEYCLOAK_CLIENT_SECRET:your-secret}

# 리소스 권한 체크 활성화
security:
  auth:
    authentication:
      check-resource-permission: true  # Keycloak UMA 권한 체크 활성화
```

### 2. Keycloak 서버 설정

#### 2.1 Client 설정
1. Keycloak Admin Console 접속
2. Clients > [your-client] 선택
3. Settings 탭에서:
   - Authorization Enabled: ON
   - Service Accounts Enabled: ON (필요시)

#### 2.2 Authorization 설정
1. Authorization 탭 이동
2. Resources 생성:
   ```
   Name: User API
   Type: urn:myapp:resources:user
   URI: /api/users/*
   Scopes: GET, POST, PUT, DELETE
   ```

3. Policies 생성:
   ```
   Name: Only Admin Policy
   Type: Role
   Roles: admin
   ```

4. Permissions 생성:
   ```
   Name: User API Permission
   Resource: User API
   Policy: Only Admin Policy
   Decision Strategy: UNANIMOUS
   ```

## 동작 원리

### 1. 요청 흐름
```
Client → [Bearer Token] → Application → Keycloak
                             ↓
                    1. 토큰 기본 검증
                    2. 엔드포인트 정보 수집
                       - URI: /api/users/123
                       - Method: GET
                    3. UMA 권한 체크
                       - Permission: /api/users/123#GET
                    4. 권한 결과 반환
```

### 2. 코드 구조
- `TokenValidationContext`: 요청 컨텍스트 정보 (URI, HTTP Method 등)
- `JwtAuthenticationFilter`: 요청 정보 캡처 및 컨텍스트 생성
- `KeycloakTokenProvider`: UMA 2.0 권한 체크 수행

## 권한 체크 로직

### UMA 2.0 Grant Type
```http
POST /realms/{realm}/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded
Authorization: Bearer {access_token}

grant_type=urn:ietf:params:oauth:grant-type:uma-ticket
audience={client_id}
permission=/api/users/123#GET
```

### 응답 처리
- **200 OK + RPT Token**: 권한 있음
- **403 Forbidden**: 권한 없음
- **기타 에러**: fail-open 정책 (설정 가능)

## 로깅 및 디버깅

### 로그 레벨 설정
```yaml
logging:
  level:
    com.dx.hexacore.security.auth: DEBUG
```

### 주요 로그 메시지
- `Checking resource permission for URI: /api/users/123 with method: GET`
- `UMA permission granted for: /api/users/123#GET`
- `Resource permission denied for URI: /api/users/123 with method: DELETE`

## 고려사항

### 1. 성능
- UMA 권한 체크는 추가 네트워크 요청 발생
- 캐싱 전략 고려 필요
- 토큰에 권한이 이미 포함된 경우 체크 생략

### 2. 실패 정책
- **fail-open**: 에러 시 접근 허용 (개발 환경)
- **fail-close**: 에러 시 접근 차단 (프로덕션 환경)

### 3. 제외 경로
인증이 필요 없는 경로는 여전히 제외 가능:
```yaml
hexacore:
  security:
    filter:
      excludePaths:
        - /api/public/**
        - /health
```

## 문제 해결

### 1. 403 Forbidden 계속 발생
- Keycloak에서 리소스 URI 패턴 확인
- Client의 Authorization 설정 활성화 확인
- Service Account에 uma_protection 역할 부여 확인

### 2. 권한 체크가 동작하지 않음
- `check-resource-permission: true` 설정 확인
- TokenProvider가 KeycloakTokenProvider인지 확인
- 로그 레벨을 DEBUG로 설정하여 상세 로그 확인

### 3. 성능 이슈
- 권한 체크 결과 캐싱 구현 고려
- 특정 엔드포인트만 권한 체크하도록 선택적 적용