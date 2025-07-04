package it.uninsubria.laboratorioa.objects.enums;

import lombok.Getter;

@Getter
public enum PriceRange {
    ECONOMY("$", 0, 25),
    MODERATE("$$", 25, 50),
    EXPENSIVE("$$$", 50, 100),
    LUXURY("$$$$", 100, Integer.MAX_VALUE);

    private final String symbol;
    private final int minPrice; 
    private final int maxPrice;

    PriceRange(String symbol, int minPrice, int maxPrice) {
        this.symbol = symbol;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }
}