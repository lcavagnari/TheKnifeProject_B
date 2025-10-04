package it.uninsubria.laboratorioa.objects.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CuisineType Enum Tests")
class CuisineTypeTest {

    @Test
    @DisplayName("Should convert to lowercase string")
    void testToString() {
        assertEquals("italian", CuisineType.ITALIAN.toString());
        assertEquals("chinese", CuisineType.CHINESE.toString());
        assertEquals("japanese", CuisineType.JAPANESE.toString());
    }

    @Test
    @DisplayName("Should have all major cuisine types")
    void testMajorCuisineTypes() {
        assertNotNull(CuisineType.valueOf("ITALIAN"));
        assertNotNull(CuisineType.valueOf("FRENCH"));
        assertNotNull(CuisineType.valueOf("CHINESE"));
        assertNotNull(CuisineType.valueOf("JAPANESE"));
        assertNotNull(CuisineType.valueOf("MEXICAN"));
        assertNotNull(CuisineType.valueOf("INDIAN"));
    }
}
