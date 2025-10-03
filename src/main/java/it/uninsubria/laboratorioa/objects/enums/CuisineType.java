package it.uninsubria.laboratorioa.objects.enums;

/**
 * Enum che rappresenta le possibili cucine servite da un ristorante.
 * Ogni valore identifica uno stile culinario regionale, nazionale o tematico.
 * <p>
 * I nomi dei valori sono scritti in maiuscolo e separati da underscore (`_`), e vengono resi
 * in minuscolo tramite il metodo {@link #toString()} per l'esportazione o la visualizzazione.
 * <p>
 * Nota: i valori di questo enum vengono utilizzati per la serializzazione/deserializzazione e il filtraggio.
 * <p>
 *
 * @author Luca Cavagnari
 * @version 1.0
 */
public enum CuisineType {
    /**
     * Cucina afgana tradizionale
     */
    AFGHAN,
    /**
     * Cucina africana regionale
     */
    AFRICAN,
    /**
     * Cucina alpina tipica delle regioni montane
     */
    ALPINE,
    /**
     * Cucina alsaziana francese
     */
    ALSATIAN,
    /**
     * Cucina americana classica
     */
    AMERICAN,
    /**
     * Cucina americana contemporanea
     */
    AMERICAN_CONTEMPORARY,
    /**
     * Cucina andalusa spagnola
     */
    ANDALUSIAN,
    /**
     * Cucina pugliese italiana
     */
    APULIAN,
    /**
     * Cucina argentina
     */
    ARGENTINIAN,
    /**
     * Cucina armena
     */
    ARMENIAN,
    /**
     * Cucina asiatica generica
     */
    ASIAN,
    /**
     * Influenze asiatiche miste
     */
    ASIAN_INFLUENCES,
    /**
     * Cucina asturiana spagnola
     */
    ASTURIAN,
    /**
     * Cucina austriaca
     */
    AUSTRIAN,
    /**
     * Panetteria e prodotti da forno
     */
    BAKERY,
    /**
     * Cucina balinese dell'isola di Bali
     */
    BALINESE,
    /**
     * Cucina balcanica
     */
    BALKAN,
    /**
     * Cucina barbecue tipica di grigliate
     */
    BARBECUE,
    /**
     * Cucina basca spagnola
     */
    BASQUE,
    /**
     * Cucina bavarese tedesca
     */
    BAVARIAN,
    /**
     * Specialità a base di manzo
     */
    BEEF,
    /**
     * Cucina di Pechino, Cina
     */
    BEIJING,
    /**
     * Cucina belga
     */
    BELGIAN,
    /**
     * Cucina brasiliana
     */
    BRAZILIAN,
    /**
     * Cucina bretone francese
     */
    BRETON,
    /**
     * Bulgogi coreano
     */
    BULGOGI,
    /**
     * Cucina borgognona francese
     */
    BURGUNDIAN,
    /**
     * Cucina birmana
     */
    BURMESE,
    /**
     * Cucina cajun della Louisiana
     */
    CAJUN,
    /**
     * Cucina calabrese italiana
     */
    CALABRIAN,
    /**
     * Cucina californiana
     */
    CALIFORNIAN,
    /**
     * Cucina cambogiana
     */
    CAMBODIAN,
    /**
     * Cucina campana italiana
     */
    CAMPANIAN,
    /**
     * Cucina cantonese
     */
    CANTONESE,
    /**
     * Carni arrosto cantonese
     */
    CANTONESE_ROAST_MEATS,
    /**
     * Cucina caraibica
     */
    CARIBBEAN,
    /**
     * Cucina castigliana spagnola
     */
    CASTILIAN,
    /**
     * Cucina catalana spagnola
     */
    CATALAN,
    /**
     * Cucina dell’Asia centrale
     */
    CENTRAL_ASIAN,
    /**
     * Cucina Chao Zhou cinese
     */
    CHAO_ZHOU,
    /**
     * Specialità di formaggi
     */
    CHEESE,
    /**
     * Specialità a base di pollo
     */
    CHICKEN_SPECIALITIES,
    /**
     * Cucina cinese generica
     */
    CHINESE,
    /**
     * Cucina Chiu Chow cinese
     */
    CHIU_CHOW,
    /**
     * Cucina Chueotang coreana
     */
    CHUEOTANG,
    /**
     * Cucina classica
     */
    CLASSIC,
    /**
     * Cucina colombiana
     */
    COLOMBIAN,
    /**
     * Congee – porridge di riso cinese
     */
    CONGEE,
    /**
     * Cucina contemporanea generica
     */
    CONTEMPORARY,
    /**
     * Cucina corsa
     */
    CORSICAN,
    /**
     * Cucina casalinga di campagna
     */
    COUNTRY,
    /**
     * Specialità a base di granchio
     */
    CRAB_SPECIALITIES,
    /**
     * Cucina creativa
     */
    CREATIVE,
    /**
     * Cucina creola
     */
    CREOLE,
    /**
     * Cucina croata
     */
    CROATIAN,
    /**
     * Cucina cubana
     */
    CUBAN,
    /**
     * Cucina al curry
     */
    CURRY,
    /**
     * Cucina ceca
     */
    CZECH,
    /**
     * Cucina danese
     */
    DANISH,
    /**
     * Gastronomia, specialità da deli
     */
    DELI,
    /**
     * Dim sum cinese
     */
    DIM_SUM,
    /**
     * Cucina doganitang coreana
     */
    DOGANITANG,
    /**
     * Cucina Dongbei cinese
     */
    DONGBEI,
    /**
     * Cucina Dubu coreana
     */
    DUBU,
    /**
     * Specialità a base di anatra
     */
    DUCK_SPECIALITIES,
    /**
     * Specialità di ravioli
     */
    DUMPLINGS,
    /**
     * Dwaeji Gukbap – zuppa di maiale coreana
     */
    DWAEJI_GUKBAP,
    /**
     * Cucina dell'Europa orientale
     */
    EASTERN_EUROPEAN,
    /**
     * Cucina egiziana
     */
    EGYPTIAN,
    /**
     * Cucina emiliana italiana
     */
    EMILIAN,
    /**
     * Cucina degli Emirati Arabi
     */
    EMIRATI,
    /**
     * Cucina inglese
     */
    ENGLISH,
    /**
     * Cucina etiope
     */
    ETHIOPIAN,
    /**
     * Cucina europea generica
     */
    EUROPEAN,
    /**
     * Cucina "farm to table" – a chilometro zero
     */
    FARM_TO_TABLE,
    /**
     * Cucina filippina
     */
    FILIPINO,
    /**
     * Cucina finlandese
     */
    FINNISH,
    /**
     * Cucina fiamminga
     */
    FLEMISH,
    /**
     * Fondue – specialità svizzera
     */
    FONDUE,
    /**
     * Cucina francese
     */
    FRENCH,
    /**
     * Anguilla di acqua dolce
     */
    FRESHWATER_EEL,
    /**
     * Cucina friulana italiana
     */
    FRIULIAN,
    /**
     * Fugu – pesce palla giapponese
     */
    FUGU,
    /**
     * Cucina Fujian cinese
     */
    FUJIAN,
    /**
     * Cucina fusion
     */
    FUSION,
    /**
     * Cucina galiziana spagnola
     */
    GALICIAN,
    /**
     * Gastropub – cucina da pub
     */
    GASTROPUB,
    /**
     * Gejang – specialità coreana di granchi marinati
     */
    GEJANG,
    /**
     * Cucina tedesca
     */
    GERMAN,
    /**
     * Gomtang – zuppa coreana
     */
    GOMTANG,
    /**
     * Cucina greca
     */
    GREEK,
    /**
     * Grigliate generiche
     */
    GRILLS,
    /**
     * Cucina hainanese cinese
     */
    HAINANESE,
    /**
     * Cucina hakkanese cinese
     */
    HAKKANENESE,
    /**
     * Cucina Hang Zhou cinese
     */
    HANG_ZHOU,
    /**
     * Cucina casalinga
     */
    HOME_COOKING,
    /**
     * Hotpot – piatto cinese
     */
    HOTPOT,
    /**
     * Cucina Huaiyang cinese
     */
    HUAIYANG,
    /**
     * Cucina Hubei cinese
     */
    HUBEI,
    /**
     * Cucina Hui cinese
     */
    HUI,
    /**
     * Cucina hunanese cinese
     */
    HUNANESE,
    /**
     * Cucina ungherese
     */
    HUNGARIAN,
    /**
     * Cucina indiana
     */
    INDIAN,
    /**
     * Cucina vegetariana indiana
     */
    INDIAN_VEGETARIAN,
    /**
     * Cucina indonesiana
     */
    INDONESIAN,
    /**
     * Cucina innovativa
     */
    INNOVATIVE,
    /**
     * Cucina internazionale
     */
    INTERNATIONAL,
    /**
     * Cucina irlandese
     */
    IRISH,
    /**
     * Cucina Isan thailandese
     */
    ISAN,
    /**
     * Cucina israeliana
     */
    ISRAELI,
    /**
     * Cucina italiana
     */
    ITALIAN,
    /**
     * Cucina italo-americana
     */
    ITALIAN_AMERICAN,
    /**
     * Cucina italiana contemporanea
     */
    ITALIAN_CONTEMPORARY,
    /**
     * Izakaya – cucina giapponese informale
     */
    IZAKAYA,
    /**
     * Cucina giamaicana
     */
    JAMAICAN,
    /**
     * Cucina giapponese
     */
    JAPAN,
    /**
     * Cucina giapponese
     */
    JAPANESE,
    /**
     * Cucina giapponese contemporanea
     */
    JAPANESE_CONTEMPORARY,
    /**
     * Steakhouse giapponese
     */
    JAPANESE_STEAKHOUSE,
    /**
     * Cucina Jiangzhe cinese
     */
    JIANGZHE,
    /**
     * Jokbal – specialità coreana a base di maiale
     */
    JOKBAL,
    /**
     * Kalguksu – zuppa coreana
     */
    KALGUKSU,
    /**
     * Cucina coreana
     */
    KOREAN,
    /**
     * Cucina coreana contemporanea
     */
    KOREAN_CONTEMPORARY,
    /**
     * Kushiage – cibo fritto giapponese
     */
    KUSHIAGE,
    /**
     * Cucina Kyoto giapponese
     */
    KYOTO,
    /**
     * Specialità a base di agnello
     */
    LAMB_SPECIALITIES,
    /**
     * Cucina laotiana
     */
    LAO,
    /**
     * Cucina latino-americana
     */
    LATIN_AMERICAN,
    /**
     * Cucina libanese
     */
    LEBANESE,
    /**
     * Cucina ligure italiana
     */
    LIGURIAN,
    /**
     * Cucina lombarda italiana
     */
    LOMBARDIAN,
    /**
     * Cucina lionese francese
     */
    LYONNAISE,
    /**
     * Cucina macanese
     */
    MACANESE,
    /**
     * Cucina malese
     */
    MALAYSIAN,
    /**
     * Mandu – ravioli coreani
     */
    MANDU,
    /**
     * Mantuano – cucina italiana
     */
    MANTUAN,
    /**
     * Cucina mediterranea
     */
    MEDITERRANEAN,
    /**
     * Memil Guksu – piatto coreano
     */
    MEMIL_GUKSU,
    /**
     * Cucina messicana
     */
    MEXICAN,
    /**
     * Cucina medio-orientale
     */
    MIDDLE_EASTERN,
    /**
     * Cucina milanese italiana
     */
    MILANESE,
    /**
     * Cucina moderna
     */
    MODERN,
    /**
     * Cucina britannica
     */
    BRITISH,
    /**
     * Cucina marocchina
     */
    MOROCCAN,
    /**
     * Naengmyeon – piatto coreano
     */
    NAENGMYEON,
    /**
     * Nakagyo-ku – zona gastronomica giapponese
     */
    NAKAGYO_KU,
    /**
     * Cucina nepalese
     */
    NEPALI,
    /**
     * Cucina Ningbo cinese
     */
    NINGBO,
    /**
     * Piatti a base di noodle
     */
    NOODLES,
    /**
     * Cucina nordafricana
     */
    NORTH_AFRICAN,
    /**
     * Cucina nordamericana
     */
    NORTH_AMERICAN,
    /**
     * Cucina thailandese settentrionale
     */
    NORTHERN_THAI,
    /**
     * Cucina norvegese
     */
    NORWEGIAN,
    /**
     * Obanzai – cucina tradizionale giapponese
     */
    OBANZAI,
    /**
     * Oden – piatto giapponese
     */
    ODEN,
    /**
     * Okonomiyaki – piatto giapponese
     */
    OKONOMIYAKI,
    /**
     * Onigiri – polpette di riso giapponesi
     */
    ONIGIRI,
    /**
     * Cucina biologica
     */
    ORGANIC,
    /**
     * Specialità a base di ostriche
     */
    OYSTER_SPECIALITIES,
    /**
     * Cucina pakistana
     */
    PAKISTANI,
    /**
     * Cucina peranakan
     */
    PERANAKAN,
    /**
     * Cucina persiana
     */
    PERSIAN,
    /**
     * Cucina peruviana
     */
    PERUVIAN,
    /**
     * Cucina piemontese italiana
     */
    PIEDMONTESE,
    /**
     * Pizza italiana
     */
    PIZZA,
    /**
     * Cucina polacca
     */
    POLISH,
    /**
     * Specialità a base di maiale
     */
    PORK,
    /**
     * Cucina portoghese
     */
    PORTUGUESE,
    /**
     * Cucina provenzale francese
     */
    PROVENCAL,
    /**
     * Cucina portoricana
     */
    PUERTO_RICAN,
    /**
     * Pesce palla
     */
    PUFFERFISH,
    /**
     * Raclette – specialità svizzera
     */
    RACLETTE,
    /**
     * Ramen – piatto giapponese
     */
    RAMEN,
    /**
     * Cucina regionale generica
     */
    REGIONAL,
    /**
     * Cucina europea regionale
     */
    REGIONAL_EUROPEAN,
    /**
     * Piatti a base di riso
     */
    RICE_DISHES,
    /**
     * Cucina romana italiana
     */
    ROMAN,
    /**
     * Cucina russa
     */
    RUSSIAN,
    /**
     * Cucina sarda italiana
     */
    SARDINIAN,
    /**
     * Cucina savoiarda francese
     */
    SAVOYARD,
    /**
     * Cucina scandinava
     */
    SCANDINAVIAN,
    /**
     * Cucina scozzese
     */
    SCOTTISH,
    /**
     * Specialità di mare
     */
    SEAFOOD,
    /**
     * Cucina stagionale
     */
    SEASONAL,
    /**
     * Seolleongtang – zuppa coreana
     */
    SEOLLEONGTANG,
    /**
     * Cucina Shaanxi cinese
     */
    SHAANXI,
    /**
     * Shabu-shabu – piatto giapponese
     */
    SHABU_SHABU,
    /**
     * Cucina Shandong cinese
     */
    SHANDONG,
    /**
     * Cucina shanghainese cinese
     */
    SHANGHAINESE,
    /**
     * Cucina da condividere
     */
    SHARING,
    /**
     * Specialità di molluschi
     */
    SHELLFISH_SPECIALITIES,
    /**
     * Shojin – cucina buddhista giapponese
     */
    SHOJIN,
    /**
     * Cucina Shun Tak
     */
    SHUN_TAK,
    /**
     * Cucina Sichuan cinese
     */
    SICHUAN,
    /**
     * Cucina siciliana italiana
     */
    SICILIAN,
    /**
     * Cucina singaporiana
     */
    SINGAPOREAN,
    /**
     * Piccoli piatti
     */
    SMALL_EATS,
    /**
     * Smørrebrød – cucina danese
     */
    SMORREBRED,
    /**
     * Soba – tipo di noodle giapponesi
     */
    SOBA,
    /**
     * Cucina sudafricana
     */
    SOUTH_AFRICAN,
    /**
     * Cucina sudamericana
     */
    SOUTH_AMERICAN,
    /**
     * Cucina del sud-est asiatico
     */
    SOUTH_EAST_ASIAN,
    /**
     * Cucina del sud degli Stati Uniti
     */
    SOUTHERN,
    /**
     * Cucina thailandese meridionale
     */
    SOUTHERN_THAI,
    /**
     * Cucina del sud dell’India
     */
    SOUTH_INDIAN,
    /**
     * Cucina tirolese meridionale
     */
    SOUTH_TYROLEAN,
    /**
     * Cucina spagnola
     */
    SPANISH,
    /**
     * Cucina spagnola contemporanea
     */
    SPANISH_CONTEMPORARY,
    /**
     * Cucina dello Sri Lanka
     */
    SRI_LANKAN,
    /**
     * Steakhouse – ristorante di bistecche
     */
    STEAKHOUSE,
    /**
     * Cibo da strada
     */
    STREET,
    /**
     * Sujebi – zuppa coreana
     */
    SUJEBI,
    /**
     * Sukiyaki – piatto giapponese
     */
    SUKIYAKI,
    /**
     * Sushi – cucina giapponese
     */
    SUSHI,
    /**
     * Cucina sveva
     */
    SWABIAN,
    /**
     * Cucina svedese
     */
    SWEDISH,
    /**
     * Cucina svizzera
     */
    SWISS,
    /**
     * Cucina taiwanese
     */
    TAIWANESE,
    /**
     * Cucina taiwanese contemporanea
     */
    TAIWANESE_CONTEMPORARY,
    /**
     * Cucina Taizhou cinese
     */
    TAIZHOU,
    /**
     * Tempura – fritto giapponese
     */
    TEMPURA,
    /**
     * Cucina Teochew cinese
     */
    TEOCHEW,
    /**
     * Teppanyaki – cucina giapponese alla piastra
     */
    TEPPANYAKI,
    /**
     * Cucina Tex-Mex
     */
    TEX_MEX,
    /**
     * Cucina thailandese
     */
    THAI,
    /**
     * Cucina tibetana
     */
    TIBETAN,
    /**
     * Tonkatsu – cotoletta di maiale giapponese
     */
    TONKATSU,
    /**
     * Cucina tradizionale
     */
    TRADITIONAL,
    /**
     * Cucina turca
     */
    TURKISH,
    /**
     * Cucina toscana italiana
     */
    TUSCAN,
    /**
     * Udon – noodle giapponesi
     */
    UDON,
    /**
     * Cucina umbra italiana
     */
    UMBRIAN,
    /**
     * Unagi – anguilla giapponese
     */
    UNAGI,
    /**
     * Cucina vegana
     */
    VEGAN,
    /**
     * Cucina vegetariana
     */
    VEGETARIAN,
    /**
     * Cucina veneziana italiana
     */
    VENETIAN,
    /**
     * Cucina venezuelana
     */
    VENEZUELAN,
    /**
     * Cucina vietnamita
     */
    VIETNAMESE,
    /**
     * Cucina occidentale
     */
    WESTERN,
    /**
     * Cucina mondiale, fusion di vari stili
     */
    WORLD,
    /**
     * Cucina Xibei cinese
     */
    XIBEI,
    /**
     * Cucina Xinjiang cinese
     */
    XINJIANG,
    /**
     * Yakitori – spiedini giapponesi
     */
    YAKITORI,
    /**
     * Yoshoku – cucina giapponese occidentale
     */
    YOSHOKU,
    /**
     * Yukhoe – specialità coreana di carne cruda
     */
    YUKHOE,
    /**
     * Cucina Yunnan cinese
     */
    YUNNANESE,

    ZHOU,
    /**
     * Cucina Zhejiang cinese
     */
    ZHEJIANG;


    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
