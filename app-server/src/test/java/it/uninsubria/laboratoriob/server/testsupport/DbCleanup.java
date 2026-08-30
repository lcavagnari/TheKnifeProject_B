package it.uninsubria.laboratoriob.server.testsupport;

import it.uninsubria.laboratoriob.server.utils.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

/**
 * Raw-JDBC teardown helpers for integration tests that run against the real
 * docker-composed Postgres. Deletes child rows before parents to respect FK
 * constraints (none of the relevant tables cascade on delete).
 */
public final class DbCleanup {

    private DbCleanup() {}

    /**
     * Elimina un ristorante e le tabelle figlie (review, cuisine, services, restaurants, favorites).
     *
     * @param restaurantId UUID del ristorante da eliminare
     */
    public static void deleteRestaurant(UUID restaurantId) {
        exec("DELETE FROM review WHERE restaurant_id=?", restaurantId);
        exec("DELETE FROM restaurant_cuisine WHERE restaurant_id=?", restaurantId);
        exec("DELETE FROM restaurant_services WHERE restaurant_id=?", restaurantId);
        exec("DELETE FROM user_restaurants WHERE restaurant_id=?", restaurantId);
        exec("DELETE FROM user_favorites WHERE restaurant_id=?", restaurantId);
        exec("DELETE FROM restaurant WHERE id=?", restaurantId);
    }

    /**
     * Elimina un utente e le tabelle figlie (favorites, restaurants, review).
     *
     * @param userId UUID dell'utente da eliminare
     */
    public static void deleteUser(UUID userId) {
        exec("DELETE FROM user_favorites WHERE user_id=?", userId);
        exec("DELETE FROM user_restaurants WHERE user_id=?", userId);
        exec("DELETE FROM review WHERE user_id=?", userId);
        exec("DELETE FROM \"user\" WHERE id=?", userId);
    }

    /**
     * Elimina una recensione per ID.
     *
     * @param reviewId UUID della recensione da eliminare
     */
    public static void deleteReview(UUID reviewId) {
        exec("DELETE FROM review WHERE id=?", reviewId);
    }

    /**
     * Elimina una posizione per coordinate geografiche.
     *
     * @param lat latitudine
     * @param lon longitudine
     */
    public static void deleteLocation(double lat, double lon) {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM location WHERE latitude=? AND longitude=?")) {
            stmt.setDouble(1, lat);
            stmt.setDouble(2, lon);
            stmt.executeUpdate();
        } catch (SQLException ignored) {
            // best-effort cleanup only
        }
    }

    private static void exec(String sql, UUID id) {
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id, Types.OTHER);
            stmt.executeUpdate();
        } catch (SQLException ignored) {
            // best-effort cleanup only
        }
    }
}
