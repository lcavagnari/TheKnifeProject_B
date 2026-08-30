package it.uninsubria.laboratoriob.server.data.repository;

import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.api.objects.Review;
import it.uninsubria.laboratoriob.server.data.dao.ReviewDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ReviewRepository {

    private final ReviewDAO dao = new ReviewDAO();
    private final RestaurantRepository restaurantRepo;

    public ReviewRepository(RestaurantRepository restaurantRepo) {
        this.restaurantRepo = restaurantRepo;
    }

    // ── Write operations (DB + in-memory Restaurant) ──

    public boolean save(Review review) {
        boolean ok = dao.save(review);
        if (ok && review.getRestaurant() != null) {
            Restaurant r = restaurantRepo.findById(review.getRestaurant().getId());
            if (r != null) r.addReview(review);
        }
        return ok;
    }

    public boolean update(Review review) {
        boolean ok = dao.update(review);
        if (ok && review.getRestaurant() != null) {
            Restaurant r = restaurantRepo.findById(review.getRestaurant().getId());
            if (r != null) r.getReviews().put(review.getId(), review);
        }
        return ok;
    }

    public boolean delete(UUID id) {
        boolean ok = dao.delete(id);
        if (ok) {
            for (Restaurant r : restaurantRepo.findAll()) {
                if (r.getReviews().remove(id) != null) break;
            }
        }
        return ok;
    }

    // ── Read operations ──

    public Review findById(UUID id) {
        for (Restaurant r : restaurantRepo.findAll())
            for (Review review : r.getReviews().values())
                if (review.getId().equals(id))
                    return review;
        return null;
    }

    public List<Review> findByRestaurant(UUID restaurantId) {
        Restaurant r = restaurantRepo.findById(restaurantId);
        if (r == null) return List.of();
        return List.copyOf(r.getReviews().values());
    }

    public List<Review> findByUser(UUID userId) {
        List<Review> result = new ArrayList<>();
        for (Restaurant r : restaurantRepo.findAll())
            for (Review review : r.getReviews().values())
                if (review.getUser() != null && review.getUser().getId().equals(userId))
                    result.add(review);
        return result;
    }

    public List<Review> findAll() {
        List<Review> result = new ArrayList<>();
        for (Restaurant r : restaurantRepo.findAll())
            result.addAll(r.getReviews().values());
        return result;
    }

    public long count() {
        return dao.count();
    }

    // ── DAO access (for Loader bulk load) ──

    public CompletableFuture<List<Review>> loadForRestaurant(UUID restaurantId) {
        return CompletableFuture
                .supplyAsync(() -> dao.findByRestaurant(restaurantId))
                .exceptionally(ex -> {
                    System.err.println(
                            "Errore caricamento review for restaurant #"
                                    + restaurantId + ": " + ex.getMessage()
                    );
                    return new ArrayList<>();
                });
    }
}
