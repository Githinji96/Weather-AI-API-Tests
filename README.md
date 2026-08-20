# WeatherAI API Test Suite

A comprehensive automated REST API test framework for the [WeatherAI API](https://weather-ai.co/docs), built with **Java 17, Maven, Rest Assured, JUnit 5, AssertJ, Allure, and iText 7**.

The framework provides automated coverage across weather, authentication, authorization, account usage, webhooks, SMS, forestry, validation, error handling, rate limiting, and response schema validation.

[![CI](https://github.com/Githinji96/Weather-AI-API-Tests/actions/workflows/ci.yml/badge.svg)](https://github.com/Githinji96/Weather-AI-API-Tests/actions)

> **Stack:** Java 17 · Maven · Rest Assured 5 · JUnit 5 · AssertJ · Allure · iText 7

---

## Table of Contents

* [Overview](#overview)
* [Prerequisites](#prerequisites)
* [Setup](#setup)
* [Running the Tests](#running-the-tests)
* [Test Strategy](#test-strategy)
* [Test Coverage](#test-coverage)
* [Project Structure](#project-structure)
* [Reports](#reports)
* [CI / GitHub Actions](#ci--github-actions)
* [Security](#security)

---

## Overview

This project is a black-box API automation framework designed to validate the WeatherAI REST API against its documented behaviour and observed runtime behaviour.

The framework focuses on:

* Functional API testing
* Authentication and authorization
* Positive and negative scenarios
* Input validation
* HTTP status code validation
* Rate-limit testing
* Response schema validation
* Business-rule validation
* Plan/feature access validation
* Webhook testing
* SMS API testing
* Forestry API testing
* API request and response reporting
* Automated Allure reporting
* Automated PDF test reporting
* CI execution through GitHub Actions

All tests execute against the live WeatherAI API without mocks.

---

## Prerequisites

| Tool       |                         Minimum Version |
| ---------- | --------------------------------------: |
| Java JDK   |                                      17 |
| Maven      |                                    3.8+ |
| Git        |                                     2.x |
| Allure CLI | 2.x *(optional for local HTML reports)* |

Verify your environment:

```bash
java -version
mvn -version
git --version
```

---

## Setup

### 1. Clone the repository

```bash
git clone https://github.com/Githinji96/Weather-AI-API-Tests.git
cd Weather-AI-API-Tests
```

### 2. Configure the API key

The framework reads the WeatherAI API key from the `WAI_API_KEY` environment variable.

Obtain your API key from the [WeatherAI Dashboard](https://weather-ai.co).

> **Security:** Never commit your real API key to Git. The local `config.properties` file is ignored by Git, while `config.properties.example` is provided as a safe configuration template.

#### Windows CMD — current session

```cmd
set WAI_API_KEY=wai_your_key_here
```

#### Windows CMD — permanent

```cmd
setx WAI_API_KEY "wai_your_key_here"
```

After using `setx`, open a new terminal.

#### Windows PowerShell

```powershell
$env:WAI_API_KEY = "wai_your_key_here"
```

#### Linux / macOS

```bash
export WAI_API_KEY=wai_your_key_here
```

### 3. Install Maven dependencies

```bash
mvn dependency:resolve
```

---

## Running the Tests

### Run the complete test suite

```bash
mvn clean test
```

The full suite currently contains **256 automated tests**.

### Run tests by endpoint group

| Group    | Command                                                                                                                                                                                      |
| -------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Weather  | `mvn test -Dtest="GetWeatherTests,GetForecastTests,GetCurrentTests,GetDailyTests,GetHourlyTests,GetForecast14Tests,GetInsightsTests,GetWeatherGeoTests,GetIpLookupTests,WeatherSchemaTests"` |
| Common   | `mvn test -Dtest="AuthenticationTests,AuthorizationTests,ValidationTests,RateLimitTests,ErrorHandlingTests"`                                                                                 |
| Account  | `mvn test -Dtest="UsageTests"`                                                                                                                                                               |
| Webhooks | `mvn test -Dtest="CreateWebhookTests,GetWebhookTests,DeleteWebhookTests"`                                                                                                                    |
| SMS      | `mvn test -Dtest="SendSmsTests,SmsAlertTests,RegistrationTests,SmsStatsTests,SmsHealthTests"`                                                                                                |
| Forestry | `mvn test -Dtest="CountTreesTests,TreeQuotaTests"`                                                                                                                                           |

### Run a single test class

```bash
mvn test -Dtest="GetWeatherTests"
```

### Run a single test method

```bash
mvn test -Dtest="GetWeatherTests#validRequest_returns200"
```

---

## Test Strategy

### Testing approach

The framework uses **black-box API testing** against the live WeatherAI REST API.

No mocks are used for the API tests. Requests are sent to the real endpoints and validated against expected HTTP responses, payloads, schemas, and business rules.

### Key design decisions

#### 1. API client abstraction

Each API domain has its own client class, such as:

* `WeatherClient`
* `AccountClient`
* `WebhookClient`
* `SmsClient`
* `ForestryClient`

The client layer encapsulates endpoint paths, request parameters, and API interaction logic.

Tests therefore focus on **test scenarios and assertions rather than constructing HTTP requests directly**.

This keeps the framework maintainable when API endpoints or request structures change.

#### 2. Centralized request specifications

`RequestSpecConfig` provides reusable Rest Assured specifications for different request scenarios:

* Authenticated requests
* Unauthenticated requests
* Invalid-token requests
* Multipart requests

Common transport configuration, logging, and response handling are centralized rather than duplicated throughout the test suite.

#### 3. Plan-aware testing

The test API key operates on the **Free plan**.

Some endpoints are restricted to PRO+, Scale, or other higher-tier plans. Instead of conditionally skipping these tests, the framework validates the expected access-control behaviour.

For example:

```text
200 → Feature is accessible
403 → Feature is correctly restricted
404 → Endpoint/feature is unavailable for the current plan
```

This allows the complete suite to execute against the available account tier while still validating plan restrictions.

#### 4. Documented vs observed behaviour

Where the live API behaves differently from its documentation, the tests explicitly document the observed behaviour.

For example, if documentation indicates `403` but the live API currently returns `404`, the assertion/reporting makes this discrepancy visible rather than silently hiding it.

This helps distinguish between:

* Expected behaviour
* Documented behaviour
* Actual API behaviour

#### 5. Response schema validation

Responses are validated beyond simple HTTP status codes.

The framework validates:

* Required fields
* Data types
* Value ranges
* Coordinates
* Temperature ranges
* Business rules
* Nested response structures
* Quota calculations

Examples include:

```text
used + remaining = limit
temp_max > temp_min
latitude within valid bounds
longitude within valid bounds
temperature within expected range
```

#### 6. One test class per endpoint/domain

Test classes are organized around API endpoints and functional areas.

Cross-cutting concerns such as authentication, authorization, validation, rate limiting, and error handling are maintained separately in the `common` package.

Schema validation is isolated in `WeatherSchemaTests` so that functional endpoint tests remain focused.

---

## Test Coverage

The current test suite contains **26 test classes and 256 automated tests**.

| Package    | Classes |   Tests |
| ---------- | :-----: | ------: |
| `common`   |    5    |      33 |
| `weather`  |    10   |     104 |
| `account`  |    1    |      15 |
| `webhooks` |    3    |      31 |
| `sms`      |    5    |      62 |
| `forestry` |    2    |      11 |
| **Total**  |  **26** | **256** |

### Coverage areas

**Weather**

* Current weather
* Forecast
* Daily forecast
* Hourly forecast
* 14-day forecast
* Weather insights
* Weather geolocation
* IP lookup
* Response schema validation

**Common**

* Authentication
* Authorization
* Validation
* Rate limiting
* Error handling

**Account**

* API usage
* Quotas
* Usage limits

**Webhooks**

* Create webhook
* Retrieve webhook
* Delete webhook

**SMS**

* Send SMS
* SMS alerts
* Registration
* SMS statistics
* SMS health

**Forestry**

* Tree counting
* Tree quotas

---

## Project Structure

```text
Weather-AI-API-Tests/
│
├── src/
│   │
│   ├── main/
│   │   ├── java/
│   │   │   └── org/example/
│   │   │       │
│   │   │       ├── config/
│   │   │       │   ├── ApiConfig.java
│   │   │       │   └── RequestSpecConfig.java
│   │   │       │
│   │   │       ├── clients/
│   │   │       │   ├── WeatherClient.java
│   │   │       │   ├── AccountClient.java
│   │   │       │   ├── WebhookClient.java
│   │   │       │   ├── SmsClient.java
│   │   │       │   └── ForestryClient.java
│   │   │       │
│   │   │       └── utils/
│   │   │           ├── TestData.java
│   │   │           └── TokenManager.java
│   │   │
│   │   └── resources/
│   │       ├── config.properties
│   │       ├── config.properties.example
│   │       └── images/
│   │           └── farm images for forestry tests
│   │
│   └── test/
│       └── java/
│           ├── common/
│           ├── weather/
│           ├── account/
│           ├── webhooks/
│           ├── sms/
│           ├── forestry/
│           │
│           └── org/example/reports/
│               └── PdfReportGenerator.java
│
├── .github/
│   └── workflows/
│       └── ci.yml
│
├── pom.xml
├── README.md
└── .gitignore
```

### Configuration

`ApiConfig.java` handles application configuration and retrieves the API key from the environment.

`RequestSpecConfig.java` provides reusable Rest Assured request specifications.

`config.properties` contains local configuration and is **not committed to Git**.

`config.properties.example` provides a safe configuration template.

---

## Reports

The framework generates both **Allure HTML reports** and **PDF test reports**.

### Allure HTML Report

Generate the Allure report:

```bash
mvn allure:report
```

Open the report locally:

```bash
mvn allure:serve
```

The report provides detailed information about:

* Test execution
* Passed/failed tests
* Test steps
* HTTP requests
* HTTP responses
* Response bodies
* Execution details
* Test history

### PDF Report

The framework also generates a PDF report from the test execution results.

After running:

```bash
mvn clean test
```

the PDF is available at:

```text
target/pdf-report/test-report.pdf
```

On Windows, open it with:

```cmd
start "" "target\pdf-report\test-report.pdf"
```

The PDF can be shared directly with stakeholders, recruiters, developers, or QA teams.

---

## CI / GitHub Actions

The project uses **GitHub Actions** for continuous integration.

The workflow executes automatically on pushes and pull requests targeting the `master` branch.

### CI pipeline

The pipeline performs the following:

| Stage                  | Description                        |
| ---------------------- | ---------------------------------- |
| Checkout               | Checks out the repository          |
| Setup Java             | Configures Java 17                 |
| Install dependencies   | Resolves Maven dependencies        |
| Run tests              | Executes the 256 API tests         |
| Generate Allure report | Creates the HTML test report       |
| Generate PDF           | Creates the PDF test report        |
| Upload artifacts       | Stores reports for download        |
| GitHub Pages           | Publishes the Allure report        |
| Test results           | Makes CI results visible in GitHub |

### Configure the API key

The API key is stored securely as a GitHub Actions repository secret.

Go to:

**Repository → Settings → Secrets and variables → Actions**

Select:

**New repository secret**

Add:

```text
Name: WAI_API_KEY
Value: wai_your_real_key_here
```

The secret is then made available to the CI workflow without exposing it in the repository.

### GitHub Actions

View the CI pipeline and previous workflow runs:

**https://github.com/Githinji96/Weather-AI-API-Tests/actions**

### CI artifacts

After a workflow completes, reports can be downloaded from the **Artifacts** section of the GitHub Actions run.

Available artifacts include:

* `allure-report`
* `pdf-test-report`

### Live Allure Report

The CI pipeline publishes the Allure report through GitHub Pages:

**https://Githinji96.github.io/Weather-AI-API-Tests/allure-report/**

---

## Security

Sensitive credentials must never be committed to the repository.

The following should remain outside Git:

```text
config.properties
WAI_API_KEY
API tokens
Passwords
Private credentials
Environment-specific secrets
```

Use environment variables locally and GitHub Actions Secrets in CI.

A safe configuration template is provided as:

```text
src/main/resources/config.properties.example
```

Example:

```properties
base.url=https://weather-ai.co
api.key=${WAI_API_KEY}
```

Before pushing changes, verify:

```bash
git status
```

and ensure no secrets or generated reports are accidentally staged.

---

## Repository

**GitHub:**
https://github.com/Githinji96/Weather-AI-API-Tests

**CI / GitHub Actions:**
https://github.com/Githinji96/Weather-AI-API-Tests/actions

**WeatherAI API Documentation:**
https://weather-ai.co/docs
