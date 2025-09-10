package com.ldx.hexacore.security.session.domain.vo;

import com.ldx.hexacore.security.util.ValidationUtils;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 * 클라이언트 IP 주소를 나타내는 Value Object
 */
public class ClientIp {
    
    private final String ipAddress;
    private final IpType type;
    
    private ClientIp(String ipAddress, IpType type) {
        this.ipAddress = ipAddress;
        this.type = type;
    }
    
    /**
     * IP 주소 문자열로부터 ClientIp를 생성합니다.
     */
    public static ClientIp of(String ipAddress) {
        String validatedIp = ValidationUtils.requireValidIpAddress(ipAddress, "IP address");
        IpType type = determineIpType(validatedIp);
        return new ClientIp(validatedIp, type);
    }
    
    /**
     * IP 주소 타입을 결정합니다.
     */
    private static IpType determineIpType(String ipAddress) {
        return ipAddress.contains(":") ? IpType.IPv6 : IpType.IPv4;
    }
    
    /**
     * IP 주소를 반환합니다.
     */
    public String getIpAddress() {
        return ipAddress;
    }
    
    /**
     * IP 타입을 반환합니다.
     */
    public IpType getType() {
        return type;
    }
    
    /**
     * IPv4 주소인지 확인합니다.
     */
    public boolean isIPv4() {
        return type == IpType.IPv4;
    }
    
    /**
     * IPv6 주소인지 확인합니다.
     */
    public boolean isIPv6() {
        return type == IpType.IPv6;
    }
    
    /**
     * 로컬호스트 주소인지 확인합니다.
     */
    public boolean isLocalhost() {
        try {
            InetAddress address = InetAddress.getByName(ipAddress);
            return address.isLoopbackAddress();
        } catch (UnknownHostException e) {
            return false;
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ClientIp clientIp = (ClientIp) obj;
        return Objects.equals(ipAddress, clientIp.ipAddress) &&
               type == clientIp.type;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(ipAddress, type);
    }
    
    @Override
    public String toString() {
        return String.format("ClientIp{ipAddress='%s', type=%s}", ipAddress, type);
    }
}