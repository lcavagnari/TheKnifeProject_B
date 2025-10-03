package it.uninsubria.laboratorioa.objects.enums;

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

    private final String isoCode;

    private static final Map<String, Nation> ALIAS_MAP = Map.ofEntries(
            // Afghanistan
            Map.entry("afghanistan", AFGHANISTAN),
            Map.entry("af", AFGHANISTAN),

            // Africa
            Map.entry("africa", AFRICA),

            // Austria
            Map.entry("austria", AUSTRIA),
            Map.entry("at", AUSTRIA),
            Map.entry("österreich", AUSTRIA),

            // Switzerland
            Map.entry("switzerland", SWITZERLAND),
            Map.entry("ch", SWITZERLAND),
            Map.entry("schweiz", SWITZERLAND),
            Map.entry("suisse", SWITZERLAND),
            Map.entry("svizzera", SWITZERLAND),
            Map.entry("svizra", SWITZERLAND),

            // Germany
            Map.entry("germany", GERMANY),
            Map.entry("de", GERMANY),
            Map.entry("deutschland", GERMANY),

            // France
            Map.entry("france", FRANCE),
            Map.entry("fr", FRANCE),

            // Spain
            Map.entry("spain", SPAIN),
            Map.entry("es", SPAIN),
            Map.entry("españa", SPAIN),

            // Portugal
            Map.entry("portugal", PORTUGAL),
            Map.entry("pt", PORTUGAL),

            // Italy
            Map.entry("italy", ITALY),
            Map.entry("it", ITALY),
            Map.entry("italia", ITALY),

            // United Kingdom
            Map.entry("united kingdom", UNITED_KINGDOM),
            Map.entry("gb", UNITED_KINGDOM),
            Map.entry("uk", UNITED_KINGDOM),
            Map.entry("great britain", UNITED_KINGDOM),
            Map.entry("britain", UNITED_KINGDOM),
            Map.entry("england", UNITED_KINGDOM),

            // United States
            Map.entry("united states", UNITED_STATES),
            Map.entry("us", UNITED_STATES),
            Map.entry("united states of america", UNITED_STATES),
            Map.entry("u.s.a.", UNITED_STATES),
            Map.entry("u.s.", UNITED_STATES),
            Map.entry("america", UNITED_STATES),

            // Brazil
            Map.entry("brazil", BRAZIL),
            Map.entry("br", BRAZIL),
            Map.entry("brasil", BRAZIL),

            // Argentina
            Map.entry("argentina", ARGENTINA),
            Map.entry("ar", ARGENTINA),

            // Mexico
            Map.entry("mexico", MEXICO),
            Map.entry("mx", MEXICO),
            Map.entry("méxico", MEXICO),

            // Peru
            Map.entry("peru", PERU),
            Map.entry("pe", PERU),
            Map.entry("perú", PERU),

            // Colombia
            Map.entry("colombia", COLOMBIA),
            Map.entry("co", COLOMBIA),

            // Puerto Rico
            Map.entry("puerto rico", PUERTO_RICO),
            Map.entry("pr", PUERTO_RICO),

            // Venezuela
            Map.entry("venezuela", VENEZUELA),
            Map.entry("ve", VENEZUELA),

            // Cuba
            Map.entry("cuba", CUBA),
            Map.entry("cu", CUBA),

            // Japan
            Map.entry("japan", JAPAN),
            Map.entry("jp", JAPAN),
            Map.entry("日本", JAPAN),
            Map.entry("nippon", JAPAN),
            Map.entry("nihon", JAPAN),

            // Korea
            Map.entry("korea", KOREA),
            Map.entry("kr", KOREA),
            Map.entry("한국", KOREA),

            // China
            Map.entry("china", CHINA),
            Map.entry("cn", CHINA),
            Map.entry("中国", CHINA),
            Map.entry("中華", CHINA),
            Map.entry("zhongguo", CHINA),

            // Thailand
            Map.entry("thailand", THAILAND),
            Map.entry("th", THAILAND),
            Map.entry("ประเทศไทย", THAILAND),
            Map.entry("prathet thai", THAILAND),

            // Vietnam
            Map.entry("vietnam", VIETNAM),
            Map.entry("vn", VIETNAM),
            Map.entry("việt nam", VIETNAM),

            // Malaysia
            Map.entry("malaysia", MALAYSIA),
            Map.entry("my", MALAYSIA),

            // Indonesia
            Map.entry("indonesia", INDONESIA),
            Map.entry("id", INDONESIA),

            // Philippines
            Map.entry("philippines", PHILIPPINES),
            Map.entry("ph", PHILIPPINES),
            Map.entry("pilipinas", PHILIPPINES),

            // Laos
            Map.entry("laos", LAOS),
            Map.entry("la", LAOS),
            Map.entry("ລາວ", LAOS),

            // Myanmar
            Map.entry("myanmar", MYANMAR),
            Map.entry("mm", MYANMAR),
            Map.entry("burma", MYANMAR),
            Map.entry("မြန်မာ", MYANMAR),

            // Cambodia
            Map.entry("cambodia", CAMBODIA),
            Map.entry("kh", CAMBODIA),
            Map.entry("កម្ពុជា", CAMBODIA),
            Map.entry("kampuchea", CAMBODIA),

            // India
            Map.entry("india", INDIA),
            Map.entry("in", INDIA),
            Map.entry("भारत", INDIA),
            Map.entry("bharat", INDIA),

            // Pakistan
            Map.entry("pakistan", PAKISTAN),
            Map.entry("pk", PAKISTAN),
            Map.entry("پاکستان", PAKISTAN),

            // Abu Dhabi
            Map.entry("abu dhabi", ABU_DHABI),
            Map.entry("ae", ABU_DHABI),
            Map.entry("أبو ظبي", ABU_DHABI),

            // Andorra
            Map.entry("andorra", ANDORRA),
            Map.entry("ad", ANDORRA),

            // Belgium
            Map.entry("belgium", BELGIUM),
            Map.entry("be", BELGIUM),
            Map.entry("belgië", BELGIUM),
            Map.entry("belgique", BELGIUM),
            Map.entry("belgien", BELGIUM),

            // Canada
            Map.entry("canada", CANADA),
            Map.entry("ca", CANADA),

            // Mainland China
            Map.entry("mainland china", MAINLAND_CHINA),
            Map.entry("中国大陆", MAINLAND_CHINA),

            // Croatia
            Map.entry("croatia", CROATIA),
            Map.entry("hr", CROATIA),
            Map.entry("hrvatska", CROATIA),

            // Czechia
            Map.entry("czechia", CZECHIA),
            Map.entry("cz", CZECHIA),
            Map.entry("česko", CZECHIA),

            // Czech Republic
            Map.entry("czech republic", CZECH_REPUBLIC),
            Map.entry("česká republika", CZECH_REPUBLIC),

            // Denmark
            Map.entry("denmark", DENMARK),
            Map.entry("dk", DENMARK),
            Map.entry("danmark", DENMARK),

            // Dubai
            Map.entry("dubai", DUBAI),
            Map.entry("دبي", DUBAI),

            // Estonia
            Map.entry("estonia", ESTONIA),
            Map.entry("ee", ESTONIA),
            Map.entry("eesti", ESTONIA),

            // Finland
            Map.entry("finland", FINLAND),
            Map.entry("fi", FINLAND),
            Map.entry("suomi", FINLAND),

            // Greece
            Map.entry("greece", GREECE),
            Map.entry("gr", GREECE),
            Map.entry("ελλάδα", GREECE),
            Map.entry("hellas", GREECE),
            Map.entry("ellada", GREECE),

            // Hong Kong
            Map.entry("hong kong", HONG_KONG),
            Map.entry("hk", HONG_KONG),
            Map.entry("香港", HONG_KONG),
            Map.entry("heung gong", HONG_KONG),

            // Hong Kong SAR
            Map.entry("hong kong sar china", HONG_KONG_SAR_CHINA),
            Map.entry("香港特別行政區", HONG_KONG_SAR_CHINA),

            // Hungary
            Map.entry("hungary", HUNGARY),
            Map.entry("hu", HUNGARY),
            Map.entry("magyarország", HUNGARY),

            // Iceland
            Map.entry("iceland", ICELAND),
            Map.entry("is", ICELAND),
            Map.entry("ísland", ICELAND),

            // Ireland
            Map.entry("ireland", IRELAND),
            Map.entry("ie", IRELAND),
            Map.entry("éire", IRELAND),

            // Latvia
            Map.entry("latvia", LATVIA),
            Map.entry("lv", LATVIA),
            Map.entry("latvija", LATVIA),

            // Lithuania
            Map.entry("lithuania", LITHUANIA),
            Map.entry("lt", LITHUANIA),
            Map.entry("lietuva", LITHUANIA),

            // Luxembourg
            Map.entry("luxembourg", LUXEMBOURG),
            Map.entry("lu", LUXEMBOURG),
            Map.entry("lëtzebuerg", LUXEMBOURG),
            Map.entry("luxemburg", LUXEMBOURG),

            // Macao
            Map.entry("macao", MACAO),
            Map.entry("mo", MACAO),
            Map.entry("澳門", MACAO),
            Map.entry("macau", MACAO),

            // Malta
            Map.entry("malta", MALTA),
            Map.entry("mt", MALTA),

            // Netherlands
            Map.entry("netherlands", NETHERLANDS),
            Map.entry("nl", NETHERLANDS),
            Map.entry("nederland", NETHERLANDS),
            Map.entry("holland", NETHERLANDS),

            // Qatar
            Map.entry("qatar", QATAR),
            Map.entry("qa", QATAR),
            Map.entry("قطر", QATAR),

            // Serbia
            Map.entry("serbia", SERBIA),
            Map.entry("rs", SERBIA),
            Map.entry("србија", SERBIA),
            Map.entry("srbija", SERBIA),

            // Singapore
            Map.entry("singapore", SINGAPORE),
            Map.entry("sg", SINGAPORE),
            Map.entry("新加坡", SINGAPORE),
            Map.entry("singapura", SINGAPORE),

            // Slovenia
            Map.entry("slovenia", SLOVENIA),
            Map.entry("si", SLOVENIA),
            Map.entry("slovenija", SLOVENIA),

            // South Korea
            Map.entry("south korea", SOUTH_KOREA),
            Map.entry("대한민국", SOUTH_KOREA),
            Map.entry("daehan minguk", SOUTH_KOREA),
            Map.entry("republic of korea", SOUTH_KOREA),

            // Sweden
            Map.entry("sweden", SWEDEN),
            Map.entry("se", SWEDEN),
            Map.entry("sverige", SWEDEN),

            // Taiwan
            Map.entry("taiwan", TAIWAN),
            Map.entry("tw", TAIWAN),
            Map.entry("台灣", TAIWAN),
            Map.entry("臺灣", TAIWAN),
            Map.entry("中華民國", TAIWAN),

            // Turkey
            Map.entry("turkey", TURKEY),
            Map.entry("tr", TURKEY),
            Map.entry("türkiye", TURKEY),
            Map.entry("turkiye", TURKEY),

            // Macau
            Map.entry("macau", MACAU),
            Map.entry("ma", MACAU),
            Map.entry("澳门", MACAU),

            // USA
            Map.entry("usa", USA),

            // Sri Lanka
            Map.entry("sri lanka", SRI_LANKA),
            Map.entry("lk", SRI_LANKA),
            Map.entry("ශ්‍රී ලංකා", SRI_LANKA),
            Map.entry("இலங்கை", SRI_LANKA),

            // Nepal
            Map.entry("nepal", NEPAL),
            Map.entry("np", NEPAL),
            Map.entry("नेपाल", NEPAL),

            // Iran
            Map.entry("iran", IRAN),
            Map.entry("ir", IRAN),
            Map.entry("ایران", IRAN),
            Map.entry("persia", IRAN),

            // Israel
            Map.entry("israel", ISRAEL),
            Map.entry("il", ISRAEL),
            Map.entry("ישראל", ISRAEL),
            Map.entry("yisra'el", ISRAEL),

            // Lebanon
            Map.entry("lebanon", LEBANON),
            Map.entry("lb", LEBANON),
            Map.entry("لبنان", LEBANON),
            Map.entry("lubnan", LEBANON),

            // Russia
            Map.entry("russia", RUSSIA),
            Map.entry("ru", RUSSIA),
            Map.entry("россия", RUSSIA),
            Map.entry("rossiya", RUSSIA),

            // Poland
            Map.entry("poland", POLAND),
            Map.entry("pl", POLAND),
            Map.entry("polska", POLAND),

            // Norway
            Map.entry("norway", NORWAY),
            Map.entry("no", NORWAY),
            Map.entry("norge", NORWAY),
            Map.entry("noreg", NORWAY),

            // Scandinavia
            Map.entry("scandinavia", SCANDINAVIA)
    );

    Nation(String isoCode) {
        this.isoCode = isoCode;
    }

    /**
     * Find a nation by name, alias, or ISO code (case-insensitive)
     * @param identifier The nation name, alias, or ISO code
     * @return The matching Nation, or null if not found
     */
    public static Nation fromString(String identifier) {
        if (identifier == null) return null;
        return ALIAS_MAP.getOrDefault(identifier.toLowerCase().trim(),null);
    }

    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}