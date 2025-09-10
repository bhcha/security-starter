package com.ldx.hexacore.security.auth.application.command.port.out;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TokenProviderType 테스트")
class TokenProviderTypeTest {

    @Test
    @DisplayName("KEYCLOAK 타입 코드 값 검증")
    void shouldReturnKeycloakCode() {
        // given
        TokenProviderType type = TokenProviderType.KEYCLOAK;
        
        // when
        String code = type.getCode();
        
        // then
        assertThat(code).isEqualTo("keycloak");
    }

    @Test
    @DisplayName("KEYCLOAK 타입 설명 검증")
    void shouldReturnKeycloakDescription() {
        // given
        TokenProviderType type = TokenProviderType.KEYCLOAK;
        
        // when
        String description = type.getDescription();
        
        // then
        assertThat(description).isEqualTo("Keycloak OAuth2 Provider");
    }

    @Test
    @DisplayName("SPRING_JWT 타입 코드 값 검증")
    void shouldReturnSpringJwtCode() {
        // given
        TokenProviderType type = TokenProviderType.SPRING_JWT;
        
        // when
        String code = type.getCode();
        
        // then
        assertThat(code).isEqualTo("jwt");
    }

    @Test
    @DisplayName("SPRING_JWT 타입 설명 검증")
    void shouldReturnSpringJwtDescription() {
        // given
        TokenProviderType type = TokenProviderType.SPRING_JWT;
        
        // when
        String description = type.getDescription();
        
        // then
        assertThat(description).isEqualTo("Spring JWT Provider");
    }

    @Test
    @DisplayName("코드로 타입 찾기 - KEYCLOAK")
    void shouldFindKeycloakByCode() {
        // given
        String code = "keycloak";
        
        // when
        TokenProviderType type = TokenProviderType.fromCode(code);
        
        // then
        assertThat(type).isEqualTo(TokenProviderType.KEYCLOAK);
    }

    @Test
    @DisplayName("코드로 타입 찾기 - SPRING_JWT")
    void shouldFindSpringJwtByCode() {
        // given
        String code = "jwt";
        
        // when
        TokenProviderType type = TokenProviderType.fromCode(code);
        
        // then
        assertThat(type).isEqualTo(TokenProviderType.SPRING_JWT);
    }

    @Test
    @DisplayName("존재하지 않는 코드로 타입 찾기")
    void shouldThrowExceptionForUnknownCode() {
        // given
        String unknownCode = "unknown";
        
        // when & then
        assertThatThrownBy(() -> TokenProviderType.fromCode(unknownCode))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown token provider type code: " + unknownCode);
    }

    @Test
    @DisplayName("null 코드로 타입 찾기")
    void shouldThrowExceptionForNullCode() {
        // given
        String nullCode = null;
        
        // when & then
        assertThatThrownBy(() -> TokenProviderType.fromCode(nullCode))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Token provider type code cannot be null");
    }

    @Test
    @DisplayName("같은 타입 동등성 검증")
    void shouldBeEqualForSameType() {
        // given
        TokenProviderType type1 = TokenProviderType.KEYCLOAK;
        TokenProviderType type2 = TokenProviderType.KEYCLOAK;
        
        // when & then
        assertThat(type1).isEqualTo(type2);
    }

    @Test
    @DisplayName("다른 타입 동등성 검증")
    void shouldNotBeEqualForDifferentTypes() {
        // given
        TokenProviderType keycloak = TokenProviderType.KEYCLOAK;
        TokenProviderType springJwt = TokenProviderType.SPRING_JWT;
        
        // when & then
        assertThat(keycloak).isNotEqualTo(springJwt);
    }

    @Test
    @DisplayName("null과의 동등성 검증")
    void shouldNotBeEqualToNull() {
        // given
        TokenProviderType type = TokenProviderType.KEYCLOAK;
        
        // when & then
        assertThat(type).isNotEqualTo(null);
    }

    @Test
    @DisplayName("hashCode 일관성 검증")
    void shouldHaveConsistentHashCode() {
        // given
        TokenProviderType type1 = TokenProviderType.KEYCLOAK;
        TokenProviderType type2 = TokenProviderType.KEYCLOAK;
        
        // when
        int hashCode1 = type1.hashCode();
        int hashCode2 = type2.hashCode();
        
        // then
        assertThat(hashCode1).isEqualTo(hashCode2);
    }
}