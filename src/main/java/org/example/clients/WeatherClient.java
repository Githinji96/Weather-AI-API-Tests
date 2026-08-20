package org.example.clients;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.example.config.RequestSpecConfig;

import static io.restassured.RestAssured.given;

/**
 * Client for all Weather endpoints:

 */
public class WeatherClient {

    private final RequestSpecification spec;

    public WeatherClient() {
        this.spec = RequestSpecConfig.defaultSpec();
    }

    /** Constructor for negative tests that need a custom spec. */
    public WeatherClient(RequestSpecification spec) {
        this.spec = spec;
    }

    // ── /v1/weather ──────────────────────────────────────────────────────────

    public Response getWeather(double lat, double lon) {
        return given(spec)
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .get("/v1/weather");
    }

    public Response getWeather(double lat, double lon, int days, boolean ai,
                               String units, String lang) {
        return given(spec)
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("days", days)
                .queryParam("ai", ai)
                .queryParam("units", units)
                .queryParam("lang", lang)
                .get("/v1/weather");
    }

    // /v1/forecast (alias)//

    public Response getForecast(double lat, double lon) {
        return given(spec)
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .get("/v1/forecast");
    }

    // /v1/current//
    public Response getCurrent(double lat, double lon) {
        return given(spec)
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .get("/v1/current");
    }

    // ── /v1/daily ─

    public Response getDaily(double lat, double lon) {
        return given(spec)
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .get("/v1/daily");
    }

    public Response getDaily(double lat, double lon, int days) {
        return given(spec)
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("days", days)
                .get("/v1/daily");
    }

    // ── /v1/hourly ─

    public Response getHourly(double lat, double lon) {
        return given(spec)
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .get("/v1/hourly");
    }

    // ── /v1/forecast14 (PRO+) ──

    public Response getForecast14(double lat, double lon) {
        return given(spec)
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .get("/v1/forecast14");
    }

    // ── /v1/insights (PRO+) ─

    public Response getInsights(double lat, double lon) {
        return given(spec)
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .get("/v1/insights");
    }

    // ── /v1/weather-geo ----

    public Response getWeatherGeo() {
        return given(spec).get("/v1/weather-geo");
    }

    public Response getWeatherGeo(double lat, double lon) {
        return given(spec)
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .get("/v1/weather-geo");
    }

    // ── /v1/ip-lookup (PRO+) --
    public Response getIpLookup(String ip) {
        return given(spec)
                .queryParam("ip", ip)
                .get("/v1/ip-lookup");
    }
}
