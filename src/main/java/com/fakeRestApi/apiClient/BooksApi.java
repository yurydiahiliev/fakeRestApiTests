package com.fakeRestApi.apiClient;

import com.fakeRestApi.models.Book;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class BooksApi extends BaseApi {

    public static final String BOOKS_PATH = "/Books";

    /**
     * GET /Books – Retrieve all books with schema validation and logs
     */
    public List<Book> getBooks() {
        log.info("Sending GET request to {}.", BOOKS_PATH);

        Response response = spec()
                .when()
                .get(BOOKS_PATH)
                .then()
                .log().ifValidationFails()
                .statusCode(HttpStatus.SC_OK)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/book.json"))
                .extract()
                .response();

        log.info("Received {} books.", response.jsonPath().getList("$").size());

        return Arrays.asList(response.as(Book[].class));
    }

    /**
     * GET /Books/{id}
     */
    public Book getBookById(int id) {
        log.info("Fetching book by ID: {}", id);

        Response response = spec()
                .pathParam("id", id)
                .when()
                .get(BOOKS_PATH + "/{id}")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/singleBook.json"))
                .extract()
                .response();

        log.info("Book with ID {} retrieved successfully.", id);

        return response.as(Book.class);
    }

    /**
     * POST /Books
     */
    public Book createBook(Book book) {
        log.info("Creating new book: {}.", book);

        Response response = spec()
                .body(book)
                .when()
                .post(BOOKS_PATH)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/singleBook.json"))
                .extract()
                .response();

        Book createdBook = response.as(Book.class);
        log.info("Created book with ID {}.", createdBook.id());
        return createdBook;
    }

    /**
     * PUT /Books/{id}
     */
    public Book updateBook(int id, Book updatedBook) {
        log.info("Updating book with ID {}: {}", id, updatedBook);

        Response response = spec()
                .pathParam("id", id)
                .body(updatedBook)
                .when()
                .put(BOOKS_PATH + "/{id}")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/singleBook.json"))
                .extract()
                .response();

        Book book = response.as(Book.class);
        log.info("Updated book ID {} successfully.", id);
        return book;
    }

    public void deleteBook(int id) {
        log.info("Deleting book with ID {}.", id);

        spec()
                .pathParam("id", id)
                .when()
                .delete(BOOKS_PATH + "/{id}")
                .then()
                .statusCode(HttpStatus.SC_OK);

        log.info("Deleted book ID {}.", id);
    }
}