package it.uninsubria.laboratoriob.server.data;

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
        String tsStr = rs.getString("created_at");
        LocalDateTime timestamp = tsStr != null ? LocalDateTime.parse(tsStr) : LocalDateTime.now();

        return new Review(
                UUID.fromString(rs.getString("id")),
                restaurant,
                user,
                rs.getInt("rating"),
                timestamp,
                rs.getString("text"),
                rs.getString("response"));
    }

    @Override
    public Optional<Review> findById(UUID id) {
        String query = """
                SELECT id, restaurant_id, user_id, rating, text, response, created_at
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

    @Override
    public List<Review> findAll() {
        List<Review> reviews = new ArrayList<>();
        String query = """
                SELECT id, restaurant_id, user_id, rating, text, response, created_at
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

    public List<Review> findByRestaurant(UUID restaurantId) {
        List<Review> reviews = new ArrayList<>();
        String query = """
                SELECT id, restaurant_id, user_id, rating, text, response, created_at
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

    public List<Review> findByUser(UUID userId) {
        List<Review> reviews = new ArrayList<>();
        String query = """
                SELECT id, restaurant_id, user_id, rating, text, response, created_at
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

    @Override
    public List<Review> findAll(int offset, int limit) {
        List<Review> reviews = new ArrayList<>();
        String query = """
                SELECT id, restaurant_id, user_id, rating, text, response, created_at
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

    @Override
    public boolean save(Review review) {
        String query = """
                INSERT INTO review (id, restaurant_id, user_id, rating, text, response, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setObject(1, review.getId(), Types.OTHER);
            stmt.setObject(2, review.getRestaurant() != null ? review.getRestaurant().getId() : null, Types.OTHER);
            stmt.setObject(3, review.getUser() != null ? review.getUser().getId() : null, Types.OTHER);
            stmt.setInt(4, review.getValue());
            stmt.setString(5, review.getText());
            stmt.setString(6, review.getReply());
            stmt.setString(7, review.getTimestamp() != null ? review.getTimestamp().toString() : null);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore save in ReviewDAO: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(Review review) {
        String query = """
                UPDATE review
                SET restaurant_id = ?, user_id = ?, rating = ?, text = ?, response = ?, created_at = ?
                WHERE id = ?
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setObject(1, review.getRestaurant() != null ? review.getRestaurant().getId() : null, Types.OTHER);
            stmt.setObject(2, review.getUser() != null ? review.getUser().getId() : null, Types.OTHER);
            stmt.setInt(3, review.getValue());
            stmt.setString(4, review.getText());
            stmt.setString(5, review.getReply());
            stmt.setString(6, review.getTimestamp() != null ? review.getTimestamp().toString() : null);
            stmt.setObject(7, review.getId(), Types.OTHER);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore update in ReviewDAO: " + e.getMessage());
            return false;
        }
    }

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
