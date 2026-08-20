package common;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.example.clients.WeatherClient;
import org.example.config.RequestSpecConfig;
import org.example.utils.TestData;
import org.example.utils.TokenManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Common")
@Feature("Authentication")
@DisplayName("Authentication Tests")
class AuthenticationTests {

    // ── 200 — valid key ──────────────────────────────────────────────────────

    @Test
    @Story("Valid authentication")
    @Description("A request with a valid Bearer token should return 200")
    @DisplayName("Valid API key returns 200")
    void validApiKey_returns200() {
        Response response = new WeatherClient()
                .getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI);

        assertThat(response.statusCode()).isEqualTo(200);
    }

    // ── 401 — missing / malformed / revoked key ──────────────────────────────

    @Test
    @Story("Missing token")
    @Description("A request without an Authorization header should return 401")
    @DisplayName("No Authorization header returns 401")
    void noAuthHeader_returns401() {
        Response response = new WeatherClient(RequestSpecConfig.unauthenticatedSpec())
                .getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI);

        assertThat(response.statusCode()).isEqualTo(401);
    }

    @Test
    @Story("Malformed token")
    @Description("A token not prefixed with 'wai_' should return 401")
    @DisplayName("Malformed token returns 401")
    void malformedToken_returns401() {
        Response response = new WeatherClient(
                RequestSpecConfig.invalidTokenSpec(TokenManager.malformedToken()))
                .getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI);

        assertThat(response.statusCode()).isEqualTo(401);
    }

    @Test
    @Story("Revoked token")
    @Description("A well-formed but non-existent key should return 401")
    @DisplayName("Revoked / non-existent token returns 401")
    void revokedToken_returns401() {
        Response response = new WeatherClient(
                RequestSpecConfig.invalidTokenSpec(TokenManager.revokedToken()))
                .getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI);

        assertThat(response.statusCode()).isEqualTo(401);
    }

    @Test
    @Story("Empty token")
    @Description("An empty Bearer token value should return 401")
    @DisplayName("Empty token returns 401")
    void emptyToken_returns401() {
        Response response = new WeatherClient(
                RequestSpecConfig.invalidTokenSpec(TokenManager.emptyToken()))
                .getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI);

        assertThat(response.statusCode()).isEqualTo(401);
    }

    // ── Response body on 401 ─────────────────────────────────────────────────

    @Test
    @Story("401 error body")
    @Description("401 response should contain an error message in the body")
    @DisplayName("401 response includes error message")
    void unauthorizedResponse_containsErrorMessage() {
        Response response = new WeatherClient(RequestSpecConfig.unauthenticatedSpec())
                .getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI);

        // /v1/weather is available on all plans so unauthenticated always returns 401
        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.body().asString()).isNotBlank();
    }
}
