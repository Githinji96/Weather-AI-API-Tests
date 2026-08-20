package weather;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.example.clients.WeatherClient;
import org.example.config.RequestSpecConfig;
import org.example.utils.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for GET /v1/ip-lookup — resolve IP to geo coordinates [PRO+]
 *
 * Confirmed response shape:
 * { "ip", "ip_hash", "ip_version",
 *   "geo": { "lat", "lon", "city", "region", "country", "timezone" } }
 */
@Epic("Weather")
@Feature("GET /v1/ip-lookup")
@DisplayName("GET /v1/ip-lookup Tests")
class GetIpLookupTests {

    private static final WeatherClient client = new WeatherClient();

    @Test
    @Story("Valid IP — 200 or 403")
    @Description("Valid IP returns 200 on Pro/Scale, 403 on Free")
    @DisplayName("ip-lookup with valid IP returns 200 or 403")
    void validIp_returns200Or403() {
        assertThat(client.getIpLookup(TestData.IP_NAIROBI).statusCode()).isIn(200, 403);
    }

    @Test
    @Story("ip=auto")
    @Description("ip=auto detects caller IP — returns 200 or 403")
    @DisplayName("ip=auto returns 200 or 403")
    void autoIp_returns200Or403() {
        assertThat(client.getIpLookup("auto").statusCode()).isIn(200, 403);
    }

    @Test
    @Story("Invalid IP → 400")
    @Description("Malformed IP returns 400 or 403")
    @DisplayName("ip-lookup with invalid IP returns 400 or 403")
    void invalidIp_returns400Or403() {
        assertThat(client.getIpLookup(TestData.IP_INVALID).statusCode()).isIn(400, 403);
    }

    @Test
    @Story("Loopback IP → 400")
    @Description("Private/loopback IP returns 400 or 403")
    @DisplayName("ip-lookup with loopback IP returns 400 or 403")
    void loopbackIp_returns400Or403() {
        assertThat(client.getIpLookup(TestData.IP_LOOPBACK).statusCode()).isIn(400, 403);
    }

    @Test
    @Story("Missing ip → 400")
    @Description("Missing ip parameter returns 400 or 403")
    @DisplayName("ip-lookup missing ip param returns 400 or 403")
    void missingParam_returns400Or403() {
        assertThat(given(RequestSpecConfig.defaultSpec()).get("/v1/ip-lookup").statusCode()).isIn(400, 403);
    }

    @Test
    @Story("Response is JSON")
    @Description("Response content-type must be application/json")
    @DisplayName("ip-lookup response is application/json")
    void response_isJson() {
        Response r = client.getIpLookup(TestData.IP_NAIROBI);
        assertThat(r.statusCode()).isIn(200, 403);
        assertThat(r.contentType()).containsIgnoringCase("application/json");
    }

    // ── Schema — only asserted when 200 ──────────────────────────────────────

    @Test
    @Story("Schema — ip non-blank")
    @Description("When 200, ip field must be non-blank")
    @DisplayName("ip field is non-blank on 200")
    void schema_ipNonBlank() {
        Response r = client.getIpLookup(TestData.IP_NAIROBI);
        if (r.statusCode() == 200) {
            assertThat(r.jsonPath().getString("ip")).isNotBlank();
        } else {
            assertThat(r.statusCode()).isEqualTo(403);
        }
    }

    @Test
    @Story("Schema — ip_version")
    @Description("When 200, ip_version must be v4 or v6")
    @DisplayName("ip_version is v4 or v6 on 200")
    void schema_ipVersionValid() {
        Response r = client.getIpLookup(TestData.IP_NAIROBI);
        if (r.statusCode() == 200) {
            assertThat(r.jsonPath().getString("ip_version")).isIn("v4", "v6");
        } else {
            assertThat(r.statusCode()).isEqualTo(403);
        }
    }

    @Test
    @Story("Schema — geo object")
    @Description("When 200, response must contain a non-null geo object")
    @DisplayName("geo object is present on 200")
    void schema_geoPresent() {
        Response r = client.getIpLookup(TestData.IP_NAIROBI);
        if (r.statusCode() == 200) {
            assertThat((Object) r.jsonPath().get("geo")).isNotNull();
        } else {
            assertThat(r.statusCode()).isEqualTo(403);
        }
    }

    @Test
    @Story("Schema — geo coordinates valid")
    @Description("When 200, geo.lat must be −90 to 90 and geo.lon −180 to 180")
    @DisplayName("geo.lat and geo.lon are in valid range on 200")
    void schema_geoCoordinatesValid() {
        Response r = client.getIpLookup(TestData.IP_NAIROBI);
        if (r.statusCode() == 200) {
            Float lat = r.jsonPath().get("geo.lat");
            Float lon = r.jsonPath().get("geo.lon");
            assertThat(lat).isNotNull();
            assertThat(lon).isNotNull();
            assertThat(lat.doubleValue()).isBetween(-90.0, 90.0);
            assertThat(lon.doubleValue()).isBetween(-180.0, 180.0);
        } else {
            assertThat(r.statusCode()).isEqualTo(403);
        }
    }

    @Test
    @Story("Schema — geo.country 2-letter")
    @Description("When 200, geo.country must be a 2-letter ISO country code")
    @DisplayName("geo.country is 2-letter code on 200")
    void schema_geoCountryTwoLetters() {
        Response r = client.getIpLookup(TestData.IP_NAIROBI);
        if (r.statusCode() == 200) {
            assertThat(r.jsonPath().getString("geo.country"))
                    .isNotBlank().hasSize(2).matches("[A-Z]{2}");
        } else {
            assertThat(r.statusCode()).isEqualTo(403);
        }
    }

    @Test
    @Story("Schema — geo.timezone non-blank")
    @Description("When 200, geo.timezone must be non-blank IANA timezone string")
    @DisplayName("geo.timezone is non-blank on 200")
    void schema_geoTimezoneNonBlank() {
        Response r = client.getIpLookup(TestData.IP_NAIROBI);
        if (r.statusCode() == 200) {
            assertThat(r.jsonPath().getString("geo.timezone")).isNotBlank();
        } else {
            assertThat(r.statusCode()).isEqualTo(403);
        }
    }
}
