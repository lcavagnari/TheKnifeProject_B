package it.uninsubria.laboratoriob.api.remote;

import it.uninsubria.laboratoriob.api.objects.Review;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

public interface ReviewServiceInter extends Remote {
    public List<Review> findByRestaurant(UUID restaurantId) throws RemoteException;
    public List<Review> findAll() throws RemoteException;
    public boolean save(Review review) throws RemoteException;
    public boolean update(Review review) throws RemoteException;
    public boolean delete(UUID id) throws RemoteException;
}
