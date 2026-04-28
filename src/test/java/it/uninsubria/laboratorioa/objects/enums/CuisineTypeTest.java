package it.uninsubria.laboratorioa.objects.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CuisineType Enum Tests")
class CuisineTypeTest {

    @Test
    @DisplayName("Should convert to lowercase string")
    void testToString() {
        assertAll(
                () -> assertEquals("italian", CuisineType.ITALIAN.toString()),
                () -> assertEquals("chinese", CuisineType.CHINESE.toString()),
                () -> assertEquals("japanese", CuisineType.JAPANESE.toString())
        );
    }


    @ParameterizedTest
    @MethodSource("readCuisinesFromFile")
    @DisplayName("Should validate cuisine type from file")
    void testCuisineFromFile(String cuisine) {
        assertNotNull(CuisineType.valueOf(cuisine.toUpperCase()));
    }

    static Stream<String> readCuisinesFromFile() throws IOException {
        return new Scanner(new File("src/test", "cuisines.txt"))
                .useDelimiter("\n")
                .tokens()
                .map(String::trim);
    }


    @Test
    @DisplayName("Should have all major cuisine types")
    void testMajorCuisineTypes() {
        assertAll(
                () -> assertNotNull(CuisineType.valueOf("ITALIAN")),
                () -> assertNotNull(CuisineType.valueOf("FRENCH")),
                () -> assertNotNull(CuisineType.valueOf("CHINESE")),
                () -> assertNotNull(CuisineType.valueOf("JAPANESE")),
                () -> assertNotNull(CuisineType.valueOf("MEXICAN")),
                () -> assertNotNull(CuisineType.valueOf("INDIAN")),
                () -> assertNotNull(CuisineType.valueOf("CONTEMPORARY"))
        );
    }
}
