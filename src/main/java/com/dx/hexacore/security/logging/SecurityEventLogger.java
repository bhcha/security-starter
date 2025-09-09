package com.dx.hexacore.security.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 보안 관련 이벤트를 중앙집중식으로 로깅하는 컴포넌트
 * 
 * 인증 성공/실패, 권한 거부, 의심스러운 활동 등을 추적합니다.
 */
@Component
public class SecurityEventLogger {
    
    private static final Logger eventLogger = LoggerFactory.getLogger("SECURITY.EVENT");
    private static final Logger auditLogger = LoggerFactory.getLogger("SECURITY.AUDIT");
    
    // 통계 추적
    private final AtomicLong authSuccessCount = new AtomicLong(0);
    private final AtomicLong authFailureCount = new AtomicLong(0);
    private final AtomicLong resourceDeniedCount = new AtomicLong(0);
    private final ConcurrentHashMap<String, AtomicLong> userAccessCount = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> endpointAccessCount = new ConcurrentHashMap<>();
    
    // 의심스러운 활동 추적
    private final ConcurrentHashMap<String, SuspiciousActivityTracker> ipActivityMap = new ConcurrentHashMap<>();
    
    // SecurityConstants에서 주입받을 설정값들
    private final com.dx.hexacore.security.config.SecurityConstants securityConstants;
    
    /**
     * SecurityEventLogger 생성자
     * 
     * @param securityConstants 보안 관련 상수 설정
     */
    public SecurityEventLogger(com.dx.hexacore.security.config.SecurityConstants securityConstants) {
        this.securityConstants = securityConstants;
    }
    
    /**
     * 인증 성공 이벤트
     */
    public void logAuthenticationSuccess(String username, String clientIp, String userAgent) {
        authSuccessCount.incrementAndGet();
        userAccessCount.computeIfAbsent(username, k -> new AtomicLong(0)).incrementAndGet();
        
        auditLogger.info("AUTH_SUCCESS | User: {} | IP: {} | Agent: {}", 
            username, clientIp, truncate(userAgent, securityConstants.getLogging().getMaxLogMessageLength()));
        
        if (eventLogger.isDebugEnabled()) {
            eventLogger.debug("✅ Authentication Success Event");
            eventLogger.debug("   └─ User: {}", username);
            eventLogger.debug("   └─ IP: {}", clientIp);
            eventLogger.debug("   └─ Time: {}", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
    }
    
    /**
     * 인증 실패 이벤트
     */
    public void logAuthenticationFailure(String reason, String clientIp, String userAgent) {
        authFailureCount.incrementAndGet();
        
        auditLogger.warn("AUTH_FAILURE | Reason: {} | IP: {} | Agent: {}", 
            reason, clientIp, truncate(userAgent, securityConstants.getLogging().getMaxLogMessageLength()));
        
        eventLogger.warn("❌ Authentication Failure Event");
        eventLogger.warn("   └─ Reason: {}", reason);
        eventLogger.warn("   └─ IP: {}", clientIp);
        
        // 동일 IP에서 반복된 실패 감지
        checkSuspiciousActivity(clientIp);
    }
    
    /**
     * 리소스 접근 거부 이벤트
     */
    public void logResourceAccessDenied(String username, String uri, String method) {
        resourceDeniedCount.incrementAndGet();
        
        auditLogger.warn("RESOURCE_DENIED | User: {} | {} {}", username, method, uri);
        
        eventLogger.warn("🔒 Resource Access Denied");
        eventLogger.warn("   ├─ User: {}", username);
        eventLogger.warn("   ├─ Resource: {} {}", method, uri);
        eventLogger.warn("   └─ Time: {}", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
    
    /**
     * 리소스 접근 허용 이벤트
     */
    public void logResourceAccessGranted(String username, String uri, String method) {
        endpointAccessCount.computeIfAbsent(uri, k -> new AtomicLong(0)).incrementAndGet();
        
        if (auditLogger.isDebugEnabled()) {
            auditLogger.debug("RESOURCE_GRANTED | User: {} | {} {}", username, method, uri);
        }
        
        if (eventLogger.isDebugEnabled()) {
            eventLogger.debug("🔓 Resource Access Granted");
            eventLogger.debug("   ├─ User: {}", username);
            eventLogger.debug("   └─ Resource: {} {}", method, uri);
        }
    }
    
    /**
     * 토큰 만료 이벤트
     */
    public void logTokenExpired(String username) {
        auditLogger.info("TOKEN_EXPIRED | User: {}", username);
        
        eventLogger.info("⏰ Token Expired Event");
        eventLogger.info("   └─ User: {}", username);
    }
    
    /**
     * 의심스러운 활동 감지
     */
    public void logSuspiciousActivity(String type, String details, String clientIp) {
        auditLogger.error("🚨 SUSPICIOUS_ACTIVITY | Type: {} | Details: {} | IP: {}", 
            type, details, clientIp);
        
        eventLogger.error("🚨 SUSPICIOUS ACTIVITY DETECTED");
        eventLogger.error("   ├─ Type: {}", type);
        eventLogger.error("   ├─ Details: {}", details);
        eventLogger.error("   ├─ IP: {}", clientIp);
        eventLogger.error("   └─ Time: {}", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
    
    /**
     * 설정 변경 이벤트
     */
    public void logConfigurationChange(String setting, String oldValue, String newValue) {
        auditLogger.info("CONFIG_CHANGE | Setting: {} | Old: {} | New: {}", 
            setting, oldValue, newValue);
        
        eventLogger.info("⚙️  Configuration Change Event");
        eventLogger.info("   ├─ Setting: {}", setting);
        eventLogger.info("   ├─ Old Value: {}", oldValue);
        eventLogger.info("   └─ New Value: {}", newValue);
    }
    
    /**
     * 보안 통계 로깅 (주기적 호출용)
     */
    public void logSecurityStatistics() {
        long total = authSuccessCount.get() + authFailureCount.get();
        
        eventLogger.info("╔════════════════════════════════════════════════════════════════");
        eventLogger.info("║ 🔐 SECURITY EVENT STATISTICS");
        eventLogger.info("╟────────────────────────────────────────────────────────────────");
        eventLogger.info("║ Authentication:");
        eventLogger.info("║   ├─ Success: {} ({:.1f}%)", 
            authSuccessCount.get(), 
            total > 0 ? (authSuccessCount.get() * 100.0 / total) : 0);
        eventLogger.info("║   └─ Failures: {} ({:.1f}%)", 
            authFailureCount.get(),
            total > 0 ? (authFailureCount.get() * 100.0 / total) : 0);
        eventLogger.info("║");
        eventLogger.info("║ Resource Access:");
        eventLogger.info("║   └─ Denied: {}", resourceDeniedCount.get());
        eventLogger.info("║");
        eventLogger.info("║ Top Users:");
        userAccessCount.entrySet().stream()
            .sorted((e1, e2) -> Long.compare(e2.getValue().get(), e1.getValue().get()))
            .limit(securityConstants.getLogging().getTopStatsLimit())
            .forEach(entry -> 
                eventLogger.info("║   ├─ {}: {} requests", entry.getKey(), entry.getValue().get()));
        eventLogger.info("║");
        eventLogger.info("║ Top Endpoints:");
        endpointAccessCount.entrySet().stream()
            .sorted((e1, e2) -> Long.compare(e2.getValue().get(), e1.getValue().get()))
            .limit(securityConstants.getLogging().getTopStatsLimit())
            .forEach(entry -> 
                eventLogger.info("║   ├─ {}: {} requests", entry.getKey(), entry.getValue().get()));
        eventLogger.info("╚════════════════════════════════════════════════════════════════");
    }
    
    /**
     * 통계 리셋
     */
    public void resetStatistics() {
        authSuccessCount.set(0);
        authFailureCount.set(0);
        resourceDeniedCount.set(0);
        userAccessCount.clear();
        endpointAccessCount.clear();
        
        eventLogger.info("📊 Security statistics have been reset");
    }
    
    /**
     * IP별 의심스러운 활동을 감지하고 경고를 출력합니다.
     * 
     * <p>지정된 시간 윈도우 내에서 임계값을 초과하는 인증 실패가 발생하면
     * 의심스러운 활동으로 간주하고 경고 로그를 출력합니다.</p>
     * 
     * @param clientIp 검사할 클라이언트 IP 주소
     */
    private void checkSuspiciousActivity(String clientIp) {
        if (clientIp == null || clientIp.trim().isEmpty()) {
            return; // 유효하지 않은 IP는 무시
        }
        
        // IP별 활동 트래커 생성 또는 조회
        SuspiciousActivityTracker tracker = ipActivityMap.computeIfAbsent(
            clientIp, 
            ip -> new SuspiciousActivityTracker(securityConstants.getSuspiciousActivityTimeWindow())
        );
        
        // 현재 시각에 실패 기록 추가
        int currentFailureCount = tracker.addFailure(LocalDateTime.now());
        
        // 임계값 초과 시 의심스러운 활동으로 판단
        if (currentFailureCount >= securityConstants.getLogging().getSuspiciousActivityThreshold()) {
            eventLogger.warn("🚨 SUSPICIOUS ACTIVITY DETECTED!");
            eventLogger.warn("   └─ IP: {} ({} failures within {} minutes)", 
                           clientIp, currentFailureCount, securityConstants.getSuspiciousActivityTimeWindow().toMinutes());
            eventLogger.warn("   └─ Time: {}", 
                           LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            // Audit 로그에도 기록 (보안 모니터링용)
            auditLogger.warn("SUSPICIOUS_ACTIVITY | IP: {} | Failures: {} | Window: {}min", 
                           clientIp, currentFailureCount, securityConstants.getSuspiciousActivityTimeWindow().toMinutes());
            
            // 추가 보안 조치가 필요한 경우 여기에 구현
            // 예: 이벤트 발행, 외부 시스템 알림 등
            publishSuspiciousActivityEvent(clientIp, currentFailureCount);
        } else if (eventLogger.isDebugEnabled()) {
            // 디버그 레벨에서 추적 정보 출력
            eventLogger.debug("🔍 Tracking failures for IP: {} ({}/{})", 
                            clientIp, currentFailureCount, securityConstants.getLogging().getSuspiciousActivityThreshold());
        }
    }
    
    /**
     * 의심스러운 활동 감지 이벤트를 발행합니다.
     * 
     * <p>현재는 로깅만 수행하지만, 향후 외부 시스템 연동이나
     * 추가적인 보안 조치를 위한 확장 포인트입니다.</p>
     * 
     * @param clientIp 의심스러운 IP 주소
     * @param failureCount 실패 횟수
     */
    private void publishSuspiciousActivityEvent(String clientIp, int failureCount) {
        // 현재는 추가 로깅만 수행 (향후 확장 가능)
        auditLogger.info("SUSPICIOUS_ACTIVITY_EVENT | IP: {} | Failures: {} | Timestamp: {}", 
                        clientIp, failureCount, System.currentTimeMillis());
        
        // TODO: 향후 추가할 수 있는 보안 조치들
        // - Spring Application Event 발행
        // - 외부 보안 시스템 알림
        // - IP 차단 요청
        // - 관리자 알림 이메일 발송
    }
    
    private String truncate(String str, int maxLength) {
        if (str == null) return "N/A";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength) + "...";
    }
}