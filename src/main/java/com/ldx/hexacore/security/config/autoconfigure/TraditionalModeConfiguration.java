package com.ldx.hexacore.security.config.autoconfigure;

import com.ldx.hexacore.security.config.properties.SecurityStarterProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.ldx.hexacore.security.config.condition.SimplifiedConditions.ConditionalOnTraditionalMode;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Traditional 모드 전용 Configuration
 * 
 * <p>전통적인 MVC 아키텍처에서 security-starter를 사용할 때 활성화됩니다.
 * 모든 레이어에서 자유롭게 사용 가능한 설정을 제공합니다.
 * 
 * <p>특징:
 * - Controller에서 직접 Domain 객체 사용 가능
 * - Service Layer에서 직접 Repository 접근 가능
 * - 단순한 설정과 빠른 개발 속도
 * 
 * @since 1.0.0
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnTraditionalMode
public class TraditionalModeConfiguration {
    
    private final SecurityStarterProperties properties;
    
    @PostConstruct
    public void init() {
        log.info("🔧 Hexacore Security - Traditional Mode 활성화");
        log.info("  - 모든 레이어에서 자유로운 사용 가능");
        log.info("  - 빠른 개발과 단순한 구조 제공");
        
        // Traditional 모드 특화 설정 로깅
        if (properties.isAuthenticationEnabled()) {
            log.debug("  - Authentication: 직접 도메인 객체 사용 가능");
        }
        
        if (properties.isSessionEnabled()) {
            log.debug("  - Session: 컨트롤러에서 직접 세션 관리 가능");
        }
        
        if (properties.isJwtEnabled()) {
            log.debug("  - JWT: 간편한 토큰 처리 지원");
        }
    }
    
    /**
     * Traditional 모드에서는 추가적인 검증 없이 모든 기능 사용 가능
     */
    public void validateConfiguration() {
        log.debug("Traditional 모드 설정 검증 완료 - 제약 없음");
    }
    
    /**
     * Traditional 모드 특화 Bean 등록이 필요한 경우 이곳에 추가
     * 예: SimplifiedAuthenticationService, DirectAccessRepository 등
     */
}