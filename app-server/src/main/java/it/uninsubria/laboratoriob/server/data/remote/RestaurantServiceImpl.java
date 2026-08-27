package it.uninsubria.laboratoriob.server.data.remote;

import it.uninsubria.laboratoriob.api.enums.CuisineType;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.api.remote.RestaurantServiceInter;
import it.uninsubria.laboratoriob.server.data.RestaurantDAO;
import it.uninsubria.laboratoriob.server.utils.Loader;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class RestaurantServiceImpl extends UnicastRemoteObject implements RestaurantServiceInter {
    private final RestaurantDAO rDAO = new RestaurantDAO();

    protected RestaurantServiceImpl() throws RemoteException {
    }

    @Override
    public List<Restaurant> findAll(int offset, int limit) throws RemoteException {
        List<Restaurant> all = new ArrayList<>(Loader.getAllRestaurants());
        if (offset >= all.size()) return List.of();
        return all.subList(offset, Math.min(offset + limit, all.size()));
    }

    @Override
    public long count() throws RemoteException {
        return Loader.countRestaurants();
    }

    @Override
    public Restaurant findById(UUID id) throws RemoteException {
        return Loader.findRestaurantById(id);
    }

    @Override
    public List<Restaurant> findByOwner(UUID id) throws RemoteException {
        return Loader.getAllRestaurants().stream()
                .filter(r -> r.getOwner() != null && r.getOwner().getId().equals(id))
                .toList();
    }

    @Override
    public boolean save(Restaurant restaurant) throws RemoteException {
        boolean ok = rDAO.save(restaurant);
        if (ok) Loader.addRestaurant(restaurant);
        return ok;
    }

    @Override
    public boolean update(Restaurant restaurant) throws RemoteException {
        Restaurant old = Loader.findRestaurantById(restaurant.getId());
        boolean ok = rDAO.update(restaurant);
        if (ok) Loader.updateRestaurant(old, restaurant);
        return ok;
    }

    @Override
    public boolean delete(UUID id) throws RemoteException {
        boolean ok = rDAO.delete(id);
        if (ok) Loader.removeRestaurant(id);
        return ok;
    }

    @Override
    public boolean updateCuisines(UUID restaurantId, Set<CuisineType> cuisines) throws RemoteException {
        return rDAO.updateCuisines(restaurantId, cuisines);
    }

    @Override
    public boolean updateServices(UUID restaurantId, Set<String> services) throws RemoteException {
        return rDAO.updateServices(restaurantId, services);
    }
}
