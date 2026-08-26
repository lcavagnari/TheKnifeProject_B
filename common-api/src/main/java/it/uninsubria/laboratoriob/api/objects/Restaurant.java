package it.uninsubria.laboratoriob.api.objects;


import it.uninsubria.laboratoriob.api.Entity;
import it.uninsubria.laboratoriob.api.Validators;
import it.uninsubria.laboratoriob.api.enums.Award;
import it.uninsubria.laboratoriob.api.enums.CuisineType;
import it.uninsubria.laboratoriob.api.enums.PriceRange;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * Rappresenta un ristorante con proprietà multiple come nome, descrizione,
 * servizi, e recensioni.
 * <p>
 * Estende {@link Entity}. Persistenza gestita da {@code RestaurantDAO}; questa
 * classe
 * è un modello dati puro senza logica di serializzazione.
 * <p>
 * Contiene insiemi di tipi di cucina, servizi, e recensioni.
 * <p>
 * Associa un {@link Owner} come proprietario e una {@link Location} geografica.
 * <p>
 *
 * @author Luke
 * @version 2.0
 */
@Getter
@Setter
public class Restaurant extends Entity {

    private static final long serialVersionUID = 1L;

    /**
     * Insieme di tipi di cucina proposti dal ristorante.
     */
    private final Set<CuisineType> cuisinesTypes;

    /**
     * Insieme di servizi offerti (es. delivery, booking).
     */
    private final Set<String> services;

    /**
     * Mappa di recensioni identificate da UUID.
     */
    private final Map<UUID, Review> reviews;

    /**
     * Descrizione del ristorante.
     */
    private String description;

    /**
     * URL del sito web del ristorante.
     */
    private String websiteUrl;

    /**
     * Numero di telefono del ristorante.
     */
    private String phone;

    /**
     * Proprietario del ristorante.
     */
    private Owner owner;

    /**
     * Posizione geografica del ristorante.
     */
    private Location location;

    /**
     * Fascia di prezzo del ristorante.
     */
    private PriceRange priceRange;

    /**
     * Flag indicante se il ristorante offre consegna a domicilio.
     */
    private boolean hasDelivery;

    /**
     * Flag indicante se il ristorante offre prenotazioni online.
     */
    private boolean hasOnlineBooking;

    /**
     * Premio assegnato al ristorante.
     */
    private Award award;

    /**
     * Flag indicante se il ristorante possiede la Stella Verde Michelin.
     */
    private boolean greenStar;

    /**
     * Nome del ristorante.
     */
    private String name;

    /**
     * Costruttore per ricostruire un ristorante esistente (UUID noto, es. da
     * database).
     * <p>
     * Gestisce valori null o vuoti con default.
     * <p>
     *
     * @param id               UUID esistente
     * @param name             nome del ristorante
     * @param description      descrizione
     * @param websiteUrl       URL sito web
     * @param owner            proprietario
     * @param phone            telefono
     * @param location         posizione geografica
     * @param priceRange       fascia di prezzo
     * @param hasDelivery      flag consegna a domicilio
     * @param hasOnlineBooking flag prenotazioni online
     * @param award            premio assegnato
     * @param greenStar        flag Stella Verde
     * @param cuisinesTypes    insieme tipi cucina
     * @param services         insieme servizi
     * @param reviews          mappa recensioni
     */
    public Restaurant(UUID id, String name, String description, String websiteUrl, Owner owner, String phone,
                      Location location, PriceRange priceRange,
                      boolean hasDelivery, boolean hasOnlineBooking, Award award, boolean greenStar,
                      Set<CuisineType> cuisinesTypes,
                      Set<String> services, Map<UUID, Review> reviews) {

        super(id);

        this.name = (name == null || name.isBlank()) ? "Restaurant" : name;
        this.description = (description == null || description.isBlank()) ? "" : description;
        this.websiteUrl = (websiteUrl == null || websiteUrl.isBlank()) ? "" : websiteUrl;
        this.phone = (phone == null || phone.isBlank()) ? "" : phone;
        this.location = location;
        this.priceRange = (priceRange == null) ? PriceRange.MODERATE : priceRange;
        this.hasDelivery = hasDelivery;
        this.hasOnlineBooking = hasOnlineBooking;
        this.award = (award == null) ? Award.NONE : award;
        this.greenStar = greenStar;

        this.owner = owner;

        this.cuisinesTypes = (cuisinesTypes == null) ? new HashSet<>() : cuisinesTypes;
        this.services = (services == null) ? new HashSet<>() : services;
        this.reviews = (reviews == null) ? new HashMap<>() : reviews;
    }

    /**
     * Costruttore per un nuovo ristorante (UUID generato automaticamente).
     * <p>
     *
     * @param name             nome del ristorante
     * @param description      descrizione
     * @param websiteUrl       URL sito web
     * @param owner            proprietario
     * @param phone            telefono
     * @param location         posizione geografica
     * @param priceRange       fascia di prezzo
     * @param hasDelivery      flag consegna a domicilio
     * @param hasOnlineBooking flag prenotazioni online
     * @param award            premio assegnato
     * @param greenStar        flag Stella Verde
     * @param cuisinesTypes    insieme tipi cucina
     * @param services         insieme servizi
     * @param reviews          mappa recensioni
     */
    public Restaurant(String name, String description, String websiteUrl, Owner owner, String phone, Location location,
                      PriceRange priceRange,
                      boolean hasDelivery, boolean hasOnlineBooking, Award award, boolean greenStar,
                      Set<CuisineType> cuisinesTypes,
                      Set<String> services, Map<UUID, Review> reviews) {

        this(UUID.randomUUID(), name, description, websiteUrl, owner, phone, location, priceRange, hasDelivery,
                hasOnlineBooking,
                award, greenStar, cuisinesTypes, services, reviews);
    }

    /**
     * Costruttore per ricostruire un ristorante esistente senza recensioni
     * precaricate.
     * <p>
     * Le recensioni possono essere popolate successivamente tramite
     * {@code ReviewDAO}.
     *
     * @param id               UUID esistente
     * @param name             nome del ristorante
     * @param description      descrizione
     * @param websiteUrl       URL sito web
     * @param owner            proprietario
     * @param phone            telefono
     * @param location         posizione geografica
     * @param priceRange       fascia di prezzo
     * @param hasDelivery      flag consegna a domicilio
     * @param hasOnlineBooking flag prenotazioni online
     * @param award            premio assegnato
     * @param greenStar        flag Stella Verde
     * @param cuisinesTypes    insieme tipi cucina
     * @param services         insieme servizi
     */
    public Restaurant(UUID id, String name, String description, String websiteUrl, Owner owner, String phone,
                      Location location, PriceRange priceRange,
                      boolean hasDelivery, boolean hasOnlineBooking, Award award, boolean greenStar,
                      Set<CuisineType> cuisinesTypes, Set<String> services) {

        this(id, name, description, websiteUrl, owner, phone, location, priceRange, hasDelivery, hasOnlineBooking,
                award, greenStar, cuisinesTypes, services, new HashMap<>());
    }

    /**
     * Aggiunge una recensione al ristorante se non è già presente.
     * <p>
     *
     * @param r recensione da aggiungere
     */
    public void addReview(Review r) {
        if (!Validators.validateReview(r) || reviews.containsKey(r.getId()))
            return;

        reviews.put(r.getId(), r);
    }

    /**
     * Rimuove una recensione dal ristorante se presente.
     * <p>
     *
     * @param r recensione da rimuovere
     */
    public void removeReview(Review r) {
        if (!Validators.validateReview(r) || !reviews.containsValue(r))
            return;
        reviews.remove(r.getId());
    }

    /**
     * Aggiunge un servizio alla collezione dei servizi offerti dal ristorante.
     * <p>
     * Il servizio viene validato tramite {@link Validators#validateString(String)} prima
     * dell'inserimento.
     * <p>
     *
     * @param service il servizio da aggiungere (es. "WiFi", "Parking")
     * @return true se il servizio è stato aggiunto con successo, false se
     * validazione fallisce o servizio già presente
     */
    public boolean addService(String service) {
        if (!Validators.validateString(service))
            return false;

        return services.add(service);
    }

    /**
     * Rimuove un servizio dalla collezione dei servizi offerti dal ristorante.
     * <p>
     * Il servizio viene validato tramite {@link Validators#validateString(String)} prima
     * della rimozione.
     * <p>
     *
     * @param service il servizio da rimuovere
     * @return true se il servizio è stato rimosso con successo, false se
     * validazione fallisce o servizio non presente
     */
    public boolean removeService(String service) {
        if (!Validators.validateString(service))
            return false;

        return services.remove(service);
    }

    /**
     * Aggiunge un tipo di cucina alla collezione delle cucine servite dal
     * ristorante.
     * <p>
     *
     * @param c il tipo di cucina da aggiungere
     * @return true se il tipo di cucina è stato aggiunto con successo, false se
     * nullo o già presente
     */
    public boolean addCuisineType(CuisineType c) {
        if (c == null)
            return false;

        return cuisinesTypes.add(c);
    }

    /**
     * Rimuove un tipo di cucina dalla collezione delle cucine servite dal
     * ristorante.
     * <p>
     *
     * @param c il tipo di cucina da rimuovere
     * @return true se il tipo di cucina è stato rimosso con successo, false se
     * nullo o non presente
     */
    public boolean removeCuisineType(CuisineType c) {
        if (c == null)
            return false;

        return cuisinesTypes.remove(c);
    }

    /**
     * Restituisce una rappresentazione stringa dettagliata del ristorante con tutti
     * i campi.
     * <p>
     *
     * @return stringa descrittiva
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("┌─────────────── Restaurant Info ────────────────┐\n");
        sb.append("│ Name:              ").append(name).append("\n");
        sb.append("│ Description:       ").append(description).append("\n");
        sb.append("│ Website:           ").append(websiteUrl).append("\n");
        sb.append("│ Phone:             ").append(phone).append("\n");
        sb.append("│ Location:          ").append(location).append("\n");
        sb.append("│ Price Range:       ").append(priceRange).append("\n");
        sb.append("│ Delivery:          ").append(hasDelivery ? "✔️" : "❌").append("\n");
        sb.append("│ Online Booking:    ").append(hasOnlineBooking ? "✔️" : "❌").append("\n");
        sb.append("│ Award:             ").append(award).append("\n");
        sb.append("│ Green Star:        ").append(greenStar ? "✔️" : "❌").append("\n");
        sb.append("│ Owner ID:          ").append(owner != null ? owner.getId() : "N/A").append("\n");

        sb.append("│ Cuisine Types:     ");
        if (cuisinesTypes.isEmpty())
            sb.append("None\n");
        else
            sb.append(cuisinesTypes).append("\n");

        sb.append("│ Services:          ");
        if (services.isEmpty())
            sb.append("None\n");
        else
            sb.append(services).append("\n");

        sb.append("│ Reviews Count:     ").append(reviews.size()).append("\n");
        if (!reviews.isEmpty()) {
            sb.append("│ Reviews IDs:       ")
                    .append(reviews.keySet().stream().map(UUID::toString).toList())
                    .append("\n");
        }

        sb.append("│ ID:                ").append(id).append("\n");
        sb.append("└───────────────────────────────────────────────┘");
        return sb.toString();
    }
}
