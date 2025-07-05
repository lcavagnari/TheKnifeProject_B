package it.uninsubria.laboratorioa.objects.enums;

public enum Award {
    NONE(0),
    ONE_STAR(1),
    TWO_STAR(2),
    THREE_STAR(3);

    private final int value;

    Award(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static Award fromInt(int val) {
        for (Award a : Award.values()) {
            if (a.value == val) return a;
        }
        return NONE;
    }
}
