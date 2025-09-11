package com.ldx.hexacore.security.config.support;

import com.ldx.hexacore.security.config.properties.SecurityStarterProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Mode 전환 감지 및 가이드 제공 리스너
 * 
 * <p>애플리케이션 시작 시 Mode 설정을 확인하고 적절한 가이드를 제공합니다.
 * 
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class ModeTransitionListener implements ApplicationListener<ApplicationReadyEvent> {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ModeTransitionListener.class);
    
    private final SecurityStarterProperties properties;
    private final Environment environment;
    private final ModeValidator modeValidator;
    
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        checkModeConsistency();
        provideModeGuidance();
    }
    
    /**
     * Mode 설정 일관성 체크
     */
    private void checkModeConsistency() {
        SecurityStarterProperties.Mode currentMode = properties.getMode();
        
        // 프로파일과 Mode 일치 여부 체크
        boolean hasDevProfile = environment.acceptsProfiles("dev", "development");
        boolean hasProdProfile = environment.acceptsProfiles("prod", "production");
        
        if (hasProdProfile && currentMode == SecurityStarterProperties.Mode.TRADITIONAL) {
            log.warn("⚠️ Production 환경에서 Traditional 모드 사용 중");
            log.warn("   Hexagonal 모드 사용을 권장합니다: security-starter.mode=HEXAGONAL");
        }
        
        if (hasDevProfile && currentMode == SecurityStarterProperties.Mode.HEXAGONAL) {
            log.info("💡 Development 환경에서 Hexagonal 모드 사용 중");
            log.info("   빠른 개발을 위해 Traditional 모드도 고려해보세요");
        }
    }
    
    /**
     * Mode별 사용 가이드 제공
     */
    private void provideModeGuidance() {
        SecurityStarterProperties.Mode currentMode = properties.getMode();
        
        log.info("=============================================================");
        log.info("🔧 Hexacore Security Mode: {}", currentMode);
        log.info("=============================================================");
        
        switch (currentMode) {
            case TRADITIONAL:
                provideTraditionalModeGuidance();
                break;
            case HEXAGONAL:
                provideHexagonalModeGuidance();
                break;
        }
        
        log.info("=============================================================");
    }
    
    /**
     * Traditional 모드 가이드
     */
    private void provideTraditionalModeGuidance() {
        log.info("📌 Traditional 모드 사용 가이드:");
        log.info("  ✓ Controller에서 Domain 객체 직접 사용 가능");
        log.info("  ✓ Service에서 Repository 직접 접근 가능");
        log.info("  ✓ 빠른 프로토타이핑과 개발 속도 우선");
        
        if (properties.isAuthenticationEnabled()) {
            log.info("  - Authentication: @Autowired AuthenticationService 사용");
        }
        
        if (properties.isJwtEnabled()) {
            log.info("  - JWT: TokenService를 통한 간편한 토큰 처리");
        }
        
        if (properties.isSessionEnabled()) {
            log.info("  - Session: SessionManager를 통한 직접 세션 관리");
        }
        
        log.info("");
        log.info("💡 팁: Production 환경에서는 Hexagonal 모드 전환을 고려하세요");
        log.info("     설정: security-starter.mode=HEXAGONAL");
    }
    
    /**
     * Hexagonal 모드 가이드
     */
    private void provideHexagonalModeGuidance() {
        log.info("🔷 Hexagonal 모드 사용 가이드:");
        log.info("  ✓ Domain Layer 독립성 보장");
        log.info("  ✓ Port-Adapter 패턴 준수");
        log.info("  ✓ 테스트 용이성과 유지보수성 향상");
        
        if (properties.isAuthenticationEnabled()) {
            log.info("  - Authentication: AuthenticationUseCase 인터페이스 사용");
            log.info("    @Autowired AuthenticationUseCase authenticationUseCase;");
        }
        
        if (properties.isJwtEnabled()) {
            log.info("  - JWT: TokenManagementUseCase를 통한 토큰 처리");
            log.info("    @Autowired TokenManagementUseCase tokenManagementUseCase;");
        }
        
        if (properties.isSessionEnabled()) {
            log.info("  - Session: Port 인터페이스를 통한 세션 관리");
            log.info("    @Autowired SessionManagementPort sessionPort;");
        }
        
        log.info("");
        log.info("💡 Best Practices:");
        log.info("  - Domain 객체는 순수 Java로 구현");
        log.info("  - Use Case에 비즈니스 로직 집중");
        log.info("  - Adapter는 package-private으로 구현");
    }
    
    /**
     * Mode 불일치 경고
     */
    public void warnModeMismatch(String component, SecurityStarterProperties.Mode expectedMode) {
        SecurityStarterProperties.Mode currentMode = properties.getMode();
        
        if (currentMode != expectedMode) {
            log.warn("⚠️ Mode 불일치 감지:");
            log.warn("  - Component: {}", component);
            log.warn("  - 현재 Mode: {}", currentMode);
            log.warn("  - 예상 Mode: {}", expectedMode);
            log.warn("  - 해결 방법: security-starter.mode={} 설정", expectedMode);
            
            // Mode 전환 가이드 제공
            String guide = modeValidator.getModeTransitionGuide(expectedMode);
            log.info("\n{}", guide);
        }
    }
}