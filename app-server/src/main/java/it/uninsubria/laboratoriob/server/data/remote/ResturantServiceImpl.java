package it.uninsubria.laboratoriob.server.data.remote;

import it.uninsubria.laboratoriob.api.enums.CuisineType;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.api.remote.ResturantServiceInter;
import it.uninsubria.laboratoriob.server.data.RestaurantDAO;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class ResturantServiceImpl implements ResturantServiceInter {
    private final RestaurantDAO rDAO =  new RestaurantDAO();

    @Override
    public List<Restaurant> findAll(int offset, int limit) throws RemoteException {
        return rDAO.findAll(offset, limit);
    }

    @Override
    public long count() throws RemoteException {
        return rDAO.count();
    }

    @Override
    public Optional<Restaurant> findById(UUID id) throws RemoteException {
        return rDAO.findById(id);
    }

    @Override
    public List<Restaurant> findByOwner(UUID id) throws RemoteException {
        return rDAO.findByOwner(id);
    }

    @Override
    public boolean save(Restaurant restaurant) throws RemoteException {
        return rDAO.save(restaurant);
    }

    @Override
    public boolean update(Restaurant restaurant) throws RemoteException {
        return rDAO.update(restaurant);
    }

    @Override
    public boolean delete(UUID id) throws RemoteException {
        return rDAO.delete(id);
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
