package org.example.config;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.DecoderConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/**
 * Factory for pre-configured RestAssured {@link RequestSpecification} objects.
 *
 * <p>All clients should obtain their base spec from here so that auth headers,
 * base URI, timeouts, and logging are configured in exactly one place.
 */
public class RequestSpecConfig {

    private static final ApiConfig CONFIG = ApiConfig.getInstance();

    private RequestSpecConfig() {}

    /**
     * Base RestAssured config shared by all specs.
     * Enables gzip/deflate response decompression so the response body
     * stream is not exhausted before logging filters can read it.
     *
     * NOTE: we do NOT call closeIdleConnectionsAfterEachResponse() here.
     * That setting forces a new TCP connection for every request, which causes
     * the server to close connections mid-stream under rapid sequential test
     * execution.  The default Apache HTTP connection pool with keep-alive is
     * the correct behaviour for a test suite.
     */
    private static RestAssuredConfig baseConfig() {
        return RestAssuredConfig.config()
                .decoderConfig(DecoderConfig.decoderConfig()
                        .contentDecoders(DecoderConfig.ContentDecoder.DEFLATE,
                                         DecoderConfig.ContentDecoder.GZIP));
    }

    /**
     * Standard spec — authenticated, JSON content type, full req/resp logging.
     */
    public static RequestSpecification defaultSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(CONFIG.getBaseUrl())
                .addHeader("Authorization", "Bearer " + CONFIG.getApiKey())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .setConfig(baseConfig())
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }

    /**
     * Unauthenticated spec — used for negative auth tests.
     */
    public static RequestSpecification unauthenticatedSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(CONFIG.getBaseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .setConfig(baseConfig())
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }

    /**
     * Spec with a custom / invalid token — used for authorization tests.
     */
    public static RequestSpecification invalidTokenSpec(String token) {
        return new RequestSpecBuilder()
                .setBaseUri(CONFIG.getBaseUrl())
                .addHeader("Authorization", "Bearer " + token)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .setConfig(baseConfig())
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }

    /**
     * Multipart spec for image-upload endpoints (Trees / Forestry).
     * Must NOT set Content-Type — RestAssured sets the multipart boundary automatically.
     */
    public static RequestSpecification multipartSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(CONFIG.getBaseUrl())
                .addHeader("Authorization", "Bearer " + CONFIG.getApiKey())
                .setConfig(baseConfig())
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }

    /**
     * Unauthenticated multipart spec — for negative auth tests on upload endpoints.
     */
    public static RequestSpecification unauthenticatedMultipartSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(CONFIG.getBaseUrl())
                .setConfig(baseConfig())
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }

    /**
     * Invalid-token multipart spec — for negative auth tests on upload endpoints.
     */
    public static RequestSpecification invalidTokenMultipartSpec(String token) {
        return new RequestSpecBuilder()
                .setBaseUri(CONFIG.getBaseUrl())
                .addHeader("Authorization", "Bearer " + token)
                .setConfig(baseConfig())
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }
}
