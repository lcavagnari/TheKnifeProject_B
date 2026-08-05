package it.uninsubria.laboratoriob.data;

import it.uninsubria.laboratoriob.DAO;
import it.uninsubria.laboratoriob.enums.Nation;
import it.uninsubria.laboratoriob.objects.Location;
import it.uninsubria.laboratoriob.utils.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * it.uninsubria.laboratoriob.DAO per l'entità {@link Location}.
 * <p>
 */
public class LocationDAO implements DAO<Location> {

    @Override
    public Optional<Location> findById(UUID id) {
        // SELECT id, nation, city, latitude, longitude, address FROM location WHERE id
        // = ?
        String query = "PLACEHOLDER";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, id.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Location location = new Location(
                            // Column Key Placeholder: id
                            UUID.fromString(rs.getString("PLACEHOLDER")),
                            // Column Placeholder: nation
                            Nation.valueOf(rs.getString("PLACEHOLDER")),
                            // Column Placeholder: city
                            rs.getString("PLACEHOLDER"),
                            // Column Placeholder: latitude
                            rs.getDouble("PLACEHOLDER"),
                            // Column Placeholder: longitude
                            rs.getDouble("PLACEHOLDER"),
                            // Column Placeholder: address
                            rs.getString("PLACEHOLDER"));
                    return Optional.of(location);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore findById in LocationDAO: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Location> findAll() {
        List<Location> locations = new ArrayList<>();
        // SELECT id, nation, city, latitude, longitude, address FROM location
        String query = "PLACEHOLDER";
        try (Connection conn = Database.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Location location = new Location(
                        // Column Key Placeholder: id
                        UUID.fromString(rs.getString("PLACEHOLDER")),
                        // Column Placeholder: nation
                        Nation.valueOf(rs.getString("PLACEHOLDER")),
                        // Column Placeholder: city
                        rs.getString("PLACEHOLDER"),
                        // Column Placeholder: latitude
                        rs.getDouble("PLACEHOLDER"),
                        // Column Placeholder: longitude
                        rs.getDouble("PLACEHOLDER"),
                        // Column Placeholder: address
                        rs.getString("PLACEHOLDER"));
                locations.add(location);
            }
        } catch (SQLException e) {
            System.err.println("Errore findAll in LocationDAO: " + e.getMessage());
        }
        return locations;
    }

    @Override
    public boolean save(Location location) {
        // INSERT INTO location (id, nation, city, latitude, longitude, address) VALUES
        // (?, ?, ?, ?, ?, ?)
        String query = "PLACEHOLDER";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, location.getId().toString());
            stmt.setString(2, location.getNation().name());
            stmt.setString(3, location.getCity());
            stmt.setDouble(4, location.getLatitude());
            stmt.setDouble(5, location.getLongitude());
            stmt.setString(6, location.getAddress());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore save in LocationDAO: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(Location location) {
        // UPDATE location SET nation = ?, city = ?, latitude = ?, longitude = ?,
        // address = ? WHERE id = ?
        String query = "PLACEHOLDER";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, location.getNation().name());
            stmt.setString(2, location.getCity());
            stmt.setDouble(3, location.getLatitude());
            stmt.setDouble(4, location.getLongitude());
            stmt.setString(5, location.getAddress());
            stmt.setString(6, location.getId().toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore update in LocationDAO: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(UUID id) {
        // DELETE FROM location WHERE id = ?
        String query = "PLACEHOLDER";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, id.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore delete in LocationDAO: " + e.getMessage());
            return false;
        }
    }
}
