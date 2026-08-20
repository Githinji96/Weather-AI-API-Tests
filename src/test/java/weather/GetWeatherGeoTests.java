package weather;

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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for GET /v1/weather-geo — weather + IP geo-detection [ALL PLANS]
 *
 * Confirmed response shape includes a geo{} object:
 * { "ip", "ip_version", "lat", "lon", "city", "region",
 *   "country" (2-letter), "timezone", "is_datacenter" }
 */
@Epic("Weather")
@Feature("GET /v1/weather-geo")
@DisplayName("GET /v1/weather-geo Tests")
class GetWeatherGeoTests {

    private static final WeatherClient client = new WeatherClient();

    @Test
    @Story("Auto IP detection")
    @Description("No params — API auto-detects location from caller IP")
    @DisplayName("weather-geo auto-detect returns 200")
    void autoDetect_returns200() {
        assertThat(client.getWeatherGeo().statusCode()).isEqualTo(200);
    }

    @Test
    @Story("Explicit coords")
    @Description("Explicit lat/lon returns 200")
    @DisplayName("weather-geo with explicit coords returns 200")
    void withCoords_returns200() {
        assertThat(client.getWeatherGeo(TestData.LAT_NAIROBI, TestData.LON_NAIROBI).statusCode()).isEqualTo(200);
    }

    @Test
    @Story("JSON")
    @Description("Response must be application/json")
    @DisplayName("weather-geo response is JSON")
    void isJson() {
        Response r = client.getWeatherGeo();
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.contentType()).containsIgnoringCase("application/json");
    }

    // ── geo{} schema ─────────────────────────────────────────────────────────

    @Test @Story("geo object present") @Description("Response contains a non-null geo object") @DisplayName("geo object is present")
    void geo_objectPresent() {
        Response r = client.getWeatherGeo();
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat((Object) r.jsonPath().get("geo")).isNotNull();
    }

    @Test @Story("geo.ip non-blank") @Description("geo.ip must be non-blank") @DisplayName("geo.ip is non-blank")
    void geo_ipNonBlank() {
        Response r = client.getWeatherGeo();
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.jsonPath().getString("geo.ip")).isNotBlank();
    }

    @Test @Story("geo.ip_version") @Description("geo.ip_version must be v4 or v6") @DisplayName("geo.ip_version is v4 or v6")
    void geo_ipVersionValid() {
        Response r = client.getWeatherGeo();
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.jsonPath().getString("geo.ip_version")).isIn("v4", "v6");
    }

    @Test @Story("geo.lat range") @Description("geo.lat must be −90 to 90") @DisplayName("geo.lat is in −90 to 90")
    void geo_latInRange() {
        Response r = client.getWeatherGeo();
        assertThat(r.statusCode()).isEqualTo(200);
        Float lat = r.jsonPath().get("geo.lat");
        assertThat(lat).isNotNull();
        assertThat(lat.doubleValue()).isBetween(-90.0, 90.0);
    }

    @Test @Story("geo.lon range") @Description("geo.lon must be −180 to 180") @DisplayName("geo.lon is in −180 to 180")
    void geo_lonInRange() {
        Response r = client.getWeatherGeo();
        assertThat(r.statusCode()).isEqualTo(200);
        Float lon = r.jsonPath().get("geo.lon");
        assertThat(lon).isNotNull();
        assertThat(lon.doubleValue()).isBetween(-180.0, 180.0);
    }

    @Test @Story("geo.city non-blank") @Description("geo.city must be non-blank") @DisplayName("geo.city is non-blank")
    void geo_cityNonBlank() {
        Response r = client.getWeatherGeo();
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.jsonPath().getString("geo.city")).isNotBlank();
    }

    @Test @Story("geo.country 2-letter") @Description("geo.country must be 2-letter ISO code") @DisplayName("geo.country is 2-letter code")
    void geo_countryIsTwoLetters() {
        Response r = client.getWeatherGeo();
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.jsonPath().getString("geo.country")).isNotBlank().hasSize(2).matches("[A-Z]{2}");
    }

    @Test @Story("geo.timezone non-blank") @Description("geo.timezone must be non-blank IANA string") @DisplayName("geo.timezone is non-blank")
    void geo_timezoneNonBlank() {
        Response r = client.getWeatherGeo();
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.jsonPath().getString("geo.timezone")).isNotBlank();
    }

    @Test @Story("geo.is_datacenter boolean") @Description("geo.is_datacenter must be true or false") @DisplayName("geo.is_datacenter is boolean")
    void geo_isDatacenterIsBoolean() {
        Response r = client.getWeatherGeo();
        assertThat(r.statusCode()).isEqualTo(200);
        Boolean isDatacenter = r.jsonPath().get("geo.is_datacenter");
        assertThat(isDatacenter).isNotNull().isIn(true, false);
    }

    @Test @Story("current and geo coexist") @Description("current and geo both present") @DisplayName("current and geo objects coexist")
    void current_and_geo_bothPresent() {
        Response r = client.getWeatherGeo();
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat((Object) r.jsonPath().get("current")).isNotNull();
        assertThat((Object) r.jsonPath().get("geo")).isNotNull();
    }
}
