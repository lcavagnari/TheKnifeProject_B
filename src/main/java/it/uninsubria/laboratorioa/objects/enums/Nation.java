package it.uninsubria.laboratorioa.objects.enums;

import lombok.Getter;

/**
 * Enum representing the nations supported by the system.
 * Each nation is associated with an ISO 3166-1 alpha-2 code.
 * <p>
 * Author: Luca Cavagnari
 * Version: 1.0
 */
@Getter
public enum Nation {

    /** Afghanistan */
    AFGHANISTAN("AF"),
    /** African continent (approximate) */
    AFRICA("AF"),
    /** Austria */
    AUSTRIA("AT"),
    /** Switzerland */
    SWITZERLAND("CH"),
    /** Germany */
    GERMANY("DE"),
    /** France */
    FRANCE("FR"),
    /** Spain */
    SPAIN("ES"),
    /** Portugal */
    PORTUGAL("PT"),
    /** Italy */
    ITALY("IT"),
    /** United Kingdom */
    UNITED_KINGDOM("GB"),
    /** United States of America */
    UNITED_STATES("US"),
    /** Brazil */
    BRAZIL("BR"),
    /** Argentina */
    ARGENTINA("AR"),
    /** Mexico */
    MEXICO("MX"),
    /** Peru */
    PERU("PE"),
    /** Colombia */
    COLOMBIA("CO"),
    /** Puerto Rico */
    PUERTO_RICO("PR"),
    /** Venezuela */
    VENEZUELA("VE"),
    /** Cuba */
    CUBA("CU"),
    /** Japan */
    JAPAN("JP"),
    /** Korea (generic) */
    KOREA("KR"),
    /** China */
    CHINA("CN"),
    /** Thailand */
    THAILAND("TH"),
    /** Vietnam */
    VIETNAM("VN"),
    /** Malaysia */
    MALAYSIA("MY"),
    /** Indonesia */
    INDONESIA("ID"),
    /** Philippines */
    PHILIPPINES("PH"),
    /** Laos */
    LAOS("LA"),
    /** Myanmar */
    MYANMAR("MM"),
    /** Cambodia */
    CAMBODIA("KH"),
    /** India */
    INDIA("IN"),
    /** Pakistan */
    PAKISTAN("PK"),
    /** Abu Dhabi (UAE) */
    ABU_DHABI("AE"),
    /** Andorra */
    ANDORRA("AD"),
    /** Belgium */
    BELGIUM("BE"),
    /** Canada */
    CANADA("CA"),
    /** Mainland China */
    MAINLAND_CHINA("CN"),
    /** Croatia */
    CROATIA("HR"),
    /** Czechia (short name) */
    CZECHIA("CZ"),
    /** Czech Republic (full name) */
    CZECH_REPUBLIC("CZ"),
    /** Denmark */
    DENMARK("DK"),
    /** Dubai (UAE) */
    DUBAI("AE"),
    /** Estonia */
    ESTONIA("EE"),
    /** Finland */
    FINLAND("FI"),
    /** Greece */
    GREECE("GR"),
    /** Hong Kong */
    HONG_KONG("HK"),
    /** Hong Kong SAR (extended name) */
    HONG_KONG_SAR_CHINA("HK"),
    /** Hungary */
    HUNGARY("HU"),
    /** Iceland */
    ICELAND("IS"),
    /** Ireland */
    IRELAND("IE"),
    /** Latvia */
    LATVIA("LV"),
    /** Lithuania */
    LITHUANIA("LT"),
    /** Luxembourg */
    LUXEMBOURG("LU"),
    /** Macao */
    MACAO("MO"),
    /** Malta */
    MALTA("MT"),
    /** Netherlands */
    NETHERLANDS("NL"),
    /** Qatar */
    QATAR("QA"),
    /** Serbia */
    SERBIA("RS"),
    /** Singapore */
    SINGAPORE("SG"),
    /** Slovenia */
    SLOVENIA("SI"),
    /** South Korea */
    SOUTH_KOREA("KR"),
    /** Sweden */
    SWEDEN("SE"),
    /** Taiwan */
    TAIWAN("TW"),
    /** Turkey */
    TÜRKIYE("TR"),
    /** Turkey */
    TURKEY("TR"),
    /** Macau */
    MACAU("MA"),
    /** USA */
    USA("US"),
    /** Sri Lanka */
    SRI_LANKA("LK"),
    /** Nepal */
    NEPAL("NP"),
    /** Iran */
    IRAN("IR"),
    /** Israel */
    ISRAEL("IL"),
    /** Lebanon */
    LEBANON("LB"),
    /** Russia */
    RUSSIA("RU"),
    /** Poland */
    POLAND("PL"),
    /** Norway */
    NORWAY("NO"),
    /** Scandinavia (approximated as Sweden) */
    SCANDINAVIA("SE");

    private final String isoCode;

    Nation(String isoCode) {
        this.isoCode = isoCode;
    }

    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
