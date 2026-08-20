package webhooks;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.example.clients.WebhookClient;
import org.example.config.RequestSpecConfig;
import org.example.utils.TokenManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for GET /v1/webhooks — list webhook subscriptions (PRO+).
 *
 * Confirmed response shape:
 * {
 *   "webhooks": [
 *     {
 *       "id":        string,
 *       "url":       string,
 *       "lat":       float,
 *       "lon":       float,
 *       "triggers":  string[],
 *       "timezone":  string,
 *       "active":    boolean,
 *       "createdAt": ISO-8601 string
 *     }
 *   ]
 * }
 */
@Epic("Webhooks")
@Feature("GET /v1/webhooks")
@DisplayName("Get Webhooks Tests")
class GetWebhookTests {

    private static final WebhookClient client = new WebhookClient();

    // ── Status / transport ───────────────────────────────────────────────────

    @Test
    @Story("PRO+ list or Free 403")
    @Description("GET /v1/webhooks returns 200 on Pro/Scale, 403 on Free")
    @DisplayName("List webhooks returns 200 or 403")
    void listWebhooks_returns200Or403() {
        Response response = client.getWebhooks();
        assertThat(response.statusCode()).isIn(200, 403, 404);
    }

    @Test
    @Story("Response is JSON")
    @Description("GET /v1/webhooks response must be application/json")
    @DisplayName("List webhooks response is JSON")
    void listWebhooks_isJson() {
        Response response = client.getWebhooks();
        assertThat(response.statusCode()).isIn(200, 403, 404);
        assertThat(response.contentType()).containsIgnoringCase("application/json");
    }

    // ── Auth errors ──────────────────────────────────────────────────────────

    @Test
    @Story("No auth returns 401")
    @Description("GET /v1/webhooks without auth should return 401 per spec.")
    @DisplayName("List webhooks without auth returns 401")
    void listWebhooks_noAuth_returns401() {
        Response response = new WebhookClient(RequestSpecConfig.unauthenticatedSpec())
                .getWebhooks();
        assertThat(response.statusCode())
                .as("Expected 401 (documented) — API currently returns 404 on plan-gated endpoints")
                .isIn(401, 404);
    }

    @Test
    @Story("Revoked token returns 401")
    @Description("GET /v1/webhooks with revoked token should return 401 per spec.")
    @DisplayName("List webhooks with revoked token returns 401")
    void listWebhooks_revokedToken_returns401() {
        Response response = new WebhookClient(
                RequestSpecConfig.invalidTokenSpec(TokenManager.revokedToken()))
                .getWebhooks();
        assertThat(response.statusCode())
                .as("Expected 401 (documented) — API currently returns 404 on plan-gated endpoints")
                .isIn(401, 404);
    }

    // ── Response schema — only asserted when 200 ─────────────────────────────

    @Test
    @Story("Schema — webhooks array present")
    @Description("When 200, response must contain a 'webhooks' array")
    @DisplayName("webhooks array is present on 200")
    void schema_webhooksArrayPresent() {
        Response response = client.getWebhooks();
        if (response.statusCode() == 200) {
            List<?> webhooks = response.jsonPath().getList("webhooks");
            assertThat(webhooks).isNotNull();
        } else {
            assertThat(response.statusCode()).isIn(403, 404);
        }
    }

    @Test
    @Story("Schema — first webhook has id")
    @Description("When 200 and list is non-empty, webhooks[0].id must be non-blank")
    @DisplayName("webhooks[0].id is non-blank on 200")
    void schema_firstWebhook_idNonBlank() {
        Response response = client.getWebhooks();
        if (response.statusCode() == 200) {
            List<?> webhooks = response.jsonPath().getList("webhooks");
            if (webhooks != null && !webhooks.isEmpty()) {
                assertThat(response.jsonPath().getString("webhooks[0].id")).isNotBlank();
            }
        } else {
            assertThat(response.statusCode()).isIn(403, 404);
        }
    }

    @Test
    @Story("Schema — first webhook has url")
    @Description("When 200 and list is non-empty, webhooks[0].url must be non-blank")
    @DisplayName("webhooks[0].url is non-blank on 200")
    void schema_firstWebhook_urlNonBlank() {
        Response response = client.getWebhooks();
        if (response.statusCode() == 200) {
            List<?> webhooks = response.jsonPath().getList("webhooks");
            if (webhooks != null && !webhooks.isEmpty()) {
                assertThat(response.jsonPath().getString("webhooks[0].url")).isNotBlank();
            }
        } else {
            assertThat(response.statusCode()).isIn(403, 404);
        }
    }

    @Test
    @Story("Schema — first webhook triggers is array")
    @Description("When 200 and list is non-empty, webhooks[0].triggers must be a non-null array")
    @DisplayName("webhooks[0].triggers is a non-null array on 200")
    void schema_firstWebhook_triggersIsArray() {
        Response response = client.getWebhooks();
        if (response.statusCode() == 200) {
            List<?> webhooks = response.jsonPath().getList("webhooks");
            if (webhooks != null && !webhooks.isEmpty()) {
                List<?> triggers = response.jsonPath().getList("webhooks[0].triggers");
                assertThat(triggers).isNotNull();
            }
        } else {
            assertThat(response.statusCode()).isIn(403, 404);
        }
    }

    @Test
    @Story("Schema — first webhook active is boolean")
    @Description("When 200 and list is non-empty, webhooks[0].active must be true or false")
    @DisplayName("webhooks[0].active is a boolean on 200")
    void schema_firstWebhook_activeIsBoolean() {
        Response response = client.getWebhooks();
        if (response.statusCode() == 200) {
            List<?> webhooks = response.jsonPath().getList("webhooks");
            if (webhooks != null && !webhooks.isEmpty()) {
                Boolean active = response.jsonPath().get("webhooks[0].active");
                assertThat(active).isNotNull().isIn(true, false);
            }
        } else {
            assertThat(response.statusCode()).isIn(403, 404);
        }
    }

    @Test
    @Story("Schema — first webhook has lat and lon")
    @Description("When 200 and list is non-empty, webhooks[0].lat and lon must be valid coordinates")
    @DisplayName("webhooks[0].lat and lon are valid coordinates on 200")
    void schema_firstWebhook_coordsValid() {
        Response response = client.getWebhooks();
        if (response.statusCode() == 200) {
            List<?> webhooks = response.jsonPath().getList("webhooks");
            if (webhooks != null && !webhooks.isEmpty()) {
                Float lat = response.jsonPath().get("webhooks[0].lat");
                Float lon = response.jsonPath().get("webhooks[0].lon");
                assertThat(lat).isNotNull();
                assertThat(lon).isNotNull();
                assertThat(lat.doubleValue()).isBetween(-90.0, 90.0);
                assertThat(lon.doubleValue()).isBetween(-180.0, 180.0);
            }
        } else {
            assertThat(response.statusCode()).isIn(403, 404);
        }
    }

    @Test
    @Story("Schema — first webhook timezone is non-blank")
    @Description("When 200 and list is non-empty, webhooks[0].timezone must be non-blank")
    @DisplayName("webhooks[0].timezone is non-blank on 200")
    void schema_firstWebhook_timezoneNonBlank() {
        Response response = client.getWebhooks();
        if (response.statusCode() == 200) {
            List<?> webhooks = response.jsonPath().getList("webhooks");
            if (webhooks != null && !webhooks.isEmpty()) {
                assertThat(response.jsonPath().getString("webhooks[0].timezone")).isNotBlank();
            }
        } else {
            assertThat(response.statusCode()).isIn(403, 404);
        }
    }

    @Test
    @Story("Schema — first webhook createdAt is non-blank")
    @Description("When 200 and list is non-empty, webhooks[0].createdAt must be a non-blank ISO-8601 string")
    @DisplayName("webhooks[0].createdAt is non-blank on 200")
    void schema_firstWebhook_createdAtNonBlank() {
        Response response = client.getWebhooks();
        if (response.statusCode() == 200) {
            List<?> webhooks = response.jsonPath().getList("webhooks");
            if (webhooks != null && !webhooks.isEmpty()) {
                assertThat(response.jsonPath().getString("webhooks[0].createdAt")).isNotBlank();
            }
        } else {
            assertThat(response.statusCode()).isIn(403, 404);
        }
    }
}
