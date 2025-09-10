package com.ldx.hexacore.security.integration;

import com.ldx.hexacore.security.auth.adapter.outbound.token.keycloak.KeycloakTokenProvider;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenValidationContext;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenValidationResult;
import com.ldx.hexacore.security.auth.domain.vo.Credentials;
import com.ldx.hexacore.security.auth.domain.vo.Token;
import com.ldx.hexacore.security.config.properties.HexacoreSecurityProperties;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Indonesia 계정의 허용 리스트 기반 리소스 권한 체크 테스트
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Indonesia 계정 허용 리스트 검증 테스트")
class IndonesiaAllowListTest {
    
    private static TokenProvider tokenProvider;
    private static String accessToken;
    
    @BeforeAll
    static void setUp() {
        System.out.println("==============================================");
        System.out.println("🚀 Indonesia Account Allow List Test");
        System.out.println("==============================================");
        
        // Keycloak 설정
        HexacoreSecurityProperties.TokenProvider.KeycloakProperties keycloakProps = 
            new HexacoreSecurityProperties.TokenProvider.KeycloakProperties();
        keycloakProps.setEnabled(true);
        keycloakProps.setServerUrl("https://auth.daewoong.co.kr/keycloak/");
        keycloakProps.setRealm("backoffice-api");
        keycloakProps.setClientId("identity-api");
        keycloakProps.setClientSecret("OkXuimOHEVonBG6pPEGhVL5PM1J2twhe");
        keycloakProps.setGrantType("password");
        keycloakProps.setScopes("openid profile email");
        keycloakProps.setPublicClient(false);
        
        tokenProvider = new KeycloakTokenProvider(keycloakProps);
        
        System.out.println("✅ Keycloak TokenProvider initialized for Indonesia test");
        System.out.println("==============================================\n");
    }
    
    @Test
    @Order(1)
    @DisplayName("1. Indonesia 계정 토큰 발급")
    void testTokenIssuance() {
        System.out.println("📝 Test 1: Token Issuance for Indonesia");
        System.out.println("----------------------------------------");
        
        Credentials credentials = Credentials.of("indonesia", "indonesia00");
        System.out.println("🔑 Issuing token for: indonesia");
        
        Token token = tokenProvider.issueToken(credentials);
        
        assertThat(token).isNotNull();
        assertThat(token.getAccessToken()).isNotBlank();
        
        accessToken = token.getAccessToken();
        
        System.out.println("✅ Token issued successfully!");
        System.out.println("----------------------------------------\n");
    }
    
    @Test
    @Order(2)
    @DisplayName("2. 허용 리스트 URL 테스트")
    void testAllowedUrls() {
        System.out.println("📝 Test 2: Allowed URLs for Indonesia");
        System.out.println("========================================");
        
        String[] allowedUrls = {
            "/api/employees/group/indonesia",
            "/api/positions/group/indonesia", 
            "/api/integrated/group/indonesia",
            "/api/organizations/group/indonesia"
        };
        
        System.out.println("Testing " + allowedUrls.length + " ALLOWED URLs...\n");
        
        for (String uri : allowedUrls) {
            System.out.println("🔍 Testing ALLOWED: GET " + uri);
            
            TokenValidationContext context = TokenValidationContext.builder()
                    .requestUri(uri)
                    .httpMethod("GET")
                    .checkResourcePermission(true)
                    .build();
            
            TokenValidationResult result = tokenProvider.validateTokenWithContext(accessToken, context);
            
            if (result.valid()) {
                System.out.println("   ✅ GRANTED - Access allowed");
            } else {
                System.out.println("   ❌ DENIED - Unexpected denial for allowed URL!");
                System.out.println("   - Reason: " + result.claims().get("error"));
            }
            System.out.println();
        }
        
        System.out.println("========================================\n");
    }
    
    @Test
    @Order(3)
    @DisplayName("3. 비허용 URL 테스트 - /api/employees/2022015")
    void testDeniedUrl_EmployeesById() {
        System.out.println("📝 Test 3: DENIED URL - /api/employees/2022015");
        System.out.println("----------------------------------------");
        
        String deniedUri = "/api/employees/2022015";
        
        System.out.println("🔍 Testing DENIED: GET " + deniedUri);
        System.out.println("   Expected: This should be DENIED (not in allow list)");
        
        TokenValidationContext context = TokenValidationContext.builder()
                .requestUri(deniedUri)
                .httpMethod("GET")
                .checkResourcePermission(true)
                .build();
        
        TokenValidationResult result = tokenProvider.validateTokenWithContext(accessToken, context);
        
        if (result.valid()) {
            System.out.println("   ⚠️ GRANTED - Unexpected access allowed!");
            System.out.println("   - This might indicate Keycloak configuration issue");
        } else {
            System.out.println("   ✅ DENIED - Correctly blocked access");
            System.out.println("   - Reason: " + result.claims().get("resource_permission_denied"));
        }
        
        System.out.println("----------------------------------------\n");
    }
    
    @Test
    @Order(4)
    @DisplayName("4. 다른 그룹 URL 테스트 - /api/employees/group/korea")
    void testDeniedUrl_DifferentGroup() {
        System.out.println("📝 Test 4: DENIED URL - Different Group");
        System.out.println("----------------------------------------");
        
        String deniedUri = "/api/employees/group/korea";
        
        System.out.println("🔍 Testing DENIED: GET " + deniedUri);
        System.out.println("   Expected: This should be DENIED (indonesia only)");
        
        TokenValidationContext context = TokenValidationContext.builder()
                .requestUri(deniedUri)
                .httpMethod("GET")
                .checkResourcePermission(true)
                .build();
        
        TokenValidationResult result = tokenProvider.validateTokenWithContext(accessToken, context);
        
        if (result.valid()) {
            System.out.println("   ⚠️ GRANTED - Unexpected access allowed!");
        } else {
            System.out.println("   ✅ DENIED - Correctly blocked different group");
        }
        
        System.out.println("----------------------------------------\n");
    }
    
    @Test
    @Order(5)
    @DisplayName("5. 다양한 HTTP 메소드 테스트")
    void testDifferentHttpMethods() {
        System.out.println("📝 Test 5: Different HTTP Methods");
        System.out.println("========================================");
        
        String allowedUri = "/api/employees/group/indonesia";
        String[] methods = {"GET", "POST", "PUT", "DELETE"};
        
        for (String method : methods) {
            System.out.println("🔍 Testing: " + method + " " + allowedUri);
            
            TokenValidationContext context = TokenValidationContext.builder()
                    .requestUri(allowedUri)
                    .httpMethod(method)
                    .checkResourcePermission(true)
                    .build();
            
            try {
                TokenValidationResult result = tokenProvider.validateTokenWithContext(accessToken, context);
                
                if (result.valid()) {
                    System.out.println("   ✅ GRANTED - " + method + " access allowed");
                } else {
                    System.out.println("   ❌ DENIED - " + method + " access denied");
                }
            } catch (Exception e) {
                System.out.println("   ⚠️ ERROR - " + e.getMessage());
            }
            
            System.out.println();
        }
        
        System.out.println("========================================");
    }
    
    @AfterAll
    static void tearDown() {
        System.out.println("\n==============================================");
        System.out.println("🏁 Indonesia Allow List Test Completed!");
        System.out.println("==============================================");
    }
}