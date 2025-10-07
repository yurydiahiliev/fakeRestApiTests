package com.fakeRestApi.tests.book;

import com.fakeRestApi.models.Book;
import com.fakeRestApi.tests.BaseApiTest;
import com.fakeRestApi.utils.TestDataManager;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.time.Instant;

import static com.fakeRestApi.apiClient.BooksApi.BOOKS_PATH;
import static org.apache.http.HttpStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

@Epic("Fake REST API tests")
@Feature("Books API")
@Story("Update Books")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UpdateBooksTests extends BaseApiTest {

    @Test
    @Description("Verify updating an existing book successfully changes its fields and returns 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    void checkUpdateExistingBookShouldReturn200() {
        Book book = booksApi.createBook(TestDataManager.bookWithValidAllFields());
        Book updated = book.toBuilder()
                .title("Updated " + book.title())
                .description("Updated description " + Instant.now())
                .pageCount(book.pageCount() + 10)
                .excerpt("Updated excerpt " + Instant.now())
                .publishDate(Instant.now().toString())
                .build();

        Response response = booksApi.spec()
                .body(updated)
                .when()
                .put(BOOKS_PATH + "/" + book.id())
                .then()
                .extract()
                .response();

        assertThat(response.statusCode())
                .as("Updating an existing book should return 200 OK")
                .isEqualTo(SC_OK);

        Book updatedBook = response.as(Book.class);

        assertThat(updatedBook.id()).isEqualTo(book.id());
        assertThat(updatedBook.title()).isEqualTo(updated.title());
        assertThat(updatedBook.description()).isEqualTo(updated.description());
        assertThat(updatedBook.pageCount()).isEqualTo(updated.pageCount());
    }

    @Test
    @Description("Verify updating a non-existent book returns 404 Not Found")
    @Severity(SeverityLevel.NORMAL)
    void checkUpdateNonExistentBookShouldReturn404() {
        Book update = TestDataManager.bookWithValidAllFields().toBuilder()
                .id(999999)
                .build();

        Response response = booksApi.spec()
                .body(update)
                .when()
                .put(BOOKS_PATH + "/" + update.id())
                .then()
                .extract()
                .response();

        assertThat(response.statusCode())
                .as("Expected 404 Not Found when updating a non-existent book")
                .isEqualTo(SC_NOT_FOUND);

        assertThat(response.jsonPath().getString("title"))
                .as("Error should describe resource not found")
                .isEqualTo("Not Found");
    }

    @Test
    @Description("Verify updating a book with null fields returns 400 Bad Request")
    @Severity(SeverityLevel.NORMAL)
    void checkUpdateBookWithNullFieldsShouldReturn400() {
        Book book = booksApi.createBook(TestDataManager.bookWithValidAllFields());
        Book updated = book.toBuilder()
                .title(null)
                .description(null)
                .excerpt(null)
                .publishDate(null)
                .build();

        Response response = booksApi.spec()
                .body(updated)
                .when()
                .put(BOOKS_PATH + "/" + book.id())
                .then()
                .extract()
                .response();

        assertThat(response.statusCode())
                .as("Updating a book with null fields should return 400 Bad Request")
                .isEqualTo(SC_BAD_REQUEST);
    }

    @Test
    @Description("Verify updating a book with empty strings returns 400 Bad Request")
    @Severity(SeverityLevel.NORMAL)
    void checkUpdateBookWithEmptyFieldsShouldReturn400() {
        Book book = booksApi.createBook(TestDataManager.bookWithValidAllFields());
        Book updated = book.toBuilder()
                .title("")
                .description("")
                .excerpt("")
                .publishDate("")
                .build();

        Response response = booksApi.spec()
                .body(updated)
                .when()
                .put(BOOKS_PATH + "/" + book.id())
                .then()
                .extract()
                .response();

        assertThat(response.statusCode())
                .as("Updating a book with empty strings should return 400 Bad Request")
                .isEqualTo(SC_BAD_REQUEST);
    }

    @Test
    @Description("Verify updating each single field one by one (title, description, pageCount, excerpt, publishDate)")
    @Severity(SeverityLevel.NORMAL)
    void checkUpdateBookSingleFieldsIndividually() {
        Book book = booksApi.createBook(TestDataManager.bookWithValidAllFields());

        String[] fields = {"title", "description", "pageCount", "excerpt", "publishDate"};
        for (String field : fields) {
            Book updated = switch (field) {
                case "title" -> book.toBuilder().title("Updated title " + Instant.now()).build();
                case "description" -> book.toBuilder().description("Updated desc " + Instant.now()).build();
                case "pageCount" -> book.toBuilder().pageCount(book.pageCount() + 5).build();
                case "excerpt" -> book.toBuilder().excerpt("Updated excerpt " + Instant.now()).build();
                case "publishDate" -> book.toBuilder().publishDate(Instant.now().toString()).build();
                default -> throw new IllegalArgumentException("Unexpected field: " + field);
            };

            Response response = booksApi.spec()
                    .body(updated)
                    .when()
                    .put(BOOKS_PATH + "/" + book.id())
                    .then()
                    .extract()
                    .response();

            assertThat(response.statusCode())
                    .as("Updating single field '%s' should return 200 OK", field)
                    .isEqualTo(SC_OK);
        }
    }

    @Test
    @Description("Verify updating a book without a request body returns 400 Bad Request")
    @Severity(SeverityLevel.CRITICAL)
    void checkUpdateBookWithoutBodyShouldReturn400() {
        int bookId = 1;

        Response response = booksApi.spec()
                .when()
                .put(BOOKS_PATH + "/" + bookId)
                .then()
                .extract()
                .response();

        assertThat(response.statusCode())
                .as("Updating without body should return 400 Bad Request")
                .isEqualTo(SC_BAD_REQUEST);

        assertThat(response.jsonPath().getString("errors.\"\"[0]"))
                .as("Error message should mention missing body")
                .contains("A non-empty request body is required.");
    }
}
