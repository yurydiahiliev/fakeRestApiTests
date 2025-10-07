package com.fakeRestApi.tests.book;

import com.fakeRestApi.models.Book;
import com.fakeRestApi.tests.BaseApiTest;
import com.fakeRestApi.utils.TestDataManager;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.fakeRestApi.apiClient.BooksApi.BOOKS_PATH;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;

@Epic("Fake REST API tests")
@Feature("Books API")
@Story("Get Books")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GetBooksApiTests extends BaseApiTest {

    private static List<Book> allBooks;

    @BeforeAll
    void initAllBooks() {
        allBooks = booksApi.getBooks();
        assertThat(allBooks)
                .as("Book list should not be empty before tests")
                .isNotEmpty();
    }

    @Test
    @Description("Verify GET /Books returns non-empty list and valid fields")
    @Severity(SeverityLevel.CRITICAL)
    void checkGetAllBooksShouldReturnValidBooksList() {
        List<Book> books = booksApi.getBooks();

        assertThat(books)
                .as("Book list should not be empty")
                .isNotEmpty()
                .allSatisfy(book -> {
                    assertThat(book.id())
                            .as("Book ID should be a positive integer")
                            .isPositive();

                    assertThat(book.title())
                            .as("Book title should not be blank (Book ID: %s)", book.id())
                            .isNotBlank();

                    assertThat(book.description())
                            .as("Book description should not be blank (Book ID: %s)", book.id())
                            .isNotBlank();

                    assertThat(book.pageCount())
                            .as("Page count should be greater than zero (Book ID: %s)", book.id())
                            .isGreaterThan(0);

                    assertThat(book.excerpt())
                            .as("Excerpt should not be blank for Book ID: %s (per Fake API spec)", book.id())
                            .isNotBlank();

                    assertThat(book.publishDate())
                            .as("Publish date must not be blank and must be in ISO format (Book ID: %s)", book.id())
                            .isNotBlank()
                            .matches("^\\d{4}-\\d{2}-\\d{2}T.*$");
                });
    }

    @Test
    @Description("Verify GET /Books/{id} returns the correct book when using a valid random ID")
    @Severity(SeverityLevel.CRITICAL)
    void checkGetBookByRandomIdShouldReturnValidBookById() {
        int randomIndex = new Random().nextInt(allBooks.size());
        Book expectedBook = allBooks.get(randomIndex);
        Book actualBook = booksApi.getBookById(expectedBook.id());

        assertThat(actualBook)
                .as("Book retrieved by ID should not be null")
                .isNotNull();

        assertThat(actualBook.id())
                .as("Book ID should match expected value")
                .isEqualTo(expectedBook.id());

        assertThat(actualBook.title())
                .as("Book title should match expected value for ID %s", expectedBook.id())
                .isEqualTo(expectedBook.title());

        assertThat(actualBook.description())
                .as("Book description should match expected value for ID %s", expectedBook.id())
                .isEqualTo(expectedBook.description());

        assertThat(actualBook.pageCount())
                .as("Book page count should match expected value for ID %s", expectedBook.id())
                .isEqualTo(expectedBook.pageCount());

        assertThat(actualBook.excerpt())
                .as("Book excerpt should match expected value for ID %s", expectedBook.id())
                .isEqualTo(expectedBook.excerpt());
    }

    @ParameterizedTest(name = "GET /Books/{0} should return 400 Bad Request")
    @ValueSource(strings = {"null", "abc", "ю", "!", "$"})
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify GET /Books/{id} returns 400 Bad Request when ID is invalid")
    void checkGetBookWithInvalidIdShouldReturnBadRequest(String invalidId) {
        Response response = booksApi.spec()
                .get(BOOKS_PATH + "/" + invalidId) // simulate invalid IDs directly in path
                .then()
                .extract()
                .response();

        assertThat(response.statusCode())
                .as("Expected 400 Bad Request for invalid ID '%s'", invalidId)
                .isEqualTo(SC_BAD_REQUEST);

        assertThat(response.getContentType())
                .as("Response for invalid ID '%s' should be problem+json", invalidId)
                .contains("application/problem+json");

        String title = response.jsonPath().getString("title");
        Integer status = response.jsonPath().getInt("status");
        String errorMessage = response.jsonPath().getString("errors.id[0]");
        String traceId = response.jsonPath().getString("traceId");

        assertThat(title)
                .as("Title for invalid ID '%s' should describe validation failure", invalidId)
                .isEqualTo("One or more validation errors occurred.");

        assertThat(status)
                .as("Status code field should equal 400 for invalid ID '%s'", invalidId)
                .isEqualTo(SC_BAD_REQUEST);

        assertThat(errorMessage)
                .as("Error message for invalid ID '%s' should mention invalid value", invalidId)
                .contains("not valid");

        assertThat(traceId)
                .as("Trace ID should exist for error response (ID '%s')", invalidId)
                .isNotBlank();
    }

    @ParameterizedTest(name = "GET /Books/{0} should return 404 Not Found")
    @ValueSource(strings = {"0", "999999999", "-5"})
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify GET /Books/{id} returns 404 Not Found for nonexistent or invalid numeric IDs")
    void checkGetBookWithNonexistentIdShouldReturnNotFound(String invalidId) {
        Response response = booksApi.spec()
                .get(BOOKS_PATH + "/" + invalidId)
                .then()
                .log().ifValidationFails()
                .extract()
                .response();

        assertThat(response.statusCode())
                .as("Expected 404 Not Found for ID '%s'", invalidId)
                .isEqualTo(SC_NOT_FOUND);

        assertThat(response.getContentType())
                .as("Response content type should be application/problem+json")
                .contains("application/problem+json; charset=utf-8; v=1.0");

        String type = response.jsonPath().getString("type");
        String title = response.jsonPath().getString("title");
        int status = response.jsonPath().getInt("status");
        String traceId = response.jsonPath().getString("traceId");

        assertThat(type)
                .as("Error type should reference RFC 7231 for ID '%s'", invalidId)
                .isEqualTo("https://tools.ietf.org/html/rfc7231#section-6.5.4");

        assertThat(title)
                .as("Title should describe 404 Not Found for ID '%s'", invalidId)
                .isEqualTo("Not Found");

        assertThat(status)
                .as("Status field should equal 404 for ID '%s'", invalidId)
                .isEqualTo(SC_NOT_FOUND);

        assertThat(traceId)
                .as("Trace ID should be present for 404 response (ID '%s')", invalidId)
                .isNotBlank();
    }

    @Test
    @Description("Verify that all book IDs are unique across the list")
    @Severity(SeverityLevel.NORMAL)
    void checkReturnedBookIdsAreUnique() {
        assertThat(allBooks)
                .as("Book list must be initialized from previous test")
                .isNotNull();

        Set<Integer> uniqueIds = new HashSet<>();
        allBooks.forEach(book -> {
            boolean added = uniqueIds.add(book.id());
            assertThat(added)
                    .as("Book ID %s should be unique", book.id())
                    .isTrue();
        });

        assertThat(uniqueIds.size())
                .as("Number of unique IDs should match total number of books")
                .isEqualTo(allBooks.size());
    }

    @Test
    @Description("Verify that books are returned in sequential order by ID")
    @Severity(SeverityLevel.NORMAL)
    void checkBooksReturnedInSequentialOrder() {
        assertThat(allBooks)
                .as("Book list must be initialized from previous test")
                .isNotNull()
                .isNotEmpty();

        boolean isSequential = Stream.iterate(1, index -> index < allBooks.size(), index -> index + 1)
                .allMatch(index -> allBooks.get(index).id() - allBooks.get(index - 1).id() == 1);

        assertThat(isSequential)
                .as("Book IDs should increase sequentially (1, 2, 3, ...)")
                .isTrue();

        List<Book> sortedById = allBooks.stream()
                .sorted(Comparator.comparingInt(Book::id))
                .toList();

        assertThat(allBooks)
                .as("Books should be sorted by ID ascending")
                .containsExactlyElementsOf(sortedById);
    }

    @Test
    @Description("Verify that each next book has an earlier publish date than the previous one (newest to oldest order)")
    @Severity(SeverityLevel.NORMAL)
    void checkBooksPublishDateIsChronologicallyDecreasing() {
        assertThat(allBooks)
                .as("Book list must be initialized from previous test")
                .isNotNull()
                .isNotEmpty();

        boolean isChronologicallyDecreasing = IntStream.range(1, allBooks.size())
                .allMatch(index -> {
                    var prevDate = Instant.parse(allBooks.get(index - 1).publishDate());
                    var nextDate = Instant.parse(allBooks.get(index).publishDate());
                    return prevDate.isAfter(nextDate);
                });

        assertThat(isChronologicallyDecreasing)
                .as("Each next book's publishDate should be earlier than the previous one (descending order)")
                .isTrue();
    }

    @Test
    @Description("GET /Books/{id} should return the correct book for an existing ID")
    @Severity(SeverityLevel.CRITICAL)
    void checkGetBookByIdShouldReturnValidBook() {
        Book newBook = booksApi.createBook(TestDataManager.bookWithValidAllFields());
        Book fetched = booksApi.getBookById(newBook.id());

        assertThat(fetched).as("Fetched book should not be null").isNotNull();
        assertThat(fetched.id()).as("ID should match the requested ID").isEqualTo(newBook.id());
        assertThat(fetched.title()).as("Title should match the original one").isEqualTo(newBook.title());
    }
}