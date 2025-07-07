package it.uninsubria.laboratorioa.objects.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum che rappresenta le fasce di prezzo di un ristorante,
 * espresse tramite simboli a forma di '$' e valori minimi e massimi.<p>
 * Ogni fascia ha un intervallo di prezzo e un simbolo associato per rappresentazione testuale.<p>
 * Metodo statico permette di ottenere la fascia in base al numero di simboli '$'.<p>
 *
 * Autore: Luke
 * @version 1.0
 */
@Getter
public enum PriceRange {
    ECONOMY("$", 0, 25),
    MODERATE("$$", 25, 50),
    EXPENSIVE("$$$", 50, 100),
    LUXURY("$$$$", 100, Integer.MAX_VALUE);

    /**
     * Mappa ausiliaria per ottenere la fascia di prezzo dal numero di simboli '$'.
     */
    private static final Map<Integer, PriceRange> SYMBOL_MAP = new HashMap<>();

    static {
        for (PriceRange pr : values())
            SYMBOL_MAP.put(pr.symbol.length(), pr);
    }

    /**
     * Simbolo che rappresenta la fascia di prezzo (esempio: '$$$').
     */
    private final String symbol;

    /**
     * Prezzo minimo (in unità monetarie) della fascia.
     */
    private final int minPrice;

    /**
     * Prezzo massimo (in unità monetarie) della fascia.
     */
    private final int maxPrice;

    /**
     * Costruttore per definire la fascia di prezzo con simbolo e range.
     *
     * @param symbol   simbolo di rappresentazione della fascia
     * @param minPrice prezzo minimo incluso
     * @param maxPrice prezzo massimo incluso
     */
    PriceRange(String symbol, int minPrice, int maxPrice) {
        this.symbol = symbol;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

    /**
     * Restituisce la fascia di prezzo in base al numero di simboli '$' forniti.<p>
     * Se non esiste una corrispondenza, restituisce la fascia `MODERATE` come default.
     *
     * @param amount numero di simboli '$'
     * @return fascia di prezzo corrispondente
     */
    public static PriceRange byDollarAmount(int amount) {
        return SYMBOL_MAP.getOrDefault(amount, MODERATE);
    }
}
