package it.uninsubria.laboratoriob.server.data.remote;

import it.uninsubria.laboratoriob.api.enums.CuisineType;
import it.uninsubria.laboratoriob.api.objects.Owner;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.api.objects.User;
import it.uninsubria.laboratoriob.api.remote.RestaurantServiceInter;
import it.uninsubria.laboratoriob.server.data.ServerDataStore;
import it.uninsubria.laboratoriob.server.data.dao.RestaurantDAO;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class RestaurantServiceImpl extends UnicastRemoteObject implements RestaurantServiceInter {
    
    private final RestaurantDAO rDAO = new RestaurantDAO();
    private final ServerDataStore store;

    public RestaurantServiceImpl(ServerDataStore store) throws RemoteException {
        this.store = store;
    }

    @Override
    public Set<Restaurant> findAll(int offset, int limit) throws RemoteException {
        List<Restaurant> allCache = new ArrayList<>(store.restaurants().findAll());
        List<Restaurant> cachePage = offset >= allCache.size()
                ? List.of()
                : allCache.subList(offset, Math.min(offset + limit, allCache.size()));

        Set<Restaurant> merged = new HashSet<>(cachePage);

        CompletableFuture<List<Restaurant>> db = CompletableFuture
                .supplyAsync(() -> rDAO.findAll(offset, limit))
                .exceptionally(ex -> List.of());
        merged.addAll(db.join());

        return merged;
    }

    @Override
    public long count() throws RemoteException {
        long cache = store.restaurants().count();
        CompletableFuture<Long> db = CompletableFuture.supplyAsync(rDAO::count);

        // Using cache as base and assuming edge-case in which # in cache != # in DB
        return cache + (db.join() - cache);
    }

    @Override
    public Restaurant findById(UUID id) throws RemoteException {
        if (id == null) return null;

        Restaurant cache = store.restaurants().findById(id);
        CompletableFuture<Restaurant> db = CompletableFuture
                .supplyAsync(() -> rDAO.findById(id).orElse(null))
                .exceptionally(ex -> null);

        return cache != null ? cache : db.join();
    }

    @Override
    public List<Restaurant> findByOwner(UUID id) throws RemoteException {
        if (id == null) return List.of();

        Set<Restaurant> merged = new HashSet<>();
        User owner = store.users().findById(id);
        if (owner instanceof Owner o) merged.addAll(o.getRestaurantsById().values());

        CompletableFuture<List<Restaurant>> db = CompletableFuture
                .supplyAsync(() -> rDAO.findByOwner(id))
                .exceptionally(ex -> List.of());
        merged.addAll(db.join());

        return new ArrayList<>(merged);
    }

    @Override
    public boolean save(Restaurant restaurant) throws RemoteException {
        if (restaurant == null) return false;

        boolean cacheOk = store.restaurants().save(restaurant);
        CompletableFuture<Boolean> dbOk = CompletableFuture
                .supplyAsync(() -> rDAO.save(restaurant))
                .exceptionally(ex -> false);

        if (cacheOk & dbOk.join()) return true;
        else throw new RemoteException("Error occured while saving changes");
    }

    @Override
    public boolean update(Restaurant restaurant) throws RemoteException {
        if (restaurant == null) return false;

        boolean cacheOk = store.restaurants().update(restaurant);
        CompletableFuture<Boolean> dbOk = CompletableFuture
                .supplyAsync(() -> rDAO.update(restaurant))
                .exceptionally(ex -> false);

        if (cacheOk & dbOk.join()) return true;
        else throw new RemoteException("Error occured while saving changes");
    }

    @Override
    public boolean delete(UUID id) throws RemoteException {
        if (id == null) return false;

        boolean cacheOk = store.restaurants().delete(id);
        CompletableFuture<Boolean> dbOk = CompletableFuture
                .supplyAsync(() -> rDAO.delete(id))
                .exceptionally(ex -> false);

        if (cacheOk & dbOk.join()) return true;
        else throw new RemoteException("Error occured while saving changes");
    }

    @Override
    public boolean registerOwner(Restaurant restaurant, UUID ownerId) throws RemoteException {
        if (restaurant == null || ownerId == null) return false;

        boolean cacheOk = store.restaurants().save(restaurant)
                && store.users().addOwnedRestaurant(ownerId, restaurant);

        CompletableFuture<Boolean> dbOk = CompletableFuture
                .supplyAsync(() -> rDAO.save(restaurant))
                .exceptionally(ex -> false);

        if (cacheOk & dbOk.join()) return true;
        else throw new RemoteException("Error occured while saving changes");
    }

    @Override
    public boolean unregisterOwner(UUID ownerId, UUID restaurantId) throws RemoteException {
        if (ownerId == null || restaurantId == null) return false;

        boolean cacheOk = store.users().removeOwnedRestaurant(ownerId, restaurantId)
                && store.restaurants().delete(restaurantId);

        CompletableFuture<Boolean> dbOk = CompletableFuture
                .supplyAsync(() -> rDAO.delete(restaurantId))
                .exceptionally(ex -> false);

        if (cacheOk & dbOk.join()) return true;
        else throw new RemoteException("Error occured while saving changes");
    }

    @Override
    public boolean updateCuisines(UUID restaurantId, Set<CuisineType> cuisines) throws RemoteException {
        if (restaurantId == null || cuisines == null) return false;

        boolean cacheOk = store.restaurants().updateCuisines(restaurantId,cuisines);
        CompletableFuture<Boolean> dbOk = CompletableFuture
                .supplyAsync(() -> rDAO.updateCuisines(restaurantId, cuisines))
                .exceptionally(ex -> false);

        if (cacheOk & dbOk.join()) return true;
        else throw new RemoteException("Error occured while saving changes");
    }

    @Override
    public boolean updateServices(UUID restaurantId, Set<String> services) throws RemoteException {
        if (restaurantId == null || services == null) return false;

        boolean cacheOk = store.restaurants().updateServices(restaurantId,services);
        CompletableFuture<Boolean> dbOk = CompletableFuture
                .supplyAsync(() -> rDAO.updateServices(restaurantId, services))
                .exceptionally(ex -> false);

        if (cacheOk & dbOk.join()) return true;
        else throw new RemoteException("Error occured while saving changes");
    }
}
