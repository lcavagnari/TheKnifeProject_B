package it.uninsubria.laboratoriob.data;

import it.uninsubria.laboratoriob.api.data.DAO;
import it.uninsubria.laboratoriob.api.enums.Award;
import it.uninsubria.laboratoriob.api.enums.CuisineType;
import it.uninsubria.laboratoriob.api.enums.PriceRange;
import it.uninsubria.laboratoriob.api.objects.Location;
import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.utils.Database;

import java.sql.*;
import java.util.*;

public class RestaurantDAO implements DAO<Restaurant> {

    private final LocationDAO locationDAO = new LocationDAO();

    private Restaurant mapRestaurant(ResultSet rs) throws SQLException {
        UUID restaurantId = UUID.fromString(rs.getString("id"));
        double lat = rs.getDouble("latitude");
        double lon = rs.getDouble("longitude");
        Location location = !rs.wasNull()
                ? locationDAO.findByCoordinates(lat, lon).orElse(null) : null;

        String ownerIdStr = rs.getString("owner_id");

        var owner = ownerIdStr != null ? new OwnerDAO().findById(UUID.fromString(ownerIdStr)).orElse(null) : null;

        Set<CuisineType> cuisinesTypes = findCuisines(restaurantId);
        Set<String> services = findServices(restaurantId);

        String priceDesc = rs.getString("price_desc");

        PriceRange priceRange = (priceDesc != null && !priceDesc.isBlank())
                ? PriceRange.byDollarAmount(priceDesc.length())
                : PriceRange.MODERATE;
        Award award = Award.fromInt(rs.getInt("award"));

        return new Restaurant(
                restaurantId,
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("web_url"),
                owner,
                rs.getString("phone_number"),
                location,
                priceRange,
                rs.getBoolean("has_delivery"),
                rs.getBoolean("has_booking"),
                award,
                rs.getBoolean("green_star"),
                cuisinesTypes,
                services);
    }


    @Override
    public Optional<Restaurant> findById(UUID id) {
        String query = """
                SELECT r.id, r.owner_id, r.name, r.description, r.web_url, r.phone_number,
                       r.award, r.green_star, r.has_delivery, r.has_booking, r.latitude, r.longitude,
                       pr.description AS price_desc
                FROM restaurant r
                LEFT JOIN price_range pr ON r.price_range = pr.id
                WHERE r.id = ?
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setObject(1, id, Types.OTHER);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRestaurant(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore findById in RestaurantDAO: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Restaurant> findAll() {
        List<Restaurant> restaurants = new ArrayList<>();
        String query = """
                SELECT r.id, r.owner_id, r.name, r.description, r.web_url, r.phone_number,
                       r.award, r.green_star, r.has_delivery, r.has_booking, r.latitude, r.longitude,
                       pr.description AS price_desc
                FROM restaurant r
                LEFT JOIN price_range pr ON r.price_range = pr.id
                """;
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) restaurants.add(mapRestaurant(rs));

        } catch (SQLException e) {
            System.err.println("Errore findAll in RestaurantDAO: " + e.getMessage());
        }

        return restaurants;
    }

    public List<Restaurant> findByOwner(UUID ownerId) {
        List<Restaurant> restaurants = new ArrayList<>();
        String query = """
                SELECT r.id, r.owner_id, r.name, r.description, r.web_url, r.phone_number,
                       r.award, r.green_star, r.has_delivery, r.has_booking, r.latitude, r.longitude,
                       pr.description AS price_desc
                FROM restaurant r
                LEFT JOIN price_range pr ON r.price_range = pr.id
                WHERE r.owner_id = ?
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setObject(1, ownerId, Types.OTHER);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    restaurants.add(mapRestaurant(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore findByOwner in RestaurantDAO: " + e.getMessage());
        }
        return restaurants;
    }

    public Set<CuisineType> findCuisines(UUID restaurantId) {
        Set<CuisineType> cuisines = new HashSet<>();
        String query = """
                SELECT ct.description
                FROM restaurant_cuisine rc
                JOIN cuisine_type ct ON rc.type = ct.id
                WHERE rc.restaurant_id = ?
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setObject(1, restaurantId, Types.OTHER);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    try {
                        cuisines.add(CuisineType.valueOf(rs.getString("description")));
                    } catch (Exception ignored) {
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore findCuisines in RestaurantDAO: " + e.getMessage());
        }
        return cuisines;
    }

    public Set<String> findServices(UUID restaurantId) {
        Set<String> services = new HashSet<>();
        String query = """
                SELECT sf.description
                FROM restaurant_services rs
                JOIN services_and_facilities sf ON rs.service = sf.id
                WHERE rs.restaurant_id = ?
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setObject(1, restaurantId, Types.OTHER);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    services.add(rs.getString("description"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore findServices in RestaurantDAO: " + e.getMessage());
        }
        return services;
    }

    @Override
    public boolean save(Restaurant restaurant) {
        String query = """
                INSERT INTO restaurant (id, owner_id, name, description, web_url, phone_number,
                                        price_range, award, green_star, has_delivery, has_booking,
                                        latitude, longitude)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setObject(1, restaurant.getId(), Types.OTHER);
            stmt.setObject(2, restaurant.getOwner() != null ? restaurant.getOwner().getId() : null, Types.OTHER);
            stmt.setString(3, restaurant.getName());
            stmt.setString(4, restaurant.getDescription());
            stmt.setString(5, restaurant.getWebsiteUrl());
            stmt.setString(6, restaurant.getPhone());
            stmt.setInt(7, restaurant.getPriceRange().getSymbol().length());
            stmt.setInt(8, restaurant.getAward().getValue());
            stmt.setBoolean(9, restaurant.isGreenStar());
            stmt.setBoolean(10, restaurant.isHasDelivery());
            stmt.setBoolean(11, restaurant.isHasOnlineBooking());

            if (restaurant.getLocation() != null) {
                stmt.setDouble(12, restaurant.getLocation().getLatitude());
                stmt.setDouble(13, restaurant.getLocation().getLongitude());
                locationDAO.save(restaurant.getLocation());

            } else {
                stmt.setNull(12, Types.DOUBLE);
                stmt.setNull(13, Types.DOUBLE);
            }

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore save in RestaurantDAO: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(Restaurant restaurant) {
        String query = """
                UPDATE restaurant
                SET owner_id = ?, name = ?, description = ?, web_url = ?, phone_number = ?,
                    price_range = ?, award = ?, green_star = ?, has_delivery = ?, has_booking = ?,
                    latitude = ?, longitude = ?
                WHERE id = ?
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setObject(1, restaurant.getOwner() != null ? restaurant.getOwner().getId() : null, Types.OTHER);
            stmt.setString(2, restaurant.getName());
            stmt.setString(3, restaurant.getDescription());
            stmt.setString(4, restaurant.getWebsiteUrl());
            stmt.setString(5, restaurant.getPhone());
            stmt.setInt(6, restaurant.getPriceRange().getSymbol().length());
            stmt.setInt(7, restaurant.getAward().getValue());
            stmt.setBoolean(8, restaurant.isGreenStar());
            stmt.setBoolean(9, restaurant.isHasDelivery());
            stmt.setBoolean(10, restaurant.isHasOnlineBooking());

            if (restaurant.getLocation() != null) {
                stmt.setDouble(11, restaurant.getLocation().getLatitude());
                stmt.setDouble(12, restaurant.getLocation().getLongitude());
                locationDAO.save(restaurant.getLocation());

            } else {
                stmt.setNull(11, Types.DOUBLE);
                stmt.setNull(12, Types.DOUBLE);
            }

            stmt.setObject(13, restaurant.getId(), Types.OTHER);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore update in RestaurantDAO: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(UUID id) {
        String query = "DELETE FROM restaurant WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setObject(1, id, Types.OTHER);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore delete in RestaurantDAO: " + e.getMessage());
            return false;
        }
    }

    public boolean updateCuisines(UUID restaurantId, Set<CuisineType> cuisines) {
        String deleteQuery = "DELETE FROM restaurant_cuisine WHERE restaurant_id = ?";
        String insertQuery = "INSERT INTO restaurant_cuisine (restaurant_id, type) VALUES (?, ?)";
        String findTypeQuery = "SELECT id FROM cuisine_type WHERE description = ?";

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
                deleteStmt.setObject(1, restaurantId, Types.OTHER);
                deleteStmt.executeUpdate();
            }

            for (CuisineType cuisine : cuisines) {
                int typeId = -1;
                try (PreparedStatement findStmt = conn.prepareStatement(findTypeQuery)) {
                    findStmt.setString(1, cuisine.name());
                    try (ResultSet rs = findStmt.executeQuery()) {
                        if (rs.next()) {
                            typeId = rs.getInt("id");
                        }
                    }
                }

                if (typeId == -1) {
                    String insertTypeQuery = "INSERT INTO cuisine_type (id, description) VALUES (DEFAULT, ?) RETURNING id";
                    try (PreparedStatement insertTypeStmt = conn.prepareStatement(insertTypeQuery)) {
                        insertTypeStmt.setString(1, cuisine.name());
                        try (ResultSet rs = insertTypeStmt.executeQuery()) {
                            if (rs.next()) {
                                typeId = rs.getInt("id");
                            }
                        }
                    }
                }

                if (typeId != -1) {
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                        insertStmt.setObject(1, restaurantId, Types.OTHER);
                        insertStmt.setInt(2, typeId);
                        insertStmt.executeUpdate();
                    }
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Errore updateCuisines in RestaurantDAO: " + e.getMessage());
            return false;
        }
    }

    public boolean updateServices(UUID restaurantId, Set<String> services) {
        String deleteQuery = "DELETE FROM restaurant_services WHERE restaurant_id = ?";
        String insertQuery = "INSERT INTO restaurant_services (restaurant_id, service) VALUES (?, ?)";
        String findServiceQuery = "SELECT id FROM services_and_facilities WHERE description = ?";

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
                deleteStmt.setObject(1, restaurantId, Types.OTHER);
                deleteStmt.executeUpdate();
            }

            for (String service : services) {
                int serviceId = -1;
                try (PreparedStatement findStmt = conn.prepareStatement(findServiceQuery)) {
                    findStmt.setString(1, service);
                    try (ResultSet rs = findStmt.executeQuery()) {
                        if (rs.next()) {
                            serviceId = rs.getInt("id");
                        }
                    }
                }

                if (serviceId == -1) {
                    String insertServiceQuery = "INSERT INTO services_and_facilities (id, description) VALUES (DEFAULT, ?) RETURNING id";
                    try (PreparedStatement insertServiceStmt = conn.prepareStatement(insertServiceQuery)) {
                        insertServiceStmt.setString(1, service);
                        try (ResultSet rs = insertServiceStmt.executeQuery()) {
                            if (rs.next()) {
                                serviceId = rs.getInt("id");
                            }
                        }
                    }
                }

                if (serviceId != -1) {
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                        insertStmt.setObject(1, restaurantId, Types.OTHER);
                        insertStmt.setInt(2, serviceId);
                        insertStmt.executeUpdate();
                    }
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Errore updateServices in RestaurantDAO: " + e.getMessage());
            return false;
        }
    }
}
