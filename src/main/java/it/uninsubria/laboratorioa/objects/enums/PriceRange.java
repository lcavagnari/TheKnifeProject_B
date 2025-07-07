package it.uninsubria.laboratorioa.objects.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum che rappresenta le fasce di prezzo di un ristorante.
 * Ogni fascia è associata a un simbolo e a un intervallo di prezzi.
 * <p>
 * @author Luca Cavagnari
 * @version 1.0
 */
@Getter
public enum PriceRange {

    /**
     * Fascia economica (fino a 25)
     */
    ECONOMY("$", 0, 25),
    /**
     * Fascia media (25 - 50)
     */
    MODERATE("$$", 25, 50),
    /**
     * Fascia costosa (50 - 100)
     */
    EXPENSIVE("$$$", 50, 100),
    /**
     * Fascia di lusso (oltre 100)
     */
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

    /**
     * Restituisce la fascia di prezzo in base alla lunghezza del simbolo ($, $$, etc.)
     *
     * @param amount numero di simboli dollaro
     * @return Fascia di prezzo corrispondente
     */
    public static PriceRange byDollarAmount(int amount) {
        return SYMBOL_MAP.getOrDefault(amount, MODERATE);
    }
}
