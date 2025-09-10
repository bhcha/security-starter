package com.ldx.hexacore.security.auth.domain.vo;

import com.ldx.hexacore.security.util.ValidationMessages;
import jakarta.persistence.Embeddable;
import java.util.Objects;

/**
 * 인증 상태를 나타내는 값 객체.
 * 
 * <p>인증 과정의 상태(PENDING, SUCCESS, FAILED)를 표현하는 불변 객체입니다.
 * 
 * <p>허용되는 상태:
 * <ul>
 *   <li>PENDING: 인증 대기 중</li>
 *   <li>SUCCESS: 인증 성공</li>
 *   <li>FAILED: 인증 실패</li>
 * </ul>
 * 
 * @since 1.0.0
 */
@Embeddable
public final class AuthenticationStatus {
    
    public enum Status {
        PENDING, SUCCESS, FAILED
    }
    
    private final Status status;
    
    // JPA를 위한 protected 기본 생성자
    protected AuthenticationStatus() {
        this.status = null;
    }
    
    // private 생성자
    private AuthenticationStatus(Status status) {
        validateStatus(status);
        this.status = status;
    }
    
    // ===== 정적 팩토리 메서드 =====
    
    /**
     * PENDING 상태의 AuthenticationStatus를 생성합니다.
     * 
     * @return PENDING 상태의 AuthenticationStatus
     */
    public static AuthenticationStatus pending() {
        return new AuthenticationStatus(Status.PENDING);
    }
    
    /**
     * SUCCESS 상태의 AuthenticationStatus를 생성합니다.
     * 
     * @return SUCCESS 상태의 AuthenticationStatus
     */
    public static AuthenticationStatus success() {
        return new AuthenticationStatus(Status.SUCCESS);
    }
    
    /**
     * FAILED 상태의 AuthenticationStatus를 생성합니다.
     * 
     * @return FAILED 상태의 AuthenticationStatus
     */
    public static AuthenticationStatus failed() {
        return new AuthenticationStatus(Status.FAILED);
    }
    
    /**
     * 문자열로부터 AuthenticationStatus를 생성합니다.
     * 
     * @param statusString 상태 문자열
     * @return 생성된 AuthenticationStatus
     * @throws IllegalArgumentException 유효하지 않은 상태인 경우
     */
    public static AuthenticationStatus of(String statusString) {
        if (statusString == null || statusString.isBlank()) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeEmpty("Status"));
        }
        
        try {
            Status status = Status.valueOf(statusString.toUpperCase());
            return new AuthenticationStatus(status);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                String.format("Invalid authentication status: %s", statusString)
            );
        }
    }
    
    // ===== 상태 확인 메서드 =====
    
    /**
     * PENDING 상태인지 확인합니다.
     * 
     * @return PENDING 상태이면 true
     */
    public boolean isPending() {
        return status == Status.PENDING;
    }
    
    /**
     * SUCCESS 상태인지 확인합니다.
     * 
     * @return SUCCESS 상태이면 true
     */
    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }
    
    /**
     * FAILED 상태인지 확인합니다.
     * 
     * @return FAILED 상태이면 true
     */
    public boolean isFailed() {
        return status == Status.FAILED;
    }
    
    // ===== Private 검증 메서드 =====
    
    private static void validateStatus(Status status) {
        if (status == null) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNull("Status"));
        }
    }
    
    // ===== Object 메서드 =====
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthenticationStatus that = (AuthenticationStatus) o;
        return status == that.status;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(status);
    }
    
    @Override
    public String toString() {
        return status.name();
    }
}