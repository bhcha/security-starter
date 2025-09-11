package com.ldx.hexacore.security.config.support;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * Security-Starter 관련 오류에 대한 개발자 친화적인 분석을 제공합니다.
 * 
 * Bean 누락, 설정 오류 등의 일반적인 문제에 대해 구체적인 해결방안을 제시합니다.
 */
public class SecurityStarterFailureAnalyzer extends AbstractFailureAnalyzer<NoSuchBeanDefinitionException> {
    
    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, NoSuchBeanDefinitionException cause) {
        if (isSecurityStarterRelated(cause)) {
            return new FailureAnalysis(
                getDescription(cause),
                getAction(cause),
                cause
            );
        }
        return null;
    }
    
    private boolean isSecurityStarterRelated(NoSuchBeanDefinitionException cause) {
        String beanName = cause.getBeanName();
        String message = cause.getMessage();
        
        if (beanName != null) {
            return beanName.contains("sessionManagement") ||
                   beanName.contains("authentication") ||
                   beanName.contains("tokenManagement") ||
                   beanName.contains("keycloak") ||
                   beanName.contains("hexacore") ||
                   beanName.contains("security");
        }
        
        if (message != null) {
            return message.contains("com.dx.hexacore.security") ||
                   message.contains("SecurityStarterFailureAnalyzer") ||
                   message.contains("SecurityStarterProperties");
        }
        
        return false;
    }
    
    private String getDescription(NoSuchBeanDefinitionException cause) {
        String beanName = cause.getBeanName();
        String type = cause.getBeanType() != null ? cause.getBeanType().getSimpleName() : "Unknown";
        
        if (beanName == null) {
            beanName = "unknown";
        }
        
        return switch (beanName) {
            case "sessionManagementUseCase" -> 
                "🚫 세션 관리 기능을 찾을 수 없습니다\n\n" +
                "세션 관리는 다음과 같은 기능을 제공합니다:\n" +
                "• 로그인 시도 추적\n" +
                "• 계정 자동 잠금\n" +
                "• 보안 위험 분석\n" +
                "• 계정 잠금 해제\n\n" +
                "이 기능이 필요한 경우 아래 해결방안을 따라주세요.";
                
            case "authenticationUseCase" ->
                "🚫 인증 기능을 찾을 수 없습니다\n\n" +
                "인증 기능은 다음과 같은 역할을 합니다:\n" +
                "• 사용자 로그인 처리\n" +
                "• JWT 토큰 생성\n" +
                "• 인증 상태 관리\n\n" +
                "Security-Starter의 핵심 기능이므로 반드시 활성화되어야 합니다.";
                
            case "tokenManagementUseCase" ->
                "🚫 토큰 관리 기능을 찾을 수 없습니다\n\n" +
                "토큰 관리는 다음과 같은 기능을 제공합니다:\n" +
                "• 토큰 유효성 검증\n" +
                "• 토큰 갱신 (Refresh)\n" +
                "• 토큰 만료 처리\n\n" +
                "JWT 또는 Keycloak 인증에 필수적인 기능입니다.";
                
            case "keycloakTokenProvider", "keycloakAuthenticationProvider" ->
                "🚫 Keycloak 인증 제공자를 찾을 수 없습니다\n\n" +
                "Keycloak 연동에는 다음이 필요합니다:\n" +
                "• Keycloak 서버 URL\n" +
                "• Realm 이름\n" +
                "• Client 설정\n" +
                "• 네트워크 연결\n\n" +
                "Keycloak 서버가 실행 중이고 접근 가능한지 확인해주세요.";
                
            case "springJwtTokenProvider", "jwtTokenProvider" ->
                "🚫 JWT 토큰 제공자를 찾을 수 없습니다\n\n" +
                "JWT 인증에는 다음이 필요합니다:\n" +
                "• JWT 라이브러리 (jjwt)\n" +
                "• 비밀키 (Secret Key)\n" +
                "• 토큰 만료 시간 설정\n\n" +
                "JWT 의존성과 설정을 확인해주세요.";
                
            case "hexacoreSecurityAutoConfiguration" ->
                "🚫 Security-Starter 자동 설정을 찾을 수 없습니다\n\n" +
                "다음 원인이 가능합니다:\n" +
                "• Security-Starter JAR 파일 누락\n" +
                "• Spring Boot 버전 호환성 문제\n" +
                "• ClassPath 설정 오류\n\n" +
                "Security-Starter 의존성과 Spring Boot 버전을 확인해주세요.";
                
            default -> {
                if (beanName.contains("security") || beanName.contains("hexacore")) {
                    yield String.format("🚫 Security-Starter의 '%s' Bean을 찾을 수 없습니다\n\n" +
                        "Bean 타입: %s\n" +
                        "이는 Security-Starter 설정 또는 의존성 문제일 수 있습니다.", beanName, type);
                } else {
                    yield null; // 다른 FailureAnalyzer가 처리하도록
                }
            }
        };
    }
    
    private String getAction(NoSuchBeanDefinitionException cause) {
        String beanName = cause.getBeanName();
        
        if (beanName == null) {
            beanName = "unknown";
        }
        
        return switch (beanName) {
            case "sessionManagementUseCase" ->
                "다음 단계를 순서대로 시도해보세요:\n\n" +
                "1️⃣ 세션 관리 활성화\n" +
                "   application.yml에 다음 설정을 추가하세요:\n" +
                "   ```yaml\n" +
                "   hexacore:\n" +
                "     security:\n" +
                "       session:\n" +
                "         enabled: true\n" +
                "         lockout:\n" +
                "           max-attempts: 5\n" +
                "           lockout-duration-minutes: 30\n" +
                "   ```\n\n" +
                "2️⃣ 데이터베이스 설정 확인\n" +
                "   세션 관리를 위한 데이터베이스 연결이 필요합니다:\n" +
                "   ```yaml\n" +
                "   spring:\n" +
                "     datasource:\n" +
                "       url: jdbc:h2:mem:testdb\n" +
                "       driver-class-name: org.h2.Driver\n" +
                "     jpa:\n" +
                "       hibernate:\n" +
                "         ddl-auto: create-drop\n" +
                "   ```\n\n" +
                "3️⃣ 애플리케이션 재시작\n\n" +
                "📖 관련 문서: https://docs.security-starter.com/session-management";
                
            case "authenticationUseCase" ->
                "다음 설정을 확인해주세요:\n\n" +
                "1️⃣ Security-Starter 활성화\n" +
                "   ```yaml\n" +
                "   hexacore:\n" +
                "     security:\n" +
                "       enabled: true\n" +
                "   ```\n\n" +
                "2️⃣ 토큰 제공자 설정\n" +
                "   JWT 사용 시:\n" +
                "   ```yaml\n" +
                "   hexacore:\n" +
                "     security:\n" +
                "       token-provider:\n" +
                "         provider: jwt\n" +
                "         jwt:\n" +
                "           enabled: true\n" +
                "           secret: your-secret-key\n" +
                "   ```\n\n" +
                "3️⃣ 의존성 확인\n" +
                "   ```gradle\n" +
                "   implementation 'com.dx:security-starter:1.0.1'\n" +
                "   ```\n\n" +
                "📖 관련 문서: https://docs.security-starter.com/authentication";
                
            case "tokenManagementUseCase" ->
                "토큰 관리 설정을 확인해주세요:\n\n" +
                "1️⃣ 토큰 제공자 선택\n" +
                "   ```yaml\n" +
                "   hexacore:\n" +
                "     security:\n" +
                "       token-provider:\n" +
                "         provider: jwt  # 또는 keycloak\n" +
                "   ```\n\n" +
                "2️⃣ JWT 설정 (JWT 선택 시)\n" +
                "   ```yaml\n" +
                "   hexacore:\n" +
                "     security:\n" +
                "       token-provider:\n" +
                "         jwt:\n" +
                "           enabled: true\n" +
                "           secret: ${JWT_SECRET:your-secret}\n" +
                "           access-token-expiration: 3600\n" +
                "           refresh-token-expiration: 604800\n" +
                "   ```\n\n" +
                "📖 관련 문서: https://docs.security-starter.com/token-management";
                
            case "keycloakTokenProvider", "keycloakAuthenticationProvider" ->
                "Keycloak 설정을 확인하고 다음과 같이 구성하세요:\n\n" +
                "1️⃣ Keycloak 서버 확인\n" +
                "   • Keycloak 서버가 실행 중인지 확인\n" +
                "   • 네트워크 연결 상태 점검\n" +
                "   • 방화벽 설정 확인\n\n" +
                "2️⃣ Keycloak 설정\n" +
                "   ```yaml\n" +
                "   hexacore:\n" +
                "     security:\n" +
                "       token-provider:\n" +
                "         provider: keycloak\n" +
                "         keycloak:\n" +
                "           enabled: true\n" +
                "           server-url: http://localhost:8080\n" +
                "           realm: your-realm\n" +
                "           client-id: your-client\n" +
                "           client-secret: your-secret\n" +
                "   ```\n\n" +
                "3️⃣ Keycloak 의존성 확인\n" +
                "   ```gradle\n" +
                "   implementation 'org.keycloak:keycloak-admin-client:26.0.0'\n" +
                "   ```\n\n" +
                "📖 관련 문서: https://docs.security-starter.com/keycloak-integration";
                
            case "springJwtTokenProvider", "jwtTokenProvider" ->
                "JWT 설정을 확인해주세요:\n\n" +
                "1️⃣ JWT 의존성 확인\n" +
                "   Security-Starter에 JWT 라이브러리가 포함되어 있지만,\n" +
                "   ClassPath에서 찾을 수 없는 경우입니다.\n\n" +
                "2️⃣ JWT 설정 확인\n" +
                "   ```yaml\n" +
                "   hexacore:\n" +
                "     security:\n" +
                "       token-provider:\n" +
                "         provider: jwt\n" +
                "         jwt:\n" +
                "           enabled: true\n" +
                "           secret: ${JWT_SECRET}\n" +
                "   ```\n\n" +
                "3️⃣ 환경 변수 설정\n" +
                "   ```bash\n" +
                "   export JWT_SECRET=\"your-256-bit-secret-key\"\n" +
                "   ```\n\n" +
                "📖 관련 문서: https://docs.security-starter.com/jwt-authentication";
                
            case "hexacoreSecurityAutoConfiguration" ->
                "Security-Starter 설정 문제를 해결해주세요:\n\n" +
                "1️⃣ 의존성 확인\n" +
                "   ```gradle\n" +
                "   dependencies {\n" +
                "       implementation 'com.dx:security-starter:1.0.1'\n" +
                "   }\n" +
                "   ```\n\n" +
                "2️⃣ Spring Boot 버전 확인\n" +
                "   Security-Starter는 Spring Boot 3.5.x 이상이 필요합니다:\n" +
                "   ```gradle\n" +
                "   id 'org.springframework.boot' version '3.5.4'\n" +
                "   ```\n\n" +
                "3️⃣ JAR 파일 확인\n" +
                "   • Build 디렉토리에 security-starter JAR 파일 존재 여부\n" +
                "   • ClassPath에 올바르게 포함되었는지 확인\n\n" +
                "4️⃣ 기본 설정 추가\n" +
                "   ```yaml\n" +
                "   hexacore:\n" +
                "     security:\n" +
                "       enabled: true\n" +
                "   ```\n\n" +
                "📖 관련 문서: https://docs.security-starter.com/getting-started";
                
            default -> {
                if (beanName.contains("security") || beanName.contains("hexacore")) {
                    yield "Security-Starter 설정을 확인하고 관련 문서를 참조하세요:\n\n" +
                        "1️⃣ 기본 설정 확인\n" +
                        "   ```yaml\n" +
                        "   hexacore:\n" +
                        "     security:\n" +
                        "       enabled: true\n" +
                        "   ```\n\n" +
                        "2️⃣ 의존성 확인\n" +
                        "   ```gradle\n" +
                        "   implementation 'com.dx:security-starter:1.0.1'\n" +
                        "   ```\n\n" +
                        "3️⃣ 애플리케이션 로그 확인\n" +
                        "   시작 로그에서 Security-Starter 관련 오류 메시지를 찾아보세요.\n\n" +
                        "📖 문서: https://docs.security-starter.com/troubleshooting\n" +
                        "🆘 지원: https://github.com/your-org/security-starter/issues";
                } else {
                    yield null;
                }
            }
        };
    }
}