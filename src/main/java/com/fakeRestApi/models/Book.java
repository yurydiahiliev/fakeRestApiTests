package com.fakeRestApi.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record Book(
        Integer id,
        String title,
        String description,
        Integer pageCount,
        String excerpt,
        String publishDate
) {}
