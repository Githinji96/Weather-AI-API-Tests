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
 * Tests for GET /v1/hourly — hourly forecast breakdown [ALL PLANS]
 */
@Epic("Weather")
@Feature("GET /v1/hourly")
@DisplayName("GET /v1/hourly Tests")
class GetHourlyTests {

    private static final WeatherClient client = new WeatherClient();

    @Test
    @Story("200")
    @Description("GET /v1/hourly with valid coordinates returns 200")
    @DisplayName("Hourly forecast returns 200")
    void returns200() {
        assertThat(client.getHourly(TestData.LAT_NAIROBI, TestData.LON_NAIROBI).statusCode()).isEqualTo(200);
    }

    @Test
    @Story("JSON")
    @Description("Hourly response content-type must be application/json")
    @DisplayName("Hourly response is JSON")
    void isJson() {
        Response r = client.getHourly(TestData.LAT_NAIROBI, TestData.LON_NAIROBI);
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.contentType()).containsIgnoringCase("application/json");
    }

    @Test
    @Story("Mombasa")
    @Description("GET /v1/hourly for Mombasa returns 200")
    @DisplayName("Hourly forecast for Mombasa returns 200")
    void mombasa_returns200() {
        assertThat(client.getHourly(TestData.LAT_MOMBASA, TestData.LON_MOMBASA).statusCode()).isEqualTo(200);
    }
}
