package com.fakeRestApi.utils;

import io.restassured.module.jsv.JsonSchemaValidator;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Fluent assertion helper for validating API responses.
 * Keeps generic type consistency with ResponseParser<T>.
 *
 * @param <T> Type of the response POJO
 */
@RequiredArgsConstructor
public class ResponseAssertions<T> {

    private final ResponseParser<T> responseParser;

    /**
     * Verifies that the response status code matches the expected value.
     * @param expected expected HTTP status code
     * @return this ResponseAssertions instance
     */
    public ResponseAssertions<T> hasStatusCode(int expected) {
        int actual = responseParser.statusCode();
        assertThat(actual)
                .as("Status Code mismatch. Expected: %d but was: %d", expected, actual)
                .isEqualTo(expected);
        return this;
    }

    /**
     * Verifies that the response status code is 200 OK.
     * @return this ResponseAssertions instance
     */
    public ResponseAssertions<T> verifyStatusCodeOk() {
        return hasStatusCode(SC_OK);
    }

    /**
     * Verifies that the response status code is 404 Not Found.
     * @return this ResponseAssertions instance
     */
    public ResponseAssertions<T> verifyStatusCodeNotFound() {
        return hasStatusCode(SC_NOT_FOUND);
    }

    /**
     * Verifies that the response status code is 400 Bad Request.
     * @return this ResponseAssertions instance
     */
    public ResponseAssertions<T> verifyStatusCodeBadRequest() {
        return hasStatusCode(SC_BAD_REQUEST);
    }

    /**
     * Verifies that the response Content-Type header matches the expected value.
     * @param expectedContentType expected content type string
     * @return this ResponseAssertions instance
     */
    public ResponseAssertions<T> verifyContentType(String expectedContentType) {
        String actual = responseParser.getContentType();
        assertThat(actual)
                .as("Content-Type mismatch. Expected: %s, Actual: %s", expectedContentType, actual)
                .contains(expectedContentType);
        return this;
    }

    /**
     * Verifies that the value of a JSON path matches the expected string.
     * @param jsonPath JSON path expression
     * @param expectedValue expected string value
     * @return this ResponseAssertions instance
     */
    public ResponseAssertions<T> verifyStringJsonPath(String jsonPath, String expectedValue) {
        String actual = responseParser.getJsonPath().getString(jsonPath);
        assertThat(actual)
                .as("JSON path '%s' value mismatch. Expected: %s, Actual: %s", jsonPath, expectedValue, actual)
                .contains(expectedValue);
        return this;
    }

    /**
     * Verifies that the value at a given JSON path is not blank.
     * @param jsonPath JSON path expression
     * @return this ResponseAssertions instance
     */
    public ResponseAssertions<T> verifyStringJsonPathIsNotBlank(String jsonPath) {
        String actual = responseParser.getJsonPath().getString(jsonPath);
        assertThat(actual)
                .as("Expected non-blank value at JSON path '%s'", jsonPath)
                .isNotBlank();
        return this;
    }

    /**
     * Verifies that the integer value at a JSON path matches the expected value.
     * @param jsonPath JSON path expression
     * @param expectedValue expected integer value
     * @return this ResponseAssertions instance
     */
    public ResponseAssertions<T> verifyIntegerJsonPath(String jsonPath, int expectedValue) {
        int actual = responseParser.getJsonPath().getInt(jsonPath);
        assertThat(actual)
                .as("JSON path '%s' integer value mismatch. Expected: %d, Actual: %d", jsonPath, expectedValue, actual)
                .isEqualTo(expectedValue);
        return this;
    }

    /**
     * Verifies that the response body matches the expected POJO.
     * @param expected expected POJO object
     * @return this ResponseAssertions instance
     */
    public ResponseAssertions<T> verifyBodyEqualsToPojo(T expected) {
        T actual = responseParser.asPojo();
        assertThat(actual)
                .as("Response body not equal to expected object")
                .isEqualTo(expected);
        return this;
    }

    /**
     * Verifies that the response contains a non-empty list of POJOs.
     * @return this ResponseAssertions instance
     */
    public ResponseAssertions<T> verifyPojoListNotEmpty() {
        List<T> list = responseParser.asListOfPojo();
        assertThat(list)
                .as("Expected non-empty list in response body")
                .isNotNull()
                .isNotEmpty();
        return this;
    }

    /**
     * Validates the response body against a JSON schema from the classpath.
     * @param schemaPath path to the JSON schema file
     * @return this ResponseAssertions instance
     */
    public ResponseAssertions<T> validateJsonSchema(String schemaPath) {
        responseParser.response()
                .then()
                .assertThat()
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath(schemaPath));
        return this;
    }

    /**
     * Returns the underlying ResponseParser for further chaining.
     * @return ResponseParser instance
     */
    public ResponseParser<T> toResponse() {
        return responseParser;
    }
}