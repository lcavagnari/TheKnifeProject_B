package it.uninsubria.laboratoriob.server.data.remote;

import it.uninsubria.laboratoriob.api.objects.Review;
import it.uninsubria.laboratoriob.api.remote.ReviewServiceInter;
import it.uninsubria.laboratoriob.server.data.ServerDataStore;
import it.uninsubria.laboratoriob.server.data.dao.ReviewDAO;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
        // NOTE: not propagated to the cacheOk/dbOk pattern - store.reviews().save(review)
        // already does its own INSERT via ReviewRepository's internal DAO, so pairing it
        // with rDAO.save(review) here would double-INSERT the same primary key and always
        // fail, same risk as RestaurantServiceImpl.save()/registerOwner(). Left as a single
        // DB-only write, flagged per your instruction rather than "fixed" unilaterally.
        return rDAO.save(review);
    }

    @Override
    public boolean update(Review review) throws RemoteException {
        if (review == null) return false;

        boolean cacheOk = store.reviews().update(review);
        CompletableFuture<Boolean> dbOk = CompletableFuture
                .supplyAsync(() -> rDAO.update(review))
                .exceptionally(ex -> false);

        if (cacheOk && dbOk.join()) return true;
        else throw new RemoteException("Error occured while saving changes");
    }

    @Override
    public boolean delete(UUID id) throws RemoteException {
        if (id == null) return false;

        boolean cacheOk = store.reviews().delete(id);
        CompletableFuture<Boolean> dbOk = CompletableFuture
                .supplyAsync(() -> rDAO.delete(id))
                .exceptionally(ex -> false);

        if (cacheOk && dbOk.join()) return true;
        else throw new RemoteException("Error occured while saving changes");
    }

    @Override
    public boolean replyToReview(UUID reviewId, String reply) throws RemoteException {
        if (reviewId == null || reply == null || reply.isBlank()) return false;

        CompletableFuture<Boolean> dbOk = CompletableFuture
                .supplyAsync(() -> rDAO.saveReply(reviewId, reply))
                .exceptionally(ex -> false);

        boolean ok = dbOk.join();
        if (ok) {
            Review review = store.reviews().findById(reviewId);
            if (review != null) {
                review.setReply(reply);
                review.setRespondedAt(LocalDateTime.now());
            }
        }

        if (ok) return true;
        else throw new RemoteException("Error occured while saving changes");
    }
}
