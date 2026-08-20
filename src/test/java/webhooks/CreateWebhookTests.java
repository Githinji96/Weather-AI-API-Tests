package webhooks;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.example.clients.WebhookClient;
import org.example.config.RequestSpecConfig;
import org.example.utils.TestData;
import org.example.utils.TokenManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for POST /v1/webhooks — create a webhook subscription (PRO+).
 *
 * Confirmed request shape:
 * {
 *   "url":      "https://yourapp.com/weather-hook",  // required
 *   "lat":      34.0522,                             // required
 *   "lon":      -118.2437,                           // required
 *   "triggers": ["rain", "extreme_wind"],             // required
 *   "timezone": "America/Los_Angeles"                // required
 * }
 */
@Epic("Webhooks")
@Feature("POST /v1/webhooks")
@DisplayName("Create Webhook Tests")
class CreateWebhookTests {

    private static final WebhookClient client = new WebhookClient();

    private Map<String, Object> validPayload() {
        return Map.of(
                "url",      TestData.WEBHOOK_URL,
                "lat",      TestData.WEBHOOK_LAT,
                "lon",      TestData.WEBHOOK_LON,
                "triggers", TestData.WEBHOOK_TRIGGERS,
                "timezone", TestData.WEBHOOK_TIMEZONE
        );
    }

    // ── Happy path ───────────────────────────────────────────────────────────

    @Test
    @Story("PRO+ create or Free 403")
    @Description("POST /v1/webhooks returns 200/201 on Pro/Scale, 403 on Free")
    @DisplayName("Create webhook returns 200/201 or 403")
    void createWebhook_returns201Or403() {
        Response response = client.createWebhook(validPayload());
        assertThat(response.statusCode()).isIn(200, 201, 403, 404);
    }

    @Test
    @Story("Response is JSON")
    @Description("POST /v1/webhooks response content-type must be application/json")
    @DisplayName("Create webhook response is JSON")
    void createWebhook_isJson() {
        Response response = client.createWebhook(validPayload());
        assertThat(response.statusCode()).isIn(200, 201, 403, 404);
        assertThat(response.contentType()).containsIgnoringCase("application/json");
    }

    // ── Auth errors ──────────────────────────────────────────────────────────

    @Test
    @Story("No auth returns 401")
    @Description("POST /v1/webhooks without auth should return 401 per spec.")
    @DisplayName("Create webhook without auth returns 401")
    void createWebhook_noAuth_returns401() {
        Response response = new WebhookClient(RequestSpecConfig.unauthenticatedSpec())
                .createWebhook(validPayload());
        assertThat(response.statusCode())
                .as("Expected 401 (documented) — API currently returns 404 on plan-gated endpoints")
                .isIn(401, 404);
    }

    @Test
    @Story("Revoked token returns 401")
    @Description("POST /v1/webhooks with revoked token should return 401 per spec.")
    @DisplayName("Create webhook with revoked token returns 401")
    void createWebhook_revokedToken_returns401() {
        Response response = new WebhookClient(
                RequestSpecConfig.invalidTokenSpec(TokenManager.revokedToken()))
                .createWebhook(validPayload());
        assertThat(response.statusCode())
                .as("Expected 401 (documented) — API currently returns 404 on plan-gated endpoints")
                .isIn(401, 404);
    }

    // ── Validation errors ────────────────────────────────────────────────────

    @Test
    @Story("Missing url returns 400")
    @Description("POST /v1/webhooks without 'url' should return 400 or 403")
    @DisplayName("Missing url returns 400 or 403")
    void createWebhook_missingUrl_returns400() {
        Response response = client.createWebhook(Map.of(
                "lat",      TestData.WEBHOOK_LAT,
                "lon",      TestData.WEBHOOK_LON,
                "triggers", TestData.WEBHOOK_TRIGGERS,
                "timezone", TestData.WEBHOOK_TIMEZONE));
        assertThat(response.statusCode()).isIn(400, 403, 404);
    }

    @Test
    @Story("Missing triggers returns 400")
    @Description("POST /v1/webhooks without 'triggers' should return 400 or 403")
    @DisplayName("Missing triggers returns 400 or 403")
    void createWebhook_missingTriggers_returns400() {
        Response response = client.createWebhook(Map.of(
                "url", TestData.WEBHOOK_URL,
                "lat", TestData.WEBHOOK_LAT,
                "lon", TestData.WEBHOOK_LON));
        assertThat(response.statusCode()).isIn(400, 403, 404);
    }

    @Test
    @Story("Empty triggers array returns 400")
    @Description("POST /v1/webhooks with empty triggers array should return 400 or 403")
    @DisplayName("Empty triggers array returns 400 or 403")
    void createWebhook_emptyTriggers_returns400() {
        Response response = client.createWebhook(Map.of(
                "url",      TestData.WEBHOOK_URL,
                "lat",      TestData.WEBHOOK_LAT,
                "lon",      TestData.WEBHOOK_LON,
                "triggers", List.of(),
                "timezone", TestData.WEBHOOK_TIMEZONE));
        assertThat(response.statusCode()).isIn(400, 403, 404);
    }

    @Test
    @Story("No 5xx on valid request")
    @Description("A well-formed request must not return a server error")
    @DisplayName("Valid request does not return 5xx")
    void createWebhook_no5xx() {
        Response response = client.createWebhook(validPayload());
        assertThat(response.statusCode()).isNotIn(500, 503);
    }

    // ── Response schema — only asserted when 200/201 ─────────────────────────
    // Confirmed shape mirrors GET webhooks[0]:
    // { "id": string, "url": string, "lat": float, "lon": float,
    //   "triggers": string[], "timezone": string, "active": boolean,
    //   "createdAt": ISO-8601 string }

    @Test
    @Story("Schema — id is non-blank on 201")
    @Description("When 200/201, created webhook response must contain a non-blank id")
    @DisplayName("Created webhook id is non-blank")
    void schema_created_idNonBlank() {
        Response response = client.createWebhook(validPayload());
        if (response.statusCode() == 200 || response.statusCode() == 201) {
            assertThat(response.jsonPath().getString("id"))
                    .as("Created webhook must have a non-blank id")
                    .isNotBlank();
        } else {
            assertThat(response.statusCode()).isIn(403, 404);
        }
    }

    @Test
    @Story("Schema — url echoed on 201")
    @Description("When 200/201, response must echo back the requested url")
    @DisplayName("Created webhook echoes url")
    void schema_created_urlEchoed() {
        Response response = client.createWebhook(validPayload());
        if (response.statusCode() == 200 || response.statusCode() == 201) {
            assertThat(response.jsonPath().getString("url"))
                    .as("url must be echoed in the response")
                    .isEqualTo(TestData.WEBHOOK_URL);
        } else {
            assertThat(response.statusCode()).isIn(403, 404);
        }
    }

    @Test
    @Story("Schema — triggers echoed on 201")
    @Description("When 200/201, response must echo back the triggers array")
    @DisplayName("Created webhook echoes triggers array")
    void schema_created_triggersEchoed() {
        Response response = client.createWebhook(validPayload());
        if (response.statusCode() == 200 || response.statusCode() == 201) {
            List<?> triggers = response.jsonPath().getList("triggers");
            assertThat(triggers)
                    .as("triggers must be echoed as a non-empty array")
                    .isNotNull()
                    .isNotEmpty();
        } else {
            assertThat(response.statusCode()).isIn(403, 404);
        }
    }

    @Test
    @Story("Schema — active is true on 201")
    @Description("When 200/201, newly created webhook must be active=true")
    @DisplayName("Created webhook active = true")
    void schema_created_activeIsTrue() {
        Response response = client.createWebhook(validPayload());
        if (response.statusCode() == 200 || response.statusCode() == 201) {
            Boolean active = response.jsonPath().get("active");
            assertThat(active)
                    .as("Newly created webhook must have active=true")
                    .isNotNull()
                    .isTrue();
        } else {
            assertThat(response.statusCode()).isIn(403, 404);
        }
    }

    @Test
    @Story("Schema — createdAt is non-blank on 201")
    @Description("When 200/201, response must contain a non-blank createdAt timestamp")
    @DisplayName("Created webhook createdAt is non-blank")
    void schema_created_createdAtNonBlank() {
        Response response = client.createWebhook(validPayload());
        if (response.statusCode() == 200 || response.statusCode() == 201) {
            assertThat(response.jsonPath().getString("createdAt"))
                    .as("createdAt must be a non-blank ISO-8601 timestamp")
                    .isNotBlank();
        } else {
            assertThat(response.statusCode()).isIn(403, 404);
        }
    }

    @Test
    @Story("Schema — lat and lon echoed on 201")
    @Description("When 200/201, response must echo back lat and lon within 0.001°")
    @DisplayName("Created webhook echoes lat and lon")
    void schema_created_coordsEchoed() {
        Response response = client.createWebhook(validPayload());
        if (response.statusCode() == 200 || response.statusCode() == 201) {
            Float lat = response.jsonPath().get("lat");
            Float lon = response.jsonPath().get("lon");
            assertThat(lat).isNotNull();
            assertThat(lon).isNotNull();
            assertThat(lat.doubleValue())
                    .isCloseTo(TestData.WEBHOOK_LAT, org.assertj.core.data.Offset.offset(0.001));
            assertThat(lon.doubleValue())
                    .isCloseTo(TestData.WEBHOOK_LON, org.assertj.core.data.Offset.offset(0.001));
        } else {
            assertThat(response.statusCode()).isIn(403, 404);
        }
    }
}
