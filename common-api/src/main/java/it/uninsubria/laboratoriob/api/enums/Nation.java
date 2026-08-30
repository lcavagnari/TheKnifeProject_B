package it.uninsubria.laboratoriob.api.enums;

import lombok.Getter;

import java.util.Map;

/**
 * Enum representing the nations supported by the system.
 * Each nation is associated with an ISO 3166-1 alpha-2 code.
 * <p>
 * Author: Luca Cavagnari
 * Version: 1.0
 */
@Getter
public enum Nation {
    /**
     * Afghanistan
     */
    AFGHANISTAN("AF"),
    /**
     * African continent (approximate)
     */
    AFRICA("AF"),
    /**
     * Austria
     */
    AUSTRIA("AT"),
    /**
     * Switzerland
     */
    SWITZERLAND("CH"),
    /**
     * Germany
     */
    GERMANY("DE"),
    /**
     * France
     */
    FRANCE("FR"),
    /**
     * Spain
     */
    SPAIN("ES"),
    /**
     * Portugal
     */
    PORTUGAL("PT"),
    /**
     * Italy
     */
    ITALY("IT"),
    /**
     * United Kingdom
     */
    UNITED_KINGDOM("GB"),
    /**
     * United States of America
     */
    UNITED_STATES("US"),
    /**
     * Brazil
     */
    BRAZIL("BR"),
    /**
     * Argentina
     */
    ARGENTINA("AR"),
    /**
     * Mexico
     */
    MEXICO("MX"),
    /**
     * Peru
     */
    PERU("PE"),
    /**
     * Colombia
     */
    COLOMBIA("CO"),
    /**
     * Puerto Rico
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
     * Japan
     */
    JAPAN("JP"),
    /**
     * Korea (generic)
     */
    KOREA("KR"),
    /**
     * China
     */
    CHINA("CN"),
    /**
     * Thailand
     */
    THAILAND("TH"),
    /**
     * Vietnam
     */
    VIETNAM("VN"),
    /**
     * Malaysia
     */
    MALAYSIA("MY"),
    /**
     * Indonesia
     */
    INDONESIA("ID"),
    /**
     * Philippines
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
     * Cambodia
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
     * Belgium
     */
    BELGIUM("BE"),
    /**
     * Canada
     */
    CANADA("CA"),
    /**
     * Mainland China
     */
    MAINLAND_CHINA("CN"),
    /**
     * Croatia
     */
    CROATIA("HR"),
    /**
     * Czechia (short name)
     */
    CZECHIA("CZ"),
    /**
     * Czech Republic (full name)
     */
    CZECH_REPUBLIC("CZ"),
    /**
     * Denmark
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
     * Finland
     */
    FINLAND("FI"),
    /**
     * Greece
     */
    GREECE("GR"),
    /**
     * Hong Kong
     */
    HONG_KONG("HK"),
    /**
     * Hong Kong SAR (extended name)
     */
    HONG_KONG_SAR_CHINA("HK"),
    /**
     * Hungary
     */
    HUNGARY("HU"),
    /**
     * Iceland
     */
    ICELAND("IS"),
    /**
     * Ireland
     */
    IRELAND("IE"),
    /**
     * Latvia
     */
    LATVIA("LV"),
    /**
     * Lithuania
     */
    LITHUANIA("LT"),
    /**
     * Luxembourg
     */
    LUXEMBOURG("LU"),
    /**
     * Macao
     */
    MACAO("MO"),
    /**
     * Malta
     */
    MALTA("MT"),
    /**
     * Netherlands
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
     * South Korea
     */
    SOUTH_KOREA("KR"),
    /**
     * Sweden
     */
    SWEDEN("SE"),
    /**
     * Taiwan
     */
    TAIWAN("TW"),
    /**
     * Turkey
     */
    TURKEY("TR"),
    /**
     * Macau
     */
    MACAU("MA"),
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
     * Israel
     */
    ISRAEL("IL"),
    /**
     * Lebanon
     */
    LEBANON("LB"),
    /**
     * Russia
     */
    RUSSIA("RU"),
    /**
     * Poland
     */
    POLAND("PL"),
    /**
     * Norway
     */
    NORWAY("NO"),
    /**
     * Scandinavia (approximated as Sweden)
     */
    SCANDINAVIA("SE");

    /**
     * Mappa degli alias per i nomi delle nazioni
     */
    private static final Map<String, Nation> ALIAS_MAP = Map.<String, Nation>ofEntries(
            // Afghanistan
            Map.entry("af", AFGHANISTAN),
            // Austria
            Map.entry("at", AUSTRIA),
            Map.entry("österreich", AUSTRIA),
            // Switzerland
            Map.entry("ch", SWITZERLAND),
            Map.entry("schweiz", SWITZERLAND),
            Map.entry("suisse", SWITZERLAND),
            Map.entry("svizzera", SWITZERLAND),
            Map.entry("svizra", SWITZERLAND),
            // Germany
            Map.entry("de", GERMANY),
            Map.entry("deutschland", GERMANY),
            // France
            Map.entry("fr", FRANCE),
            // Spain
            Map.entry("es", SPAIN),
            Map.entry("españa", SPAIN),
            // Portugal
            Map.entry("pt", PORTUGAL),
            // Italy
            Map.entry("it", ITALY),
            Map.entry("italia", ITALY),
            // United Kingdom
            Map.entry("gb", UNITED_KINGDOM),
            Map.entry("uk", UNITED_KINGDOM),
            Map.entry("great britain", UNITED_KINGDOM),
            Map.entry("britain", UNITED_KINGDOM),
            Map.entry("england", UNITED_KINGDOM),
            // United States
            Map.entry("us", UNITED_STATES),
            Map.entry("united states of america", UNITED_STATES),
            Map.entry("u.s.a.", UNITED_STATES),
            Map.entry("u.s.", UNITED_STATES),
            Map.entry("america", UNITED_STATES),
            // Brazil
            Map.entry("br", BRAZIL),
            Map.entry("brasil", BRAZIL),
            // Argentina
            Map.entry("ar", ARGENTINA),
            // Mexico
            Map.entry("mx", MEXICO),
            Map.entry("méxico", MEXICO),
            // Peru
            Map.entry("pe", PERU),
            Map.entry("perú", PERU),
            // Colombia
            Map.entry("co", COLOMBIA),
            // Puerto Rico
            Map.entry("pr", PUERTO_RICO),
            // Venezuela
            Map.entry("ve", VENEZUELA),
            // Cuba
            Map.entry("cu", CUBA),
            // Japan
            Map.entry("jp", JAPAN),
            Map.entry("日本", JAPAN),
            Map.entry("nippon", JAPAN),
            Map.entry("nihon", JAPAN),
            // Korea
            Map.entry("kr", KOREA),
            Map.entry("한국", KOREA),
            // China
            Map.entry("cn", CHINA),
            Map.entry("中国", CHINA),
            Map.entry("中華", CHINA),
            Map.entry("zhongguo", CHINA),
            // Thailand
            Map.entry("th", THAILAND),
            Map.entry("ประเทศไทย", THAILAND),
            Map.entry("prathet thai", THAILAND),
            // Vietnam
            Map.entry("vn", VIETNAM),
            Map.entry("việt nam", VIETNAM),
            // Malaysia
            Map.entry("my", MALAYSIA),
            // Indonesia
            Map.entry("id", INDONESIA),
            // Philippines
            Map.entry("ph", PHILIPPINES),
            Map.entry("pilipinas", PHILIPPINES),
            // Laos
            Map.entry("la", LAOS),
            Map.entry("ລາວ", LAOS),
            // Myanmar
            Map.entry("mm", MYANMAR),
            Map.entry("burma", MYANMAR),
            Map.entry("မြန်မာ", MYANMAR),
            // Cambodia
            Map.entry("kh", CAMBODIA),
            Map.entry("កម្ពុជា", CAMBODIA),
            Map.entry("kampuchea", CAMBODIA),
            // India
            Map.entry("in", INDIA),
            Map.entry("भारत", INDIA),
            Map.entry("bharat", INDIA),
            // Pakistan
            Map.entry("pk", PAKISTAN),
            Map.entry("پاکستان", PAKISTAN),
            // Abu Dhabi
            Map.entry("ae", ABU_DHABI),
            Map.entry("أبو ظبي", ABU_DHABI),
            // Andorra
            Map.entry("ad", ANDORRA),
            // Belgium
            Map.entry("be", BELGIUM),
            Map.entry("belgië", BELGIUM),
            Map.entry("belgique", BELGIUM),
            Map.entry("belgien", BELGIUM),
            // Canada
            Map.entry("ca", CANADA),
            // Mainland China
            Map.entry("中国大陆", MAINLAND_CHINA),
            // Croatia
            Map.entry("hr", CROATIA),
            Map.entry("hrvatska", CROATIA),
            // Czechia
            Map.entry("cz", CZECHIA),
            Map.entry("česko", CZECHIA),
            // Czech Republic
            Map.entry("česká republika", CZECH_REPUBLIC),
            // Denmark
            Map.entry("dk", DENMARK),
            Map.entry("danmark", DENMARK),
            // Dubai
            Map.entry("دبي", DUBAI),
            // Estonia
            Map.entry("ee", ESTONIA),
            Map.entry("eesti", ESTONIA),
            // Finland
            Map.entry("fi", FINLAND),
            Map.entry("suomi", FINLAND),
            // Greece
            Map.entry("gr", GREECE),
            Map.entry("ελλάδα", GREECE),
            Map.entry("hellas", GREECE),
            Map.entry("ellada", GREECE),
            // Hong Kong
            Map.entry("hk", HONG_KONG),
            Map.entry("香港", HONG_KONG),
            Map.entry("heung gong", HONG_KONG),
            // Hong Kong SAR
            Map.entry("香港特別行政區", HONG_KONG_SAR_CHINA),
            // Hungary
            Map.entry("hu", HUNGARY),
            Map.entry("magyarország", HUNGARY),
            // Iceland
            Map.entry("is", ICELAND),
            Map.entry("ísland", ICELAND),
            // Ireland
            Map.entry("ie", IRELAND),
            Map.entry("éire", IRELAND),
            // Latvia
            Map.entry("lv", LATVIA),
            Map.entry("latvija", LATVIA),
            // Lithuania
            Map.entry("lt", LITHUANIA),
            Map.entry("lietuva", LITHUANIA),
            // Luxembourg
            Map.entry("lu", LUXEMBOURG),
            Map.entry("lëtzebuerg", LUXEMBOURG),
            Map.entry("luxemburg", LUXEMBOURG),
            // Macao
            Map.entry("mo", MACAO),
            Map.entry("澳門", MACAO),
            Map.entry("macau", MACAO),
            // Malta
            Map.entry("mt", MALTA),
            // Netherlands
            Map.entry("nl", NETHERLANDS),
            Map.entry("nederland", NETHERLANDS),
            Map.entry("holland", NETHERLANDS),
            // Qatar
            Map.entry("qa", QATAR),
            Map.entry("قطر", QATAR),
            // Serbia
            Map.entry("rs", SERBIA),
            Map.entry("србија", SERBIA),
            Map.entry("srbija", SERBIA),
            // Singapore
            Map.entry("sg", SINGAPORE),
            Map.entry("新加坡", SINGAPORE),
            Map.entry("singapura", SINGAPORE),
            // Slovenia
            Map.entry("si", SLOVENIA),
            Map.entry("slovenija", SLOVENIA),
            // South Korea
            Map.entry("대한민국", SOUTH_KOREA),
            Map.entry("daehan minguk", SOUTH_KOREA),
            Map.entry("republic of korea", SOUTH_KOREA),
            // Sweden
            Map.entry("se", SWEDEN),
            Map.entry("sverige", SWEDEN),
            // Taiwan
            Map.entry("tw", TAIWAN),
            Map.entry("台灣", TAIWAN),
            Map.entry("臺灣", TAIWAN),
            Map.entry("中華民國", TAIWAN),
            // Turkey
            Map.entry("tr", TURKEY),
            Map.entry("türkiye", TURKEY),
            Map.entry("turkiye", TURKEY),
            // Macau
            Map.entry("ma", MACAU),
            Map.entry("澳门", MACAU),
            // Sri Lanka
            Map.entry("lk", SRI_LANKA),
            Map.entry("ශ්‍රී ලංකා", SRI_LANKA),
            Map.entry("இலங்கை", SRI_LANKA),
            // Nepal
            Map.entry("np", NEPAL),
            Map.entry("नेपाल", NEPAL),
            // Iran
            Map.entry("ir", IRAN),
            Map.entry("ایران", IRAN),
            Map.entry("persia", IRAN),
            // Israel
            Map.entry("il", ISRAEL),
            Map.entry("ישראל", ISRAEL),
            Map.entry("yisra'el", ISRAEL),
            // Lebanon
            Map.entry("lb", LEBANON),
            Map.entry("لبنان", LEBANON),
            Map.entry("lubnan", LEBANON),
            // Russia
            Map.entry("ru", RUSSIA),
            Map.entry("россия", RUSSIA),
            Map.entry("rossiya", RUSSIA),
            // Poland
            Map.entry("pl", POLAND),
            Map.entry("polska", POLAND),
            // Norway
            Map.entry("no", NORWAY),
            Map.entry("norge", NORWAY),
            Map.entry("noreg", NORWAY)
    );

    private final String isoCode;

    Nation(String isoCode) {
        this.isoCode = isoCode;
    }

    /**
     * Find a nation by name, alias, or ISO code (case-insensitive)
     *
     * @param identifier The nation name, alias, or ISO code
     * @return The matching Nation, or null if not found
     */
    public static Nation fromString(String identifier) {
        if (identifier == null) return null;

        String s = identifier.toLowerCase().trim();
        Nation byAlias = ALIAS_MAP.getOrDefault(s, null);
        if (byAlias != null) return byAlias;

        // Fallback: confronta col nome dell'enum stesso (es. "united kingdom",
        // "united-kingdom" o "united_kingdom" per UNITED_KINGDOM), cosi ogni
        // nazione resta riconoscibile anche quando non e' presente in ALIAS_MAP.
        String normalized = s.replace(' ', '_').replace('-', '_');
        for (Nation n : values())
            if (n.name().equalsIgnoreCase(normalized)) return n;

        return null;
    }

    /**
     * @return Nome nazione capitalizzato
     */
    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}