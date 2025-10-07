package com.fakeRestApi.apiClient;

import com.fakeRestApi.config.ConfigHandler;
import com.fakeRestApi.utils.ResponseParser;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.Filter;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

/**
 * Base API client providing reusable HTTP operations and shared configuration.
 *
 * @param <T> entity type used for deserialization (e.g., Author, Book)
 */
public abstract class BaseApi<T> {

    protected final RequestSpecification requestSpecification;
    private final Class<T> entityClass;

    /**
     * Initializes a new BaseApi with default configuration and optional logging.
     * Loads base URL and log level from ConfigHandler.
     * @param entityClass class type used for response deserialization
     */
    protected BaseApi(Class<T> entityClass) {
        this.entityClass = entityClass;

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

    /**
     * Checks whether the configured log level is verbose.
     * @param level log level string (DEBUG or TRACE)
     * @return true if verbose logging should be enabled
     */
    private boolean isVerbose(String level) {
        return "DEBUG".equalsIgnoreCase(level) || "TRACE".equalsIgnoreCase(level);
    }

    /**
     * Returns a base RestAssured RequestSpecification for reuse.
     * @return RequestSpecification instance
     */
    public RequestSpecification spec() {
        return given().spec(requestSpecification);
    }

    /**
     * Sends a GET request to the specified path and returns a typed response parser.
     * @param path request endpoint path
     * @return ResponseParser with typed entity
     */
    protected ResponseParser<T> get(String path) {
        Response response = spec().when().get(path).then().extract().response();
        return ResponseParser.of(response, entityClass);
    }

    /**
     * Sends a GET request with a single path parameter.
     * @param path request endpoint path
     * @param paramName name of the path parameter
     * @param paramValue value of the path parameter
     * @return ResponseParser with typed entity
     */
    protected ResponseParser<T> get(String path, String paramName, Object paramValue) {
        Response response = spec().pathParam(paramName, paramValue)
                .when().get(path)
                .then().extract().response();
        return ResponseParser.of(response, entityClass);
    }

    /**
     * Sends a POST request with a request body.
     * @param path request endpoint path
     * @param body request body object
     * @return ResponseParser with typed entity
     */
    protected ResponseParser<T> post(String path, Object body) {
        Response response = spec().body(body)
                .when().post(path)
                .then().extract().response();
        return ResponseParser.of(response, entityClass);
    }

    /**
     * Sends a PUT request with a path parameter and request body.
     * @param path request endpoint path
     * @param paramName name of the path parameter
     * @param paramValue value of the path parameter
     * @param body request body object
     * @return ResponseParser with typed entity
     */
    protected ResponseParser<T> put(String path, String paramName, Object paramValue, Object body) {
        Response response = spec().pathParam(paramName, paramValue)
                .body(body)
                .when().put(path)
                .then().extract().response();
        return ResponseParser.of(response, entityClass);
    }

    /**
     * Sends a DELETE request with a single path parameter.
     * @param path request endpoint path
     * @param paramName name of the path parameter
     * @param paramValue value of the path parameter
     * @return ResponseParser with typed entity
     */
    protected ResponseParser<T> delete(String path, String paramName, Object paramValue) {
        Response response = spec().pathParam(paramName, paramValue)
                .when().delete(path)
                .then().extract().response();
        return ResponseParser.of(response, entityClass);
    }
}