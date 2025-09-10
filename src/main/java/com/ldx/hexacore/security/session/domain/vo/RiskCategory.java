package com.ldx.hexacore.security.session.domain.vo;

/**
 * 위험도 범주를 나타내는 열거형
 */
public enum RiskCategory {
    LOW,      // 0-25점: 낮은 위험도
    MEDIUM,   // 26-50점: 중간 위험도  
    HIGH,     // 51-75점: 높은 위험도
    CRITICAL  // 76-100점: 심각한 위험도
}