package com.fakeRestApi.apiClient;

import com.fakeRestApi.models.Author;
import lombok.extern.slf4j.Slf4j;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class AuthorsApi extends BaseApi {

    public static final String AUTHORS_PATH = "/Authors";
    private static final String AUTHORS_BY_BOOK_PATH = AUTHORS_PATH + "/authors/books/{idBook}";

    /**
     * GET /Authors — Retrieve all authors
     */
    public List<Author> getAuthors() {
        log.info("Sending GET request to {}", AUTHORS_PATH);

        Response response = spec()
                .when()
                .get(AUTHORS_PATH)
                .then()
                .log().ifValidationFails()
                .statusCode(HttpStatus.SC_OK)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/author.json"))
                .extract()
                .response();

        log.info("Received {} authors.", response.jsonPath().getList("$").size());

        return Arrays.asList(response.as(Author[].class));
    }

    /**
     * GET /Authors/{id} — Retrieve a specific author by ID
     */
    public Author getAuthorById(int id) {
        log.info("Fetching author with ID {}", id);

        Response response = spec()
                .pathParam("id", id)
                .when()
                .get(AUTHORS_PATH + "/{id}")
                .then()
                .log().ifValidationFails()
                .statusCode(HttpStatus.SC_OK)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/singleAuthor.json"))
                .extract()
                .response();

        Author author = response.as(Author.class);
        log.info("Fetched author: {}", author);
        return author;
    }

    /**
     * GET /Authors/authors/books/{idBook} — Retrieve authors by book ID
     */
    public List<Author> getAuthorsByBookId(int idBook) {
        log.info("Fetching authors for book ID: {}.", idBook);

        Response response = spec()
                .pathParam("idBook", idBook)
                .when()
                .get(AUTHORS_BY_BOOK_PATH)
                .then()
                .log().ifValidationFails()
                .statusCode(HttpStatus.SC_OK)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/author.json"))
                .extract()
                .response();

        log.info("Retrieved {} authors linked to book {}.", response.jsonPath().getList("$").size(), idBook);

        return Arrays.asList(response.as(Author[].class));
    }

    /**
     * POST /Authors — Create a new author
     */
    public Author createAuthor(Author author) {
        log.info("Creating new author: {}.", author);

        Response response = spec()
                .body(author)
                .when()
                .post(AUTHORS_PATH)
                .then()
                .log().ifValidationFails()
                .statusCode(HttpStatus.SC_OK)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/singleAuthor.json"))
                .extract()
                .response();

        Author createdAuthor = response.as(Author.class);
        log.info("Created author with ID {}.", createdAuthor.id());
        return createdAuthor;
    }

    /**
     * PUT /Authors/{id} — Update existing author
     */
    public Author updateAuthor(int id, Author updatedAuthor) {
        log.info("Updating author ID {}: {}.", id, updatedAuthor);

        Response response = spec()
                .pathParam("id", id)
                .body(updatedAuthor)
                .when()
                .put(AUTHORS_PATH + "/{id}")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/singleAuthor.json"))
                .extract()
                .response();

        Author author = response.as(Author.class);
        log.info("Updated author ID {} successfully.", id);
        return author;
    }

    /**
     * DELETE /Authors/{id} — Remove author
     */
    public void deleteAuthor(int id) {
        log.info("Deleting author ID {}.", id);

        spec()
                .pathParam("id", id)
                .when()
                .delete(AUTHORS_PATH + "/{id}")
                .then()
                .log().ifValidationFails()
                .statusCode(HttpStatus.SC_OK);

        log.info("Deleted author ID {}.", id);
    }
}
