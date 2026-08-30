package it.uninsubria.laboratoriob.server.data.repository;

import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.api.objects.Review;
import it.uninsubria.laboratoriob.server.data.dao.ReviewDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Repository con cache in memoria per l'entita' Review. Incapsula ReviewDAO e legge dagli oggetti Restaurant in memoria.
 */
public class ReviewRepository {

    private final ReviewDAO dao = new ReviewDAO();
    private final RestaurantRepository restaurantRepo;

    /**
     * Costruisce un ReviewRepository con il Repository dei ristoranti specificato.
     *
     * @param restaurantRepo il repository dei ristoranti da utilizzare per le operazioni di lettura
     */
    public ReviewRepository(RestaurantRepository restaurantRepo) {
        this.restaurantRepo = restaurantRepo;
    }

    // ── Write operations (DB + in-memory Restaurant) ──

    /**
     * Salva una recensione nel database e la aggiunge all'oggetto Restaurant in memoria.
     *
     * @param review la recensione da salvare
     * @return {@code true} se il salvataggio ha avuto successo, {@code false} altrimenti
     */
    public boolean save(Review review) {
        boolean ok = dao.save(review);
        if (ok && review.getRestaurant() != null) {
            Restaurant r = restaurantRepo.findById(review.getRestaurant().getId());
            if (r != null) r.addReview(review);
        }
        return ok;
    }

    /**
     * Aggiorna una recensione esistente nel database e nell'oggetto Restaurant in memoria.
     *
     * @param review la recensione con i dati aggiornati
     * @return {@code true} se l'aggiornamento ha avuto successo, {@code false} altrimenti
     */
    public boolean update(Review review) {
        boolean ok = dao.update(review);
        if (ok && review.getRestaurant() != null) {
            Restaurant r = restaurantRepo.findById(review.getRestaurant().getId());
            if (r != null) r.getReviews().put(review.getId(), review);
        }
        return ok;
    }

    /**
     * Elimina una recensione dal database e la rimuove dall'oggetto Restaurant in memoria.
     *
     * @param id l'identificativo della recensione da eliminare
     * @return {@code true} se l'eliminazione ha avuto successo, {@code false} altrimenti
     */
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

    /**
     * Cerca una recensione per ID attraverso gli oggetti Restaurant in memoria.
     *
     * @param id l'identificativo della recensione
     * @return la recensione trovata, oppure {@code null} se non esiste
     */
    public Review findById(UUID id) {
        for (Restaurant r : restaurantRepo.findAll())
            for (Review review : r.getReviews().values())
                if (review.getId().equals(id))
                    return review;
        return null;
    }

    /**
     * Cerca tutte le recensioni di un ristorante attraverso gli oggetti Restaurant in memoria.
     *
     * @param restaurantId l'identificativo del ristorante
     * @return una lista non modificabile delle recensioni del ristorante specificato,
     *         oppure una lista vuota se il ristorante non esiste
     */
    public List<Review> findByRestaurant(UUID restaurantId) {
        Restaurant r = restaurantRepo.findById(restaurantId);
        if (r == null) return List.of();
        return List.copyOf(r.getReviews().values());
    }

    /**
     * Cerca tutte le recensioni scritte da un utente attraverso gli oggetti Restaurant in memoria.
     *
     * @param userId l'identificativo dell'utente
     * @return una lista delle recensioni scritte dall'utente specificato
     */
    public List<Review> findByUser(UUID userId) {
        List<Review> result = new ArrayList<>();
        for (Restaurant r : restaurantRepo.findAll())
            for (Review review : r.getReviews().values())
                if (review.getUser() != null && review.getUser().getId().equals(userId))
                    result.add(review);
        return result;
    }

    /**
     * Restituisce tutte le recensioni presenti in memoria attraverso gli oggetti Restaurant.
     *
     * @return una lista di tutte le recensioni
     */
    public List<Review> findAll() {
        List<Review> result = new ArrayList<>();
        for (Restaurant r : restaurantRepo.findAll())
            result.addAll(r.getReviews().values());
        return result;
    }

    /**
     * Restituisce il numero totale di recensioni delegando al DAO.
     *
     * @return il numero totale di recensioni nel database
     */
    public long count() {
        return dao.count();
    }

    // ── DAO access (for Loader bulk load) ──

    /**
     * Carica tutte le recensioni di un ristorante dal database in modo asincrono.
     *
     * @param restaurantId l'identificativo del ristorante
     * @return un {@link CompletableFuture} contenente la lista delle recensioni del ristorante,
     *         oppure una lista vuota in caso di errore
     */
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
