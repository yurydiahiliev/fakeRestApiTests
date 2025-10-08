package com.fakeRestApi.tests;

import com.fakeRestApi.apiClient.AuthorsApi;
import com.fakeRestApi.apiClient.BooksApi;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;

import java.lang.reflect.Method;

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
        log.info("---- STARTED: {} - {}", testInfo.getDisplayName(), getTestMethodName(testInfo));
    }

    @AfterEach
    @Step("Finish test")
    void logTestFinish(TestInfo testInfo) {
        log.info("---- FINISHED: {} - {}", testInfo.getDisplayName(), getTestMethodName(testInfo));
    }

    @AfterAll
    @Step("Tear down test environment")
    void tearDown() {
        log.info("========== TEST SUITE FINISHED ==========");
        RestAssured.reset();
    }

    private String getTestMethodName(TestInfo testInfo) {
        return testInfo.getTestMethod().map(Method::getName).orElse("unknown");
    }
}