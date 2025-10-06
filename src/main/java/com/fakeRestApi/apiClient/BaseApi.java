package com.fakeRestApi.apiClient;

import com.fakeRestApi.config.ConfigHandler;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.Filter;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseApi {

    protected final RequestSpecification requestSpecification;

    protected BaseApi() {
        String baseUrl = ConfigHandler.getBaseUrl();
        String logLevel = ConfigHandler.getLogLevel();

        List<Filter> filters = new ArrayList<>();
        filters.add(new AllureRestAssured());

        if (isVerbose(logLevel)) {
            filters.add(new RequestLoggingFilter());
            filters.add(new ResponseLoggingFilter());
        }

        this.requestSpecification = new RequestSpecBuilder()
                .setBaseUri(baseUrl)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilters(filters)
                .build()
                .log().ifValidationFails(LogDetail.ALL);
    }

    private boolean isVerbose(String level) {
        return "DEBUG".equalsIgnoreCase(level) || "TRACE".equalsIgnoreCase(level);
    }

    protected RequestSpecification spec() {
        return requestSpecification;
    }
}