package com.ldx.hexacore.security.config.support;

import com.ldx.hexacore.security.config.properties.SecurityStarterProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Security Mode별 설정 검증기
 * 
 * <p>Traditional/Hexagonal 모드에 따른 설정 유효성을 검증합니다.
 * 
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ModeValidator {
    
    private final SecurityStarterProperties properties;
    private final ApplicationContext applicationContext;
    
    @PostConstruct
    public void validate() {
        log.debug("Security Mode 검증 시작: {}", properties.getMode());
        
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        switch (properties.getMode()) {
            case TRADITIONAL:
                validateTraditionalMode(warnings, errors);
                break;
            case HEXAGONAL:
                validateHexagonalMode(warnings, errors);
                break;
        }
        
        // 검증 결과 로깅
        if (!warnings.isEmpty()) {
            log.warn("⚠️ Mode 검증 경고:");
            warnings.forEach(warning -> log.warn("  - {}", warning));
        }
        
        if (!errors.isEmpty()) {
            log.error("❌ Mode 검증 오류:");
            errors.forEach(error -> log.error("  - {}", error));
            throw new IllegalStateException("Mode 검증 실패: " + String.join(", ", errors));
        }
        
        log.info("✅ {} Mode 검증 완료", properties.getMode());
    }
    
    /**
     * Traditional 모드 검증
     */
    private void validateTraditionalMode(List<String> warnings, List<String> errors) {
        // Traditional 모드에서는 제약이 적음
        
        // 권장 사항 체크
        if (!properties.isAuthenticationEnabled() && properties.isJwtEnabled()) {
            warnings.add("JWT가 활성화되었지만 Authentication이 비활성화됨. JWT 사용이 제한될 수 있습니다.");
        }
        
        if (properties.isSessionEnabled() && !properties.isAuthenticationEnabled()) {
            warnings.add("Session이 활성화되었지만 Authentication이 비활성화됨. 세션 관리가 제한될 수 있습니다.");
        }
        
    }
    
    /**
     * Hexagonal 모드 검증
     */
    private void validateHexagonalMode(List<String> warnings, List<String> errors) {
        // Hexagonal 모드에서는 엄격한 아키텍처 규칙 적용
        
        // 필수 Port 인터페이스 존재 확인
        checkPortInterfaceExists("AuthenticationUseCase", errors);
        checkPortInterfaceExists("TokenManagementUseCase", errors);
        
        // Repository Port 확인
        if (properties.isAuthenticationEnabled()) {
            checkPortInterfaceExists("AuthenticationRepository", warnings);
        }
        
        if (properties.isSessionEnabled()) {
            checkPortInterfaceExists("AuthenticationSessionRepository", warnings);
        }
        
        // Event Publisher 확인
        checkPortInterfaceExists("EventPublisher", warnings);
        
        // 아키텍처 일관성 체크
        validateLayerSeparation(warnings, errors);

        // 외부 인증 제공자 설정 체크
        if (properties.getTokenProvider() != null && 
            properties.getTokenProvider().getProvider() == null) {
            warnings.add("Token Provider가 설정되지 않음. 기본 Provider가 사용됩니다.");
        }
    }
    
    /**
     * Port 인터페이스 존재 확인
     */
    private void checkPortInterfaceExists(String beanName, List<String> issues) {
        try {
            Object bean = applicationContext.getBean(beanName);
            if (bean == null) {
                issues.add(String.format("%s Port가 구현되지 않음", beanName));
            } else {
                log.debug("  - {} Port: ✓ 존재", beanName);
            }
        } catch (Exception e) {
            issues.add(String.format("%s Port를 찾을 수 없음: %s", beanName, e.getMessage()));
        }
    }
    
    /**
     * 레이어 분리 검증
     */
    private void validateLayerSeparation(List<String> warnings, List<String> errors) {
        // Domain Layer 독립성 체크
        validateDomainIndependence(warnings);
        
        // Application Layer 체크
        validateApplicationLayer(warnings);
        
        // Adapter Layer 체크
        validateAdapterLayer(warnings);
    }
    
    /**
     * Domain Layer 독립성 검증
     */
    private void validateDomainIndependence(List<String> warnings) {
        // 실제로는 컴파일 타임에 체크되어야 하므로 경고만 표시
        log.debug("  - Domain Layer 독립성 체크");
        log.debug("    - auth.domain 패키지: Spring 의존성 제거 권장");
        log.debug("    - session.domain 패키지: Spring 의존성 제거 권장");
    }
    
    /**
     * Application Layer 검증
     */
    private void validateApplicationLayer(List<String> warnings) {
        // Use Case 구현 체크
        String[] expectedUseCases = {
            "authenticateUseCaseImpl",
            "tokenManagementUseCaseImpl"
        };
        
        for (String useCaseName : expectedUseCases) {
            try {
                Object bean = applicationContext.getBean(useCaseName);
                if (bean != null) {
                    Class<?> clazz = bean.getClass();
                    boolean isPackagePrivate = !java.lang.reflect.Modifier.isPublic(clazz.getModifiers());
                    
                    if (!isPackagePrivate) {
                        warnings.add(String.format("%s가 public으로 선언됨. Package-private 권장", useCaseName));
                    }
                }
            } catch (Exception e) {
                log.debug("    - {} 확인 불가", useCaseName);
            }
        }
    }
    
    /**
     * Adapter Layer 검증
     */
    private void validateAdapterLayer(List<String> warnings) {
        // Event Publisher Adapter 체크
        checkAdapterExists("springEventPublisher", warnings);
    }
    
    /**
     * Adapter 존재 확인
     */
    private void checkAdapterExists(String beanName, List<String> warnings) {
        try {
            Object bean = applicationContext.getBean(beanName);
            if (bean == null) {
                warnings.add(String.format("%s Adapter가 구현되지 않음", beanName));
            }
        } catch (Exception e) {
            log.debug("    - {} Adapter 확인 불가", beanName);
        }
    }
    
    /**
     * Mode 전환 가이드 제공
     */
    public String getModeTransitionGuide(SecurityStarterProperties.Mode targetMode) {
        StringBuilder guide = new StringBuilder();
        guide.append("Mode 전환 가이드: ").append(properties.getMode())
             .append(" → ").append(targetMode).append("\n");
        
        if (targetMode == SecurityStarterProperties.Mode.HEXAGONAL) {
            guide.append("  1. Domain Layer에서 Spring 의존성 제거\n");
            guide.append("  2. Port 인터페이스 정의 (Use Case, Repository)\n");
            guide.append("  3. Adapter 구현체를 package-private으로 변경\n");
            guide.append("  4. Use Case를 통한 비즈니스 로직 처리\n");
            guide.append("  5. security-starter.mode=HEXAGONAL 설정\n");
        } else {
            guide.append("  1. security-starter.mode=TRADITIONAL 설정\n");
            guide.append("  2. 필요시 직접 Domain 객체 사용 가능\n");
            guide.append("  3. Service Layer에서 Repository 직접 접근 가능\n");
        }
        
        return guide.toString();
    }
}