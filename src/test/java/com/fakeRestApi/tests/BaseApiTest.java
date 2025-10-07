package com.fakeRestApi.tests;

import com.fakeRestApi.apiClient.AuthorsApi;
import com.fakeRestApi.apiClient.BooksApi;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.lang.reflect.Method;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseApiTest {

    protected BooksApi booksApi;
    protected AuthorsApi authorsApi;

    @BeforeAll
    @Step("Initialize API clients")
    void initApiClients() {
        log.info("========== TEST SUITE STARTED: {} ==========", this.getClass().getSimpleName());
        booksApi = new BooksApi();
        authorsApi = new AuthorsApi();
    }

    @BeforeEach
    @Step("Start test")
    void logTestStart(TestInfo testInfo) {
        log.info("---- STARTED: {} - {}", testInfo.getDisplayName(), testInfo.getTestMethod().map(Method::getName).orElse("unknown"));
    }

    @AfterEach
    @Step("Finish test")
    void logTestFinish(TestInfo testInfo) {
        log.info("---- FINISHED: {} - {}", testInfo.getDisplayName(), testInfo.getTestMethod().map(Method::getName).orElse("unknown"));
    }

    @AfterAll
    @Step("Tear down test environment")
    void tearDown() {
        log.info("========== TEST SUITE FINISHED ==========");
        RestAssured.reset();
    }

    /**
     * Helper method to verify standardized 400 Bad Request RFC7807 error responses
     */
    protected void assertBadRequestResponse(Response response, String expectedErrorKey) {
        assertThat(response.statusCode())
                .as("Expected HTTP 400 Bad Request")
                .isEqualTo(SC_BAD_REQUEST);

        assertThat(response.getContentType())
                .as("Response should use RFC7807 problem+json format")
                .contains("application/problem+json");

        String title = response.jsonPath().getString("title");
        Integer status = response.jsonPath().getInt("status");
        String type = response.jsonPath().getString("type");
        String traceId = response.jsonPath().getString("traceId");

        // Flexible key for different validation errors
        String errorMessage = expectedErrorKey.isEmpty()
                ? response.jsonPath().getString("errors.\"\"[0]")
                : response.jsonPath().getString("errors.'" + expectedErrorKey + "'[0]");

        assertThat(title)
                .as("Title should describe validation failure")
                .isEqualTo("One or more validation errors occurred.");

        assertThat(status)
                .as("Status in body should match HTTP 400")
                .isEqualTo(SC_BAD_REQUEST);

        assertThat(type)
                .as("Type should reference RFC 7231 section 6.5.1")
                .contains("rfc7231#section-6.5.1");

        assertThat(errorMessage)
                .as("Error message should be present and meaningful")
                .isNotBlank();

        assertThat(traceId)
                .as("Trace ID should be present for troubleshooting")
                .isNotBlank();
    }


}