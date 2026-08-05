package it.uninsubria.laboratoriob.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Award Enum Tests")
class AwardTest {

    @Test
    @DisplayName("Should get award from integer value")
    void testFromInt() {
        assertEquals(Award.NONE, Award.fromInt(0));
        assertEquals(Award.ONE_STAR, Award.fromInt(1));
        assertEquals(Award.TWO_STARS, Award.fromInt(2));
        assertEquals(Award.THREE_STARS, Award.fromInt(3));
        assertEquals(Award.BIB_GOURMAND, Award.fromInt(4));
        assertEquals(Award.SELECTED_RESTAURANTS, Award.fromInt(5));
    }

    @Test
    @DisplayName("Should return NONE for invalid value")
    void testFromIntInvalid() {
        assertEquals(Award.NONE, Award.fromInt(99));
        assertEquals(Award.NONE, Award.fromInt(-1));
    }

    @Test
    @DisplayName("Should format toString correctly")
    void testToString() {
        assertEquals("Bib Gourmand", Award.BIB_GOURMAND.toString());
        assertEquals("Selected Restaurants", Award.SELECTED_RESTAURANTS.toString());
        assertEquals("1 star", Award.ONE_STAR.toString());
        assertEquals("2 stars", Award.TWO_STARS.toString());
    }
}

