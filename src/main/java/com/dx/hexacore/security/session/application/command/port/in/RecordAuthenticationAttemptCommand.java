package com.dx.hexacore.security.session.application.command.port.in;

import java.util.regex.Pattern;

/**
 * 인증 시도 기록 명령
 * 
 * 사용자의 인증 시도를 세션에 기록하기 위한 명령 객체입니다.
 * 성공/실패 여부와 위험도 정보를 포함합니다.
 */
public record RecordAuthenticationAttemptCommand(
    String sessionId,        // 세션 ID
    String userId,           // 사용자 ID
    String clientIp,         // 클라이언트 IP 주소
    boolean isSuccessful,    // 인증 성공 여부
    int riskScore,           // 위험도 점수 (0-100)
    String riskReason        // 위험도 이유
) {
    
    // IPv4 패턴
    private static final Pattern IPV4_PATTERN = Pattern.compile(
        "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    );
    
    // IPv6 패턴 (간단한 버전)
    private static final Pattern IPV6_PATTERN = Pattern.compile(
        "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|^::1$|^::$"
    );
    
    public RecordAuthenticationAttemptCommand {
        validateSessionId(sessionId);
        validateUserId(userId);
        validateClientIp(clientIp);
        validateRiskScore(riskScore);
        validateRiskReason(riskReason);
    }
    
    private void validateSessionId(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
    }
    
    private void validateUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
    }
    
    private void validateClientIp(String clientIp) {
        if (clientIp == null || clientIp.trim().isEmpty()) {
            throw new IllegalArgumentException("Client IP cannot be null or empty");
        }
        
        if (!isValidIpAddress(clientIp)) {
            throw new IllegalArgumentException("Invalid IP address format");
        }
    }
    
    private void validateRiskScore(int riskScore) {
        if (riskScore < 0 || riskScore > 100) {
            throw new IllegalArgumentException("Risk score must be between 0 and 100");
        }
    }
    
    private void validateRiskReason(String riskReason) {
        if (riskReason == null || riskReason.trim().isEmpty()) {
            throw new IllegalArgumentException("Risk reason cannot be null or empty");
        }
    }
    
    private boolean isValidIpAddress(String ip) {
        return IPV4_PATTERN.matcher(ip).matches() || 
               IPV6_PATTERN.matcher(ip).matches() ||
               isValidFullIPv6(ip);
    }
    
    private boolean isValidFullIPv6(String ip) {
        // 더 완전한 IPv6 검증을 위한 추가 로직
        try {
            // IPv6는 콜론으로 구분된 8개의 16비트 그룹
            if (ip.contains("::")) {
                // 축약형 IPv6 처리
                String[] parts = ip.split("::");
                if (parts.length > 2) return false;
                
                int totalGroups = 0;
                for (String part : parts) {
                    if (!part.isEmpty()) {
                        totalGroups += part.split(":").length;
                    }
                }
                return totalGroups <= 8;
            } else {
                // 전체형 IPv6
                String[] groups = ip.split(":");
                if (groups.length != 8) return false;
                
                for (String group : groups) {
                    if (group.length() == 0 || group.length() > 4) return false;
                    for (char c : group.toCharArray()) {
                        if (!Character.isDigit(c) && !(c >= 'a' && c <= 'f') && !(c >= 'A' && c <= 'F')) {
                            return false;
                        }
                    }
                }
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }
}