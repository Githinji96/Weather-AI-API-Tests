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

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for GET /v1/forecast14 — 14-day extended forecast [PRO+]
 */
@Epic("Weather")
@Feature("GET /v1/forecast14")
@DisplayName("GET /v1/forecast14 Tests")
class GetForecast14Tests {

    private static final WeatherClient client = new WeatherClient();

    @Test
    @Story("200 or 403")
    @Description("Returns 200 on Pro/Scale, 403 on Free")
    @DisplayName("forecast14 returns 200 or 403")
    void returns200Or403() {
        assertThat(client.getForecast14(TestData.LAT_NAIROBI, TestData.LON_NAIROBI).statusCode()).isIn(200, 403);
    }

    @Test
    @Story("Revoked token → 401")
    @Description("Revoked token must return 401")
    @DisplayName("forecast14 revoked token returns 401")
    void revokedToken_returns401() {
        assertThat(new WeatherClient(RequestSpecConfig.invalidTokenSpec(TokenManager.revokedToken()))
                .getForecast14(TestData.LAT_NAIROBI, TestData.LON_NAIROBI).statusCode()).isEqualTo(401);
    }

    @Test
    @Story("Missing coords → 400 or 403")
    @Description("Missing lat/lon returns 400 on Pro/Scale, 403 on Free")
    @DisplayName("forecast14 missing coordinates returns 400 or 403")
    void missingCoords_returns400Or403() {
        assertThat(given(RequestSpecConfig.defaultSpec()).get("/v1/forecast14").statusCode()).isIn(400, 403);
    }
}
