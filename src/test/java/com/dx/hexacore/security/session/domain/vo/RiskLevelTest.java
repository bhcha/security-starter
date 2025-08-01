package com.dx.hexacore.security.session.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RiskLevel Value Object 테스트")
class RiskLevelTest {

    @Test
    @DisplayName("낮은 위험도 RiskLevel 생성")
    void createLowRiskLevel() {
        // Given
        String reason = "Normal login attempt";
        
        // When
        RiskLevel riskLevel = RiskLevel.low(reason);
        
        // Then
        assertThat(riskLevel.getScore()).isEqualTo(0);
        assertThat(riskLevel.getCategory()).isEqualTo(RiskCategory.LOW);
        assertThat(riskLevel.getReason()).isEqualTo(reason);
        assertThat(riskLevel.isLow()).isTrue();
        assertThat(riskLevel.isMedium()).isFalse();
        assertThat(riskLevel.isHigh()).isFalse();
        assertThat(riskLevel.isCritical()).isFalse();
    }

    @Test
    @DisplayName("중간 위험도 RiskLevel 생성")
    void createMediumRiskLevel() {
        // Given
        String reason = "Failed login attempt";
        
        // When
        RiskLevel riskLevel = RiskLevel.medium(reason);
        
        // Then
        assertThat(riskLevel.getScore()).isEqualTo(38);
        assertThat(riskLevel.getCategory()).isEqualTo(RiskCategory.MEDIUM);
        assertThat(riskLevel.getReason()).isEqualTo(reason);
        assertThat(riskLevel.isLow()).isFalse();
        assertThat(riskLevel.isMedium()).isTrue();
        assertThat(riskLevel.isHigh()).isFalse();
        assertThat(riskLevel.isCritical()).isFalse();
    }

    @Test
    @DisplayName("높은 위험도 RiskLevel 생성")
    void createHighRiskLevel() {
        // Given
        String reason = "Multiple failed attempts";
        
        // When
        RiskLevel riskLevel = RiskLevel.high(reason);
        
        // Then
        assertThat(riskLevel.getScore()).isEqualTo(63);
        assertThat(riskLevel.getCategory()).isEqualTo(RiskCategory.HIGH);
        assertThat(riskLevel.getReason()).isEqualTo(reason);
        assertThat(riskLevel.isLow()).isFalse();
        assertThat(riskLevel.isMedium()).isFalse();
        assertThat(riskLevel.isHigh()).isTrue();
        assertThat(riskLevel.isCritical()).isFalse();
    }

    @Test
    @DisplayName("치명적 위험도 RiskLevel 생성")
    void createCriticalRiskLevel() {
        // Given
        String reason = "Brute force attack detected";
        
        // When
        RiskLevel riskLevel = RiskLevel.critical(reason);
        
        // Then
        assertThat(riskLevel.getScore()).isEqualTo(88);
        assertThat(riskLevel.getCategory()).isEqualTo(RiskCategory.CRITICAL);
        assertThat(riskLevel.getReason()).isEqualTo(reason);
        assertThat(riskLevel.isLow()).isFalse();
        assertThat(riskLevel.isMedium()).isFalse();
        assertThat(riskLevel.isHigh()).isFalse();
        assertThat(riskLevel.isCritical()).isTrue();
    }

    @Test
    @DisplayName("커스텀 점수로 RiskLevel 생성")
    void createCustomRiskLevel() {
        // Given
        int customScore = 60;
        String reason = "Custom risk assessment";
        
        // When
        RiskLevel riskLevel = RiskLevel.of(customScore, reason);
        
        // Then
        assertThat(riskLevel.getScore()).isEqualTo(customScore);
        assertThat(riskLevel.getCategory()).isEqualTo(RiskCategory.HIGH);
        assertThat(riskLevel.getReason()).isEqualTo(reason);
    }

    @Test
    @DisplayName("점수 범위별 카테고리 자동 분류 테스트")
    void testCategoryDeterminationByScore() {
        // When & Then
        assertThat(RiskLevel.of(10, "Low").getCategory()).isEqualTo(RiskCategory.LOW);
        assertThat(RiskLevel.of(25, "Low").getCategory()).isEqualTo(RiskCategory.LOW);
        assertThat(RiskLevel.of(26, "Medium").getCategory()).isEqualTo(RiskCategory.MEDIUM);
        assertThat(RiskLevel.of(50, "Medium").getCategory()).isEqualTo(RiskCategory.MEDIUM);
        assertThat(RiskLevel.of(51, "High").getCategory()).isEqualTo(RiskCategory.HIGH);
        assertThat(RiskLevel.of(75, "High").getCategory()).isEqualTo(RiskCategory.HIGH);
        assertThat(RiskLevel.of(76, "Critical").getCategory()).isEqualTo(RiskCategory.CRITICAL);
        assertThat(RiskLevel.of(100, "Critical").getCategory()).isEqualTo(RiskCategory.CRITICAL);
    }

    @Test
    @DisplayName("null 이유로 생성 시 예외 발생")
    void createRiskLevelWithNullReason() {
        // When & Then
        assertThatThrownBy(() -> RiskLevel.low(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Risk reason cannot be null or empty");
    }

    @Test
    @DisplayName("빈 이유로 생성 시 예외 발생")
    void createRiskLevelWithEmptyReason() {
        // When & Then
        assertThatThrownBy(() -> RiskLevel.medium(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Risk reason cannot be null or empty");
    }

    @Test
    @DisplayName("공백만 있는 이유로 생성 시 예외 발생")
    void createRiskLevelWithBlankReason() {
        // When & Then
        assertThatThrownBy(() -> RiskLevel.high("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Risk reason cannot be null or empty");
    }

    @Test
    @DisplayName("음수 점수로 생성 시 예외 발생")
    void createRiskLevelWithNegativeScore() {
        // When & Then
        assertThatThrownBy(() -> RiskLevel.of(-1, "Invalid"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Risk score must be between 0 and 100");
    }

    @Test
    @DisplayName("100 초과 점수로 생성 시 예외 발생")
    void createRiskLevelWithOverMaxScore() {
        // When & Then
        assertThatThrownBy(() -> RiskLevel.of(101, "Invalid"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Risk score must be between 0 and 100");
    }

    @Test
    @DisplayName("경계값 점수 테스트")
    void testBoundaryScores() {
        // When & Then
        assertThatNoException().isThrownBy(() -> RiskLevel.of(0, "Minimum"));
        assertThatNoException().isThrownBy(() -> RiskLevel.of(100, "Maximum"));
    }

    @Test
    @DisplayName("이유 문자열 앞뒤 공백 제거 테스트")
    void testReasonTrimming() {
        // Given
        String reasonWithSpaces = "  Normal login  ";
        
        // When
        RiskLevel riskLevel = RiskLevel.low(reasonWithSpaces);
        
        // Then
        assertThat(riskLevel.getReason()).isEqualTo("Normal login");
    }

    @Test
    @DisplayName("RiskLevel 동등성 테스트")
    void riskLevelEquality() {
        // Given
        RiskLevel riskLevel1 = RiskLevel.low("Same reason");
        RiskLevel riskLevel2 = RiskLevel.low("Same reason");
        RiskLevel riskLevel3 = RiskLevel.medium("Different reason");
        
        // When & Then
        assertThat(riskLevel1).isEqualTo(riskLevel2);
        assertThat(riskLevel1).isNotEqualTo(riskLevel3);
        assertThat(riskLevel1.hashCode()).isEqualTo(riskLevel2.hashCode());
    }

    @Test
    @DisplayName("위험도 비교 테스트")
    void testRiskLevelComparison() {
        // Given
        RiskLevel low = RiskLevel.low("Low risk");
        RiskLevel medium = RiskLevel.medium("Medium risk");
        RiskLevel high = RiskLevel.high("High risk");
        RiskLevel critical = RiskLevel.critical("Critical risk");
        
        // When & Then
        assertThat(low.getScore()).isLessThan(medium.getScore());
        assertThat(medium.getScore()).isLessThan(high.getScore());
        assertThat(high.getScore()).isLessThan(critical.getScore());
    }
}