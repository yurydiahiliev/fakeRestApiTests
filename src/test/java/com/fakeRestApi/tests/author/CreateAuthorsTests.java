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
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.ThreadLocalRandom;

import static com.fakeRestApi.apiClient.AuthorsApi.AUTHORS_PATH;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;

@Epic("Fake REST API tests")
@Feature("Authors API")
@Story("Create Authors")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(SoftAssertionsExtension.class)
public class CreateAuthorsTests extends BaseApiTest {

    @Test
    @Description("Verify that user can create an author with all required valid fields")
    @Severity(SeverityLevel.CRITICAL)
    void checkUserCanCreateAuthorWithAllFields(SoftAssertions softly) {
        Author author = TestDataManager.authorWithValidAllFields();
        Author created = authorsApi.createAuthor(author)
                .verify()
                .verifyStatusCodeOk()
                .validateJsonSchema("schemas/singleAuthor.json")
                .toResponse()
                .asPojo();

        softly.assertThat(created)
                .as("Created author object should not be null")
                .isNotNull();

        softly.assertThat(created.id()).as("Author ID should be positive").isPositive();
        softly.assertThat(created.firstName()).isEqualTo(author.firstName());
        softly.assertThat(created.lastName()).isEqualTo(author.lastName());
        softly.assertThat(created.idBook()).isEqualTo(author.idBook());
    }

    @Test
    @Description("Verify that user can create an author with minimal valid data")
    @Severity(SeverityLevel.NORMAL)
    void checkUserCanCreateAuthorWithMinimalData(SoftAssertions softly) {
        Author author = Author.builder()
                .id(ThreadLocalRandom.current().nextInt(1, 9999))
                .idBook(1)
                .firstName("John")
                .lastName("Doe")
                .build();

        Author created = authorsApi.createAuthor(author)
                .verify()
                .verifyStatusCodeOk()
                .validateJsonSchema("schemas/singleAuthor.json")
                .toResponse()
                .asPojo();

        softly.assertThat(created)
                .as("Created author should not be null")
                .isNotNull();

        softly.assertThat(created.firstName()).isEqualTo("John");
        softly.assertThat(created.lastName()).isEqualTo("Doe");
        softly.assertThat(created.idBook()).isEqualTo(1);
    }

    @Test
    @Description("Verify that creating an author with empty fields returns 400 Bad Request")
    @Severity(SeverityLevel.NORMAL)
    void checkCreateAuthorWithEmptyFieldsShouldReturnBadRequest() {
        Author author = TestDataManager.authorWithEmptyFields();

        authorsApi
                .createAuthor(author)
                .verify()
                .verifyStatusCodeBadRequest()
                .verifyIntegerJsonPath("status", SC_BAD_REQUEST)
                .verifyContentType("application/problem+json")
                .verifyStringJsonPathIsNotBlank("title");
    }

    @Test
    @Description("Verify that creating an author with null fields returns 400 Bad Request")
    @Severity(SeverityLevel.NORMAL)
    void checkCreateAuthorWithNullFieldsShouldReturnBadRequest() {
        Author author = TestDataManager.authorWithNullFields();

        authorsApi
                .createAuthor(author)
                .verify()
                .verifyStatusCodeBadRequest()
                .verifyIntegerJsonPath("status", SC_BAD_REQUEST)
                .verifyContentType("application/problem+json")
                .verifyStringJsonPathIsNotBlank("title");
    }

    @ParameterizedTest(name = "Create author with single field: {0}")
    @ValueSource(strings = {"firstName", "lastName", "idBook"})
    @Description("Verify creating author with only one populated field behaves as expected")
    @Severity(SeverityLevel.NORMAL)
    void checkCreateAuthorWithSingleField(String fieldName) {
        Author author = TestDataManager.authorWithSingleField(fieldName);

        authorsApi
                .createAuthor(author)
                .verify()
                .verifyStatusCodeOk()
                .verifyBodyEqualsToPojo(author);
    }

    @Test
    @Description("Verify that creating duplicate authors is allowed and returns same data (fake API behavior)")
    @Severity(SeverityLevel.NORMAL)
    void checkCreateDuplicateAuthorIsAllowed(SoftAssertions softly) {
        Author author = TestDataManager.authorWithValidAllFields();
        Author first = authorsApi.createAuthor(author).verify()
                .verifyStatusCodeOk()
                .validateJsonSchema("schemas/singleAuthor.json")
                .toResponse()
                .asPojo();
        Author second = authorsApi.createAuthor(author).verify()
                .verifyStatusCodeOk()
                .validateJsonSchema("schemas/singleAuthor.json")
                .toResponse()
                .asPojo();

        softly.assertThat(first)
                .as("First created author should not be null")
                .isNotNull();

        softly.assertThat(second)
                .as("Second created author should not be null")
                .isNotNull();

        softly.assertThat(first)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(second);
    }

    @Test
    @Description("Verify that creating author with special characters in names is accepted")
    @Severity(SeverityLevel.MINOR)
    void checkCreateAuthorWithSpecialCharacters(SoftAssertions softly) {
        Author author = TestDataManager.generateValidAuthorBuilder()
                .firstName("Jean-Luc 🧑‍🚀")
                .lastName("O'Neill #42")
                .build();

        Author created = authorsApi.createAuthor(author).verify()
                .verifyStatusCodeOk()
                .validateJsonSchema("schemas/singleAuthor.json")
                .toResponse()
                .asPojo();

        softly.assertThat(created.firstName()).contains("Jean-Luc");
        softly.assertThat(created.lastName()).contains("#42");
    }

    @Test
    @Description("Verify that POST /Authors without body returns 400 Bad Request")
    @Severity(SeverityLevel.CRITICAL)
    void checkCreateAuthorWithoutBodyShouldReturnBadRequest(SoftAssertions softly) {
        Response response = authorsApi.spec()
                .when()
                .post(AUTHORS_PATH)
                .then()
                .extract()
                .response();

        softly.assertThat(response.statusCode())
                .as("Expected 400 Bad Request for empty request body")
                .isEqualTo(SC_BAD_REQUEST);

        String error = response.jsonPath().getString("errors.\"\"[0]");
        softly.assertThat(error).isEqualTo("A non-empty request body is required.");
    }
}