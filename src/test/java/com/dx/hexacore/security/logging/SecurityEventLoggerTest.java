package com.dx.hexacore.security.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

/**
 * SecurityEventLogger 클래스의 단위 테스트
 */
@DisplayName("SecurityEventLogger 테스트")
class SecurityEventLoggerTest {

    private SecurityEventLogger eventLogger;
    private ListAppender<ILoggingEvent> eventLogAppender;
    private ListAppender<ILoggingEvent> auditLogAppender;

    @BeforeEach
    void setUp() {
        // SecurityConstants 생성 (기본값 사용)
        com.dx.hexacore.security.config.SecurityConstants securityConstants = 
            new com.dx.hexacore.security.config.SecurityConstants();
        eventLogger = new SecurityEventLogger(securityConstants);
        
        // Event Logger 캡처 설정
        Logger eventLoggerInstance = (Logger) LoggerFactory.getLogger("SECURITY.EVENT");
        eventLogAppender = new ListAppender<>();
        eventLogAppender.start();
        eventLoggerInstance.addAppender(eventLogAppender);
        
        // Audit Logger 캡처 설정
        Logger auditLoggerInstance = (Logger) LoggerFactory.getLogger("SECURITY.AUDIT");
        auditLogAppender = new ListAppender<>();
        auditLogAppender.start();
        auditLoggerInstance.addAppender(auditLogAppender);
    }

    @Nested
    @DisplayName("인증 성공 로깅 테스트")
    class AuthenticationSuccessTest {

        @Test
        @DisplayName("인증 성공 이벤트 정상 로깅")
        void logAuthenticationSuccess_ShouldLogCorrectly() {
            // given
            String username = "testuser";
            String clientIp = "192.168.1.100";
            String userAgent = "Mozilla/5.0 Test Browser";

            // when
            eventLogger.logAuthenticationSuccess(username, clientIp, userAgent);

            // then
            List<ILoggingEvent> auditLogs = auditLogAppender.list;
            assertThat(auditLogs).hasSize(1);
            assertThat(auditLogs.get(0).getFormattedMessage())
                .contains("AUTH_SUCCESS")
                .contains(username)
                .contains(clientIp);
        }

        @Test
        @DisplayName("긴 User Agent 문자열 truncate 확인")
        void logAuthenticationSuccess_LongUserAgent_ShouldTruncate() {
            // given
            String longUserAgent = "A".repeat(100); // 100자 문자열
            String expectedTruncated = "A".repeat(50) + "...";

            // when
            eventLogger.logAuthenticationSuccess("testuser", "127.0.0.1", longUserAgent);

            // then
            List<ILoggingEvent> auditLogs = auditLogAppender.list;
            assertThat(auditLogs.get(0).getFormattedMessage())
                .contains(expectedTruncated)
                .doesNotContain(longUserAgent);
        }
    }

    @Nested
    @DisplayName("인증 실패 로깅 테스트")
    class AuthenticationFailureTest {

        @Test
        @DisplayName("인증 실패 이벤트 정상 로깅")
        void logAuthenticationFailure_ShouldLogCorrectly() {
            // given
            String reason = "Invalid credentials";
            String clientIp = "192.168.1.100";
            String userAgent = "Test Browser";

            // when
            eventLogger.logAuthenticationFailure(reason, clientIp, userAgent);

            // then
            List<ILoggingEvent> auditLogs = auditLogAppender.list;
            assertThat(auditLogs).hasSizeGreaterThanOrEqualTo(1);
            
            // AUTH_FAILURE 로그 확인
            boolean hasAuthFailureLog = auditLogs.stream()
                .anyMatch(log -> log.getFormattedMessage().contains("AUTH_FAILURE") &&
                               log.getFormattedMessage().contains(reason) &&
                               log.getFormattedMessage().contains(clientIp));
            assertThat(hasAuthFailureLog).isTrue();
        }

        @Test
        @DisplayName("단일 실패 - 의심스러운 활동 미감지")
        void logAuthenticationFailure_SingleFailure_ShouldNotTriggerSuspiciousActivity() {
            // given
            String clientIp = "192.168.1.100";

            // when
            eventLogger.logAuthenticationFailure("Invalid password", clientIp, "Test Browser");

            // then
            List<ILoggingEvent> eventLogs = eventLogAppender.list;
            boolean hasSuspiciousActivityLog = eventLogs.stream()
                .anyMatch(log -> log.getFormattedMessage().contains("SUSPICIOUS ACTIVITY"));
            
            assertThat(hasSuspiciousActivityLog).isFalse();
        }
    }

    @Nested
    @DisplayName("의심스러운 활동 감지 테스트")
    class SuspiciousActivityDetectionTest {

        @Test
        @DisplayName("5회 연속 실패 시 의심스러운 활동 감지")
        void logAuthenticationFailure_5ConsecutiveFailures_ShouldTriggerSuspiciousActivity() {
            // given
            String clientIp = "192.168.1.100";
            String reason = "Invalid password";

            // when - 5번의 연속 실패
            for (int i = 0; i < 5; i++) {
                eventLogger.logAuthenticationFailure(reason, clientIp, "Test Browser");
            }

            // then
            List<ILoggingEvent> eventLogs = eventLogAppender.list;
            List<ILoggingEvent> auditLogs = auditLogAppender.list;

            // Event 로그에 의심스러운 활동 경고 확인
            boolean hasEventSuspiciousLog = eventLogs.stream()
                .anyMatch(log -> log.getFormattedMessage().contains("🚨 SUSPICIOUS ACTIVITY DETECTED!"));
            assertThat(hasEventSuspiciousLog).isTrue();

            // Audit 로그에 의심스러운 활동 기록 확인
            boolean hasAuditSuspiciousLog = auditLogs.stream()
                .anyMatch(log -> log.getFormattedMessage().contains("SUSPICIOUS_ACTIVITY") &&
                               log.getFormattedMessage().contains(clientIp) &&
                               log.getFormattedMessage().contains("5"));
            assertThat(hasAuditSuspiciousLog).isTrue();
        }

        @Test
        @DisplayName("서로 다른 IP에서의 실패는 각각 추적")
        void logAuthenticationFailure_DifferentIPs_ShouldTrackSeparately() {
            // given
            String ip1 = "192.168.1.100";
            String ip2 = "192.168.1.200";

            // when - 각 IP에서 3회씩 실패 (총 6회, 하지만 IP별로는 3회)
            for (int i = 0; i < 3; i++) {
                eventLogger.logAuthenticationFailure("Invalid password", ip1, "Browser1");
                eventLogger.logAuthenticationFailure("Invalid password", ip2, "Browser2");
            }

            // then - 어느 IP도 임계값(5회)에 도달하지 않아야 함
            List<ILoggingEvent> eventLogs = eventLogAppender.list;
            boolean hasSuspiciousActivityLog = eventLogs.stream()
                .anyMatch(log -> log.getFormattedMessage().contains("SUSPICIOUS ACTIVITY DETECTED"));
            
            assertThat(hasSuspiciousActivityLog).isFalse();
        }

        @Test
        @DisplayName("임계값 초과 후 추가 실패에도 지속적인 감지")
        void logAuthenticationFailure_AfterThreshold_ShouldContinueDetection() {
            // given
            String clientIp = "192.168.1.100";

            // when - 임계값을 초과하는 7회 실패
            for (int i = 0; i < 7; i++) {
                eventLogger.logAuthenticationFailure("Invalid password", clientIp, "Browser");
            }

            // then - 5회차와 7회차 모두에서 의심스러운 활동 감지되어야 함
            List<ILoggingEvent> eventLogs = eventLogAppender.list;
            long suspiciousActivityCount = eventLogs.stream()
                .filter(log -> log.getFormattedMessage().contains("🚨 SUSPICIOUS ACTIVITY DETECTED!"))
                .count();
            
            // 5회차, 6회차, 7회차에서 각각 감지되어야 함
            assertThat(suspiciousActivityCount).isEqualTo(3);
        }

        @Test
        @DisplayName("null 또는 빈 IP에 대한 안전한 처리")
        void logAuthenticationFailure_NullOrEmptyIP_ShouldHandleSafely() {
            // when & then - 예외 발생하지 않아야 함
            assertThatNoException().isThrownBy(() -> {
                eventLogger.logAuthenticationFailure("test", null, "browser");
                eventLogger.logAuthenticationFailure("test", "", "browser");
                eventLogger.logAuthenticationFailure("test", "   ", "browser");
            });
        }
    }

    @Nested
    @DisplayName("리소스 접근 거부 로깅 테스트")
    class ResourceAccessDeniedTest {

        @Test
        @DisplayName("리소스 접근 거부 이벤트 정상 로깅")
        void logResourceAccessDenied_ShouldLogCorrectly() {
            // given
            String username = "testuser";
            String resource = "/admin/users";
            String reason = "Insufficient privileges";

            // when
            eventLogger.logResourceAccessDenied(username, resource, reason);

            // then
            List<ILoggingEvent> auditLogs = auditLogAppender.list;
            assertThat(auditLogs).hasSize(1);
            assertThat(auditLogs.get(0).getFormattedMessage())
                .contains("RESOURCE_DENIED")
                .contains(username)
                .contains(resource)
                .contains(reason);
        }
    }

    @Nested
    @DisplayName("보안 통계 및 리포트 테스트")
    class SecurityStatsTest {

        @Test
        @DisplayName("보안 통계 생성 및 출력")
        void generateSecurityReport_ShouldShowCorrectStats() {
            // given - 다양한 이벤트 발생
            eventLogger.logAuthenticationSuccess("user1", "192.168.1.1", "browser1");
            eventLogger.logAuthenticationSuccess("user2", "192.168.1.2", "browser2");
            eventLogger.logAuthenticationFailure("invalid", "192.168.1.3", "browser3");
            eventLogger.logResourceAccessDenied("user1", "/admin", "no permission");

            // when
            eventLogger.logSecurityStatistics();

            // then
            List<ILoggingEvent> eventLogs = eventLogAppender.list;
            boolean hasReportLog = eventLogs.stream()
                .anyMatch(log -> log.getFormattedMessage().contains("🔐 SECURITY EVENT STATISTICS"));
            
            assertThat(hasReportLog).isTrue();
        }

        @Test
        @DisplayName("보안 통계 리셋 기능")
        void resetSecurityStats_ShouldClearAllCounters() {
            // given
            eventLogger.logAuthenticationSuccess("user1", "192.168.1.1", "browser1");
            eventLogger.logAuthenticationFailure("invalid", "192.168.1.2", "browser2");

            // when
            eventLogger.resetStatistics();

            // then
            List<ILoggingEvent> eventLogs = eventLogAppender.list;
            boolean hasResetLog = eventLogs.stream()
                .anyMatch(log -> log.getFormattedMessage().contains("📊 Security statistics have been reset"));
            
            assertThat(hasResetLog).isTrue();
        }
    }

    @Nested
    @DisplayName("동시성 테스트")
    class ConcurrencyTest {

        @Test
        @DisplayName("다중 스레드에서 동시 인증 실패 처리")
        void logAuthenticationFailure_MultipleThreads_ShouldBeThreadSafe() throws InterruptedException {
            // given
            String clientIp = "192.168.1.100";
            int threadCount = 10;
            int failuresPerThread = 2; // 총 20회 실패로 의심스러운 활동 충분히 감지
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            // when
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    for (int j = 0; j < failuresPerThread; j++) {
                        eventLogger.logAuthenticationFailure("Invalid password", clientIp, "Browser");
                        try {
                            Thread.sleep(10); // 약간의 지연으로 현실적인 시나리오 시뮬레이션
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                });
            }

            executor.shutdown();
            boolean completed = executor.awaitTermination(10, TimeUnit.SECONDS);

            // then
            assertThat(completed).isTrue();
            
            // 의심스러운 활동이 감지되었는지 확인 (20회 > 5회 임계값)
            List<ILoggingEvent> eventLogs = eventLogAppender.list;
            boolean hasSuspiciousActivity = eventLogs.stream()
                .anyMatch(log -> log.getFormattedMessage().contains("SUSPICIOUS ACTIVITY DETECTED"));
            
            assertThat(hasSuspiciousActivity).isTrue();
        }

        @Test
        @DisplayName("여러 IP에서 동시 접근 처리")
        void logAuthenticationFailure_MultipleIPs_ShouldHandleConcurrency() throws InterruptedException {
            // given
            int ipCount = 5;
            int failuresPerIp = 6; // 각 IP당 6회씩 실패로 의심스러운 활동 감지
            ExecutorService executor = Executors.newFixedThreadPool(ipCount);

            // when
            IntStream.range(0, ipCount).forEach(i -> {
                executor.submit(() -> {
                    String ip = "192.168.1." + (100 + i);
                    for (int j = 0; j < failuresPerIp; j++) {
                        eventLogger.logAuthenticationFailure("Invalid password", ip, "Browser");
                    }
                });
            });

            executor.shutdown();
            boolean completed = executor.awaitTermination(10, TimeUnit.SECONDS);

            // then
            assertThat(completed).isTrue();
            
            // 각 IP마다 의심스러운 활동이 감지되었는지 확인
            List<ILoggingEvent> auditLogs = auditLogAppender.list;
            long suspiciousActivityCount = auditLogs.stream()
                .filter(log -> log.getFormattedMessage().contains("SUSPICIOUS_ACTIVITY"))
                .count();
            
            // 각 IP에서 6회 실패하므로 5개 IP 모두에서 의심스러운 활동 감지되어야 함
            assertThat(suspiciousActivityCount).isGreaterThanOrEqualTo(ipCount);
        }
    }

    @Nested
    @DisplayName("유틸리티 메서드 테스트")
    class UtilityMethodTest {

        @Test
        @DisplayName("null 문자열 truncate 처리")
        void truncate_NullString_ShouldReturnNA() {
            // given & when
            eventLogger.logAuthenticationSuccess("user", "127.0.0.1", null);

            // then
            List<ILoggingEvent> auditLogs = auditLogAppender.list;
            assertThat(auditLogs.get(0).getFormattedMessage()).contains("N/A");
        }

        @Test
        @DisplayName("짧은 문자열 truncate 처리")
        void truncate_ShortString_ShouldReturnOriginal() {
            // given
            String shortString = "Short";

            // when
            eventLogger.logAuthenticationSuccess("user", "127.0.0.1", shortString);

            // then
            List<ILoggingEvent> auditLogs = auditLogAppender.list;
            assertThat(auditLogs.get(0).getFormattedMessage())
                .contains(shortString)
                .doesNotContain("...");
        }
    }
}