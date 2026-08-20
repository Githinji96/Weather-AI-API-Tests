package weather;

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
 * Response schema validation for GET /v1/weather.
 *
 * Covers two confirmed live response shapes:
 *   - days=7, ai=false  (ResponseSchema)
 *   - days=3, ai=false  (ResponseSchemaDays3)
 *
 * All field assertions validated against actual API responses.
 */
@Epic("Weather")
@Feature("GET /v1/weather — Schema")
@DisplayName("Weather Response Schema Tests")
class WeatherSchemaTests {

    private static final WeatherClient client = new WeatherClient();

    // ═══════════════════════════════════════════════════════
    // Schema — days=7, ai=false
    // ═══════════════════════════════════════════════════════

    private Response fetchDays7() {
        return client.getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI, 7, false, TestData.UNITS_METRIC, TestData.LANG_EN);
    }

    @Test @Story("days=7 — units") @Description("units must be 'metric'") @DisplayName("[days=7] units = metric")
    void days7_units_isMetric() {
        Response r = fetchDays7();
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.jsonPath().getString("units")).isEqualTo("metric");
    }

    @Test @Story("days=7 — days") @Description("days must equal 7") @DisplayName("[days=7] days = 7")
    void days7_days_isSeven() {
        Response r = fetchDays7();
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat((Integer) r.jsonPath().get("days")).isEqualTo(7);
    }

    @Test @Story("days=7 — daily has 7 entries") @Description("daily array must have 7 entries") @DisplayName("[days=7] daily has 7 entries")
    void days7_daily_hasSevenEntries() {
        Response r = fetchDays7();
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.jsonPath().getList("daily")).hasSize(7);
    }

    @Test @Story("days=7 — current.interval = 900") @Description("interval must be 900s") @DisplayName("[days=7] current.interval = 900")
    void days7_current_intervalIs900() {
        Response r = fetchDays7();
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat((Integer) r.jsonPath().get("current.interval")).isEqualTo(900);
    }

    @Test @Story("days=7 — current.temperature plausible") @Description("temperature in −90 to 60") @DisplayName("[days=7] current.temperature is plausible")
    void days7_current_temperaturePlausible() {
        Response r = fetchDays7();
        assertThat(r.statusCode()).isEqualTo(200);
        Float temp = r.jsonPath().get("current.temperature");
        assertThat(temp).isNotNull();
        assertThat(temp.doubleValue()).isBetween(-90.0, 60.0);
    }

    @Test @Story("days=7 — current.windspeed >= 0") @Description("windspeed must be non-negative") @DisplayName("[days=7] current.windspeed >= 0")
    void days7_current_windspeedNonNegative() {
        Response r = fetchDays7();
        assertThat(r.statusCode()).isEqualTo(200);
        Number ws = r.jsonPath().get("current.windspeed");
        assertThat(ws).isNotNull();
        assertThat(ws.doubleValue()).isGreaterThanOrEqualTo(0.0);
    }

    @Test @Story("days=7 — current.winddirection 0–360") @Description("winddirection 0–360") @DisplayName("[days=7] current.winddirection in 0–360")
    void days7_current_winddirectionInRange() {
        Response r = fetchDays7();
        assertThat(r.statusCode()).isEqualTo(200);
        Integer dir = r.jsonPath().get("current.winddirection");
        assertThat(dir).isNotNull().isBetween(0, 360);
    }

    @Test @Story("days=7 — current.is_day") @Description("is_day is 0 or 1") @DisplayName("[days=7] current.is_day is 0 or 1")
    void days7_current_isDayBinary() {
        Response r = fetchDays7();
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat((Integer) r.jsonPath().get("current.is_day")).isNotNull().isIn(0, 1);
    }

    @Test @Story("days=7 — current.weathercode >= 0") @Description("weathercode >= 0") @DisplayName("[days=7] current.weathercode >= 0")
    void days7_current_weathercodeNonNegative() {
        Response r = fetchDays7();
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat((Integer) r.jsonPath().get("current.weathercode")).isNotNull().isGreaterThanOrEqualTo(0);
    }

    @Test @Story("days=7 — daily[0].date") @Description("daily[0].date non-blank") @DisplayName("[days=7] daily[0].date is non-blank")
    void days7_daily0_dateNonBlank() {
        Response r = fetchDays7();
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.jsonPath().getString("daily[0].date")).isNotBlank();
    }

    @Test @Story("days=7 — temp_max > temp_min") @Description("temp_max > temp_min") @DisplayName("[days=7] daily[0].temp_max > temp_min")
    void days7_daily0_tempMaxGreaterThanMin() {
        Response r = fetchDays7();
        assertThat(r.statusCode()).isEqualTo(200);
        Number max = r.jsonPath().get("daily[0].temp_max");
        Number min = r.jsonPath().get("daily[0].temp_min");
        assertThat(max).isNotNull();
        assertThat(min).isNotNull();
        assertThat(max.doubleValue()).isGreaterThan(min.doubleValue());
    }

    @Test @Story("days=7 — precipitation >= 0") @Description("precipitation >= 0") @DisplayName("[days=7] daily[0].precipitation >= 0")
    void days7_daily0_precipitationNonNegative() {
        Response r = fetchDays7();
        assertThat(r.statusCode()).isEqualTo(200);
        Float precip = r.jsonPath().get("daily[0].precipitation");
        assertThat(precip).isNotNull();
        assertThat(precip.doubleValue()).isGreaterThanOrEqualTo(0.0);
    }

    @Test @Story("days=7 — hourly non-empty") @Description("hourly array non-empty") @DisplayName("[days=7] hourly array is non-empty")
    void days7_hourly_nonEmpty() {
        Response r = fetchDays7();
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.jsonPath().getList("hourly")).isNotNull().isNotEmpty();
    }

    @Test @Story("days=7 — hourly[0].time") @Description("hourly[0].time non-blank") @DisplayName("[days=7] hourly[0].time is non-blank")
    void days7_hourly0_timeNonBlank() {
        Response r = fetchDays7();
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.jsonPath().getString("hourly[0].time")).isNotBlank();
    }

    @Test @Story("days=7 — hourly[0].temp plausible") @Description("hourly[0].temp plausible") @DisplayName("[days=7] hourly[0].temp is plausible")
    void days7_hourly0_tempPlausible() {
        Response r = fetchDays7();
        assertThat(r.statusCode()).isEqualTo(200);
        Float temp = r.jsonPath().get("hourly[0].temp");
        assertThat(temp).isNotNull();
        assertThat(temp.doubleValue()).isBetween(-90.0, 60.0);
    }

    @Test @Story("days=7 — ai_summary null") @Description("ai_summary null when ai=false") @DisplayName("[days=7] ai_summary is null when ai=false")
    void days7_aiSummary_null() {
        assertThat(fetchDays7().jsonPath().getString("ai_summary")).isNullOrEmpty();
    }

    // ═══════════════════════════════════════════════════════
    // Schema — days=3, ai=false
    // ═══════════════════════════════════════════════════════

    private Response fetchDays3() {
        return client.getWeather(TestData.LAT_NAIROBI, TestData.LON_NAIROBI, 3, false, TestData.UNITS_METRIC, TestData.LANG_EN);
    }

    @Test @Story("days=3 — lat echoed") @Description("lat echoed within 0.001°") @DisplayName("[days=3] lat = -1.2921")
    void days3_lat_isEchoed() {
        Response r = fetchDays3();
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(((Float) r.jsonPath().get("lat")).doubleValue()).isCloseTo(-1.2921, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test @Story("days=3 — lon echoed") @Description("lon echoed within 0.001°") @DisplayName("[days=3] lon = 36.8219")
    void days3_lon_isEchoed() {
        Response r = fetchDays3();
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(((Float) r.jsonPath().get("lon")).doubleValue()).isCloseTo(36.8219, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test @Story("days=3 — units") @Description("units must be metric") @DisplayName("[days=3] units = metric")
    void days3_units_isMetric() {
        Response r = fetchDays3();
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.jsonPath().getString("units")).isEqualTo("metric");
    }

    @Test @Story("days=3 — days") @Description("days must equal 3") @DisplayName("[days=3] days = 3")
    void days3_days_isThree() {
        Response r = fetchDays3();
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat((Integer) r.jsonPath().get("days")).isEqualTo(3);
    }

    @Test @Story("days=3 — interval=900") @Description("interval must be 900s") @DisplayName("[days=3] current.interval = 900")
    void days3_current_intervalIs900() {
        Response r = fetchDays3();
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat((Integer) r.jsonPath().get("current.interval")).isEqualTo(900);
    }

    @Test @Story("days=3 — temperature plausible") @Description("temperature in −90 to 60") @DisplayName("[days=3] current.temperature is plausible")
    void days3_current_temperaturePlausible() {
        Response r = fetchDays3();
        assertThat(r.statusCode()).isEqualTo(200);
        Float temp = r.jsonPath().get("current.temperature");
        assertThat(temp).isNotNull();
        assertThat(temp.doubleValue()).isBetween(-90.0, 60.0);
    }

    @Test @Story("days=3 — daily has 3 entries") @Description("daily must have 3 entries") @DisplayName("[days=3] daily has 3 entries")
    void days3_daily_hasThreeEntries() {
        Response r = fetchDays3();
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.jsonPath().getList("daily")).hasSize(3);
    }

    @Test @Story("days=3 — all daily dates non-blank") @Description("all 3 daily dates non-blank") @DisplayName("[days=3] all daily entries have date")
    void days3_allDailyEntries_haveDate() {
        Response r = fetchDays3();
        assertThat(r.statusCode()).isEqualTo(200);
        for (int i = 0; i < 3; i++) {
            assertThat(r.jsonPath().getString("daily[" + i + "].date"))
                    .as("daily[%d].date must be non-blank", i).isNotBlank();
        }
    }

    @Test @Story("days=3 — temp_max > temp_min") @Description("temp_max > temp_min") @DisplayName("[days=3] daily[0].temp_max > temp_min")
    void days3_daily0_tempMaxGreaterThanMin() {
        Response r = fetchDays3();
        assertThat(r.statusCode()).isEqualTo(200);
        Number max = r.jsonPath().get("daily[0].temp_max");
        Number min = r.jsonPath().get("daily[0].temp_min");
        assertThat(max).isNotNull();
        assertThat(min).isNotNull();
        assertThat(max.doubleValue()).isGreaterThan(min.doubleValue());
    }

    @Test @Story("days=3 — hourly non-empty") @Description("hourly non-empty") @DisplayName("[days=3] hourly array is non-empty")
    void days3_hourly_nonEmpty() {
        Response r = fetchDays3();
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.jsonPath().getList("hourly")).isNotNull().isNotEmpty();
    }

    @Test @Story("days=3 — hourly[0].time") @Description("hourly[0].time non-blank") @DisplayName("[days=3] hourly[0].time is non-blank")
    void days3_hourly0_timeNonBlank() {
        Response r = fetchDays3();
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.jsonPath().getString("hourly[0].time")).isNotBlank();
    }

    @Test @Story("days=3 — ai_summary null") @Description("ai_summary null when ai=false") @DisplayName("[days=3] ai_summary is null when ai=false")
    void days3_aiSummary_null() {
        assertThat(fetchDays3().jsonPath().getString("ai_summary")).isNullOrEmpty();
    }
}
