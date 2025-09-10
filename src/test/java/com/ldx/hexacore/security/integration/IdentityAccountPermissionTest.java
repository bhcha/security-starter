package com.ldx.hexacore.security.integration;

import com.ldx.hexacore.security.auth.adapter.outbound.token.keycloak.KeycloakTokenProvider;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenProvider;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenValidationContext;
import com.ldx.hexacore.security.auth.application.command.port.out.TokenValidationResult;
import com.ldx.hexacore.security.auth.domain.vo.Credentials;
import com.ldx.hexacore.security.auth.domain.vo.Token;
import com.ldx.hexacore.security.config.properties.HexacoreSecurityProperties;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Identity 계정 권한 테스트
 * 
 * <p>identity/P@ssw0rd1! 계정이 모든 엔드포인트에 접근 권한을 가지고 있는지 검증합니다.</p>
 * 
 * <p>외부 Keycloak 서버에 의존하는 통합 테스트이므로 integration 프로파일에서만 실행됩니다.</p>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Identity Account Permission Test")
@org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "test.integration", matches = "true")
class IdentityAccountPermissionTest {
    
    private static final Logger logger = LoggerFactory.getLogger(IdentityAccountPermissionTest.class);
    
    private static TokenProvider tokenProvider;
    private static String accessToken;
    
    @BeforeAll
    static void setUp() {
        System.out.println("\n==============================================");
        System.out.println("🔑 Identity Account Permission Test");
        System.out.println("==============================================");
        System.out.println("📋 Test Purpose:");
        System.out.println("   - identity/P@ssw0rd1! 계정 권한 검증");
        System.out.println("   - 모든 엔드포인트 접근 권한 확인");
        System.out.println("   - 관리자 권한 테스트");
        System.out.println("==============================================\n");
        
        // Keycloak 설정
        HexacoreSecurityProperties.TokenProvider.KeycloakProperties keycloakProps = 
            new HexacoreSecurityProperties.TokenProvider.KeycloakProperties();
        keycloakProps.setEnabled(true);
        keycloakProps.setServerUrl("https://auth.daewoong.co.kr/keycloak");
        keycloakProps.setRealm("backoffice-api");
        keycloakProps.setClientId("identity-api");
        keycloakProps.setClientSecret("OkXuimOHEVonBG6pPEGhVL5PM1J2twhe");
        keycloakProps.setGrantType("password");
        keycloakProps.setScopes("openid profile email");
        keycloakProps.setPublicClient(false);
        
        tokenProvider = new KeycloakTokenProvider(keycloakProps);
        
        logger.info("✅ Identity Account 테스트용 TokenProvider 초기화 완료");
    }
    
    @Test
    @Order(1)
    @DisplayName("1. Identity 계정으로 토큰 발급")
    void testTokenIssuance() {
        System.out.println("📝 Test 1: Token Issuance for Identity User");
        System.out.println("----------------------------------------");
        
        try {
            Credentials credentials = Credentials.of("identity", "P@ssw0rd1!");
            System.out.println("🔑 Attempting login with username: identity");
            
            Token token = tokenProvider.issueToken(credentials);
            
            assertThat(token).isNotNull();
            assertThat(token.getAccessToken()).isNotBlank();
            assertThat(token.getRefreshToken()).isNotBlank();
            assertThat(token.getExpiresIn()).isGreaterThan(0);
            
            accessToken = token.getAccessToken();
            
            System.out.println("✅ Token issued successfully for user: identity");
            System.out.println("   Token length: " + accessToken.length() + " characters");
            
        } catch (Exception e) {
            System.out.println("❌ Token issuance failed: " + e.getMessage());
            throw e;
        }
        
        System.out.println("----------------------------------------\n");
    }
    
    @Test
    @Order(2)
    @DisplayName("2. Indonesia 그룹 엔드포인트 접근 테스트")
    void testIndonesiaGroupEndpoints() {
        System.out.println("📝 Test 2: Indonesia Group Endpoints Access");
        System.out.println("========================================");
        
        String[] indonesiaEndpoints = {
            "/api/employees/group/indonesia",
            "/api/positions/group/indonesia", 
            "/api/integrated/group/indonesia",
            "/api/organizations/group/indonesia"
        };
        
        for (String endpoint : indonesiaEndpoints) {
            System.out.printf("🔍 Testing: GET %s%n", endpoint);
            
            TokenValidationContext context = TokenValidationContext.builder()
                    .requestUri(endpoint)
                    .httpMethod("GET")
                    .checkResourcePermission(true)
                    .build();
            
            try {
                TokenValidationResult result = tokenProvider.validateTokenWithContext(accessToken, context);
                
                if (result.valid()) {
                    System.out.printf("   ✅ GRANTED - Access allowed%n");
                    assertThat(result.valid()).isTrue();
                } else {
                    System.out.printf("   ❌ DENIED - No permission for this resource%n");
                    System.out.printf("   💡 Expected GRANTED but got DENIED%n");
                    throw new AssertionError("Expected access to be granted for " + endpoint);
                }
                
            } catch (Exception e) {
                System.out.printf("   ⚠️ ERROR - %s%n", e.getMessage());
                throw e;
            }
            
            System.out.println();
        }
        
        System.out.println("========================================\n");
    }
    
    @Test
    @Order(3)
    @DisplayName("3. 다른 그룹 엔드포인트 접근 테스트")
    void testOtherGroupEndpoints() {
        System.out.println("📝 Test 3: Other Group Endpoints Access");
        System.out.println("========================================");
        
        String[] otherEndpoints = {
            "/api/employees/group/korea",
            "/api/employees/group/japan",
            "/api/employees/group/singapore"
        };
        
        for (String endpoint : otherEndpoints) {
            System.out.printf("🔍 Testing: GET %s%n", endpoint);
            
            TokenValidationContext context = TokenValidationContext.builder()
                    .requestUri(endpoint)
                    .httpMethod("GET")
                    .checkResourcePermission(true)
                    .build();
            
            try {
                TokenValidationResult result = tokenProvider.validateTokenWithContext(accessToken, context);
                
                if (result.valid()) {
                    System.out.printf("   ✅ GRANTED - Access allowed%n");
                    assertThat(result.valid()).isTrue();
                } else {
                    System.out.printf("   ❌ DENIED - No permission for this resource%n");
                    System.out.printf("   💡 Expected GRANTED but got DENIED%n");
                    throw new AssertionError("Expected access to be granted for " + endpoint);
                }
                
            } catch (Exception e) {
                System.out.printf("   ⚠️ ERROR - %s%n", e.getMessage());
                throw e;
            }
            
            System.out.println();
        }
        
        System.out.println("========================================\n");
    }
    
    @Test
    @Order(4)
    @DisplayName("4. 관리자 엔드포인트 접근 테스트")
    void testAdminEndpoints() {
        System.out.println("📝 Test 4: Admin Endpoints Access");
        System.out.println("========================================");
        
        String[] adminEndpoints = {
            "/api/admin",
            "/api/admin/users",
            "/api/admin/settings",
            "/api/users",
            "/api/products"
        };
        
        for (String endpoint : adminEndpoints) {
            System.out.printf("🔍 Testing: GET %s%n", endpoint);
            
            TokenValidationContext context = TokenValidationContext.builder()
                    .requestUri(endpoint)
                    .httpMethod("GET")
                    .checkResourcePermission(true)
                    .build();
            
            try {
                TokenValidationResult result = tokenProvider.validateTokenWithContext(accessToken, context);
                
                if (result.valid()) {
                    System.out.printf("   ✅ GRANTED - Access allowed%n");
                    assertThat(result.valid()).isTrue();
                } else {
                    System.out.printf("   ❌ DENIED - No permission for this resource%n");
                    System.out.printf("   💡 Expected GRANTED but got DENIED%n");
                    throw new AssertionError("Expected access to be granted for " + endpoint);
                }
                
            } catch (Exception e) {
                System.out.printf("   ⚠️ ERROR - %s%n", e.getMessage());
                throw e;
            }
            
            System.out.println();
        }
        
        System.out.println("========================================\n");
    }
    
    @Test
    @Order(5)
    @DisplayName("5. 다양한 HTTP 메서드 테스트")
    void testDifferentHttpMethods() {
        System.out.println("📝 Test 5: Different HTTP Methods");
        System.out.println("========================================");
        
        String endpoint = "/api/employees/group/indonesia";
        String[] methods = {"GET", "POST", "PUT", "DELETE", "PATCH"};
        
        for (String method : methods) {
            System.out.printf("🔍 Testing: %s %s%n", method, endpoint);
            
            TokenValidationContext context = TokenValidationContext.builder()
                    .requestUri(endpoint)
                    .httpMethod(method)
                    .checkResourcePermission(true)
                    .build();
            
            try {
                TokenValidationResult result = tokenProvider.validateTokenWithContext(accessToken, context);
                
                if (result.valid()) {
                    System.out.printf("   ✅ GRANTED - Access allowed%n");
                    assertThat(result.valid()).isTrue();
                } else {
                    System.out.printf("   ❌ DENIED - No permission for this resource%n");
                    System.out.printf("   💡 Expected GRANTED but got DENIED%n");
                    throw new AssertionError("Expected access to be granted for " + method + " " + endpoint);
                }
                
            } catch (Exception e) {
                System.out.printf("   ⚠️ ERROR - %s%n", e.getMessage());
                throw e;
            }
            
            System.out.println();
        }
        
        System.out.println("========================================\n");
    }
    
    @Test
    @Order(6)
    @DisplayName("6. 종합 권한 테스트")
    void testComprehensivePermissions() {
        System.out.println("📝 Test 6: Comprehensive Permissions Test");
        System.out.println("========================================");
        
        String[][] testCases = {
            // Indonesia 그룹
            {"/api/employees/group/indonesia", "GET", "SHOULD_BE_GRANTED"},
            {"/api/positions/group/indonesia", "GET", "SHOULD_BE_GRANTED"},
            {"/api/integrated/group/indonesia", "GET", "SHOULD_BE_GRANTED"},
            {"/api/organizations/group/indonesia", "GET", "SHOULD_BE_GRANTED"},
            
            // 다른 그룹들
            {"/api/employees/group/korea", "GET", "SHOULD_BE_GRANTED"},
            {"/api/employees/group/japan", "GET", "SHOULD_BE_GRANTED"},
            
            // 관리자 엔드포인트
            {"/api/admin", "GET", "SHOULD_BE_GRANTED"},
            {"/api/users", "GET", "SHOULD_BE_GRANTED"},
            {"/api/products", "GET", "SHOULD_BE_GRANTED"}
        };
        
        int passed = 0;
        int failed = 0;
        
        for (String[] testCase : testCases) {
            String uri = testCase[0];
            String method = testCase[1];
            String expected = testCase[2];
            
            TokenValidationContext context = TokenValidationContext.builder()
                    .requestUri(uri)
                    .httpMethod(method)
                    .checkResourcePermission(true)
                    .build();
            
            try {
                TokenValidationResult result = tokenProvider.validateTokenWithContext(accessToken, context);
                
                boolean granted = result.valid();
                String actual = granted ? "GRANTED" : "DENIED";
                boolean success = (expected.equals("SHOULD_BE_GRANTED") && granted);
                
                System.out.printf("%-40s Expected: GRANTED  Actual: %s  %s%n", 
                    uri, actual, success ? "✓ PASS" : "✗ FAIL");
                
                if (success) {
                    passed++;
                } else {
                    failed++;
                    // identity 계정은 모든 권한이 있어야 하므로 실패 시 에러
                    throw new AssertionError("Identity account should have access to " + uri);
                }
                
            } catch (Exception e) {
                System.out.printf("%-40s Expected: GRANTED  Actual: ERROR  ✗ FAIL%n", uri);
                System.out.println("   Error: " + e.getMessage());
                failed++;
                throw e;
            }
        }
        
        System.out.println("========================================");
        System.out.printf("Test Results: %d PASSED, %d FAILED%n", passed, failed);
        System.out.println("========================================\n");
        
        assertThat(failed).as("All tests should pass for identity account").isEqualTo(0);
    }
    
    @AfterAll
    static void tearDown() {
        System.out.println("==============================================");
        System.out.println("✅ Identity Account Permission Test Complete");
        System.out.println("==============================================");
        System.out.println("📋 Summary:");
        System.out.println("   - identity 계정 모든 엔드포인트 접근 권한 검증 완료");
        System.out.println("   - 팀 keycloak-client 라이브러리 정상 동작 확인");
        System.out.println("   - 관리자 권한 정상 동작 확인");
        System.out.println("==============================================\n");
    }
}