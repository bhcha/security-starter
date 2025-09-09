# Security Starter 로깅 가이드

## 📊 로깅 체계 개요

Security Starter는 체계적인 로깅 시스템을 제공하여 부모 프로젝트에서 모든 보안 관련 동작을 쉽게 추적할 수 있습니다.

## 🎯 로거 카테고리

| 로거명 | 용도 | 로그 레벨 |
|--------|------|-----------|
| `SECURITY.STARTUP` | 시작 시 설정 및 초기화 상태 | INFO |
| `SECURITY.REQUEST` | 각 요청의 처리 과정 | INFO/DEBUG |
| `SECURITY.EVENT` | 보안 이벤트 (인증, 권한) | INFO/WARN |
| `SECURITY.AUDIT` | 감사 로그 | INFO |
| `SECURITY.PERFORMANCE` | 성능 메트릭 | INFO/WARN |
| `SECURITY.AUTH` | 인증 관련 이벤트 | INFO |

## 📝 샘플 로그 출력

### 1. 애플리케이션 시작 시

```
╔═══════════════════════════════════════════════════════════════════════╗
║                    🔐 HEXACORE SECURITY STARTER                      ║
║                           Version 1.2.0                              ║
╚═══════════════════════════════════════════════════════════════════════╝

┌─── 🎯 Security Mode ───────────────────────────────────────────────┐
│ Security Enabled: ✅ YES
│ Token Provider: KEYCLOAK 🔑
└────────────────────────────────────────────────────────────────────┘

┌─── 🛡️  Authentication Settings ────────────────────────────────────┐
│ 🎯 Resource Permission Check: ENABLED ✅
│    └─ Keycloak UMA 2.0 authorization is ACTIVE
│    └─ Each request will be validated against Keycloak resources
│ Default Role: ROLE_USER
└────────────────────────────────────────────────────────────────────┘

┌─── 🚦 Endpoint Protection Status ──────────────────────────────────┐
│ ✅ Keycloak Resource-based Authorization ACTIVE
│
│ Protected Endpoints:
│    ├─ /api/employees/group/indonesia → Only 'indonesia' resource
│    ├─ /api/users → Requires 'users' resource
│    └─ All others → Denied by default
└────────────────────────────────────────────────────────────────────┘
```

### 2. API 요청 처리 시

#### 성공적인 요청 (권한 있음)
```
╔════════════════════════════════════════════════════════════════
║ 🔍 REQUEST START [a3f2b891]
╟────────────────────────────────────────────────────────────────
║ Method: GET /api/employees/group/indonesia
║ Remote IP: 192.168.1.100
║ Auth Header: Present
╚════════════════════════════════════════════════════════════════

┌─── 🔐 Token Validation Context ───────────────────────────────
│ Request URI: /api/employees/group/indonesia
│ HTTP Method: GET
│ Resource Check: ✅ ENABLED
│ ⚡ Keycloak UMA authorization will be performed
└────────────────────────────────────────────────────────────────

🔐 Requesting UMA permission check for resource name: indonesia
✅ UMA permission GRANTED for resource: indonesia

✅ AUTH SUCCESS | User: indonesia | Duration: 125ms

┌─── ✅ Token Validation SUCCESS ────────────────────────────────
│ Username: indonesia
│ User ID: af1c566e-b7c2-4862-a66a-94368a3dab3d
│ Duration: 125ms
└────────────────────────────────────────────────────────────────

╔════════════════════════════════════════════════════════════════
║ ✅ REQUEST COMPLETE [a3f2b891]
║ Total Duration: 150ms
╚════════════════════════════════════════════════════════════════
```

#### 실패한 요청 (권한 없음)
```
╔════════════════════════════════════════════════════════════════
║ 🔍 REQUEST START [b7d4c923]
╟────────────────────────────────────────────────────────────────
║ Method: GET /api/users
║ Remote IP: 192.168.1.100
║ Auth Header: Present
╚════════════════════════════════════════════════════════════════

┌─── 🔐 Token Validation Context ───────────────────────────────
│ Request URI: /api/users
│ HTTP Method: GET
│ Resource Check: ✅ ENABLED
│ ⚡ Keycloak UMA authorization will be performed
└────────────────────────────────────────────────────────────────

❌ No resource mapping found for URI: /api/users
❌ Resource permission DENIED for URI: /api/users with method: GET

❌ AUTH FAILED | Duration: 89ms | Reason: Resource Permission Denied

┌─── ❌ Token Validation FAILED ─────────────────────────────────
│ Duration: 89ms
│ Reason: Resource Permission Denied
│ Requested URI: /api/users
│ Requested Method: GET
└────────────────────────────────────────────────────────────────

🔒 Resource Access Denied
   ├─ User: indonesia
   ├─ Resource: GET /api/users
   └─ Time: 2025-08-25T00:45:23

╔════════════════════════════════════════════════════════════════
║ ❌ REQUEST COMPLETE [b7d4c923]
║ Total Duration: 95ms
╚════════════════════════════════════════════════════════════════
```

### 3. 보안 이벤트 통계

```
╔════════════════════════════════════════════════════════════════
║ 🔐 SECURITY EVENT STATISTICS
╟────────────────────────────────────────────────────────────────
║ Authentication:
║   ├─ Success: 245 (78.5%)
║   └─ Failures: 67 (21.5%)
║
║ Resource Access:
║   └─ Denied: 34
║
║ Top Users:
║   ├─ indonesia: 125 requests
║   ├─ korea: 89 requests
║   └─ admin: 45 requests
║
║ Top Endpoints:
║   ├─ /api/employees/group/indonesia: 125 requests
║   ├─ /api/organizations/all: 45 requests
║   └─ /api/positions/all: 32 requests
╚════════════════════════════════════════════════════════════════
```

## 🔧 로그 레벨 설정

### application.yml에서 설정

```yaml
logging:
  level:
    SECURITY.STARTUP: INFO       # 시작 로그
    SECURITY.REQUEST: DEBUG      # 상세 요청 로그
    SECURITY.EVENT: INFO         # 보안 이벤트
    SECURITY.AUDIT: INFO         # 감사 로그
    SECURITY.PERFORMANCE: WARN   # 성능 경고만
    SECURITY.AUTH: INFO          # 인증 이벤트
```

### 환경변수로 설정

```bash
export LOGGING_LEVEL_SECURITY_REQUEST=DEBUG
export LOGGING_LEVEL_SECURITY_EVENT=WARN
```

## 📊 로그 분석 팁

### 1. 권한 거부 추적
```bash
grep "RESOURCE_DENIED" app.log | awk '{print $6, $8}' | sort | uniq -c
```

### 2. 느린 요청 찾기
```bash
grep "SLOW_OPERATION" app.log | grep -E "took [0-9]{4,}ms"
```

### 3. 특정 사용자 활동 추적
```bash
grep "User: indonesia" app.log | grep -E "(AUTH_SUCCESS|RESOURCE_)"
```

### 4. 의심스러운 활동 감지
```bash
grep "SUSPICIOUS_ACTIVITY" app.log
```

## 🎯 문제 진단 체크리스트

로그를 통해 다음 사항을 확인할 수 있습니다:

- [ ] **시작 시**: Resource Permission Check가 ENABLED인가?
- [ ] **시작 시**: 올바른 TokenProvider가 로드되었는가?
- [ ] **시작 시**: Keycloak 설정이 올바른가?
- [ ] **요청 시**: Token이 성공적으로 추출되는가?
- [ ] **요청 시**: Resource Check가 활성화되어 실행되는가?
- [ ] **요청 시**: UMA 권한 체크가 수행되는가?
- [ ] **실패 시**: 정확한 실패 이유가 로그에 나오는가?

## 💡 Best Practices

1. **프로덕션 환경**
   - `SECURITY.REQUEST`를 INFO로 설정 (DEBUG는 개발 환경만)
   - `SECURITY.AUDIT`는 항상 INFO 유지 (규정 준수)
   - 로그를 중앙 로그 시스템으로 전송

2. **개발 환경**
   - 모든 로거를 DEBUG로 설정하여 상세 정보 확인
   - 콘솔 출력 사용

3. **성능 모니터링**
   - `SECURITY.PERFORMANCE` 로그 모니터링
   - 임계값 초과 시 알림 설정

4. **보안 감사**
   - `SECURITY.AUDIT` 로그를 별도 파일로 저장
   - 정기적인 로그 분석 수행