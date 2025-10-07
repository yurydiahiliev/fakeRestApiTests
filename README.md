# Fake REST API Tests

![Build](https://github.com/yurydiahiliev/fakeRestApiTests/actions/workflows/gradle.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-21-blue)
![Gradle](https://img.shields.io/badge/Gradle-8.10-green)
![Allure](https://img.shields.io/badge/Allure-Report-orange)

End-to-end API test suite for the [Fake REST API](https://fakerestapi.azurewebsites.net/index.html) using **JUnit 5**, **REST Assured**, **AssertJ**, **Lombok**, **Log4j2**, and **Allure Reporting**.  
Built with **Gradle 8.10** and **Java 21**.

---

## API Documentation (Swagger UI)

The project under test uses the public **Fake REST API**.

You can explore and test all available endpoints directly via Swagger UI:

 **[Fake REST API Swagger Docs](https://fakerestapi.azurewebsites.net/index.html)**

This interactive documentation provides:
- All available endpoints (`/Books`, `/Authors`, `/Activities`, `/Users`, etc.)
- Supported HTTP methods (GET, POST, PUT, DELETE)
- Request/response schemas
- Example payloads and responses

> **Note:**  
> Current automated test coverage in this repository includes only the **Books** and **Authors** API modules.

You can use this Swagger UI both for manual exploration and to verify API responses that the automated tests assert against.

---


## Tech stack

| Layer | Tool |
|--------|------|
| Language / Build | Java 21 (Temurin), Gradle Wrapper 8.10 |
| Testing Framework | JUnit 5 |
| HTTP Client | REST Assured |
| Assertions | AssertJ |
| Reporting | Allure (JUnit5 + REST Assured adapters) |
| Logging | SLF4J + Log4j2 |
| CI/CD | GitHub Actions (Gradle caching + Allure GH Pages) |

---

## Project structure

```
fakeRestApiTests/
├── .github/
│   └── workflows/
│       └── gradle.yml                        # CI workflow (GitHub Actions)
├── build/
│   ├── allure/
│   │   └── commandline/                      # Bundled Allure CLI
│   ├── allure-results/                       # Raw Allure results (generated)
│   ├── classes/
│   │   ├── java/main/                        # Compiled main classes
│   │   └── java/test/                        # Compiled test classes
│   ├── reports/
│   │   ├── allure-report/
│   │   │   └── allureReport/
│   │   │       └── index.html                # Local Allure HTML entrypoint
│   │   └── tests/                            # Gradle test HTML report
│   ├── resources/
│   │   ├── main/                             # Copied main resources
│   │   └── test/                             # Copied test resources
│   └── test-results/                         # JUnit XML results
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/fakeRestApi/              # Main Java sources
│   │   └── resources/
│   │       └── api.properties                # API base URL defaults
│   └── test/
│       ├── java/
│       │   └── com/                          # JUnit 5 tests
│       └── resources/
│           ├── log4j2.xml                    # Log4j2 config
│           └── schemas/                      # JSON Schemas
│               ├── author.json
│               ├── book.json
│               ├── singleAuthor.json
│               └── singleBook.json
├── build.gradle                               # Build configuration
├── settings.gradle                            # Gradle settings
├── gradlew                                    # Gradle wrapper (Unix)
├── gradlew.bat                                # Gradle wrapper (Windows)
└── README.md
```

Key paths:
- `build/allure-results` → raw Allure data
- `build/reports/allure-report/allureReport/index.html` → local HTML report
- `.github/workflows/gradle.yml` → CI pipeline definition

---

## Quick start

### 1. Clone the repository
```bash
git clone https://github.com/yurydiahiliev/fakeRestApiTests.git
cd fakeRestApiTests
```

### 2. Verify environment
```bash
./gradlew -v
java -version
```

### 3. Run full flow (tests + Allure report)
```bash
./gradlew all
```

Open the report locally:  
`build/reports/allure-report/allureReport/index.html`

---

## Running tests

Run all tests:
```bash
./gradlew clean test
```

Run a single class:
```bash
./gradlew test --tests "com.fakeRestApi.tests.author.CreateAuthorsTests"
```

Run tests by package:
```bash
./gradlew test --tests "com.fakeRestApi.tests.book.*"
```

Run with debug logs:
```bash
LOG_LEVEL=DEBUG ./gradlew test -Dlog.level=DEBUG
```

---

## Configuration

### API Base URL
Default in `src/main/resources/api.properties`:
```properties
base.api.url=https://fakerestapi.azurewebsites.net/api/v1
```

Override at runtime:
```bash
./gradlew test -Dbase.api.url=https://your-env.example.com/api/v1
```

---

## Logging

Controlled by two layers:

| Source | Property | Example |
|--------|-----------|----------|
| Gradle JVM | `-Dlog.level` | `-Dlog.level=DEBUG` |
| Log4j2 env | `LOG_LEVEL` | `LOG_LEVEL=WARN ./gradlew test` |

Default level: `INFO`

---

## Allure Reporting

Allure plugin is configured in `build.gradle`.

- JUnit5 and REST Assured adapters included
- Results stored in `build/allure-results`
- Generate report locally:
  ```bash
  ./gradlew allureReport
  ```
- Open the report:  
  `build/reports/allure-report/allureReport/index.html`

---

## GitHub Actions Integration

The project includes a **CI/CD pipeline** that:
1. Sets up **Java 21** and **Gradle 8.10**
2. Caches Gradle dependencies
3. Runs tests (continues even if some fail)
4. Publishes **Allure reports with history** to GitHub Pages

### Workflow location
`.github/workflows/gradle.yml`

### Report URL
```
https://yurydiahiliev.github.io/fakeRestApiTests/
```

### Key steps
-  `Run Tests` → executes all test suites
-  `Allure Report action` → generates + merges historical results
-  `Deploy report to GitHub Pages` → publishes to `gh-pages`
-  `Add summary` → adds link to workflow summary

Uses:
- [`gradle/actions/setup-gradle@v3`](https://github.com/gradle/actions)
- [`simple-elf/allure-report-action`](https://github.com/simple-elf/allure-report-action)
- [`peaceiris/actions-gh-pages`](https://github.com/peaceiris/actions-gh-pages)

---

## Useful Gradle tasks

| Command | Description |
|----------|-------------|
| `./gradlew clean test` | Run full test suite |
| `./gradlew allureReport` | Generate Allure HTML report |
| `./gradlew all` | Clean + test + generate report |
| `./gradlew dependencies` | Show dependency tree |
| `./gradlew test --tests "com.fakeRestApi.tests.book.*"` | Run only Book tests |

---

## Troubleshooting

**No Allure report generated**
- Check if `build/allure-results` contains results
- Then run:
  ```bash
  ./gradlew allureReport
  ```

**Allure report empty on GitHub Pages**
- See “List Allure Results” step in workflow logs
- Verify results copied correctly from `build/allure-results`

**403 push denied when publishing**
- Go to `Settings → Actions → General → Workflow permissions`  
  and enable **Read and write permissions**

---
