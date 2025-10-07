package com.fakeRestApi.tests.author;

import com.fakeRestApi.models.Author;
import com.fakeRestApi.tests.BaseApiTest;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Random;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;

@Epic("Fake REST API tests")
@Feature("Authors API")
@Story("Update Authors")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UpdateAuthorsTests extends BaseApiTest {

    @Test
    @Description("Verify PUT /Authors/{id} updates an existing author successfully")
    @Severity(SeverityLevel.CRITICAL)
    void shouldUpdateExistingAuthor() {
        Author author = getExistingAuthor();
        Author updated = Author.builder()
                .id(author.id())
                .idBook(author.idBook())
                .firstName("Updated_" + author.firstName())
                .lastName("Updated_" + author.lastName())
                .build();

        Author response = authorsApi.updateAuthor(author.id(), updated).asPojo(Author.class);

        assertThat(response)
                .as("Response body should not be null")
                .isNotNull();

        assertThat(response.id()).isEqualTo(author.id());
        assertThat(response.firstName()).isEqualTo(updated.firstName());
        assertThat(response.lastName()).isEqualTo(updated.lastName());
        assertThat(response.idBook()).isEqualTo(author.idBook());
    }

    @Test
    @Description("PUT /Authors/{id} should return 404 when author does not exist")
    @Severity(SeverityLevel.NORMAL)
    void shouldReturn404WhenUpdatingNonExistingAuthor() {
        int missingId = getNonExistingId();

        Author ghost = Author.builder()
                .id(missingId)
                .idBook(1)
                .firstName("Ghost")
                .lastName("Writer")
                .build();

        authorsApi
                .updateAuthor(missingId, ghost)
                .verify()
                .verifyStatusCodeNotFound()
                .verifyStringJsonPath("title", "Not Found");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t"})
    @Description("PUT /Authors/{id} should return 400 when firstName is blank")
    @Severity(SeverityLevel.MINOR)
    void checkShouldReturnBadRequestWhenFirstNameBlank(String invalid) {
        Author author = getExistingAuthor();
        Author invalidAuthor = Author.builder()
                .id(author.id())
                .idBook(author.idBook())
                .firstName(invalid)
                .lastName("Valid")
                .build();

        authorsApi
                .updateAuthor(author.id(), invalidAuthor)
                .verify()
                .verifyStatusCodeBadRequest();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\n"})
    @Description("PUT /Authors/{id} should return 400 when lastName is blank")
    @Severity(SeverityLevel.MINOR)
    void checkShouldReturnBadRequestWhenLastNameBlank(String invalid) {
        Author author = getExistingAuthor();
        Author invalidAuthor = Author.builder()
                .id(author.id())
                .idBook(author.idBook())
                .firstName("Valid")
                .lastName(invalid)
                .build();

        authorsApi
                .updateAuthor(author.id(), invalidAuthor)
                .verify()
                .verifyStatusCodeBadRequest()
                .verifyContentType("application/problem+json")
                .verifyIntegerJsonPath("status", SC_BAD_REQUEST);
    }

    @Test
    @Description("PUT /Authors/{id} should return 400 when body ID mismatches path ID")
    @Severity(SeverityLevel.NORMAL)
    void checkShouldReturnBadRequestOnIdMismatch() {
        Author author = getExistingAuthor();

        Author mismatched = Author.builder()
                .id(author.id() + 100)
                .idBook(author.idBook())
                .firstName("Mismatch")
                .lastName("Case")
                .build();

        authorsApi
                .updateAuthor(author.id(), mismatched)
                .verify()
                .verifyStatusCodeBadRequest()
                .verifyContentType("application/problem+json")
                .verifyIntegerJsonPath("status", SC_BAD_REQUEST);
    }

    @Test
    @Description("PUT /Authors/{id} should return 400 when idBook = 0 (invalid reference)")
    @Severity(SeverityLevel.NORMAL)
    void checkShouldReturnBadRequestWhenIdBookInvalid() {
        Author author = getExistingAuthor();
        Author invalidBookAuthor = Author.builder()
                .id(author.id())
                .idBook(0)
                .firstName("Bad")
                .lastName("Reference")
                .build();

        authorsApi
                .updateAuthor(author.id(), invalidBookAuthor)
                .verify()
                .verifyStatusCodeBadRequest()
                .verifyContentType("application/problem+json")
                .verifyIntegerJsonPath("status", SC_BAD_REQUEST);
    }

    @Test
    @Description("PUT /Authors/{id} with non-numeric path should return 400")
    @Severity(SeverityLevel.NORMAL)
    void checkShouldReturn400WhenPathIsNonNumeric() {
        Author author = Author.builder()
                .id(1)
                .idBook(1)
                .firstName("Bad")
                .lastName("Path")
                .build();

        authorsApi
                .updateAuthor("abc", author)
                .verify()
                .verifyStatusCodeBadRequest()
                .verifyContentType("application/problem+json")
                .verifyIntegerJsonPath("status", SC_BAD_REQUEST);
    }

    private Author getExistingAuthor() {
        List<Author> authors = authorsApi.getAuthors().asListOfPojo(Author.class);
        assertThat(authors)
                .as("At least one author should exist in system")
                .isNotEmpty();
        return authors.getFirst();
    }

    private int getNonExistingId() {
        return 10_000 + new Random().nextInt(50_000);
    }
}