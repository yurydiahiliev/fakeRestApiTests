package com.fakeRestApi.tests.author;

import com.fakeRestApi.models.Author;
import com.fakeRestApi.tests.BaseApiTest;
import com.fakeRestApi.utils.TestDataManager;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.fakeRestApi.apiClient.AuthorsApi.AUTHORS_PATH;
import static org.apache.http.HttpStatus.SC_METHOD_NOT_ALLOWED;
import static org.assertj.core.api.Assertions.assertThat;

@Epic("Fake REST API tests")
@Feature("Authors API")
@Story("Delete Authors")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DeleteAuthorsTests extends BaseApiTest {

    @Test
    @Description("Verify that deleting an existing author returns 200 OK and subsequent GET returns 404 Not Found")
    @Severity(SeverityLevel.CRITICAL)
    void checkDeleteExistingAuthorShouldReturnOkAndAuthorShouldBeGone() {
        Author createdAuthor = authorsApi
                .createAuthor(TestDataManager.authorWithValidFields())
                .asPojo();

        authorsApi
                .deleteAuthor(createdAuthor.id())
                .verify()
                .verifyStatusCodeOk();

        authorsApi
                .getAuthorById(createdAuthor.id())
                .verify()
                .verifyStatusCodeNotFound()
                .verifyStringJsonPath("title", "Not Found");
    }

    @Test
    @Description("Verify deleting a non-existent author returns 404 Not Found")
    @Severity(SeverityLevel.NORMAL)
    void checkDeleteNonexistentAuthorShouldReturnNotFound() {
        int nonexistentId = 88888888;

        authorsApi
                .deleteAuthor(nonexistentId)
                .verify()
                .verifyStatusCodeNotFound()
                .verifyStringJsonPath("title", "Not Found")
                .verifyStringJsonPath("type", "7231#section-6.5.4");
    }

    @ParameterizedTest(name = "DELETE /Authors/{0} should return 400 Bad Request")
    @ValueSource(strings = {"abc", "!", "$", "ю", "null"})
    @Description("Verify that invalid author IDs return 400 Bad Request")
    @Severity(SeverityLevel.NORMAL)
    void checkDeleteInvalidAuthorIdShouldReturnBadRequest(String invalidId) {
        authorsApi
                .deleteAuthor(invalidId)
                .verify()
                .verifyStatusCodeBadRequest()
                .verifyContentType("application/problem+json")
                .verifyStringJsonPathIsNotBlank("title");
    }

    @Test
    @Description("Verify DELETE /Authors without specifying ID returns 405 Method Not Allowed")
    @Severity(SeverityLevel.MINOR)
    void checkDeleteWithoutIdShouldReturn405() {
        Response response = authorsApi.spec()
                .when()
                .delete(AUTHORS_PATH)
                .then()
                .extract()
                .response();

        assertThat(response.statusCode())
                .as("DELETE /Authors (without ID) should be disallowed by API design")
                .isEqualTo(SC_METHOD_NOT_ALLOWED);
    }

    @Test
    @Description("Verify double deletion returns 404 for the second call")
    @Severity(SeverityLevel.NORMAL)
    void checkDeleteAuthorTwiceShouldReturnNotFoundSecondTime() {
        Author createdAuthor = authorsApi
                .createAuthor(TestDataManager.authorWithValidFields())
                .asPojo();

        authorsApi
                .deleteAuthor(createdAuthor.id())
                .verify()
                .verifyStatusCodeOk();

        authorsApi
                .deleteAuthor(createdAuthor.id())
                .verify()
                .verifyStatusCodeNotFound()
                .verifyStringJsonPath("title", "Not Found");
    }
}