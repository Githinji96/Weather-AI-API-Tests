package org.example.clients;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.example.config.RequestSpecConfig;

import static io.restassured.RestAssured.given;

/**
 * Client for Account / Usage endpoint:
 */
public class AccountClient {

    private final RequestSpecification spec;

    public AccountClient() {
        this.spec = RequestSpecConfig.defaultSpec();
    }

    public AccountClient(RequestSpecification spec) {
        this.spec = spec;
    }

    /** Fetch billing-period usage stats. No query parameters required. */
    public Response getUsage() {
        return given(spec).get("/v1/usage");
    }
}
