package com.dx.hexacore.security.auth.application.command.port.out;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TokenProviderErrorCode 테스트")
class TokenProviderErrorCodeTest {

    @Test
    @DisplayName("TOKEN_ISSUE_FAILED 에러 코드 검증")
    void shouldReturnTokenIssueFailedString() {
        // given
        TokenProviderErrorCode errorCode = TokenProviderErrorCode.TOKEN_ISSUE_FAILED;
        
        // when
        String result = errorCode.toString();
        
        // then
        assertThat(result).isEqualTo("TOKEN_ISSUE_FAILED");
    }

    @Test
    @DisplayName("TOKEN_VALIDATION_FAILED 에러 코드 검증")
    void shouldReturnTokenValidationFailedString() {
        // given
        TokenProviderErrorCode errorCode = TokenProviderErrorCode.TOKEN_VALIDATION_FAILED;
        
        // when
        String result = errorCode.toString();
        
        // then
        assertThat(result).isEqualTo("TOKEN_VALIDATION_FAILED");
    }

    @Test
    @DisplayName("TOKEN_REFRESH_FAILED 에러 코드 검증")
    void shouldReturnTokenRefreshFailedString() {
        // given
        TokenProviderErrorCode errorCode = TokenProviderErrorCode.TOKEN_REFRESH_FAILED;
        
        // when
        String result = errorCode.toString();
        
        // then
        assertThat(result).isEqualTo("TOKEN_REFRESH_FAILED");
    }

    @Test
    @DisplayName("INVALID_CREDENTIALS 에러 코드 검증")
    void shouldReturnInvalidCredentialsString() {
        // given
        TokenProviderErrorCode errorCode = TokenProviderErrorCode.INVALID_CREDENTIALS;
        
        // when
        String result = errorCode.toString();
        
        // then
        assertThat(result).isEqualTo("INVALID_CREDENTIALS");
    }

    @Test
    @DisplayName("TOKEN_EXPIRED 에러 코드 검증")
    void shouldReturnTokenExpiredString() {
        // given
        TokenProviderErrorCode errorCode = TokenProviderErrorCode.TOKEN_EXPIRED;
        
        // when
        String result = errorCode.toString();
        
        // then
        assertThat(result).isEqualTo("TOKEN_EXPIRED");
    }

    @Test
    @DisplayName("PROVIDER_UNAVAILABLE 에러 코드 검증")
    void shouldReturnProviderUnavailableString() {
        // given
        TokenProviderErrorCode errorCode = TokenProviderErrorCode.PROVIDER_UNAVAILABLE;
        
        // when
        String result = errorCode.toString();
        
        // then
        assertThat(result).isEqualTo("PROVIDER_UNAVAILABLE");
    }
}