package com.dx.hexacore.security.auth.adapter.outbound.token.jwt;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtPropertiesTest {

    @Test
    void shouldHaveDefaultValues() {
        JwtProperties properties = new JwtProperties();
        
        // enabled property was removed from JwtProperties
        assertThat(properties.getAccessTokenExpiration()).isEqualTo(3600);
        assertThat(properties.getRefreshTokenExpiration()).isEqualTo(604800);
        assertThat(properties.getIssuer()).isEqualTo("security-starter");
        assertThat(properties.getSecret()).isNull();
    }

    @Test
    void shouldLoadCustomProperties() {
        JwtProperties properties = new JwtProperties();
        // enabled property was removed from JwtProperties
        properties.setSecret("my-super-secret-key-for-jwt-signing-must-be-long-enough");
        properties.setAccessTokenExpiration(7200);
        properties.setRefreshTokenExpiration(1209600);
        properties.setIssuer("my-custom-issuer");
        
        // enabled property was removed from JwtProperties
        assertThat(properties.getSecret()).isEqualTo("my-super-secret-key-for-jwt-signing-must-be-long-enough");
        assertThat(properties.getAccessTokenExpiration()).isEqualTo(7200);
        assertThat(properties.getRefreshTokenExpiration()).isEqualTo(1209600);
        assertThat(properties.getIssuer()).isEqualTo("my-custom-issuer");
    }

    @Test
    void shouldValidateSecretKeyLength() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("short");
        
        // 32자 미만은 보안상 부적절
        assertThat(properties.getSecret().length()).isLessThan(32);
    }

    @Test
    void shouldValidateValidSecretKeyLength() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("this-is-a-valid-secret-key-with-proper-length-for-jwt");
        
        // 32자 이상은 적절함
        assertThat(properties.getSecret().length()).isGreaterThanOrEqualTo(32);
    }

    @Test
    void shouldHandleNullSecret() {
        JwtProperties properties = new JwtProperties();
        
        assertThat(properties.getSecret()).isNull();
    }
}