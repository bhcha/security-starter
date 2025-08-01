package com.example.securitytest.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 보호된 엔드포인트 컨트롤러
 * 
 * Spring Security와 Hexacore Security의 통합 동작을 테스트하기 위한 
 * 인증이 필요한 엔드포인트들을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/protected")
public class ProtectedController {

    /**
     * 인증된 사용자만 접근 가능한 엔드포인트
     */
    @GetMapping("/user-info")
    public ResponseEntity<?> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Unauthorized",
                "message", "User not authenticated"
            ));
        }
        
        String username = authentication.getName();
        var authorities = authentication.getAuthorities();
        
        log.info("User info requested by: {}", username);
        
        return ResponseEntity.ok(Map.of(
            "username", username,
            "authorities", authorities,
            "authenticated", true,
            "timestamp", LocalDateTime.now(),
            "message", "Successfully accessed protected resource"
        ));
    }

    /**
     * 관리자 권한이 필요한 엔드포인트
     */
    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAdminData() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        log.info("Admin endpoint accessed by: {}", username);
        
        return ResponseEntity.ok(Map.of(
            "message", "Admin access granted",
            "username", username,
            "data", "Sensitive admin data",
            "timestamp", LocalDateTime.now()
        ));
    }

    /**
     * 사용자 프로필 엔드포인트
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        return ResponseEntity.ok(Map.of(
            "username", username,
            "profile", Map.of(
                "email", username + "@example.com",
                "fullName", "Test User",
                "lastLogin", LocalDateTime.now(),
                "roles", authentication.getAuthorities()
            ),
            "timestamp", LocalDateTime.now()
        ));
    }

    /**
     * 토큰 정보 확인 엔드포인트
     */
    @GetMapping("/token-info")
    public ResponseEntity<?> getTokenInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        return ResponseEntity.ok(Map.of(
            "principal", authentication.getPrincipal(),
            "authorities", authentication.getAuthorities(),
            "details", authentication.getDetails(),
            "authenticated", authentication.isAuthenticated(),
            "timestamp", LocalDateTime.now()
        ));
    }

    /**
     * 데이터 수정 테스트 엔드포인트
     */
    @PostMapping("/update-data")
    public ResponseEntity<?> updateData(@RequestBody Map<String, Object> data) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        log.info("Data update requested by: {} with data: {}", username, data);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Data updated successfully",
            "updatedBy", username,
            "updatedData", data,
            "timestamp", LocalDateTime.now()
        ));
    }
}