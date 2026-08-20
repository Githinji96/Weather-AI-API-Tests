# WeatherAI API Test Suite

Automated REST API test suite for the [WeatherAI API](https://weather-ai.co/docs)

[![CI](https://github.com/YOUR_GITHUB_USERNAME/WeatherAI_API_Test/actions/workflows/ci.yml/badge.svg)](https://github.com/YOUR_GITHUB_USERNAME/WeatherAI_API_Test/actions/workflows/ci.yml)

> **Stack:** Java 17 · Maven · RestAssured 5 · JUnit 5 · AssertJ · Allure · iText 7 (PDF reports)

---

## Table of Contents
- [Setup](#setup)
- [Running the Tests](#running-the-tests)
- [Test Strategy](#test-strategy)
- [Project Structure](#project-structure)
- [CI / GitHub Actions](#ci--github-actions)

---

## Setup

### Prerequisites

| Tool | Version |
|------|---------|
| Java JDK | 17+ |
| Maven | 3.8+ |
| Allure CLI (optional, for HTML report) | 2.x |

### 1. Clone the repository

```bash
git clone https://github.com/YOUR_GITHUB_USERNAME/WeatherAI_API_Test.git
cd WeatherAI_API_Test
```

### 2. Set your API key

The test suite reads the API key from an environment variable.  
Get your free key from [weather-ai.co](https://weather-ai.co).

**Windows CMD — set for current session then run:**
```cmd
set WAI_API_KEY=wai_your_key_here
mvn clean test
```

**Make it permanent (survives terminal restarts):**
```cmd
setx WAI_API_KEY "wai_your_key_here"
```
Then open a new terminal and run `mvn clean test` — no `set` needed.

**Windows PowerShell:**
```powershell
$env:WAI_API_KEY = "wai_your_key_here"
mvn clean test
```

**Linux / macOS:**
```bash
export WAI_API_KEY=wai_your_key_here
mvn clean test
```

> Replace `wai_your_key_here` with your actual key from  
> [weather-ai.co](https://weather-ai.co) → Dashboard → API Keys.  
> **Never commit the real key to git** — `config.properties` is gitignored for this reason.

### 3. Install dependencies

```bash
mvn dependency:resolve
```

---

## Running the Tests

### Run the full suite
```cmd
mvn clean test
```

### Run by endpoint group

| Group | Command |
|-------|---------|
| All weather endpoints | `mvn test -Dtest="GetWeatherTests,GetForecastTests,GetCurrentTests,GetDailyTests,GetHourlyTests,GetForecast14Tests,GetInsightsTests,GetWeatherGeoTests,GetIpLookupTests,WeatherSchemaTests"` |
| Common (auth/validation/errors) | `mvn test -Dtest="AuthenticationTests,AuthorizationTests,ValidationTests,RateLimitTests,ErrorHandlingTests"` |
| Account | `mvn test -Dtest="UsageTests"` |
| Webhooks | `mvn test -Dtest="CreateWebhookTests,GetWebhookTests,DeleteWebhookTests"` |
| SMS | `mvn test -Dtest="SendSmsTests,SmsAlertTests,RegistrationTests,SmsStatsTests,SmsHealthTests"` |
| Forestry | `mvn test -Dtest="CountTreesTests,TreeQuotaTests"` |

### Run a single test class
```cmd
mvn test -Dtest="GetWeatherTests"
```

### Run a single test method
```cmd
mvn test -Dtest="GetWeatherTests#validRequest_returns200"
```

### Generate Allure HTML report
```cmd
mvn allure:report
mvn allure:serve
```

### PDF report
Generated automatically after every `mvn test` run at:
```
target/pdf-report/test-report.pdf
```

---

## Test Strategy

### Approach

The suite tests the [WeatherAI REST API](https://weather-ai.co/docs) contract end-to-end using
**black-box API testing** — no mocks, all requests hit the live API.

### Key decisions

**1. Client layer abstraction**  
Each API section has a dedicated client class (`WeatherClient`, `SmsClient`, etc.)
that encapsulates endpoint paths and parameters. Tests never construct HTTP
requests directly — this makes refactoring a single endpoint change a one-file fix.

**2. Shared `RequestSpecConfig`**  
A single factory provides four spec variants (authenticated, unauthenticated,
invalid-token, multipart) with gzip decompression and logging pre-configured.
This ensures every test uses identical transport settings.

**3. Plan-aware assertions**  
The test key is on the Free plan. PRO+/Scale-only endpoints return `403` or `404`
instead of `200`. All gated tests use `isIn(200, 403, 404)` so the full suite
passes on any plan tier without skipping tests.

**4. Documented vs observed behaviour**  
Where the API deviates from its own docs (e.g. returning `404` instead of `403`
on plan-gated endpoints, or omitting `X-RateLimit-*` headers), tests include a
`.as("Expected X (documented) — API currently returns Y")` message so the gap
is visible in every test report without causing a hard failure.

**5. Schema validation**  
Every endpoint with a confirmed response shape has field-level assertions:
types, ranges (temperatures −90 to 60, coordinates in bounds), business logic
constraints (used + remaining = limit, temp_max > temp_min).

**6. Separation of concerns**  
- One test class per endpoint (weather package has 10 classes — one per route)
- `common/` package covers cross-cutting concerns: auth, authz, validation, error codes, rate limits
- Schema tests are isolated in `WeatherSchemaTests` so endpoint smoke tests stay fast

### Coverage summary

| Package | Classes | Tests |
|---------|---------|-------|
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
│   │   │   ├── ApiConfig.java          # config.properties loader + env var override
│   │   │   └── RequestSpecConfig.java  # RestAssured spec factory (auth/unauth/multipart)
│   │   ├── clients/
│   │   │   ├── WeatherClient.java      # /v1/weather, /forecast, /current, /daily, /hourly,
│   │   │   │                           #   /forecast14, /insights, /weather-geo, /ip-lookup
│   │   │   ├── AccountClient.java      # /v1/usage
│   │   │   ├── WebhookClient.java      # /v1/webhooks (POST/GET/DEL)
│   │   │   ├── SmsClient.java          # /v1/sms/* (Scale only)
│   │   │   └── ForestryClient.java     # /v1/trees/*, /v1/forestry/count-trees (PRO+)
│   │   └── utils/
│   │       ├── TestData.java           # all test constants (coords, phones, IDs…)
│   │       └── TokenManager.java       # valid / invalid token helpers
│   └── resources/
│       ├── config.properties           # base URL, API key, image paths
│       └── images/                     # farm images for forestry upload tests
└── test/
    ├── java/
    │   ├── common/                     # auth, authz, validation, rate limits, errors
    │   ├── weather/                    # one class per endpoint
    │   ├── account/
    │   ├── webhooks/
    │   ├── sms/
    │   ├── forestry/
    │   └── org/example/reports/
    │       └── PdfReportGenerator.java # iText 7 PDF report from Surefire XML
    └── resources/
        └── testdata/                   # optional farm images for forestry tests
```

---

## CI / GitHub Actions

The workflow runs on every push and pull request to `main`.

**View live results:**  
👉 [GitHub Actions — CI runs](https://github.com/YOUR_GITHUB_USERNAME/WeatherAI_API_Test/actions)

The `WAI_API_KEY` secret must be added in your repository:  
`Settings → Secrets and variables → Actions → New repository secret`

```
Name:  WAI_API_KEY
Value: wai_your_real_key_here
```
