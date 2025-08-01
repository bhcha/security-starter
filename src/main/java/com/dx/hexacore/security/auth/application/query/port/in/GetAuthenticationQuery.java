package com.dx.hexacore.security.auth.application.query.port.in;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/**
 * 인증 정보 조회 Query.
 * 
 * @since 1.0.0
 */
public class GetAuthenticationQuery {

    @NotNull(message = "Authentication ID is required")
    @NotBlank(message = "Authentication ID cannot be blank")
    private final String authenticationId;

    private GetAuthenticationQuery(String authenticationId) {
        this.authenticationId = validateAuthenticationId(authenticationId);
    }

    /**
     * 인증 ID로 Query 객체를 생성합니다.
     * 
     * @param authenticationId 조회할 인증 ID
     * @return 생성된 Query 객체
     * @throws IllegalArgumentException 인증 ID가 null, 빈 문자열 또는 공백인 경우
     */
    public static GetAuthenticationQuery of(String authenticationId) {
        return new GetAuthenticationQuery(authenticationId);
    }

    /**
     * Builder 패턴을 위한 Builder 객체 생성.
     * 
     * @return Builder 객체
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 인증 ID를 반환합니다.
     * 
     * @return 인증 ID
     */
    public String getAuthenticationId() {
        return authenticationId;
    }

    private String validateAuthenticationId(String authenticationId) {
        if (authenticationId == null) {
            throw new IllegalArgumentException("Authentication ID cannot be null");
        }
        if (authenticationId.isEmpty()) {
            throw new IllegalArgumentException("Authentication ID cannot be empty");
        }
        if (authenticationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Authentication ID cannot be blank");
        }
        return authenticationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetAuthenticationQuery that = (GetAuthenticationQuery) o;
        return Objects.equals(authenticationId, that.authenticationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authenticationId);
    }

    @Override
    public String toString() {
        return "GetAuthenticationQuery{" +
               "authenticationId='" + authenticationId + '\'' +
               '}';
    }

    /**
     * Builder 클래스.
     */
    public static class Builder {
        private String authenticationId;

        private Builder() {}

        /**
         * 인증 ID를 설정합니다.
         * 
         * @param authenticationId 설정할 인증 ID
         * @return Builder 객체
         */
        public Builder authenticationId(String authenticationId) {
            this.authenticationId = authenticationId;
            return this;
        }

        /**
         * Query 객체를 생성합니다.
         * 
         * @return 생성된 Query 객체
         */
        public GetAuthenticationQuery build() {
            return new GetAuthenticationQuery(authenticationId);
        }
    }
}