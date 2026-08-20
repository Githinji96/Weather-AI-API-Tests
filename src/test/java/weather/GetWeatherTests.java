package weather;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.assertj.core.data.Offset;
import org.example.clients.WeatherClient;
import org.example.config.RequestSpecConfig;
import org.example.utils.TestData;
import org.example.utils.TokenManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for GET /v1/weather — current conditions + forecast [ALL PLANS]
 *
 * Response shape:
 * { "lat", "lon", "units", "days",
 *   "current": { "time", "interval", "temperature", "windspeed",
 *                "winddirection", "is_day", "weathercode" },
 *   "daily":  [{ "date", "temp_max", "temp_min", "precipitation", "weathercode" }],
 *   "hourly": [{ "time", "temp", "precipitation", "weathercode" }],
 *   "ai_summary": string | null }
 */
@Epic("Weather")
@Feature("GET /v1/weather")
@DisplayName("GET /v1/weather Tests")
class GetWeatherTests {

    private static final WeatherClient client = new WeatherClient();

    // ── HTTP / transport ─────────────────────────────────────────────────────

    @Test @Story("Status 200") @Description("Valid coordinates return 200") @DisplayName("Valid request returns 200")
    void validRequest_returns200() {
        assertThat(client.getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI).statusCode()).isEqualTo(200);
    }

    @Test @Story("Content type") @Description("Response must be application/json") @DisplayName("Response is JSON")
    void response_isJson() {
        Response r = client.getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI);
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.contentType()).containsIgnoringCase("application/json");
    }

    @Test @Story("Non-empty body") @Description("Response body must not be blank") @DisplayName("Response body is non-empty")
    void response_bodyNotEmpty() {
        Response r = client.getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI);
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.body().asString()).isNotBlank();
    }

    // ── Top-level fields ─────────────────────────────────────────────────────

    @Test @Story("lat echoed") @Description("Response echoes lat") @DisplayName("lat is echoed")
    void topLevel_latEchoed() {
        Response r = client.getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI);
        assertThat(r.statusCode()).isEqualTo(200);
        Float lat = r.jsonPath().get("lat");
        assertThat(lat).isNotNull();
        assertThat(lat.doubleValue()).isCloseTo(TestData.LAT_NAIROBI, Offset.offset(0.001));
    }

    @Test @Story("lon echoed") @Description("Response echoes lon") @DisplayName("lon is echoed")
    void topLevel_lonEchoed() {
        Response r = client.getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI);
        assertThat(r.statusCode()).isEqualTo(200);
        Float lon = r.jsonPath().get("lon");
        assertThat(lon).isNotNull();
        assertThat(lon.doubleValue()).isCloseTo(TestData.LON_NAIROBI, Offset.offset(0.001));
    }

    @Test @Story("units=metric echoed") @Description("units=metric echoed") @DisplayName("units=metric is echoed")
    void topLevel_unitsMetricEchoed() {
        Response r = client.getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI, 3, true, TestData.UNITS_METRIC, TestData.LANG_EN);
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.jsonPath().getString("units")).isEqualTo("metric");
    }

    @Test @Story("units=imperial echoed") @Description("units=imperial echoed") @DisplayName("units=imperial is echoed")
    void topLevel_unitsImperialEchoed() {
        Response r = client.getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI, 3, true, TestData.UNITS_IMPERIAL, TestData.LANG_EN);
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.jsonPath().getString("units")).isEqualTo("imperial");
    }

    @Test @Story("days echoed") @Description("days=3 echoed") @DisplayName("days=3 is echoed")
    void topLevel_daysEchoed() {
        Response r = client.getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI, 3, true, TestData.UNITS_METRIC, TestData.LANG_EN);
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat((Integer) r.jsonPath().get("days")).isEqualTo(3);
    }

    @Test @Story("current present") @Description("current object present") @DisplayName("current object is present")
    void topLevel_currentPresent() {
        Response r = client.getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI);
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat((Object) r.jsonPath().get("current")).isNotNull();
    }

    @Test @Story("daily present") @Description("daily array present") @DisplayName("daily array is non-empty")
    void topLevel_dailyPresent() {
        Response r = client.getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI);
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.jsonPath().getList("daily")).isNotNull().isNotEmpty();
    }

    // ── current{} ────────────────────────────────────────────────────────────

    @Test @Story("current.time") @Description("current.time is non-blank") @DisplayName("current.time is non-blank")
    void current_timePresent() {
        Response r = client.getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI);
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.jsonPath().getString("current.time")).isNotBlank();
    }

    @Test @Story("current.interval") @Description("current.interval > 0") @DisplayName("current.interval is positive")
    void current_intervalPositive() {
        Response r = client.getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI);
        assertThat(r.statusCode()).isEqualTo(200);
        Integer interval = r.jsonPath().get("current.interval");
        assertThat(interval).isNotNull().isGreaterThan(0);
    }

    @Test @Story("current.temperature") @Description("temperature in −90 to 60") @DisplayName("current.temperature is plausible")
    void current_temperaturePlausible() {
        Response r = client.getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI);
        assertThat(r.statusCode()).isEqualTo(200);
        Float temp = r.jsonPath().get("current.temperature");
        assertThat(temp).isNotNull();
        assertThat(temp.doubleValue()).isBetween(-90.0, 60.0);
    }

    @Test @Story("current.windspeed") @Description("windspeed >= 0") @DisplayName("current.windspeed is non-negative")
    void current_windspeedNonNegative() {
        Response r = client.getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI);
        assertThat(r.statusCode()).isEqualTo(200);
        Number ws = r.jsonPath().get("current.windspeed");
        assertThat(ws).isNotNull();
        assertThat(ws.doubleValue()).isGreaterThanOrEqualTo(0.0);
    }

    @Test @Story("current.winddirection") @Description("winddirection 0–360") @DisplayName("current.winddirection is 0–360")
    void current_winddirectionValid() {
        Response r = client.getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI);
        assertThat(r.statusCode()).isEqualTo(200);
        Integer dir = r.jsonPath().get("current.winddirection");
        assertThat(dir).isNotNull().isBetween(0, 360);
    }

    @Test @Story("current.is_day") @Description("is_day is 0 or 1") @DisplayName("current.is_day is 0 or 1")
    void current_isDayBinary() {
        Response r = client.getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI);
        assertThat(r.statusCode()).isEqualTo(200);
        Integer isDay = r.jsonPath().get("current.is_day");
        assertThat(isDay).isNotNull().isIn(0, 1);
    }

    @Test @Story("current.weathercode") @Description("weathercode >= 0") @DisplayName("current.weathercode is non-negative")
    void current_weathercodeNonNegative() {
        Response r = client.getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI);
        assertThat(r.statusCode()).isEqualTo(200);
        Integer code = r.jsonPath().get("current.weathercode");
        assertThat(code).isNotNull().isGreaterThanOrEqualTo(0);
    }

    // ── daily[] ───────────────────────────────────────────────────────────────

    @Test @Story("daily length") @Description("daily length matches days=3") @DisplayName("daily array length matches days")
    void daily_lengthMatchesDays() {
        Response r = client.getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI, 3, true, TestData.UNITS_METRIC, TestData.LANG_EN);
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.jsonPath().getList("daily")).hasSize(3);
    }

    @Test @Story("daily[0].date") @Description("daily uses 'date' field") @DisplayName("daily[0].date is non-blank")
    void daily_firstEntryHasDate() {
        Response r = client.getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI);
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.jsonPath().getString("daily[0].date")).isNotBlank();
    }

    // ── AI summary ────────────────────────────────────────────────────────────

    @Test @Story("ai=true includes ai_summary") @Description("ai=true includes ai_summary key") @DisplayName("ai=true includes ai_summary")
    void aiEnabled_summaryKeyPresent() {
        Response r = client.getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI, 3, true, TestData.UNITS_METRIC, TestData.LANG_EN);
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.body().asString()).containsIgnoringCase("ai_summary");
    }

    @Test @Story("ai=false — no summary") @Description("ai=false yields null ai_summary") @DisplayName("ai=false: ai_summary is null")
    void aiDisabled_summaryNullOrAbsent() {
        Response r = client.getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI, 3, false, TestData.UNITS_METRIC, TestData.LANG_EN);
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.jsonPath().getString("ai_summary")).isNullOrEmpty();
    }

    // ── Parameter variants ────────────────────────────────────────────────────

    @Test @Story("Mombasa") @Description("Mombasa coords echoed") @DisplayName("Mombasa coordinates echoed")
    void mombasa_coordsEchoed() {
        Response r = client.getWeather(TestData.LAT_MOMBASA, TestData.LON_MOMBASA);
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(((Float) r.jsonPath().get("lat")).doubleValue()).isCloseTo(TestData.LAT_MOMBASA, Offset.offset(0.001));
        assertThat(((Float) r.jsonPath().get("lon")).doubleValue()).isCloseTo(TestData.LON_MOMBASA, Offset.offset(0.001));
    }

    @Test @Story("lang=sw") @Description("lang=sw returns 200") @DisplayName("lang=sw returns 200")
    void langSwahili_returns200() {
        assertThat(client.getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI, 3, true, TestData.UNITS_METRIC, TestData.LANG_SW).statusCode()).isEqualTo(200);
    }

    @Test @Story("days=1") @Description("days=1 returns 1-entry daily") @DisplayName("days=1 returns 1-entry daily array")
    void daysMin_singleDailyEntry() {
        Response r = client.getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI, 1, true, TestData.UNITS_METRIC, TestData.LANG_EN);
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.jsonPath().getList("daily")).hasSize(1);
    }

    @Test @Story("days=7") @Description("days=7 returns 7-entry daily") @DisplayName("days=7 returns 7-entry daily array")
    void daysFreePlanMax_sevenDailyEntries() {
        Response r = client.getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI, 7, true, TestData.UNITS_METRIC, TestData.LANG_EN);
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.jsonPath().getList("daily")).hasSize(7);
    }

    // ── Error cases ───────────────────────────────────────────────────────────

    @Test @Story("Missing lat → 400") @Description("Missing lat returns 400") @DisplayName("Missing lat returns 400")
    void missingLat_returns400() {
        assertThat(given(RequestSpecConfig.defaultSpec()).queryParam("lon", TestData.LON_NAIROBI).get("/v1/weather").statusCode()).isEqualTo(400);
    }

    @Test @Story("Missing lon → 400") @Description("Missing lon returns 400") @DisplayName("Missing lon returns 400")
    void missingLon_returns400() {
        assertThat(given(RequestSpecConfig.defaultSpec()).queryParam("lat", TestData.LAT_NAIROBI).get("/v1/weather").statusCode()).isEqualTo(400);
    }

    @Test @Story("Missing both → 400") @Description("Missing lat+lon returns 400") @DisplayName("Missing lat and lon returns 400")
    void missingBothParams_returns400() {
        assertThat(given(RequestSpecConfig.defaultSpec()).get("/v1/weather").statusCode()).isEqualTo(400);
    }

    @Test @Story("Invalid coords → 400 or 502") @Description("Invalid coords return 400 or 502") @DisplayName("Invalid coordinates return 400 or 502")
    void invalidCoordinates_returns400Or502() {
        assertThat(client.getWeather(TestData.LAT_INVALID, TestData.LON_INVALID).statusCode()).isIn(400, 502);
    }

    @Test @Story("No auth → 401") @Description("No auth returns 401") @DisplayName("No auth returns 401")
    void noAuth_returns401() {
        assertThat(new WeatherClient(RequestSpecConfig.unauthenticatedSpec()).getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI).statusCode()).isEqualTo(401);
    }

    @Test @Story("Revoked token → 401") @Description("Revoked token returns 401") @DisplayName("Revoked token returns 401")
    void revokedToken_returns401() {
        assertThat(new WeatherClient(RequestSpecConfig.invalidTokenSpec(TokenManager.revokedToken())).getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI).statusCode()).isEqualTo(401);
    }

    @Test @Story("400 body is JSON") @Description("400 error is non-empty JSON") @DisplayName("400 error response is non-empty JSON")
    void errorResponse_400_isJson() {
        Response r = given(RequestSpecConfig.defaultSpec()).get("/v1/weather");
        assertThat(r.statusCode()).isEqualTo(400);
        assertThat(r.contentType()).containsIgnoringCase("application/json");
        assertThat(r.body().asString()).isNotBlank();
    }

    @Test @Story("No 5xx") @Description("Valid request returns no 5xx") @DisplayName("Valid request does not return 5xx")
    void validRequest_no5xx() {
        assertThat(client.getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI).statusCode()).isNotIn(500, 503);
    }
}
