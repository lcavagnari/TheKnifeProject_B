package it.uninsubria.laboratorioa.objects.enums;

import lombok.Getter;

/**
 * Enum che rappresenta i premi assegnati ad un ristorante dalla guida Michelin.
 */
public enum Award {

    /**
     * Nessun premio
     */
    NONE(""),
    /**
     * 1 stella Michelin
     */
    ONE_STAR("1"),
    /**
     * 2 stelle Michelin
     */
    TWO_STARS("2"),
    /**
     * 3 stelle Michelin
     */
    THREE_STARS("3"),
    /**
     * Bib Gourmand
     */
    BIB_GOURMAND("bib");

    @Getter
    private final String value;

    Award(String value) {
        this.value = value;
    }
}
