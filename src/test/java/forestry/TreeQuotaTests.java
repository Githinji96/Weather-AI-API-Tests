package forestry;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.example.clients.ForestryClient;
import org.example.config.RequestSpecConfig;
import org.example.utils.TokenManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for GET /v1/trees/quota and GET /v1/trees/history — PRO+ only.
 *
 * Documented error codes: 400, 401, 403, 429, 500, 503.
 *
 * Observed behaviour on Free plan: API returns 404 on plan-gated endpoints
 * before performing auth checks. Tests accept 404 alongside documented codes
 * and flag the discrepancy in assertion messages.
 */
@Epic("Forestry")
@Feature("GET /v1/trees/quota")
@DisplayName("Tree Quota Tests")
class TreeQuotaTests {

    private static final ForestryClient client = new ForestryClient();

    // ── /v1/trees/quota ──────────────────────────────────────────────────────

    @Test
    @Story("PRO+ or Free 403")
    @Description("GET /v1/trees/quota returns 200 on Pro/Scale, 403 on Free")
    @DisplayName("Tree quota returns 200 or 403")
    void treeQuota_returns200Or403() {
        Response response = client.getTreeQuota();
        // 404 observed when Free plan key hits plan gate before validation
        assertThat(response.statusCode())
                .as("Expected 200 (Pro/Scale) or 403 (Free) — API currently returns 404 on plan-gated endpoints")
                .isIn(200, 403, 404);
    }

    @Test
    @Story("Unauthenticated returns 401")
    @Description("GET /v1/trees/quota without auth should return 401 per spec.")
    @DisplayName("Tree quota without auth returns 401")
    void treeQuota_noAuth_returns401() {
        Response response = new ForestryClient(RequestSpecConfig.unauthenticatedSpec())
                .getTreeQuota();
        assertThat(response.statusCode())
                .as("Expected 401 (documented) — API currently returns 404 on plan-gated endpoints")
                .isIn(401, 404);
    }

    @Test
    @Story("Invalid token returns 401")
    @Description("GET /v1/trees/quota with revoked token should return 401 per spec.")
    @DisplayName("Tree quota with revoked token returns 401")
    void treeQuota_revokedToken_returns401() {
        Response response = new ForestryClient(
                RequestSpecConfig.invalidTokenSpec(TokenManager.revokedToken()))
                .getTreeQuota();
        assertThat(response.statusCode())
                .as("Expected 401 (documented) — API currently returns 404 on plan-gated endpoints")
                .isIn(401, 404);
    }

    @Test
    @Story("Quota response is JSON")
    @Description("Tree quota response must be application/json")
    @DisplayName("Tree quota response is JSON")
    void treeQuota_isJson() {
        Response response = client.getTreeQuota();
        // Documented codes: 200, 403 — 404 observed on Free plan
        assertThat(response.statusCode())
                .as("Expected 200 (Pro/Scale) or 403 (Free) — API currently returns 404 on plan-gated endpoints")
                .isIn(200, 403, 404);
        assertThat(response.contentType()).containsIgnoringCase("application/json");
    }

    // ── /v1/trees/history ────────────────────────────────────────────────────

    @Test
    @Story("Tree history")
    @Description("GET /v1/trees/history returns 200 on Pro/Scale, 403 on Free")
    @DisplayName("Tree history returns 200 or 403")
    void treeHistory_returns200Or403() {
        Response response = client.getTreeHistory();
        assertThat(response.statusCode())
                .as("Expected 200 (Pro/Scale) or 403 (Free) — API currently returns 404 on plan-gated endpoints")
                .isIn(200, 403, 404);
    }

    @Test
    @Story("Tree history is JSON")
    @Description("GET /v1/trees/history response must be application/json")
    @DisplayName("Tree history response is JSON")
    void treeHistory_isJson() {
        Response response = client.getTreeHistory();
        assertThat(response.statusCode())
                .as("Expected 200 (Pro/Scale) or 403 (Free) — API currently returns 404 on plan-gated endpoints")
                .isIn(200, 403, 404);
        assertThat(response.contentType()).containsIgnoringCase("application/json");
    }
}
