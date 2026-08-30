package it.uninsubria.laboratoriob.server.data.remote;

import it.uninsubria.laboratoriob.api.objects.Review;
import it.uninsubria.laboratoriob.api.remote.ReviewServiceInter;
import it.uninsubria.laboratoriob.server.data.ServerDataStore;
import it.uninsubria.laboratoriob.server.data.dao.ReviewDAO;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.UUID;

public class ReviewServiceImpl extends UnicastRemoteObject implements ReviewServiceInter {
    
    private final ReviewDAO rDAO = new ReviewDAO();
    private final ServerDataStore store;
    
    public ReviewServiceImpl(ServerDataStore store) throws RemoteException {
        this.store = store;
    }

    @Override
    public List<Review> findByRestaurant(UUID restaurantId) throws RemoteException {
        return store.reviews().findByRestaurant(restaurantId);
    }

    @Override
    public List<Review> findByUser(UUID userId) throws RemoteException {
        return store.reviews().findByUser(userId);
    }

    @Override
    public List<Review> findAll() throws RemoteException {
        return store.reviews().findAll();
    }

    @Override
    public List<Review> findAll(int offset, int limit) throws RemoteException {
        List<Review> all = store.reviews().findAll();
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

    @Override
    public boolean replyToReview(UUID reviewId, String reply) throws RemoteException {
        if (reply == null || reply.isBlank()) return false;
        return rDAO.saveReply(reviewId, reply);
    }
}
