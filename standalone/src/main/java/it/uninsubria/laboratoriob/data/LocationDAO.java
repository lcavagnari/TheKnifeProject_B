package it.uninsubria.laboratoriob.data;

import it.uninsubria.laboratoriob.api.data.DAO;
import it.uninsubria.laboratoriob.api.enums.Nation;
import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.utils.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementazione del DAO per l'entità {@link Location}.
 * <p>
 * Gestisce le operazioni CRUD sulle posizioni geografiche nel database PostgreSQL.
 * Le location sono identificate dalla coppia di coordinate (latitudine, longitudine)
 * anziché da un UUID, poiché la tabella {@code location} utilizza queste come chiave primaria.
 * </p>
 *
 * <h2>Responsabilità</h2>
 * <ul>
 *   <li>Ricerca per coordinate geografiche.</li>
 *   <li>Salvataggio con gestione dei conflitti (ON CONFLICT DO NOTHING).</li>
 *   <li>Aggiornamento e cancellazione per coordinate.</li>
 * </ul>
 *
 * @author Luca Cavagnari
 * @version 2.0
 * @see DAO
 * @see Location
 */
public final class LocationDAO implements DAO<Location> {

    // Location has no ID attribute
    @Override
    public Optional<Location> findById(UUID id) {
        return Optional.empty();
    }

    @Override
    public boolean delete(UUID id) {
        return false;
    }


    public Optional<Location> findByCoordinates(double lat, double longit) {
        final String query = "SELECT * FROM location WHERE latitude=? AND longitude=?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDouble(1, lat);
            stmt.setDouble(2, longit);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Location location = new Location(
                            Nation.fromString(rs.getString("country")),       // nation
                            rs.getString("city"),                             // city
                            lat,                                                           // latitude
                            longit,                                                        // longitude
                            rs.getString("address"));                         // address

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
        String query = "SELECT * FROM location";

        List<Location> locations = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(query)) {

            while (rs.next()) {
                Location location = new Location(
                        Nation.fromString(rs.getString("country")),       // nation
                        rs.getString("city"),                             // city
                        rs.getDouble("latitude"),                         // latitude
                        rs.getDouble("longitude"),                        // longitude
                        rs.getString("address"));                         // address

                locations.add(location);
            }
        } catch (SQLException e) {
            System.err.println("Errore findAll in LocationDAO: " + e.getMessage());
        }

        return locations;
    }

    @Override
    public boolean save(Location location) {
        String query = "INSERT INTO location (latitude, longitude, city, country, address) VALUES (?,?,?,?,?) ON CONFLICT (latitude, longitude) DO NOTHING";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDouble(1, location.getLatitude());
            stmt.setDouble(2, location.getLongitude());
            stmt.setString(3, location.getCity() != null && location.getCity().isBlank() ? null : location.getCity());
            stmt.setString(4, location.getNation().getIsoCode());
            stmt.setString(5, location.getAddress() != null && location.getAddress().isBlank() ? null : location.getAddress());

            return stmt.executeUpdate() >= 0;
        } catch (SQLException e) {
            System.err.println("Errore save in LocationDAO: " + e.getMessage());
            return false;
        }
    }


    public boolean update(double lat, double longit, Location newLoc) {
        String query = "UPDATE location SET country = ?, city = ?, latitude = ?, longitude = ?, address = ? WHERE latitude = ? AND longitude = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newLoc.getNation().name());
            stmt.setString(2, newLoc.getCity());
            stmt.setDouble(3, newLoc.getLatitude());
            stmt.setDouble(4, newLoc.getLongitude());
            stmt.setString(5, newLoc.getAddress());
            stmt.setDouble(6, lat);
            stmt.setDouble(7, longit);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore update in LocationDAO: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(Location entity) {
        return update(entity.getLatitude(), entity.getLongitude(), entity);
    }


    public boolean delete(double lat, double longit) {
        // DELETE FROM location WHERE id = ?
        String query = "DELETE FROM location WHERE latitude = ? AND longitude = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDouble(1, lat);
            stmt.setDouble(2, longit);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore delete in LocationDAO: " + e.getMessage());
            return false;
        }
    }
}
