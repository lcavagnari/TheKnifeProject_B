package it.uninsubria.laboratorioa.objects.users;

import com.fasterxml.jackson.databind.node.ArrayNode;
import it.uninsubria.laboratorioa.objects.Location;
import it.uninsubria.laboratorioa.objects.Restaurant;
import it.uninsubria.laboratorioa.objects.enums.UserRole;
import lombok.Getter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Classe che rappresenta un utente di tipo Cliente.<p>
 * Mantiene un insieme di ristoranti preferiti identificati da UUID.<p>
 * Fornisce metodi per aggiungere e rimuovere ristoranti preferiti.<p>
 * Estende la classe {@link User} aggiungendo funzionalità specifiche del cliente.<p>
 *
 * @author Luca Cavagnari
 * @version 1.0
 */
public class Client extends User {

    /**
     * Insieme degli ID dei ristoranti preferiti dal cliente.
     */
    @Getter
    private final Set<UUID> favouriteRestourants;

    /**
     * Array JSON contenente gli ID dei ristoranti preferiti, usato per la serializzazione.
     */
    private final ArrayNode favouritesArray;

    /**
     * Costruttore principale senza salt.<p>
     * Inizializza l'array JSON e l'insieme dei preferiti.<p>
     * Invoca il metodo {@link #build()} per costruire la rappresentazione JSON.<p>
     *
     * @param username    nome utente
     * @param password    password in chiaro
     * @param name        nome
     * @param lastName    cognome
     * @param location    posizione geografica
     * @param dateOfBirth data di nascita
     */
    public Client(String username, String password, String name, String lastName, Location location, LocalDate dateOfBirth) {
        super(username, password, name, lastName, location, dateOfBirth);

        this.favouritesArray = mapper.createArrayNode();
        this.favouriteRestourants = new HashSet<>();

        build();
    }

    /**
     * Costruttore con password hashata e salt.<p>
     * Inizializza l'array JSON e l'insieme dei preferiti.<p>
     * Invoca il metodo {@link #build()} per costruire la rappresentazione JSON.<p>
     *
     * @param username    nome utente
     * @param password    password hashata
     * @param salt        salt usato nell'hash
     * @param name        nome
     * @param lastName    cognome
     * @param location    posizione geografica
     * @param dateOfBirth data di nascita
     */
    public Client(String username, String password, String salt, String name, String lastName, Location location, LocalDate dateOfBirth) {
        super(username, name, lastName, location, dateOfBirth, password, salt);

        this.favouritesArray = mapper.createArrayNode();
        this.favouriteRestourants = new HashSet<>();

        build();
    }

    /**
     * Costruttore con password hashata, salt e lista di ristoranti preferiti.<p>
     * Inizializza l'array JSON.<p>
     *
     * @param username             nome utente
     * @param password             password hashata
     * @param salt                 salt usato nell'hash
     * @param name                 nome
     * @param lastName             cognome
     * @param location             posizione geografica
     * @param dateOfBirth          data di nascita
     * @param favouriteRestourants insieme di UUID dei ristoranti preferiti
     */
    public Client(String username, String password, String salt, String name, String lastName, Location location, LocalDate dateOfBirth, Set<UUID> favouriteRestourants) {
        super(username, name, lastName, location, dateOfBirth, password, salt);
        this.favouriteRestourants = favouriteRestourants;
        this.favouritesArray = mapper.createArrayNode();
    }

    /**
     * Aggiunge un ristorante ai preferiti del cliente.<p>
     * Restituisce false se il ristorante è nullo o già presente.<p>
     *
     * @param r ristorante da aggiungere
     * @return true se aggiunto correttamente, false altrimenti
     */
    public boolean addFavourite(Restaurant r) {
        return r != null && favouriteRestourants.add(r.getId());
    }

    /**
     * Rimuove un ristorante dai preferiti del cliente.<p>
     * Restituisce false se il ristorante è nullo o non presente.<p>
     *
     * @param r ristorante da rimuovere
     * @return true se rimosso correttamente, false altrimenti
     */
    public boolean removeFavourite(Restaurant r) {
        return r != null && favouriteRestourants.remove(r.getId());
    }


    /**
     * Costruisce la rappresentazione JSON dell'oggetto, sovrascrivendo quella della superclasse.<p>
     * Aggiunge il campo "role" con valore "Client" e aggiorna l'array JSON dei preferiti.<p>
     */
    @Override
    public void build() {
        super.build();

        if (favouritesArray != null) {
            favouritesArray.removeAll();
            favouriteRestourants.forEach(fav -> favouritesArray.add(fav.toString()));
        }

        jsonObject.set("favourites", favouritesArray);
    }

    /**
     * Restituisce la rappresentazione testuale dell'istanza, includendo i preferiti.<p>
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
