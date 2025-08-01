package com.dx.hexacore.security.session.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RiskCategory Enum 테스트")
class RiskCategoryTest {

    @Test
    @DisplayName("LOW 카테고리 값 확인")
    void testLowCategory() {
        // When
        RiskCategory category = RiskCategory.LOW;
        
        // Then
        assertThat(category.name()).isEqualTo("LOW");
        assertThat(category.toString()).isEqualTo("LOW");
    }

    @Test
    @DisplayName("MEDIUM 카테고리 값 확인")
    void testMediumCategory() {
        // When
        RiskCategory category = RiskCategory.MEDIUM;
        
        // Then
        assertThat(category.name()).isEqualTo("MEDIUM");
        assertThat(category.toString()).isEqualTo("MEDIUM");
    }

    @Test
    @DisplayName("HIGH 카테고리 값 확인")
    void testHighCategory() {
        // When
        RiskCategory category = RiskCategory.HIGH;
        
        // Then
        assertThat(category.name()).isEqualTo("HIGH");
        assertThat(category.toString()).isEqualTo("HIGH");
    }

    @Test
    @DisplayName("CRITICAL 카테고리 값 확인")
    void testCriticalCategory() {
        // When
        RiskCategory category = RiskCategory.CRITICAL;
        
        // Then
        assertThat(category.name()).isEqualTo("CRITICAL");
        assertThat(category.toString()).isEqualTo("CRITICAL");
    }

    @Test
    @DisplayName("RiskCategory 열거형 값 개수 확인")
    void testRiskCategoryValues() {
        // When
        RiskCategory[] values = RiskCategory.values();
        
        // Then
        assertThat(values).hasSize(4);
        assertThat(values).containsExactly(
            RiskCategory.LOW, 
            RiskCategory.MEDIUM, 
            RiskCategory.HIGH, 
            RiskCategory.CRITICAL
        );
    }

    @Test
    @DisplayName("RiskCategory valueOf 테스트")
    void testValueOf() {
        // When & Then
        assertThat(RiskCategory.valueOf("LOW")).isEqualTo(RiskCategory.LOW);
        assertThat(RiskCategory.valueOf("MEDIUM")).isEqualTo(RiskCategory.MEDIUM);
        assertThat(RiskCategory.valueOf("HIGH")).isEqualTo(RiskCategory.HIGH);
        assertThat(RiskCategory.valueOf("CRITICAL")).isEqualTo(RiskCategory.CRITICAL);
    }

    @Test
    @DisplayName("잘못된 값으로 valueOf 호출 시 예외 발생")
    void testValueOfWithInvalidValue() {
        // When & Then
        assertThatThrownBy(() -> RiskCategory.valueOf("INVALID"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("RiskCategory 비교 테스트")
    void testRiskCategoryComparison() {
        // Given
        RiskCategory low1 = RiskCategory.LOW;
        RiskCategory low2 = RiskCategory.LOW;
        RiskCategory medium = RiskCategory.MEDIUM;
        
        // When & Then
        assertThat(low1).isEqualTo(low2);
        assertThat(low1).isNotEqualTo(medium);
        assertThat(low1 == low2).isTrue();
        assertThat(low1 == medium).isFalse();
    }

    @Test
    @DisplayName("RiskCategory ordinal 테스트")
    void testRiskCategoryOrdinal() {
        // When & Then
        assertThat(RiskCategory.LOW.ordinal()).isEqualTo(0);
        assertThat(RiskCategory.MEDIUM.ordinal()).isEqualTo(1);
        assertThat(RiskCategory.HIGH.ordinal()).isEqualTo(2);
        assertThat(RiskCategory.CRITICAL.ordinal()).isEqualTo(3);
    }

    @Test
    @DisplayName("RiskCategory 순서 테스트 (심각도 순)")
    void testRiskCategoryOrder() {
        // Given
        RiskCategory[] categories = RiskCategory.values();
        
        // When & Then - ordinal 값이 심각도 순서를 나타냄
        assertThat(RiskCategory.LOW.ordinal()).isLessThan(RiskCategory.MEDIUM.ordinal());
        assertThat(RiskCategory.MEDIUM.ordinal()).isLessThan(RiskCategory.HIGH.ordinal());
        assertThat(RiskCategory.HIGH.ordinal()).isLessThan(RiskCategory.CRITICAL.ordinal());
    }

    @Test
    @DisplayName("RiskCategory switch 문 테스트")
    void testRiskCategorySwitch() {
        // Given & When & Then
        String lowDescription = switch (RiskCategory.LOW) {
            case LOW -> "낮은 위험도";
            case MEDIUM -> "보통 위험도";
            case HIGH -> "높은 위험도";
            case CRITICAL -> "치명적 위험도";
        };
        
        String criticalDescription = switch (RiskCategory.CRITICAL) {
            case LOW -> "낮은 위험도";
            case MEDIUM -> "보통 위험도";
            case HIGH -> "높은 위험도";
            case CRITICAL -> "치명적 위험도";
        };
        
        assertThat(lowDescription).isEqualTo("낮은 위험도");
        assertThat(criticalDescription).isEqualTo("치명적 위험도");
    }

    @Test
    @DisplayName("RiskCategory 비교 연산자 테스트")
    void testRiskCategoryCompareTo() {
        // When & Then
        assertThat(RiskCategory.LOW.compareTo(RiskCategory.MEDIUM)).isLessThan(0);
        assertThat(RiskCategory.MEDIUM.compareTo(RiskCategory.LOW)).isGreaterThan(0);
        assertThat(RiskCategory.HIGH.compareTo(RiskCategory.HIGH)).isEqualTo(0);
        assertThat(RiskCategory.CRITICAL.compareTo(RiskCategory.LOW)).isGreaterThan(0);
    }
}