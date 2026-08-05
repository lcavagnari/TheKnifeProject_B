package it.uninsubria.laboratoriob.objects;

import it.uninsubria.laboratoriob.enums.Award;
import it.uninsubria.laboratoriob.enums.PriceRange;
import it.uninsubria.laboratoriob.enums.UserRole;
import lombok.Getter;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Classe che rappresenta un utente di tipo Proprietario (Owner).
 * <p>
 * Mantiene un insieme di ristoranti di cui è proprietario.
 * <p>
 * Fornisce metodi per aggiungere, rimuovere e modificare ristoranti.
 * <p>
 * Estende la classe {@link User}. Persistenza gestita da {@code OwnerDAO}; le
 * modifiche
 * ai ristoranti mutano solo lo stato in memoria — la persistenza va richiamata
 * esplicitamente
 * tramite {@code RestaurantDAO} da chi possiede la transazione.
 * <p>
 * <p>
 * Autore: Luke
 *
 * @version 2.0
 */
@Getter
public class Owner extends User {

    /**
     * Mappa dei ristoranti indicizzati per ID UUID.
     * <p>
     */
    @Getter
    private final Map<UUID, Restaurant> restaurantsById = new HashMap<>();

    /**
     * Mappa dei ristoranti indicizzati per nome.
     * <p>
     */
    @Getter
    private final Map<String, Restaurant> restaurantsByName = new HashMap<>();

    /**
     * Costruttore completo con set di ristoranti esistenti (UUID esistente, es. da
     * database).
     * <p>
     *
     * @param id          identificatore univoco dell'owner
     * @param username    nome utente per il login
     * @param password    password hashata
     * @param salt        salt utilizzato per l'hashing
     * @param name        nome del proprietario
     * @param lastName    cognome del proprietario
     * @param location    posizione geografica
     * @param dateOfBirth data di nascita
     * @param restaurants set di ristoranti da associare all'owner
     */
    public Owner(UUID id, String username, String password, String salt, String name, String lastName,
                 Location location, LocalDate dateOfBirth, Set<Restaurant> restaurants) {
        super(id, username, password, salt, name, lastName, location, dateOfBirth);

        if (restaurants == null || restaurants.isEmpty())
            return;
        for (Restaurant r : restaurants) {
            restaurantsById.put(r.getId(), r);
            restaurantsByName.put(r.getName(), r);
        }
    }

    /**
     * Costruttore con credenziali complete ma senza ristoranti (UUID esistente).
     * <p>
     *
     * @param id          identificatore univoco dell'owner
     * @param username    nome utente per il login
     * @param password    password hashata
     * @param salt        salt utilizzato per l'hashing
     * @param name        nome del proprietario
     * @param lastName    cognome del proprietario
     * @param location    posizione geografica
     * @param dateOfBirth data di nascita
     */
    public Owner(UUID id, String username, String password, String salt, String name, String lastName,
                 Location location, LocalDate dateOfBirth) {
        super(id, username, password, salt, name, lastName, location, dateOfBirth);
    }

    public Owner(String username, String passwordHash, String passwordSalt, String firstName, String lastName, Location location, LocalDate dateOfBirth) {
        super(username, passwordHash, passwordSalt, firstName, lastName, location, dateOfBirth);
    }


    /**
     * Aggiunge un ristorante alle collezioni dell'owner.
     * <p>
     *
     * @param r il ristorante da aggiungere
     * @return true se il ristorante è stato aggiunto con successo, false se nullo o
     * già presente
     */
    public boolean addRestaurant(Restaurant r) {
        if (r == null || restaurantsById.containsKey(r.getId()))
            return false;
        restaurantsById.put(r.getId(), r);
        restaurantsByName.put(r.getName(), r);
        return true;
    }

    /**
     * Rimuove un ristorante dalle collezioni dell'owner.
     * <p>
     *
     * @param r il ristorante da rimuovere
     * @return true se il ristorante è stato rimosso con successo, false se nullo o
     * non presente
     */
    public boolean removeRestaurant(Restaurant r) {
        if (r == null || !restaurantsById.containsKey(r.getId()))
            return false;
        restaurantsById.remove(r.getId());
        restaurantsByName.remove(r.getName());
        return true;
    }

    /**
     * Rinomina un ristorante esistente.
     * <p>
     * Il nuovo nome deve rispettare il pattern: 4-30 caratteri alfanumerici, spazi,
     * trattini e apostrofi.
     *
     * @param id      identificatore del ristorante da rinominare
     * @param newName nuovo nome da assegnare
     * @return true se la rinomina è avvenuta con successo, false se validazione
     * fallisce o ristorante non trovato
     */
    public boolean renameRestaurant(UUID id, String newName) {
        if (id == null || newName == null || newName.isBlank() || !newName.matches("[\\p{L}0-9 \\-']{4,30}$")
                || !restaurantsById.containsKey(id))
            return false;

        Restaurant r = restaurantsById.get(id);
        if (r == null) {
            return false;
        }

        restaurantsByName.remove(r.getName());
        r.setName(newName);
        restaurantsByName.put(newName, r);

        return true;
    }

    /**
     * Modifica la descrizione di un ristorante.
     * <p>
     *
     * @param r              il ristorante da modificare
     * @param newDescription nuova descrizione
     * @return true se la modifica è avvenuta con successo, false se ristorante
     * nullo o non posseduto
     */
    public boolean modifyRestaurantDescription(Restaurant r, String newDescription) {
        if (r == null || newDescription == null || !restaurantsById.containsKey(r.getId()))
            return false;

        r.setDescription(newDescription);

        return true;
    }

    /**
     * Modifica l'URL del sito web di un ristorante.
     * <p>
     *
     * @param r             il ristorante da modificare
     * @param newWebsiteUrl nuovo URL del sito web
     * @return true se la modifica è avvenuta con successo, false se ristorante
     * nullo o non posseduto
     */
    public boolean modifyRestaurantWebsite(Restaurant r, String newWebsiteUrl) {
        if (r == null || newWebsiteUrl == null || !restaurantsById.containsKey(r.getId()))
            return false;

        r.setWebsiteUrl(newWebsiteUrl);

        return true;
    }

    /**
     * Modifica il numero di telefono di un ristorante.
     * <p>
     *
     * @param r        il ristorante da modificare
     * @param newPhone nuovo numero di telefono
     * @return true se la modifica è avvenuta con successo, false se ristorante
     * nullo o non posseduto
     */
    public boolean modifyRestaurantPhone(Restaurant r, String newPhone) {
        if (r == null || newPhone == null || !restaurantsById.containsKey(r.getId()))
            return false;

        r.setPhone(newPhone);

        return true;
    }

    /**
     * Modifica la posizione geografica di un ristorante.
     * <p>
     *
     * @param r           il ristorante da modificare
     * @param newLocation nuova posizione geografica
     * @return true se la modifica è avvenuta con successo, false se ristorante o
     * location nulli o ristorante non posseduto
     */
    public boolean modifyRestaurantLocation(Restaurant r, Location newLocation) {
        if (r == null || newLocation == null || !restaurantsById.containsKey(r.getId()))
            return false;

        r.setLocation(newLocation);

        return true;
    }

    /**
     * Modifica la fascia di prezzo di un ristorante.
     * <p>
     *
     * @param r             il ristorante da modificare
     * @param newPriceRange nuova fascia di prezzo
     * @return true se la modifica è avvenuta con successo, false se ristorante
     * nullo o non posseduto
     */
    public boolean modifyRestaurantPriceRange(Restaurant r, PriceRange newPriceRange) {
        if (r == null || !restaurantsById.containsKey(r.getId()))
            return false;

        r.setPriceRange(newPriceRange);

        return true;
    }

    /**
     * Modifica il premio assegnato a un ristorante.
     * <p>
     *
     * @param r        il ristorante da modificare
     * @param newAward nuovo premio da assegnare
     * @return true se la modifica è avvenuta con successo, false se ristorante o
     * award nulli o ristorante non posseduto
     */
    public boolean modifyRestaurantAward(Restaurant r, Award newAward) {
        if (r == null || newAward == null || !restaurantsById.containsKey(r.getId()))
            return false;

        r.setAward(newAward);

        return true;
    }

    /**
     * Modifica lo stato della Stella Verde Michelin per un ristorante.
     * <p>
     *
     * @param r         il ristorante da modificare
     * @param greenStar true se il ristorante ha la Stella Verde, false altrimenti
     * @return true se la modifica è avvenuta con successo, false se ristorante
     * nullo o non posseduto
     */
    public boolean modifyRestaurantGreenStar(Restaurant r, boolean greenStar) {
        if (r == null || !restaurantsById.containsKey(r.getId()))
            return false;

        r.setGreenStar(greenStar);

        return true;
    }

    /**
     * Modifica la disponibilità del servizio di consegna a domicilio.
     * <p>
     *
     * @param r           il ristorante da modificare
     * @param hasDelivery true se il ristorante offre consegna, false altrimenti
     * @return true se la modifica è avvenuta con successo, false se ristorante
     * nullo o non posseduto
     */
    public boolean modifyRestaurantHasDelivery(Restaurant r, boolean hasDelivery) {
        if (r == null || !restaurantsById.containsKey(r.getId()))
            return false;

        r.setHasDelivery(hasDelivery);
        return true;
    }

    /**
     * Modifica la disponibilità del servizio di prenotazione online.
     * <p>
     *
     * @param r          il ristorante da modificare
     * @param hasBooking true se il ristorante offre prenotazioni online, false
     *                   altrimenti
     * @return true se la modifica è avvenuta con successo, false se ristorante
     * nullo o non posseduto
     */
    public boolean modifyRestaurantHasBooking(Restaurant r, boolean hasBooking) {
        if (r == null || !restaurantsById.containsKey(r.getId()))
            return false;

        r.setHasOnlineBooking(hasBooking);

        return true;
    }

    /**
     * Restituisce i dettagli completi di un ristorante in formato stringa.
     * <p>
     *
     * @param r il ristorante di cui mostrare i dettagli
     * @return rappresentazione testuale del ristorante, null se ristorante nullo o
     * non posseduto
     */
    public String showRestaurantDetails(Restaurant r) {
        if (r == null || !restaurantsById.containsKey(r.getId()))
            return null;
        return r.toString();
    }

    /**
     * Restituisce il ruolo dell'utente.
     * <p>
     *
     * @return {@link UserRole#OWNER}
     */
    @Override
    public UserRole getRole() {
        return UserRole.OWNER;
    }
}
