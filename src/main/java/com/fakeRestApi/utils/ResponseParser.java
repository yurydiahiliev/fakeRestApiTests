package com.fakeRestApi.utils;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class ResponseParser {

    private Response response;

    public Response response() {
        return response;
    }

    public JsonPath getJsonPath() {
        return response.jsonPath();
    }

    public String getContentType() {
        return response.getContentType();
    }

    public <T> T asPojo(Class<T> clz) {
        return response.as(clz);
    }

    public <T> List<T> asListOfPojo(Class<T> clz) {
        return response.jsonPath().getList("$", clz);
    }

    public int statusCode() {
        return response.getStatusCode();
    }

    public static ResponseParser of(Response response) {
        if (response == null) return ResponseParser.empty();
        return new ResponseParser(response);
    }

    public static ResponseParser empty() {
        return new ResponseParser(null);
    }

    public ResponseAssertions verify() {
        return new ResponseAssertions(this);
    }
}