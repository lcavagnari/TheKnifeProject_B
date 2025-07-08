package it.uninsubria.laboratorioa.objects.enums;

import lombok.Getter;

/**
 * Enum che rappresenta le Nazioni supportate dal sistema.
 * Ogni nazione è associata a un codice ISO 3166-1 alpha-2.
 * <p>
 *
 * @author Luca Cavagnari
 * @version 1.0
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
    SVIZZERA("CH"),
    /**
     * Germania
     */
    GERMANIA("DE"),
    /**
     * Francia
     */
    FRANCIA("FR"),
    /**
     * Spagna
     */
    SPAGNA("ES"),
    /**
     * Portogallo
     */
    PORTOGALLO("PT"),
    /**
     * Italia
     */
    ITALIA("IT"),
    /**
     * Regno Unito
     */
    REGNO_UNITO("GB"),
    /**
     * Stati Uniti d'America
     */
    STATI_UNITI("US"),
    /**
     * Brasile
     */
    BRASILE("BR"),
    /**
     * Argentina
     */
    ARGENTINA("AR"),
    /**
     * Messico
     */
    MESSICO("MX"),
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
    PORTO_RICO("PR"),
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
    GIAPPONE("JP"),
    /**
     * Corea (generico)
     */
    COREA("KR"),
    /**
     * Cina
     */
    CINA("CN"),
    /**
     * Thailandia
     */
    THAILANDIA("TH"),
    /**
     * Vietnam
     */
    VIETNAM("VN"),
    /**
     * Malesia
     */
    MALESIA("MY"),
    /**
     * Indonesia
     */
    INDONESIA("ID"),
    /**
     * Filippine
     */
    FILIPPINE("PH"),
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
    CAMBOGIA("KH"),
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
    BELGIO("BE"),
    /**
     * Canada
     */
    CANADA("CA"),
    /**
     * Cina continentale
     */
    CINA_CONTINENTALE("CN"),
    /**
     * Croazia
     */
    CROAZIA("HR"),
    /**
     * Repubblica Ceca (nome breve)
     */
    CECHIA("CZ"),
    /**
     * Repubblica Ceca (nome completo)
     */
    REPUBBLICA_CECA("CZ"),
    /**
     * Danimarca
     */
    DANIMARCA("DK"),
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
    FINLANDIA("FI"),
    /**
     * Grecia
     */
    GRECIA("GR"),
    /**
     * Hong Kong
     */
    HONG_KONG("HK"),
    /**
     * Hong Kong SAR (versione estesa)
     */
    HONG_KONG_SAR_CINA("HK"),
    /**
     * Ungheria
     */
    UNGHERIA("HU"),
    /**
     * Islanda
     */
    ISLANDA("IS"),
    /**
     * Irlanda
     */
    IRLANDA("IE"),
    /**
     * Lettonia
     */
    LETTONIA("LV"),
    /**
     * Lituania
     */
    LITUANIA("LT"),
    /**
     * Lussemburgo
     */
    LUSSEMBURGO("LU"),
    /**
     * Macao
     */
    MACAO("MO"),
    /**
     * Malta
     */
    MALTA("MT"),
    /**
     * Paesi Bassi
     */
    PAESI_BASSI("NL"),
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
    COREA_DEL_SUD("KR"),
    /**
     * Svezia
     */
    SVEZIA("SE"),
    /**
     * Taiwan
     */
    TAIWAN("TW"),
    /**
     * Turchia
     */
    TURCHIA("TR"),
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
    ISRAELE("IL"),
    /**
     * Libano
     */
    LIBANO("LB"),
    /**
     * Russia
     */
    RUSSIA("RU"),
    /**
     * Polonia
     */
    POLONIA("PL"),
    /**
     * Norvegia
     */
    NORVEGIA("NO"),
    /**
     * Scandinavia (approssimato come Svezia)
     */
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
