package it.uninsubria.laboratoriob.server.data.remote;

import it.uninsubria.laboratoriob.api.objects.Review;
import it.uninsubria.laboratoriob.api.remote.ReviewServiceInter;
import it.uninsubria.laboratoriob.server.data.ReviewDAO;
import it.uninsubria.laboratoriob.server.utils.Loader;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.UUID;

public class ReviewServiceImpl extends UnicastRemoteObject implements ReviewServiceInter {
    private final ReviewDAO rDAO = new ReviewDAO();

    public ReviewServiceImpl() throws RemoteException {
    }

    @Override
    public List<Review> findByRestaurant(UUID restaurantId) throws RemoteException {
        return Loader.findReviewsByRestaurant(restaurantId);
    }

    @Override
    public List<Review> findByUser(UUID userId) throws RemoteException {
        return Loader.findReviewsByUser(userId);
    }

    @Override
    public List<Review> findAll() throws RemoteException {
        return Loader.findAllReviews();
    }

    @Override
    public List<Review> findAll(int offset, int limit) throws RemoteException {
        List<Review> all = Loader.findAllReviews();
        if (offset >= all.size()) return List.of();
        return all.subList(offset, Math.min(offset + limit, all.size()));
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
