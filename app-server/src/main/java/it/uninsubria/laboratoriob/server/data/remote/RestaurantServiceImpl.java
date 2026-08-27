package it.uninsubria.laboratoriob.server.data.remote;

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
        List<Restaurant> all = new ArrayList<>(store.getAllRestaurants());
        if (offset >= all.size()) return List.of();
        return all.subList(offset, Math.min(offset + limit, all.size()));
    }

    @Override
    public long count() throws RemoteException {
        return store.restaurantDAO().count();
    }

    @Override
    public Restaurant findById(UUID id) throws RemoteException {
        return store.findRestaurantById(id);
    }

    @Override
    public List<Restaurant> findByOwner(UUID id) throws RemoteException {
        return store.getAllRestaurants().stream()
                .filter(r -> r.getOwner() != null && r.getOwner().getId().equals(id))
                .toList();
    }

    @Override
    public boolean save(Restaurant restaurant) throws RemoteException {
        boolean ok = store.restaurantDAO().save(restaurant);
        if (ok) store.addRestaurant(restaurant);
        return ok;
    }

    @Override
    public boolean update(Restaurant restaurant) throws RemoteException {
        Restaurant old = store.findRestaurantById(restaurant.getId());
        boolean ok = store.restaurantDAO().update(restaurant);
        if (ok) store.updateRestaurant(old, restaurant);
        return ok;
    }

    @Override
    public boolean delete(UUID id) throws RemoteException {
        boolean ok = store.restaurantDAO().delete(id);
        if (ok) store.removeRestaurant(id);
        return ok;
    }

    @Override
    public boolean updateCuisines(UUID restaurantId, Set<CuisineType> cuisines) throws RemoteException {
        return store.restaurantDAO().updateCuisines(restaurantId, cuisines);
    }

    @Override
    public boolean updateServices(UUID restaurantId, Set<String> services) throws RemoteException {
        return store.restaurantDAO().updateServices(restaurantId, services);
    }
}
