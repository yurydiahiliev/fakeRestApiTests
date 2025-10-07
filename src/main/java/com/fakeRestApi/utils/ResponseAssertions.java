package com.fakeRestApi.utils;

import io.restassured.module.jsv.JsonSchemaValidator;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static org.apache.http.HttpStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class ResponseAssertions {

    private final ResponseParser responseParser;

    public ResponseAssertions hasStatusCode(int expected) {
        int actual = responseParser.statusCode();
        assertThat(actual)
                .as(actual + " Status Code not equal to - " + expected)
                .isEqualTo(expected);
        return this;
    }

    public ResponseAssertions verifyStatusCodeOk() {
        return hasStatusCode(SC_OK);
    }

    public ResponseAssertions verifyStatusCodeNotFound() {
        return hasStatusCode(SC_NOT_FOUND);
    }

    public ResponseAssertions verifyStatusCodeBadRequest() {
        return hasStatusCode(SC_BAD_REQUEST);
    }

    public <T> ResponseAssertions verifyBodyEqualsToPojo(Class<T> clazz, T expected) {
        T actual = responseParser.asPojo(clazz);
        assertThat(actual)
                .as("Response body not equals to - " + expected).
                isEqualTo(expected);
        return this;
    }

    public <T> ResponseAssertions verifyPojoListNotEmpty(Class<T> clazz) {
        List<T> list = responseParser.asListOfPojo(clazz);
        assertThat(list)
                .as("JSON List in Response Body is not empty - " + list.toString())
                .isNotNull()
                .isNotEmpty();
        return this;
    }

    public ResponseParser toResponse() {
        return responseParser;
    }

    public ResponseAssertions verifyContentType(String expectedContentType) {
        String responseContentType = responseParser.getContentType();
        assertThat(responseContentType)
                .as("Content-Type mismatch. Expected: %s, Actual: %s", expectedContentType, responseContentType)
                .contains(expectedContentType);
        return this;
    }

    public ResponseAssertions verifyStringJsonPath(String stringJsonPath, String expectedValue) {
        String actualValue = responseParser.getJsonPath().getString(stringJsonPath);
        assertThat(actualValue)
                .as("JSON path '%s' value mismatch. Expected: %s, Actual: %s", stringJsonPath, expectedValue, actualValue)
                .contains(expectedValue);
        return this;
    }

    public ResponseAssertions verifyStringJsonPathIsNotBlank(String stringJsonPath) {
        String actualValue = responseParser.getJsonPath().getString(stringJsonPath);
        assertThat(actualValue)
                .as("Expected non-blank value at JSON path '%s'", stringJsonPath)
                .isNotBlank();
        return this;
    }

    public ResponseAssertions verifyIntegerJsonPath(String intJsonPath, int expectedValue) {
        int actualValue = responseParser.getJsonPath().getInt(intJsonPath);
        assertThat(actualValue)
                .as("JSON path '%s' integer value mismatch. Expected: %d, Actual: %d", intJsonPath, expectedValue, actualValue)
                .isEqualTo(expectedValue);
        return this;
    }

    public ResponseAssertions validateJsonSchema(String jsonSchemaPath) {
        responseParser.response()
                .then()
                .assertThat()
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath(jsonSchemaPath));
        return this;
    }
}