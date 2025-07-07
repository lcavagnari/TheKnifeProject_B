package it.uninsubria.laboratorioa.objects.users;

import it.uninsubria.laboratorioa.objects.Location;
import it.uninsubria.laboratorioa.objects.Restaurant;
import lombok.Getter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Classe che rappresenta un utente di tipo Proprietario (Owner).<p>
 * Mantiene un insieme di ristoranti di cui è proprietario.<p>
 * Fornisce metodi per aggiungere e rimuovere ristoranti.<p>
 * Estende la classe {@link User} aggiungendo funzionalità specifiche del proprietario.<p>
 *
 * Autore: Luke
 * @version 1.0
 */
@Getter
public class Owner extends User {

    /**
     * Insieme dei ristoranti posseduti dal proprietario.
     */
    private final Set<Restaurant> restaurants;

    /**
     * Costruttore con password hashata e salt.<p>
     * Inizializza l'insieme dei ristoranti.<p>
     *
     * @param username nome utente
     * @param password password hashata
     * @param salt salt usato nell'hash
     * @param name nome
     * @param lastName cognome
     * @param location posizione geografica
     * @param dateOfBirth data di nascita
     */
    public Owner(String username, String password, String salt, String name, String lastName, Location location, LocalDate dateOfBirth) {
        super(username, name, lastName, location, dateOfBirth, password, salt);

        this.restaurants = new HashSet<>();
    }

    /**
     * Costruttore con password in chiaro.<p>
     * Inizializza l'insieme dei ristoranti.<p>
     *
     * @param username nome utente
     * @param password password in chiaro
     * @param name nome
     * @param lastName cognome
     * @param location posizione geografica
     * @param dateOfBirth data di nascita
     */
    public Owner(String username, String password, String name, String lastName, Location location, LocalDate dateOfBirth) {
        super(username, password, name, lastName, location, dateOfBirth);

        this.restaurants = new HashSet<>();
    }

    /**
     * Costruttore con password hashata, salt e insieme di ristoranti.<p>
     * Se il set di ristoranti è nullo, inizializza un insieme vuoto.<p>
     *
     * @param username nome utente
     * @param password password hashata
     * @param salt salt usato nell'hash
     * @param name nome
     * @param lastName cognome
     * @param location posizione geografica
     * @param dateOfBirth data di nascita
     * @param restaurants insieme di ristoranti del proprietario
     */
    public Owner(String username, String password, String salt, String name, String lastName, Location location, LocalDate dateOfBirth, Set<Restaurant> restaurants) {
        super(username, name, lastName, location, dateOfBirth, password, salt);

        this.restaurants = (restaurants == null) ? new HashSet<>() : restaurants;
    }

    /**
     * Aggiunge un ristorante al set dei ristoranti posseduti.<p>
     * Restituisce false se il ristorante è nullo o già presente.<p>
     *
     * @param r ristorante da aggiungere
     * @return true se aggiunto correttamente, false altrimenti
     */
    public boolean addRestaurant(Restaurant r) {
        return r != null && restaurants.add(r);
    }

    /**
     * Rimuove un ristorante dal set dei ristoranti posseduti.<p>
     * Restituisce false se il ristorante è nullo o non presente.<p>
     *
     * @param r ristorante da rimuovere
     * @return true se rimosso correttamente, false altrimenti
     */
    public boolean removeRestaurant(Restaurant r) {
        return r != null && restaurants.remove(r);
    }

    /**
     * Costruisce la rappresentazione JSON dell'oggetto, sovrascrivendo quella della superclasse.<p>
     * Aggiunge il campo "role" con valore "Owner".<p>
     */
    @Override
    protected void build() {
        super.build();

        jsonObject.put("role", "Owner");
    }
}
