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
 * Tests for GET /v1/current — current conditions only [ALL PLANS]
 */
@Epic("Weather")
@Feature("GET /v1/current")
@DisplayName("GET /v1/current Tests")
class GetCurrentTests {

    private static final WeatherClient client = new WeatherClient();

    @Test
    @Story("200")
    @Description("GET /v1/current with valid coordinates returns 200")
    @DisplayName("Current weather returns 200")
    void returns200() {
        assertThat(client.getCurrent(TestData.LAT_NAIROBI, TestData.LON_NAIROBI).statusCode()).isEqualTo(200);
    }

    @Test
    @Story("JSON body")
    @Description("Response must be non-empty application/json")
    @DisplayName("Current weather is non-empty JSON")
    void isNonEmptyJson() {
        Response r = client.getCurrent(TestData.LAT_NAIROBI, TestData.LON_NAIROBI);
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.contentType()).containsIgnoringCase("application/json");
        assertThat(r.body().asString()).isNotBlank();
    }

    @Test
    @Story("London")
    @Description("GET /v1/current for London returns 200")
    @DisplayName("London current weather returns 200")
    void london_returns200() {
        assertThat(client.getCurrent(TestData.LAT_LONDON, TestData.LON_LONDON).statusCode()).isEqualTo(200);
    }
}
