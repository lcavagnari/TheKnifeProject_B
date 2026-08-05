package it.uninsubria.laboratoriob.enums;

import it.uninsubria.laboratoriob.enums.Nation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Nation Enum Tests")
class NationTest {

    @Test
    @DisplayName("Should find nation by full name")
    void testFromStringFullName() {
        assertEquals(Nation.ITALY, Nation.fromString("italia"));
        assertEquals(Nation.FRANCE, Nation.fromString("fr"));
        assertEquals(Nation.GERMANY, Nation.valueOf("GERMANY"));
    }

    @Test
    @DisplayName("Should find nation by ISO code")
    void testFromStringIsoCode() {
        assertEquals(Nation.ITALY, Nation.fromString("it"));
        assertEquals(Nation.FRANCE, Nation.fromString("fr"));
        assertEquals(Nation.UNITED_STATES, Nation.fromString("us"));
    }

    @Test
    @DisplayName("Should be case insensitive")
    void testFromStringCaseInsensitive() {
        assertEquals(Nation.ITALY, Nation.fromString("italia"));
        assertEquals(Nation.ITALY, Nation.valueOf("ITALY"));
    }

    @Test
    @DisplayName("Should return null for unknown identifier")
    void testFromStringUnknown() {
        assertNull(Nation.fromString("Unknown Country"));
        assertNull(Nation.fromString("XYZ"));
    }

    @Test
    @DisplayName("Should return null for null input")
    void testFromStringNull() {
        assertNull(Nation.fromString(null));
    }
}
