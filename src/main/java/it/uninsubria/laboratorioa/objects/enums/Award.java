package it.uninsubria.laboratorioa.objects.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public enum Award {
    NONE(0),
    ONE_STAR(1),
    TWO_STARS(2),
    THREE_STARS(3),
    BIB_GOURMAND(4),
    SELECTED_RESTAURANTS(5);

    private static final Map<Integer, Award> BY_VALUE = new HashMap<>();

    static {
        for (Award a : values()) {
            BY_VALUE.put(a.value, a);
        }
    }

    @Getter
    private final int value;

    Award(int value) {
        this.value = value;
    }

    public static Award fromInt(int val) {
        return BY_VALUE.getOrDefault(val, Award.NONE);
    }

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
