package it.uninsubria.laboratoriob.api.remote;

import it.uninsubria.laboratoriob.api.enums.CuisineType;
import it.uninsubria.laboratoriob.api.objects.Restaurant;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface RestaurantServiceInter extends Remote {
    public List<Restaurant> findAll(int offset, int limit) throws RemoteException;
    public long count() throws RemoteException;
    public Restaurant findById(UUID id) throws RemoteException;
    public List<Restaurant> findByOwner(UUID id) throws  RemoteException;
    public boolean save (Restaurant restaurant) throws RemoteException;
    public boolean update(Restaurant restaurant) throws RemoteException;
    public boolean delete(UUID id) throws RemoteException;
    public boolean updateCuisines(UUID restaurantId, Set<CuisineType> cuisines) throws RemoteException;
    boolean updateServices(UUID restaurantId, Set<String> services) throws RemoteException;
}
