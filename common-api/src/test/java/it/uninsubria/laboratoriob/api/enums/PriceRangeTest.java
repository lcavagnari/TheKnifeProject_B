package it.uninsubria.laboratoriob.api.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("PriceRange Enum Tests")
class PriceRangeTest {

    @Test
    @DisplayName("Should get price range by dollar amount")
    void testByDollarAmount() {
        assertEquals(PriceRange.ECONOMY, PriceRange.byDollarAmount(1));
        assertEquals(PriceRange.MODERATE, PriceRange.byDollarAmount(2));
        assertEquals(PriceRange.EXPENSIVE, PriceRange.byDollarAmount(3));
        assertEquals(PriceRange.LUXURY, PriceRange.byDollarAmount(4));
    }

    @Test
    @DisplayName("Should return MODERATE for invalid amount")
    void testByDollarAmountInvalid() {
        assertEquals(PriceRange.MODERATE, PriceRange.byDollarAmount(0));
        assertEquals(PriceRange.MODERATE, PriceRange.byDollarAmount(99));
    }

    @Test
    @DisplayName("Should have correct symbols")
    void testSymbols() {
        assertEquals("$", PriceRange.ECONOMY.getSymbol());
        assertEquals("$$", PriceRange.MODERATE.getSymbol());
        assertEquals("$$$", PriceRange.EXPENSIVE.getSymbol());
        assertEquals("$$$$", PriceRange.LUXURY.getSymbol());
    }

    @Test
    @DisplayName("Should have correct price ranges")
    void testPriceRanges() {
        assertEquals(0, PriceRange.ECONOMY.getMinPrice());
        assertEquals(25, PriceRange.ECONOMY.getMaxPrice());

        assertEquals(25, PriceRange.MODERATE.getMinPrice());
        assertEquals(50, PriceRange.MODERATE.getMaxPrice());

        assertEquals(100, PriceRange.LUXURY.getMinPrice());
    }
}

