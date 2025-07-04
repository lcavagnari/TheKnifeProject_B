package it.uninsubria.laboratorioa.objects.enums;


import lombok.Getter;

public enum Currency {
    DOLLAR('$'),
    POUND('£'),
    YEN('¥'),
    BAHT('฿'),
    WON('₩'),
    DONG('₫'),
    EURO('€'),
    TURKISH_LIRA('₺'),
    RIAL('﷼');

    @Getter
    private final char symbol;

    Currency(char symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return "" + symbol;
    }
}