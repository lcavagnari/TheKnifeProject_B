package it.uninsubria.laboratoriob.api.remote;

import it.uninsubria.laboratoriob.api.enums.CuisineType;
import it.uninsubria.laboratoriob.api.objects.Restaurant;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;
import java.util.UUID;

public interface RestaurantServiceInter extends Remote {
    Set<Restaurant> findAll(int offset, int limit) throws RemoteException;
    long count() throws RemoteException;
    Restaurant findById(UUID id) throws RemoteException;
    Set<Restaurant> findByOwner(UUID id) throws  RemoteException;
    boolean save(Restaurant restaurant) throws RemoteException;
    boolean update(Restaurant restaurant) throws RemoteException;
    boolean delete(UUID id) throws RemoteException;
    boolean registerOwner(Restaurant restaurant, UUID ownerId) throws RemoteException;
    boolean unregisterOwner(UUID ownerId, UUID restaurantId) throws RemoteException;
    boolean updateCuisines(UUID restaurantId, Set<CuisineType> cuisines) throws RemoteException;
    boolean updateServices(UUID restaurantId, Set<String> services) throws RemoteException;
}
