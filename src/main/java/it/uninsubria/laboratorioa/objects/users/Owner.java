package it.uninsubria.laboratorioa.objects.users;

import it.uninsubria.laboratorioa.objects.Location;
import it.uninsubria.laboratorioa.objects.Restaurant;
import it.uninsubria.laboratorioa.objects.enums.Award;
import it.uninsubria.laboratorioa.objects.enums.PriceRange;
import it.uninsubria.laboratorioa.objects.enums.UserRole;
import lombok.Getter;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Classe che rappresenta un utente di tipo Proprietario (Owner).<p>
 * Mantiene un insieme di ristoranti di cui è proprietario.<p>
 * Fornisce metodi per aggiungere, rimuovere e modificare ristoranti.<p>
 * Estende la classe {@link User} aggiungendo funzionalità specifiche del proprietario.<p>
 * <p>
 * Autore: Luke
 *
 * @version 1.1
 */
@Getter
public class Owner extends User {

    /**
     * Mappa dei ristoranti indicizzati per ID UUID.<p>
     * Permette accesso rapido a un ristorante dato il suo identificatore univoco.
     */
    @Getter
    private final Map<UUID, Restaurant> restaurantsById = new HashMap<>();

    /**
     * Mappa dei ristoranti indicizzati per nome.<p>
     * Permette ricerca rapida di un ristorante dato il suo nome.
     */
    @Getter
    private final Map<String, Restaurant> restaurantsByName = new HashMap<>();

    /**
     * Costruttore completo con set di ristoranti esistenti.<p>
     * Inizializza l'owner con credenziali e popola le mappe dei ristoranti.
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
    public Owner(UUID id, String username, String password, String salt, String name, String lastName, Location location, LocalDate dateOfBirth, Set<Restaurant> restaurants) {
        super(id, username, name, lastName, name, lastName, location, dateOfBirth);

        if (restaurants == null || restaurants.isEmpty()) return;
        for (Restaurant r : restaurants) {
            restaurantsById.put(r.getId(), r);
            restaurantsByName.put(r.getName(), r);
        }
    }

    /**
     * Costruttore con credenziali complete ma senza ristoranti.<p>
     * Inizializza l'owner con id esistente e password hashata.
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
    public Owner(UUID id, String username, String password, String salt, String name, String lastName, Location location, LocalDate dateOfBirth) {
        super(id, username, password, salt, name, lastName, location, dateOfBirth);
    }

    /**
     * Costruttore semplificato per nuovo owner.<p>
     * Genera automaticamente un nuovo UUID e hasha la password in chiaro.
     *
     * @param username    nome utente per il login
     * @param password    password in chiaro (verrà hashata)
     * @param name        nome del proprietario
     * @param lastName    cognome del proprietario
     * @param location    posizione geografica
     * @param dateOfBirth data di nascita
     */
    public Owner(String username, String password, String name, String lastName, Location location, LocalDate dateOfBirth) {
        super(username, password, name, lastName, location, dateOfBirth);
    }

    /**
     * Aggiunge un ristorante alle collezioni dell'owner.<p>
     * Il ristorante viene indicizzato sia per ID che per nome.
     *
     * @param r il ristorante da aggiungere
     * @return true se il ristorante è stato aggiunto con successo, false se nullo o già presente
     */
    public boolean addRestaurant(Restaurant r) {
        if (r == null || restaurantsById.containsKey(r.getId())) return false;
        restaurantsById.put(r.getId(), r);
        restaurantsByName.put(r.getName(), r);
        return true;
    }

    /**
     * Rimuove un ristorante dalle collezioni dell'owner.<p>
     * Il ristorante viene rimosso da entrambe le mappe (per ID e per nome).
     *
     * @param r il ristorante da rimuovere
     * @return true se il ristorante è stato rimosso con successo, false se nullo o non presente
     */
    public boolean removeRestaurant(Restaurant r) {
        if (r == null || !restaurantsById.containsKey(r.getId())) return false;
        restaurantsById.remove(r.getId());
        restaurantsByName.remove(r.getName());
        return true;
    }

    /**
     * Rinomina un ristorante esistente.<p>
     * Il nuovo nome deve rispettare il pattern: 4-30 caratteri alfanumerici, spazi, trattini e apostrofi.
     *
     * @param id      identificatore del ristorante da rinominare
     * @param newName nuovo nome da assegnare
     * @return true se la rinomina è avvenuta con successo, false se validazione fallisce o ristorante non trovato
     */
    public boolean renameRestaurant(UUID id, String newName) {
        if (id == null || newName == null || newName.isBlank() || !newName.matches("[\\p{L}0-9 \\-']{4,30}$") || !restaurantsById.containsKey(id))
            return false;

        Restaurant r = restaurantsById.get(id);
        if (r == null) {
            restaurantsById.remove(id);
            return false;
        }

        r.setName(newName);
        r.build();

        return true;
    }

    /**
     * Modifica la descrizione di un ristorante.<p>
     * Aggiorna la rappresentazione JSON del ristorante.
     *
     * @param r              il ristorante da modificare
     * @param newDescription nuova descrizione
     * @return true se la modifica è avvenuta con successo, false se ristorante nullo o non posseduto
     */
    public boolean modifyRestaurantDescription(Restaurant r, String newDescription) {
        if (r == null || newDescription == null || !restaurantsById.containsKey(r.getId())) return false;

        r.setDescription(newDescription);
        r.build();

        return true;
    }

    /**
     * Modifica l'URL del sito web di un ristorante.<p>
     * Aggiorna la rappresentazione JSON del ristorante.
     *
     * @param r             il ristorante da modificare
     * @param newWebsiteUrl nuovo URL del sito web
     * @return true se la modifica è avvenuta con successo, false se ristorante nullo o non posseduto
     */
    public boolean modifyRestaurantWebsite(Restaurant r, String newWebsiteUrl) {
        if (r == null || newWebsiteUrl == null || !restaurantsById.containsKey(r.getId())) return false;

        r.setWebsiteUrl(newWebsiteUrl);
        r.build();

        return true;
    }

    /**
     * Modifica il numero di telefono di un ristorante.<p>
     * Ricostruisce la rappresentazione JSON dell'owner.
     *
     * @param r        il ristorante da modificare
     * @param newPhone nuovo numero di telefono
     * @return true se la modifica è avvenuta con successo, false se ristorante nullo o non posseduto
     */
    public boolean modifyRestaurantPhone(Restaurant r, String newPhone) {
        if (r == null || newPhone == null || !restaurantsById.containsKey(r.getId())) return false;

        r.setPhone(newPhone);
        build();

        return true;
    }

    /**
     * Modifica la posizione geografica di un ristorante.<p>
     * Ricostruisce la rappresentazione JSON dell'owner.
     *
     * @param r           il ristorante da modificare
     * @param newLocation nuova posizione geografica
     * @return true se la modifica è avvenuta con successo, false se ristorante o location nulli o ristorante non posseduto
     */
    public boolean modifyRestaurantLocation(Restaurant r, Location newLocation) {
        if (r == null || newLocation == null || !restaurantsById.containsKey(r.getId())) return false;

        r.setLocation(newLocation);
        build();

        return true;
    }

    /**
     * Modifica la fascia di prezzo di un ristorante.<p>
     * Ricostruisce la rappresentazione JSON dell'owner.
     *
     * @param r             il ristorante da modificare
     * @param newPriceRange nuova fascia di prezzo
     * @return true se la modifica è avvenuta con successo, false se ristorante nullo o non posseduto
     */
    public boolean modifyRestaurantPriceRange(Restaurant r, PriceRange newPriceRange) {
        if (r == null || !restaurantsById.containsKey(r.getId())) return false;

        r.setPriceRange(newPriceRange);
        build();

        return true;
    }

    /**
     * Modifica il premio assegnato a un ristorante.<p>
     * Ricostruisce la rappresentazione JSON dell'owner.
     *
     * @param r        il ristorante da modificare
     * @param newAward nuovo premio da assegnare
     * @return true se la modifica è avvenuta con successo, false se ristorante o award nulli o ristorante non posseduto
     */
    public boolean modifyRestaurantAward(Restaurant r, Award newAward) {
        if (r == null || newAward == null || !restaurantsById.containsKey(r.getId())) return false;

        r.setAward(newAward);
        build();

        return true;
    }

    /**
     * Modifica lo stato della Stella Verde Michelin per un ristorante.<p>
     * Ricostruisce la rappresentazione JSON dell'owner.
     *
     * @param r         il ristorante da modificare
     * @param greenStar true se il ristorante ha la Stella Verde, false altrimenti
     * @return true se la modifica è avvenuta con successo, false se ristorante nullo o non posseduto
     */
    public boolean modifyRestaurantGreenStar(Restaurant r, boolean greenStar) {
        if (r == null || !restaurantsById.containsKey(r.getId())) return false;

        r.setGreenStar(greenStar);
        build();

        return true;
    }

    /**
     * Modifica la disponibilità del servizio di consegna a domicilio.<p>
     * Ricostruisce la rappresentazione JSON dell'owner.
     *
     * @param r           il ristorante da modificare
     * @param hasDelivery true se il ristorante offre consegna, false altrimenti
     * @return true se la modifica è avvenuta con successo, false se ristorante nullo o non posseduto
     */
    public boolean modifyRestaurantHasDelivery(Restaurant r, boolean hasDelivery) {
        if (r == null || !restaurantsById.containsKey(r.getId())) return false;

        r.setHasDelivery(hasDelivery);
        build();
        return true;
    }

    /**
     * Modifica la disponibilità del servizio di prenotazione online.<p>
     * Ricostruisce la rappresentazione JSON dell'owner.
     *
     * @param r          il ristorante da modificare
     * @param hasBooking true se il ristorante offre prenotazioni online, false altrimenti
     * @return true se la modifica è avvenuta con successo, false se ristorante nullo o non posseduto
     */
    public boolean modifyRestaurantHasBooking(Restaurant r, boolean hasBooking) {
        if (r == null || !restaurantsById.containsKey(r.getId())) return false;

        r.setHasOnlineBooking(hasBooking);
        build();

        return true;
    }

    /**
     * Restituisce i dettagli completi di un ristorante in formato stringa.<p>
     * Utilizza il metodo toString() del ristorante per la formattazione.
     *
     * @param r il ristorante di cui mostrare i dettagli
     * @return rappresentazione testuale del ristorante, null se ristorante nullo o non posseduto
     */
    public String showRestaurantDetails(Restaurant r) {
        if (r == null || !restaurantsById.containsKey(r.getId())) return null;
        return r.toString();
    }

    /**
     * Restituisce il ruolo dell'utente.<p>
     * Override del metodo astratto della superclasse {@link User}.
     *
     * @return {@link UserRole#OWNER}
     */
    @Override
    public UserRole getRole() {
        return UserRole.OWNER;
    }
}