package weather;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.example.clients.WeatherClient;
import org.example.utils.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for GET /v1/daily — daily forecast breakdown [ALL PLANS]
 */
@Epic("Weather")
@Feature("GET /v1/daily")
@DisplayName("GET /v1/daily Tests")
class GetDailyTests {

    private static final WeatherClient client = new WeatherClient();

    @Test
    @Story("200 default")
    @Description("GET /v1/daily with valid coordinates returns 200")
    @DisplayName("Daily forecast returns 200")
    void returns200() {
        assertThat(client.getDaily(TestData.LAT_NAIROBI, TestData.LON_NAIROBI).statusCode()).isEqualTo(200);
    }

    @Test
    @Story("days=3")
    @Description("GET /v1/daily with days=3 returns 200")
    @DisplayName("Daily forecast days=3 returns 200")
    void threeDays_returns200() {
        assertThat(client.getDaily(TestData.LAT_NAIROBI, TestData.LON_NAIROBI, 3).statusCode()).isEqualTo(200);
    }

    @Test
    @Story("days=1 minimum")
    @Description("GET /v1/daily with days=1 returns 200")
    @DisplayName("Daily forecast days=1 returns 200")
    void minDays_returns200() {
        assertThat(client.getDaily(TestData.LAT_NAIROBI, TestData.LON_NAIROBI, TestData.DAYS_MIN).statusCode()).isEqualTo(200);
    }

    @Test
    @Story("days=7 free plan max")
    @Description("GET /v1/daily with days=7 returns 200")
    @DisplayName("Daily forecast days=7 returns 200")
    void freePlanMax_returns200() {
        assertThat(client.getDaily(TestData.LAT_NAIROBI, TestData.LON_NAIROBI, TestData.DAYS_FREE_MAX).statusCode()).isEqualTo(200);
    }
}
