# Migration Guide: security-auth-starter → security-starter

기존 `security-auth-starter`를 사용하던 프로젝트를 `security-starter` 통합 라이브러리로 마이그레이션하는 가이드입니다.

## 🎯 마이그레이션 개요

### 변경사항 요약
- **단일 라이브러리 통합**: `security-auth-starter` + `security-starter` → `security-starter`
- **설정 prefix 변경**: `security.auth.*` → `hexacore.security.*`
- **Auto-Configuration 통합**: 별도 Starter → 내장 Auto-Configuration

### 이점
✅ **단순화된 의존성**: 하나의 라이브러리만 추가  
✅ **일관된 설정**: 통합된 설정 구조  
✅ **향상된 호환성**: 버전 충돌 없음  
✅ **쉬운 업그레이드**: 단일 버전 관리  

## 📋 마이그레이션 체크리스트

### Step 1: 의존성 변경

#### Before (기존)
```gradle
dependencies {
    implementation 'com.dx:security-starter:1.0.0'
    implementation 'com.dx:security-auth-starter:1.0.0'  // 제거
}
```

#### After (통합 후)
```gradle
dependencies {
    implementation 'com.dx:security-starter:1.0.0'
    // security-auth-starter는 더 이상 필요 없음
}
```

### Step 2: 설정 변경

#### Before (기존 설정)
```yaml
# security-auth-starter 설정
security:
  auth:
    enabled: true
    keycloak:
      enabled: true
      server-url: https://keycloak.example.com
      realm: my-realm
      client-id: my-client
      client-secret: secret
    jwt:
      enabled: false
      secret: jwt-secret
      expiration: 3600
    session:
      max-failed-attempts: 5
      lockout-duration: PT30M
    filter:
      enabled: true
      exclude-urls:
        - "/actuator/**"
        - "/public/**"
    headers:
      enabled: true
      frame-options: DENY
    rate-limit:
      enabled: false
      requests-per-minute: 100
    ip-restriction:
      enabled: false
      allowed-ips: []
    persistence:
      type: jpa
      enabled: true
    cache:
      type: caffeine
      enabled: true
```

#### After (통합 설정)
```yaml
# 새로운 통합 설정 (hexacore.security prefix)
hexacore:
  security:
    enabled: true
    token:
      provider: keycloak  # 'keycloak' 또는 'jwt'
      keycloak:
        enabled: true
        server-url: https://keycloak.example.com
        realm: my-realm
        client-id: my-client
        client-secret: secret
      jwt:
        enabled: false
        secret: jwt-secret
        access-token-expiration: PT1H
        refresh-token-expiration: P7D
    session:
      max-failed-attempts: 5
      lockout-duration: PT30M
      event:
        enabled: true
    filter:
      enabled: true
      exclude-urls:
        - "/actuator/**"
        - "/public/**"
    headers:
      enabled: true
      frame-options: DENY
      content-type-options: nosniff
      xss-protection: "1; mode=block"
    rate-limit:
      enabled: false
      requests-per-minute: 100
      strategy: TOKEN_BUCKET
      distributed: false
    ip-restriction:
      enabled: false
      allowed-ips: []
      proxy-headers:
        - "X-Forwarded-For"
        - "X-Real-IP"
    persistence:
      type: jpa
      enabled: true
      connection-pool:
        enabled: true
        max-size: 20
    cache:
      type: caffeine
      enabled: true
      caffeine:
        max-size: 10000
        expire-after-write: PT15M
```

### Step 3: 코드 변경 (필요시)

#### 대부분의 코드는 변경 불필요

기존에 Port 인터페이스를 사용한 코드는 **그대로 사용 가능**합니다:

```java
// ✅ 변경 불필요 - 그대로 사용 가능
@Autowired
private AuthenticationUseCase authenticationUseCase;

@Autowired 
private TokenManagementUseCase tokenManagementUseCase;

@Autowired
private CheckLockoutUseCase checkLockoutUseCase;
```

#### Health Check 변경 (선택사항)

```java
// Before: security-auth-starter의 Health Indicator
// 자동으로 활성화되었던 기능

// After: 필요시 커스텀 Health Indicator 구현
@Component
public class SecurityHealthIndicator implements HealthIndicator {
    
    @Autowired
    private AuthenticationUseCase authenticationUseCase;
    
    @Override
    public Health health() {
        // 커스텀 헬스 체크 로직
        return Health.up()
            .withDetail("authentication", "available")
            .build();
    }
}
```

## 🔧 마이그레이션 단계별 실행

### Phase 1: 준비 단계 (5분)

1. **의존성 확인**
   ```bash
   # 현재 사용 중인 라이브러리 버전 확인
   ./gradlew dependencies | grep -E "(security-starter|security-auth-starter)"
   ```

2. **설정 백업**
   ```bash
   # 기존 설정 파일 백업
   cp src/main/resources/application.yml application.yml.backup
   ```

### Phase 2: 마이그레이션 실행 (10분)

1. **의존성 변경**
   ```gradle
   // build.gradle에서 security-auth-starter 제거
   dependencies {
       implementation 'com.dx:security-starter:1.0.0'
       // implementation 'com.dx:security-auth-starter:1.0.0'  // 이 줄 삭제
   }
   ```

2. **설정 변경**
   ```yaml
   # application.yml 설정 prefix 변경
   # security.auth.* → hexacore.security.*
   ```

3. **빌드 테스트**
   ```bash
   ./gradlew clean build
   ```

### Phase 3: 검증 단계 (10분)

1. **애플리케이션 실행**
   ```bash
   ./gradlew bootRun
   ```

2. **기능 테스트**
   ```bash
   # 로그인 API 테스트
   curl -X POST http://localhost:8080/api/login \
     -H "Content-Type: application/json" \
     -d '{"username":"test","password":"test"}'

   # 보호된 리소스 접근 테스트
   curl -X GET http://localhost:8080/api/profile \
     -H "Authorization: Bearer YOUR_TOKEN"
   ```

3. **로그 확인**
   ```bash
   # 애플리케이션 로그에서 security-starter 초기화 확인
   # "Hexacore Security Auto Configuration initialized" 메시지 확인
   ```

## 🚨 주의사항 및 문제 해결

### 일반적인 문제

#### 1. 설정 바인딩 오류
```
Description:
Binding to target org.springframework.boot.context.properties.bind.BindException: 
Failed to bind properties under 'security.auth'
```

**해결방법**: 설정 prefix를 `hexacore.security`로 변경하세요.

#### 2. Auto-Configuration 충돌
```
Parameter 0 of constructor in SecurityConfig required a bean of type 'TokenProvider' 
that could not be found.
```

**해결방법**: 
- `hexacore.security.enabled=true` 설정 확인
- `hexacore.security.token.provider` 설정 확인 (keycloak 또는 jwt)

#### 3. 클래스패스 충돌
```
java.lang.NoClassDefFoundError: Could not initialize class 
com.dx.hexacore.security.config.SecurityAuthAutoConfiguration
```

**해결방법**: security-auth-starter를 완전히 제거했는지 확인하세요.

### 설정 호환성 매핑

| 기존 설정 | 새 설정 | 비고 |
|----------|---------|------|
| `security.auth.enabled` | `hexacore.security.enabled` | prefix 변경 |
| `security.auth.keycloak.*` | `hexacore.security.token.keycloak.*` | 구조 변경 |
| `security.auth.jwt.*` | `hexacore.security.token.jwt.*` | 구조 변경 |
| `security.auth.session.*` | `hexacore.security.session.*` | prefix만 변경 |
| `security.auth.filter.*` | `hexacore.security.filter.*` | prefix만 변경 |

## 🔄 롤백 계획

마이그레이션에 문제가 있는 경우 롤백 방법:

### 1. 즉시 롤백 (1분)
```gradle
// build.gradle 복원
dependencies {
    implementation 'com.dx:security-starter:1.0.0'
    implementation 'com.dx:security-auth-starter:1.0.0'  // 다시 추가
}
```

```bash
# 설정 파일 복원
cp application.yml.backup src/main/resources/application.yml

# 재빌드
./gradlew clean build
```

### 2. 점진적 마이그레이션

두 라이브러리를 일시적으로 함께 사용하면서 점진적으로 마이그레이션할 수도 있습니다:

```yaml
# 두 설정을 동시에 유지 (임시)
security:
  auth:
    enabled: false  # 기존 starter 비활성화

hexacore:
  security:
    enabled: true   # 새 통합 라이브러리 활성화
    # ... 새 설정
```

## 📞 지원 및 문의

### 마이그레이션 지원
- 📧 이메일: support@dx.com
- 📚 문서: [security-starter documentation](README.md)
- 🐛 이슈 리포트: [GitHub Issues](https://github.com/dx/security-starter/issues)

### FAQ

**Q: 기존 데이터베이스 스키마는 변경되나요?**  
A: 아니요. 테이블 구조는 동일하게 유지됩니다.

**Q: 마이그레이션 중 다운타임이 필요한가요?**  
A: 대부분의 경우 무중단 마이그레이션이 가능합니다. 다만 설정 변경 후 재시작이 필요합니다.

**Q: 기존 JWT 토큰은 계속 유효한가요?**  
A: 네, JWT 검증 로직은 동일하므로 기존 토큰이 계속 유효합니다.

**Q: Spring Boot 버전 호환성은?**  
A: Spring Boot 3.0+ 버전에서 동일하게 작동합니다.

## ✅ 마이그레이션 완료 확인

다음 모든 항목이 확인되면 마이그레이션이 완료됩니다:

- [ ] `security-auth-starter` 의존성 제거 완료
- [ ] 설정 prefix `hexacore.security.*`로 변경 완료  
- [ ] 애플리케이션 정상 실행
- [ ] 로그인 API 정상 동작
- [ ] JWT 토큰 인증 정상 동작
- [ ] 세션 관리 기능 정상 동작
- [ ] 기존 기능 모두 정상 동작

🎉 **마이그레이션 완료!** 이제 통합된 `security-starter` 라이브러리의 모든 기능을 사용할 수 있습니다.