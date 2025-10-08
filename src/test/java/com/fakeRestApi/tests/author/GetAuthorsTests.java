package com.fakeRestApi.tests.author;

import com.fakeRestApi.models.Author;
import com.fakeRestApi.tests.BaseApiTest;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.http.HttpStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

@Epic("Fake REST API tests")
@Feature("Authors API")
@Story("Get Authors")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(SoftAssertionsExtension.class)
public class GetAuthorsTests extends BaseApiTest {

    private static List<Author> allAuthors;
    private final String regexpForMatchingNames = "^[\\p{L}\\d\\-\\s]+$";

    @BeforeAll
    void initAllAuthors() {
        allAuthors = authorsApi.getAuthors().asListOfPojo();
        assertThat(allAuthors)
                .as("Authors list should be fetched before tests")
                .isNotNull()
                .isNotEmpty();
    }

    @Test
    @Description("Verify GET /Authors returns non-empty list and all fields are valid")
    @Severity(SeverityLevel.CRITICAL)
    void checkGetAllAuthorsShouldReturnValidList() {
        List<Author> authors = authorsApi.getAuthors()
                .verify()
                .verifyStatusCodeOk()
                .verifyPojoListNotEmpty()
                .validateJsonSchema("schemas/author.json")
                .toResponse()
                .asListOfPojo();

        assertThat(authors)
                .as("Authors list should not be empty")
                .isNotEmpty()
                .allSatisfy(author -> {
                    assertThat(author.id()).as("Author ID should be positive").isPositive();
                    assertThat(author.firstName()).as("First name should not be blank").isNotBlank();
                    assertThat(author.lastName()).as("Last name should not be blank").isNotBlank();
                    assertThat(author.idBook()).as("Book ID should be positive").isPositive();
                });
    }

    @Test
    @Description("Verify GET /Authors/{id} returns correct author by valid ID")
    @Severity(SeverityLevel.CRITICAL)
    void checkGetAuthorByIdShouldReturnCorrectAuthor(SoftAssertions softly) {
        Author expectedAuthor = allAuthors.getFirst();
        Author actualAuthor = authorsApi.getAuthorById(expectedAuthor.id()).asPojo();

        softly.assertThat(actualAuthor)
                .as("Fetched author should not be null")
                .isNotNull();

        softly.assertThat(actualAuthor.id())
                .as("Author ID should match")
                .isEqualTo(expectedAuthor.id());

        softly.assertThat(actualAuthor.firstName())
                .as("Author first name should match expected value")
                .isEqualTo(expectedAuthor.firstName());

        softly.assertThat(actualAuthor.lastName())
                .as("Author last name should match expected value")
                .isEqualTo(expectedAuthor.lastName());
    }

    @ParameterizedTest(name = "GET /Authors/{0} → should return 400 Bad Request")
    @ValueSource(strings = {"abc", "!", "$", "ю", "null"})
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify GET /Authors/{id} returns 400 Bad Request for malformed IDs")
    void checkGetAuthorWithInvalidIdShouldReturnBadRequest(String invalidId) {
        authorsApi.getAuthorById(invalidId)
                .verify()
                .verifyStatusCodeBadRequest()
                .verifyContentType("application/problem+json")
                .verifyStringJsonPath("title", "One or more validation errors occurred.")
                .verifyIntegerJsonPath("status", SC_BAD_REQUEST)
                .verifyStringJsonPath("errors.id[0]", "The value '" + invalidId + "' is not valid.")
                .verifyStringJsonPathIsNotBlank("traceId");
    }

    @ParameterizedTest(name = "GET /Authors/{0} → should return 404 Not Found")
    @ValueSource(strings = {"0", "-5", "999999"})
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify GET /Authors/{id} returns 404 Not Found for nonexistent IDs")
    void checkGetAuthorWithNonexistentIdShouldReturnBadRequest(String invalidId) {
        authorsApi.getAuthorById(invalidId)
                .verify()
                .verifyStatusCodeNotFound()
                .verifyContentType("application/problem+json")
                .verifyStringJsonPath("title", "Not Found")
                .verifyStringJsonPath("type", "https://tools.ietf.org/html/rfc7231#section-6.5.4")
                .verifyIntegerJsonPath("status", SC_NOT_FOUND)
                .verifyStringJsonPathIsNotBlank("traceId");
    }

    @Test
    @Description("Verify all Author IDs are unique")
    @Severity(SeverityLevel.NORMAL)
    void checkAuthorIdsAreUnique() {
        Set<Integer> ids = new HashSet<>();
        allAuthors.forEach(author -> assertThat(ids.add(author.id()))
                .as("Author ID %s should be unique", author.id())
                .isTrue());

        assertThat(ids.size())
                .as("Unique IDs should match total author count")
                .isEqualTo(allAuthors.size());
    }

    @Test
    @Description("Verify GET /Authors/authors/books/{idBook} returns authors related to that book")
    @Severity(SeverityLevel.NORMAL)
    void checkGetAuthorsByBookIdShouldReturnRelatedAuthors(SoftAssertions softly) {
        int bookId = allAuthors.getFirst().idBook();
        List<Author> authorsByBook = authorsApi.getAuthorsByBookId(bookId).asListOfPojo();

        softly.assertThat(authorsByBook)
                .as("Authors list for book ID %s should not be empty", bookId)
                .isNotEmpty();

        softly.assertThat(authorsByBook)
                .as("All authors should reference the same book ID")
                .allMatch(author -> author.idBook() == bookId);
    }

    @ParameterizedTest(name = "GET /Authors/authors/books/{0} → should return 404 for nonexistent book ID")
    @ValueSource(strings = {"0", "-1", "9999999"})
    @Description("Verify GET /Authors/authors/books/{idBook} returns 404 for invalid or missing books")
    @Severity(SeverityLevel.NORMAL)
    void checkGetAuthorsByNonexistentBookIdShouldReturnBadRequest(String invalidBookId) {
        authorsApi
                .getAuthorsByBookId(invalidBookId)
                .verify()
                .verifyStatusCodeNotFound()
                .verifyContentType("application/problem+json")
                .verifyStringJsonPath("title", "Not Found");
    }

    @Test
    @Description("Verify author IDs are in sequential order")
    @Severity(SeverityLevel.MINOR)
    void checkAuthorsAreSortedByIdAscending() {
        List<Integer> authorIds = allAuthors.stream().map(Author::id).toList();
        List<Integer> sorted = new ArrayList<>(authorIds);
        Collections.sort(sorted);

        assertThat(authorIds)
                .as("Authors should be sorted by ID ascending")
                .containsExactlyElementsOf(sorted);
    }

    @Test
    @Description("Verify each author has a valid first and last name pattern")
    @Severity(SeverityLevel.MINOR)
    void checkAuthorNamesHaveValidPattern() {
        assertThat(allAuthors)
                .allSatisfy(author -> {
                    assertThat(author.firstName())
                            .as("First name should contain only letters, digits, spaces, or dashes for Author ID %s", author.id())
                            .matches(regexpForMatchingNames);
                    assertThat(author.lastName())
                            .as("Last name should contain only letters, digits, spaces, or dashes for Author ID %s", author.id())
                            .matches(regexpForMatchingNames);
                });
    }
}