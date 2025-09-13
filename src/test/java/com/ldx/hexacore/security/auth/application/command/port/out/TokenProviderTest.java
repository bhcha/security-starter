package com.ldx.hexacore.security.auth.application.command.port.out;

import com.ldx.hexacore.security.auth.domain.vo.Credentials;
import com.ldx.hexacore.security.auth.domain.vo.Token;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TokenProvider 인터페이스 테스트")
class TokenProviderTest {

    @Test
    @DisplayName("issueToken 메서드 시그니처 테스트")
    void shouldHaveIssueTokenMethod() throws NoSuchMethodException {
        // when
        Method method = TokenProvider.class.getDeclaredMethod("issueToken", Credentials.class);
        
        // then
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(Token.class);
        assertThat(method.getParameterTypes()).containsExactly(Credentials.class);
        assertThat(Arrays.asList(method.getExceptionTypes())).contains(TokenProviderException.class);
    }

    @Test
    @DisplayName("validateToken 메서드 시그니처 테스트")
    void shouldHaveValidateTokenMethod() throws NoSuchMethodException {
        // when
        Method method = TokenProvider.class.getDeclaredMethod("validateToken", String.class);
        
        // then
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(TokenValidationResult.class);
        assertThat(method.getParameterTypes()).containsExactly(String.class);
        assertThat(Arrays.asList(method.getExceptionTypes())).contains(TokenProviderException.class);
    }

    @Test
    @DisplayName("refreshToken 메서드 시그니처 테스트")
    void shouldHaveRefreshTokenMethod() throws NoSuchMethodException {
        // when
        Method method = TokenProvider.class.getDeclaredMethod("refreshToken", String.class);
        
        // then
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(Token.class);
        assertThat(method.getParameterTypes()).containsExactly(String.class);
        assertThat(Arrays.asList(method.getExceptionTypes())).contains(TokenProviderException.class);
    }

    @Test
    @DisplayName("getProviderType 메서드 시그니처 테스트")
    void shouldHaveGetProviderTypeMethod() throws NoSuchMethodException {
        // when
        Method method = TokenProvider.class.getDeclaredMethod("getProviderType");
        
        // then
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(TokenProviderType.class);
        assertThat(method.getParameterTypes()).isEmpty();
    }

    @Test
    @DisplayName("인터페이스 public 접근제한자 테스트")
    void shouldBePublicInterface() {
        // when
        int modifiers = TokenProvider.class.getModifiers();
        
        // then
        assertThat(Modifier.isPublic(modifiers)).isTrue();
        assertThat(Modifier.isInterface(modifiers)).isTrue();
    }

    @Test
    @DisplayName("인터페이스 메서드 개수 테스트")
    void shouldHaveFourMethods() {
        // when
        Method[] methods = TokenProvider.class.getDeclaredMethods();
        
        // then
        assertThat(methods).hasSize(5);
    }

    @Test
    @DisplayName("인터페이스 패키지 위치 테스트")
    void shouldBeInCorrectPackage() {
        // when
        Package pkg = TokenProvider.class.getPackage();
        
        // then
        assertThat(pkg.getName()).isEqualTo("com.ldx.hexacore.security.auth.application.command.port.out");
    }

    @Test
    @DisplayName("인터페이스 어노테이션 테스트")
    void shouldHaveNoSpecialAnnotations() {
        // when
        var annotations = TokenProvider.class.getDeclaredAnnotations();
        
        // then
        assertThat(annotations).isEmpty();
    }
}