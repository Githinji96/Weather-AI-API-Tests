package common;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.example.clients.WeatherClient;
import org.example.utils.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Input validation behaviour for GET /v1/weather.
 *
 * Observed API behaviour (empirically confirmed):
 *  - Missing lat/lon         → 400  (enforced)
 *  - Invalid coordinates     → 400 or 502 (depends on upstream provider)
 *  - Unrecognised units      → 200  (silently ignored, defaults to metric)
 *  - days=0 or days>max      → 200  (silently capped/defaulted, not rejected)
 */
@Epic("Common")
@Feature("Validation")
@DisplayName("Input Validation Tests")
class ValidationTests {

    private static final WeatherClient client = new WeatherClient();

    // ── Missing required parameters → 400 ───────────────────────────────────

    @Test
    @Story("Missing lat")
    @Description("Omitting the lat parameter must return 400 Bad Request")
    @DisplayName("Missing lat returns 400")
    void missingLat_returns400() {
        Response r = io.restassured.RestAssured
                .given(org.example.config.RequestSpecConfig.defaultSpec())
                .queryParam("lon", TestData.LON_NAIROBI)
                .get("/v1/weather");
        assertThat(r.statusCode()).isEqualTo(400);
    }

    @Test
    @Story("Missing lon")
    @Description("Omitting the lon parameter must return 400 Bad Request")
    @DisplayName("Missing lon returns 400")
    void missingLon_returns400() {
        Response r = io.restassured.RestAssured
                .given(org.example.config.RequestSpecConfig.defaultSpec())
                .queryParam("lat", TestData.LAT_NAIROBI)
                .get("/v1/weather");
        assertThat(r.statusCode()).isEqualTo(400);
    }

    @Test
    @Story("Missing lat and lon")
    @Description("Omitting both lat and lon must return 400 Bad Request")
    @DisplayName("Missing lat and lon returns 400")
    void missingLatAndLon_returns400() {
        Response r = io.restassured.RestAssured
                .given(org.example.config.RequestSpecConfig.defaultSpec())
                .get("/v1/weather");
        assertThat(r.statusCode()).isEqualTo(400);
    }

    // ── Out-of-range coordinates → 400 or 502 ───────────────────────────────

    @Test
    @Story("Invalid coordinates")
    @Description("Coordinates far outside valid range return 400 or 502 (upstream provider error)")
    @DisplayName("Invalid lat/lon values return 400 or 502")
    void invalidCoordinates_returns400Or502() {
        Response r = client.getWeather(TestData.LAT_INVALID, TestData.LON_INVALID);
        assertThat(r.statusCode())
                .as("Invalid coordinates: expected 400 (validation) or 502 (upstream error)")
                .isIn(400, 502);
    }

    // ── Optional parameter leniency — API does NOT validate these ────────────

    @Test
    @Story("Invalid units — lenient")
    @Description("API silently ignores unrecognised units and defaults to metric — returns 200")
    @DisplayName("Invalid units value is ignored — returns 200")
    void invalidUnits_apiIsLenient() {
        Response r = client.getWeather(
                TestData.LAT_NAIROBI, TestData.LON_NAIROBI,
                7, true, TestData.UNITS_INVALID, TestData.LANG_EN);
        assertThat(r.statusCode())
                .as("API does not validate units — unknown value silently defaults to metric")
                .isEqualTo(200);
        // Confirm the response still applies a valid units value
        String units = r.jsonPath().getString("units");
        assertThat(units).isIn("metric", "imperial");
    }

    @Test
    @Story("days=0 — lenient")
    @Description("API silently ignores days=0 and returns a default forecast — returns 200")
    @DisplayName("days=0 is ignored — returns 200")
    void zeroDays_apiIsLenient() {
        Response r = client.getWeather(
                TestData.LAT_NAIROBI, TestData.LON_NAIROBI,
                TestData.DAYS_INVALID, true, TestData.UNITS_METRIC, TestData.LANG_EN);
        assertThat(r.statusCode())
                .as("API does not reject days=0 — returns default forecast")
                .isEqualTo(200);
    }

    @Test
    @Story("days=17 — lenient")
    @Description("API silently caps days=17 at plan maximum and returns 200")
    @DisplayName("days=17 is capped — returns 200")
    void overMaxDays_apiIsLenient() {
        Response r = client.getWeather(
                TestData.LAT_NAIROBI, TestData.LON_NAIROBI,
                TestData.DAYS_OVER_MAX, true, TestData.UNITS_METRIC, TestData.LANG_EN);
        assertThat(r.statusCode())
                .as("API does not reject days=17 — caps at plan maximum and returns 200")
                .isEqualTo(200);
    }
}
