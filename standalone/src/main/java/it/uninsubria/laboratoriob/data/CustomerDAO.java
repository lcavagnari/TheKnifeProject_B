package it.uninsubria.laboratoriob.data;

import it.uninsubria.laboratoriob.objects.Customer;
import it.uninsubria.laboratoriob.objects.Location;
import it.uninsubria.laboratoriob.utils.Database;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * it.uninsubria.laboratoriob.data.DAO per l'entità {@link Customer}.
 * <p>
 */
public class CustomerDAO implements UserDAO<Customer> {

    private final LocationDAO locationDAO = new LocationDAO();

    @Override
    public Optional<Customer> findById(UUID uId) {
        // SELECT id, username, password_hash, salt, name, last_name, location_id,
        // date_of_birth FROM customer WHERE id = ?
        String query = "SELECT username, psw_hash, psw_salt, first_name,last_name, latitude,longitude, birth_date FROM \"user\" where id=? AND is_owner = false";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UUID locationId = UUID.fromString(rs.getString("..."));
                    Location location = locationDAO.findById(locationId).orElse(null);
                    Set<UUID> favourites = findFavourites(uId);

                    Customer customer = new Customer(
                            uId,
                            // username
                            rs.getString("..."),
                            // password_hash
                            rs.getString("..."),
                            // salt
                            rs.getString("..."),
                            // name
                            rs.getString("..."),
                            // last_name
                            rs.getString("..."),
                            location,
                            // date_of_birth
                            LocalDate.parse(rs.getString("...")),
                            favourites);
                    return Optional.of(customer);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore findById in CustomerDAO: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Customer> findByUsername(String username) {
        // SELECT id, username, password_hash, salt, name, last_name, location_id,
        // date_of_birth FROM customer WHERE username = ?
        String query = "SELECT id, psw_hash, psw_salt, first_name,last_name, latitude,longitude, birth_date FROM \"user\" where username=? AND is_owner = false";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UUID id = UUID.fromString(rs.getString("..."));
                    UUID locationId = UUID.fromString(rs.getString("..."));
                    Location location = locationDAO.findById(locationId).orElse(null);
                    Set<UUID> favourites = findFavourites(id);

                    Customer customer = new Customer(
                            id,
                            rs.getString("..."),
                            rs.getString("..."),
                            rs.getString("..."),
                            rs.getString("..."),
                            rs.getString("..."),
                            location,
                            LocalDate.parse(rs.getString("...")),
                            favourites);
                    return Optional.of(customer);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore findByUsername in CustomerDAO: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Customer> findAll() {
        List<Customer> customers = new ArrayList<>();
        // SELECT id, username, password_hash, salt, name, last_name, location_id,
        // date_of_birth FROM customer
        String query = "SELECT username, psw_hash, psw_salt, first_name,last_name, latitude, longitude, birth_date FROM \"user\" where is_owner = false";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                UUID id = UUID.fromString(rs.getString("..."));
                UUID locationId = UUID.fromString(rs.getString("..."));
                Location location = locationDAO.findById(locationId).orElse(null);
                Set<UUID> favourites = findFavourites(id);

                Customer customer = new Customer(
                        id,
                        rs.getString("..."),
                        rs.getString("..."),
                        rs.getString("..."),
                        rs.getString("..."),
                        rs.getString("..."),
                        location,
                        LocalDate.parse(rs.getString("...")),
                        favourites);
                customers.add(customer);
            }
        } catch (SQLException e) {
            System.err.println("Errore findAll in CustomerDAO: " + e.getMessage());
        }
        return customers;
    }

    @Override
    public boolean save(Customer customer) {
        Location loc = customer.getLocation();

        String query = "INSERT INTO \"user\" (id, username, psw_hash, psw_salt, first_name, last_name, latitude,longitude,is_owner) VALUES (?, ?, ?, ?, ?, ?, ?, ?,?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, customer.getId().toString());
            stmt.setString(2, customer.getUsername());
            stmt.setString(3, customer.getPasswordHash());
            stmt.setString(4, customer.getPasswordSalt());
            stmt.setString(5, customer.getName());
            stmt.setString(6, customer.getLastName());
            stmt.setDouble(7,loc.getLatitude());
            stmt.setDouble(7,loc.getLongitude());
            stmt.setString(8, customer.getDateOfBirth().toString());

            if (customer.getLocation() != null) {
                // TODO: Aggiungere verifica se esiste una posizione uguale per evitare duplicati nella tabella di lookup
                locationDAO.save(customer.getLocation());
            }

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                // DELETE FROM customer_favourite_restaurants WHERE customer_id = ?
                String deleteFavs = "...";
                try (PreparedStatement delStmt = conn.prepareStatement(deleteFavs)) {
                    delStmt.setString(1, customer.getId().toString());
                    delStmt.executeUpdate();
                }
                for (UUID restId : customer.getFavouriteRestourants()) {
                    addFavourite(customer.getId(), restId);
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Errore save in CustomerDAO: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(Customer customer) {
        return save(customer);
    }

    @Override
    public boolean delete(UUID id) {
        // DELETE FROM customer WHERE id = ?
        String query = "...";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, id.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore delete in CustomerDAO: " + e.getMessage());
            return false;
        }
    }


    public Set<UUID> findFavourites(UUID customerId) {
        Set<UUID> favourites = new HashSet<>();
        // SELECT restaurant_id FROM customer_favourite_restaurants WHERE customer_id = ?
        String query = "...";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, customerId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    favourites.add(UUID.fromString(rs.getString("...")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore findFavourites in CustomerDAO: " + e.getMessage());
        }
        return favourites;
    }


    public boolean addFavourite(UUID customerId, UUID restaurantId) {
        // INSERT INTO customer_favourite_restaurants (customer_id, restaurant_id) VALUES
        // (?, ?)
        String query = "...";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, customerId.toString());
            stmt.setString(2, restaurantId.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore addFavourite in CustomerDAO: " + e.getMessage());
            return false;
        }
    }


    public boolean removeFavourite(UUID customerId, UUID restaurantId) {
        // DELETE FROM customer_favourite_restaurants WHERE customer_id = ? AND
        // restaurant_id = ?
        String query = "...";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, customerId.toString());
            stmt.setString(2, restaurantId.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore removeFavourite in CustomerDAO: " + e.getMessage());
            return false;
        }
    }
}
