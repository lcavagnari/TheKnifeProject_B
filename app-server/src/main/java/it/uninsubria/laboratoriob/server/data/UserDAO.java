package it.uninsubria.laboratoriob.server.data;

import it.uninsubria.laboratoriob.api.data.DAO;
import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.objects.User;
import it.uninsubria.laboratoriob.server.utils.Database;

import java.sql.*;
import java.util.*;

/**
 * Classe astratta base per i DAO che gestiscono gli utenti ({@link it.uninsubria.laboratoriob.api.objects.Customer}, {@link it.uninsubria.laboratoriob.api.objects.Owner}).
 * <p>
 * Fornisce le operazioni CRUD comuni a tutti i tipi di utente, gestendo la tabella
 * {@code "user"} nel database PostgreSQL. Utilizza un flag {@code isOwner} per distinguere
 * tra clienti e proprietari nella stessa tabella.
 * </p>
 *
 * <h2>Responsabilità</h2>
 * <ul>
 *   <li>Mappatura delle righe del ResultSet in sottotipi concreti di {@link User}.</li>
 *   <li>Gestione delle operazioni di ricerca per ID e username.</li>
 *   <li>Gestione delle associazioni many-to-many (preferiti o ristoranti posseduti).</li>
 *   <li>Persistenza delle location associate agli utenti.</li>
 * </ul>
 *
 * @param <T> sottotipo concreto di User gestito da questo DAO
 * @author Luca Cavagnari
 * @version 2.0
 * @see DAO
 * @see CustomerDAO
 * @see OwnerDAO
 */
public abstract class UserDAO<T extends User> implements DAO<T> {

    protected final LocationDAO locationDAO;
    protected final RestaurantDAO restaurantDAO;
    private final boolean isOwner;

    public UserDAO(boolean isOwner) {
        this.locationDAO = new LocationDAO();
        this.restaurantDAO = new RestaurantDAO();

        this.isOwner = isOwner;
    }

    protected abstract T mapRow(ResultSet rs) throws SQLException;

    public Optional<T> findById(UUID uId) {
        final String query = "SELECT id, username, psw_hash, psw_salt, first_name, last_name, latitude, longitude, birth_date, is_system FROM \"user\" where id=? AND is_owner = " + isOwner;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setObject(1, uId, Types.OTHER);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.printf("Errore findById in %sDAO: %s", this.getClass().getCanonicalName(), e.getMessage());
        }

        return Optional.empty();
    }

    public Optional<T> findByUsername(String username) {
        final String query = "SELECT id, psw_hash, psw_salt, first_name, last_name, latitude, longitude, birth_date, is_system FROM \"user\" where username=? AND is_owner = " + isOwner;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.printf("Errore findByUsername in %sDAO: %s", this.getClass().getCanonicalName(), e.getMessage());
        }

        return Optional.empty();
    }

    public List<T> findAll() {
        List<T> users = new ArrayList<>();

        final String query = "SELECT id, username, psw_hash, psw_salt, first_name,last_name, latitude, longitude, birth_date, is_system FROM \"user\" where is_owner = " + isOwner;

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                T user = mapRow(rs);
                users.add(user);
            }

        } catch (SQLException e) {
            System.err.printf("Errore findAll in %sDAO: %s", this.getClass().getCanonicalName(), e.getMessage());
        }

        return users;
    }

    public List<T> findAll(int offset, int limit) {
        List<T> users = new ArrayList<>();

        final String query = "SELECT id, username, psw_hash, psw_salt, first_name,last_name, latitude, longitude, birth_date, is_system FROM \"user\" where is_owner = " + isOwner + " OFFSET ? LIMIT ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, offset);
            stmt.setInt(2, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    T user = mapRow(rs);
                    users.add(user);
                }
            }

        } catch (SQLException e) {
            System.err.printf("Errore findAll(offset,limit) in %sDAO: %s", this.getClass().getCanonicalName(), e.getMessage());
        }

        return users;
    }

    public long count() {
        final String query = "SELECT COUNT(*) FROM \"user\" where is_owner = " + isOwner;

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) return rs.getLong(1);

        } catch (SQLException e) {
            System.err.printf("Errore count in %sDAO: %s", this.getClass().getCanonicalName(), e.getMessage());
        }

        return 0;
    }

    public boolean save(T user) {
        Location loc = user.getLocation();

        String query = "INSERT INTO \"user\" (id, username, psw_hash, psw_salt, first_name, last_name, birth_date, latitude, longitude, is_owner, is_system) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (id) DO NOTHING";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setObject(1, user.getId(), Types.OTHER);
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getPasswordHash());
            stmt.setString(4, user.getPasswordSalt());
            stmt.setString(5, user.getName());
            stmt.setString(6, user.getLastName());
            stmt.setDate(7, java.sql.Date.valueOf(user.getDateOfBirth()));
            if (loc != null) {
                stmt.setDouble(8, loc.getLatitude());
                stmt.setDouble(9, loc.getLongitude());
            } else {
                stmt.setNull(8, Types.DOUBLE);
                stmt.setNull(9, Types.DOUBLE);
            }
            stmt.setBoolean(10, isOwner);
            stmt.setBoolean(11, user.isSystem());

            if (user.getLocation() != null) {
                locationDAO.save(user.getLocation());
            }

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.printf("Errore save in %sDAO: %s", this.getClass().getCanonicalName(), e.getMessage());
        }

        return false;
    }

    @Override
    public boolean update(T user) {
        final String query = "UPDATE \"user\" SET username=?, psw_hash=?, psw_salt=?, first_name=?, last_name=?, birth_date=?, latitude=?, longitude=?, is_system=? WHERE id=? AND is_owner=" + isOwner;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getPasswordSalt());
            stmt.setString(4, user.getName());
            stmt.setString(5, user.getLastName());
            stmt.setDate(6, java.sql.Date.valueOf(user.getDateOfBirth()));
            if (user.getLocation() != null) {
                stmt.setDouble(7, user.getLocation().getLatitude());
                stmt.setDouble(8, user.getLocation().getLongitude());
            } else {
                stmt.setNull(7, Types.DOUBLE);
                stmt.setNull(8, Types.DOUBLE);
            }
            stmt.setBoolean(9, user.isSystem());
            stmt.setObject(10, user.getId(), Types.OTHER);

            if (user.getLocation() != null)
                locationDAO.save(user.getLocation());


            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.printf("Errore update in %sDAO: %s", this.getClass().getCanonicalName(), e.getMessage());
            return false;
        }
    }

    public boolean delete(UUID id) {
        String query = "DELETE FROM \"user\" WHERE id=? AND is_owner=" + isOwner;


        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setObject(1, id, Types.OTHER);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.printf("Errore delete in %sDAO: %s", this.getClass().getCanonicalName(), e.getMessage());
            return false;
        }
    }


    protected Set<UUID> findSpecial(UUID userId) {
        String query = "SELECT restaurant_id FROM " + ((isOwner) ? "user_restaurants" : "user_favorites") + " WHERE user_id=?";

        Set<UUID> favourites = new HashSet<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setObject(1, userId, Types.OTHER);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next())
                    favourites.add(UUID.fromString(rs.getString("restaurant_id")));
            }

        } catch (SQLException e) {
            System.err.printf("Errore find%s in %sDAO: %s", (isOwner) ? "Restaurants" : "Favourites", this.getClass().getCanonicalName(), e.getMessage());
        }

        return favourites;
    }

    protected boolean addSpecial(UUID userId, UUID restaurantId) {
        String query = "INSERT INTO " + ((isOwner) ? "user_restaurants" : "user_favorites") + " VALUES (?,?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setObject(1, userId, Types.OTHER);
            stmt.setObject(2, restaurantId, Types.OTHER);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.printf("Errore add%s in %sDAO: %s", (isOwner) ? "Restaurants" : "Favourites", this.getClass().getCanonicalName(), e.getMessage());
            return false;
        }
    }


    protected boolean removeSpecial(UUID customerId, UUID restaurantId) {
        String query = "DELETE FROM " + ((isOwner) ? "user_restaurants" : "user_favorites") + " WHERE restaurant_id=? AND user_id=?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setObject(1, restaurantId, Types.OTHER);
            stmt.setObject(2, customerId, Types.OTHER);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.printf("Errore remove%s in %sDAO: %s", (isOwner) ? "Restaurants" : "Favourites", this.getClass().getCanonicalName(), e.getMessage());
            return false;
        }
    }
}
