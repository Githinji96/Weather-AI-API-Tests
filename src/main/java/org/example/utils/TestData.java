package org.example.utils;

import java.util.List;

/**
 * Centralised test data constants.
 *
 * <p>All coordinates, phone numbers, identifiers, and payloads used across
 * tests live here so that a change in one place propagates everywhere.
 */
public class TestData {

    private TestData() {}

    // ── Coordinates ──────────────────────────────────────────────────────────
    /** Nairobi, Kenya — default test location. */
    public static final double LAT_NAIROBI    = -1.2921;
    public static final double LON_NAIROBI    = 36.8219;

    /** Mombasa, Kenya */
    public static final double LAT_MOMBASA    = -4.0435;
    public static final double LON_MOMBASA    = 39.6682;

    /** London, UK */
    public static final double LAT_LONDON     = 51.5074;
    public static final double LON_LONDON     = -0.1278;

    /** North Pole — edge-case coordinate */
    public static final double LAT_NORTH_POLE = 90.0;
    public static final double LON_NORTH_POLE = 0.0;

    /** Invalid latitude/longitude (out of range) */
    public static final double LAT_INVALID    = 999.0;
    public static final double LON_INVALID    = 999.0;

    // ── Forecast days ────────────────────────────────────────────────────────
    public static final int DAYS_DEFAULT  = 7;
    public static final int DAYS_MIN      = 1;
    public static final int DAYS_FREE_MAX = 7;
    public static final int DAYS_PRO_MAX  = 14;
    public static final int DAYS_SCALE_MAX = 16;
    public static final int DAYS_INVALID  = 0;
    public static final int DAYS_OVER_MAX = 17;

    // ── Units ────────────────────────────────────────────────────────────────
    public static final String UNITS_METRIC   = "metric";
    public static final String UNITS_IMPERIAL = "imperial";
    public static final String UNITS_INVALID  = "celsius";

    // ── Language codes ───────────────────────────────────────────────────────
    public static final String LANG_EN = "en";
    public static final String LANG_SW = "sw";

    // ── IP addresses ─────────────────────────────────────────────────────────
    public static final String IP_NAIROBI  = "196.201.216.1";
    public static final String IP_LOOPBACK = "127.0.0.1";
    public static final String IP_INVALID  = "999.999.999.999";

    // ── SMS ──────────────────────────────────────────────────────────────────
    /** E.164 format Kenyan number for SMS tests */
    public static final String PHONE_VALID       = "+254712345678";
    public static final String PHONE_INVALID     = "0700";
    public static final String SMS_MESSAGE       = "Heavy rain expected tomorrow. Plan ahead.";
    public static final String SMS_TYPE          = "weather_alert";
    public static final String SMS_PILOT_TAG     = "pilot-bomet-2026";

    // SMS alert types (documented: rain · frost · extreme_wind · drought)
    public static final String ALERT_TYPE_RAIN   = "rain";
    public static final String ALERT_TYPE_FROST  = "frost";
    public static final String ALERT_TYPE_WIND   = "extreme_wind";
    public static final String ALERT_TYPE_DROUGHT = "drought";

    // ── Forestry / Trees ─────────────────────────────────────────────────────
    public static final String FARMER_ID     = "F-TEST-001";
    public static final String COUNTY        = "Bomet";
    public static final double LAND_ACRES    = 2.5;
    public static final String FARM_LOCATION = "Test Farm, Block A";
    public static final String FARM_NOTES    = "Tea plantation, automation test";

    // ── Bomet farmer registration ─────────────────────────────────────────────
    public static final String FARMER_NAME      = "John Kipchoge";
    public static final String FARMER_LOCATION  = "Bomet Central";
    public static final String FARMER_CROP_TYPE = "maize";

    // ── Webhook ──────────────────────────────────────────────────────────────
    // Confirmed request shape: { url, lat, lon, triggers[], timezone }
    public static final String       WEBHOOK_URL      = "https://yourapp.com/weather-hook";
    public static final double       WEBHOOK_LAT      = 34.0522;
    public static final double       WEBHOOK_LON      = -118.2437;
    public static final List<String> WEBHOOK_TRIGGERS = List.of("rain", "extreme_wind");
    public static final String       WEBHOOK_TIMEZONE = "America/Los_Angeles";
}
