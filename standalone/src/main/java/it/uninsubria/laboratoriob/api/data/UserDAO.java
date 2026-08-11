package it.uninsubria.laboratoriob.api.data;

import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.objects.User;
import it.uninsubria.laboratoriob.utils.Database;

import java.sql.*;
import java.util.*;

/**
 * Contratto it.uninsubria.laboratoriob.api.data.DAO comune ai sottotipi di {@link User} ({@code customer},
 * {@code Owner}).
 * <p>
 *
 * @param <T> sottotipo concreto di User
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
        final String query = "SELECT id, username, psw_hash, psw_salt, first_name, last_name, latitude, longitude, birth_date FROM \"user\" where id=? AND is_owner = "+isOwner;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.printf("Errore findById in %sDAO: %s", this.getClass().getCanonicalName(),e.getMessage());
        }

        return Optional.empty();
    }

    public Optional<T> findByUsername(String username) {
        final String query = "SELECT id, psw_hash, psw_salt, first_name, last_name, latitude, longitude, birth_date FROM \"user\" where username=? AND is_owner = "+isOwner;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.printf("Errore findByUsername in %sDAO: %s", this.getClass().getCanonicalName(),e.getMessage());
        }

        return Optional.empty();
    }

    public List<T> findAll() {
        List<T> users = new ArrayList<>();

        final String query = "SELECT id, username, psw_hash, psw_salt, first_name,last_name, latitude, longitude, birth_date FROM \"user\" where is_owner = "+isOwner;

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                T user = mapRow(rs);
                users.add(user);
            }

        } catch (SQLException e) {
            System.err.printf("Errore findAll in %sDAO: %s", this.getClass().getCanonicalName(),e.getMessage());
        }

        return users;
    }

    public boolean save(T user) {
        Location loc = user.getLocation();

        String query = "INSERT INTO \"user\" (id, username, psw_hash, psw_salt, first_name, last_name, latitude,longitude, is_owner) VALUES (?, ?, ?, ?, ?, ?, ?, ?,?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, user.getId().toString());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getPasswordHash());
            stmt.setString(4, user.getPasswordSalt());
            stmt.setString(5, user.getName());
            stmt.setString(6, user.getLastName());
            stmt.setDouble(7, loc.getLatitude());
            stmt.setDouble(7, loc.getLongitude());
            stmt.setString(8, user.getDateOfBirth().toString());
            stmt.setBoolean(9,isOwner);

            if (user.getLocation() != null) {
                // TODO: Aggiungere verifica se esiste una posizione uguale per evitare duplicati nella tabella di lookup
                locationDAO.save(user.getLocation());
            }

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.printf("Errore save in %sDAO: %s", this.getClass().getCanonicalName(),e.getMessage());
        }

        return false;
    }

    @Override
    public boolean update(T user) {
        final String query = "UPDATE \"user\" SET username=?, password_hash=?, salt=?, name=?, last_name=?, location_id=?, date_of_birth=?, WHERE id=? AND is_owner ="+isOwner;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getPasswordSalt());
            stmt.setString(4, user.getName());
            stmt.setString(5, user.getLastName());
            stmt.setString(6, user.getLocation() != null ? user.getLocation().getId().toString() : null);
            stmt.setString(7, user.getDateOfBirth().toString());
            stmt.setString(8, user.getId().toString());

            if (user.getLocation() != null)
                locationDAO.save(user.getLocation());


            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.printf("Errore update in %sDAO: %s", this.getClass().getCanonicalName(),e.getMessage());
            return false;
        }
    }

    public boolean delete(UUID id) {
        String query = "DELETE FROM \"user\" WHERE id=? AND is_owner="+isOwner;


        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, id.toString());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.printf("Errore delete in %sDAO: %s", this.getClass().getCanonicalName(),e.getMessage());
            return false;
        }
    }


    protected Set<UUID> findSpecial(UUID userId) {
        String query = "SELECT restaurant_id FROM "+ ((isOwner) ? "user_restaurants" : "user_favorites") +" WHERE user_id=?";

        Set<UUID> favourites = new HashSet<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, userId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next())
                    favourites.add(UUID.fromString(rs.getString("restaurant_id")));
            }

        } catch (SQLException e) {
            System.err.printf("Errore find%s in %sDAO: %s",(isOwner) ? "Restaurants" : "Favourites", this.getClass().getCanonicalName(), e.getMessage());
        }

        return favourites;
    }

    protected boolean addSpecial(UUID userId, UUID restaurantId) {
        String query = "INSERT INTO" + ((isOwner) ? "user_restaurants" : "user_favorites") +"VALUES (?,?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, userId.toString());
            stmt.setString(2, restaurantId.toString());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.printf("Errore add%s in %sDAO: %s",(isOwner) ? "Restaurants" : "Favourites", this.getClass().getCanonicalName(), e.getMessage());
            return false;
        }
    }


    protected boolean removeSpecial(UUID customerId, UUID restaurantId) {
        String query = "DELETE FROM " + ((isOwner) ? "user_restaurants" : "user_favorites") + " WHERE restaurant_id=? AND user_id=?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, restaurantId.toString());
            stmt.setString(2, customerId.toString());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.printf("Errore remove%s in %sDAO: %s",(isOwner) ? "Restaurants" : "Favourites", this.getClass().getCanonicalName(), e.getMessage());
            return false;
        }
    }
}
