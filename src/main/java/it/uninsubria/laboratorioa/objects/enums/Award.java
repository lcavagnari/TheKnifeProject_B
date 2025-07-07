package it.uninsubria.laboratorioa.objects.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum che rappresenta le categorie di riconoscimenti Michelin ottenibili da un ristorante.
 * I valori includono le classiche stelle, il Bib Gourmand e i ristoranti selezionati.
 * <p>
 * Ogni valore è associato a un intero che rappresenta il codice persistente.
 * <p>
 * @author Luca Cavagnari
 * @version 1.0
 */
public enum Award {

    NONE(0),
    ONE_STAR(1),
    TWO_STARS(2),
    THREE_STARS(3),
    BIB_GOURMAND(4),
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
     * Costruttore privato che associa il valore intero al tipo di premio.
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
        switch (value) {
            case 4 -> {
                return "Bib Gourmand";
            }
            case 5 -> {
                return "Selected Restaurants";
            }
            case 1 -> {
                return "1 star";
            }
            default -> {
                return value + " stars";
            }
        }
    }
}
