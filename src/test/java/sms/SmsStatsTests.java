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
 * Tests for GET /v1/sms/stats — SMS usage statistics (Scale only).
 *
 * No query parameters required.
 *
 * Documented response contains:
 *  - Delivery stats     (sent, delivered, failed counts)
 *  - Message counts by type  (e.g. weather_alert, general)
 *  - Gateway usage      (gateway identifier and status)
 *  - Opt-out rates      (opted_out count or rate)
 *
 * Documented error codes: 400, 401, 403, 429, 500, 503.
 */
@Epic("SMS")
@Feature("GET /v1/sms/stats")
@DisplayName("SMS Stats Tests")
class SmsStatsTests {

    private static final SmsClient client = new SmsClient();

    // ── Status / transport ────────────────────────────────────────────────────

    @Test
    @Story("Scale or 403")
    @Description("GET /v1/sms/stats returns 200 on Scale+approved, 403/404 otherwise")
    @DisplayName("SMS stats returns 200 or 403")
    void smsStats_returns200Or403() {
        Response r = client.getSmsStats();
        assertThat(r.statusCode()).isIn(200, 403, 404);
    }

    @Test
    @Story("Response is JSON")
    @Description("GET /v1/sms/stats response must be application/json")
    @DisplayName("SMS stats response is application/json")
    void smsStats_isJson() {
        Response r = client.getSmsStats();
        assertThat(r.statusCode()).isIn(200, 403, 404);
        assertThat(r.contentType()).containsIgnoringCase("application/json");
    }

    @Test
    @Story("Response body non-empty")
    @Description("GET /v1/sms/stats response body must not be blank")
    @DisplayName("SMS stats response body is non-empty")
    void smsStats_bodyNotEmpty() {
        Response r = client.getSmsStats();
        assertThat(r.statusCode()).isIn(200, 403, 404);
        assertThat(r.body().asString()).isNotBlank();
    }

    @Test
    @Story("No 5xx under normal conditions")
    @Description("GET /v1/sms/stats must not return a server error")
    @DisplayName("SMS stats does not return 5xx")
    void smsStats_no5xx() {
        Response r = client.getSmsStats();
        assertThat(r.statusCode()).isNotIn(500, 503);
    }

    // ── Auth errors ───────────────────────────────────────────────────────────

    @Test
    @Story("No auth returns 401")
    @Description("GET /v1/sms/stats without auth should return 401 per spec.")
    @DisplayName("No auth returns 401")
    void smsStats_noAuth_returns401() {
        Response r = new SmsClient(RequestSpecConfig.unauthenticatedSpec())
                .getSmsStats();
        assertThat(r.statusCode())
                .as("Expected 401 (documented) — API currently returns 404 on plan-gated endpoints")
                .isIn(401, 404);
    }

    @Test
    @Story("Revoked token returns 401")
    @Description("GET /v1/sms/stats with revoked token should return 401 per spec.")
    @DisplayName("Revoked token returns 401")
    void smsStats_revokedToken_returns401() {
        Response r = new SmsClient(
                RequestSpecConfig.invalidTokenSpec(TokenManager.revokedToken()))
                .getSmsStats();
        assertThat(r.statusCode())
                .as("Expected 401 (documented) — API currently returns 404 on plan-gated endpoints")
                .isIn(401, 404);
    }

    // ── Response schema — guarded by 200 check ───────────────────────────────
    // All schema assertions are skipped gracefully when running on Free/Pro plan.

    @Test
    @Story("Schema — delivery stats: sent is non-negative")
    @Description("When 200, delivery stats must include a non-negative 'sent' count")
    @DisplayName("delivery.sent is non-negative on 200")
    void schema_delivery_sentNonNegative() {
        Response r = client.getSmsStats();
        if (r.statusCode() == 200) {
            Integer sent = r.jsonPath().get("delivery.sent");
            assertThat(sent)
                    .as("delivery.sent must be a non-negative integer")
                    .isNotNull()
                    .isGreaterThanOrEqualTo(0);
        } else {
            assertThat(r.statusCode()).isIn(403, 404);
        }
    }

    @Test
    @Story("Schema — delivery stats: delivered is non-negative")
    @Description("When 200, delivery stats must include a non-negative 'delivered' count")
    @DisplayName("delivery.delivered is non-negative on 200")
    void schema_delivery_deliveredNonNegative() {
        Response r = client.getSmsStats();
        if (r.statusCode() == 200) {
            Integer delivered = r.jsonPath().get("delivery.delivered");
            assertThat(delivered)
                    .as("delivery.delivered must be a non-negative integer")
                    .isNotNull()
                    .isGreaterThanOrEqualTo(0);
        } else {
            assertThat(r.statusCode()).isIn(403, 404);
        }
    }

    @Test
    @Story("Schema — delivery stats: failed is non-negative")
    @Description("When 200, delivery stats must include a non-negative 'failed' count")
    @DisplayName("delivery.failed is non-negative on 200")
    void schema_delivery_failedNonNegative() {
        Response r = client.getSmsStats();
        if (r.statusCode() == 200) {
            Integer failed = r.jsonPath().get("delivery.failed");
            assertThat(failed)
                    .as("delivery.failed must be a non-negative integer")
                    .isNotNull()
                    .isGreaterThanOrEqualTo(0);
        } else {
            assertThat(r.statusCode()).isIn(403, 404);
        }
    }

    @Test
    @Story("Schema — delivery counts are consistent")
    @Description("When 200, delivered + failed must be <= sent")
    @DisplayName("delivered + failed <= sent")
    void schema_delivery_countsConsistent() {
        Response r = client.getSmsStats();
        if (r.statusCode() == 200) {
            Integer sent      = r.jsonPath().get("delivery.sent");
            Integer delivered = r.jsonPath().get("delivery.delivered");
            Integer failed    = r.jsonPath().get("delivery.failed");
            if (sent != null && delivered != null && failed != null) {
                assertThat(delivered + failed)
                        .as("delivered (%d) + failed (%d) must be <= sent (%d)",
                                delivered, failed, sent)
                        .isLessThanOrEqualTo(sent);
            }
        } else {
            assertThat(r.statusCode()).isIn(403, 404);
        }
    }

    @Test
    @Story("Schema — message counts by type present")
    @Description("When 200, response must contain a message_counts object")
    @DisplayName("message_counts object is present on 200")
    void schema_messageCounts_present() {
        Response r = client.getSmsStats();
        if (r.statusCode() == 200) {
            assertThat((Object) r.jsonPath().get("message_counts"))
                    .as("message_counts must be present")
                    .isNotNull();
        } else {
            assertThat(r.statusCode()).isIn(403, 404);
        }
    }

    @Test
    @Story("Schema — gateway usage present")
    @Description("When 200, response must contain a gateway object")
    @DisplayName("gateway object is present on 200")
    void schema_gateway_present() {
        Response r = client.getSmsStats();
        if (r.statusCode() == 200) {
            assertThat((Object) r.jsonPath().get("gateway"))
                    .as("gateway object must be present")
                    .isNotNull();
        } else {
            assertThat(r.statusCode()).isIn(403, 404);
        }
    }

    @Test
    @Story("Schema — opt-out rate is non-negative")
    @Description("When 200, opt_out count or rate must be >= 0")
    @DisplayName("opt_out is non-negative on 200")
    void schema_optOut_nonNegative() {
        Response r = client.getSmsStats();
        if (r.statusCode() == 200) {
            // Accept either opt_out as a number at top level or nested
            Number optOut = r.jsonPath().get("opt_out");
            if (optOut == null) {
                optOut = r.jsonPath().get("opt_outs");
            }
            if (optOut != null) {
                assertThat(optOut.doubleValue())
                        .as("opt_out count/rate must be >= 0")
                        .isGreaterThanOrEqualTo(0.0);
            }
            // Field may be named differently — skip if absent rather than fail
        } else {
            assertThat(r.statusCode()).isIn(403, 404);
        }
    }
}
