package it.uninsubria.laboratoriob.api.objects;

import it.uninsubria.laboratoriob.api.enums.UserRole;
import lombok.Getter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Classe che rappresenta un utente di tipo Cliente.
 * <p>
 * Mantiene un insieme di ristoranti preferiti identificati da UUID.
 * <p>
 * Fornisce metodi per aggiungere e rimuovere ristoranti preferiti.
 * <p>
 * Estende la classe {@link User}. Persistenza gestita da {@code ClientDAO},
 * inclusa
 * la tabella di join per i preferiti.
 * <p>
 *
 * @author Luca Cavagnari
 * @version 2.0
 */
public class Customer extends User {

    private static final long serialVersionUID = 1L;

    /**
     * Insieme degli ID dei ristoranti preferiti dal cliente.
     */
    @Getter
    private final Set<UUID> favouriteRestourants;

    /**
     * Costruttore con password hashata e salt (UUID esistente, es. da database).
     * <p>
     *
     * @param id          identificativo
     * @param username    nome utente
     * @param password    password hashata
     * @param salt        salt usato nell'hash
     * @param name        nome
     * @param lastName    cognome
     * @param location    posizione geografica
     * @param dateOfBirth data di nascita
     */
    public Customer(UUID id, String username, String password, String salt, String name, String lastName,
                    Location location, LocalDate dateOfBirth) {
        this(id, username, password, salt, name, lastName, location, dateOfBirth, new HashSet<>(), false);
    }

    /**
     * Costruttore con password hashata, salt e insieme di ristoranti preferiti.
     * <p>
     *
     * @param id                   identificativo
     * @param username             nome utente
     * @param password             password hashata
     * @param salt                 salt usato nell'hash
     * @param name                 nome
     * @param lastName             cognome
     * @param location             posizione geografica
     * @param dateOfBirth          data di nascita
     * @param favouriteRestourants insieme di UUID dei ristoranti preferiti
     */
    public Customer(UUID id, String username, String password, String salt, String name, String lastName,
                    Location location, LocalDate dateOfBirth, Set<UUID> favouriteRestourants) {
        this(id, username, password, salt, name, lastName, location, dateOfBirth, favouriteRestourants, false);
    }

    /**
     * Costruttore completo con flag system.
     *
     * @param id identificatore univoco
     * @param username nome utente
     * @param password password hashata
     * @param salt salt per l'hashing
     * @param name nome
     * @param lastName cognome
     * @param location posizione
     * @param dateOfBirth data di nascita
     * @param favouriteRestourants ristoranti preferiti
     * @param system true se utente di sistema
     */
    public Customer(UUID id, String username, String password, String salt, String name, String lastName,
                    Location location, LocalDate dateOfBirth, Set<UUID> favouriteRestourants, boolean system) {
        super(id, username, password, salt, name, lastName, location, dateOfBirth, system);
        this.favouriteRestourants = (favouriteRestourants != null) ? favouriteRestourants : new HashSet<>();
    }

    /**
     * Costruttore per registrazione senza ID pre-esistente.
     *
     * @param username nome utente
     * @param passwordHash hash della password
     * @param passwordSalt salt della password
     * @param firstName nome
     * @param lastName cognome
     * @param location posizione
     * @param dateOfBirth data di nascita
     */
    public Customer(String username, String passwordHash, String passwordSalt, String firstName, String lastName, Location location, LocalDate dateOfBirth) {
        super(username, passwordHash, passwordSalt, firstName, lastName, location, dateOfBirth);
        this.favouriteRestourants = new HashSet<>();
    }

    /**
     * Aggiunge un ristorante ai preferiti del cliente.
     * <p>
     * Restituisce false se il ristorante è nullo o già presente.
     * <p>
     *
     * @param r ristorante da aggiungere
     * @return true se aggiunto correttamente, false altrimenti
     */
    public boolean addFavourite(Restaurant r) {
        return r != null && favouriteRestourants.add(r.getId());
    }

    /**
     * Rimuove un ristorante dai preferiti del cliente.
     * <p>
     * Restituisce false se il ristorante è nullo o non presente.
     * <p>
     *
     * @param r ristorante da rimuovere
     * @return true se rimosso correttamente, false altrimenti
     */
    public boolean removeFavourite(Restaurant r) {
        return r != null && favouriteRestourants.remove(r.getId());
    }

    /**
     * Restituisce la rappresentazione testuale dell'istanza, includendo i
     * preferiti.
     * <p>
     *
     * @return stringa descrittiva dell'oggetto Client
     */
    @Override
    public String toString() {
        return "Client{" +
                super.toString() +
                ", favourites=" + favouriteRestourants +
                '}';
    }

    @Override
    public UserRole getRole() {
        return UserRole.CLIENT;
    }
}
