package it.uninsubria.laboratoriob.data;

import it.uninsubria.laboratoriob.DAO;
import it.uninsubria.laboratoriob.objects.Restaurant;
import it.uninsubria.laboratoriob.objects.Review;
import it.uninsubria.laboratoriob.objects.User;
import it.uninsubria.laboratoriob.utils.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ReviewDAO implements DAO<Review> {

    private final RestaurantDAO restaurantDAO = new RestaurantDAO();
    private final ClientDAO clientDAO = new ClientDAO();

    @Override
    public Optional<Review> findById(UUID id) {
        // SELECT id, restaurant_id, client_id, rating, timestamp, comment, reply FROM
        // review WHERE id = ?
        String query = "PLACEHOLDER";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, id.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UUID restaurantId = UUID.fromString(rs.getString("PLACEHOLDER"));
                    UUID clientId = UUID.fromString(rs.getString("PLACEHOLDER"));
                    Restaurant restaurant = restaurantDAO.findById(restaurantId).orElse(null);
                    User user = clientDAO.findById(clientId).orElse(null);
                    String tsStr = rs.getString("PLACEHOLDER");
                    LocalDateTime timestamp = tsStr != null ? LocalDateTime.parse(tsStr) : LocalDateTime.now();

                    Review review = new Review(
                            // Column Key Placeholder: id
                            UUID.fromString(rs.getString("PLACEHOLDER")),
                            restaurant,
                            user,
                            // Column Placeholder: rating
                            rs.getInt("PLACEHOLDER"),
                            timestamp,
                            // Column Placeholder: comment
                            rs.getString("PLACEHOLDER"),
                            // Column Placeholder: reply
                            rs.getString("PLACEHOLDER"));
                    return Optional.of(review);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore findById in ReviewDAO: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Review> findAll() {
        List<Review> reviews = new ArrayList<>();
        // SELECT id, restaurant_id, client_id, rating, timestamp, comment, reply FROM
        // review
        String query = "PLACEHOLDER";
        try (Connection conn = Database.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                UUID restaurantId = UUID.fromString(rs.getString("PLACEHOLDER"));
                UUID clientId = UUID.fromString(rs.getString("PLACEHOLDER"));
                Restaurant restaurant = restaurantDAO.findById(restaurantId).orElse(null);
                User user = clientDAO.findById(clientId).orElse(null);
                String tsStr = rs.getString("PLACEHOLDER");
                LocalDateTime timestamp = tsStr != null ? LocalDateTime.parse(tsStr) : LocalDateTime.now();

                Review review = new Review(
                        UUID.fromString(rs.getString("PLACEHOLDER")),
                        restaurant,
                        user,
                        rs.getInt("PLACEHOLDER"),
                        timestamp,
                        rs.getString("PLACEHOLDER"),
                        rs.getString("PLACEHOLDER"));
                reviews.add(review);
            }
        } catch (SQLException e) {
            System.err.println("Errore findAll in ReviewDAO: " + e.getMessage());
        }
        return reviews;
    }

    public List<Review> findByRestaurant(UUID restaurantId) {
        List<Review> reviews = new ArrayList<>();
        // SELECT id, restaurant_id, client_id, rating, timestamp, comment, reply FROM
        // review WHERE restaurant_id = ?
        String query = "PLACEHOLDER";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, restaurantId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID restId = UUID.fromString(rs.getString("PLACEHOLDER"));
                    UUID clientId = UUID.fromString(rs.getString("PLACEHOLDER"));
                    Restaurant restaurant = restaurantDAO.findById(restId).orElse(null);
                    User user = clientDAO.findById(clientId).orElse(null);
                    String tsStr = rs.getString("PLACEHOLDER");
                    LocalDateTime timestamp = tsStr != null ? LocalDateTime.parse(tsStr) : LocalDateTime.now();

                    Review review = new Review(
                            UUID.fromString(rs.getString("PLACEHOLDER")),
                            restaurant,
                            user,
                            rs.getInt("PLACEHOLDER"),
                            timestamp,
                            rs.getString("PLACEHOLDER"),
                            rs.getString("PLACEHOLDER"));
                    reviews.add(review);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore findByRestaurant in ReviewDAO: " + e.getMessage());
        }
        return reviews;
    }

    @Override
    public boolean save(Review review) {
        // INSERT INTO review (id, restaurant_id, client_id, rating, timestamp, comment,
        // reply) VALUES (?, ?, ?, ?, ?, ?, ?)
        String query = "PLACEHOLDER";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, review.getId().toString());
            stmt.setString(2, review.getRestaurant() != null ? review.getRestaurant().getId().toString() : null);
            stmt.setString(3, review.getUser() != null ? review.getUser().getId().toString() : null);
            stmt.setInt(4, review.getValue());
            stmt.setString(5, review.getTimestamp() != null ? review.getTimestamp().toString() : null);
            stmt.setString(6, review.getText());
            stmt.setString(7, review.getReply());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore save in ReviewDAO: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(Review review) {
        return save(review);
    }

    @Override
    public boolean delete(UUID id) {
        // DELETE FROM review WHERE id = ?
        String query = "PLACEHOLDER";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, id.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore delete in ReviewDAO: " + e.getMessage());
            return false;
        }
    }
}
