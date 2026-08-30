package it.uninsubria.laboratoriob.server.data.remote;

import it.uninsubria.laboratoriob.api.objects.Review;
import it.uninsubria.laboratoriob.api.remote.ReviewServiceInter;
import it.uninsubria.laboratoriob.server.data.ServerDataStore;
import it.uninsubria.laboratoriob.server.data.dao.ReviewDAO;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/** Implementazione RMI del servizio recensioni (cache + DB, fallback). */
public class ReviewServiceImpl extends UnicastRemoteObject implements ReviewServiceInter {
    
    private final ReviewDAO rDAO = new ReviewDAO();
    private final ServerDataStore store;
    
    /**
     * Costruisce l'implementazione remota del servizio recensioni.
     *
     * @param store facade dei dati server
     * @throws RemoteException se l'esportazione RMI fallisce
     */
    public ReviewServiceImpl(ServerDataStore store) throws RemoteException {
        this.store = store;
    }

    @Override
    public List<Review> findByRestaurant(UUID restaurantId) throws RemoteException {
        if (restaurantId == null) return List.of();

        Set<Review> merged = new HashSet<>(store.reviews().findByRestaurant(restaurantId));

        CompletableFuture<List<Review>> db = CompletableFuture
                .supplyAsync(() -> rDAO.findByRestaurant(restaurantId))
                .exceptionally(ex -> List.of());
        merged.addAll(db.join());

        return new ArrayList<>(merged);
    }

    @Override
    public List<Review> findByUser(UUID userId) throws RemoteException {
        if (userId == null) return List.of();

        Set<Review> merged = new HashSet<>(store.reviews().findByUser(userId));

        CompletableFuture<List<Review>> db = CompletableFuture
                .supplyAsync(() -> rDAO.findByUser(userId))
                .exceptionally(ex -> List.of());
        merged.addAll(db.join());

        return new ArrayList<>(merged);
    }

    @Override
    public List<Review> findAll() throws RemoteException {
        Set<Review> merged = new HashSet<>(store.reviews().findAll());

        CompletableFuture<List<Review>> db = CompletableFuture
                .supplyAsync(rDAO::findAll)
                .exceptionally(ex -> List.of());
        merged.addAll(db.join());

        return new ArrayList<>(merged);
    }

    @Override
    public Set<Review> findAll(int offset, int limit) throws RemoteException {
        List<Review> allCache = store.reviews().findAll();
        List<Review> cachePage = offset >= allCache.size()
                ? List.of()
                : allCache.subList(offset, Math.min(offset + limit, allCache.size()));

        Set<Review> merged = new HashSet<>(cachePage);

        CompletableFuture<List<Review>> db = CompletableFuture
                .supplyAsync(() -> rDAO.findAll(offset, limit))
                .exceptionally(ex -> List.of());
        merged.addAll(db.join());

        return merged;
    }

    // NOTE: not propagated to the cacheOk/dbOk pattern since store.reviews().save(review)
    // already does its own INSERT via ReviewRepository's internal DAO, risk of duplication
    @Override
    public boolean save(Review review) throws RemoteException {
        return rDAO.save(review);
    }

    @Override
    public boolean update(Review review) throws RemoteException {
        if (review == null) return false;

        boolean cacheOk = store.reviews().update(review);
        CompletableFuture<Boolean> dbOk = CompletableFuture
                .supplyAsync(() -> rDAO.update(review))
                .exceptionally(ex -> false);

        if (cacheOk & dbOk.join()) return true;
        else throw new RemoteException("Error occured while saving changes");
    }

    @Override
    public boolean delete(UUID id) throws RemoteException {
        if (id == null) return false;

        boolean cacheOk = store.reviews().delete(id);
        CompletableFuture<Boolean> dbOk = CompletableFuture
                .supplyAsync(() -> rDAO.delete(id))
                .exceptionally(ex -> false);

        if (cacheOk & dbOk.join()) return true;
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
