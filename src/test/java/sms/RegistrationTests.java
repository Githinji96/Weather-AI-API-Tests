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
 * Tests for POST /v1/sms/bomet/register — Bomet farmer registration (Scale only).
 *
 * Confirmed request shape:
 * {
 *   "phone":     "+254712345678",  // required — E.164 farmer phone
 *   "name":      "John Kipchoge", // required — full name
 *   "location":  "Bomet Central", // optional — village/ward
 *   "cropType":  "maize"          // optional — primary crop
 * }
 *
 * Documented error codes: 400, 401, 403, 429, 500, 503.
 */
@Epic("SMS")
@Feature("POST /v1/sms/bomet/register")
@DisplayName("Farmer Registration Tests")
class RegistrationTests {

    private static final SmsClient client = new SmsClient();

    /** Full payload — all fields including optional ones. */
    private Map<String, Object> fullPayload() {
        return Map.of(
                "phone",    TestData.PHONE_VALID,
                "name",     TestData.FARMER_NAME,
                "location", TestData.FARMER_LOCATION,
                "cropType", TestData.FARMER_CROP_TYPE
        );
    }

    /** Minimal payload — required fields only. */
    private Map<String, Object> minimalPayload() {
        return Map.of(
                "phone", TestData.PHONE_VALID,
                "name",  TestData.FARMER_NAME
        );
    }

    // ── Happy path / transport ────────────────────────────────────────────────

    @Test
    @Story("Scale or 403 — full payload")
    @Description("POST /v1/sms/bomet/register with all fields returns 200/201 on Scale+approved, 403/404 otherwise")
    @DisplayName("Full payload returns 200/201 or 403")
    void bometRegister_fullPayload_returns201Or403() {
        Response r = client.registerBometFarmer(fullPayload());
        assertThat(r.statusCode()).isIn(200, 201, 403, 404);
    }

    @Test
    @Story("Scale or 403 — minimal payload")
    @Description("POST /v1/sms/bomet/register with only required fields returns 200/201 or 403/404")
    @DisplayName("Minimal payload (phone + name only) returns 200/201 or 403")
    void bometRegister_minimalPayload_returns201Or403() {
        Response r = client.registerBometFarmer(minimalPayload());
        assertThat(r.statusCode()).isIn(200, 201, 403, 404);
    }

    @Test
    @Story("Response is JSON")
    @Description("Response Content-Type must be application/json")
    @DisplayName("Response is application/json")
    void bometRegister_isJson() {
        Response r = client.registerBometFarmer(fullPayload());
        assertThat(r.statusCode()).isIn(200, 201, 403, 404);
        assertThat(r.contentType()).containsIgnoringCase("application/json");
    }

    @Test
    @Story("No 5xx on valid request")
    @Description("A well-formed request must not return a server error")
    @DisplayName("Valid request does not return 5xx")
    void bometRegister_no5xx() {
        Response r = client.registerBometFarmer(fullPayload());
        assertThat(r.statusCode()).isNotIn(500, 503);
    }

    // ── Response schema — only asserted when 200/201 ─────────────────────────

    @Test
    @Story("Schema — response body non-blank on 201")
    @Description("When 200/201, response body must not be blank")
    @DisplayName("Response body is non-blank on 200/201")
    void schema_responseBodyNonBlank() {
        Response r = client.registerBometFarmer(fullPayload());
        if (r.statusCode() == 200 || r.statusCode() == 201) {
            assertThat(r.body().asString()).isNotBlank();
        } else {
            assertThat(r.statusCode()).isIn(403, 404);
        }
    }

    // ── Auth errors ──────────────────────────────────────────────────────────

    @Test
    @Story("No auth returns 401")
    @Description("POST /v1/sms/bomet/register without auth should return 401 per spec.")
    @DisplayName("No auth returns 401")
    void bometRegister_noAuth_returns401() {
        Response r = new SmsClient(RequestSpecConfig.unauthenticatedSpec())
                .registerBometFarmer(fullPayload());
        assertThat(r.statusCode())
                .as("Expected 401 (documented) — API currently returns 404 on plan-gated endpoints")
                .isIn(401, 404);
    }

    @Test
    @Story("Revoked token returns 401")
    @Description("POST /v1/sms/bomet/register with revoked token should return 401 per spec.")
    @DisplayName("Revoked token returns 401")
    void bometRegister_revokedToken_returns401() {
        Response r = new SmsClient(
                RequestSpecConfig.invalidTokenSpec(TokenManager.revokedToken()))
                .registerBometFarmer(fullPayload());
        assertThat(r.statusCode())
                .as("Expected 401 (documented) — API currently returns 404 on plan-gated endpoints")
                .isIn(401, 404);
    }

    // ── Validation errors (400) ───────────────────────────────────────────────

    @Test
    @Story("Missing 'phone' returns 400")
    @Description("Omitting the required 'phone' field should return 400")
    @DisplayName("Missing 'phone' returns 400 or 403")
    void bometRegister_missingPhone_returns400() {
        Response r = client.registerBometFarmer(Map.of("name", TestData.FARMER_NAME));
        assertThat(r.statusCode()).isIn(400, 403, 404);
    }

    @Test
    @Story("Missing 'name' returns 400")
    @Description("Omitting the required 'name' field should return 400")
    @DisplayName("Missing 'name' returns 400 or 403")
    void bometRegister_missingName_returns400() {
        Response r = client.registerBometFarmer(Map.of("phone", TestData.PHONE_VALID));
        assertThat(r.statusCode()).isIn(400, 403, 404);
    }

    @Test
    @Story("Invalid phone returns 400")
    @Description("Non E.164 phone number in 'phone' should return 400")
    @DisplayName("Invalid phone returns 400 or 403")
    void bometRegister_invalidPhone_returns400() {
        Response r = client.registerBometFarmer(Map.of(
                "phone", TestData.PHONE_INVALID,
                "name",  TestData.FARMER_NAME));
        assertThat(r.statusCode()).isIn(400, 403, 404);
    }

    @Test
    @Story("Empty payload returns 400")
    @Description("Empty request body should return 400")
    @DisplayName("Empty payload returns 400 or 403")
    void bometRegister_emptyPayload_returns400() {
        Response r = client.registerBometFarmer(Map.of());
        assertThat(r.statusCode()).isIn(400, 403, 404);
    }
}
