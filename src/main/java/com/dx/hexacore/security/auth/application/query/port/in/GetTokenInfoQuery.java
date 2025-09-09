package com.dx.hexacore.security.auth.application.query.port.in;

import com.dx.hexacore.security.util.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/**
 * 토큰 정보 조회 Query.
 * 
 * @since 1.0.0
 */
public class GetTokenInfoQuery {

    @NotNull(message = "Token is required")
    @NotBlank(message = "Token cannot be blank")
    private final String token;

    private GetTokenInfoQuery(String token) {
        this.token = validateToken(token);
    }

    /**
     * 토큰으로 Query 객체를 생성합니다.
     * 
     * @param token 조회할 토큰
     * @return 생성된 Query 객체
     * @throws IllegalArgumentException 토큰이 null, 빈 문자열 또는 공백인 경우
     */
    public static GetTokenInfoQuery of(String token) {
        return new GetTokenInfoQuery(token);
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
     * 토큰을 반환합니다.
     * 
     * @return 토큰
     */
    public String getToken() {
        return token;
    }

    private String validateToken(String token) {
        if (token == null) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeNull("Token"));
        }
        if (token.isEmpty()) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeEmpty("Token"));
        }
        if (token.trim().isEmpty()) {
            throw new IllegalArgumentException(ValidationMessages.cannotBeBlank("Token"));
        }
        return token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetTokenInfoQuery that = (GetTokenInfoQuery) o;
        return Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token);
    }

    @Override
    public String toString() {
        return "GetTokenInfoQuery{" +
               "token='" + token + '\'' +
               '}';
    }

    /**
     * Builder 클래스.
     */
    public static class Builder {
        private String token;

        private Builder() {}

        /**
         * 토큰을 설정합니다.
         * 
         * @param token 설정할 토큰
         * @return Builder 객체
         */
        public Builder token(String token) {
            this.token = token;
            return this;
        }

        /**
         * Query 객체를 생성합니다.
         * 
         * @return 생성된 Query 객체
         */
        public GetTokenInfoQuery build() {
            return new GetTokenInfoQuery(token);
        }
    }
}