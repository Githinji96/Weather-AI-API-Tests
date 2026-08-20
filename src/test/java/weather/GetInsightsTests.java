package weather;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.example.clients.WeatherClient;
import org.example.config.RequestSpecConfig;
import org.example.utils.TestData;
import org.example.utils.TokenManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for GET /v1/insights — AI-powered weather insights [PRO+]
 */
@Epic("Weather")
@Feature("GET /v1/insights")
@DisplayName("GET /v1/insights Tests")
class GetInsightsTests {

    private static final WeatherClient client = new WeatherClient();

    @Test
    @Story("200 or 403")
    @Description("Returns 200 on Pro/Scale, 403 on Free")
    @DisplayName("insights returns 200 or 403")
    void returns200Or403() {
        assertThat(client.getInsights(TestData.LAT_NAIROBI, TestData.LON_NAIROBI).statusCode()).isIn(200, 403);
    }

    @Test
    @Story("Revoked token → 401")
    @Description("Revoked token must return 401")
    @DisplayName("insights revoked token returns 401")
    void revokedToken_returns401() {
        assertThat(new WeatherClient(RequestSpecConfig.invalidTokenSpec(TokenManager.revokedToken()))
                .getInsights(TestData.LAT_NAIROBI, TestData.LON_NAIROBI).statusCode()).isEqualTo(401);
    }
}
