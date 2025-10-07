package com.fakeRestApi.apiClient;

import com.fakeRestApi.models.Author;
import com.fakeRestApi.utils.ResponseParser;
import lombok.extern.slf4j.Slf4j;
import io.restassured.response.Response;

@Slf4j
public class AuthorsApi extends BaseApi {

    public static final String AUTHORS_PATH = "/Authors";
    private static final String AUTHORS_BY_BOOK_PATH = AUTHORS_PATH + "/authors/books/{idBook}";

    /**
     * GET /Authors — Retrieve all authors
     */
    public ResponseParser getAuthors() {
        log.info("Sending GET request to {}", AUTHORS_PATH);

        Response response = spec()
                .when()
                .get(AUTHORS_PATH)
                .then()
                .extract()
                .response();

        log.info("Received {} authors.", response.jsonPath().getList("$").size());

        return ResponseParser.of(response);
    }

    /**
     * GET /Authors/{id} — Retrieve a specific author by ID
     */
    public ResponseParser getAuthorById(int id) {
        log.info("Fetching author with ID {}", id);

        Response response = spec()
                .pathParam("id", id)
                .when()
                .get(AUTHORS_PATH + "/{id}")
                .then()
                .extract()
                .response();

        Author author = response.as(Author.class);
        log.info("Fetched author: {}", author);
        return ResponseParser.of(response);
    }

    public ResponseParser getAuthorById(String id) {
        log.info("Fetching author with ID {}", id);

        Response response = spec()
                .pathParam("id", id)
                .when()
                .get(AUTHORS_PATH + "/{id}")
                .then()
                .extract()
                .response();

        Author author = response.as(Author.class);
        log.info("Fetched author: {}", author);
        return ResponseParser.of(response);
    }

    /**
     * GET /Authors/authors/books/{idBook} — Retrieve authors by book ID
     */
    public ResponseParser getAuthorsByBookId(int idBook) {
        log.info("Fetching authors for book ID: {}.", idBook);

        Response response = spec()
                .pathParam("idBook", idBook)
                .when()
                .get(AUTHORS_BY_BOOK_PATH)
                .then()
                .extract()
                .response();

        log.info("Retrieved {} authors linked to book {}.", response.jsonPath().getList("$").size(), idBook);

        return ResponseParser.of(response);
    }

    public ResponseParser getAuthorsByBookId(String idBook) {
        log.info("Fetching authors for book ID: {}.", idBook);

        Response response = spec()
                .pathParam("idBook", idBook)
                .when()
                .get(AUTHORS_BY_BOOK_PATH)
                .then()
                .extract()
                .response();

        log.info("Retrieved {} authors linked to book {}.", response.jsonPath().getList("$").size(), idBook);

        return ResponseParser.of(response);
    }

    /**
     * POST /Authors — Create a new author
     */
    public ResponseParser createAuthor(Author author) {
        log.info("Creating new author: {}.", author);

        Response response = spec()
                .body(author)
                .when()
                .post(AUTHORS_PATH)
                .then()
                .extract()
                .response();

        Author createdAuthor = response.as(Author.class);
        log.info("Created author with ID {}.", createdAuthor.id());
        return ResponseParser.of(response);
    }

    /**
     * PUT /Authors/{id} — Update existing author
     */
    public ResponseParser updateAuthor(int id, Author updatedAuthor) {
        log.info("Updating author ID {}: {}.", id, updatedAuthor);

        Response response = spec()
                .pathParam("id", id)
                .body(updatedAuthor)
                .when()
                .put(AUTHORS_PATH + "/{id}")
                .then()
                .extract()
                .response();

        log.info("Updated author ID {} successfully.", id);
        return ResponseParser.of(response);
    }

    public ResponseParser updateAuthor(String id, Author updatedAuthor) {
        log.info("Updating author ID {}: {}.", id, updatedAuthor);

        Response response = spec()
                .pathParam("id", id)
                .body(updatedAuthor)
                .when()
                .put(AUTHORS_PATH + "/{id}")
                .then()
                .extract()
                .response();

        log.info("Updated author ID {} successfully.", id);
        return ResponseParser.of(response);
    }

    /**
     * DELETE /Authors/{id} — Remove author
     */
    public ResponseParser deleteAuthor(int id) {
        log.info("Deleting author ID {}.", id);

        Response response = spec()
                .pathParam("id", id)
                .when()
                .delete(AUTHORS_PATH + "/{id}");

        log.info("Deleted author ID {}.", id);
        return ResponseParser.of(response);
    }

    public ResponseParser deleteAuthor(String id) {
        log.info("Deleting author ID {}.", id);

        Response response = spec()
                .pathParam("id", id)
                .when()
                .delete(AUTHORS_PATH + "/{id}");

        log.info("Deleted author ID {}.", id);
        return ResponseParser.of(response);
    }
}
