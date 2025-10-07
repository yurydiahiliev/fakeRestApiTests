package com.fakeRestApi.tests.author;

import com.fakeRestApi.models.Author;
import com.fakeRestApi.tests.BaseApiTest;
import com.fakeRestApi.utils.TestDataManager;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.fakeRestApi.apiClient.AuthorsApi.AUTHORS_PATH;
import static org.apache.http.HttpStatus.*;
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
        Author createdAuthor = authorsApi.createAuthor(TestDataManager.authorWithValidFields());

        Response deleteResponse = authorsApi.spec()
                .when()
                .delete(AUTHORS_PATH + "/" + createdAuthor.id())
                .then()
                .extract()
                .response();

        assertThat(deleteResponse.statusCode())
                .as("Deleting existing author should return 200 OK")
                .isEqualTo(SC_OK);

        Response getResponse = authorsApi.spec()
                .when()
                .get(AUTHORS_PATH + "/" + createdAuthor.id())
                .then()
                .extract()
                .response();

        assertThat(getResponse.statusCode())
                .as("Subsequent GET after DELETE should return 404 Not Found")
                .isEqualTo(SC_NOT_FOUND);

        assertThat(getResponse.jsonPath().getString("title"))
                .as("Error title should describe 'Not Found'")
                .isEqualTo("Not Found");
    }

    @Test
    @Description("Verify deleting a non-existent author returns 404 Not Found")
    @Severity(SeverityLevel.NORMAL)
    void checkDeleteNonexistentAuthorShouldReturnNotFound() {
        int nonexistentId = 88888888;

        Response response = authorsApi.spec()
                .when()
                .delete(AUTHORS_PATH + "/" + nonexistentId)
                .then()
                .extract()
                .response();

        assertThat(response.statusCode())
                .as("Expected 404 Not Found for deleting non-existent author")
                .isEqualTo(SC_NOT_FOUND);

        assertThat(response.jsonPath().getString("title"))
                .as("Error should describe 'Not Found'")
                .isEqualTo("Not Found");

        assertThat(response.jsonPath().getString("type"))
                .as("Type should reference RFC 7231 6.5.4")
                .contains("7231#section-6.5.4");
    }

    @ParameterizedTest(name = "DELETE /Authors/{0} should return 400 Bad Request")
    @ValueSource(strings = {"abc", "!", "$", "ю", "null"})
    @Description("Verify that invalid author IDs return 400 Bad Request")
    @Severity(SeverityLevel.NORMAL)
    void checkDeleteInvalidAuthorIdShouldReturn400(String invalidId) {
        Response response = authorsApi.spec()
                .when()
                .delete(AUTHORS_PATH + "/" + invalidId)
                .then()
                .extract()
                .response();

        assertThat(response.statusCode())
                .as("Expected 400 Bad Request for invalid ID: %s", invalidId)
                .isEqualTo(SC_BAD_REQUEST);

        assertThat(response.jsonPath().getString("title"))
                .as("Error should mention validation issue")
                .containsIgnoringCase("validation");
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

        assertThat(response.jsonPath().getString("title"))
                .as("Error title should indicate method not allowed")
                .containsIgnoringCase("method not allowed");
    }

    @Test
    @Description("Verify double deletion returns 404 for the second call")
    @Severity(SeverityLevel.NORMAL)
    void checkDeleteAuthorTwiceShouldReturnNotFoundSecondTime() {
        Author createdAuthor = authorsApi.createAuthor(TestDataManager.authorWithValidFields());

        Response firstDelete = authorsApi.spec()
                .when()
                .delete(AUTHORS_PATH + "/" + createdAuthor.id())
                .then()
                .extract()
                .response();

        assertThat(firstDelete.statusCode())
                .as("First DELETE should return 200 OK")
                .isEqualTo(SC_OK);

        Response secondDelete = authorsApi.spec()
                .when()
                .delete(AUTHORS_PATH + "/" + createdAuthor.id())
                .then()
                .extract()
                .response();

        assertThat(secondDelete.statusCode())
                .as("Second DELETE on same ID should return 404 Not Found")
                .isEqualTo(SC_NOT_FOUND);

        assertThat(secondDelete.jsonPath().getString("title"))
                .as("Error should indicate 'Not Found'")
                .isEqualTo("Not Found");
    }
}