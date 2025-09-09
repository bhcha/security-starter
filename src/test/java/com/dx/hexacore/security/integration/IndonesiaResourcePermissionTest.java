package com.dx.hexacore.security.integration;

import com.dx.hexacore.security.auth.adapter.outbound.token.keycloak.KeycloakTokenProvider;
import com.dx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.dx.hexacore.security.auth.application.command.port.out.TokenValidationContext;
import com.dx.hexacore.security.auth.application.command.port.out.TokenValidationResult;
import com.dx.hexacore.security.auth.domain.vo.Credentials;
import com.dx.hexacore.security.auth.domain.vo.Token;
import com.dx.hexacore.security.config.properties.HexacoreSecurityProperties;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Indonesia 리소스 권한 테스트
 * 
 * Keycloak 설정:
 * - Resource: indonesia (URI: /api/employees/group/indonesia)
 * - Policy: onlyindonesia (indonesia 계정만 positive)
 * - Permission: indonesia 리소스 + onlyindonesia 정책
 * 
 * 예상 동작:
 * - indonesia 계정으로 /api/employees/group/indonesia 접근: ✅ 허용
 * - indonesia 계정으로 다른 엔드포인트 접근: ❌ 차단
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Indonesia 리소스 전용 권한 테스트")
class IndonesiaResourcePermissionTest {
    
    private static TokenProvider tokenProvider;
    private static String accessToken;
    
    @BeforeAll
    static void setUp() {
        System.out.println("\n==============================================");
        System.out.println("🚀 Indonesia Resource Permission Test");
        System.out.println("==============================================");
        System.out.println("📋 Test Scenario:");
        System.out.println("   - ONLY /api/employees/group/indonesia is allowed");
        System.out.println("   - ALL other endpoints should be DENIED");
        System.out.println("==============================================\n");
        
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
    }
    
    @Test
    @Order(1)
    @DisplayName("1. Indonesia 계정으로 토큰 발급")
    void testTokenIssuance() {
        System.out.println("📝 Test 1: Token Issuance for Indonesia User");
        System.out.println("----------------------------------------");
        
        // Given
        Credentials credentials = Credentials.of("indonesia", "indonesia00");
        
        // When
        Token token = tokenProvider.issueToken(credentials);
        accessToken = token.getAccessToken();
        
        // Then
        assertThat(token).isNotNull();
        assertThat(accessToken).isNotBlank();
        
        System.out.println("✅ Token issued successfully for user: indonesia");
        System.out.println("----------------------------------------\n");
    }
    
    @Test
    @Order(2)
    @DisplayName("2. 허용된 엔드포인트 접근 - /api/employees/group/indonesia")
    void testAllowedEndpoint() {
        System.out.println("📝 Test 2: Access to ALLOWED Endpoint");
        System.out.println("----------------------------------------");
        
        // Given
        String allowedUri = "/api/employees/group/indonesia";
        TokenValidationContext context = TokenValidationContext.builder()
                .requestUri(allowedUri)
                .httpMethod("GET")
                .checkResourcePermission(true)
                .build();
        
        System.out.println("🔐 Checking: GET " + allowedUri);
        System.out.println("   Expected: ✅ GRANTED");
        
        // When
        TokenValidationResult result = tokenProvider.validateTokenWithContext(accessToken, context);
        
        // Then
        System.out.println("   Result: " + (result.valid() ? "✅ GRANTED" : "❌ DENIED"));
        
        // 이 엔드포인트는 허용되어야 함
        assertThat(result.valid()).isTrue();
        
        System.out.println("----------------------------------------\n");
    }
    
    @Test
    @Order(3)
    @DisplayName("3. 차단된 엔드포인트 접근 - /api/users")
    void testDeniedEndpoint_Users() {
        System.out.println("📝 Test 3: Access to DENIED Endpoint - /api/users");
        System.out.println("----------------------------------------");
        
        // Given
        String deniedUri = "/api/users";
        TokenValidationContext context = TokenValidationContext.builder()
                .requestUri(deniedUri)
                .httpMethod("GET")
                .checkResourcePermission(true)
                .build();
        
        System.out.println("🔐 Checking: GET " + deniedUri);
        System.out.println("   Expected: ❌ DENIED");
        
        // When
        TokenValidationResult result = tokenProvider.validateTokenWithContext(accessToken, context);
        
        // Then
        System.out.println("   Result: " + (result.valid() ? "✅ GRANTED" : "❌ DENIED"));
        
        // 이 엔드포인트는 차단되어야 함
        assertThat(result.valid()).isFalse();
        
        System.out.println("----------------------------------------\n");
    }
    
    @Test
    @Order(4)
    @DisplayName("4. 차단된 엔드포인트 접근 - /api/employees/group/korea")
    void testDeniedEndpoint_OtherGroup() {
        System.out.println("📝 Test 4: Access to DENIED Endpoint - Similar Path");
        System.out.println("----------------------------------------");
        
        // Given
        String deniedUri = "/api/employees/group/korea";
        TokenValidationContext context = TokenValidationContext.builder()
                .requestUri(deniedUri)
                .httpMethod("GET")
                .checkResourcePermission(true)
                .build();
        
        System.out.println("🔐 Checking: GET " + deniedUri);
        System.out.println("   Expected: ❌ DENIED");
        
        // When
        TokenValidationResult result = tokenProvider.validateTokenWithContext(accessToken, context);
        
        // Then
        System.out.println("   Result: " + (result.valid() ? "✅ GRANTED" : "❌ DENIED"));
        
        // 비슷한 경로지만 정확히 일치하지 않으므로 차단되어야 함
        assertThat(result.valid()).isFalse();
        
        System.out.println("----------------------------------------\n");
    }
    
    @Test
    @Order(5)
    @DisplayName("5. 여러 엔드포인트 권한 체크 종합")
    void testMultipleEndpoints() {
        System.out.println("📝 Test 5: Comprehensive Endpoint Test");
        System.out.println("========================================");
        
        // 테스트할 엔드포인트 목록
        String[][] endpoints = {
            // URI, Expected Result (true=allowed, false=denied)
            {"/api/employees/group/indonesia", "true"},     // ✅ Only this should be allowed
            {"/api/employees/group/korea", "false"},        // ❌
            {"/api/employees/group/japan", "false"},        // ❌
            {"/api/employees", "false"},                    // ❌
            {"/api/users", "false"},                        // ❌
            {"/api/admin", "false"},                        // ❌
            {"/api/products", "false"},                     // ❌
            {"/api/employees/group/indonesia/details", "false"}, // ❌ Different path
            {"/api/employees/group", "false"},              // ❌
            {"/", "false"}                                  // ❌
        };
        
        int passCount = 0;
        int failCount = 0;
        
        for (String[] endpoint : endpoints) {
            String uri = endpoint[0];
            boolean shouldBeAllowed = Boolean.parseBoolean(endpoint[1]);
            
            TokenValidationContext context = TokenValidationContext.builder()
                    .requestUri(uri)
                    .httpMethod("GET")
                    .checkResourcePermission(true)
                    .build();
            
            TokenValidationResult result = tokenProvider.validateTokenWithContext(accessToken, context);
            boolean isAllowed = result.valid();
            
            // 결과 출력
            String expectedIcon = shouldBeAllowed ? "✅" : "❌";
            String actualIcon = isAllowed ? "✅" : "❌";
            boolean testPassed = (isAllowed == shouldBeAllowed);
            
            System.out.printf("%-40s Expected: %s  Actual: %s  %s%n",
                uri, 
                expectedIcon,
                actualIcon,
                testPassed ? "✓ PASS" : "✗ FAIL"
            );
            
            if (testPassed) {
                passCount++;
            } else {
                failCount++;
            }
        }
        
        System.out.println("========================================");
        System.out.println("Test Results: " + passCount + " PASSED, " + failCount + " FAILED");
        
        // 모든 테스트가 통과해야 함
        assertThat(failCount).isZero();
        
        System.out.println("========================================\n");
    }
    
    @Test
    @Order(6)
    @DisplayName("6. 다른 HTTP 메소드로 indonesia 엔드포인트 접근")
    void testDifferentHttpMethods() {
        System.out.println("📝 Test 6: Different HTTP Methods on Indonesia Endpoint");
        System.out.println("----------------------------------------");
        
        String uri = "/api/employees/group/indonesia";
        String[] methods = {"GET", "POST", "PUT", "DELETE", "PATCH"};
        
        for (String method : methods) {
            TokenValidationContext context = TokenValidationContext.builder()
                    .requestUri(uri)
                    .httpMethod(method)
                    .checkResourcePermission(true)
                    .build();
            
            TokenValidationResult result = tokenProvider.validateTokenWithContext(accessToken, context);
            
            System.out.printf("   %6s %s: %s%n", 
                method, 
                uri, 
                result.valid() ? "✅ GRANTED" : "❌ DENIED"
            );
        }
        
        System.out.println("----------------------------------------\n");
    }
    
    @AfterAll
    static void tearDown() {
        System.out.println("==============================================");
        System.out.println("✅ Indonesia Resource Permission Test Complete");
        System.out.println("==============================================");
        System.out.println("📋 Summary:");
        System.out.println("   - Indonesia user can ONLY access:");
        System.out.println("     /api/employees/group/indonesia");
        System.out.println("   - All other endpoints are DENIED");
        System.out.println("==============================================\n");
    }
}