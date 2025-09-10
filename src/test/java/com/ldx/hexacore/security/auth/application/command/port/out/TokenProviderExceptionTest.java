package com.ldx.hexacore.security.auth.application.command.port.out;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TokenProviderException 테스트")
class TokenProviderExceptionTest {

    @Test
    @DisplayName("기본 생성자 테스트")
    void shouldCreateExceptionWithBasicConstructor() {
        // given
        String message = "Token provider error";
        TokenProviderErrorCode errorCode = TokenProviderErrorCode.TOKEN_ISSUE_FAILED;
        String providerType = "keycloak";
        
        // when
        TokenProviderException exception = new TokenProviderException(message, errorCode, providerType);
        
        // then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getProviderType()).isEqualTo(providerType);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("메시지와 원인 예외가 있는 생성자 테스트")
    void shouldCreateExceptionWithCause() {
        // given
        String message = "Token provider error";
        Throwable cause = new RuntimeException("Original cause");
        TokenProviderErrorCode errorCode = TokenProviderErrorCode.TOKEN_VALIDATION_FAILED;
        String providerType = "jwt";
        
        // when
        TokenProviderException exception = new TokenProviderException(message, cause, errorCode, providerType);
        
        // then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getProviderType()).isEqualTo(providerType);
    }

    @Test
    @DisplayName("null 메시지로 생성자 테스트")
    void shouldThrowExceptionForNullMessage() {
        // given
        String nullMessage = null;
        TokenProviderErrorCode errorCode = TokenProviderErrorCode.TOKEN_ISSUE_FAILED;
        String providerType = "keycloak";
        
        // when & then
        assertThatThrownBy(() -> new TokenProviderException(nullMessage, errorCode, providerType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Message cannot be null or empty");
    }

    @Test
    @DisplayName("null 에러코드로 생성자 테스트")
    void shouldThrowExceptionForNullErrorCode() {
        // given
        String message = "Token provider error";
        TokenProviderErrorCode nullErrorCode = null;
        String providerType = "keycloak";
        
        // when & then
        assertThatThrownBy(() -> new TokenProviderException(message, nullErrorCode, providerType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Error code cannot be null");
    }

    @Test
    @DisplayName("null 프로바이더 타입으로 생성자 테스트")
    void shouldThrowExceptionForNullProviderType() {
        // given
        String message = "Token provider error";
        TokenProviderErrorCode errorCode = TokenProviderErrorCode.TOKEN_ISSUE_FAILED;
        String nullProviderType = null;
        
        // when & then
        assertThatThrownBy(() -> new TokenProviderException(message, errorCode, nullProviderType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Provider type cannot be null or empty");
    }

    @Test
    @DisplayName("빈 메시지로 생성자 테스트")
    void shouldThrowExceptionForEmptyMessage() {
        // given
        String emptyMessage = "";
        TokenProviderErrorCode errorCode = TokenProviderErrorCode.TOKEN_ISSUE_FAILED;
        String providerType = "keycloak";
        
        // when & then
        assertThatThrownBy(() -> new TokenProviderException(emptyMessage, errorCode, providerType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Message cannot be null or empty");
    }

    @Test
    @DisplayName("빈 프로바이더 타입으로 생성자 테스트")
    void shouldThrowExceptionForEmptyProviderType() {
        // given
        String message = "Token provider error";
        TokenProviderErrorCode errorCode = TokenProviderErrorCode.TOKEN_ISSUE_FAILED;
        String emptyProviderType = "";
        
        // when & then
        assertThatThrownBy(() -> new TokenProviderException(message, errorCode, emptyProviderType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Provider type cannot be null or empty");
    }

    @Test
    @DisplayName("모든 null로 생성자 테스트")
    void shouldThrowExceptionForAllNullParameters() {
        // when & then
        assertThatThrownBy(() -> new TokenProviderException(null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Message cannot be null or empty");
    }

    @Test
    @DisplayName("tokenIssueFailed 팩토리 메서드 테스트")
    void shouldCreateTokenIssueFailedException() {
        // given
        String providerType = "keycloak";
        Throwable cause = new RuntimeException("Connection failed");
        
        // when
        TokenProviderException exception = TokenProviderException.tokenIssueFailed(providerType, cause);
        
        // then
        assertThat(exception.getErrorCode()).isEqualTo(TokenProviderErrorCode.TOKEN_ISSUE_FAILED);
        assertThat(exception.getProviderType()).isEqualTo(providerType);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getMessage()).contains("Failed to issue token");
    }

    @Test
    @DisplayName("tokenValidationFailed 팩토리 메서드 테스트")
    void shouldCreateTokenValidationFailedException() {
        // given
        String providerType = "jwt";
        Throwable cause = new RuntimeException("Invalid signature");
        
        // when
        TokenProviderException exception = TokenProviderException.tokenValidationFailed(providerType, cause);
        
        // then
        assertThat(exception.getErrorCode()).isEqualTo(TokenProviderErrorCode.TOKEN_VALIDATION_FAILED);
        assertThat(exception.getProviderType()).isEqualTo(providerType);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getMessage()).contains("Failed to validate token");
    }

    @Test
    @DisplayName("tokenRefreshFailed 팩토리 메서드 테스트")
    void shouldCreateTokenRefreshFailedException() {
        // given
        String providerType = "keycloak";
        Throwable cause = new RuntimeException("Refresh token expired");
        
        // when
        TokenProviderException exception = TokenProviderException.tokenRefreshFailed(providerType, cause);
        
        // then
        assertThat(exception.getErrorCode()).isEqualTo(TokenProviderErrorCode.TOKEN_REFRESH_FAILED);
        assertThat(exception.getProviderType()).isEqualTo(providerType);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getMessage()).contains("Failed to refresh token");
    }

    @Test
    @DisplayName("invalidCredentials 팩토리 메서드 테스트")
    void shouldCreateInvalidCredentialsException() {
        // given
        String providerType = "jwt";
        
        // when
        TokenProviderException exception = TokenProviderException.invalidCredentials(providerType);
        
        // then
        assertThat(exception.getErrorCode()).isEqualTo(TokenProviderErrorCode.INVALID_CREDENTIALS);
        assertThat(exception.getProviderType()).isEqualTo(providerType);
        assertThat(exception.getCause()).isNull();
        assertThat(exception.getMessage()).contains("Invalid credentials");
    }

    @Test
    @DisplayName("tokenExpired 팩토리 메서드 테스트")
    void shouldCreateTokenExpiredException() {
        // given
        String providerType = "keycloak";
        
        // when
        TokenProviderException exception = TokenProviderException.tokenExpired(providerType);
        
        // then
        assertThat(exception.getErrorCode()).isEqualTo(TokenProviderErrorCode.TOKEN_EXPIRED);
        assertThat(exception.getProviderType()).isEqualTo(providerType);
        assertThat(exception.getCause()).isNull();
        assertThat(exception.getMessage()).contains("Token has expired");
    }

    @Test
    @DisplayName("providerUnavailable 팩토리 메서드 테스트")
    void shouldCreateProviderUnavailableException() {
        // given
        String providerType = "keycloak";
        Throwable cause = new RuntimeException("Service unavailable");
        
        // when
        TokenProviderException exception = TokenProviderException.providerUnavailable(providerType, cause);
        
        // then
        assertThat(exception.getErrorCode()).isEqualTo(TokenProviderErrorCode.PROVIDER_UNAVAILABLE);
        assertThat(exception.getProviderType()).isEqualTo(providerType);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getMessage()).contains("Token provider is unavailable");
    }

    @Test
    @DisplayName("팩토리 메서드 null 프로바이더 타입 테스트")
    void shouldThrowExceptionForNullProviderTypeInFactoryMethods() {
        // given
        String nullProviderType = null;
        Throwable cause = new RuntimeException("test");
        
        // when & then
        assertThatThrownBy(() -> TokenProviderException.tokenIssueFailed(nullProviderType, cause))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Provider type cannot be null or empty");
                
        assertThatThrownBy(() -> TokenProviderException.tokenValidationFailed(nullProviderType, cause))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Provider type cannot be null or empty");
                
        assertThatThrownBy(() -> TokenProviderException.invalidCredentials(nullProviderType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Provider type cannot be null or empty");
    }

    @Test
    @DisplayName("팩토리 메서드 빈 프로바이더 타입 테스트")
    void shouldThrowExceptionForEmptyProviderTypeInFactoryMethods() {
        // given
        String emptyProviderType = "";
        Throwable cause = new RuntimeException("test");
        
        // when & then
        assertThatThrownBy(() -> TokenProviderException.tokenRefreshFailed(emptyProviderType, cause))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Provider type cannot be null or empty");
                
        assertThatThrownBy(() -> TokenProviderException.tokenExpired(emptyProviderType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Provider type cannot be null or empty");
                
        assertThatThrownBy(() -> TokenProviderException.providerUnavailable(emptyProviderType, cause))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Provider type cannot be null or empty");
    }
}