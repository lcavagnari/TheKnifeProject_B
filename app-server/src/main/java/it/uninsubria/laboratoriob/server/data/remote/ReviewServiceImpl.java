package it.uninsubria.laboratoriob.server.data.remote;

import it.uninsubria.laboratoriob.api.objects.Review;
import it.uninsubria.laboratoriob.api.remote.ReviewServiceInter;
import it.uninsubria.laboratoriob.server.data.ReviewDAO;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.UUID;

public class ReviewServiceImpl extends UnicastRemoteObject implements ReviewServiceInter {
    ReviewDAO rDAO = new  ReviewDAO();

    protected ReviewServiceImpl() throws RemoteException {
    }

    @Override
    public List<Review> findByRestaurant(UUID restaurantId) throws RemoteException {
        return rDAO.findByRestaurant(restaurantId);
    }

    @Override
    public List<Review> findAll() throws RemoteException {
        return rDAO.findAll();
    }

    @Override
    public boolean save(Review review) throws RemoteException {
        return rDAO.save(review);
    }

    @Override
    public boolean update(Review review) throws RemoteException {
        return rDAO.update(review);
    }

    @Override
    public boolean delete(UUID id) throws RemoteException {
        return rDAO.delete(id);
    }
}
