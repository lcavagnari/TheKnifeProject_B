package it.uninsubria.laboratorioa.objects.enums;

public enum Nation {
    AFGHANISTAN,
    AFRICA,
    AUSTRIA,
    SWITZERLAND,
    GERMANY,
    FRANCE,
    SPAIN,
    PORTUGAL,
    ITALY,
    UNITED_KINGDOM,
    UNITED_STATES,
    BRAZIL,
    ARGENTINA,
    MEXICO,
    PERU,
    COLOMBIA,
    PUERTO_RICO,
    VENEZUELA,
    CUBA,
    JAPAN,
    KOREA,
    CHINA,
    THAILAND,
    VIETNAM,
    MALAYSIA,
    INDONESIA,
    PHILIPPINES,
    LAOS,
    MYANMAR,
    CAMBODIA,
    INDIA,
    PAKISTAN,
    SRI_LANKA,
    NEPAL,
    IRAN,
    ISRAEL,
    LEBANON,
    RUSSIA,
    POLAND,
    NORWAY,
    SCANDINAVIA,
    TURKEY;

    @Override
    public String toString() {
        return name().charAt(0)+name().substring(1).toLowerCase();
    }
}


