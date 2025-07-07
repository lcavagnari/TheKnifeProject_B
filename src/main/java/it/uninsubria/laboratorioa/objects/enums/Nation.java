package it.uninsubria.laboratorioa.objects.enums;

import lombok.Getter;

/**
 * Enum che rappresenta le Nazioni supportate dal sistema.
 * Ogni nazione è associata a un codice ISO 3166-1 alpha-2.
 */
@Getter
public enum Nation {

    /**
     * Afghanistan
     */
    AFGHANISTAN("AF"),
    /**
     * Continente africano (approssimativo)
     */
    AFRICA("AF"),
    /**
     * Austria
     */
    AUSTRIA("AT"),
    /**
     * Svizzera
     */
    SWITZERLAND("CH"),
    /**
     * Germania
     */
    GERMANY("DE"),
    /**
     * Francia
     */
    FRANCE("FR"),
    /**
     * Spagna
     */
    SPAIN("ES"),
    /**
     * Portogallo
     */
    PORTUGAL("PT"),
    /**
     * Italia
     */
    ITALY("IT"),
    /**
     * Regno Unito
     */
    UNITED_KINGDOM("GB"),
    /**
     * Stati Uniti d'America
     */
    UNITED_STATES("US"),
    /**
     * Brasile
     */
    BRAZIL("BR"),
    /**
     * Argentina
     */
    ARGENTINA("AR"),
    /**
     * Messico
     */
    MEXICO("MX"),
    /**
     * Perù
     */
    PERU("PE"),
    /**
     * Colombia
     */
    COLOMBIA("CO"),
    /**
     * Porto Rico
     */
    PUERTO_RICO("PR"),
    /**
     * Venezuela
     */
    VENEZUELA("VE"),
    /**
     * Cuba
     */
    CUBA("CU"),
    /**
     * Giappone
     */
    JAPAN("JP"),
    /**
     * Corea (generico)
     */
    KOREA("KR"),
    /**
     * Cina
     */
    CHINA("CN"),
    /**
     * Thailandia
     */
    THAILAND("TH"),
    /**
     * Vietnam
     */
    VIETNAM("VN"),
    /**
     * Malesia
     */
    MALAYSIA("MY"),
    /**
     * Indonesia
     */
    INDONESIA("ID"),
    /**
     * Filippine
     */
    PHILIPPINES("PH"),
    /**
     * Laos
     */
    LAOS("LA"),
    /**
     * Myanmar
     */
    MYANMAR("MM"),
    /**
     * Cambogia
     */
    CAMBODIA("KH"),
    /**
     * India
     */
    INDIA("IN"),
    /**
     * Pakistan
     */
    PAKISTAN("PK"),
    /**
     * Abu Dhabi (UAE)
     */
    ABU_DHABI("AE"),
    /**
     * Andorra
     */
    ANDORRA("AD"),
    /**
     * Belgio
     */
    BELGIUM("BE"),
    /**
     * Canada
     */
    CANADA("CA"),
    /**
     * Cina continentale
     */
    CHINA_MAINLAND("CN"),
    /**
     * Croazia
     */
    CROATIA("HR"),
    /**
     * Repubblica Ceca (nome breve)
     */
    CZECHIA("CZ"),
    /**
     * Repubblica Ceca (nome completo)
     */
    CZECH_REPUBLIC("CZ"),
    /**
     * Danimarca
     */
    DENMARK("DK"),
    /**
     * Dubai (UAE)
     */
    DUBAI("AE"),
    /**
     * Estonia
     */
    ESTONIA("EE"),
    /**
     * Finlandia
     */
    FINLAND("FI"),
    /**
     * Grecia
     */
    GREECE("GR"),
    /**
     * Hong Kong
     */
    HONG_KONG("HK"),
    /**
     * Hong Kong SAR (versione estesa)
     */
    HONG_KONG_SAR_CHINA("HK"),
    /**
     * Ungheria
     */
    HUNGARY("HU"),
    /**
     * Islanda
     */
    ICELAND("IS"),
    /**
     * Irlanda
     */
    IRELAND("IE"),
    /**
     * Lettonia
     */
    LATVIA("LV"),
    /**
     * Lituania
     */
    LITHUANIA("LT"),
    /**
     * Lussemburgo
     */
    LUXEMBOURG("LU"),
    /**
     * Macao
     */
    MACAU("MO"),
    /**
     * Malta
     */
    MALTA("MT"),
    /**
     * Paesi Bassi
     */
    NETHERLANDS("NL"),
    /**
     * Qatar
     */
    QATAR("QA"),
    /**
     * Serbia
     */
    SERBIA("RS"),
    /**
     * Singapore
     */
    SINGAPORE("SG"),
    /**
     * Slovenia
     */
    SLOVENIA("SI"),
    /**
     * Corea del Sud
     */
    SOUTH_KOREA("KR"),
    /**
     * Svezia
     */
    SWEDEN("SE"),
    /**
     * Taiwan
     */
    TAIWAN("TW"),
    /**
     * Turchia
     */
    TURKIYE("TR"),
    /**
     * USA
     */
    USA("US"),
    /**
     * Sri Lanka
     */
    SRI_LANKA("LK"),
    /**
     * Nepal
     */
    NEPAL("NP"),
    /**
     * Iran
     */
    IRAN("IR"),
    /**
     * Israele
     */
    ISRAEL("IL"),
    /**
     * Libano
     */
    LEBANON("LB"),
    /**
     * Russia
     */
    RUSSIA("RU"),
    /**
     * Polonia
     */
    POLAND("PL"),
    /**
     * Norvegia
     */
    NORWAY("NO"),
    /**
     * Scandinavia (approssimato come Svezia)
     */
    SCANDINAVIA("SE"),
    /**
     * Turchia
     */
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
