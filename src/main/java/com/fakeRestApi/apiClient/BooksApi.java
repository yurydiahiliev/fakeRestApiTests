package com.fakeRestApi.apiClient;

import com.fakeRestApi.models.Book;
import com.fakeRestApi.utils.ResponseParser;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;

@Slf4j
public class BooksApi extends BaseApi {

    public static final String BOOKS_PATH = "/Books";

    /**
     * GET /Books – Retrieve all books with schema validation and logs
     */
    public ResponseParser getBooks() {
        log.info("Sending GET request to {}.", BOOKS_PATH);

        Response response = spec()
                .when()
                .get(BOOKS_PATH)
                .then()
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/book.json"))
                .extract()
                .response();

        log.info("Received {} books.", response.jsonPath().getList("$").size());

        return ResponseParser.of(response);
    }

    /**
     * GET /Books/{id}
     */
    public ResponseParser getBookById(int id) {
        log.info("Fetching book by ID: {}", id);

        Response response = spec()
                .when()
                .get(BOOKS_PATH + "/" + id)
                .then()
                .extract()
                .response();

        log.info("Book with ID {} retrieved successfully.", id);

        return ResponseParser.of(response);
    }

    public ResponseParser getBookById(String id) {
        log.info("Fetching book by ID: {}", id);

        Response response = spec()
                .when()
                .get(BOOKS_PATH + "/" + id)
                .then()
                .extract()
                .response();

        log.info("Book with ID {} retrieved successfully.", id);

        return ResponseParser.of(response);
    }

    /**
     * POST /Books
     */
    public ResponseParser createBook(Book book) {
        log.info("Creating new book: {}.", book);

        Response response = spec()
                .body(book)
                .when()
                .post(BOOKS_PATH)
                .then()
                .extract()
                .response();

        Book createdBook = response.as(Book.class);
        log.info("Created book with ID {}.", createdBook.id());
        return ResponseParser.of(response);
    }

    /**
     * PUT /Books/{id}
     */
    public ResponseParser updateBook(int id, Book updatedBook) {
        log.info("Updating book with ID {}: {}", id, updatedBook);

        RequestSpecification reqSpec = spec()
                .pathParam("id", id);

        if (updatedBook != null) {
            reqSpec.body(updatedBook);
        }
        Response response = reqSpec
                .when()
                .put(BOOKS_PATH + "/{id}")
                .then()
                .extract()
                .response();

        log.info("Updated book ID {} successfully.", id);
        return ResponseParser.of(response);
    }

    public ResponseParser deleteBook(int id) {
        log.info("Deleting book with ID {}.", id);

        Response response = spec()
                .pathParam("id", id)
                .when()
                .delete(BOOKS_PATH + "/{id}");

        log.info("Deleted book ID {}.", id);

        return ResponseParser.of(response);
    }

    public ResponseParser deleteBook(String id) {
        log.info("Deleting book with ID {}.", id);

        Response response = spec()
                .pathParam("id", id)
                .when()
                .delete(BOOKS_PATH + "/{id}");

        log.info("Deleted book ID {}.", id);

        return ResponseParser.of(response);
    }
}