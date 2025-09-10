package com.ldx.hexacore.security.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

/**
 * SuspiciousActivityTracker 클래스의 단위 테스트
 */
@DisplayName("SuspiciousActivityTracker 테스트")
class SuspiciousActivityTrackerTest {

    private SuspiciousActivityTracker tracker;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        tracker = new SuspiciousActivityTracker();
        baseTime = LocalDateTime.of(2025, 9, 9, 12, 0, 0);
    }

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTest {

        @Test
        @DisplayName("기본 생성자는 5분 시간 윈도우로 생성")
        void defaultConstructor_ShouldCreateWith5MinuteWindow() {
            // given & when
            SuspiciousActivityTracker defaultTracker = new SuspiciousActivityTracker();
            
            // then
            assertThat(defaultTracker.getTimeWindow()).isEqualTo(Duration.ofMinutes(5));
            assertThat(defaultTracker.getRawRecordCount()).isZero();
        }

        @Test
        @DisplayName("커스텀 시간 윈도우로 생성")
        void customConstructor_ShouldCreateWithSpecifiedWindow() {
            // given
            Duration customWindow = Duration.ofMinutes(10);
            
            // when
            SuspiciousActivityTracker customTracker = new SuspiciousActivityTracker(customWindow);
            
            // then
            assertThat(customTracker.getTimeWindow()).isEqualTo(customWindow);
        }

        @Test
        @DisplayName("null 시간 윈도우로 생성시 예외 발생")
        void constructor_WithNullTimeWindow_ShouldThrowException() {
            // when & then
            assertThatThrownBy(() -> new SuspiciousActivityTracker(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Time window cannot be null");
        }

        @Test
        @DisplayName("음수 시간 윈도우로 생성시 예외 발생")
        void constructor_WithNegativeTimeWindow_ShouldThrowException() {
            // given
            Duration negativeWindow = Duration.ofMinutes(-1);
            
            // when & then
            assertThatThrownBy(() -> new SuspiciousActivityTracker(negativeWindow))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Time window must be positive");
        }

        @Test
        @DisplayName("0 시간 윈도우로 생성시 예외 발생")
        void constructor_WithZeroTimeWindow_ShouldThrowException() {
            // given
            Duration zeroWindow = Duration.ZERO;
            
            // when & then
            assertThatThrownBy(() -> new SuspiciousActivityTracker(zeroWindow))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Time window must be positive");
        }
    }

    @Nested
    @DisplayName("실패 추가 테스트")
    class AddFailureTest {

        @Test
        @DisplayName("첫 번째 실패 추가시 카운트 1 반환")
        void addFailure_FirstFailure_ShouldReturn1() {
            // when
            int count = tracker.addFailure(baseTime);
            
            // then
            assertThat(count).isEqualTo(1);
            assertThat(tracker.getFailuresInWindow(baseTime)).isEqualTo(1);
        }

        @Test
        @DisplayName("연속 실패 추가시 카운트 증가")
        void addFailure_MultipleFailures_ShouldIncreaseCount() {
            // when
            tracker.addFailure(baseTime);
            tracker.addFailure(baseTime.plusSeconds(30));
            int count = tracker.addFailure(baseTime.plusMinutes(1));
            
            // then
            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("null 시각으로 실패 추가시 예외 발생")
        void addFailure_WithNullTime_ShouldThrowException() {
            // when & then
            assertThatThrownBy(() -> tracker.addFailure(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Failure time cannot be null");
        }

        @Test
        @DisplayName("시간 윈도우를 벗어난 실패는 자동 제거됨")
        void addFailure_OldFailures_ShouldBeAutomaticallyRemoved() {
            // given
            tracker.addFailure(baseTime);
            tracker.addFailure(baseTime.plusMinutes(2));
            
            // when - 5분 후 새로운 실패 추가 (첫 번째 실패는 윈도우를 벗어남)
            int count = tracker.addFailure(baseTime.plusMinutes(6));
            
            // then
            assertThat(count).isEqualTo(2); // 두 번째와 세 번째 실패만 카운트
        }
    }

    @Nested
    @DisplayName("실패 횟수 조회 테스트")
    class GetFailureCountTest {

        @Test
        @DisplayName("실패 기록 없을 때 0 반환")
        void getFailureCount_NoFailures_ShouldReturn0() {
            // when & then
            assertThat(tracker.getFailureCount()).isZero();
            assertThat(tracker.getFailuresInWindow(baseTime)).isZero();
        }

        @Test
        @DisplayName("시간 윈도우 내 실패만 카운트")
        void getFailuresInWindow_ShouldCountOnlyWithinWindow() {
            // given
            tracker.addFailure(baseTime.minusMinutes(6)); // 윈도우 밖
            tracker.addFailure(baseTime.minusMinutes(4)); // 윈도우 안
            tracker.addFailure(baseTime.minusMinutes(2)); // 윈도우 안
            tracker.addFailure(baseTime);                 // 윈도우 안
            
            // when
            int count = tracker.getFailuresInWindow(baseTime);
            
            // then
            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("정확히 시간 윈도우 경계의 실패는 제외")
        void getFailuresInWindow_ExactBoundary_ShouldExclude() {
            // given
            LocalDateTime exactBoundary = baseTime.minusMinutes(5); // 정확히 5분 전
            tracker.addFailure(exactBoundary);
            tracker.addFailure(exactBoundary.plusSeconds(1)); // 경계보다 1초 후
            
            // when
            int count = tracker.getFailuresInWindow(baseTime);
            
            // then
            assertThat(count).isEqualTo(1); // 경계선 기록은 제외됨
        }

        @Test
        @DisplayName("null 기준 시각으로 조회시 예외 발생")
        void getFailuresInWindow_WithNullTime_ShouldThrowException() {
            // when & then
            assertThatThrownBy(() -> tracker.getFailuresInWindow(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Current time cannot be null");
        }
    }

    @Nested
    @DisplayName("정리(Cleanup) 테스트")
    class CleanupTest {

        @Test
        @DisplayName("시간 윈도우를 벗어난 기록들 제거")
        void cleanup_ShouldRemoveOldRecords() {
            // given - 연대순으로 추가하여 자동 정리가 발생하지 않도록 함
            tracker.addFailure(baseTime.minusMinutes(6));
            tracker.addFailure(baseTime.minusMinutes(5));
            tracker.addFailure(baseTime.minusMinutes(3));
            // baseTime.minusMinutes(6) 기록은 마지막 addFailure 시점에서 자동 제거되지 않음
            assertThat(tracker.getRawRecordCount()).isEqualTo(3);
            
            // when - baseTime 기준으로 명시적 정리 (5분 전 이전 기록들 제거)
            tracker.cleanup(baseTime);
            
            // then - 5분 전과 그 이전 기록들은 제거되고, 3분 전 기록만 남음
            assertThat(tracker.getRawRecordCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("모든 기록이 오래된 경우 전부 제거")
        void cleanup_AllRecordsOld_ShouldRemoveAll() {
            // given
            tracker.addFailure(baseTime.minusMinutes(6));
            tracker.addFailure(baseTime.minusMinutes(7));
            tracker.addFailure(baseTime.minusMinutes(8));
            
            // when
            tracker.cleanup(baseTime);
            
            // then
            assertThat(tracker.getRawRecordCount()).isZero();
        }

        @Test
        @DisplayName("null 기준 시각으로 정리시 예외 발생")
        void cleanup_WithNullTime_ShouldThrowException() {
            // when & then
            assertThatThrownBy(() -> tracker.cleanup(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Current time cannot be null");
        }
    }

    @Nested
    @DisplayName("기타 메서드 테스트")
    class OtherMethodsTest {

        @Test
        @DisplayName("clear 메서드로 모든 기록 제거")
        void clear_ShouldRemoveAllRecords() {
            // given
            tracker.addFailure(baseTime);
            tracker.addFailure(baseTime.plusMinutes(1));
            assertThat(tracker.getRawRecordCount()).isEqualTo(2);
            
            // when
            tracker.clear();
            
            // then
            assertThat(tracker.getRawRecordCount()).isZero();
            assertThat(tracker.getFailureCount()).isZero();
        }

        @Test
        @DisplayName("toString 메서드 정상 동작")
        void toString_ShouldReturnCorrectFormat() {
            // given
            tracker.addFailure(baseTime);
            
            // when
            String result = tracker.toString();
            
            // then
            assertThat(result).contains("SuspiciousActivityTracker")
                             .contains("timeWindow=PT5M")
                             .contains("recordCount=1");
        }
    }

    @Nested
    @DisplayName("동시성 테스트")
    class ConcurrencyTest {

        @Test
        @DisplayName("다중 스레드에서 동시 실패 추가")
        void addFailure_MultipleThreads_ShouldBeThreadSafe() throws InterruptedException {
            // given
            int threadCount = 10;
            int failuresPerThread = 5;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch finishLatch = new CountDownLatch(threadCount);
            
            // when
            IntStream.range(0, threadCount).forEach(i -> {
                executor.submit(() -> {
                    try {
                        startLatch.await(); // 모든 스레드가 동시에 시작하도록 대기
                        
                        for (int j = 0; j < failuresPerThread; j++) {
                            tracker.addFailure(baseTime.plusSeconds(j));
                        }
                        
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        finishLatch.countDown();
                    }
                });
            });
            
            startLatch.countDown(); // 모든 스레드 시작
            finishLatch.await(5, TimeUnit.SECONDS); // 완료까지 최대 5초 대기
            
            // then
            assertThat(tracker.getRawRecordCount()).isEqualTo(threadCount * failuresPerThread);
            
            executor.shutdown();
        }

        @Test
        @DisplayName("실패 추가와 조회 동시 실행")
        void addFailureAndGetCount_Concurrent_ShouldBeThreadSafe() throws InterruptedException {
            // given
            ExecutorService executor = Executors.newFixedThreadPool(4);
            CountDownLatch latch = new CountDownLatch(100);
            
            // when - 50번의 추가와 50번의 조회를 동시에 실행
            for (int i = 0; i < 50; i++) {
                final int index = i;
                
                // 실패 추가 스레드
                executor.submit(() -> {
                    try {
                        tracker.addFailure(baseTime.plusSeconds(index));
                    } finally {
                        latch.countDown();
                    }
                });
                
                // 조회 스레드  
                executor.submit(() -> {
                    try {
                        tracker.getFailuresInWindow(baseTime.plusSeconds(index));
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            // then
            boolean completed = latch.await(10, TimeUnit.SECONDS);
            assertThat(completed).isTrue();
            assertThat(tracker.getRawRecordCount()).isLessThanOrEqualTo(50);
            
            executor.shutdown();
        }
    }

    @Nested
    @DisplayName("실제 시나리오 테스트")
    class ScenarioTest {

        @Test
        @DisplayName("의심스러운 활동 시나리오: 5분 내 5회 실패")
        void suspiciousActivity_5FailuresWithin5Minutes() {
            // given
            SuspiciousActivityTracker customTracker = new SuspiciousActivityTracker(Duration.ofMinutes(5));
            
            // when - 5분 내에 5번의 실패
            customTracker.addFailure(baseTime);
            customTracker.addFailure(baseTime.plusSeconds(30));
            customTracker.addFailure(baseTime.plusMinutes(1));
            customTracker.addFailure(baseTime.plusMinutes(2));
            int finalCount = customTracker.addFailure(baseTime.plusMinutes(3));
            
            // then
            assertThat(finalCount).isEqualTo(5);
            assertThat(customTracker.getFailuresInWindow(baseTime.plusMinutes(3))).isEqualTo(5);
        }

        @Test
        @DisplayName("정상 활동 시나리오: 시간 간격이 긴 실패들")
        void normalActivity_FailuresWithLongInterval() {
            // given & when - 각각 6분 간격으로 실패 (시간 윈도우: 5분)
            tracker.addFailure(baseTime);
            tracker.addFailure(baseTime.plusMinutes(6));
            int count = tracker.addFailure(baseTime.plusMinutes(12));
            
            // then
            assertThat(count).isEqualTo(1); // 마지막 실패만 윈도우 내에 있음
        }
    }
}