package it.uninsubria.laboratoriob.data;

import it.uninsubria.laboratoriob.objects.Client;
import it.uninsubria.laboratoriob.objects.Location;
import it.uninsubria.laboratoriob.utils.Database;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * it.uninsubria.laboratoriob.DAO per l'entità {@link Client}.
 * <p>
 */
public class ClientDAO implements UserDAO<Client> {

    private final LocationDAO locationDAO = new LocationDAO();

    @Override
    public Optional<Client> findById(UUID id) {
        // SELECT id, username, password_hash, salt, name, last_name, location_id,
        // date_of_birth FROM client WHERE id = ?
        String query = "PLACEHOLDER";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, id.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UUID locationId = UUID.fromString(rs.getString("PLACEHOLDER"));
                    Location location = locationDAO.findById(locationId).orElse(null);
                    Set<UUID> favourites = findFavourites(id);

                    Client client = new Client(
                            id,
                            // Column Placeholder: username
                            rs.getString("PLACEHOLDER"),
                            // Column Placeholder: password_hash
                            rs.getString("PLACEHOLDER"),
                            // Column Placeholder: salt
                            rs.getString("PLACEHOLDER"),
                            // Column Placeholder: name
                            rs.getString("PLACEHOLDER"),
                            // Column Placeholder: last_name
                            rs.getString("PLACEHOLDER"),
                            location,
                            // Column Placeholder: date_of_birth
                            LocalDate.parse(rs.getString("PLACEHOLDER")),
                            favourites);
                    return Optional.of(client);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore findById in ClientDAO: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Client> findByUsername(String username) {
        // SELECT id, username, password_hash, salt, name, last_name, location_id,
        // date_of_birth FROM client WHERE username = ?
        String query = "PLACEHOLDER";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UUID id = UUID.fromString(rs.getString("PLACEHOLDER"));
                    UUID locationId = UUID.fromString(rs.getString("PLACEHOLDER"));
                    Location location = locationDAO.findById(locationId).orElse(null);
                    Set<UUID> favourites = findFavourites(id);

                    Client client = new Client(
                            id,
                            rs.getString("PLACEHOLDER"),
                            rs.getString("PLACEHOLDER"),
                            rs.getString("PLACEHOLDER"),
                            rs.getString("PLACEHOLDER"),
                            rs.getString("PLACEHOLDER"),
                            location,
                            LocalDate.parse(rs.getString("PLACEHOLDER")),
                            favourites);
                    return Optional.of(client);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore findByUsername in ClientDAO: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Client> findAll() {
        List<Client> clients = new ArrayList<>();
        // SELECT id, username, password_hash, salt, name, last_name, location_id,
        // date_of_birth FROM client
        String query = "PLACEHOLDER";
        try (Connection conn = Database.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                UUID id = UUID.fromString(rs.getString("PLACEHOLDER"));
                UUID locationId = UUID.fromString(rs.getString("PLACEHOLDER"));
                Location location = locationDAO.findById(locationId).orElse(null);
                Set<UUID> favourites = findFavourites(id);

                Client client = new Client(
                        id,
                        rs.getString("PLACEHOLDER"),
                        rs.getString("PLACEHOLDER"),
                        rs.getString("PLACEHOLDER"),
                        rs.getString("PLACEHOLDER"),
                        rs.getString("PLACEHOLDER"),
                        location,
                        LocalDate.parse(rs.getString("PLACEHOLDER")),
                        favourites);
                clients.add(client);
            }
        } catch (SQLException e) {
            System.err.println("Errore findAll in ClientDAO: " + e.getMessage());
        }
        return clients;
    }

    @Override
    public boolean save(Client client) {
        // INSERT INTO client (id, username, password_hash, salt, name, last_name,
        // location_id, date_of_birth) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        String query = "PLACEHOLDER";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, client.getId().toString());
            stmt.setString(2, client.getUsername());
            stmt.setString(3, client.getPasswordHash());
            stmt.setString(4, client.getPasswordSalt());
            stmt.setString(5, client.getName());
            stmt.setString(6, client.getLastName());
            stmt.setString(7, client.getLocation() != null ? client.getLocation().getId().toString() : null);
            stmt.setString(8, client.getDateOfBirth().toString());

            if (client.getLocation() != null) {
                locationDAO.save(client.getLocation());
            }

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                // DELETE FROM client_favourite_restaurants WHERE client_id = ?
                String deleteFavs = "PLACEHOLDER";
                try (PreparedStatement delStmt = conn.prepareStatement(deleteFavs)) {
                    delStmt.setString(1, client.getId().toString());
                    delStmt.executeUpdate();
                }
                for (UUID restId : client.getFavouriteRestourants()) {
                    addFavourite(client.getId(), restId);
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Errore save in ClientDAO: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(Client client) {
        return save(client);
    }

    @Override
    public boolean delete(UUID id) {
        // DELETE FROM client WHERE id = ?
        String query = "PLACEHOLDER";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, id.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore delete in ClientDAO: " + e.getMessage());
            return false;
        }
    }


    public Set<UUID> findFavourites(UUID clientId) {
        Set<UUID> favourites = new HashSet<>();
        // SELECT restaurant_id FROM client_favourite_restaurants WHERE client_id = ?
        String query = "PLACEHOLDER";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, clientId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    favourites.add(UUID.fromString(rs.getString("PLACEHOLDER")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore findFavourites in ClientDAO: " + e.getMessage());
        }
        return favourites;
    }


    public boolean addFavourite(UUID clientId, UUID restaurantId) {
        // INSERT INTO client_favourite_restaurants (client_id, restaurant_id) VALUES
        // (?, ?)
        String query = "PLACEHOLDER";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, clientId.toString());
            stmt.setString(2, restaurantId.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore addFavourite in ClientDAO: " + e.getMessage());
            return false;
        }
    }


    public boolean removeFavourite(UUID clientId, UUID restaurantId) {
        // DELETE FROM client_favourite_restaurants WHERE client_id = ? AND
        // restaurant_id = ?
        String query = "PLACEHOLDER";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, clientId.toString());
            stmt.setString(2, restaurantId.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore removeFavourite in ClientDAO: " + e.getMessage());
            return false;
        }
    }
}
