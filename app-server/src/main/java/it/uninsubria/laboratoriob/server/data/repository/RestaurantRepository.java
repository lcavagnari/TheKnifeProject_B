package it.uninsubria.laboratoriob.server.data.repository;

import it.uninsubria.laboratoriob.api.enums.CuisineType;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.server.data.dao.RestaurantDAO;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class RestaurantRepository {

    private final RestaurantDAO dao = new RestaurantDAO();
    private final Map<UUID, Restaurant> byId = new ConcurrentHashMap<>();
    private final Map<String, Restaurant> byName = new ConcurrentHashMap<>();

    // ── Write operations (DB + cache) ──

    public boolean save(Restaurant r) {
        boolean ok = dao.save(r);
        if (ok) putCache(r);

        return ok;
    }

    public boolean update(Restaurant r) {
        boolean ok = dao.update(r);
        if (ok) {
            byId.put(r.getId(), r);
            byName.put(r.getName(), r);
        }

        return ok;
    }

    public boolean delete(UUID id) {
        Restaurant removed = byId.remove(id);
        if (removed != null) byName.remove(removed.getName());
        return dao.delete(id);
    }

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

    public Restaurant findById(UUID id) { return byId.get(id); }

    public Restaurant findByName(String name) { return byName.get(name); }

    public boolean hasByName(String name) { return byName.containsKey(name); }

    public long count() { return byId.size(); }

    public Collection<Restaurant> findAll() { return Collections.unmodifiableCollection(byId.values()); }

    public List<Restaurant> findByOwner(UUID ownerId) {
        return byId.values().stream()
                .filter(r -> r.getOwner() != null && r.getOwner().getId().equals(ownerId))
                .toList();
    }

    // ── Cache-only operations (for Loader) ──

    public void putCache(Restaurant r) {
        byId.put(r.getId(), r);
        byName.put(r.getName(), r);
    }

    public void removeCache(UUID id) {
        Restaurant r = byId.remove(id);
        if (r != null) byName.remove(r.getName());
    }

    // ── DAO access (for Loader bulk load) ──

    public CompletableFuture<List<Restaurant>> loadAllFromDb() {
        return CompletableFuture
                .supplyAsync(dao::findAll)
                .exceptionally(ex -> {
                    System.err.println("Errore caricamento restaurants: " + ex.getMessage());
                    return new ArrayList<>();
                });
    }

    public Set<CuisineType> findCuisines(UUID restaurantId) { return dao.findCuisines(restaurantId); }

    public Set<String> findServices(UUID restaurantId) { return dao.findServices(restaurantId); }
}
