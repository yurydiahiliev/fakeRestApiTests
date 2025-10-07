package com.fakeRestApi.apiClient;

import com.fakeRestApi.models.Author;
import com.fakeRestApi.utils.ResponseParser;
import lombok.extern.slf4j.Slf4j;

/**
 * API client for /Authors endpoints.
 */
@Slf4j
public class AuthorsApi extends BaseApi<Author> {

    public static final String AUTHORS_PATH = "/Authors";
    private static final String AUTHORS_BY_BOOK_PATH = AUTHORS_PATH + "/authors/books/{idBook}";

    public AuthorsApi() {
        super(Author.class);
    }

    /** GET /Authors — Retrieve all authors */
    public ResponseParser<Author> getAuthors() {
        log.info("Fetching all authors.");
        ResponseParser<Author> response = get(AUTHORS_PATH);
        log.info("Received {} authors.", response.getJsonPath().getList("$").size());
        return response;
    }

    /** GET /Authors/{id} — Retrieve author by ID */
    public ResponseParser<Author> getAuthorById(Object id) {
        log.info("Fetching author with ID {}.", id);
        ResponseParser<Author> response = get(AUTHORS_PATH + "/{id}", "id", id);
        log.info("Fetched author: {}", response.asPojo());
        return response;
    }

    /** GET /Authors/authors/books/{idBook} — Retrieve authors by book ID */
    public ResponseParser<Author> getAuthorsByBookId(Object idBook) {
        log.info("Fetching authors for book ID {}.", idBook);
        ResponseParser<Author> response = get(AUTHORS_BY_BOOK_PATH, "idBook", idBook);
        log.info("Retrieved {} authors linked to book {}.",
                response.getJsonPath().getList("$").size(), idBook);
        return response;
    }

    /** POST /Authors — Create a new author */
    public ResponseParser<Author> createAuthor(Author author) {
        log.info("Creating new author: {}", author);
        ResponseParser<Author> response = post(AUTHORS_PATH, author);
        log.info("Created author: {}", response.asPojo());
        return response;
    }

    /** PUT /Authors/{id} — Update existing author */
    public ResponseParser<Author> updateAuthor(Object id, Author updatedAuthor) {
        log.info("Updating author ID {}: {}", id, updatedAuthor);
        ResponseParser<Author> response = put(AUTHORS_PATH + "/{id}", "id", id, updatedAuthor);
        log.info("Updated author ID {} successfully.", id);
        return response;
    }

    /** DELETE /Authors/{id} — Delete author */
    public ResponseParser<Author> deleteAuthor(Object id) {
        log.info("Deleting author ID {}...", id);
        ResponseParser<Author> response = delete(AUTHORS_PATH + "/{id}", "id", id);
        log.info("Deleted author ID {}.", id);
        return response;
    }
}