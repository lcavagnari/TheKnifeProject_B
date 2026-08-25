package it.uninsubria.laboratoriob.server.data.remote;

import it.uninsubria.laboratoriob.api.objects.Review;
import it.uninsubria.laboratoriob.api.remote.ReviewServiceInter;

import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

public class ReviewServiceImpl implements ReviewServiceInter {
    @Override
    public List<Review> findByRestaurant(UUID restaurantId) throws RemoteException {
        return List.of();
    }

    @Override
    public List<Review> findAll() throws RemoteException {
        return List.of();
    }

    @Override
    public boolean save(Review review) throws RemoteException {
        return false;
    }

    @Override
    public boolean update(Review review) throws RemoteException {
        return false;
    }

    @Override
    public boolean delete(UUID id) throws RemoteException {
        return false;
    }
}
