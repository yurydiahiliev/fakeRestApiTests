package com.fakeRestApi.tests.author;

import com.fakeRestApi.models.Author;
import com.fakeRestApi.tests.BaseApiTest;
import com.fakeRestApi.utils.TestDataManager;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.ThreadLocalRandom;

import static com.fakeRestApi.apiClient.AuthorsApi.AUTHORS_PATH;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;

@Epic("Fake REST API tests")
@Feature("Authors API")
@Story("Create Authors")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CreateAuthorsTests extends BaseApiTest {

    @Test
    @Description("Verify that user can create an author with all required valid fields")
    @Severity(SeverityLevel.CRITICAL)
    void checkUserCanCreateAuthorWithAllFields() {
        Author author = TestDataManager.authorWithValidAllFields();
        Author created = authorsApi.createAuthor(author);

        assertThat(created)
                .as("Created author object should not be null")
                .isNotNull();

        assertThat(created.id()).as("Author ID should be positive").isPositive();
        assertThat(created.firstName()).isEqualTo(author.firstName());
        assertThat(created.lastName()).isEqualTo(author.lastName());
        assertThat(created.idBook()).isEqualTo(author.idBook());
    }

    @Test
    @Description("Verify that user can create an author with minimal valid data")
    @Severity(SeverityLevel.NORMAL)
    void checkUserCanCreateAuthorWithMinimalData() {
        Author author = Author.builder()
                .id(ThreadLocalRandom.current().nextInt(1, 9999))
                .idBook(1)
                .firstName("John")
                .lastName("Doe")
                .build();

        Author created = authorsApi.createAuthor(author);

        assertThat(created)
                .as("Created author should not be null")
                .isNotNull();

        assertThat(created.firstName()).isEqualTo("John");
        assertThat(created.lastName()).isEqualTo("Doe");
        assertThat(created.idBook()).isEqualTo(1);
    }

    @Test
    @Description("Verify that creating an author with empty fields returns 400 Bad Request")
    @Severity(SeverityLevel.NORMAL)
    void checkCreateAuthorWithEmptyFieldsShouldReturnBadRequest() {
        Author author = Author.builder()
                .id(0)
                .idBook(0)
                .firstName("")
                .lastName("")
                .build();

        Response response = authorsApi.spec()
                .body(author)
                .when()
                .post(AUTHORS_PATH)
                .then()
                .extract()
                .response();

        assertThat(response.statusCode())
                .as("Expected 400 Bad Request for invalid empty fields")
                .isEqualTo(SC_BAD_REQUEST);

        String title = response.jsonPath().getString("title");
        assertThat(title).contains("validation");
    }

    @Test
    @Description("Verify that creating an author with null fields returns 400 Bad Request")
    @Severity(SeverityLevel.NORMAL)
    void checkCreateAuthorWithNullFieldsShouldReturnBadRequest() {
        Author author = Author.builder()
                .id(null)
                .idBook(null)
                .firstName(null)
                .lastName(null)
                .build();

        Response response = authorsApi.spec()
                .body(author)
                .when()
                .post(AUTHORS_PATH)
                .then()
                .extract()
                .response();

        assertThat(response.statusCode())
                .as("Expected 400 Bad Request for null fields")
                .isEqualTo(SC_BAD_REQUEST);

        assertThat(response.jsonPath().getString("title"))
                .contains("validation");
    }

    @ParameterizedTest(name = "Create author with single field: {0}")
    @ValueSource(strings = {"firstName", "lastName", "idBook"})
    @Description("Verify creating author with only one populated field behaves as expected")
    @Severity(SeverityLevel.NORMAL)
    void checkCreateAuthorWithSingleField(String fieldName) {
        Author author = TestDataManager.authorWithSingleField(fieldName);
        Response response = authorsApi.spec()
                .body(author)
                .when()
                .post(AUTHORS_PATH)
                .then()
                .extract()
                .response();

        assertThat(response.statusCode())
                .as("Creating with idBook only should fail with 400 Bad Request")
                .isEqualTo(SC_OK);
    }

    @Test
    @Description("Verify that creating duplicate authors is allowed and returns same data (fake API behavior)")
    @Severity(SeverityLevel.NORMAL)
    void checkCreateDuplicateAuthorIsAllowed() {
        Author author = TestDataManager.authorWithValidAllFields();
        Author first = authorsApi.createAuthor(author);
        Author second = authorsApi.createAuthor(author);

        assertThat(first)
                .as("First created author should not be null")
                .isNotNull();

        assertThat(second)
                .as("Second created author should not be null")
                .isNotNull();

        assertThat(first)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(second);
    }

    @Test
    @Description("Verify that creating author with special characters in names is accepted")
    @Severity(SeverityLevel.MINOR)
    void checkCreateAuthorWithSpecialCharacters() {
        Author author = TestDataManager.generateValidAuthorBuilder()
                .firstName("Jean-Luc 🧑‍🚀")
                .lastName("O'Neill #42")
                .build();

        Author created = authorsApi.createAuthor(author);

        assertThat(created.firstName()).contains("Jean-Luc");
        assertThat(created.lastName()).contains("#42");
    }

    @Test
    @Description("Verify that POST /Authors without body returns 400 Bad Request")
    @Severity(SeverityLevel.CRITICAL)
    void checkCreateAuthorWithoutBodyShouldReturn400() {
        Response response = authorsApi.spec()
                .when()
                .post(AUTHORS_PATH)
                .then()
                .extract()
                .response();

        assertThat(response.statusCode())
                .as("Expected 400 Bad Request for empty request body")
                .isEqualTo(SC_BAD_REQUEST);

        String error = response.jsonPath().getString("errors.\"\"[0]");
        assertThat(error).isEqualTo("A non-empty request body is required.");
    }
}