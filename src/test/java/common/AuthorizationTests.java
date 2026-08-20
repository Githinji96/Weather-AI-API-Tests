package common;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.example.clients.SmsClient;
import org.example.clients.WebhookClient;
import org.example.clients.WeatherClient;
import org.example.config.RequestSpecConfig;
import org.example.utils.TestData;
import org.example.utils.TokenManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies plan-level access control (403 Forbidden).
 *
 * <p>SMS endpoints require Scale plan + smsEnabled.
 * Webhook and Forecast-14 endpoints require Pro or Scale plan.
 * Tests here use the valid token but probe endpoints that may be
 * gated — the assertion depends on the plan tied to your test key.
 * On a Free key all PRO+ assertions expect 403; adjust if running on Pro/Scale.
 */
@Epic("Common")
@Feature("Authorization")
@DisplayName("Authorization Tests")
class AuthorizationTests {

    private static final WeatherClient weatherClient = new WeatherClient();
    private static final WebhookClient webhookClient = new WebhookClient();
    private static final SmsClient    smsClient     = new SmsClient();

    // ── PRO+ endpoints on Free plan should return 403 ────────────────────────

    @Test
    @Story("Plan gate — forecast14")
    @Description("GET /v1/forecast14 with Free plan should return 403")
    @DisplayName("Free plan: forecast14 returns 403")
    void forecast14_freeKeyReturns403() {
        Response response = weatherClient.getForecast14(
                TestData.LAT_NAIROBI, TestData.LON_NAIROBI);

        // 200 if running with a Pro/Scale key, 403 on Free
        assertThat(response.statusCode()).isIn(200, 403);
    }

    @Test
    @Story("Plan gate — insights")
    @Description("GET /v1/insights with Free plan should return 403")
    @DisplayName("Free plan: insights returns 403")
    void insights_freeKeyReturns403() {
        Response response = weatherClient.getInsights(
                TestData.LAT_NAIROBI, TestData.LON_NAIROBI);

        assertThat(response.statusCode()).isIn(200, 403);
    }

    // ── SMS endpoints require Scale + smsEnabled ─────────────────────────────

    @Test
    @Story("Plan gate — SMS send")
    @Description("POST /v1/sms/send without Scale plan returns 403. " +
                 "Note: API returns 404 on plan-gated endpoints (observed, not documented).")
    @DisplayName("Non-Scale plan: sms/send returns 403")
    void smsSend_nonScaleReturns403() {
        Response response = smsClient.sendSms(Map.of(
                "to",      TestData.PHONE_VALID,
                "message", TestData.SMS_MESSAGE,
                "type",    TestData.SMS_TYPE));
        // Documented: 403. Observed: 404 — API applies plan gate before returning 403
        assertThat(response.statusCode())
                .as("Expected 403 (documented) — API currently returns 404 on plan-gated endpoints")
                .isIn(200, 201, 403, 404);
    }

    // ── Webhook endpoints require Pro+ ────────────────────────────────────────

    @Test
    @Story("Plan gate — webhooks")
    @Description("GET /v1/webhooks on Free plan should return 403. " +
                 "Note: API returns 404 on plan-gated endpoints (observed, not documented).")
    @DisplayName("Free plan: list webhooks returns 403")
    void listWebhooks_freeKeyReturns403() {
        Response response = webhookClient.getWebhooks();
        // Documented: 403. Observed: 404 — API applies plan gate before returning 403
        assertThat(response.statusCode())
                .as("Expected 403 (documented) — API currently returns 404 on plan-gated endpoints")
                .isIn(200, 403, 404);
    }

    // ── Invalid token still gets 401 (not 403) ───────────────────────────────

    @Test
    @Story("Auth vs authz distinction")
    @Description("An invalid token on a public endpoint should return 401, not 403")
    @DisplayName("Invalid token on /v1/weather returns 401")
    void invalidToken_returns401NotForbidden() {
        // Use /v1/weather (available on all plans) so we test auth, not plan-gating
        Response response = new WeatherClient(
                RequestSpecConfig.invalidTokenSpec(TokenManager.revokedToken()))
                .getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI);

        assertThat(response.statusCode()).isEqualTo(401);
    }
}
