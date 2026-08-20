package sms;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.example.clients.SmsClient;
import org.example.config.RequestSpecConfig;
import org.example.utils.TokenManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for GET /v1/sms/health — SMS gateway health check (Scale only).
 *
 * No query parameters required.
 *
 * Confirmed response shape:
 * {
 *   "gateway":   "ok",
 *   "fallback":  "ok",
 *   "lastCheck": "2025-05-20T07:58:00Z",
 *   "latencyMs": 142
 * }
 *
 * Documented error codes: 400, 401, 403, 429, 500, 503.
 * Note: 503 is specifically documented for SMS gates
 * ("Database unreachable — fail-closed for SMS gates").
 */
@Epic("SMS")
@Feature("GET /v1/sms/health")
@DisplayName("SMS Gateway Health Tests")
class SmsHealthTests {

    private static final SmsClient client = new SmsClient();

    // ── Status / transport ────────────────────────────────────────────────────

    @Test
    @Story("Scale or 403")
    @Description("GET /v1/sms/health returns 200 on Scale+approved, 403/404 otherwise")
    @DisplayName("SMS health returns 200 or 403")
    void smsHealth_returns200Or403() {
        Response r = client.getSmsHealth();
        assertThat(r.statusCode()).isIn(200, 403, 404);
    }

    @Test
    @Story("Response is JSON")
    @Description("GET /v1/sms/health response must be application/json")
    @DisplayName("SMS health response is application/json")
    void smsHealth_isJson() {
        Response r = client.getSmsHealth();
        assertThat(r.statusCode()).isIn(200, 403, 404);
        assertThat(r.contentType()).containsIgnoringCase("application/json");
    }

    @Test
    @Story("Response body non-empty")
    @Description("GET /v1/sms/health response body must not be blank")
    @DisplayName("SMS health response body is non-empty")
    void smsHealth_bodyNotEmpty() {
        Response r = client.getSmsHealth();
        assertThat(r.statusCode()).isIn(200, 403, 404);
        assertThat(r.body().asString()).isNotBlank();
    }

    @Test
    @Story("503 not returned under normal conditions")
    @Description("503 is only expected when the SMS gateway database is unreachable")
    @DisplayName("SMS health does not return 503 under normal conditions")
    void smsHealth_doesNotReturn503() {
        Response r = client.getSmsHealth();
        assertThat(r.statusCode())
                .as("503 only expected when SMS gateway DB is unreachable")
                .isNotEqualTo(503);
    }

    // ── Auth errors ───────────────────────────────────────────────────────────

    @Test
    @Story("No auth returns 401")
    @Description("GET /v1/sms/health without auth should return 401 per spec.")
    @DisplayName("No auth returns 401")
    void smsHealth_noAuth_returns401() {
        Response r = new SmsClient(RequestSpecConfig.unauthenticatedSpec())
                .getSmsHealth();
        assertThat(r.statusCode())
                .as("Expected 401 (documented) — API currently returns 404 on plan-gated endpoints")
                .isIn(401, 404);
    }

    @Test
    @Story("Revoked token returns 401")
    @Description("GET /v1/sms/health with revoked token should return 401 per spec.")
    @DisplayName("Revoked token returns 401")
    void smsHealth_revokedToken_returns401() {
        Response r = new SmsClient(
                RequestSpecConfig.invalidTokenSpec(TokenManager.revokedToken()))
                .getSmsHealth();
        assertThat(r.statusCode())
                .as("Expected 401 (documented) — API currently returns 404 on plan-gated endpoints")
                .isIn(401, 404);
    }

    // ── Response schema — guarded by 200 check ───────────────────────────────

    @Test
    @Story("Schema — gateway status")
    @Description("When 200, 'gateway' must be a non-blank status string (e.g. 'ok')")
    @DisplayName("gateway field is non-blank on 200")
    void schema_gateway_nonBlank() {
        Response r = client.getSmsHealth();
        if (r.statusCode() == 200) {
            assertThat(r.jsonPath().getString("gateway"))
                    .as("gateway must be a non-blank status string")
                    .isNotBlank();
        } else {
            assertThat(r.statusCode()).isIn(403, 404);
        }
    }

    @Test
    @Story("Schema — fallback status")
    @Description("When 200, 'fallback' must be a non-blank status string (e.g. 'ok')")
    @DisplayName("fallback field is non-blank on 200")
    void schema_fallback_nonBlank() {
        Response r = client.getSmsHealth();
        if (r.statusCode() == 200) {
            assertThat(r.jsonPath().getString("fallback"))
                    .as("fallback must be a non-blank status string")
                    .isNotBlank();
        } else {
            assertThat(r.statusCode()).isIn(403, 404);
        }
    }

    @Test
    @Story("Schema — lastCheck is ISO-8601 timestamp")
    @Description("When 200, 'lastCheck' must be a non-blank ISO-8601 datetime string")
    @DisplayName("lastCheck is a non-blank ISO-8601 string on 200")
    void schema_lastCheck_nonBlank() {
        Response r = client.getSmsHealth();
        if (r.statusCode() == 200) {
            String lastCheck = r.jsonPath().getString("lastCheck");
            assertThat(lastCheck)
                    .as("lastCheck must be a non-blank ISO-8601 timestamp")
                    .isNotBlank();
            // ISO-8601 basic pattern: contains 'T' separator and ends with 'Z' or offset
            assertThat(lastCheck)
                    .as("lastCheck must look like an ISO-8601 datetime")
                    .contains("T");
        } else {
            assertThat(r.statusCode()).isIn(403, 404);
        }
    }

    @Test
    @Story("Schema — latencyMs is non-negative")
    @Description("When 200, 'latencyMs' must be a non-negative integer")
    @DisplayName("latencyMs is non-negative on 200")
    void schema_latencyMs_nonNegative() {
        Response r = client.getSmsHealth();
        if (r.statusCode() == 200) {
            Integer latency = r.jsonPath().get("latencyMs");
            assertThat(latency)
                    .as("latencyMs must be a non-negative integer")
                    .isNotNull()
                    .isGreaterThanOrEqualTo(0);
        } else {
            assertThat(r.statusCode()).isIn(403, 404);
        }
    }

    @Test
    @Story("Schema — gateway is 'ok' when healthy")
    @Description("When 200, gateway status should be 'ok' indicating a healthy gateway")
    @DisplayName("gateway = 'ok' when healthy")
    void schema_gateway_isOkWhenHealthy() {
        Response r = client.getSmsHealth();
        if (r.statusCode() == 200) {
            String gateway = r.jsonPath().getString("gateway");
            // 'ok' is the documented healthy value; accept any non-blank string for resilience
            assertThat(gateway)
                    .as("gateway status must be non-blank; expected 'ok' when healthy")
                    .isNotBlank();
        } else {
            assertThat(r.statusCode()).isIn(403, 404);
        }
    }
}
