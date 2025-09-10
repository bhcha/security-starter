package com.ldx.hexacore.security.logging;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * IP별 의심스러운 인증 실패 활동을 추적하는 클래스입니다.
 * 
 * <p>이 클래스는 특정 시간 윈도우 내에서 발생한 인증 실패를 추적하여
 * 의심스러운 활동을 감지하는 데 사용됩니다. Thread-safe하게 구현되어
 * 동시성 환경에서 안전하게 사용할 수 있습니다.</p>
 * 
 * <p><b>주요 기능:</b></p>
 * <ul>
 *   <li>시간 윈도우 기반 실패 횟수 추적</li>
 *   <li>자동 오래된 기록 정리</li>
 *   <li>Thread-safe 동시성 지원</li>
 * </ul>
 * 
 * @since 1.0.0
 */
public class SuspiciousActivityTracker {
    
    /**
     * 실패 시각들을 저장하는 deque.
     * ConcurrentLinkedDeque를 사용하여 thread-safe 보장
     */
    private final ConcurrentLinkedDeque<LocalDateTime> failureTimes;
    
    /**
     * 추적할 시간 윈도우 (기본: 5분)
     */
    private final Duration timeWindow;
    
    /**
     * 기본 시간 윈도우(5분)로 트래커를 생성합니다.
     */
    public SuspiciousActivityTracker() {
        this(Duration.ofMinutes(5));
    }
    
    /**
     * 지정된 시간 윈도우로 트래커를 생성합니다.
     * 
     * @param timeWindow 추적할 시간 윈도우 (null일 수 없음)
     * @throws IllegalArgumentException timeWindow가 null이거나 음수인 경우
     */
    public SuspiciousActivityTracker(Duration timeWindow) {
        if (timeWindow == null) {
            throw new IllegalArgumentException("Time window cannot be null");
        }
        if (timeWindow.isNegative() || timeWindow.isZero()) {
            throw new IllegalArgumentException("Time window must be positive");
        }
        
        this.timeWindow = timeWindow;
        this.failureTimes = new ConcurrentLinkedDeque<>();
    }
    
    /**
     * 인증 실패를 기록하고 현재 시간 윈도우 내 실패 횟수를 반환합니다.
     * 
     * <p>이 메서드는 자동으로 오래된 기록을 정리한 후 새로운 실패를 추가합니다.
     * 반환값은 정리 후의 총 실패 횟수입니다.</p>
     * 
     * @param failureTime 실패 발생 시각 (null일 수 없음)
     * @return 시간 윈도우 내 총 실패 횟수 (새로 추가된 실패 포함)
     * @throws IllegalArgumentException failureTime이 null인 경우
     */
    public int addFailure(LocalDateTime failureTime) {
        if (failureTime == null) {
            throw new IllegalArgumentException("Failure time cannot be null");
        }
        
        // 오래된 기록 먼저 정리
        cleanup(failureTime);
        
        // 새로운 실패 기록 추가 (시간순 정렬을 위해 끝에 추가)
        failureTimes.addLast(failureTime);
        
        return failureTimes.size();
    }
    
    /**
     * 현재 시간 기준으로 시간 윈도우 내 실패 횟수를 반환합니다.
     * 
     * @return 현재 시간 기준 시간 윈도우 내 실패 횟수
     */
    public int getFailureCount() {
        return getFailuresInWindow(LocalDateTime.now());
    }
    
    /**
     * 지정된 시간 기준으로 시간 윈도우 내 실패 횟수를 반환합니다.
     * 
     * <p>이 메서드는 자동으로 오래된 기록을 정리한 후 결과를 반환합니다.</p>
     * 
     * @param now 기준 시각 (null일 수 없음)
     * @return 지정된 시간 기준 시간 윈도우 내 실패 횟수
     * @throws IllegalArgumentException now가 null인 경우
     */
    public int getFailuresInWindow(LocalDateTime now) {
        if (now == null) {
            throw new IllegalArgumentException("Current time cannot be null");
        }
        
        cleanup(now);
        return failureTimes.size();
    }
    
    /**
     * 지정된 시간을 기준으로 시간 윈도우를 벗어난 오래된 실패 기록들을 제거합니다.
     * 
     * <p>이 메서드는 thread-safe하게 구현되어 동시 호출시에도 안전합니다.
     * 제거 작업은 deque의 앞쪽부터 수행되며, 시간순으로 정렬된 특성을 활용합니다.</p>
     * 
     * @param now 기준 시각 (null일 수 없음)
     * @throws IllegalArgumentException now가 null인 경우
     */
    public void cleanup(LocalDateTime now) {
        if (now == null) {
            throw new IllegalArgumentException("Current time cannot be null");
        }
        
        LocalDateTime cutoffTime = now.minus(timeWindow);
        
        // ConcurrentLinkedDeque는 iterator를 사용한 제거가 thread-safe하지 않으므로
        // pollFirst()를 사용하여 앞쪽부터 제거
        while (!failureTimes.isEmpty()) {
            LocalDateTime oldestFailure = failureTimes.peekFirst();
            if (oldestFailure == null || !oldestFailure.isAfter(cutoffTime)) {
                // 시간 윈도우를 벗어난 기록 제거
                failureTimes.pollFirst();
            } else {
                // 더 이상 제거할 기록이 없음 (시간순 정렬되어 있으므로)
                break;
            }
        }
    }
    
    /**
     * 현재 저장된 모든 실패 기록을 제거합니다.
     * 
     * <p>이 메서드는 주로 테스트나 시스템 초기화 시 사용됩니다.</p>
     */
    public void clear() {
        failureTimes.clear();
    }
    
    /**
     * 설정된 시간 윈도우를 반환합니다.
     * 
     * @return 시간 윈도우
     */
    public Duration getTimeWindow() {
        return timeWindow;
    }
    
    /**
     * 디버깅 목적으로 현재 저장된 실패 기록 수를 반환합니다.
     * 
     * <p>이 값은 cleanup 없이 raw 데이터의 크기를 반환하므로
     * 실제 유효한 실패 횟수와 다를 수 있습니다.</p>
     * 
     * @return 저장된 실패 기록의 총 개수 (cleanup 미수행)
     */
    public int getRawRecordCount() {
        return failureTimes.size();
    }
    
    @Override
    public String toString() {
        return String.format("SuspiciousActivityTracker{timeWindow=%s, recordCount=%d}", 
                           timeWindow, getRawRecordCount());
    }
}