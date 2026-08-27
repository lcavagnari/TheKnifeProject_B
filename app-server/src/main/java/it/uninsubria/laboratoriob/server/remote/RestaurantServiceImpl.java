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
        return all.subList(offset, Math.min(offset + limit, all.size()));
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
        return store.restaurants().save(restaurant);
    }

    @Override
    public boolean update(Restaurant restaurant) throws RemoteException {
        return store.restaurants().update(restaurant);
    }

    @Override
    public boolean delete(UUID id) throws RemoteException {
        return store.restaurants().delete(id);
    }

    @Override
    public boolean updateCuisines(UUID restaurantId, Set<CuisineType> cuisines) throws RemoteException {
        return store.restaurants().updateCuisines(restaurantId, cuisines);
    }

    @Override
    public boolean updateServices(UUID restaurantId, Set<String> services) throws RemoteException {
        return store.restaurants().updateServices(restaurantId, services);
    }
}
