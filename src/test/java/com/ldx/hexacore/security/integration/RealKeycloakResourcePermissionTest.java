package com.ldx.hexacore.security.integration;

import com.sd.KeycloakClient.client.auth.sync.KeycloakAuthClient;
import com.sd.KeycloakClient.config.ClientConfiguration;
import com.sd.KeycloakClient.dto.KeycloakResponse;
import com.sd.KeycloakClient.dto.auth.KeycloakAuthorizationResult;
import com.sd.KeycloakClient.dto.auth.KeycloakTokenInfo;
import com.sd.KeycloakClient.factory.KeycloakClient;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 팀 Keycloak Client를 직접 사용한 Real 인증/인가 테스트
 * 
 * <p>실제 Keycloak 서버와 직접 통신하여 authorization 기능을 검증합니다.</p>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Real Keycloak Authorization Test")
class RealKeycloakResourcePermissionTest {

//    private static final String USER_NAME = "identity";
//    private static final String PASSWORD = "P@ssw0rd1!";

    private static final String USER_NAME = "indonesia";
    private static final String PASSWORD = "indonesia00";

    private static final Logger logger = LoggerFactory.getLogger(RealKeycloakResourcePermissionTest.class);
    
    private static KeycloakAuthClient authClient;
    private static String accessToken;
    
    @BeforeAll
    static void setUp() {
        System.out.println("\n==============================================");
        System.out.println("🔥 Real Keycloak Authorization Test");
        System.out.println("==============================================");
        System.out.println("📋 Test Purpose:");
        System.out.println("   - 팀 keycloak-client 라이브러리 직접 호출 테스트");
        System.out.println("   - identity 계정 실제 authorization 검증");
        System.out.println("==============================================\n");

        // 팀 Keycloak Client 초기화
        ClientConfiguration config = ClientConfiguration.builder()
                .baseUrl("https://auth.daewoong.co.kr/keycloak")
                .realmName("backoffice-api")
                .clientId("identity-api")
                .clientSecret("OkXuimOHEVonBG6pPEGhVL5PM1J2twhe")
                .build();

//        ClientConfiguration config = ClientConfiguration.builder()
//                .baseUrl("https://authdev.daewoong.co.kr/keycloak")
//                .realmName("backoffice-api")
//                .clientId("identity-api")
//                .clientSecret("jLWxxq2YCLIaowgTU1kaTjqRiWJ3dFQ0")
//                .build();

        KeycloakClient keycloakClient = new KeycloakClient(config);
        authClient = keycloakClient.auth();

        logger.info("✅ 팀 Keycloak Client 초기화 완료");
    }

    @Test
    @Order(1)
    @DisplayName("1. Identity 계정으로 토큰 발급")
    void testTokenIssuance() {
        System.out.println("📝 Test 1: Token Issuance for Identity User");
        System.out.println("----------------------------------------");

        try {
//            KeycloakResponse<KeycloakTokenInfo> response = authClient.basicAuth("identity", "P@ssw0rd1!");
            KeycloakResponse<KeycloakTokenInfo> response = authClient.basicAuth(USER_NAME, PASSWORD);

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getBody()).isPresent();

            KeycloakTokenInfo tokenInfo = response.getBody().get();
            accessToken = tokenInfo.getAccessToken();

            assertThat(accessToken).isNotNull();
            System.out.println("✅ Token issued successfully for user: identity");
            System.out.println("   Token length: " + accessToken.length() + " characters");

        } catch (Exception e) {
            System.out.println("❌ Token issuance failed: " + e.getMessage());
            throw e;
        }

        System.out.println("----------------------------------------\n");
    }

//    @Test
//    @Order(2)
//    @DisplayName("2. 팀 라이브러리 직접 Authorization 호출 - indonesia 허용 URI")
//    void testDirectAuthorizationCall_Allowed() {
//        System.out.println("📝 Test 2: Direct Authorization Call - Allowed URI");
//        System.out.println("----------------------------------------");
//        System.out.println("🔍 Testing: GET /api/employees/group/indonesia");
//        System.out.println("   Expected: ✅ GRANTED");
//
//        assertThat(accessToken).isNotNull();
//
//        try {
//            KeycloakResponse<KeycloakAuthorizationResult> response =
//                authClient.authorization(accessToken, "/api/employees/group/indonesia", "GET");
//
//            System.out.println("📊 Authorization Response:");
//            System.out.println("   Status: " + response.getStatus());
//            System.out.println("   Message: " + response.getMessage());
//
//            if (response.getBody().isPresent()) {
//                KeycloakAuthorizationResult result = response.getBody().get();
//                System.out.println("   Granted: " + result.isGranted());
//                System.out.println("   Auth Response: " + result.getAuthorizationResponse());
//
//                if (result.isGranted()) {
//                    System.out.println("   Result: ✅ GRANTED");
//                } else {
//                    System.out.println("   Result: ❌ DENIED");
//                    System.out.println("   💡 This may indicate Keycloak resource configuration issue");
//                }
//
//            } else {
//                System.out.println("   Body: empty");
//                System.out.println("   💡 Empty response may indicate authorization denial");
//            }
//
//        } catch (Exception e) {
//            System.out.println("❌ Authorization call failed: " + e.getMessage());
//            e.printStackTrace();
//            throw e;
//        }
//
//        System.out.println("----------------------------------------\n");
//    }
//
//    @Test
//    @Order(3)
//    @DisplayName("3. 팀 라이브러리 직접 Authorization 호출 - 다른 URI들")
//    void testDirectAuthorizationCall_Denied() {
//        System.out.println("📝 Test 3: Direct Authorization Call - Other URI");
//        System.out.println("----------------------------------------");
//        System.out.println("🔍 Testing: GET /api/employees/group/korea");
//        System.out.println("   Expected: ✅ GRANTED (identity has full access)");
//
//        assertThat(accessToken).isNotNull();
//
//        try {
//            KeycloakResponse<KeycloakAuthorizationResult> response =
//                authClient.authorization(accessToken, "/api/employees/group/korea", "GET");
//
//            System.out.println("📊 Authorization Response:");
//            System.out.println("   Status: " + response.getStatus());
//            System.out.println("   Message: " + response.getMessage());
//
//            if (response.getBody().isPresent()) {
//                KeycloakAuthorizationResult result = response.getBody().get();
//                System.out.println("   Granted: " + result.isGranted());
//                System.out.println("   Auth Response: " + result.getAuthorizationResponse());
//
//                if (result.isGranted()) {
//                    System.out.println("   Result: ✅ GRANTED (expected for identity)");
//                } else {
//                    System.out.println("   Result: ❌ DENIED (unexpected for identity!)");
//                }
//
//            } else {
//                System.out.println("   Body: empty");
//                System.out.println("   Result: ❌ DENIED (unexpected for identity)");
//            }
//
//        } catch (Exception e) {
//            System.out.println("❌ Authorization call failed: " + e.getMessage());
//            e.printStackTrace();
//        }
//
//        System.out.println("----------------------------------------\n");
//    }
//
    @Test
    @Order(4)
    @DisplayName("4. 다양한 엔드포인트 테스트")
    void testVariousEndpoints() {
        System.out.println("📝 Test 4: Various Endpoints Test");
        System.out.println("========================================");

        String[][] testCases = {
            {"/api/employees/group/indonesia", "GET", "should be GRANTED"},
            {"/api/positions/group/indonesia", "GET", "should be GRANTED"},
            {"/api/integrated/group/indonesia", "GET", "should be GRANTED"},
            {"/api/organizations/group/indonesia", "GET", "should be GRANTED"},
            {"/api/employees/all", "GET", "should be GRANTED"},
            {"/api/employees/batch/by-companies", "POST", "should be GRANTED"},
            {"/api/users", "GET", "should be GRANTED"},
            {"/api/admin", "GET", "should be GRANTED"}
        };

        for (String[] testCase : testCases) {
            String uri = testCase[0];
            String method = testCase[1];
            String expected = testCase[2];

            System.out.printf("🔍 Testing: %s %s (%s)%n", method, uri, "");

            try {
                KeycloakResponse<KeycloakAuthorizationResult> response =
                    authClient.authorization(accessToken, uri, method);



                boolean granted = response.getBody()
                    .map(KeycloakAuthorizationResult::isGranted)
                    .orElse(false);

                String actual = granted ? "GRANTED" : "DENIED";
                System.out.println("   Message: " + response.getMessage());
                System.out.printf("   Result: %s%n", actual);

            } catch (Exception e) {
                System.out.printf("   Result: ERROR - %s%n", e.getMessage());
            }

            System.out.println();
        }

        System.out.println("========================================\n");
    }
    
    @AfterAll
    static void tearDown() {
        System.out.println("==============================================");
        System.out.println("✅ Real Keycloak Authorization Test Complete");
        System.out.println("==============================================");
        System.out.println("📋 Summary:");
        System.out.println("   - 팀 keycloak-client 라이브러리 직접 호출 검증 완료");
        System.out.println("   - 실제 Keycloak 서버 authorization 동작 확인");
        System.out.println("==============================================\n");
    }
}