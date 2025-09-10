package com.ldx.hexacore.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 보안 관련 상수 설정을 관리하는 Configuration Properties 클래스
 * 
 * <p>하드코딩된 magic number들을 외부 설정으로 분리하여 
 * 유연한 설정 관리를 가능하게 합니다.</p>
 * 
 * @author Claude Code Assistant
 */
@Data
@ConfigurationProperties(prefix = "hexacore.security")
public class SecurityConstants {

    private Session session = new Session();
    private Token token = new Token();
    private Validation validation = new Validation();
    private Logging logging = new Logging();

    /**
     * 세션 관련 상수 설정
     */
    @Data
    public static class Session {
        /**
         * 세션 시간 윈도우 (분)
         * AuthenticationSession에서 실패 시도를 추적하는 시간 범위
         */
        private int timeWindowMinutes = 15;

        /**
         * 세션 타임아웃 시간 (시간)
         * 세션이 유지되는 최대 시간
         */
        private int timeoutHours = 24;

        /**
         * 최대 실패 시도 횟수
         * 계정 잠금이 발생하는 실패 임계값
         */
        private int maxFailedAttempts = 5;

        /**
         * 계정 잠금 지속 시간 (분)
         * 계정이 잠긴 후 자동으로 해제되는 시간
         */
        private int lockoutDurationMinutes = 30;
    }

    /**
     * 토큰 관련 상수 설정
     */
    @Data
    public static class Token {
        /**
         * 토큰 최소 만료 시간 (초)
         */
        private long minExpiresIn = 1L;

        /**
         * 토큰 최대 만료 시간 (초)
         * 기본값: 86400초 (24시간)
         */
        private long maxExpiresIn = 86400L;
    }

    /**
     * 입력값 검증 관련 상수 설정
     */
    @Data
    public static class Validation {
        /**
         * 사용자명 최소 길이
         */
        private int minUsernameLength = 3;

        /**
         * 사용자명 최대 길이
         */
        private int maxUsernameLength = 50;

        /**
         * 비밀번호 최소 길이
         */
        private int minPasswordLength = 8;

        /**
         * JWT Secret 최소 길이 (바이트)
         * 보안상 256비트 (32바이트) 이상 필요
         */
        private int minJwtSecretLength = 32;

        /**
         * 프로덕션 환경 JWT Secret 권장 길이 (바이트)
         */
        private int recommendedJwtSecretLength = 64;
    }

    /**
     * 로깅 및 보안 감지 관련 상수 설정
     */
    @Data
    public static class Logging {
        /**
         * 의심스러운 활동 감지 임계값
         * SecurityEventLogger에서 사용되는 실패 횟수 임계값
         */
        private int suspiciousActivityThreshold = 5;

        /**
         * 의심스러운 활동 감지 시간 윈도우 (분)
         * SuspiciousActivityTracker에서 사용되는 시간 범위
         */
        private int suspiciousActivityTimeWindowMinutes = 5;

        /**
         * 로그 메시지 최대 길이
         * User-Agent 등 긴 문자열을 truncate하는 길이
         */
        private int maxLogMessageLength = 50;

        /**
         * 통계 상위 항목 표시 개수
         * 보안 통계에서 상위 사용자/엔드포인트 표시 개수
         */
        private int topStatsLimit = 5;
    }

    /**
     * 시간 윈도우를 Duration 객체로 반환
     * 
     * @return 세션 시간 윈도우 Duration
     */
    public Duration getSessionTimeWindow() {
        return Duration.ofMinutes(session.timeWindowMinutes);
    }

    /**
     * 의심스러운 활동 감지 시간 윈도우를 Duration 객체로 반환
     * 
     * @return 의심스러운 활동 감지 시간 윈도우 Duration
     */
    public Duration getSuspiciousActivityTimeWindow() {
        return Duration.ofMinutes(logging.suspiciousActivityTimeWindowMinutes);
    }
}