package com.ldx.hexacore.security.session.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ClientIp Value Object 테스트")
class ClientIpTest {

    @Test
    @DisplayName("유효한 IPv4 주소로 ClientIp 생성")
    void createClientIpWithValidIPv4() {
        // Given
        String ipv4 = "192.168.0.1";
        
        // When
        ClientIp clientIp = ClientIp.of(ipv4);
        
        // Then
        assertThat(clientIp.getIpAddress()).isEqualTo(ipv4);
        assertThat(clientIp.getType()).isEqualTo(IpType.IPv4);
        assertThat(clientIp.isIPv4()).isTrue();
        assertThat(clientIp.isIPv6()).isFalse();
    }

    @Test
    @DisplayName("유효한 IPv6 주소로 ClientIp 생성")
    void createClientIpWithValidIPv6() {
        // Given
        String ipv6 = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
        
        // When
        ClientIp clientIp = ClientIp.of(ipv6);
        
        // Then
        assertThat(clientIp.getIpAddress()).isEqualTo(ipv6);
        assertThat(clientIp.getType()).isEqualTo(IpType.IPv6);
        assertThat(clientIp.isIPv4()).isFalse();
        assertThat(clientIp.isIPv6()).isTrue();
    }

    @Test
    @DisplayName("로컬호스트 IPv4 주소 테스트")
    void createClientIpWithLocalhostIPv4() {
        // Given
        String localhost = "127.0.0.1";
        
        // When
        ClientIp clientIp = ClientIp.of(localhost);
        
        // Then
        assertThat(clientIp.getIpAddress()).isEqualTo(localhost);
        assertThat(clientIp.getType()).isEqualTo(IpType.IPv4);
        assertThat(clientIp.isLocalhost()).isTrue();
    }

    @Test
    @DisplayName("로컬호스트 IPv6 주소 테스트")
    void createClientIpWithLocalhostIPv6() {
        // Given
        String localhost = "::1";
        
        // When
        ClientIp clientIp = ClientIp.of(localhost);
        
        // Then
        assertThat(clientIp.getIpAddress()).isEqualTo(localhost);
        assertThat(clientIp.getType()).isEqualTo(IpType.IPv6);
        assertThat(clientIp.isLocalhost()).isTrue();
    }

    @Test
    @DisplayName("null IP 주소로 생성 시 예외 발생")
    void createClientIpWithNullIp() {
        // When & Then
        assertThatThrownBy(() -> ClientIp.of(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("IP address cannot be null or empty");
    }

    @Test
    @DisplayName("빈 IP 주소로 생성 시 예외 발생")
    void createClientIpWithEmptyIp() {
        // When & Then
        assertThatThrownBy(() -> ClientIp.of(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("IP address cannot be null or empty");
    }

    @Test
    @DisplayName("공백만 있는 IP 주소로 생성 시 예외 발생")
    void createClientIpWithBlankIp() {
        // When & Then
        assertThatThrownBy(() -> ClientIp.of("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("IP address cannot be null or empty");
    }

    @Test
    @DisplayName("앞뒤 공백이 있는 IP 주소로 생성 시 예외 발생")
    void createClientIpWithTrimmedIp() {
        // Given
        String ipWithSpaces = " 192.168.0.1 ";
        
        // When & Then
        assertThatThrownBy(() -> ClientIp.of(ipWithSpaces))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid IP address format: " + ipWithSpaces);
    }

    @Test
    @DisplayName("잘못된 IP 주소 형식으로 생성 시 예외 발생")
    void createClientIpWithInvalidFormat() {
        // Given
        String invalidIp = "999.999.999.999";
        
        // When & Then
        assertThatThrownBy(() -> ClientIp.of(invalidIp))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid IP address format: " + invalidIp);
    }

    @Test
    @DisplayName("잘못된 문자가 포함된 IP 주소로 생성 시 예외 발생")
    void createClientIpWithInvalidCharacters() {
        // Given
        String invalidIp = "192.168.0.abc";
        
        // When & Then
        assertThatThrownBy(() -> ClientIp.of(invalidIp))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid IP address format: " + invalidIp);
    }

    @Test
    @DisplayName("ClientIp 동등성 테스트")
    void clientIpEquality() {
        // Given
        String ip = "192.168.0.1";
        ClientIp clientIp1 = ClientIp.of(ip);
        ClientIp clientIp2 = ClientIp.of(ip);
        ClientIp clientIp3 = ClientIp.of("192.168.0.2");
        
        // When & Then
        assertThat(clientIp1).isEqualTo(clientIp2);
        assertThat(clientIp1).isNotEqualTo(clientIp3);
        assertThat(clientIp1.hashCode()).isEqualTo(clientIp2.hashCode());
    }

    @Test
    @DisplayName("IPv4 사설 IP 주소 테스트")
    void testPrivateIPv4Addresses() {
        // Given & When & Then
        assertThat(ClientIp.of("10.0.0.1").isIPv4()).isTrue();
        assertThat(ClientIp.of("172.16.0.1").isIPv4()).isTrue();
        assertThat(ClientIp.of("192.168.1.1").isIPv4()).isTrue();
    }

    @Test
    @DisplayName("IPv4 공인 IP 주소 테스트")
    void testPublicIPv4Addresses() {
        // Given & When & Then
        assertThat(ClientIp.of("8.8.8.8").isIPv4()).isTrue();
        assertThat(ClientIp.of("1.1.1.1").isIPv4()).isTrue();
        assertThat(ClientIp.of("208.67.222.222").isIPv4()).isTrue();
    }

    @Test
    @DisplayName("IPv6 주소 형식 테스트")
    void testIPv6AddressFormats() {
        // Given & When & Then
        assertThat(ClientIp.of("2001:db8::1").isIPv6()).isTrue();
        assertThat(ClientIp.of("fe80::1").isIPv6()).isTrue();
        assertThat(ClientIp.of("::ffff:192.0.2.1").isIPv6()).isTrue();
    }
}