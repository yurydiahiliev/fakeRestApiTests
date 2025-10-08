package com.fakeRestApi.utils;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic response parser for RestAssured responses.
 * Provides typed access to response content, status, and schema validation.
 *
 * @param <T> type of the POJO represented by the response
 */
@AllArgsConstructor
@NoArgsConstructor
public class ResponseParser<T> {

    private Response response;
    private Class<T> type;

    /**
     * Returns the raw RestAssured Response object.
     * @return response instance
     */
    public Response response() {
        return response;
    }

    /**
     * Returns a JsonPath object for querying JSON content.
     * @return JsonPath instance
     */
    public JsonPath getJsonPath() {
        return response.jsonPath();
    }

    /**
     * Returns the Content-Type header from the response.
     * @return content type as a string
     */
    public String getContentType() {
        return response.getContentType();
    }

    /**
     * Deserializes the response body into a single POJO of the defined type.
     * @return deserialized POJO or null if response or type is null
     */
    public T asPojo() {
        if (response == null || type == null) return null;
        return response.as(type);
    }

    /**
     * Deserializes the response body into a list of POJOs of the defined type.
     * @return list of deserialized POJOs, or an empty list if response or type is null
     */
    public List<T> asListOfPojo() {
        if (response == null || type == null) return List.of();
        return response.jsonPath().getList("$", type);
    }

    /**
     * Returns the HTTP status code of the response.
     * @return status code integer
     */
    public int statusCode() {
        return response.getStatusCode();
    }

    /**
     * Creates a typed ResponseParser instance from a response and class type.
     * @param response RestAssured response
     * @param type class type for deserialization
     * @param <T> generic type of the POJO
     * @return new typed ResponseParser instance or empty parser if response is null
     */
    public static <T> ResponseParser<T> of(Response response, Class<T> type) {
        if (response == null) return ResponseParser.empty(type);
        return new ResponseParser<>(response, type);
    }

    /**
     * Returns an empty ResponseParser for the specified type.
     * @param type class type for deserialization
     * @param <T> generic type of the POJO
     * @return empty typed ResponseParser
     */
    public static <T> ResponseParser<T> empty(Class<T> type) {
        return new ResponseParser<>(null, type);
    }

    /**
     * Provides access to ResponseAssertions for fluent verification.
     * @return new ResponseAssertions instance
     */
    public ResponseAssertions<T> verify() {
        return new ResponseAssertions<>(this);
    }
}