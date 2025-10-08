package com.fakeRestApi.apiClient;

import com.fakeRestApi.models.Book;
import com.fakeRestApi.utils.ResponseParser;
import lombok.extern.slf4j.Slf4j;

/**
 * API client for /Books endpoints.
 */
@Slf4j
public class BooksApi extends BaseApi<Book> {

    public static final String BOOKS_PATH = "/Books";

    public BooksApi() {
        super(Book.class);
    }

    /**
     * GET /Books — Retrieve all books with schema validation
     */
    public ResponseParser<Book> getBooks() {
        log.info("Fetching all books...");

        var responseParser = get(BOOKS_PATH);

        log.info("Received {} books.", responseParser.getJsonPath().getList("$").size());
        return responseParser;
    }

    /**
     * GET /Books/{id} — Retrieve a specific book by ID
     */
    public ResponseParser<Book> getBookById(Object id) {
        log.info("Fetching book by ID {}...", id);
        ResponseParser<Book> response = get(BOOKS_PATH + "/{id}", "id", id);
        log.info("Book fetched: {}", response.asPojo());
        return response;
    }

    /**
     * POST /Books — Create a new book
     */
    public ResponseParser<Book> createBook(Book book) {
        log.info("Creating new book: {}", book);
        ResponseParser<Book> response = post(BOOKS_PATH, book);
        log.info("Created book: {}", response.asPojo());
        return response;
    }

    /**
     * PUT /Books/{id} — Update existing book
     */
    public ResponseParser<Book> updateBook(Object id, Book updatedBook) {
        log.info("Updating book ID {}: {}", id, updatedBook);
        ResponseParser<Book> response = put(BOOKS_PATH + "/{id}", "id", id, updatedBook);
        log.info("Updated book ID {} successfully.", id);
        return response;
    }

    /**
     * DELETE /Books/{id} — Remove a book
     */
    public ResponseParser<Book> deleteBook(Object id) {
        log.info("Deleting book ID {}.", id);
        ResponseParser<Book> response = delete(BOOKS_PATH + "/{id}", "id", id);
        log.info("Deleted book ID {}.", id);
        return response;
    }
}