package it.uninsubria.laboratoriob.server.data.remote;

import it.uninsubria.laboratoriob.api.objects.Review;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.api.remote.ReviewServiceInter;
import it.uninsubria.laboratoriob.server.data.ServerDataStore;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.UUID;

public class ReviewServiceImpl extends UnicastRemoteObject implements ReviewServiceInter {
    private final ServerDataStore store;

    public ReviewServiceImpl(ServerDataStore store) throws RemoteException {
        this.store = store;
    }

    @Override
    public List<Review> findByRestaurant(UUID restaurantId) throws RemoteException {
        return store.findReviewsByRestaurant(restaurantId);
    }

    @Override
    public List<Review> findByUser(UUID userId) throws RemoteException {
        return store.findReviewsByUser(userId);
    }

    @Override
    public List<Review> findAll() throws RemoteException {
        return store.findAllReviews();
    }

    @Override
    public List<Review> findAll(int offset, int limit) throws RemoteException {
        List<Review> all = store.findAllReviews();
        if (offset >= all.size()) return List.of();
        return all.subList(offset, Math.min(offset + limit, all.size()));
    }

    @Override
    public boolean save(Review review) throws RemoteException {
        boolean ok = store.reviewDAO().save(review);
        if (ok && review.getRestaurant() != null) {
            Restaurant r = store.findRestaurantById(review.getRestaurant().getId());
            if (r != null) r.addReview(review);
        }
        return ok;
    }

    @Override
    public boolean update(Review review) throws RemoteException {
        return store.reviewDAO().update(review);
    }

    @Override
    public boolean delete(UUID id) throws RemoteException {
        return store.reviewDAO().delete(id);
    }
}
