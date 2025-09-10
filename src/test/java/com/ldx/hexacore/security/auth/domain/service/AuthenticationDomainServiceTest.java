package com.ldx.hexacore.security.auth.domain.service;

import com.ldx.hexacore.security.auth.domain.Authentication;
import com.ldx.hexacore.security.auth.domain.vo.Credentials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AuthenticationDomainService 테스트")
class AuthenticationDomainServiceTest {

    private AuthenticationDomainService authenticationDomainService;

    @BeforeEach
    void setUp() {
        authenticationDomainService = new AuthenticationDomainService();
    }

    @Test
    @DisplayName("유효한 자격증명으로 인증을 처리할 수 있다")
    void shouldAuthenticateWithValidCredentials() {
        // Given
        Credentials credentials = Credentials.of("validuser", "validpassword");

        // When
        Authentication authentication = authenticationDomainService.authenticate(credentials);

        // Then
        assertThat(authentication).isNotNull();
        assertThat(authentication.getCredentials()).isEqualTo(credentials);
        assertThat(authentication.getStatus().isPending()).isTrue();
        assertThat(authentication.getId()).isNotNull();
        assertThat(authentication.getAttemptTime()).isNotNull();
    }

    @Test
    @DisplayName("null 자격증명으로 인증 시 예외가 발생한다")
    void shouldThrowExceptionWhenCredentialsIsNull() {
        // When & Then
        assertThatThrownBy(() -> authenticationDomainService.authenticate(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Credentials cannot be null");
    }

    @Test
    @DisplayName("빈 username으로 인증 시 도메인 규칙을 적용한다")
    void shouldApplyDomainRulesForInvalidCredentials() {
        // When & Then
        // Credentials VO 자체에서 검증되어 예외 발생
        assertThatThrownBy(() -> Credentials.of("", "password"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("여러 인증 요청을 독립적으로 처리할 수 있다")
    void shouldHandleMultipleAuthenticationRequestsIndependently() {
        // Given
        Credentials credentials1 = Credentials.of("user1", "password1");
        Credentials credentials2 = Credentials.of("user2", "password2");

        // When
        Authentication auth1 = authenticationDomainService.authenticate(credentials1);
        Authentication auth2 = authenticationDomainService.authenticate(credentials2);

        // Then
        assertThat(auth1).isNotNull();
        assertThat(auth2).isNotNull();
        assertThat(auth1.getId()).isNotEqualTo(auth2.getId());
        assertThat(auth1.getCredentials()).isNotEqualTo(auth2.getCredentials());
    }

    @Test
    @DisplayName("인증 서비스는 stateless하게 동작한다")
    void shouldOperateStateless() {
        // Given
        Credentials credentials = Credentials.of("testuser", "testpassword");

        // When
        Authentication auth1 = authenticationDomainService.authenticate(credentials);
        Authentication auth2 = authenticationDomainService.authenticate(credentials);

        // Then
        assertThat(auth1.getId()).isNotEqualTo(auth2.getId());
        assertThat(auth1.getAttemptTime()).isNotEqualTo(auth2.getAttemptTime());
    }

    @Test
    @DisplayName("도메인 이벤트가 올바르게 생성된다")
    void shouldGenerateCorrectDomainEvents() {
        // Given
        Credentials credentials = Credentials.of("testuser", "testpassword");

        // When
        Authentication authentication = authenticationDomainService.authenticate(credentials);

        // Then
        assertThat(authentication.getDomainEvents()).hasSize(1);
        assertThat(authentication.getDomainEvents().get(0).getClass().getSimpleName())
            .isEqualTo("AuthenticationAttempted");
    }
}