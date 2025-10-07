package com.fakeRestApi.utils;

import com.fakeRestApi.models.Author;
import com.fakeRestApi.models.Book;
import com.github.javafaker.Faker;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class TestDataManager {

    private static final Faker faker = new Faker();

    public static Book.BookBuilder generateValidBookBuilder() {
        int randomId = ThreadLocalRandom.current().nextInt(1, 9999);
        return Book.builder()
                .id(randomId)
                .title(faker.book().title())
                .description(faker.lorem().sentence(10))
                .pageCount(faker.number().numberBetween(1, 500))
                .excerpt(faker.lorem().paragraph())
                .publishDate(Instant.now().toString());
    }

    public static Book bookWithValidAllFields() {
        return generateValidBookBuilder().build();
    }

    public static Book bookWithEmptyFields() {
        return Book.builder()
                .id(0)
                .title("")
                .description("")
                .pageCount(0)
                .excerpt("")
                .publishDate("")
                .build();
    }

    public static Book bookWithNullFields() {
        return Book.builder()
                .id(null)
                .title(null)
                .description(null)
                .pageCount(null)
                .excerpt(null)
                .publishDate(null)
                .build();
    }

    public static Book bookWithSingleField(String fieldName) {
        int randomId = ThreadLocalRandom.current().nextInt(1, 9999);
        String randomDate = Instant.now().toString();

        return switch (fieldName.toLowerCase()) {
            case "id" -> new Book(randomId, null, null, null, null, null);
            case "title" -> new Book(null, faker.book().title(), null, null, null, null);
            case "description" -> new Book(null, null, faker.lorem().sentence(8), null, null, null);
            case "pagecount" -> new Book(null, null, null, faker.number().numberBetween(1, 999), null, null);
            case "excerpt" -> new Book(null, null, null, null, faker.lorem().paragraph(), null);
            case "publishdate" -> new Book(null, null, null, null, null, randomDate);
            default -> throw new IllegalArgumentException("Unknown field name: " + fieldName);
        };
    }

    /* ----------------------------- AUTHORS ----------------------------- */

    public static Author.AuthorBuilder generateValidAuthorBuilder() {
        int randomId = ThreadLocalRandom.current().nextInt(1, 9999);
        int randomBookId = ThreadLocalRandom.current().nextInt(1, 100);
        return Author.builder()
                .id(randomId)
                .idBook(randomBookId)
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName());
    }

    public static Author authorWithValidAllFields() {
        return generateValidAuthorBuilder().build();
    }

    public static Author authorWithEmptyFields() {
        return Author.builder()
                .id(0)
                .idBook(0)
                .firstName("")
                .lastName("")
                .build();
    }

    public static Author authorWithNullFields() {
        return Author.builder()
                .id(null)
                .idBook(null)
                .firstName(null)
                .lastName(null)
                .build();
    }

    public static Author authorWithSingleField(String fieldName) {
        int randomId = ThreadLocalRandom.current().nextInt(1, 9999);
        int randomBookId = ThreadLocalRandom.current().nextInt(1, 100);

        return switch (fieldName.toLowerCase()) {
            case "id" -> Author.builder().id(randomId).build();
            case "idbook" -> Author.builder().idBook(randomBookId).build();
            case "firstname" -> Author.builder().firstName(faker.name().firstName()).build();
            case "lastname" -> Author.builder().lastName(faker.name().lastName()).build();
            default -> throw new IllegalArgumentException("Unknown field name: " + fieldName);
        };
    }

    public static Author authorWithValidFields() {
        return Author.builder()
                .id(89)
                .idBook(10)
                .firstName("John")
                .lastName("Doe")
                .build();
    }
}