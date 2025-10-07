package com.fakeRestApi.tests.book;

import com.fakeRestApi.models.Book;
import com.fakeRestApi.utils.TestDataManager;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.Story;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static com.fakeRestApi.apiClient.BooksApi.BOOKS_PATH;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;

@Epic("Fake REST API tests")
@Feature("Books API")
@Story("Create Books")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CreateBooksTests extends BaseApiTest {

    @Test
    @Description("Verify that a user can create a book with all required fields and they are correctly returned")
    @Severity(SeverityLevel.CRITICAL)
    void checkUserCanCreateBookWithAllFields() {
        Book book = TestDataManager.bookWithValidAllFields();
        Book createdBook = booksApi.createBook(book);

        assertThat(createdBook)
                .as("Created book object should not be null")
                .isNotNull();

        assertThat(createdBook.id())
                .as("Book ID should be positive")
                .isPositive();

        assertThat(createdBook.title())
                .as("Book title should match the sent title")
                .isEqualTo(book.title());

        assertThat(createdBook.description())
                .as("Book description should match the sent description")
                .isEqualTo(book.description());

        assertThat(createdBook.pageCount())
                .as("Book page count should match the sent page count")
                .isEqualTo(book.pageCount());

        assertThat(createdBook.excerpt())
                .as("Book excerpt should match the sent excerpt")
                .isEqualTo(book.excerpt());

        assertThat(createdBook.publishDate())
                .as("Book publish date should not be blank and should match the format")
                .isNotBlank()
                .matches("^\\d{4}-\\d{2}-\\d{2}T.*$");
    }

    @Test
    @Description("Verify that creating a book with empty fields except 'publishDate' returns 400 Bad Request")
    @Severity(SeverityLevel.NORMAL)
    void checkUserCanCreateBookWithEmptyFieldsExceptDate() {
        Book book = TestDataManager.bookWithEmptyFields().toBuilder()
                .publishDate(Instant.now().toString())
                .build();
        Book createdBook = booksApi.createBook(book);

        assertThat(createdBook)
                .as("Created book object should not be null")
                .isNotNull();

        assertThat(createdBook.id())
                .as("Book ID should be zero")
                .isZero();

        assertThat(createdBook.title())
                .as("Book title should match the sent title")
                .isEqualTo(book.title());

        assertThat(createdBook.description())
                .as("Book description should match the sent description")
                .isEqualTo(book.description());

        assertThat(createdBook.pageCount())
                .as("Book page count should match the sent page count")
                .isEqualTo(book.pageCount());

        assertThat(createdBook.excerpt())
                .as("Book excerpt should match the sent excerpt")
                .isEqualTo(book.excerpt());

        assertThat(createdBook.publishDate())
                .as("Book publish date should not be blank and should match the format")
                .isNotBlank()
                .matches("^\\d{4}-\\d{2}-\\d{2}T.*$");
    }

    @Test
    @Description("Verify that creating a book with empty all fields returns 400 Bad Request")
    @Severity(SeverityLevel.NORMAL)
    void checkUserCanCreateBookWithEmptyFields() {
        Book bookWithEmptyAllFields = TestDataManager.bookWithEmptyFields();
        Response response = booksApi.spec()
                .body(bookWithEmptyAllFields)
                .when()
                .post(BOOKS_PATH)
                .then()
                .extract()
                .response();

        assertThat(response.statusCode())
                .as("Expected 400 Bad Request for invalid publishDate")
                .isEqualTo(SC_BAD_REQUEST);

        assertThat(response.getContentType())
                .as("Response should use RFC 7807 problem JSON format")
                .contains("application/problem+json");

        String title = response.jsonPath().getString("title");
        Integer status = response.jsonPath().getInt("status");
        String type = response.jsonPath().getString("type");
        String traceId = response.jsonPath().getString("traceId");
        String errorMsg = response.jsonPath().getString("errors.'$.publishDate'[0]");

        assertThat(title)
                .as("Title should describe validation failure")
                .isEqualTo("One or more validation errors occurred.");

        assertThat(status)
                .as("Status field in body should match HTTP status code 400")
                .isEqualTo(SC_BAD_REQUEST);

        assertThat(type)
                .as("Error type should reference RFC 7231 6.5.1 section")
                .contains("https://tools.ietf.org/html/rfc7231#section-6.5.1");

        assertThat(errorMsg)
                .as("Error message should mention publishDate conversion issue")
                .contains("could not be converted to System.DateTime");

        assertThat(traceId)
                .as("Trace ID should be present for request tracking")
                .isNotBlank();
    }

    @Test
    @Description("Verify that creating a book with invalid or empty publishDate returns 400 Bad Request and correct validation error")
    @Severity(SeverityLevel.NORMAL)
    void checkCreateBookWithEmptyPublishDateShouldReturnBadRequest() {
        Book bookWithEmptyPublishDate = TestDataManager.generateValidBookBuilder()
                .publishDate("")
                .build();

        Response response = booksApi.spec()
                .body(bookWithEmptyPublishDate)
                .when()
                .post(BOOKS_PATH)
                .then()
                .extract()
                .response();

        assertThat(response.statusCode())
                .as("Expected 400 Bad Request for invalid publishDate")
                .isEqualTo(SC_BAD_REQUEST);

        assertThat(response.getContentType())
                .as("Response should use RFC 7807 problem JSON format")
                .contains("application/problem+json");

        String title = response.jsonPath().getString("title");
        Integer status = response.jsonPath().getInt("status");
        String type = response.jsonPath().getString("type");
        String traceId = response.jsonPath().getString("traceId");
        String errorMsg = response.jsonPath().getString("errors.'$.publishDate'[0]");

        assertThat(title)
                .as("Title should describe validation failure")
                .isEqualTo("One or more validation errors occurred.");

        assertThat(status)
                .as("Status field in body should match HTTP status code 400")
                .isEqualTo(SC_BAD_REQUEST);

        assertThat(type)
                .as("Error type should reference RFC 7231 6.5.1 section")
                .contains("https://tools.ietf.org/html/rfc7231#section-6.5.1");

        assertThat(errorMsg)
                .as("Error message should mention publishDate conversion issue")
                .contains("could not be converted to System.DateTime");

        assertThat(traceId)
                .as("Trace ID should be present for request tracking")
                .isNotBlank();
    }

    @Test
    @Description("Verify that creating a book with empty fields returns sucess")
    @Severity(SeverityLevel.NORMAL)
    void checkUserCanCreateBookWithNullFields() {
        Book book = TestDataManager.bookWithNullFields();
        Book createdBook = booksApi.createBook(book);

        assertThat(createdBook)
                .as("Created book object should not be null")
                .isNotNull();

        assertThat(createdBook.id())
                .as("Book ID should be zero")
                .isZero();

        assertThat(createdBook.title())
                .as("Book title should match the sent title")
                .isEqualTo(book.title());

        assertThat(createdBook.description())
                .as("Book description should match the sent description")
                .isEqualTo(book.description());

        assertThat(createdBook.pageCount())
                .as("Book page count should match zero")
                .isEqualTo(0);

        assertThat(createdBook.excerpt())
                .as("Book excerpt should match the sent excerpt")
                .isEqualTo(book.excerpt());

        assertThat(createdBook.publishDate())
                .as("Book publish date should not be blank and should match the format")
                .isNotBlank()
                .matches("^\\d{4}-\\d{2}-\\d{2}T.*$");
    }

    @ParameterizedTest(name = "Create Book with only field: {0}")
    @ValueSource(strings = {"id", "title", "description", "pageCount", "excerpt", "publishDate"})
    @Description("Verify creating a Book with only one populated field behaves as expected")
    @Severity(SeverityLevel.NORMAL)
    void checkCreateBookWithSingleField(String fieldName) {
        Book partialBook = TestDataManager.bookWithSingleField(fieldName);
        Book createdBook = booksApi.createBook(partialBook);

        assertThat(createdBook)
                .as("Created book response should not be null for field: %s", fieldName)
                .isNotNull();

        switch (fieldName.toLowerCase()) {
            case "id" -> assertThat(createdBook.id())
                    .as("Book ID should not be null when provided")
                    .isNotNull();

            case "title" -> assertThat(createdBook.title())
                    .as("Book title should match input when only title is provided")
                    .isEqualTo(partialBook.title());

            case "description" -> assertThat(createdBook.description())
                    .as("Book description should match input")
                    .isEqualTo(partialBook.description());

            case "pagecount" -> assertThat(createdBook.pageCount())
                    .as("Book page count should match input")
                    .isEqualTo(partialBook.pageCount());

            case "excerpt" -> assertThat(createdBook.excerpt())
                    .as("Book excerpt should match input")
                    .isEqualTo(partialBook.excerpt());

            case "publishdate" -> assertThat(createdBook.publishDate())
                    .as("Book publishDate should match input format")
                    .isNotBlank()
                    .matches("^\\d{4}-\\d{2}-\\d{2}T.*Z?$");
        }

        assertThat(createdBook)
                .as("Other fields should have default values when not populated")
                .satisfies(book -> {
                    if (!fieldName.equalsIgnoreCase("id"))
                        assertThat(book.id())
                                .as("Default id should be present (server may override null with 0 or actual ID)")
                                .isNotNull();

                    if (!fieldName.equalsIgnoreCase("title"))
                        assertThat(book.title())
                                .as("Title should be null when not provided")
                                .isNull();

                    if (!fieldName.equalsIgnoreCase("description"))
                        assertThat(book.description())
                                .as("Description should be null when not provided")
                                .isNull();

                    if (!fieldName.equalsIgnoreCase("pageCount"))
                        assertThat(book.pageCount())
                                .as("PageCount should default to 0 if not provided")
                                .isZero();

                    if (!fieldName.equalsIgnoreCase("excerpt"))
                        assertThat(book.excerpt())
                                .as("Excerpt should be null when not provided")
                                .isNull();

                    if (!fieldName.equalsIgnoreCase("publishDate"))
                        assertThat(book.publishDate())
                                .as("PublishDate should default to '0001-01-01T00:00:00' if not provided")
                                .isIn("0001-01-01T00:00:00", null);
                });
    }

    @Test
    @Description("Verify that creating a book with identical data twice is allowed and both return the same book data")
    @Severity(SeverityLevel.NORMAL)
    void checkUserCanCreateBookWithSameDataTwice() {
        Book originalBook = TestDataManager.bookWithValidAllFields();
        Book firstCreatedBook = booksApi.createBook(originalBook);
        Book secondCreatedBook = booksApi.createBook(originalBook);

        assertThat(firstCreatedBook)
                .as("First created book should not be null")
                .isNotNull();

        assertThat(secondCreatedBook)
                .as("Second created book should not be null")
                .isNotNull();

        assertThat(firstCreatedBook.id())
                .as("API echoes back same ID for duplicate POST calls")
                .isEqualTo(secondCreatedBook.id());

        assertThat(firstCreatedBook)
                .usingRecursiveComparison()
                .ignoringFields("publishDate") // publishDate might differ slightly
                .as("Both created books should have identical content except publishDate")
                .isEqualTo(secondCreatedBook);
    }
}