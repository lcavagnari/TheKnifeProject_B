package it.uninsubria.laboratoriob.server.data.dao;

import it.uninsubria.laboratoriob.api.data.DAO;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.api.objects.Review;
import it.uninsubria.laboratoriob.api.objects.User;
import it.uninsubria.laboratoriob.server.utils.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementazione del DAO per l'entità {@link Review}.
 * <p>
 * Gestisce le operazioni CRUD sulle recensioni nel database PostgreSQL.
 * Ogni recensione è associata a un ristorante e a un utente.
 * </p>
 *
 * <h2>Responsabilità</h2>
 * <ul>
 *   <li>Mappatura delle righe del ResultSet in oggetti {@link Review}.</li>
 *   <li>Ricerca per ID, per ristorante, e recupero di tutte le recensioni.</li>
 *   <li>Gestione dei timestamp di creazione e risposta.</li>
 * </ul>
 *
 * @author Luca Cavagnari
 * @version 2.0
 * @see DAO
 * @see Review
 */
public class ReviewDAO implements DAO<Review> {

    private final RestaurantDAO restaurantDAO = new RestaurantDAO();
    private final CustomerDAO CustomerDAO = new CustomerDAO();

    private Review mapReview(ResultSet rs) throws SQLException {
        UUID restaurantId = UUID.fromString(rs.getString("restaurant_id"));
        UUID customerId = UUID.fromString(rs.getString("user_id"));
        Restaurant restaurant = restaurantDAO.findById(restaurantId).orElse(null);
        User user = CustomerDAO.findById(customerId).orElse(null);
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime timestamp = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

        Review review = new Review(
                UUID.fromString(rs.getString("id")),
                restaurant,
                user,
                rs.getInt("rating"),
                timestamp,
                rs.getString("text"),
                rs.getString("response"));

        Timestamp respondedAt = rs.getTimestamp("responded_at");
        if (respondedAt != null) review.setRespondedAt(respondedAt.toLocalDateTime());

        return review;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Review> findById(UUID id) {
        String query = """
                SELECT id, restaurant_id, user_id, rating, text, response, responded_at, created_at
                FROM review
                WHERE id = ?
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setObject(1, id, Types.OTHER);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return Optional.of(mapReview(rs));

            }
        } catch (SQLException e) {
            System.err.println("Errore findById in ReviewDAO: " + e.getMessage());
        }
        return Optional.empty();
    }

    /** {@inheritDoc} */
    @Override
    public List<Review> findAll() {
        List<Review> reviews = new ArrayList<>();
        String query = """
                SELECT id, restaurant_id, user_id, rating, text, response, responded_at, created_at
                FROM review
                """;
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                reviews.add(mapReview(rs));
            }
        } catch (SQLException e) {
            System.err.println("Errore findAll in ReviewDAO: " + e.getMessage());
        }
        return reviews;
    }

    /** {@inheritDoc} */
    @Override
    public long count() {
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM review")) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            System.err.println("Errore count in ReviewDAO: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Trova tutte le recensioni associate a un determinato ristorante.
     *
     * @param restaurantId l'UUID del ristorante
     * @return una lista di recensioni per il ristorante specificato
     */
    public List<Review> findByRestaurant(UUID restaurantId) {
        List<Review> reviews = new ArrayList<>();
        String query = """
                SELECT id, restaurant_id, user_id, rating, text, response, responded_at, created_at
                FROM review
                WHERE restaurant_id = ?
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setObject(1, restaurantId, Types.OTHER);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reviews.add(mapReview(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore findByRestaurant in ReviewDAO: " + e.getMessage());
        }
        return reviews;
    }

    /**
     * Trova tutte le recensioni scritte da un determinato utente.
     *
     * @param userId l'UUID dell'utente
     * @return una lista di recensioni scritte dall'utente specificato
     */
    public List<Review> findByUser(UUID userId) {
        List<Review> reviews = new ArrayList<>();
        String query = """
                SELECT id, restaurant_id, user_id, rating, text, response, responded_at, created_at
                FROM review
                WHERE user_id = ?
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setObject(1, userId, Types.OTHER);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reviews.add(mapReview(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore findByUser in ReviewDAO: " + e.getMessage());
        }
        return reviews;
    }

    /** {@inheritDoc} */
    @Override
    public List<Review> findAll(int offset, int limit) {
        List<Review> reviews = new ArrayList<>();
        String query = """
                SELECT id, restaurant_id, user_id, rating, text, response, responded_at, created_at
                FROM review
                LIMIT ? OFFSET ?
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reviews.add(mapReview(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore findAll(offset, limit) in ReviewDAO: " + e.getMessage());
        }
        return reviews;
    }

    /** {@inheritDoc} */
    @Override
    public boolean save(Review review) {
        String query = """
                INSERT INTO review (id, restaurant_id, user_id, rating, text, response, responded_at, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setObject(1, review.getId(), Types.OTHER);
            stmt.setObject(2, review.getRestaurant() != null ? review.getRestaurant().getId() : null, Types.OTHER);
            stmt.setObject(3, review.getUser() != null ? review.getUser().getId() : null, Types.OTHER);
            stmt.setInt(4, review.getValue());
            stmt.setString(5, review.getText());
            stmt.setString(6, review.getReply());
            if (review.getRespondedAt() != null) stmt.setTimestamp(7, Timestamp.valueOf(review.getRespondedAt()));
            else stmt.setNull(7, Types.TIMESTAMP);
            if (review.getTimestamp() != null) stmt.setTimestamp(8, Timestamp.valueOf(review.getTimestamp()));
            else stmt.setNull(8, Types.TIMESTAMP);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore save in ReviewDAO: " + e.getMessage());
            return false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean update(Review review) {
        String query = """
                UPDATE review
                SET restaurant_id = ?, user_id = ?, rating = ?, text = ?, response = ?, responded_at = ?, created_at = ?
                WHERE id = ?
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setObject(1, review.getRestaurant() != null ? review.getRestaurant().getId() : null, Types.OTHER);
            stmt.setObject(2, review.getUser() != null ? review.getUser().getId() : null, Types.OTHER);
            stmt.setInt(3, review.getValue());
            stmt.setString(4, review.getText());
            stmt.setString(5, review.getReply());
            if (review.getRespondedAt() != null) stmt.setTimestamp(6, Timestamp.valueOf(review.getRespondedAt()));
            else stmt.setNull(6, Types.TIMESTAMP);
            if (review.getTimestamp() != null) stmt.setTimestamp(7, Timestamp.valueOf(review.getTimestamp()));
            else stmt.setNull(7, Types.TIMESTAMP);
            stmt.setObject(8, review.getId(), Types.OTHER);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore update in ReviewDAO: " + e.getMessage());
            return false;
        }
    }

    /**
     * Salva una risposta del proprietario a una recensione esistente.
     *
     * @param id l'UUID della recensione da aggiornare
     * @param replyToRwview il testo della risposta
     * @return {@code true} se l'aggiornamento ha avuto successo
     */
    public final boolean saveReply(UUID id, String replyToRwview) {
        final String query = """
                UPDATE review
                SET response = ?, responded_at = ?
                WHERE id = ?
                """;

        try (Connection conn = Database.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, replyToRwview);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setObject(3, id, Types.OTHER);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore saveReply in ReviewDAO: " + e.getMessage());
            return false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean delete(UUID id) {
        String query = """
                DELETE FROM review WHERE id = ?
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setObject(1, id, Types.OTHER);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore delete in ReviewDAO: " + e.getMessage());
            return false;
        }
    }
}
