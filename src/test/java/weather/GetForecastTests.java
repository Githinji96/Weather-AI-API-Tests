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
 * Tests for GET /v1/forecast — alias of /v1/weather [ALL PLANS]
 */
@Epic("Weather")
@Feature("GET /v1/forecast")
@DisplayName("GET /v1/forecast Tests")
class GetForecastTests {

    private static final WeatherClient client = new WeatherClient();

    @Test
    @Story("200")
    @Description("GET /v1/forecast behaves identically to GET /v1/weather")
    @DisplayName("Forecast alias returns 200")
    void returns200() {
        assertThat(client.getForecast(TestData.LAT_NAIROBI, TestData.LON_NAIROBI).statusCode()).isEqualTo(200);
    }

    @Test
    @Story("JSON")
    @Description("/v1/forecast response must be application/json")
    @DisplayName("Forecast alias returns JSON")
    void isJson() {
        Response r = client.getForecast(TestData.LAT_NAIROBI, TestData.LON_NAIROBI);
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.contentType()).containsIgnoringCase("application/json");
    }

    @Test
    @Story("Body non-empty")
    @Description("Response body must not be blank")
    @DisplayName("Forecast alias body is non-empty")
    void bodyNotEmpty() {
        Response r = client.getForecast(TestData.LAT_NAIROBI, TestData.LON_NAIROBI);
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.body().asString()).isNotBlank();
    }
}
