package it.uninsubria.laboratoriob.objects;


import it.uninsubria.laboratoriob.Entity;
import it.uninsubria.laboratoriob.enums.Nation;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Classe che rappresenta una posizione geografica con nazione, città, indirizzo
 * e coordinate.
 * <p>
 * Estende {@link Entity}. Persistita nella propria tabella, referenziata
 * tramite FK
 * da {@code Restaurant} e {@code User}.
 * <p>
 *
 * @author Luca Cavagnari
 * @version 2.0
 */
@Getter
@Setter
public class Location extends Entity {

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
     * Costruttore per una nuova Location (UUID generato automaticamente).
     * <p>
     *
     * @param nation    nazione di appartenenza
     * @param city      nome della città
     * @param latitude  latitudine geografica
     * @param longitude longitudine geografica
     * @param address   indirizzo completo
     */
    public Location(Nation nation, String city, double latitude, double longitude, String address) {
        super();
        this.nation = nation;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    /**
     * Costruttore per ricostruire una Location esistente (UUID noto, es. da
     * database).
     * <p>
     *
     * @param id        UUID esistente
     * @param nation    nazione di appartenenza
     * @param city      nome della città
     * @param latitude  latitudine geografica
     * @param longitude longitudine geografica
     * @param address   indirizzo completo
     */
    public Location(UUID id, Nation nation, String city, double latitude, double longitude, String address) {
        super(id);
        this.nation = nation;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    /**
     * Restituisce la rappresentazione testuale dettagliata dell'oggetto Location.
     * <p>
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
                longitude);
    }
}
