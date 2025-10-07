package com.fakeRestApi.tests.book;

import com.fakeRestApi.models.Book;
import com.fakeRestApi.tests.BaseApiTest;
import com.fakeRestApi.utils.TestDataManager;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.Story;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static com.fakeRestApi.apiClient.BooksApi.BOOKS_PATH;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;

@Epic("Fake REST API tests")
@Feature("Books API")
@Story("Create Books")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(SoftAssertionsExtension.class)
public class CreateBooksTests extends BaseApiTest {

    @Test
    @Description("Verify that a user can create a book with all required fields and they are correctly returned")
    @Severity(SeverityLevel.CRITICAL)
    void checkUserCanCreateBookWithAllFields(SoftAssertions softly) {
        Book book = TestDataManager.bookWithValidAllFields();
        Book createdBook = booksApi.createBook(book)
                .verify()
                .verifyStatusCodeOk()
                .validateJsonSchema("schemas/singleBook.json")
                .toResponse()
                .asPojo();

        softly.assertThat(createdBook)
                .as("Created book object should not be null")
                .isNotNull();

        softly.assertThat(createdBook.id())
                .as("Book ID should be positive")
                .isPositive();

        softly.assertThat(createdBook.title())
                .as("Book title should match the sent title")
                .isEqualTo(book.title());

        softly.assertThat(createdBook.description())
                .as("Book description should match the sent description")
                .isEqualTo(book.description());

        softly.assertThat(createdBook.pageCount())
                .as("Book page count should match the sent page count")
                .isEqualTo(book.pageCount());

        softly.assertThat(createdBook.excerpt())
                .as("Book excerpt should match the sent excerpt")
                .isEqualTo(book.excerpt());

        softly.assertThat(createdBook.publishDate())
                .as("Book publish date should not be blank and should match the format")
                .isNotBlank()
                .matches("^\\d{4}-\\d{2}-\\d{2}T.*$");
    }

    @Test
    @Description("Verify that creating a book with empty fields except 'publishDate' returns 400 Bad Request")
    @Severity(SeverityLevel.NORMAL)
    void checkUserCanCreateBookWithEmptyFieldsExceptDate(SoftAssertions softly) {
        Book book = TestDataManager.bookWithEmptyFields().toBuilder()
                .publishDate(Instant.now().toString())
                .build();
        Book createdBook = booksApi.createBook(book)
                .verify()
                .verifyStatusCodeOk()
                .validateJsonSchema("schemas/singleBook.json")
                .toResponse()
                .asPojo();

        softly.assertThat(createdBook)
                .as("Created book object should not be null")
                .isNotNull();

        softly.assertThat(createdBook.id())
                .as("Book ID should be zero")
                .isZero();

        softly.assertThat(createdBook.title())
                .as("Book title should match the sent title")
                .isEqualTo(book.title());

        softly.assertThat(createdBook.description())
                .as("Book description should match the sent description")
                .isEqualTo(book.description());

        softly.assertThat(createdBook.pageCount())
                .as("Book page count should match the sent page count")
                .isEqualTo(book.pageCount());

        softly.assertThat(createdBook.excerpt())
                .as("Book excerpt should match the sent excerpt")
                .isEqualTo(book.excerpt());

        softly.assertThat(createdBook.publishDate())
                .as("Book publish date should not be blank and should match the format")
                .isNotBlank()
                .matches("^\\d{4}-\\d{2}-\\d{2}T.*$");
    }

    @Test
    @Description("Verify that creating a book with empty all fields returns 400 Bad Request")
    @Severity(SeverityLevel.NORMAL)
    void checkUserCanCreateBookWithEmptyFields() {
        Book bookWithEmptyAllFields = TestDataManager.bookWithEmptyFields();

        booksApi.createBook(bookWithEmptyAllFields)
                .verify()
                .verifyContentType("application/problem+json")
                .verifyStringJsonPath("title", "One or more validation errors occurred.")
                .verifyIntegerJsonPath("status", SC_BAD_REQUEST)
                .verifyStringJsonPath("errors['$.publishDate'][0]", "The JSON value could not be converted to System.DateTime. Path: $.publishDate | LineNumber: 0 | BytePositionInLine:")
                .verifyStringJsonPathIsNotBlank("traceId");
    }

    @Test
    @Description("Verify that creating a book with invalid or empty publishDate returns 400 Bad Request and correct validation error")
    @Severity(SeverityLevel.NORMAL)
    void checkCreateBookWithEmptyPublishDateShouldReturnBadRequest() {
        Book bookWithEmptyPublishDate = TestDataManager.generateValidBookBuilder()
                .publishDate("")
                .build();

        booksApi.createBook(bookWithEmptyPublishDate)
                .verify()
                .verifyContentType("application/problem+json")
                .verifyStringJsonPath("title", "One or more validation errors occurred.")
                .verifyIntegerJsonPath("status", SC_BAD_REQUEST)
                .verifyStringJsonPath("errors['$.publishDate'][0]", "The JSON value could not be converted to System.DateTime. Path: $.publishDate | LineNumber: 0 | BytePositionInLine:")
                .verifyStringJsonPathIsNotBlank("traceId");
    }

    @Test
    @Description("Verify that creating a book with empty fields returns sucess")
    @Severity(SeverityLevel.NORMAL)
    void checkUserCanCreateBookWithNullFields(SoftAssertions softly) {
        Book book = TestDataManager.bookWithNullFields();
        Book createdBook = booksApi.createBook(book)
                .verify()
                .verifyStatusCodeOk()
                .validateJsonSchema("schemas/singleBook.json")
                .toResponse()
                .asPojo();

        softly.assertThat(createdBook)
                .as("Created book object should not be null")
                .isNotNull();

        softly.assertThat(createdBook.id())
                .as("Book ID should be zero")
                .isZero();

        softly.assertThat(createdBook.title())
                .as("Book title should match the sent title")
                .isEqualTo(book.title());

        softly.assertThat(createdBook.description())
                .as("Book description should match the sent description")
                .isEqualTo(book.description());

        softly.assertThat(createdBook.pageCount())
                .as("Book page count should match zero")
                .isEqualTo(0);

        softly.assertThat(createdBook.excerpt())
                .as("Book excerpt should match the sent excerpt")
                .isEqualTo(book.excerpt());

        softly.assertThat(createdBook.publishDate())
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
        Book createdBook = booksApi.createBook(partialBook)
                .verify()
                .verifyStatusCodeOk()
                .toResponse()
                .asPojo();

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
    void checkUserCanCreateBookWithSameDataTwice(SoftAssertions softly) {
        Book originalBook = TestDataManager.bookWithValidAllFields();
        Book firstCreatedBook = booksApi.createBook(originalBook)
                .verify()
                .verifyStatusCodeOk()
                .validateJsonSchema("schemas/singleBook.json")
                .toResponse()
                .asPojo();
        Book secondCreatedBook = booksApi.createBook(originalBook)
                .verify()
                .verifyStatusCodeOk()
                .validateJsonSchema("schemas/singleBook.json")
                .toResponse()
                .asPojo();

        softly.assertThat(firstCreatedBook)
                .as("First created book should not be null")
                .isNotNull();

        softly.assertThat(secondCreatedBook)
                .as("Second created book should not be null")
                .isNotNull();

        softly.assertThat(firstCreatedBook.id())
                .as("API echoes back same ID for duplicate POST calls")
                .isEqualTo(secondCreatedBook.id());

        softly.assertThat(firstCreatedBook)
                .usingRecursiveComparison()
                .ignoringFields("publishDate")
                .as("Both created books should have identical content except publishDate")
                .isEqualTo(secondCreatedBook);
    }

    @Test
    @Description("Create a book with numeric-only title and description")
    @Severity(SeverityLevel.NORMAL)
    void checkCreateBookWithNumericStrings(SoftAssertions softly) {
        Book book = TestDataManager.generateValidBookBuilder()
                .title("1234567890")
                .description("9876543210")
                .build();

        Book created = booksApi.createBook(book)
                .verify()
                .verifyStatusCodeOk()
                .validateJsonSchema("schemas/singleBook.json")
                .toResponse()
                .asPojo();

        softly.assertThat(created)
                .as("Response should contain numeric title and description as-is")
                .isNotNull();
        softly.assertThat(created.title()).isEqualTo("1234567890");
        softly.assertThat(created.description()).isEqualTo("9876543210");
    }

    @Test
    @Description("Create a book with special characters in all string fields")
    @Severity(SeverityLevel.NORMAL)
    void checkCreateBookWithSpecialSymbols(SoftAssertions softly) {
        String symbols = "!@#$%^&*()_+-={}[]:\";'<>?,./|\\`~";
        Book book = TestDataManager.generateValidBookBuilder()
                .title(symbols)
                .description(symbols)
                .excerpt(symbols)
                .build();

        Book created = booksApi.createBook(book)
                .verify()
                .verifyStatusCodeOk()
                .validateJsonSchema("schemas/singleBook.json")
                .toResponse()
                .asPojo();

        softly.assertThat(created)
                .as("API should accept special symbols without sanitization errors")
                .isNotNull();
        softly.assertThat(created.title()).containsAnyOf("@", "#", "$");
    }

    @Test
    @Description("Create a book with emoji and unicode characters")
    @Severity(SeverityLevel.MINOR)
    void checkCreateBookWithEmojiAndUnicode(SoftAssertions softly) {
        String emojiTitle = "The ☀️ Sun & The 🌙 Moon";
        String unicodeDescription = "Привіт 🌍 — こんにちは世界 — مرحبا بالعالم";

        Book book = TestDataManager.generateValidBookBuilder()
                .title(emojiTitle)
                .description(unicodeDescription)
                .excerpt("🧪 test excerpt 🌟")
                .build();

        Book created = booksApi.createBook(book).verify()
                .verifyStatusCodeOk()
                .validateJsonSchema("schemas/singleBook.json")
                .toResponse()
                .asPojo();

        softly.assertThat(created)
                .as("API should correctly handle UTF-8 and emoji characters")
                .isNotNull();
        softly.assertThat(created.title()).contains("☀️").contains("🌙");
        softly.assertThat(created.description()).contains("Привіт").contains("こんにちは");
    }

    @Test
    @Description("Create a book with a title that includes mixed characters")
    @Severity(SeverityLevel.NORMAL)
    void checkCreateBookWithMixedCharacters(SoftAssertions softly) {
        String mixed = "Book_№42_🔥_!@#_漢字";
        Book book = TestDataManager.generateValidBookBuilder()
                .title(mixed)
                .description("Test " + mixed)
                .build();

        Book created = booksApi.createBook(book)
                .verify()
                .verifyStatusCodeOk()
                .validateJsonSchema("schemas/singleBook.json")
                .toResponse()
                .asPojo();

        softly.assertThat(created.title())
                .as("Title should retain mixed characters")
                .isEqualTo(mixed);
        softly.assertThat(created.description())
                .contains("Test");
    }

    @Test
    @Description("Verify that sending POST /Books without a request body returns 400 Bad Request")
    @Severity(SeverityLevel.CRITICAL)
    void checkCreateBookWithoutBodyShouldReturnBadRequest() {
        Response response = booksApi.spec()
                .when()
                .post(BOOKS_PATH)
                .then()
                .extract()
                .response();

        assertBadRequestResponse(response);

        String errorMessage = response.jsonPath().getString("errors.\"\"[0]");
        assertThat(errorMessage)
                .as("Error message should specify that the request body is missing")
                .isEqualTo("A non-empty request body is required.");
    }

    @Test
    @Description("Verify that creating a book with a past publish date is allowed and the date is stored correctly")
    @Severity(SeverityLevel.NORMAL)
    void checkCreateBookWithPastPublishDate(SoftAssertions softly) {
        Instant pastDate = LocalDateTime.now()
                .minusYears(5)
                .atZone(ZoneOffset.UTC)
                .toInstant();

        Book book = TestDataManager.generateValidBookBuilder()
                .publishDate(pastDate.toString())
                .build();

        Book createdBook = booksApi.createBook(book).verify()
                .verifyStatusCodeOk()
                .validateJsonSchema("schemas/singleBook.json")
                .toResponse()
                .asPojo();

        softly.assertThat(createdBook)
                .as("Created book object should not be null")
                .isNotNull();

        softly.assertThat(createdBook.id())
                .as("Book ID should be positive")
                .isPositive();

        softly.assertThat(createdBook.title())
                .as("Book title should match the sent title")
                .isEqualTo(book.title());

        softly.assertThat(createdBook.description())
                .as("Book description should match the sent description")
                .isEqualTo(book.description());

        softly.assertThat(createdBook.pageCount())
                .as("Page count should match the sent value")
                .isEqualTo(book.pageCount());

        softly.assertThat(createdBook.excerpt())
                .as("Excerpt should match the sent value")
                .isEqualTo(book.excerpt());

        softly.assertThat(createdBook.publishDate())
                .as("Publish date should match the provided past date")
                .startsWith(pastDate.toString().substring(0, 10));
    }

    private void assertBadRequestResponse(Response response) {
        assertThat(response.statusCode())
                .as("Expected HTTP 400 Bad Request")
                .isEqualTo(SC_BAD_REQUEST);

        assertThat(response.getContentType())
                .as("Response should use RFC7807 problem+json format")
                .contains("application/problem+json");

        String title = response.jsonPath().getString("title");
        Integer status = response.jsonPath().getInt("status");
        String type = response.jsonPath().getString("type");
        String traceId = response.jsonPath().getString("traceId");

        String errorMessage = response.jsonPath().getString("errors.\"\"[0]");

        assertThat(title)
                .as("Title should describe validation failure")
                .isEqualTo("One or more validation errors occurred.");

        assertThat(status)
                .as("Status in body should match HTTP 400")
                .isEqualTo(SC_BAD_REQUEST);

        assertThat(type)
                .as("Type should reference RFC 7231 section 6.5.1")
                .contains("rfc7231#section-6.5.1");

        assertThat(errorMessage)
                .as("Error message should be present and meaningful")
                .isNotBlank();

        assertThat(traceId)
                .as("Trace ID should be present for troubleshooting")
                .isNotBlank();
    }
}