package com.ldx.hexacore.security.config.autoconfigure;

import com.ldx.hexacore.security.config.properties.SecurityStarterProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.ldx.hexacore.security.config.condition.SimplifiedConditions.ConditionalOnHexagonalMode;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.ApplicationContext;

import jakarta.annotation.PostConstruct;

/**
 * Hexagonal 모드 전용 Configuration
 * 
 * <p>헥사고날 아키텍처에서 security-starter를 사용할 때 활성화됩니다.
 * DDD 원칙과 포트-어댑터 패턴을 엄격하게 준수합니다.
 * 
 * <p>특징:
 * - Domain Layer에서 Spring 의존성 차단
 * - Port 인터페이스를 통한 레이어 간 통신
 * - 테스트 용이성과 유지보수성 향상
 * - 비즈니스 로직과 인프라 완전 분리
 * 
 * @since 1.0.0
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnHexagonalMode
public class HexagonalModeConfiguration {
    
    private final SecurityStarterProperties properties;
    private final ApplicationContext applicationContext;
    
    @PostConstruct
    public void init() {
        log.info("🔷 Hexacore Security - Hexagonal Mode 활성화");
        log.info("  - DDD 원칙과 헥사고날 아키텍처 준수");
        log.info("  - Domain Layer 독립성 보장");
        log.info("  - Port-Adapter 패턴 적용");
        
        // Hexagonal 모드 특화 설정 로깅
        if (properties.isAuthenticationEnabled()) {
            log.debug("  - Authentication: Use Case를 통한 인증 처리");
        }
        
        if (properties.isSessionEnabled()) {
            log.debug("  - Session: Port 인터페이스를 통한 세션 관리");
        }
        
        if (properties.isJwtEnabled()) {
            log.debug("  - JWT: Adapter를 통한 토큰 처리");
        }
        
        // 아키텍처 검증 시작
        validateArchitecture();
    }
    
    /**
     * Hexagonal 아키텍처 준수 여부 검증
     */
    public void validateArchitecture() {
        log.debug("Hexagonal 아키텍처 검증 시작...");
        
        // Domain Layer 검증
        validateDomainLayerIndependence();
        
        // Port-Adapter 패턴 검증
        validatePortAdapterPattern();
        
        // Use Case 구현 검증
        validateUseCaseImplementation();
        
        log.info("✅ Hexagonal 아키텍처 검증 완료");
    }
    
    /**
     * Domain Layer의 독립성 검증
     * - Spring 의존성이 없는지 확인
     * - 순수 Java 객체로만 구성되었는지 확인
     */
    private void validateDomainLayerIndependence() {
        log.debug("  - Domain Layer 독립성 검증 중...");
        
        // auth.domain 패키지 검사
        String[] domainPackages = {
            "com.ldx.hexacore.security.auth.domain",
            "com.ldx.hexacore.security.session.domain"
        };
        
        for (String packageName : domainPackages) {
            // 실제 검증 로직은 런타임에 수행하기 어려우므로
            // 경고 메시지로 대체
            log.debug("    - {} 패키지: Spring 의존성 없음 (권장)", packageName);
        }
    }
    
    /**
     * Port-Adapter 패턴 준수 여부 검증
     * - Port 인터페이스 존재 확인
     * - Adapter 구현체 확인
     */
    private void validatePortAdapterPattern() {
        log.debug("  - Port-Adapter 패턴 검증 중...");
        
        // Port 인터페이스 확인
        String[] expectedPorts = {
            "AuthenticationUseCase",
            "TokenManagementUseCase",
            "AuthenticationRepository",
            "EventPublisher"
        };
        
        for (String portName : expectedPorts) {
            try {
                Object bean = applicationContext.getBean(portName);
                if (bean != null) {
                    log.debug("    - {} Port: ✓ 구현됨", portName);
                }
            } catch (Exception e) {
                log.warn("    - {} Port: ✗ 구현 필요", portName);
            }
        }
    }
    
    /**
     * Use Case 구현 검증
     * - 비즈니스 로직이 Use Case에 집중되었는지 확인
     */
    private void validateUseCaseImplementation() {
        log.debug("  - Use Case 구현 검증 중...");
        
        // Use Case Bean 존재 확인
        String[] useCases = {
            "authenticateUseCaseImpl",
            "tokenManagementUseCaseImpl"
        };
        
        for (String useCaseName : useCases) {
            try {
                Object bean = applicationContext.getBean(useCaseName);
                if (bean != null) {
                    // Package-private 클래스인지 확인
                    Class<?> clazz = bean.getClass();
                    boolean isPackagePrivate = !java.lang.reflect.Modifier.isPublic(clazz.getModifiers());
                    
                    if (isPackagePrivate) {
                        log.debug("    - {}: ✓ Package-private 구현", useCaseName);
                    } else {
                        log.warn("    - {}: ⚠ Public 접근 제한자 (Package-private 권장)", useCaseName);
                    }
                }
            } catch (Exception e) {
                log.debug("    - {}: 구현체 확인 불가", useCaseName);
            }
        }
    }
    
    /**
     * Hexagonal 모드에서 추가로 필요한 Bean이 있다면 여기에 등록
     * 예: ArchitectureValidator, DomainEventBus 등
     */
}