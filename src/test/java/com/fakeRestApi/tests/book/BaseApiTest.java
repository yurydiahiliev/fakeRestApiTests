package com.fakeRestApi.tests.book;

import com.fakeRestApi.apiClient.AuthorsApi;
import com.fakeRestApi.apiClient.BooksApi;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

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
}