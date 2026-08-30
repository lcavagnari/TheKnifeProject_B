package it.uninsubria.laboratoriob.server.data.repository;

import it.uninsubria.laboratoriob.api.objects.Customer;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.api.objects.User;
import it.uninsubria.laboratoriob.server.data.dao.CustomerDAO;
import it.uninsubria.laboratoriob.server.data.dao.OwnerDAO;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository con cache in memoria per gli utenti (Customer e Owner). Incapsula CustomerDAO e OwnerDAO.
 */
public class UserRepository {

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final OwnerDAO ownerDAO = new OwnerDAO();
    private final Map<UUID, User> byId = new ConcurrentHashMap<>();
    private final Map<String, User> byName = new ConcurrentHashMap<>();

    // ── Write operations (DB + cache) ──

    /**
     * Salva un customer nel database e nella cache in memoria.
     *
     * @param c il customer da salvare
     * @return {@code true} se il salvataggio ha avuto successo, {@code false} altrimenti
     */
    public boolean saveCustomer(Customer c) {
        boolean ok = customerDAO.save(c);
        if (ok) putCache(c);
        return ok;
    }

    /**
     * Salva un owner nel database e nella cache in memoria.
     *
     * @param o l'owner da salvare
     * @return {@code true} se il salvataggio ha avuto successo, {@code false} altrimenti
     */
    public boolean saveOwner(Owner o) {
        boolean ok = ownerDAO.save(o);
        if (ok) putCache(o);
        return ok;
    }

    /**
     * Aggiorna un customer esistente nel database e nella cache in memoria.
     *
     * @param c il customer con i dati aggiornati
     * @return {@code true} se l'aggiornamento ha avuto successo, {@code false} altrimenti
     */
    public boolean updateCustomer(Customer c) {
        boolean ok = customerDAO.update(c);
        if (ok) putCache(c);
        return ok;
    }

    /**
     * Aggiorna un owner esistente nel database e nella cache in memoria.
     *
     * @param o l'owner con i dati aggiornati
     * @return {@code true} se l'aggiornamento ha avuto successo, {@code false} altrimenti
     */
    public boolean updateOwner(Owner o) {
        boolean ok = ownerDAO.update(o);
        if (ok) putCache(o);
        return ok;
    }

    /**
     * Elimina un utente (Customer o Owner) dal database e dalla cache in memoria.
     * Il tipo di utente viene determinato dall'istanza rimossa dalla cache.
     *
     * @param id l'identificativo dell'utente da eliminare
     * @return {@code true} se l'eliminazione ha avuto successo, {@code false} altrimenti
     */
    public boolean delete(UUID id) {
        User removed = byId.remove(id);
        if (removed != null) byName.remove(removed.getUsername());
        if (removed instanceof Customer) return customerDAO.delete(id);
        if (removed instanceof Owner) return ownerDAO.delete(id);
        return false;
    }

    // ── Favourite operations ──

    /**
     * Aggiunge un ristorante ai preferiti di un customer nel database e nella cache in memoria.
     *
     * @param userId       l'identificativo del customer
     * @param restaurantId l'identificativo del ristorante da aggiungere ai preferiti
     * @return {@code true} se l'operazione ha avuto successo, {@code false} altrimenti
     */
    public boolean addFavourite(UUID userId, UUID restaurantId) {
        boolean ok = customerDAO.addFavourites(userId, restaurantId);
        if (ok) {
            User u = byId.get(userId);
            if (u instanceof Customer c) c.getFavouriteRestourants().add(restaurantId);
        }
        return ok;
    }

    /**
     * Rimuove un ristorante dai preferiti di un customer nel database e nella cache in memoria.
     *
     * @param userId       l'identificativo del customer
     * @param restaurantId l'identificativo del ristorante da rimuovere dai preferiti
     * @return {@code true} se l'operazione ha avuto successo, {@code false} altrimenti
     */
    public boolean removeFavourite(UUID userId, UUID restaurantId) {
        boolean ok = customerDAO.removeFavourites(userId, restaurantId);
        if (ok) {
            User u = byId.get(userId);
            if (u instanceof Customer c) c.getFavouriteRestourants().remove(restaurantId);
        }
        return ok;
    }

    /**
     * Restituisce i ristoranti preferiti di un customer dalla cache in memoria.
     * Se il customer non e' in cache, delega al CustomerDAO.
     *
     * @param userId l'identificativo del customer
     * @return un insieme non modificabile degli ID dei ristoranti preferiti
     */
    public Set<UUID> findFavourites(UUID userId) {
        User u = byId.get(userId);
        if (u instanceof Customer c) return Set.copyOf(c.getFavouriteRestourants());
        return customerDAO.findFavourites(userId);
    }

    // ── Owned-restaurant operations ──

    /**
     * Aggiunge un ristorante alla lista di proprieta' di un owner nel database e nella cache in memoria.
     *
     * @param ownerId  l'identificativo dell'owner
     * @param restaurant il ristorante da aggiungere alla proprieta'
     * @return {@code true} se l'operazione ha avuto successo, {@code false} altrimenti
     */
    public boolean addOwnedRestaurant(UUID ownerId, Restaurant restaurant) {
        boolean ok = ownerDAO.addRestaurant(ownerId, restaurant.getId());
        if (ok) {
            User u = byId.get(ownerId);
            if (u instanceof Owner o) o.addRestaurant(restaurant);
        }
        return ok;
    }

    /**
     * Rimuove un ristorante dalla lista di proprieta' di un owner nel database e nella cache in memoria.
     *
     * @param ownerId      l'identificativo dell'owner
     * @param restaurantId l'identificativo del ristorante da rimuovere
     * @return {@code true} se l'operazione ha avuto successo, {@code false} altrimenti
     */
    public boolean removeOwnedRestaurant(UUID ownerId, UUID restaurantId) {
        boolean ok = ownerDAO.removeRestaurant(ownerId, restaurantId);
        if (ok) {
            User u = byId.get(ownerId);
            if (u instanceof Owner o) {
                Restaurant r = o.getRestaurantsById().get(restaurantId);
                if (r != null) o.removeRestaurant(r);
            }
        }
        return ok;
    }

    // ── Read operations (cache) ──

    /**
     * Cerca un utente per ID nella cache in memoria.
     *
     * @param id l'identificativo dell'utente
     * @return l'utente trovato (Customer o Owner), oppure {@code null} se non esiste
     */
    public User findById(UUID id) { return byId.get(id); }

    /**
     * Cerca un utente per nome utente nella cache in memoria.
     *
     * @param name il nome utente da cercare
     * @return l'utente trovato (Customer o Owner), oppure {@code null} se non esiste
     */
    public User findByName(String name) { return byName.get(name); }

    /**
     * Verifica se esiste un utente con il nome utente specificato nella cache in memoria.
     *
     * @param name il nome utente da verificare
     * @return {@code true} se esiste un utente con quel nome, {@code false} altrimenti
     */
    public boolean hasByName(String name) { return byName.containsKey(name); }

    /**
     * Restituisce il numero di utenti presenti nella cache in memoria.
     *
     * @return il numero totale di utenti
     */
    public long count() { return byId.size(); }

    // ── Cache-only operations (for Loader) ──

    /**
     * Inserisce un utente nella cache in memoria (per operazioni di caricamento bulk).
     *
     * @param u l'utente da inserire nella cache
     */
    public void putCache(User u) {
        byId.put(u.getId(), u);
        byName.put(u.getUsername(), u);
    }

    /**
     * Rimuove un utente dalla cache in memoria.
     *
     * @param id l'identificativo dell'utente da rimuovere
     */
    public void removeCache(UUID id) {
        User u = byId.remove(id);
        if (u != null) byName.remove(u.getUsername());
    }

    // ── DAO access (for Loader bulk load) ──

    /**
     * Carica tutti gli owner dal database in modo asincrono.
     *
     * @return un {@link CompletableFuture} contenente la lista di tutti gli owner,
     *         oppure {@code null} in caso di errore
     */
    public CompletableFuture<List<Owner>> loadAllOwnersFromDb() {
        return CompletableFuture
                .supplyAsync(ownerDAO::findAll)
                .exceptionally(ex -> {
                    System.err.println("Error occured while loading owners: " + ex.getMessage());
                    return null;
                });
    }

    /**
     * Carica tutti i customer dal database in modo asincrono.
     *
     * @return un {@link CompletableFuture} contenente la lista di tutti i customer,
     *         oppure {@code null} in caso di errore
     */
    public CompletableFuture<List<Customer>> loadAllCustomersFromDb() {
        return CompletableFuture
                .supplyAsync(customerDAO::findAll)
                .exceptionally(ex -> {
                    System.err.println("Errore caricamento proprietari: " + ex.getMessage());
                    return null;
                });
    }
}
