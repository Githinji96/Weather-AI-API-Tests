package org.example.clients;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.example.config.RequestSpecConfig;

import java.util.Map;

import static io.restassured.RestAssured.given;


 //Client for SMS / USSD endpoints (Scale plan only):

public class SmsClient {

    private final RequestSpecification spec;

    public SmsClient() {
        this.spec = RequestSpecConfig.defaultSpec();
    }

    public SmsClient(RequestSpecification spec) {
        this.spec = spec;
    }

    /**
     * Send a plain SMS message.
     *
     * @param payload must contain: "to" (E.164 phone), "message" (string).
     *                Optional: "type" (e.g. "weather_alert").
     */
    public Response sendSms(Map<String, Object> payload) {
        return given(spec)
                .body(payload)
                .post("/v1/sms/send");
    }

    // Send a structured weather alert via SMS. //
    public Response sendAlert(Map<String, Object> payload) {
        return given(spec)
                .body(payload)
                .post("/v1/sms/alert");
    }

    /**
     * Register a farmer in the Bomet programme.
     *
     * @param payload must contain: "phone" (E.164), "name" (full name).
     *                Optional: "location" (village/ward), "cropType" (primary crop).
     */
    public Response registerBometFarmer(Map<String, Object> payload) {
        return given(spec)
                .body(payload)
                .post("/v1/sms/bomet/register");
    }

    /** Retrieve SMS usage statistics. */
    public Response getSmsStats() {
        return given(spec).get("/v1/sms/stats");
    }

    /** Check SMS gateway health. */
    public Response getSmsHealth() {
        return given(spec).get("/v1/sms/health");
    }
}
