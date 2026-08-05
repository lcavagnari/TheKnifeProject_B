package it.uninsubria.laboratoriob.data;

import it.uninsubria.laboratoriob.objects.Location;
import it.uninsubria.laboratoriob.objects.Owner;
import it.uninsubria.laboratoriob.objects.Restaurant;
import it.uninsubria.laboratoriob.utils.Database;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class OwnerDAO implements UserDAO<Owner> {

    private final LocationDAO locationDAO = new LocationDAO();
    private final RestaurantDAO restaurantDAO = new RestaurantDAO();

    @Override
    public Optional<Owner> findById(UUID id) {
        // SELECT id, username, password_hash, salt, name, last_name, location_id,
        // date_of_birth FROM owner WHERE id = ?
        String query = "PLACEHOLDER";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, id.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UUID locationId = rs.getString("PLACEHOLDER") != null ? UUID.fromString(rs.getString("PLACEHOLDER"))
                            : null;
                    Location location = locationId != null ? locationDAO.findById(locationId).orElse(null) : null;
                    Set<Restaurant> restaurants = new HashSet<>(restaurantDAO.findByOwner(id));

                    Owner owner = new Owner(
                            // Column Key Placeholder: id
                            UUID.fromString(rs.getString("PLACEHOLDER")),
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
                            restaurants);
                    return Optional.of(owner);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore findById in OwnerDAO: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Owner> findByUsername(String username) {
        // SELECT id, username, password_hash, salt, name, last_name, location_id,
        // date_of_birth FROM owner WHERE username = ?
        String query = "PLACEHOLDER";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UUID id = UUID.fromString(rs.getString("PLACEHOLDER"));
                    UUID locationId = rs.getString("PLACEHOLDER") != null ? UUID.fromString(rs.getString("PLACEHOLDER"))
                            : null;
                    Location location = locationId != null ? locationDAO.findById(locationId).orElse(null) : null;
                    Set<Restaurant> restaurants = new HashSet<>(restaurantDAO.findByOwner(id));

                    Owner owner = new Owner(
                            id,
                            rs.getString("PLACEHOLDER"),
                            rs.getString("PLACEHOLDER"),
                            rs.getString("PLACEHOLDER"),
                            rs.getString("PLACEHOLDER"),
                            rs.getString("PLACEHOLDER"),
                            location,
                            LocalDate.parse(rs.getString("PLACEHOLDER")),
                            restaurants);
                    return Optional.of(owner);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore findByUsername in OwnerDAO: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Owner> findAll() {
        List<Owner> owners = new ArrayList<>();
        // SELECT id, username, password_hash, salt, name, last_name, location_id,
        // date_of_birth FROM owner
        String query = "PLACEHOLDER";
        try (Connection conn = Database.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                UUID id = UUID.fromString(rs.getString("PLACEHOLDER"));
                UUID locationId = rs.getString("PLACEHOLDER") != null ? UUID.fromString(rs.getString("PLACEHOLDER"))
                        : null;
                Location location = locationId != null ? locationDAO.findById(locationId).orElse(null) : null;
                Set<Restaurant> restaurants = new HashSet<>(restaurantDAO.findByOwner(id));

                Owner owner = new Owner(
                        id,
                        rs.getString("PLACEHOLDER"),
                        rs.getString("PLACEHOLDER"),
                        rs.getString("PLACEHOLDER"),
                        rs.getString("PLACEHOLDER"),
                        rs.getString("PLACEHOLDER"),
                        location,
                        LocalDate.parse(rs.getString("PLACEHOLDER")),
                        restaurants);
                owners.add(owner);
            }
        } catch (SQLException e) {
            System.err.println("Errore findAll in OwnerDAO: " + e.getMessage());
        }
        return owners;
    }

    @Override
    public boolean save(Owner owner) {
        // INSERT INTO owner (id, username, password_hash, salt, name, last_name,
        // location_id, date_of_birth) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        String query = "PLACEHOLDER";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, owner.getId().toString());
            stmt.setString(2, owner.getUsername());
            stmt.setString(3, owner.getPasswordHash());
            stmt.setString(4, owner.getPasswordSalt());
            stmt.setString(5, owner.getName());
            stmt.setString(6, owner.getLastName());
            stmt.setString(7, owner.getLocation() != null ? owner.getLocation().getId().toString() : null);
            stmt.setString(8, owner.getDateOfBirth().toString());

            if (owner.getLocation() != null) {
                locationDAO.save(owner.getLocation());
            }

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore save in OwnerDAO: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(Owner owner) {
        return save(owner);
    }

    @Override
    public boolean delete(UUID id) {
        // DELETE FROM owner WHERE id = ?
        String query = "PLACEHOLDER";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, id.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore delete in OwnerDAO: " + e.getMessage());
            return false;
        }
    }
}
