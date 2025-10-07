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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;

@Epic("Fake REST API tests")
@Feature("Books API")
@Story("Get Books")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(SoftAssertionsExtension.class)
public class GetBooksApiTests extends BaseApiTest {

    private static List<Book> allBooks;

    @BeforeAll
    void initAllBooks() {
        allBooks = booksApi.getBooks().asListOfPojo();
        assertThat(allBooks)
                .as("Book list should not be empty before tests")
                .isNotEmpty();
    }

    @Test
    @Description("Verify GET /Books returns non-empty list and valid fields")
    @Severity(SeverityLevel.CRITICAL)
    void checkGetAllBooksShouldReturnValidBooksList() {
        List<Book> books = booksApi.getBooks()
                .verify()
                .verifyStatusCodeOk()
                .verifyPojoListNotEmpty()
                .validateJsonSchema("schemas/book.json")
                .toResponse()
                .asListOfPojo();

        assertThat(books)
                .as("Book list should not be empty")
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
    void checkGetBookByRandomIdShouldReturnValidBookById(SoftAssertions softly) {
        int randomIndex = new Random().nextInt(allBooks.size());
        Book expectedBook = allBooks.get(randomIndex - 1);
        Book actualBook = booksApi.getBookById(String.valueOf(randomIndex))
                .verify()
                .validateJsonSchema("schemas/singleBook.json")
                .toResponse()
                .asPojo();

        softly.assertThat(actualBook)
                .as("Book retrieved by ID should not be null")
                .isNotNull();

        softly.assertThat(actualBook.id())
                .as("Book ID should match expected value")
                .isEqualTo(expectedBook.id());

        softly.assertThat(actualBook.title())
                .as("Book title should match expected value for ID %s", expectedBook.id())
                .isEqualTo(expectedBook.title());

        softly.assertThat(actualBook.description())
                .as("Book description should match expected value for ID %s", expectedBook.id())
                .isEqualTo(expectedBook.description());

        softly.assertThat(actualBook.pageCount())
                .as("Book page count should match expected value for ID %s", expectedBook.id())
                .isEqualTo(expectedBook.pageCount());

        softly.assertThat(actualBook.excerpt())
                .as("Book excerpt should match expected value for ID %s", expectedBook.id())
                .isEqualTo(expectedBook.excerpt());
    }

    @ParameterizedTest(name = "GET /Books/{0} should return 400 Bad Request")
    @ValueSource(strings = {"null", "abc", "ю", "!", "$"})
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify GET /Books/{id} returns 400 Bad Request when ID is invalid")
    void checkGetBookWithInvalidIdShouldReturnBadRequest(String invalidId) {
        booksApi.getBookById(invalidId)
                .verify()
                .verifyStatusCodeBadRequest()
                .verifyContentType("application/problem+json")
                .verifyStringJsonPath("title", "One or more validation errors occurred.")
                .verifyIntegerJsonPath("status", SC_BAD_REQUEST)
                .verifyStringJsonPath("errors.id[0]", "The value '" + invalidId + "' is not valid.")
                .verifyStringJsonPathIsNotBlank("traceId");
    }

    @ParameterizedTest(name = "GET /Books/{0} should return 404 Not Found")
    @ValueSource(strings = {"0", "999999999", "-5"})
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify GET /Books/{id} returns 404 Not Found for nonexistent or invalid numeric IDs")
    void checkGetBookWithNonexistentIdShouldReturnNotFound(String invalidId) {
        booksApi.getBookById(invalidId)
                .verify()
                .verifyStatusCodeNotFound()
                .verifyContentType("application/problem+json")
                .verifyStringJsonPath("title", "Not Found")
                .verifyStringJsonPath("type", "https://tools.ietf.org/html/rfc7231#section-6.5.4")
                .verifyIntegerJsonPath("status", SC_NOT_FOUND)
                .verifyStringJsonPathIsNotBlank("traceId");
    }

    @Test
    @Description("Verify that all book IDs are unique across the list")
    @Severity(SeverityLevel.NORMAL)
    void checkReturnedBookIdsAreUnique() {
        assertThat(allBooks)
                .as("Book list must be initialized from before fixture")
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
    void checkBooksReturnedInSequentialOrder(SoftAssertions softly) {
        assertThat(allBooks)
                .as("Book list must be initialized from previous test")
                .isNotNull()
                .isNotEmpty();

        boolean isSequential = Stream.iterate(1, index -> index < allBooks.size(), index -> index + 1)
                .allMatch(index -> allBooks.get(index).id() - allBooks.get(index - 1).id() == 1);

        softly.assertThat(isSequential)
                .as("Book IDs should increase sequentially (1, 2, 3, ...)")
                .isTrue();

        List<Book> sortedById = allBooks.stream()
                .sorted(Comparator.comparingInt(Book::id))
                .toList();

        softly.assertThat(allBooks)
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
    void checkGetBookByIdShouldReturnValidBook(SoftAssertions softly) {
        Book newBook = booksApi.createBook(TestDataManager.bookWithValidAllFields()).asPojo();
        Book fetched = booksApi.getBookById(newBook.id()).asPojo();

        softly.assertThat(fetched).as("Fetched book should not be null").isNotNull();
        softly.assertThat(fetched.id()).as("ID should match the requested ID").isEqualTo(newBook.id());
        softly.assertThat(fetched.title()).as("Title should match the original one").isEqualTo(newBook.title());
    }
}