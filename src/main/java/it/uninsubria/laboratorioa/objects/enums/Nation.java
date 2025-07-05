package it.uninsubria.laboratorioa.objects.enums;

import lombok.Getter;

@Getter
public enum
Nation {
    AFGHANISTAN("AF"),
    AFRICA("AF"), // Approximate, adjust if needed
    AUSTRIA("AT"),
    SWITZERLAND("CH"),
    GERMANY("DE"),
    FRANCE("FR"),
    SPAIN("ES"),
    PORTUGAL("PT"),
    ITALY("IT"),
    UNITED_KINGDOM("GB"),
    UNITED_STATES("US"),
    BRAZIL("BR"),
    ARGENTINA("AR"),
    MEXICO("MX"),
    PERU("PE"),
    COLOMBIA("CO"),
    PUERTO_RICO("PR"),
    VENEZUELA("VE"),
    CUBA("CU"),
    JAPAN("JP"),
    KOREA("KR"),
    CHINA("CN"),
    THAILAND("TH"),
    VIETNAM("VN"),
    MALAYSIA("MY"),
    INDONESIA("ID"),
    PHILIPPINES("PH"),
    LAOS("LA"),
    MYANMAR("MM"),
    CAMBODIA("KH"),
    INDIA("IN"),
    PAKISTAN("PK"),
    ABU_DHABI("AE"), // UAE code for Abu Dhabi
    ANDORRA("AD"),
    BELGIUM("BE"),
    CANADA("CA"),
    CHINA_MAINLAND("CN"),
    CROATIA("HR"),
    CZECHIA("CZ"),
    CZECH_REPUBLIC("CZ"),
    DENMARK("DK"),
    DUBAI("AE"),
    ESTONIA("EE"),
    FINLAND("FI"),
    GREECE("GR"),
    HONG_KONG("HK"),
    HONG_KONG_SAR_CHINA("HK"),
    HUNGARY("HU"),
    ICELAND("IS"),
    IRELAND("IE"),
    LATVIA("LV"),
    LITHUANIA("LT"),
    LUXEMBOURG("LU"),
    MACAU("MO"),
    MALTA("MT"),
    NETHERLANDS("NL"),
    QATAR("QA"),
    SERBIA("RS"),
    SINGAPORE("SG"),
    SLOVENIA("SI"),
    SOUTH_KOREA("KR"),
    SWEDEN("SE"),
    TAIWAN("TW"),
    TÜRKIYE("TR"),
    USA("US"),
    SRI_LANKA("LK"),
    NEPAL("NP"),
    IRAN("IR"),
    ISRAEL("IL"),
    LEBANON("LB"),
    RUSSIA("RU"),
    POLAND("PL"),
    NORWAY("NO"),
    SCANDINAVIA("SE"), // Approximate as Scandinavia spans countries
    TURKEY("TR");

    private final String isoCode;

    Nation(String isoCode) {
        this.isoCode = isoCode;
    }

    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
