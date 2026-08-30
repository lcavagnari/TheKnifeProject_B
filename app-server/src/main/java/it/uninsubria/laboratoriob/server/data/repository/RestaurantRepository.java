package it.uninsubria.laboratoriob.server.data.repository;

import it.uninsubria.laboratoriob.api.enums.CuisineType;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.server.data.dao.RestaurantDAO;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository con cache in memoria per l'entita' Restaurant. Incapsula RestaurantDAO.
 */
public class RestaurantRepository {

    private final RestaurantDAO dao = new RestaurantDAO();
    private final Map<UUID, Restaurant> byId = new ConcurrentHashMap<>();
    private final Map<String, Restaurant> byName = new ConcurrentHashMap<>();

    // ── Write operations (DB + cache) ──

    /**
     * Salva un ristorante nel database e nella cache in memoria.
     *
     * @param r il ristorante da salvare
     * @return {@code true} se il salvataggio ha avuto successo, {@code false} altrimenti
     */
    public boolean save(Restaurant r) {
        boolean ok = dao.save(r);
        if (ok) putCache(r);

        return ok;
    }

    /**
     * Aggiorna un ristorante esistente nel database e nella cache in memoria.
     *
     * @param r il ristorante con i dati aggiornati
     * @return {@code true} se l'aggiornamento ha avuto successo, {@code false} altrimenti
     */
    public boolean update(Restaurant r) {
        boolean ok = dao.update(r);
        if (ok) {
            byId.put(r.getId(), r);
            byName.put(r.getName(), r);
        }

        return ok;
    }

    /**
     * Elimina un ristorante dal database e dalla cache in memoria.
     *
     * @param id l'identificativo del ristorante da eliminare
     * @return {@code true} se l'eliminazione ha avuto successo, {@code false} altrimenti
     */
    public boolean delete(UUID id) {
        Restaurant removed = byId.remove(id);
        if (removed != null) byName.remove(removed.getName());
        return dao.delete(id);
    }

    /**
     * Aggiorna i tipi di cucina di un ristorante nel database e nella cache in memoria.
     *
     * @param restaurantId l'identificativo del ristorante
     * @param cuisines     l'insieme dei nuovi tipi di cucina
     * @return {@code true} se l'aggiornamento ha avuto successo, {@code false} altrimenti
     */
    public boolean updateCuisines(UUID restaurantId, Set<CuisineType> cuisines) {
        boolean ok = dao.updateCuisines(restaurantId, cuisines);
        if (ok) {
            Restaurant r = byId.get(restaurantId);
            if (r != null) {
                r.getCuisinesTypes().clear();
                r.getCuisinesTypes().addAll(cuisines);
            }
        }

        return ok;
    }

    /**
     * Aggiorna i servizi offerti da un ristorante nel database e nella cache in memoria.
     *
     * @param restaurantId l'identificativo del ristorante
     * @param services     l'insieme dei nuovi servizi
     * @return {@code true} se l'aggiornamento ha avuto successo, {@code false} altrimenti
     */
    public boolean updateServices(UUID restaurantId, Set<String> services) {
        boolean ok = dao.updateServices(restaurantId, services);
        if (ok) {
            Restaurant r = byId.get(restaurantId);
            if (r != null) {
                r.getServices().clear();
                r.getServices().addAll(services);
            }
        }
        return ok;
    }

    // ── Read operations (cache) ──

    /**
     * Cerca un ristorante per ID nella cache in memoria.
     *
     * @param id l'identificativo del ristorante
     * @return il ristorante trovato, oppure {@code null} se non esiste
     */
    public Restaurant findById(UUID id) { return byId.get(id); }

    /**
     * Cerca un ristorante per nome nella cache in memoria.
     *
     * @param name il nome del ristorante
     * @return il ristorante trovato, oppure {@code null} se non esiste
     */
    public Restaurant findByName(String name) { return byName.get(name); }

    /**
     * Verifica se esiste un ristorante con il nome specificato nella cache in memoria.
     *
     * @param name il nome da verificare
     * @return {@code true} se esiste un ristorante con quel nome, {@code false} altrimenti
     */
    public boolean hasByName(String name) { return byName.containsKey(name); }

    /**
     * Restituisce il numero di ristoranti presenti nella cache in memoria.
     *
     * @return il numero totale di ristoranti
     */
    public long count() { return byId.size(); }

    /**
     * Restituisce tutti i ristoranti presenti nella cache in memoria come collezione non modificabile.
     *
     * @return una collezione non modificabile di tutti i ristoranti
     */
    public Collection<Restaurant> findAll() { return Collections.unmodifiableCollection(byId.values()); }

    /**
     * Cerca tutti i ristoranti posseduti da un determinato proprietario nella cache in memoria.
     *
     * @param ownerId l'identificativo del proprietario
     * @return una lista di ristoranti posseduti dal proprietario specificato
     */
    public List<Restaurant> findByOwner(UUID ownerId) {
        return byId.values().stream()
                .filter(r -> r.getOwner() != null && r.getOwner().getId().equals(ownerId))
                .toList();
    }

    // ── Cache-only operations (for Loader) ──

    /**
     * Inserisce un ristorante nella cache in memoria (per operazioni di caricamento bulk).
     *
     * @param r il ristorante da inserire nella cache
     */
    public void putCache(Restaurant r) {
        byId.put(r.getId(), r);
        byName.put(r.getName(), r);
    }

    /**
     * Rimuove un ristorante dalla cache in memoria.
     *
     * @param id l'identificativo del ristorante da rimuovere
     */
    public void removeCache(UUID id) {
        Restaurant r = byId.remove(id);
        if (r != null) byName.remove(r.getName());
    }

    // ── DAO access (for Loader bulk load) ──

    /**
     * Carica tutti i ristoranti dal database in modo asincrono.
     *
     * @return un {@link CompletableFuture} contenente la lista di tutti i ristoranti,
     *         oppure una lista vuota in caso di errore
     */
    public CompletableFuture<List<Restaurant>> loadAllFromDb() {
        return CompletableFuture
                .supplyAsync(dao::findAll)
                .exceptionally(ex -> {
                    System.err.println("Errore caricamento restaurants: " + ex.getMessage());
                    return new ArrayList<>();
                });
    }

    /**
     * Restituisce i tipi di cucina di un ristorante delegando direttamente al DAO.
     *
     * @param restaurantId l'identificativo del ristorante
     * @return l'insieme dei tipi di cucina associati al ristorante
     */
    public Set<CuisineType> findCuisines(UUID restaurantId) { return dao.findCuisines(restaurantId); }

    /**
     * Restituisce i servizi offerti da un ristorante delegando direttamente al DAO.
     *
     * @param restaurantId l'identificativo del ristorante
     * @return l'insieme dei servizi associati al ristorante
     */
    public Set<String> findServices(UUID restaurantId) { return dao.findServices(restaurantId); }
}
