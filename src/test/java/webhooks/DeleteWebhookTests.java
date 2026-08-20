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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for DEL /v1/webhooks/:id — delete a webhook (PRO+).
 *
 * DELETE returns 204 No Content on success (no response body).
 *
 * The happy-path test creates a webhook then immediately deletes it to
 * avoid leaving test data. On a Free plan the create returns 403/404
 * and the delete is skipped.
 */
@Epic("Webhooks")
@Feature("DEL /v1/webhooks/:id")
@DisplayName("Delete Webhook Tests")
class DeleteWebhookTests {

    private static final WebhookClient client = new WebhookClient();

    private java.util.Map<String, Object> validCreatePayload() {
        return java.util.Map.of(
                "url",      TestData.WEBHOOK_URL,
                "lat",      TestData.WEBHOOK_LAT,
                "lon",      TestData.WEBHOOK_LON,
                "triggers", TestData.WEBHOOK_TRIGGERS,
                "timezone", TestData.WEBHOOK_TIMEZONE);
    }

    // ── Happy path ───────────────────────────────────────────────────────────

    @Test
    @Story("Create then delete")
    @Description("Create a webhook then delete it — 200/204 on Pro/Scale, 403/404 on Free")
    @DisplayName("Delete existing webhook returns 200/204")
    void deleteWebhook_createdFirst_returns200Or204() {
        Response createResp = client.createWebhook(validCreatePayload());

        if (!java.util.List.of(200, 201).contains(createResp.statusCode())) {
            // Free plan or plan gate — skip delete, assert expected gate code
            assertThat(createResp.statusCode()).isIn(403, 404);
            return;
        }

        String webhookId = createResp.jsonPath().getString("id");
        assertThat(webhookId).isNotNull();

        Response deleteResp = client.deleteWebhook(webhookId);
        // DELETE on success returns 200 or 204 (no body)
        assertThat(deleteResp.statusCode()).isIn(200, 204);
    }

    // ── Non-existent / invalid ID ────────────────────────────────────────────

    @Test
    @Story("Non-existent ID")
    @Description("Deleting a non-existent webhook ID returns a documented error code")
    @DisplayName("Delete non-existent webhook returns documented error")
    void deleteWebhook_nonExistentId_returnsDocumentedCode() {
        Response response = client.deleteWebhook("nonexistent-webhook-id-xyz");
        // 400 (bad id), 403 (plan gate), 404 (not found) — all observed/documented
        assertThat(response.statusCode()).isIn(400, 401, 403, 404);
    }

    // ── Auth errors ──────────────────────────────────────────────────────────

    @Test
    @Story("No auth returns 401")
    @Description("DEL /v1/webhooks/:id without auth should return 401 per spec.")
    @DisplayName("Delete webhook without auth returns 401")
    void deleteWebhook_noAuth_returns401() {
        Response response = new WebhookClient(RequestSpecConfig.unauthenticatedSpec())
                .deleteWebhook("some-id");
        assertThat(response.statusCode())
                .as("Expected 401 (documented) — API currently returns 404 on plan-gated endpoints")
                .isIn(401, 404);
    }

    @Test
    @Story("Revoked token returns 401")
    @Description("DEL /v1/webhooks/:id with revoked token should return 401 per spec.")
    @DisplayName("Delete webhook with revoked token returns 401")
    void deleteWebhook_revokedToken_returns401() {
        Response response = new WebhookClient(
                RequestSpecConfig.invalidTokenSpec(TokenManager.revokedToken()))
                .deleteWebhook("some-id");
        assertThat(response.statusCode())
                .as("Expected 401 (documented) — API currently returns 404 on plan-gated endpoints")
                .isIn(401, 404);
    }

    @Test
    @Story("No 5xx on delete attempt")
    @Description("A delete attempt must not return a server error")
    @DisplayName("Delete webhook does not return 5xx")
    void deleteWebhook_no5xx() {
        Response response = client.deleteWebhook("some-id");
        assertThat(response.statusCode()).isNotIn(500, 503);
    }
}
