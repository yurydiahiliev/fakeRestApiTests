package com.fakeRestApi.tests.book;

import com.fakeRestApi.models.Book;
import com.fakeRestApi.tests.BaseApiTest;
import com.fakeRestApi.utils.TestDataManager;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Instant;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;

@Epic("Fake REST API tests")
@Feature("Books API")
@Story("Update Books")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(SoftAssertionsExtension.class)
public class UpdateBooksTests extends BaseApiTest {

    @Test
    @Description("Verify updating an existing book successfully changes its fields and returns 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    void checkUpdateExistingBookShouldReturnOk(SoftAssertions softly) {
        Book existingBook = booksApi.createBook(TestDataManager.bookWithValidAllFields()).verify()
                .verifyStatusCodeOk()
                .validateJsonSchema("schemas/singleBook.json")
                .toResponse()
                .asPojo();
        Book updatedBook = existingBook.toBuilder()
                .title("Updated " + existingBook.title())
                .description("Updated description " + Instant.now())
                .pageCount(existingBook.pageCount() + 10)
                .excerpt("Updated excerpt " + Instant.now())
                .publishDate(Instant.now().toString())
                .build();

        Book updatedBookResponse = booksApi
                .updateBook(existingBook.id(), updatedBook)
                .verify()
                .verifyStatusCodeOk()
                .validateJsonSchema("schemas/singleBook.json")
                .toResponse()
                .asPojo();

        softly.assertThat(updatedBookResponse.id()).isEqualTo(existingBook.id());
        softly.assertThat(updatedBookResponse.title()).isEqualTo(updatedBook.title());
        softly.assertThat(updatedBookResponse.description()).isEqualTo(updatedBook.description());
        softly.assertThat(updatedBookResponse.pageCount()).isEqualTo(updatedBook.pageCount());
    }

    @Test
    @Description("Verify updating a non-existent book returns 404 Not Found")
    @Severity(SeverityLevel.NORMAL)
    void checkUpdateNonExistentBookShouldReturnNotFound() {
        Book updatedBook = TestDataManager.bookWithValidAllFields().toBuilder()
                .id(9999999)
                .build();

        booksApi
                .updateBook(updatedBook.id(), updatedBook)
                .verify()
                .verifyStatusCodeNotFound()
                .verifyStringJsonPath("title", "Not Found");
    }

    @Test
    @Description("Verify updating a book with null fields returns 400 Bad Request")
    @Severity(SeverityLevel.NORMAL)
    void checkUpdateBookWithNullFieldsShouldReturnBadRequest() {
        Book book = booksApi.createBook(TestDataManager.bookWithValidAllFields()).asPojo();
        Book updatedBook = book.toBuilder()
                .title(null)
                .description(null)
                .excerpt(null)
                .publishDate(null)
                .build();

        booksApi.updateBook(book.id(), updatedBook)
                .verify()
                .verifyStatusCodeBadRequest()
                .verifyContentType("application/problem+json")
                .verifyIntegerJsonPath("status", SC_BAD_REQUEST);
    }

    @Test
    @Description("Verify updating a book with empty strings returns 400 Bad Request")
    @Severity(SeverityLevel.NORMAL)
    void checkUpdateBookWithEmptyFieldsShouldReturn400() {
        Book book = booksApi.createBook(TestDataManager.bookWithValidAllFields()).asPojo();
        Book updatedBook = book.toBuilder()
                .title("")
                .description("")
                .excerpt("")
                .publishDate("")
                .build();

        booksApi.updateBook(book.id(), updatedBook)
                .verify()
                .verifyStatusCodeBadRequest()
                .verifyContentType("application/problem+json")
                .verifyIntegerJsonPath("status", SC_BAD_REQUEST);
    }

    @Test
    @Description("Verify updating each single field one by one (title, description, pageCount, excerpt, publishDate)")
    @Severity(SeverityLevel.NORMAL)
    void checkUpdateBookSingleFieldsIndividually() {
        Book book = booksApi.createBook(TestDataManager.bookWithValidAllFields()).asPojo();

        String[] fields = {"title", "description", "pageCount", "excerpt", "publishDate"};
        for (String field : fields) {
            Book updatedBook = switch (field) {
                case "title" -> book.toBuilder().title("Updated title " + Instant.now()).build();
                case "description" -> book.toBuilder().description("Updated desc " + Instant.now()).build();
                case "pageCount" -> book.toBuilder().pageCount(book.pageCount() + 5).build();
                case "excerpt" -> book.toBuilder().excerpt("Updated excerpt " + Instant.now()).build();
                case "publishDate" -> book.toBuilder().publishDate(Instant.now().toString()).build();
                default -> throw new IllegalArgumentException("Unexpected field: " + field);
            };

            booksApi.updateBook(book.id(), updatedBook)
                    .verify()
                    .verifyStatusCodeOk();
        }
    }

    @Test
    @Description("Verify updating a book without a request body returns 400 Bad Request")
    @Severity(SeverityLevel.CRITICAL)
    void checkUpdateBookWithoutBodyShouldReturnBadRequest() {
        int bookId = 1;

        booksApi
                .updateBook(bookId, null)
                .verify()
                .verifyStatusCodeBadRequest()
                .verifyContentType("application/problem+json")
                .verifyStringJsonPath("title", "One or more validation errors occurred.")
                .verifyStringJsonPath("type", "https://tools.ietf.org/html/rfc7231#section-6.5.1")
                .verifyIntegerJsonPath("status", SC_BAD_REQUEST);
    }
}
