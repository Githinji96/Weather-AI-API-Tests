# WeatherAI API Test Suite

Automated REST API test suite for the [WeatherAI API](https://weather-ai.co/docs)

[![CI](https://github.com/YOUR_GITHUB_USERNAME/WeatherAI_API_Test/actions/workflows/ci.yml/badge.svg)](https://github.com/YOUR_GITHUB_USERNAME/WeatherAI_API_Test/actions/workflows/ci.yml)

> **Stack:** Java 17 · Maven · RestAssured 5 · JUnit 5 · AssertJ · Allure · iText 7

> ⚠️ Replace `YOUR_GITHUB_USERNAME` in the badge URL above with your actual GitHub username.

---

## Table of Contents

- [Prerequisites](#prerequisites)
- [Setup](#setup)
- [Running the Tests](#running-the-tests)
- [Test Strategy](#test-strategy)
- [Project Structure](#project-structure)
- [Reports](#reports)
- [CI / GitHub Actions](#ci--github-actions)

---

## Prerequisites

| Tool | Minimum Version |
|------|----------------|
| Java JDK | 17 |
| Maven | 3.8 |
| Allure CLI *(optional — for local HTML report)* | 2.x |

---

## Setup

### 1. Clone the repository

```bash
git clone https://github.com/YOUR_GITHUB_USERNAME/WeatherAI_API_Test.git
cd WeatherAI_API_Test
```

### 2. Set your API key

The suite reads the API key from the `WAI_API_KEY` environment variable.  
Get your free key from [weather-ai.co](https://weather-ai.co) → Dashboard → API Keys.

> **Never commit the real key to git.** `config.properties` is gitignored for this reason.

**Windows CMD — current session:**
```cmd
set WAI_API_KEY=wai_your_key_here
```

**Windows CMD — permanent (survives restarts):**
```cmd
setx WAI_API_KEY "wai_your_key_here"
```
Then open a new terminal — the key is available in every session from that point on.

**Windows PowerShell:**
```powershell
$env:WAI_API_KEY = "wai_your_key_here"
```

**Linux / macOS:**
```bash
export WAI_API_KEY=wai_your_key_here
```

### 3. Install dependencies

```bash
mvn dependency:resolve
```

---

## Running the Tests

### Full suite

```cmd
mvn clean test
```

### By endpoint group

| Group | Command |
|-------|---------|
| Weather | `mvn test -Dtest="GetWeatherTests,GetForecastTests,GetCurrentTests,GetDailyTests,GetHourlyTests,GetForecast14Tests,GetInsightsTests,GetWeatherGeoTests,GetIpLookupTests,WeatherSchemaTests"` |
| Common | `mvn test -Dtest="AuthenticationTests,AuthorizationTests,ValidationTests,RateLimitTests,ErrorHandlingTests"` |
| Account | `mvn test -Dtest="UsageTests"` |
| Webhooks | `mvn test -Dtest="CreateWebhookTests,GetWebhookTests,DeleteWebhookTests"` |
| SMS | `mvn test -Dtest="SendSmsTests,SmsAlertTests,RegistrationTests,SmsStatsTests,SmsHealthTests"` |
| Forestry | `mvn test -Dtest="CountTreesTests,TreeQuotaTests"` |

### Single class

```cmd
mvn test -Dtest="GetWeatherTests"
```

### Single method

```cmd
mvn test -Dtest="GetWeatherTests#validRequest_returns200"
```

---

## Reports

### Allure HTML report

```cmd
mvn allure:report
mvn allure:serve
```

`allure:serve` generates the report and opens it in your browser automatically.

Live report (published by CI after each push to `master`):  
👉 `https://YOUR_GITHUB_USERNAME.github.io/WeatherAI_API_Test/allure-report/`

### PDF report

Generated automatically after every `mvn test` run:

```
target/pdf-report/test-report.pdf
```

Open it with:

```cmd
start "" "target\pdf-report\test-report.pdf"
```

---

## Test Strategy

### Approach

Black-box API testing against the live [WeatherAI REST API](https://weather-ai.co/docs) — no mocks.  
All 256 tests hit real endpoints.

### Key decisions

**1. Client layer abstraction**  
Each API section has its own client class (`WeatherClient`, `SmsClient`, etc.) that encapsulates endpoint paths and parameters. Tests never build HTTP requests directly — a single endpoint change requires only one file to update.

**2. Shared `RequestSpecConfig`**  
A factory provides four spec variants (authenticated, unauthenticated, invalid-token, multipart) with gzip decompression and request/response logging pre-configured, ensuring consistent transport settings across all tests.

**3. Plan-aware assertions**  
The test key is on the Free plan. PRO+/Scale-only endpoints return `403` or `404`. All gated-endpoint tests use `isIn(200, 403, 404)` so the full suite passes on any plan tier without conditionally skipping tests.

**4. Documented vs observed behaviour**  
Where the API deviates from its docs (e.g. returning `404` instead of `403` on plan-gated endpoints, omitting `X-RateLimit-*` headers), tests use `.as("Expected X — API currently returns Y")` so the gap is visible in reports without causing a false failure.

**5. Schema validation**  
Confirmed response shapes are validated field by field: correct types, value ranges (temperatures −90 to 60 °C, coordinates within bounds), and business logic constraints (e.g. `used + remaining = limit`, `temp_max > temp_min`).

**6. One class per endpoint**  
Each API route has its own test class. Cross-cutting concerns (auth, validation, error codes, rate limits) live in `common/`. Schema tests are isolated in `WeatherSchemaTests` so smoke tests stay fast.

### Coverage

| Package | Classes | Tests |
|---------|:-------:|:-----:|
| common | 5 | 33 |
| weather | 10 | 104 |
| account | 1 | 15 |
| webhooks | 3 | 31 |
| sms | 5 | 62 |
| forestry | 2 | 11 |
| **Total** | **26** | **256** |

---

## Project Structure

```
src/
├── main/
│   ├── java/org/example/
│   │   ├── config/
│   │   │   ├── ApiConfig.java           # loads config.properties + WAI_API_KEY env var
│   │   │   └── RequestSpecConfig.java   # RestAssured spec factory
│   │   ├── clients/
│   │   │   ├── WeatherClient.java       # /v1/weather, /forecast, /current, /daily,
│   │   │   │                            #   /hourly, /forecast14, /insights,
│   │   │   │                            #   /weather-geo, /ip-lookup
│   │   │   ├── AccountClient.java       # /v1/usage
│   │   │   ├── WebhookClient.java       # /v1/webhooks  POST · GET · DELETE
│   │   │   ├── SmsClient.java           # /v1/sms/*  (Scale plan only)
│   │   │   └── ForestryClient.java      # /v1/trees/*  /v1/forestry/count-trees  (PRO+)
│   │   └── utils/
│   │       ├── TestData.java            # all constants — coords, phones, IDs…
│   │       └── TokenManager.java        # valid / invalid API key helpers
│   └── resources/
│       ├── config.properties            # base URL · image paths  (gitignored — no key)
│       ├── config.properties.example    # committed template — safe to share
│       └── images/                      # farm images for forestry upload tests
└── test/
    └── java/
        ├── common/                      # auth · authz · validation · rate limits · errors
        ├── weather/                     # one class per endpoint  (10 classes)
        ├── account/
        ├── webhooks/
        ├── sms/
        ├── forestry/
        └── org/example/reports/
            └── PdfReportGenerator.java  # iText 7 — builds PDF from Surefire XML
```

---

## CI / GitHub Actions

The workflow triggers on every push and pull request to `master`.

**Setup required before first run:**

1. Go to your repository → **Settings → Secrets and variables → Actions**
2. Click **New repository secret**
3. Add:
   ```
   Name:  WAI_API_KEY
   Value: wai_your_real_key_here
   ```

**What the workflow does:**

| Step | Output |
|------|--------|
| Run 256 tests with `mvn clean test` | Surefire XML results |
| Generate Allure HTML report | Uploaded as `allure-report` artifact |
| Generate PDF report | Uploaded as `pdf-test-report` artifact |
| Deploy Allure to GitHub Pages | Live at `YOUR_USERNAME.github.io/WeatherAI_API_Test/allure-report/` |
| Publish test check on PR | Pass / fail visible on pull request |

**View CI runs:**  
👉 [GitHub Actions](https://github.com/YOUR_GITHUB_USERNAME/WeatherAI_API_Test/actions)

**Download reports** from the **Artifacts** section of any completed workflow run.
