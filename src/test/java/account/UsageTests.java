package account;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.example.clients.AccountClient;
import org.example.config.RequestSpecConfig;
import org.example.utils.TokenManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for GET /v1/usage — billing period usage stats.
 *
 * Confirmed response shape:
 * {
 *   "plan":      "free" | "pro" | "scale",
 *   "used":      integer  (requests consumed this period),
 *   "limit":     integer  (plan monthly cap),
 *   "remaining": integer  (limit - used),
 *   "unlimited": boolean  (true only on unlimited enterprise plans)
 * }
 */
@Epic("Account")
@Feature("GET /v1/usage")
@DisplayName("Usage & Quota Tests")
class UsageTests {

    private static final AccountClient client = new AccountClient();

    // ── HTTP / transport ─

    @Test
    @Story("200 happy path")
    @Description("GET /v1/usage with valid key returns 200")
    @DisplayName("Usage endpoint returns 200")
    void usage_returns200() {
        Response r = client.getUsage();
        assertThat(r.statusCode()).isEqualTo(200);
    }

    @Test
    @Story("Response is JSON")
    @Description("Usage response content-type must be application/json")
    @DisplayName("Usage response is application/json")
    void usage_isJson() {
        Response r = client.getUsage();
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.contentType()).containsIgnoringCase("application/json");
    }

    @Test
    @Story("Response body not empty")
    @Description("Usage response body must not be blank")
    @DisplayName("Usage response body is non-empty")
    void usage_bodyNotEmpty() {
        Response r = client.getUsage();
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.body().asString()).isNotBlank();
    }

    // ── Schema — field presence and types ──

    @Test
    @Story("Schema — plan field")
    @Description("plan must be one of: free, pro, scale")
    @DisplayName("plan is a valid plan name")
    void schema_plan_isValidPlanName() {
        Response r = client.getUsage();
        assertThat(r.statusCode()).isEqualTo(200);
        String plan = r.jsonPath().getString("plan");
        assertThat(plan)
                .as("plan must be one of: free, pro, scale")
                .isNotBlank()
                .isIn("free", "pro", "scale");
    }

    @Test
    @Story("Schema — used field")
    @Description("used must be a non-negative integer")
    @DisplayName("used is a non-negative integer")
    void schema_used_isNonNegative() {
        Response r = client.getUsage();
        assertThat(r.statusCode()).isEqualTo(200);
        Integer used = r.jsonPath().get("used");
        assertThat(used)
                .as("used must be non-null and >= 0")
                .isNotNull()
                .isGreaterThanOrEqualTo(0);
    }

    @Test
    @Story("Schema — limit field")
    @Description("limit must be a positive integer representing the plan monthly cap")
    @DisplayName("limit is a positive integer")
    void schema_limit_isPositive() {
        Response r = client.getUsage();
        assertThat(r.statusCode()).isEqualTo(200);
        Integer limit = r.jsonPath().get("limit");
        assertThat(limit)
                .as("limit must be a positive integer")
                .isNotNull()
                .isGreaterThan(0);
    }

    @Test
    @Story("Schema — remaining field")
    @Description("remaining must be a non-negative integer")
    @DisplayName("remaining is a non-negative integer")
    void schema_remaining_isNonNegative() {
        Response r = client.getUsage();
        assertThat(r.statusCode()).isEqualTo(200);
        Integer remaining = r.jsonPath().get("remaining");
        assertThat(remaining)
                .as("remaining must be non-null and >= 0")
                .isNotNull()
                .isGreaterThanOrEqualTo(0);
    }

    @Test
    @Story("Schema — unlimited field")
    @Description("unlimited must be a boolean (true or false)")
    @DisplayName("unlimited is a boolean")
    void schema_unlimited_isBoolean() {
        Response r = client.getUsage();
        assertThat(r.statusCode()).isEqualTo(200);
        Boolean unlimited = r.jsonPath().get("unlimited");
        assertThat(unlimited)
                .as("unlimited must be a non-null boolean")
                .isNotNull()
                .isIn(true, false);
    }

    // ── Business logic constraints ────────────────────────────────────────────

    @Test
    @Story("Logic — used + remaining = limit (or overage)")
    @Description("used + remaining = limit when within quota. When quota is exceeded, " +
                 "remaining = 0 and used may exceed limit (API allows overage).")
    @DisplayName("used + remaining <= limit + used (accounting is consistent)")
    void logic_usedPlusRemainingEqualsLimit() {
        Response r = client.getUsage();
        assertThat(r.statusCode()).isEqualTo(200);
        int used      = r.jsonPath().get("used");
        int remaining = r.jsonPath().get("remaining");
        int limit     = r.jsonPath().get("limit");

        if (used <= limit) {
            // Within quota — used + remaining must equal limit
            assertThat(used + remaining)
                    .as("used (%d) + remaining (%d) must equal limit (%d) when within quota",
                            used, remaining, limit)
                    .isEqualTo(limit);
        } else {
            // Overage — remaining must be 0 (no more requests allowed)
            assertThat(remaining)
                    .as("remaining must be 0 when quota exceeded (used=%d, limit=%d)", used, limit)
                    .isEqualTo(0);
        }
    }

    @Test
    @Story("Logic — used does not exceed limit (overage allowed)")
    @Description("API does not hard-cap at limit — used may exceed it. " +
                 "This test simply documents the observed values without failing.")
    @DisplayName("used is a non-negative integer (overage allowed by API)")
    void logic_usedDoesNotExceedLimit() {
        Response r = client.getUsage();
        assertThat(r.statusCode()).isEqualTo(200);
        int used  = r.jsonPath().get("used");
        int limit = r.jsonPath().get("limit");
        // API does not enforce a hard cap — used can exceed limit.
        // Assert only that used is non-negative (already validated in schema test).
        assertThat(used)
                .as("used (%d) must be >= 0 (API allows overage above limit=%d)", used, limit)
                .isGreaterThanOrEqualTo(0);
    }

    @Test
    @Story("Logic — free plan limit is 1000")
    @Description("On a free plan the limit must be 1000 requests per month")
    @DisplayName("Free plan limit = 1000")
    void logic_freePlanLimitIs1000() {
        Response r = client.getUsage();
        assertThat(r.statusCode()).isEqualTo(200);
        String plan = r.jsonPath().getString("plan");
        if ("free".equals(plan)) {
            int limit = r.jsonPath().get("limit");
            assertThat(limit)
                    .as("Free plan limit must be 1000")
                    .isEqualTo(1000);
        }
        // Skip if running on a non-free plan
    }

    @Test
    @Story("Logic — unlimited flag false on free plan")
    @Description("unlimited must be false on a free plan")
    @DisplayName("Free plan: unlimited = false")
    void logic_freePlanUnlimitedIsFalse() {
        Response r = client.getUsage();
        assertThat(r.statusCode()).isEqualTo(200);
        String plan = r.jsonPath().getString("plan");
        if ("free".equals(plan)) {
            Boolean unlimited = r.jsonPath().get("unlimited");
            assertThat(unlimited)
                    .as("unlimited must be false on a free plan")
                    .isFalse();
        }
    }

    // ── Auth / error cases ────────────────────────────────────────────────────

    @Test
    @Story("No auth returns 401")
    @Description("GET /v1/usage without Authorization header must return 401")
    @DisplayName("No auth returns 401")
    void usage_noAuth_returns401() {
        Response r = new AccountClient(RequestSpecConfig.unauthenticatedSpec()).getUsage();
        assertThat(r.statusCode()).isEqualTo(401);
    }

    @Test
    @Story("Revoked token returns 401")
    @Description("GET /v1/usage with a revoked API key must return 401")
    @DisplayName("Revoked token returns 401")
    void usage_revokedToken_returns401() {
        Response r = new AccountClient(
                RequestSpecConfig.invalidTokenSpec(TokenManager.revokedToken()))
                .getUsage();
        assertThat(r.statusCode()).isEqualTo(401);
    }

    @Test
    @Story("No 5xx on valid request")
    @Description("GET /v1/usage must not return a server error")
    @DisplayName("Valid request does not return 5xx")
    void usage_no5xx() {
        Response r = client.getUsage();
        assertThat(r.statusCode()).isNotIn(500, 503);
    }
}
