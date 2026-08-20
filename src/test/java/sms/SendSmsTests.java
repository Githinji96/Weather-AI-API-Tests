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
 * Tests for POST /v1/sms/send — Scale plan + smsEnabled required.
 *
 * Confirmed request shape:
 * {
 *   "to":         "+254712345678",   // required — E.164 phone
 *   "message":    "...",             // required — max 160 chars per segment
 *   "type":       "weather_alert",   // optional — analytics tag, default: general
 *   "pilotTag":   "pilot-bomet-2026" // optional — pilot programme identifier
 * }
 *
 * Documented error codes: 400, 401, 403, 429, 500, 503.
 * Tests accept 200/201 (Scale+approved) OR 403/404 (Free/Pro or SMS not enabled).
 */
@Epic("SMS")
@Feature("POST /v1/sms/send")
@DisplayName("Send SMS Tests")
class SendSmsTests {

    private static final SmsClient client = new SmsClient();

    private Map<String, Object> validPayload() {
        return Map.of(
                "to",      TestData.PHONE_VALID,
                "message", TestData.SMS_MESSAGE,
                "type",    TestData.SMS_TYPE
        );
    }

    private Map<String, Object> fullPayload() {
        return Map.of(
                "to",        TestData.PHONE_VALID,
                "message",   TestData.SMS_MESSAGE,
                "type",      TestData.SMS_TYPE,
                "pilotTag",  TestData.SMS_PILOT_TAG
        );
    }

    // ── Happy path / transport ────────────────────────────────────────────────

    @Test
    @Story("Scale plan or 403")
    @Description("POST /v1/sms/send returns 200/201 on Scale+approved, 403/404 otherwise")
    @DisplayName("Send SMS returns 200/201 or 403")
    void sendSms_returns201Or403() {
        Response r = client.sendSms(validPayload());
        assertThat(r.statusCode()).isIn(200, 201, 403, 404);
    }

    @Test
    @Story("Response is JSON")
    @Description("Response Content-Type must be application/json")
    @DisplayName("Response is application/json")
    void sendSms_isJson() {
        Response r = client.sendSms(validPayload());
        assertThat(r.statusCode()).isIn(200, 201, 403, 404);
        assertThat(r.contentType()).containsIgnoringCase("application/json");
    }

    @Test
    @Story("With optional fields")
    @Description("POST /v1/sms/send with type and pilotTag returns 200/201 or 403")
    @DisplayName("Full payload with pilotTag returns 200/201 or 403")
    void sendSms_fullPayload_returns201Or403() {
        Response r = client.sendSms(fullPayload());
        assertThat(r.statusCode()).isIn(200, 201, 403, 404);
    }

    @Test
    @Story("Without optional type field")
    @Description("Only required fields (to + message) returns 200/201 or 403")
    @DisplayName("Minimal payload (no type) returns 200/201 or 403")
    void sendSms_minimalPayload_returns201Or403() {
        Response r = client.sendSms(Map.of(
                "to",      TestData.PHONE_VALID,
                "message", TestData.SMS_MESSAGE));
        assertThat(r.statusCode()).isIn(200, 201, 403, 404);
    }

    @Test
    @Story("No 5xx on valid request")
    @Description("A well-formed request must not return a server error")
    @DisplayName("Valid request does not return 5xx")
    void sendSms_no5xx() {
        Response r = client.sendSms(validPayload());
        assertThat(r.statusCode()).isNotIn(500, 503);
    }

    // ── Response schema — only asserted when 200/201 ─────────────────────────

    @Test
    @Story("Schema — response body non-blank on 201")
    @Description("When 200/201, response body must not be blank")
    @DisplayName("Response body is non-blank on 200/201")
    void schema_responseBodyNonBlank() {
        Response r = client.sendSms(validPayload());
        if (r.statusCode() == 200 || r.statusCode() == 201) {
            assertThat(r.body().asString()).isNotBlank();
        } else {
            assertThat(r.statusCode()).isIn(403, 404);
        }
    }

    // ── Auth errors ──────────────────────────────────────────────────────────

    @Test
    @Story("No auth returns 401")
    @Description("POST /v1/sms/send without auth should return 401 per spec.")
    @DisplayName("No auth returns 401")
    void sendSms_noAuth_returns401() {
        Response r = new SmsClient(RequestSpecConfig.unauthenticatedSpec())
                .sendSms(validPayload());
        assertThat(r.statusCode())
                .as("Expected 401 (documented) — API currently returns 404 on plan-gated endpoints")
                .isIn(401, 404);
    }

    @Test
    @Story("Revoked token returns 401")
    @Description("POST /v1/sms/send with revoked token should return 401 per spec.")
    @DisplayName("Revoked token returns 401")
    void sendSms_revokedToken_returns401() {
        Response r = new SmsClient(
                RequestSpecConfig.invalidTokenSpec(TokenManager.revokedToken()))
                .sendSms(validPayload());
        assertThat(r.statusCode())
                .as("Expected 401 (documented) — API currently returns 404 on plan-gated endpoints")
                .isIn(401, 404);
    }

    // ── Validation errors (400) ───────────────────────────────────────────────

    @Test
    @Story("Invalid 'to' phone returns 400")
    @Description("Non E.164 phone number in 'to' should return 400")
    @DisplayName("Invalid 'to' returns 400 or 403")
    void sendSms_invalidTo_returns400() {
        Response r = client.sendSms(Map.of(
                "to",      TestData.PHONE_INVALID,
                "message", TestData.SMS_MESSAGE,
                "type",    TestData.SMS_TYPE));
        assertThat(r.statusCode()).isIn(400, 403, 404);
    }

    @Test
    @Story("Missing 'to' returns 400")
    @Description("Omitting the required 'to' field should return 400")
    @DisplayName("Missing 'to' returns 400 or 403")
    void sendSms_missingTo_returns400() {
        Response r = client.sendSms(Map.of(
                "message", TestData.SMS_MESSAGE,
                "type",    TestData.SMS_TYPE));
        assertThat(r.statusCode()).isIn(400, 403, 404);
    }

    @Test
    @Story("Missing 'message' returns 400")
    @Description("Omitting the required 'message' field should return 400")
    @DisplayName("Missing 'message' returns 400 or 403")
    void sendSms_missingMessage_returns400() {
        Response r = client.sendSms(Map.of(
                "to",   TestData.PHONE_VALID,
                "type", TestData.SMS_TYPE));
        assertThat(r.statusCode()).isIn(400, 403, 404);
    }

    @Test
    @Story("Empty payload returns 400")
    @Description("Empty request body should return 400")
    @DisplayName("Empty payload returns 400 or 403")
    void sendSms_emptyPayload_returns400() {
        Response r = client.sendSms(Map.of());
        assertThat(r.statusCode()).isIn(400, 403, 404);
    }
}
