package com.fakeRestApi.tests.author;

import com.fakeRestApi.models.Author;
import com.fakeRestApi.tests.BaseApiTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;

import static com.fakeRestApi.apiClient.AuthorsApi.AUTHORS_PATH;
import static org.apache.http.HttpStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

@Epic("Fake REST API tests")
@Feature("Authors API")
@Story("Get Authors")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GetAuthorsTests extends BaseApiTest {

    private static List<Author> allAuthors;

    @BeforeAll
    void initAllAuthors() {
        allAuthors = authorsApi.getAuthors();
        assertThat(allAuthors)
                .as("Authors list should be fetched before tests")
                .isNotNull()
                .isNotEmpty();
    }

    @Test
    @Description("Verify GET /Authors returns non-empty list and all fields are valid")
    @Severity(SeverityLevel.CRITICAL)
    void checkGetAllAuthorsShouldReturnValidList() {
        List<Author> authors = authorsApi.getAuthors();

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
    void checkGetAuthorByIdShouldReturnCorrectAuthor() {
        Author expectedAuthor = allAuthors.get(0);
        Author actualAuthor = authorsApi.getAuthorById(expectedAuthor.id());

        assertThat(actualAuthor)
                .as("Fetched author should not be null")
                .isNotNull();

        assertThat(actualAuthor.id())
                .as("Author ID should match")
                .isEqualTo(expectedAuthor.id());

        assertThat(actualAuthor.firstName())
                .as("Author first name should match expected value")
                .isEqualTo(expectedAuthor.firstName());

        assertThat(actualAuthor.lastName())
                .as("Author last name should match expected value")
                .isEqualTo(expectedAuthor.lastName());
    }

    @ParameterizedTest(name = "GET /Authors/{0} → should return 400 Bad Request")
    @ValueSource(strings = {"abc", "!", "$", "ю", "null"})
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify GET /Authors/{id} returns 400 Bad Request for malformed IDs")
    void checkGetAuthorWithInvalidIdShouldReturn400(String invalidId) {
        Response response = authorsApi.spec()
                .get(AUTHORS_PATH + "/" + invalidId)
                .then()
                .extract()
                .response();

        assertThat(response.statusCode())
                .as("Invalid IDs should result in 400 Bad Request")
                .isEqualTo(SC_BAD_REQUEST);

        assertThat(response.jsonPath().getString("title"))
                .as("Error title should describe validation problem")
                .containsIgnoringCase("validation");
    }

    @ParameterizedTest(name = "GET /Authors/{0} → should return 404 Not Found")
    @ValueSource(strings = {"0", "-5", "999999"})
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify GET /Authors/{id} returns 404 Not Found for nonexistent IDs")
    void checkGetAuthorWithNonexistentIdShouldReturn404(String id) {
        Response response = authorsApi.spec()
                .get(AUTHORS_PATH + "/" + id)
                .then()
                .extract()
                .response();

        assertThat(response.statusCode())
                .as("Nonexistent author should return 404")
                .isEqualTo(SC_NOT_FOUND);

        assertThat(response.jsonPath().getString("title"))
                .as("Error message should indicate Not Found")
                .isEqualTo("Not Found");
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
    void checkGetAuthorsByBookIdShouldReturnRelatedAuthors() {
        // Use a book ID from an existing author
        int bookId = allAuthors.get(0).idBook();
        List<Author> authorsByBook = authorsApi.getAuthorsByBookId(bookId);

        assertThat(authorsByBook)
                .as("Authors list for book ID %s should not be empty", bookId)
                .isNotEmpty();

        assertThat(authorsByBook)
                .as("All authors should reference the same book ID")
                .allMatch(author -> author.idBook() == bookId);
    }

    @ParameterizedTest(name = "GET /Authors/authors/books/{0} → should return 404 for nonexistent book ID")
    @ValueSource(strings = {"0", "-1", "999999"})
    @Description("Verify GET /Authors/authors/books/{idBook} returns 404 for invalid or missing books")
    @Severity(SeverityLevel.NORMAL)
    void checkGetAuthorsByNonexistentBookIdShouldReturn404(String invalidBookId) {
        Response response = authorsApi.spec()
                .get(AUTHORS_PATH + "/authors/books/" + invalidBookId)
                .then()
                .extract()
                .response();

        assertThat(response.statusCode())
                .as("Expected 404 Not Found for nonexistent book ID")
                .isEqualTo(SC_NOT_FOUND);

        assertThat(response.jsonPath().getString("title"))
                .as("Error title should describe Not Found condition")
                .isEqualTo("Not Found");
    }

    @Test
    @Description("Verify author IDs are in sequential order")
    @Severity(SeverityLevel.MINOR)
    void checkAuthorsAreSortedByIdAscending() {
        List<Integer> ids = allAuthors.stream().map(Author::id).toList();
        List<Integer> sorted = new ArrayList<>(ids);
        Collections.sort(sorted);

        assertThat(ids)
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
                            .matches("^[\\p{L}\\d\\-\\s]+$");
                    assertThat(author.lastName())
                            .as("Last name should contain only letters, digits, spaces, or dashes for Author ID %s", author.id())
                            .matches("^[\\p{L}\\d\\-\\s]+$");
                });
    }
}
