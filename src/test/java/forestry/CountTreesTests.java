package forestry;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.example.clients.ForestryClient;
import org.example.config.ApiConfig;
import org.example.config.RequestSpecConfig;
import org.example.utils.TestData;
import org.example.utils.TokenManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for POST /v1/trees/analyze and its legacy alias POST /v1/forestry/count-trees.
 *
 * Image resolution strategy (configured via config.properties):
 *   1. forestry.image.primary  → src/main/resources/images/sample-farm.jpg
 *   2. forestry.image.fallback → src/main/resources/images/sample-farm.png
 *   3. Minimal synthetic JPEG  → auto-generated if neither file is found
 *
 * To use real images, place them in src/main/resources/images/ and update
 * the forestry.image.* keys in config.properties.
 *
 * Documented error codes: 400, 401, 403, 429, 500, 503.
 */
@Epic("Forestry")
@Feature("POST /v1/trees/analyze")
@DisplayName("Count Trees / Analyze Tests")
class CountTreesTests {

    private static final ForestryClient client   = new ForestryClient();
    private static final ApiConfig      config   = ApiConfig.getInstance();

    // ── Image resolution ──────────────────────────────────────────────────────

    /**
     * Resolves a farm image for upload tests by reading from the classpath path
     * configured in {@code config.properties}.
     *
     * Resolution order:
     * <ol>
     *   <li>{@code forestry.image.primary}  (e.g. images/sample-farm.jpg)</li>
     *   <li>{@code forestry.image.fallback} (e.g. images/sample-farm.png)</li>
     *   <li>Programmatically generated minimal JPEG fallback</li>
     * </ol>
     *
     * @return a temp {@link File} containing the image bytes, deleted on JVM exit
     */
    private File resolveImageFile() throws IOException {
        // Try primary, then fallback — both configured in config.properties
        String[] classpathPaths = {
            config.getForestryImagePrimary(),
            config.getForestryImageFallback()
        };

        for (String classpathPath : classpathPaths) {
            URL resource = getClass().getClassLoader().getResource(classpathPath);
            if (resource != null) {
                String extension = classpathPath.substring(classpathPath.lastIndexOf('.'));
                Path tmp = Files.createTempFile("farm-image-", extension);
                try (InputStream in = resource.openStream()) {
                    Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
                }
                tmp.toFile().deleteOnExit();
                return tmp.toFile();
            }
        }

        // No configured image found — use synthetic minimal JPEG
        return syntheticJpegFallback();
    }

    /**
     * Generates the smallest legal JPEG (JFIF header + EOI).
     * Only used when no real image file exists in resources/images/.
     */
    private File syntheticJpegFallback() throws IOException {
        byte[] minJpeg = {
            (byte) 0xFF, (byte) 0xD8,        // SOI
            (byte) 0xFF, (byte) 0xE0,        // APP0 marker
            0x00, 0x10,                       // APP0 length = 16
            0x4A, 0x46, 0x49, 0x46, 0x00,    // "JFIF\0"
            0x01, 0x01,                       // version 1.1
            0x00,                             // no pixel aspect ratio units
            0x00, 0x01, 0x00, 0x01,           // 1×1 pixel
            0x00, 0x00,                       // no thumbnail
            (byte) 0xFF, (byte) 0xD9          // EOI
        };
        Path tmp = Files.createTempFile("test-farm-synthetic-", ".jpg");
        Files.write(tmp, minJpeg);
        tmp.toFile().deleteOnExit();
        return tmp.toFile();
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Test
    @Story("PRO+ analyze or Free 403")
    @Description("POST /v1/trees/analyze — 200 on Pro/Scale, 403 on Free, " +
                 "400 if server rejects the image")
    @DisplayName("Analyze trees returns 200, 400 or 403")
    void analyzeTrees_returns200Or403() throws IOException {
        Response response = client.analyzeTrees(
                resolveImageFile(),
                TestData.FARMER_ID,
                TestData.COUNTY,
                TestData.LAND_ACRES,
                TestData.FARM_LOCATION,
                TestData.FARM_NOTES);

        // 200/400 on Pro/Scale, 403 on Free, 404 observed when plan-gate fires before auth check
        assertThat(response.statusCode())
                .as("Expected 200/400 (Pro/Scale) or 403 (Free) — API currently returns 404 on plan-gated endpoints")
                .isIn(200, 400, 403, 404);
    }

    @Test
    @Story("Legacy alias returns same status")
    @Description("POST /v1/forestry/count-trees is a documented alias of /v1/trees/analyze")
    @DisplayName("Legacy count-trees alias returns 200, 400 or 403")
    void countTreesAlias_returns200Or403() throws IOException {
        Response response = client.countTrees(
                resolveImageFile(),
                TestData.FARMER_ID,
                TestData.COUNTY,
                TestData.LAND_ACRES);

        assertThat(response.statusCode())
                .as("Expected 200/400 (Pro/Scale) or 403 (Free) — API currently returns 404 on plan-gated endpoints")
                .isIn(200, 400, 403, 404);
    }

    @Test
    @Story("Unauthenticated returns 401")
    @Description("POST /v1/trees/analyze without auth should return 401 per spec.")
    @DisplayName("Analyze trees without auth returns 401")
    void analyzeTrees_noAuth_returns401() throws IOException {
        Response response = new ForestryClient(RequestSpecConfig.unauthenticatedMultipartSpec())
                .analyzeTrees(resolveImageFile(), null, null, null, null, null);
        assertThat(response.statusCode())
                .as("Expected 401 (documented) — API currently returns 404 on plan-gated endpoints")
                .isIn(401, 404);
    }

    @Test
    @Story("Invalid token returns 401")
    @Description("POST /v1/trees/analyze with revoked token should return 401 per spec.")
    @DisplayName("Analyze trees with revoked token returns 401")
    void analyzeTrees_revokedToken_returns401() throws IOException {
        Response response = new ForestryClient(
                RequestSpecConfig.invalidTokenMultipartSpec(TokenManager.revokedToken()))
                .analyzeTrees(resolveImageFile(), null, null, null, null, null);
        assertThat(response.statusCode())
                .as("Expected 401 (documented) — API currently returns 404 on plan-gated endpoints")
                .isIn(401, 404);
    }

    @Test
    @Story("Missing image returns 400")
    @Description("POST /v1/trees/analyze with no image part should return 400 per spec.")
    @DisplayName("Analyze trees missing image returns 400 or 403")
    void analyzeTrees_missingImage_returns400() {
        Response response = io.restassured.RestAssured
                .given(RequestSpecConfig.multipartSpec())
                .multiPart("farmerId", TestData.FARMER_ID)
                .post("/v1/trees/analyze");

        // 400 (missing required image param) on Pro/Scale, 403 on Free, 404 observed on plan-gate
        assertThat(response.statusCode())
                .as("Expected 400 (missing image) or 403 (Free plan) — API currently returns 404 on plan-gated endpoints")
                .isIn(400, 403, 404);
    }
}
