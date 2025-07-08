package it.uninsubria.laboratorioa.objects;

import com.fasterxml.jackson.databind.node.ArrayNode;
import it.uninsubria.laboratorioa.objects.enums.Award;
import it.uninsubria.laboratorioa.objects.enums.CuisineType;
import it.uninsubria.laboratorioa.objects.enums.PriceRange;
import it.uninsubria.laboratorioa.objects.users.Owner;
import it.uninsubria.laboratorioa.ui.IO;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * Rappresenta un ristorante con proprietà multiple come nome, descrizione, servizi, e recensioni.<p>
 * Estende {@link JsonEntity} per la gestione JSON e persistenza.<p>
 * Contiene insiemi di tipi di cucina, servizi, e recensioni.<p>
 * Associa un {@link Owner} come proprietario e una {@link Location} geografica.<p>
 * Gestisce la serializzazione automatica in JSON tramite arrayNode.<p>
 *
 * @author Luke
 * @version 1.0
 */
@Getter
@Setter
public class Restaurant extends JsonEntity {

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
     * Array JSON dei tipi di cucina per la serializzazione.
     */
    @Getter(AccessLevel.NONE)
    private final ArrayNode cuisinesTypesArray;

    /**
     * Array JSON delle recensioni per la serializzazione.
     */
    @Getter(AccessLevel.NONE)
    private final ArrayNode reviewsArray;

    /**
     * Array JSON dei servizi per la serializzazione.
     */
    @Getter(AccessLevel.NONE)
    private final ArrayNode servicesArray;

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
     * Costruttore completo per inizializzare tutte le proprietà.<p>
     * Gestisce valori null o vuoti con default.<p>
     * Inizializza e popola gli array JSON per serializzazione.<p>
     * Costruisce la rappresentazione JSON chiamando {@link #build()}.
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
     * @param reviews          mappa recensioni
     * @param services         insieme servizi
     */
    public Restaurant(String name, String description, String websiteUrl, Owner owner, String phone, Location location, PriceRange priceRange,
                      boolean hasDelivery, boolean hasOnlineBooking, Award award, boolean greenStar, Set<CuisineType> cuisinesTypes,
                      Map<UUID, Review> reviews, Set<String> services) {

        super("restaurants");

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

        this.reviewsArray = mapper.createArrayNode();
        this.cuisinesTypesArray = mapper.createArrayNode();
        this.servicesArray = mapper.createArrayNode();

        this.cuisinesTypes.forEach(c -> cuisinesTypesArray.add(c.toString()));
        this.services.forEach(servicesArray::add);
        this.reviews.forEach((u, r) -> reviewsArray.add(r.jsonObject));

        build();
    }

    /**
     * Costruttore semplificato senza recensioni.<p>
     * Inizializza la mappa recensioni vuota.<p>
     * Non costruisce la rappresentazione JSON automaticamente.
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
     */
    public Restaurant(String name, String description, String websiteUrl, Owner owner, String phone, Location location, PriceRange priceRange,
                      boolean hasDelivery, boolean hasOnlineBooking, Award award, boolean greenStar, Set<CuisineType> cuisinesTypes, Set<String> services) {

        super("restaurants");
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
        this.reviews = new HashMap<>();

        this.reviewsArray = mapper.createArrayNode();
        this.cuisinesTypesArray = mapper.createArrayNode();
        this.servicesArray = mapper.createArrayNode();

        IO.validateRestaurant(this);
    }


    /**
     * Ricostruisce l'oggetto JSON del ristorante con tutti i dati aggiornati.<p>
     * Imposta proprietà base, location, tipi di cucina, servizi e recensioni.<p>
     */
    @Override
    public void build() {
        jsonObject.put("owner", owner.getId().toString())
                .put("name", name)
                .put("address", description)
                .put("phone", phone)
                .put("priceRange", priceRange.getSymbol())
                .put("award", award.getValue())
                .put("greenStar", greenStar);

        if (location != null) jsonObject.set("location", location.jsonObject);

        cuisinesTypesArray.removeAll();
        cuisinesTypes.forEach(c -> cuisinesTypesArray.add(c.toString()));
        jsonObject.set("cuisinesTypes", cuisinesTypesArray);

        servicesArray.removeAll();
        services.forEach(servicesArray::add);
        jsonObject.set("services", servicesArray);

        reviewsArray.removeAll();
        reviews.forEach((u, r) -> reviewsArray.add(r.jsonObject));
        jsonObject.set("reviews", reviewsArray);
    }

    /**
     * Aggiunge una recensione al ristorante se non è già presente.<p>
     * Aggiunge la recensione anche all'array JSON.<p>
     *
     * @param r recensione da aggiungere
     */
    public void addReview(Review r) {
        if (!IO.validateReview(r) || !reviews.containsKey(r.id)) return;

        reviews.put(r.id, r);
        reviewsArray.add(r.jsonObject);
    }

    /**
     * Rimuove una recensione dal ristorante se presente.<p>
     * Aggiorna l'array JSON delle recensioni e ricostruisce l'oggetto JSON.<p>
     *
     * @param r recensione da rimuovere
     */
    public void removeReview(Review r) {
        if (!IO.validateReview(r) || !reviews.containsValue(r)) return;
        reviews.remove(r.id);
        reviewsArray.removeAll();
        reviews.values().forEach(rv -> reviewsArray.add(rv.jsonObject));
        build();
    }

    // Service collection mutations
    public boolean addService(String service) {
        if (!IO.validateString(service)) return false;

        boolean added = services.add(service);
        if (added) build();

        return added;
    }

    public boolean removeService(String service) {
        if (!IO.validateString(service)) return false;

        boolean removed = services.remove(service);
        if (removed) build();

        return removed;
    }

    public boolean addCuisineType(CuisineType c) {
        if (c == null) return false;

        boolean added = cuisinesTypes.add(c);
        if (added) build();

        return added;
    }

    public boolean removeCuisineType(CuisineType c) {
        if (c == null) return false;

        boolean removed = cuisinesTypes.remove(c);
        if (removed) build();

        return removed;
    }

    /**
     * Confronta due oggetti Restaurant per uguaglianza basandosi su tutti i campi.<p>
     *
     * @param o altro oggetto da confrontare
     * @return true se equivalenti, false altrimenti
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Restaurant that)) return false;
        if (!super.equals(o)) return false;
        return greenStar == that.greenStar && hasDelivery == that.hasDelivery && hasOnlineBooking == that.hasOnlineBooking && award == that.award && Objects.equals(name, that.name) && Objects.equals(description, that.description) && Objects.equals(websiteUrl, that.websiteUrl) && Objects.equals(phone, that.phone) && Objects.equals(location, that.location) && priceRange == that.priceRange && Objects.equals(cuisinesTypes, that.cuisinesTypes) && Objects.equals(services, that.services) && Objects.equals(reviews, that.reviews);
    }

    /**
     * Calcola hashcode combinando tutti i campi rilevanti.<p>
     *
     * @return hashcode intero
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, description, websiteUrl, phone, location, priceRange, hasDelivery, hasOnlineBooking, award, greenStar, cuisinesTypes, services, reviews);
    }

    /**
     * Restituisce una rappresentazione stringa dettagliata del ristorante con tutti i campi.<p>
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
        if (cuisinesTypes.isEmpty()) sb.append("None\n");
        else sb.append(cuisinesTypes).append("\n");

        sb.append("│ Services:          ");
        if (services.isEmpty()) sb.append("None\n");
        else sb.append(services).append("\n");

        sb.append("│ Reviews Count:     ").append(reviews.size()).append("\n");
        if (!reviews.isEmpty()) {
            sb.append("│ Reviews IDs:       ")
                    .append(reviews.keySet().stream().map(UUID::toString).toList())
                    .append("\n");
        }

        sb.append("│ ID:                ").append(id).append("\n");
        sb.append("│ SaveFile:          ").append(saveFile).append("\n");
        sb.append("└───────────────────────────────────────────────┘");
        return sb.toString();
    }
}
