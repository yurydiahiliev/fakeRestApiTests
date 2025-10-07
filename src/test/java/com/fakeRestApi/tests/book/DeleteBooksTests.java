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
import io.restassured.response.Response;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.fakeRestApi.apiClient.BooksApi.BOOKS_PATH;
import static org.apache.http.HttpStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

@Epic("Fake REST API tests")
@Feature("Books API")
@Story("Delete Books")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(SoftAssertionsExtension.class)
public class DeleteBooksTests extends BaseApiTest {

    @Test
    @Description("Verify that deleting an existing book returns 204 No Content and subsequent GET returns 404 Not Found")
    @Severity(SeverityLevel.CRITICAL)
    void checkDeleteExistingBookShouldReturnOkAndBookShouldBeGone() {
        Book createdBook = booksApi.createBook(TestDataManager.bookWithValidAllFields()).asPojo();

        booksApi
                .deleteBook(createdBook.id())
                .verify()
                .verifyStatusCodeOk();

        booksApi.getBookById(createdBook.id())
                .verify()
                .verifyStatusCodeNotFound()
                .verifyStringJsonPath("title", "Not Found");
    }

    @Test
    @Description("Verify deleting a non-existent book returns 404 Not Found")
    @Severity(SeverityLevel.NORMAL)
    void checkDeleteNonexistentBookShouldReturnNotFound() {
        int nonexistentId = 88888888;

        booksApi
                .deleteBook(nonexistentId)
                .verify()
                .verifyStatusCodeNotFound()
                .verifyStringJsonPath("title", "Not Found")
                .verifyStringJsonPath("type", "https://tools.ietf.org/html/rfc7231#section-6.5.1");
    }

    @ParameterizedTest(name = "DELETE /Books/{0} should return 400 Bad Request")
    @ValueSource(strings = {"abc", "!", "ю", "$", "null"})
    @Description("Verify that invalid book IDs return 400 Bad Request")
    @Severity(SeverityLevel.NORMAL)
    void checkDeleteInvalidBookIdShouldReturnBadRequest(String invalidId) {
        booksApi
                .deleteBook(invalidId)
                .verify()
                .verifyStatusCodeBadRequest()
                .verifyContentType("application/problem+json")
                .verifyStringJsonPathIsNotBlank("title");
    }

    @Test
    @Description("Verify DELETE /Books without specifying ID returns 405 Method Not Allowed")
    @Severity(SeverityLevel.MINOR)
    void checkDeleteWithoutIdShouldReturn405() {
        Response response = booksApi.spec()
                .when()
                .delete(BOOKS_PATH)
                .then()
                .extract()
                .response();

        assertThat(response.statusCode())
                .as("DELETE /Books (without ID) should be disallowed by API design")
                .isEqualTo(SC_METHOD_NOT_ALLOWED);
    }

    @Test
    @Description("Verify double deletion returns 404 for the second call")
    @Severity(SeverityLevel.NORMAL)
    void checkDeleteBookTwiceShouldReturnNotFoundSecondTime() {
        Book createdBook = booksApi.createBook(TestDataManager.bookWithValidAllFields()).asPojo();

        booksApi
                .deleteBook(createdBook.id())
                .verify()
                .verifyStatusCodeOk();

        booksApi
                .deleteBook(createdBook.id())
                .verify()
                .verifyStatusCodeNotFound()
                .verifyStringJsonPath("title", "Not Found");
    }
}