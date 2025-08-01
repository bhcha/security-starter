package com.dx.hexacore.security.session.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SessionId Value Object 테스트")
class SessionIdTest {

    @Test
    @DisplayName("UUID로 SessionId 생성 성공")
    void createSessionIdWithUUID() {
        // Given
        UUID uuid = UUID.randomUUID();
        
        // When
        SessionId sessionId = SessionId.of(uuid);
        
        // Then
        assertThat(sessionId.getValue()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("문자열로 SessionId 생성 성공")
    void createSessionIdWithString() {
        // Given
        String uuidString = "550e8400-e29b-41d4-a716-446655440000";
        UUID expectedUuid = UUID.fromString(uuidString);
        
        // When
        SessionId sessionId = SessionId.of(uuidString);
        
        // Then
        assertThat(sessionId.getValue()).isEqualTo(expectedUuid);
    }

    @Test
    @DisplayName("새로운 SessionId 생성")
    void generateNewSessionId() {
        // When
        SessionId sessionId1 = SessionId.generate();
        SessionId sessionId2 = SessionId.generate();
        
        // Then
        assertThat(sessionId1.getValue()).isNotNull();
        assertThat(sessionId2.getValue()).isNotNull();
        assertThat(sessionId1.getValue()).isNotEqualTo(sessionId2.getValue());
    }

    @Test
    @DisplayName("null UUID로 생성 시 예외 발생")
    void createSessionIdWithNullUUID() {
        // When & Then
        assertThatThrownBy(() -> SessionId.of((UUID) null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("SessionId UUID cannot be null");
    }

    @Test
    @DisplayName("null 문자열로 생성 시 예외 발생")
    void createSessionIdWithNullString() {
        // When & Then
        assertThatThrownBy(() -> SessionId.of((String) null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("SessionId string cannot be null or empty");
    }

    @Test
    @DisplayName("빈 문자열로 생성 시 예외 발생")
    void createSessionIdWithEmptyString() {
        // When & Then
        assertThatThrownBy(() -> SessionId.of(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("SessionId string cannot be null or empty");
    }

    @Test
    @DisplayName("공백만 있는 문자열로 생성 시 예외 발생")
    void createSessionIdWithBlankString() {
        // When & Then
        assertThatThrownBy(() -> SessionId.of("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("SessionId string cannot be null or empty");
    }

    @Test
    @DisplayName("잘못된 UUID 형식으로 생성 시 예외 발생")
    void createSessionIdWithInvalidUUIDFormat() {
        // Given
        String invalidUuidString = "invalid-uuid-format";
        
        // When & Then
        assertThatThrownBy(() -> SessionId.of(invalidUuidString))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid UUID format: " + invalidUuidString);
    }

    @Test
    @DisplayName("SessionId 동등성 테스트")
    void sessionIdEquality() {
        // Given
        UUID uuid = UUID.randomUUID();
        SessionId sessionId1 = SessionId.of(uuid);
        SessionId sessionId2 = SessionId.of(uuid);
        SessionId sessionId3 = SessionId.of(UUID.randomUUID());
        
        // When & Then
        assertThat(sessionId1).isEqualTo(sessionId2);
        assertThat(sessionId1).isNotEqualTo(sessionId3);
        assertThat(sessionId1.hashCode()).isEqualTo(sessionId2.hashCode());
    }

    @Test
    @DisplayName("SessionId toString 테스트")
    void sessionIdToString() {
        // Given
        UUID uuid = UUID.randomUUID();
        SessionId sessionId = SessionId.of(uuid);
        
        // When
        String result = sessionId.toString();
        
        // Then
        assertThat(result).isEqualTo(uuid.toString());
    }
}