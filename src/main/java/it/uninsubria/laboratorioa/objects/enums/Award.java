package it.uninsubria.laboratorioa.objects.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum che rappresenta le categorie di riconoscimenti Michelin ottenibili da un ristorante.
 * <p>
 * Autore: Luca Cavagnari
 *
 * @version 1.0
 */
public enum Award {
    /**
     * Nessun premio
     */
    NONE(0),
    /**
     * 1 stella Michelin
     */
    ONE_STAR(1),
    /**
     * 2 stelle Michelin
     */
    TWO_STARS(2),

    THREE_STARS(3),
    /**
     * 4 stelle Michelin
     */
    BIB_GOURMAND(4),
    /**
     * 5 stelle Michelin
     */
    SELECTED_RESTAURANTS(5);

    /**
     * Mappa ausiliaria per ottenere un valore `Award` a partire da un intero.
     */
    private static final Map<Integer, Award> BY_VALUE = new HashMap<>();

    static {
        for (Award a : values()) {
            BY_VALUE.put(a.value, a);
        }
    }

    /**
     * Valore intero associato all'enum.
     */
    @Getter
    private final int value;

    /**
     * Costruttore che associa il valore intero al tipo di premio.
     *
     * @param value valore intero rappresentativo del premio
     */
    Award(int value) {
        this.value = value;
    }

    /**
     * Restituisce l'istanza `Award` corrispondente al valore intero specificato.
     * Se il valore non è valido, restituisce `Award.NONE`.
     *
     * @param val valore intero
     * @return valore dell'enum corrispondente oppure `NONE`
     */
    public static Award fromInt(int val) {
        return BY_VALUE.getOrDefault(val, Award.NONE);
    }

    /**
     * Restituisce una rappresentazione testuale leggibile del premio.
     *
     * @return nome leggibile del premio
     */
    @Override
    public String toString() {
        return switch (value) {
            case 4 -> "Bib Gourmand";
            case 5 -> "Selected Restaurants";
            case 1 -> "1 star";
            default -> value + " stars";
        };
    }
}
