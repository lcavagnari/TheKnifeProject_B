package it.uninsubria.laboratorioa.objects;

import it.uninsubria.laboratorioa.objects.enums.Nation;
import it.uninsubria.laboratorioa.ui.IO;
import lombok.Getter;
import lombok.Setter;

/**
 * Classe che rappresenta una posizione geografica con nazione, città, indirizzo e coordinate.<p>
 * Estende {@link JsonEntity} per serializzazione JSON e salvataggio.<p>
 * Contiene latitudine e longitudine per la localizzazione precisa.<p>
 * <p>
 *
 * @author Luca Cavagnari
 * @version 1.0
 */
@Getter
@Setter
public class Location extends JsonEntity {

    /**
     * Nazione di appartenenza.
     */
    private Nation nation;

    /**
     * Nome della città.
     */
    private String city;

    /**
     * Indirizzo completo.
     */
    private String address;

    /**
     * Latitudine geografica.
     */
    private double latitude;

    /**
     * Longitudine geografica.
     */
    private double longitude;

    /**
     * Costruttore completo con tutti i campi.<p>
     * Rimuove il campo "id" da jsonObject prima di ricostruire la rappresentazione JSON.<p>
     *
     * @param nation    nazione di appartenenza
     * @param city      nome della città
     * @param latitude  latitudine geografica
     * @param longitude longitudine geografica
     * @param address   indirizzo completo
     */
    public Location(Nation nation, String city, double latitude, double longitude, String address) {
        this.nation = nation;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;

        this.jsonObject.remove("id");

        //IO.validateLocation(this);
        build();
    }

    /**
     * Costruisce la rappresentazione JSON aggiornata dell'oggetto Location.<p>
     * Popola i campi nation, city, address, latitude e longitude nel jsonObject.<p>
     */
    @Override
    protected void build() {
        this.jsonObject.put("nation", String.valueOf(nation))
                .put("city", city)
                .put("address", address)
                .put("latitude", latitude)
                .put("longitude", longitude);
    }

    /**
     * Restituisce la rappresentazione testuale dettagliata dell'oggetto Location.<p>
     *
     * @return stringa contenente tutti i campi
     */
    @Override
    public String toString() {
        return String.format(
                "%s, %s (%s) [lat=%.5f, lon=%.5f]",
                address,
                city,
                nation.name().replace("_", " "),
                latitude,
                longitude
        );
    }

    /**
     * Override del metodo save per Location.<p>
     * Richiama build() e restituisce sempre true (salvataggio simulato).<p>
     *
     * @return true sempre
     */
    @Override
    public boolean save() {
        build();
        return true;
    }
}
