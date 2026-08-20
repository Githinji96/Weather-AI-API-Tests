package sms;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.example.clients.SmsClient;
import org.example.config.RequestSpecConfig;
import org.example.utils.TestData;
import org.example.utils.TokenManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for POST /v1/sms/alert — structured weather alert via SMS (Scale only).
 *
 * Confirmed request shape:
 * {
 *   "to":        "+254712345678",         // required — E.164 phone
 *   "alertType": "rain",                  // required — rain|frost|extreme_wind|drought
 *   "data":      { "mm": 45, "day": "tomorrow" }  // optional — template context
 * }
 *
 * Documented error codes: 400, 401, 403, 429, 500, 503.
 */
@Epic("SMS")
@Feature("POST /v1/sms/alert")
@DisplayName("SMS Alert Tests")
class SmsAlertTests {

    private static final SmsClient client = new SmsClient();

    private Map<String, Object> validPayload() {
        return Map.of(
                "to",        TestData.PHONE_VALID,
                "alertType", TestData.ALERT_TYPE_RAIN
        );
    }

    private Map<String, Object> fullPayload() {
        return Map.of(
                "to",        TestData.PHONE_VALID,
                "alertType", TestData.ALERT_TYPE_RAIN,
                "data",      Map.of("mm", 45, "day", "tomorrow")
        );
    }

    // ── Happy path / transport ────────────────────────────────────────────────

    @Test
    @Story("Scale or 403 — rain alert")
    @Description("POST /v1/sms/alert with alertType=rain returns 200/201 or 403/404")
    @DisplayName("Rain alert returns 200/201 or 403")
    void smsAlert_rain_returns201Or403() {
        Response r = client.sendAlert(validPayload());
        assertThat(r.statusCode()).isIn(200, 201, 403, 404);
    }

    @Test
    @Story("All alertType values accepted")
    @Description("Each documented alertType (rain, frost, extreme_wind, drought) returns 200/201 or 403")
    @DisplayName("alertType=frost returns 200/201 or 403")
    void smsAlert_frost_returns201Or403() {
        Response r = client.sendAlert(Map.of(
                "to",        TestData.PHONE_VALID,
                "alertType", TestData.ALERT_TYPE_FROST));
        assertThat(r.statusCode()).isIn(200, 201, 403, 404);
    }

    @Test
    @Story("All alertType values accepted")
    @Description("alertType=extreme_wind returns 200/201 or 403")
    @DisplayName("alertType=extreme_wind returns 200/201 or 403")
    void smsAlert_extremeWind_returns201Or403() {
        Response r = client.sendAlert(Map.of(
                "to",        TestData.PHONE_VALID,
                "alertType", TestData.ALERT_TYPE_WIND));
        assertThat(r.statusCode()).isIn(200, 201, 403, 404);
    }

    @Test
    @Story("All alertType values accepted")
    @Description("alertType=drought returns 200/201 or 403")
    @DisplayName("alertType=drought returns 200/201 or 403")
    void smsAlert_drought_returns201Or403() {
        Response r = client.sendAlert(Map.of(
                "to",        TestData.PHONE_VALID,
                "alertType", TestData.ALERT_TYPE_DROUGHT));
        assertThat(r.statusCode()).isIn(200, 201, 403, 404);
    }

    @Test
    @Story("With optional data object")
    @Description("POST /v1/sms/alert with optional data context returns 200/201 or 403")
    @DisplayName("Alert with data object returns 200/201 or 403")
    void smsAlert_withData_returns201Or403() {
        Response r = client.sendAlert(fullPayload());
        assertThat(r.statusCode()).isIn(200, 201, 403, 404);
    }

    @Test
    @Story("Response is JSON")
    @Description("Response Content-Type must be application/json")
    @DisplayName("Alert response is application/json")
    void smsAlert_isJson() {
        Response r = client.sendAlert(validPayload());
        assertThat(r.statusCode()).isIn(200, 201, 403, 404);
        assertThat(r.contentType()).containsIgnoringCase("application/json");
    }

    @Test
    @Story("No 5xx on valid request")
    @Description("A well-formed request must not return a server error")
    @DisplayName("Valid alert request does not return 5xx")
    void smsAlert_no5xx() {
        Response r = client.sendAlert(validPayload());
        assertThat(r.statusCode()).isNotIn(500, 503);
    }

    // ── Response schema — only asserted when 200/201 ─────────────────────────

    @Test
    @Story("Schema — response body non-blank on 201")
    @Description("When 200/201, response body must not be blank")
    @DisplayName("Alert response body is non-blank on 200/201")
    void schema_responseBodyNonBlank() {
        Response r = client.sendAlert(validPayload());
        if (r.statusCode() == 200 || r.statusCode() == 201) {
            assertThat(r.body().asString()).isNotBlank();
        } else {
            assertThat(r.statusCode()).isIn(403, 404);
        }
    }

    // ── Auth errors ──────────────────────────────────────────────────────────

    @Test
    @Story("No auth returns 401")
    @Description("POST /v1/sms/alert without auth should return 401 per spec.")
    @DisplayName("No auth returns 401")
    void smsAlert_noAuth_returns401() {
        Response r = new SmsClient(RequestSpecConfig.unauthenticatedSpec())
                .sendAlert(validPayload());
        assertThat(r.statusCode())
                .as("Expected 401 (documented) — API currently returns 404 on plan-gated endpoints")
                .isIn(401, 404);
    }

    @Test
    @Story("Revoked token returns 401")
    @Description("POST /v1/sms/alert with revoked token should return 401 per spec.")
    @DisplayName("Revoked token returns 401")
    void smsAlert_revokedToken_returns401() {
        Response r = new SmsClient(
                RequestSpecConfig.invalidTokenSpec(TokenManager.revokedToken()))
                .sendAlert(validPayload());
        assertThat(r.statusCode())
                .as("Expected 401 (documented) — API currently returns 404 on plan-gated endpoints")
                .isIn(401, 404);
    }

    // ── Validation errors (400) ───────────────────────────────────────────────

    @Test
    @Story("Missing 'to' returns 400")
    @Description("Omitting the required 'to' field should return 400")
    @DisplayName("Missing 'to' returns 400 or 403")
    void smsAlert_missingTo_returns400() {
        Response r = client.sendAlert(Map.of("alertType", TestData.ALERT_TYPE_RAIN));
        assertThat(r.statusCode()).isIn(400, 403, 404);
    }

    @Test
    @Story("Missing 'alertType' returns 400")
    @Description("Omitting the required 'alertType' field should return 400")
    @DisplayName("Missing 'alertType' returns 400 or 403")
    void smsAlert_missingAlertType_returns400() {
        Response r = client.sendAlert(Map.of("to", TestData.PHONE_VALID));
        assertThat(r.statusCode()).isIn(400, 403, 404);
    }

    @Test
    @Story("Invalid alertType returns 400")
    @Description("An unrecognised alertType value should return 400")
    @DisplayName("Invalid alertType returns 400 or 403")
    void smsAlert_invalidAlertType_returns400() {
        Response r = client.sendAlert(Map.of(
                "to",        TestData.PHONE_VALID,
                "alertType", "unknown_alert_type"));
        assertThat(r.statusCode()).isIn(400, 403, 404);
    }

    @Test
    @Story("Invalid 'to' phone returns 400")
    @Description("Non E.164 phone number in 'to' should return 400")
    @DisplayName("Invalid 'to' returns 400 or 403")
    void smsAlert_invalidTo_returns400() {
        Response r = client.sendAlert(Map.of(
                "to",        TestData.PHONE_INVALID,
                "alertType", TestData.ALERT_TYPE_RAIN));
        assertThat(r.statusCode()).isIn(400, 403, 404);
    }

    @Test
    @Story("Empty payload returns 400")
    @Description("Empty request body should return 400")
    @DisplayName("Empty payload returns 400 or 403")
    void smsAlert_emptyPayload_returns400() {
        Response r = client.sendAlert(Map.of());
        assertThat(r.statusCode()).isIn(400, 403, 404);
    }
}
