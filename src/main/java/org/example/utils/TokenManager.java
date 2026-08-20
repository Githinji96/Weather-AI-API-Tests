package org.example.utils;

import org.example.config.ApiConfig;

/**
 * Centralises all token / key helpers used across test classes.
 *
 * Provides the valid API key as well as a set of invalid variants for
 * negative authentication and authorisation scenarios.
 */
public class TokenManager {

    private static final ApiConfig CONFIG = ApiConfig.getInstance();

    private TokenManager() {}

    /** Returns the valid API key loaded from config / environment. */
    public static String validToken() {
        return CONFIG.getApiKey();
    }

    /** An obviously malformed token — not prefixed with {@code wai_}. */
    public static String malformedToken() {
        return "invalid_token_12345";
    }

    /** An empty string — simulates missing Authorization header value. */
    public static String emptyToken() {
        return "";
    }

    /** A well-formed but revoked / nonexistent key. */
    public static String revokedToken() {
        return "wai_revoked_key_000000000000000000000000000000000000";
    }

    /**
     * Returns the Bearer header value for a given raw token.
     * Convenience wrapper for building header strings in tests.
     */
    public static String bearerHeader(String token) {
        return "Bearer " + token;
    }
}
