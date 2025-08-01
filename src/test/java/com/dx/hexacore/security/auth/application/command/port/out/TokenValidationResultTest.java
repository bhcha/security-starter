package com.dx.hexacore.security.auth.application.command.port.out;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TokenValidationResult 테스트")
class TokenValidationResultTest {

    @Test
    @DisplayName("유효한 토큰 결과 생성 테스트")
    void shouldCreateValidTokenResult() {
        // given
        boolean valid = true;
        String userId = "user123";
        String username = "testuser";
        Set<String> authorities = Set.of("ROLE_USER", "ROLE_ADMIN");
        Instant expiresAt = Instant.now().plusSeconds(3600);
        Map<String, Object> claims = Map.of("iss", "test-issuer", "scope", "read write");
        
        // when
        TokenValidationResult result = new TokenValidationResult(
                valid, userId, username, authorities, expiresAt, claims
        );
        
        // then
        assertThat(result.valid()).isTrue();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.username()).isEqualTo(username);
        assertThat(result.authorities()).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
        assertThat(result.expiresAt()).isEqualTo(expiresAt);
        assertThat(result.claims()).containsEntry("iss", "test-issuer");
        assertThat(result.claims()).containsEntry("scope", "read write");
    }

    @Test
    @DisplayName("무효한 토큰 결과 생성 테스트")
    void shouldCreateInvalidTokenResult() {
        // given
        boolean valid = false;
        String userId = null;
        String username = null;
        Set<String> authorities = null;
        Instant expiresAt = null;
        Map<String, Object> claims = null;
        
        // when
        TokenValidationResult result = new TokenValidationResult(
                valid, userId, username, authorities, expiresAt, claims
        );
        
        // then
        assertThat(result.valid()).isFalse();
        assertThat(result.userId()).isNull();
        assertThat(result.username()).isNull();
        assertThat(result.authorities()).isNull();
        assertThat(result.expiresAt()).isNull();
        assertThat(result.claims()).isNull();
    }

    @Test
    @DisplayName("null userId로 생성 테스트")
    void shouldCreateWithNullUserId() {
        // given
        String nullUserId = null;
        
        // when
        TokenValidationResult result = new TokenValidationResult(
                true, nullUserId, "username", Set.of(), Instant.now(), Map.of()
        );
        
        // then
        assertThat(result.userId()).isNull();
        assertThat(result.valid()).isTrue();
    }

    @Test
    @DisplayName("null username으로 생성 테스트")
    void shouldCreateWithNullUsername() {
        // given
        String nullUsername = null;
        
        // when
        TokenValidationResult result = new TokenValidationResult(
                true, "userId", nullUsername, Set.of(), Instant.now(), Map.of()
        );
        
        // then
        assertThat(result.username()).isNull();
        assertThat(result.valid()).isTrue();
    }

    @Test
    @DisplayName("null authorities로 생성 테스트")
    void shouldCreateWithNullAuthorities() {
        // given
        Set<String> nullAuthorities = null;
        
        // when
        TokenValidationResult result = new TokenValidationResult(
                true, "userId", "username", nullAuthorities, Instant.now(), Map.of()
        );
        
        // then
        assertThat(result.authorities()).isNull();
        assertThat(result.valid()).isTrue();
    }

    @Test
    @DisplayName("null expiresAt으로 생성 테스트")
    void shouldCreateWithNullExpiresAt() {
        // given
        Instant nullExpiresAt = null;
        
        // when
        TokenValidationResult result = new TokenValidationResult(
                true, "userId", "username", Set.of(), nullExpiresAt, Map.of()
        );
        
        // then
        assertThat(result.expiresAt()).isNull();
        assertThat(result.valid()).isTrue();
    }

    @Test
    @DisplayName("null claims로 생성 테스트")
    void shouldCreateWithNullClaims() {
        // given
        Map<String, Object> nullClaims = null;
        
        // when
        TokenValidationResult result = new TokenValidationResult(
                true, "userId", "username", Set.of(), Instant.now(), nullClaims
        );
        
        // then
        assertThat(result.claims()).isNull();
        assertThat(result.valid()).isTrue();
    }

    @Test
    @DisplayName("빈 authorities로 생성 테스트")
    void shouldCreateWithEmptyAuthorities() {
        // given
        Set<String> emptyAuthorities = Set.of();
        
        // when
        TokenValidationResult result = new TokenValidationResult(
                true, "userId", "username", emptyAuthorities, Instant.now(), Map.of()
        );
        
        // then
        assertThat(result.authorities()).isEmpty();
        assertThat(result.valid()).isTrue();
    }

    @Test
    @DisplayName("valid 팩토리 메서드 테스트")
    void shouldCreateValidResultUsingFactoryMethod() {
        // given
        String userId = "user123";
        String username = "testuser";
        Set<String> authorities = Set.of("ROLE_USER");
        Instant expiresAt = Instant.now().plusSeconds(3600);
        
        // when
        TokenValidationResult result = TokenValidationResult.valid(userId, username, authorities, expiresAt);
        
        // then
        assertThat(result.valid()).isTrue();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.username()).isEqualTo(username);
        assertThat(result.authorities()).isEqualTo(authorities);
        assertThat(result.expiresAt()).isEqualTo(expiresAt);
        assertThat(result.claims()).isEmpty();
    }

    @Test
    @DisplayName("valid 팩토리 메서드 - null userId 테스트")
    void shouldThrowExceptionForNullUserIdInValidFactory() {
        // given
        String nullUserId = null;
        
        // when & then
        assertThatThrownBy(() -> TokenValidationResult.valid(
                nullUserId, "username", Set.of(), Instant.now())
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User ID cannot be null or empty");
    }

    @Test
    @DisplayName("valid 팩토리 메서드 - 빈 userId 테스트")
    void shouldThrowExceptionForEmptyUserIdInValidFactory() {
        // given
        String emptyUserId = "";
        
        // when & then
        assertThatThrownBy(() -> TokenValidationResult.valid(
                emptyUserId, "username", Set.of(), Instant.now())
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User ID cannot be null or empty");
    }

    @Test
    @DisplayName("valid 팩토리 메서드 - null username 테스트")
    void shouldThrowExceptionForNullUsernameInValidFactory() {
        // given
        String nullUsername = null;
        
        // when & then
        assertThatThrownBy(() -> TokenValidationResult.valid(
                "userId", nullUsername, Set.of(), Instant.now())
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username cannot be null or empty");
    }

    @Test
    @DisplayName("valid 팩토리 메서드 - 빈 username 테스트")
    void shouldThrowExceptionForEmptyUsernameInValidFactory() {
        // given
        String emptyUsername = "";
        
        // when & then
        assertThatThrownBy(() -> TokenValidationResult.valid(
                "userId", emptyUsername, Set.of(), Instant.now())
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username cannot be null or empty");
    }

    @Test
    @DisplayName("invalid 팩토리 메서드 테스트")
    void shouldCreateInvalidResultUsingFactoryMethod() {
        // given
        String reason = "Token signature invalid";
        
        // when
        TokenValidationResult result = TokenValidationResult.invalid(reason);
        
        // then
        assertThat(result.valid()).isFalse();
        assertThat(result.userId()).isNull();
        assertThat(result.username()).isNull();
        assertThat(result.authorities()).isNull();
        assertThat(result.expiresAt()).isNull();
        assertThat(result.claims()).containsEntry("error", reason);
    }

    @Test
    @DisplayName("invalid 팩토리 메서드 - null 사유 테스트")
    void shouldThrowExceptionForNullReasonInInvalidFactory() {
        // given
        String nullReason = null;
        
        // when & then
        assertThatThrownBy(() -> TokenValidationResult.invalid(nullReason))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Reason cannot be null or empty");
    }

    @Test
    @DisplayName("invalid 팩토리 메서드 - 빈 사유 테스트")
    void shouldThrowExceptionForEmptyReasonInInvalidFactory() {
        // given
        String emptyReason = "";
        
        // when & then
        assertThatThrownBy(() -> TokenValidationResult.invalid(emptyReason))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Reason cannot be null or empty");
    }

    @Test
    @DisplayName("동등성 테스트")
    void shouldBeEqualForSameProperties() {
        // given
        String userId = "user123";
        String username = "testuser";
        Set<String> authorities = Set.of("ROLE_USER");
        Instant expiresAt = Instant.now().plusSeconds(3600);
        Map<String, Object> claims = Map.of("iss", "test");
        
        TokenValidationResult result1 = new TokenValidationResult(
                true, userId, username, authorities, expiresAt, claims
        );
        TokenValidationResult result2 = new TokenValidationResult(
                true, userId, username, authorities, expiresAt, claims
        );
        
        // when & then
        assertThat(result1).isEqualTo(result2);
    }

    @Test
    @DisplayName("서로 다른 인스턴스 동등성 테스트")
    void shouldNotBeEqualForDifferentProperties() {
        // given
        TokenValidationResult result1 = TokenValidationResult.valid(
                "user1", "testuser1", Set.of("ROLE_USER"), Instant.now()
        );
        TokenValidationResult result2 = TokenValidationResult.valid(
                "user2", "testuser2", Set.of("ROLE_ADMIN"), Instant.now()
        );
        
        // when & then
        assertThat(result1).isNotEqualTo(result2);
    }

    @Test
    @DisplayName("hashCode 일관성 테스트")
    void shouldHaveConsistentHashCode() {
        // given
        String userId = "user123";
        String username = "testuser";
        Set<String> authorities = Set.of("ROLE_USER");
        Instant expiresAt = Instant.now().plusSeconds(3600);
        Map<String, Object> claims = Map.of("iss", "test");
        
        TokenValidationResult result1 = new TokenValidationResult(
                true, userId, username, authorities, expiresAt, claims
        );
        TokenValidationResult result2 = new TokenValidationResult(
                true, userId, username, authorities, expiresAt, claims
        );
        
        // when
        int hashCode1 = result1.hashCode();
        int hashCode2 = result2.hashCode();
        
        // then
        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    @DisplayName("불변성 테스트")
    void shouldBeImmutable() {
        // given
        Set<String> originalAuthorities = Set.of("ROLE_USER");
        Map<String, Object> originalClaims = new HashMap<>();
        originalClaims.put("iss", "test");
        
        TokenValidationResult result = new TokenValidationResult(
                true, "userId", "username", originalAuthorities, Instant.now(), originalClaims
        );
        
        // when - 원본 컬렉션 수정 시도
        originalClaims.put("new", "value");
        
        // then - 결과 객체는 변경되지 않음
        assertThat(result.claims()).hasSize(1);
        assertThat(result.claims()).containsOnlyKeys("iss");
        assertThat(result.authorities()).containsExactly("ROLE_USER");
    }
}