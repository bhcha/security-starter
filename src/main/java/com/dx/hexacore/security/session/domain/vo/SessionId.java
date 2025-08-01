package com.dx.hexacore.security.session.domain.vo;

import java.util.UUID;

/**
 * 세션 식별자를 나타내는 Value Object
 */
public record SessionId(UUID value) {
    
    public SessionId {
        if (value == null) {
            throw new IllegalArgumentException("SessionId UUID cannot be null");
        }
    }
    
    /**
     * 새로운 세션 ID를 생성합니다.
     */
    public static SessionId generate() {
        return new SessionId(UUID.randomUUID());
    }
    
    /**
     * UUID로부터 세션 ID를 생성합니다.
     */
    public static SessionId of(UUID value) {
        return new SessionId(value);
    }
    
    /**
     * 문자열로부터 세션 ID를 생성합니다.
     */
    public static SessionId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("SessionId string cannot be null or empty");
        }
        
        try {
            UUID uuid = UUID.fromString(value);
            return new SessionId(uuid);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format: " + value);
        }
    }
    
    /**
     * UUID 값을 반환합니다.
     */
    public UUID getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return value.toString();
    }
}