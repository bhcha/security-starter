package com.ldx.hexacore.security.session.domain.vo;

import com.ldx.hexacore.security.util.ValidationUtils;
import java.util.Objects;

/**
 * 위험도 수준을 나타내는 Value Object
 */
public class RiskLevel {
    
    private final int score;
    private final RiskCategory category;
    private final String reason;
    
    private RiskLevel(int score, String reason) {
        ValidationUtils.requireInRange(score, 0, 100, "Risk score");
        ValidationUtils.requireNonNullOrEmpty(reason, "Risk reason");
        
        this.score = score;
        this.category = determineCategory(score);
        this.reason = reason.trim();
    }
    
    /**
     * 점수와 사유로 위험도를 생성합니다.
     */
    public static RiskLevel of(int score, String reason) {
        return new RiskLevel(score, reason);
    }
    
    /**
     * 낮은 위험도를 생성합니다.
     */
    public static RiskLevel low(String reason) {
        ValidationUtils.requireNonNullOrEmpty(reason, "Risk reason");
        return new RiskLevel(0, reason);
    }
    
    /**
     * 중간 위험도를 생성합니다.
     */
    public static RiskLevel medium(String reason) {
        ValidationUtils.requireNonNullOrEmpty(reason, "Risk reason");
        return new RiskLevel(38, reason);
    }
    
    /**
     * 높은 위험도를 생성합니다.
     */
    public static RiskLevel high(String reason) {
        ValidationUtils.requireNonNullOrEmpty(reason, "Risk reason");
        return new RiskLevel(63, reason);
    }
    
    /**
     * 심각한 위험도를 생성합니다.
     */
    public static RiskLevel critical(String reason) {
        ValidationUtils.requireNonNullOrEmpty(reason, "Risk reason");
        return new RiskLevel(88, reason);
    }
    
    
    /**
     * 점수에 따른 위험도 범주 결정
     */
    private static RiskCategory determineCategory(int score) {
        if (score <= 25) {
            return RiskCategory.LOW;
        } else if (score <= 50) {
            return RiskCategory.MEDIUM;
        } else if (score <= 75) {
            return RiskCategory.HIGH;
        } else {
            return RiskCategory.CRITICAL;
        }
    }
    
    /**
     * 위험도 점수를 반환합니다.
     */
    public int getScore() {
        return score;
    }
    
    /**
     * 위험도 범주를 반환합니다.
     */
    public RiskCategory getCategory() {
        return category;
    }
    
    /**
     * 판단 사유를 반환합니다.
     */
    public String getReason() {
        return reason;
    }
    
    /**
     * 낮은 위험도인지 확인합니다.
     */
    public boolean isLow() {
        return category == RiskCategory.LOW;
    }
    
    /**
     * 중간 위험도인지 확인합니다.
     */
    public boolean isMedium() {
        return category == RiskCategory.MEDIUM;
    }
    
    /**
     * 높은 위험도인지 확인합니다.
     */
    public boolean isHigh() {
        return category == RiskCategory.HIGH;
    }
    
    /**
     * 심각한 위험도인지 확인합니다.
     */
    public boolean isCritical() {
        return category == RiskCategory.CRITICAL;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RiskLevel riskLevel = (RiskLevel) obj;
        return score == riskLevel.score &&
               category == riskLevel.category &&
               Objects.equals(reason, riskLevel.reason);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(score, category, reason);
    }
    
    @Override
    public String toString() {
        return String.format("RiskLevel{score=%d, category=%s, reason='%s'}", 
                           score, category, reason);
    }
}