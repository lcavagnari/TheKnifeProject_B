package it.uninsubria.laboratorioa.objects.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum PriceRange {
    ECONOMY("$", 0, 25),
    MODERATE("$$", 25, 50),
    EXPENSIVE("$$$", 50, 100),
    LUXURY("$$$$", 100, Integer.MAX_VALUE);

    private static final Map<Integer, PriceRange> SYMBOL_MAP = new HashMap<>();

    static {
        for (PriceRange pr : values())
            SYMBOL_MAP.put(pr.symbol.length(), pr);
    }

    private final String symbol;
    private final int minPrice;
    private final int maxPrice;

    PriceRange(String symbol, int minPrice, int maxPrice) {
        this.symbol = symbol;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }


    public static PriceRange byDollarAmount(int amount) {
        return SYMBOL_MAP.getOrDefault(amount, MODERATE);
    }
}
