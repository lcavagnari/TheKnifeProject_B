package it.uninsubria.laboratoriob.server.remote;

import it.uninsubria.laboratoriob.api.enums.CuisineType;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.api.remote.RestaurantServiceInter;
import it.uninsubria.laboratoriob.server.data.ServerDataStore;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class RestaurantServiceImpl extends UnicastRemoteObject implements RestaurantServiceInter {
    private final ServerDataStore store;

    public RestaurantServiceImpl(ServerDataStore store) throws RemoteException {
        this.store = store;
    }

    @Override
    public List<Restaurant> findAll(int offset, int limit) throws RemoteException {
        List<Restaurant> all = new ArrayList<>(store.restaurants().findAll());
        if (offset >= all.size()) return List.of();
        return new ArrayList<>(all.subList(offset, Math.min(offset + limit, all.size())));
    }

    @Override
    public long count() throws RemoteException {
        return store.restaurants().count();
    }

    @Override
    public Restaurant findById(UUID id) throws RemoteException {
        return store.restaurants().findById(id);
    }

    @Override
    public List<Restaurant> findByOwner(UUID id) throws RemoteException {
        return store.restaurants().findByOwner(id);
    }

    @Override
    public boolean save(Restaurant restaurant) throws RemoteException {
        if (restaurant == null) return false;
        if (store.restaurants().save(restaurant)) return true;
        else throw new RemoteException("Error occured while saving changes");
    }

    @Override
    public boolean update(Restaurant restaurant) throws RemoteException {
        if (restaurant == null) return false;
        if (store.restaurants().update(restaurant)) return true;
        else throw new RemoteException("Error occured while saving changes");
    }

    @Override
    public boolean delete(UUID id) throws RemoteException {
        if (id == null) return false;
        if (store.restaurants().delete(id)) return true;
        else throw new RemoteException("Error occured while saving changes");
    }

    @Override
    public boolean updateCuisines(UUID restaurantId, Set<CuisineType> cuisines) throws RemoteException {
        if (restaurantId == null || cuisines == null) return false;
        if (store.restaurants().updateCuisines(restaurantId, cuisines)) return true;
        else throw new RemoteException("Error occured while saving changes");
    }

    @Override
    public boolean updateServices(UUID restaurantId, Set<String> services) throws RemoteException {
        if (restaurantId == null || services == null) return false;
        if (store.restaurants().updateServices(restaurantId, services)) return true;
        else throw new RemoteException("Error occured while saving changes");
    }

    @Override
    public boolean registerOwner(Restaurant restaurant, UUID ownerId) throws RemoteException {
        if (restaurant == null || ownerId == null) return false;

        boolean saved = store.restaurants().save(restaurant) && store.users().addOwnedRestaurant(ownerId, restaurant);
        if (saved) return true;
        else throw new RemoteException("Error occured while saving changes");
    }

    @Override
    public boolean unregisterOwner(UUID ownerId, UUID restaurantId) throws RemoteException {
        if (ownerId == null || restaurantId == null) return false;

        boolean ok = store.users().removeOwnedRestaurant(ownerId, restaurantId) && store.restaurants().delete(restaurantId);
        if (ok) return true;
        else throw new RemoteException("Error occured while saving changes");
    }
}
