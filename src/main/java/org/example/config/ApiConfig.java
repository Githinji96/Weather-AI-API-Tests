package org.example.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads configuration from config.properties (classpath) and allows
 * environment-variable overrides for CI/CD pipelines.
 *
 * <p>Priority: environment variable > config.properties > hard-coded default.
 */
public class ApiConfig {

    private static final ApiConfig INSTANCE = new ApiConfig();

    private final Properties props = new Properties();

    private ApiConfig() {
        try (InputStream in = ApiConfig.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    public static ApiConfig getInstance() {
        return INSTANCE;
    }

    // ── Convenience getters ──────────────────────────────────────────────────

    public String getBaseUrl() {
        return resolve("base.url", "https://api.weather-ai.co");
    }

    /**
     * Returns the API key, preferring the WAI_API_KEY environment variable
     * so that secrets are never committed to source control.
     *
     * Priority: WAI_API_KEY env var → config.properties api.key → error
     *
     * @throws IllegalStateException if no API key is configured at all
     */
    public String getApiKey() {
        // 1. Environment variable (recommended — use this in CI and locally)
        String envKey = System.getenv("WAI_API_KEY");
        if (envKey != null && !envKey.isBlank()) {
            return envKey;
        }

        // 2. config.properties (local development fallback — gitignored file)
        String propKey = resolve("api.key", "");
        if (!propKey.isBlank()) {
            return propKey;
        }

        // 3. Neither is set — fail fast with a clear message
        throw new IllegalStateException(
            "\n\n  *** No API key configured ***\n" +
            "  Set the WAI_API_KEY environment variable before running tests:\n" +
            "    Windows CMD:        set WAI_API_KEY=wai_your_key_here\n" +
            "    Windows PowerShell: $env:WAI_API_KEY = \"wai_your_key_here\"\n" +
            "    Linux/macOS:        export WAI_API_KEY=wai_your_key_here\n" +
            "  Get your key from: https://weather-ai.co (Dashboard → API Keys)\n");
    }

    public double getDefaultLat() {
        return Double.parseDouble(resolve("default.lat", "-1.2921"));
    }

    public double getDefaultLon() {
        return Double.parseDouble(resolve("default.lon", "36.8219"));
    }

    public int getConnectTimeout() {
        return Integer.parseInt(resolve("connect.timeout", "10000"));
    }

    public int getReadTimeout() {
        return Integer.parseInt(resolve("read.timeout", "30000"));
    }

    /**
     * Classpath path to the primary forestry test image.
     * Resolves to src/main/resources/images/sample-farm.jpg by default.
     */
    public String getForestryImagePrimary() {
        return resolve("forestry.image.primary", "images/sample-farm.jpg");
    }

    /**
     * Classpath path to the fallback forestry test image.
     * Resolves to src/main/resources/images/sample-farm.png by default.
     */
    public String getForestryImageFallback() {
        return resolve("forestry.image.fallback", "images/sample-farm.png");
    }

    // ── Internal helper ──────────────────────────────────────────────────────

    private String resolve(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }
}
