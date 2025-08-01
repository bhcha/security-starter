package com.example.securitytest.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

/**
 * 관리자 전용 엔드포인트 컨트롤러
 * 
 * ADMIN 권한이 필요한 엔드포인트들을 제공하여
 * 권한 기반 접근 제어가 정상 동작하는지 테스트합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    /**
     * 시스템 상태 조회 (관리자 전용)
     */
    @GetMapping("/system-status")
    public ResponseEntity<?> getSystemStatus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        log.info("System status requested by admin: {}", username);
        
        return ResponseEntity.ok(Map.of(
            "systemStatus", "HEALTHY",
            "uptime", "5 hours 23 minutes",
            "activeUsers", 42,
            "memoryUsage", "512MB / 2GB",
            "diskUsage", "45GB / 100GB",
            "lastBackup", LocalDateTime.now().minusHours(2),
            "requestedBy", username,
            "timestamp", LocalDateTime.now()
        ));
    }

    /**
     * 사용자 관리 (관리자 전용)
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        log.info("User list requested by admin: {}", username);
        
        return ResponseEntity.ok(Map.of(
            "users", List.of(
                Map.of("id", 1, "username", "user1", "email", "user1@example.com", "status", "ACTIVE"),
                Map.of("id", 2, "username", "user2", "email", "user2@example.com", "status", "INACTIVE"),
                Map.of("id", 3, "username", "admin", "email", "admin@example.com", "status", "ACTIVE")
            ),
            "totalCount", 3,
            "requestedBy", username,
            "timestamp", LocalDateTime.now()
        ));
    }

    /**
     * 보안 로그 조회 (관리자 전용)
     */
    @GetMapping("/security-logs")
    public ResponseEntity<?> getSecurityLogs() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        log.info("Security logs requested by admin: {}", username);
        
        return ResponseEntity.ok(Map.of(
            "logs", List.of(
                Map.of("timestamp", LocalDateTime.now().minusMinutes(5), "event", "LOGIN_SUCCESS", "user", "user1", "ip", "192.168.1.100"),
                Map.of("timestamp", LocalDateTime.now().minusMinutes(10), "event", "LOGIN_FAILED", "user", "unknown", "ip", "192.168.1.200"),
                Map.of("timestamp", LocalDateTime.now().minusMinutes(15), "event", "TOKEN_EXPIRED", "user", "user2", "ip", "192.168.1.150")
            ),
            "totalCount", 3,
            "requestedBy", username,
            "timestamp", LocalDateTime.now()
        ));
    }

    /**
     * 시스템 설정 변경 (관리자 전용)
     */
    @PostMapping("/system-config")
    public ResponseEntity<?> updateSystemConfig(@RequestBody Map<String, Object> config) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        log.info("System config update requested by admin: {} with config: {}", username, config);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "System configuration updated successfully",
            "updatedConfig", config,
            "updatedBy", username,
            "timestamp", LocalDateTime.now()
        ));
    }

    /**
     * 데이터베이스 백업 (관리자 전용)
     */
    @PostMapping("/backup")
    public ResponseEntity<?> createBackup() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        log.info("Database backup requested by admin: {}", username);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Database backup initiated",
            "backupId", "backup_" + System.currentTimeMillis(),
            "estimatedTime", "5 minutes",
            "initiatedBy", username,
            "timestamp", LocalDateTime.now()
        ));
    }
}