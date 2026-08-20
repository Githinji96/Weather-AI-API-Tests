package org.example.clients;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.example.config.RequestSpecConfig;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Client for Webhook endpoints (PRO+):
 * 
 *   POST /v1/webhooks  — create subscription
 *   GET  /v1/webhooks  — list subscriptions
 *   DEL  /v1/webhooks/:id — delete subscriptions
 * 
 */
public class WebhookClient {

    private final RequestSpecification spec;

    public WebhookClient() {
        this.spec = RequestSpecConfig.defaultSpec();
    }

    public WebhookClient(RequestSpecification spec) {
        this.spec = spec;
    }

    /**
     * Create a webhook subscription.
     *
     * @param payload must contain: "url", "lat", "lon", "triggers" (string array),
     *                "timezone" (IANA timezone string)
     */
    public Response createWebhook(Map<String, Object> payload) {
        return given(spec)
                .body(payload)
                .post("/v1/webhooks");
    }

    /** List all webhooks registered on this account. */
    public Response getWebhooks() {
        return given(spec).get("/v1/webhooks");
    }

    /** Delete a specific webhook by its ID. */
    public Response deleteWebhook(String webhookId) {
        return given(spec)
                .pathParam("id", webhookId)
                .delete("/v1/webhooks/{id}");
    }
}
