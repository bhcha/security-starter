package com.ldx.hexacore.security.session.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("IpType Enum 테스트")
class IpTypeTest {

    @Test
    @DisplayName("IPv4 타입 값 확인")
    void testIPv4Type() {
        // When
        IpType ipType = IpType.IPv4;
        
        // Then
        assertThat(ipType.name()).isEqualTo("IPv4");
        assertThat(ipType.toString()).isEqualTo("IPv4");
    }

    @Test
    @DisplayName("IPv6 타입 값 확인")
    void testIPv6Type() {
        // When
        IpType ipType = IpType.IPv6;
        
        // Then
        assertThat(ipType.name()).isEqualTo("IPv6");
        assertThat(ipType.toString()).isEqualTo("IPv6");
    }

    @Test
    @DisplayName("IpType 열거형 값 개수 확인")
    void testIpTypeValues() {
        // When
        IpType[] values = IpType.values();
        
        // Then
        assertThat(values).hasSize(2);
        assertThat(values).containsExactly(IpType.IPv4, IpType.IPv6);
    }

    @Test
    @DisplayName("IpType valueOf 테스트")
    void testValueOf() {
        // When & Then
        assertThat(IpType.valueOf("IPv4")).isEqualTo(IpType.IPv4);
        assertThat(IpType.valueOf("IPv6")).isEqualTo(IpType.IPv6);
    }

    @Test
    @DisplayName("잘못된 값으로 valueOf 호출 시 예외 발생")
    void testValueOfWithInvalidValue() {
        // When & Then
        assertThatThrownBy(() -> IpType.valueOf("IPv5"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("IpType 비교 테스트")
    void testIpTypeComparison() {
        // Given
        IpType ipv4_1 = IpType.IPv4;
        IpType ipv4_2 = IpType.IPv4;
        IpType ipv6 = IpType.IPv6;
        
        // When & Then
        assertThat(ipv4_1).isEqualTo(ipv4_2);
        assertThat(ipv4_1).isNotEqualTo(ipv6);
        assertThat(ipv4_1 == ipv4_2).isTrue();
        assertThat(ipv4_1 == ipv6).isFalse();
    }

    @Test
    @DisplayName("IpType ordinal 테스트")
    void testIpTypeOrdinal() {
        // When & Then
        assertThat(IpType.IPv4.ordinal()).isEqualTo(0);
        assertThat(IpType.IPv6.ordinal()).isEqualTo(1);
    }

    @Test
    @DisplayName("IpType switch 문 테스트")
    void testIpTypeSwitch() {
        // Given & When & Then
        String result1 = switch (IpType.IPv4) {
            case IPv4 -> "Internet Protocol version 4";
            case IPv6 -> "Internet Protocol version 6";
        };
        
        String result2 = switch (IpType.IPv6) {
            case IPv4 -> "Internet Protocol version 4";
            case IPv6 -> "Internet Protocol version 6";
        };
        
        assertThat(result1).isEqualTo("Internet Protocol version 4");
        assertThat(result2).isEqualTo("Internet Protocol version 6");
    }
}