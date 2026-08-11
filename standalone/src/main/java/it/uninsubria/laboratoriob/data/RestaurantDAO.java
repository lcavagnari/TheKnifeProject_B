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

    @Override
    public Optional<Restaurant> findById(UUID id) {
        // SELECT id, name, description, website_url, owner_id, phone, location_id,
        // price_range, has_delivery, has_online_booking, award, green_star FROM
        // restaurant WHERE id = ?
        String query = "PLACEHOLDER";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, id.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UUID restaurantId = UUID.fromString(rs.getString("PLACEHOLDER"));
                    UUID locationId = rs.getString("PLACEHOLDER") != null ? UUID.fromString(rs.getString("PLACEHOLDER"))
                            : null;
                    Location location = locationId != null ? locationDAO.findById(locationId).orElse(null) : null;
                    String ownerIdStr = rs.getString("PLACEHOLDER");
                    var owner = ownerIdStr != null ? new OwnerDAO().findById(UUID.fromString(ownerIdStr)).orElse(null)
                            : null;
                    Set<CuisineType> cuisinesTypes = findCuisines(restaurantId);
                    Set<String> services = findServices(restaurantId);

                    Restaurant restaurant = new Restaurant(
                            restaurantId,
                            // Column Placeholder: name
                            rs.getString("PLACEHOLDER"),
                            // Column Placeholder: description
                            rs.getString("PLACEHOLDER"),
                            // Column Placeholder: website_url
                            rs.getString("PLACEHOLDER"),
                            owner,
                            // Column Placeholder: phone
                            rs.getString("PLACEHOLDER"),
                            location,
                            // Column Placeholder: price_range
                            PriceRange.valueOf(rs.getString("PLACEHOLDER")),
                            // Column Placeholder: has_delivery
                            rs.getBoolean("PLACEHOLDER"),
                            // Column Placeholder: has_online_booking
                            rs.getBoolean("PLACEHOLDER"),
                            // Column Placeholder: award
                            Award.valueOf(rs.getString("PLACEHOLDER")),
                            // Column Placeholder: green_star
                            rs.getBoolean("PLACEHOLDER"),
                            cuisinesTypes,
                            services);
                    return Optional.of(restaurant);
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
        // SELECT id, name, description, website_url, owner_id, phone, location_id,
        // price_range, has_delivery, has_online_booking, award, green_star FROM
        // restaurant
        String query = "PLACEHOLDER";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                UUID restaurantId = UUID.fromString(rs.getString("PLACEHOLDER"));
                UUID locationId = rs.getString("PLACEHOLDER") != null ? UUID.fromString(rs.getString("PLACEHOLDER"))
                        : null;
                Location location = locationId != null ? locationDAO.findById(locationId).orElse(null) : null;
                String ownerIdStr = rs.getString("PLACEHOLDER");
                var owner = ownerIdStr != null ? new OwnerDAO().findById(UUID.fromString(ownerIdStr)).orElse(null)
                        : null;
                Set<CuisineType> cuisinesTypes = findCuisines(restaurantId);
                Set<String> services = findServices(restaurantId);

                Restaurant restaurant = new Restaurant(
                        restaurantId,
                        rs.getString("PLACEHOLDER"),
                        rs.getString("PLACEHOLDER"),
                        rs.getString("PLACEHOLDER"),
                        owner,
                        rs.getString("PLACEHOLDER"),
                        location,
                        PriceRange.valueOf(rs.getString("PLACEHOLDER")),
                        rs.getBoolean("PLACEHOLDER"),
                        rs.getBoolean("PLACEHOLDER"),
                        Award.valueOf(rs.getString("PLACEHOLDER")),
                        rs.getBoolean("PLACEHOLDER"),
                        cuisinesTypes,
                        services);
                restaurants.add(restaurant);
            }
        } catch (SQLException e) {
            System.err.println("Errore findAll in RestaurantDAO: " + e.getMessage());
        }
        return restaurants;
    }

    public List<Restaurant> findByOwner(UUID ownerId) {
        List<Restaurant> restaurants = new ArrayList<>();
        // SELECT id, name, description, website_url, owner_id, phone, location_id,
        // price_range, has_delivery, has_online_booking, award, green_star FROM
        // restaurant WHERE owner_id = ?
        String query = "PLACEHOLDER";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, ownerId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID restaurantId = UUID.fromString(rs.getString("PLACEHOLDER"));
                    UUID locationId = rs.getString("PLACEHOLDER") != null ? UUID.fromString(rs.getString("PLACEHOLDER"))
                            : null;
                    Location location = locationId != null ? locationDAO.findById(locationId).orElse(null) : null;
                    String ownerIdStr = rs.getString("PLACEHOLDER");
                    var owner = ownerIdStr != null ? new OwnerDAO().findById(UUID.fromString(ownerIdStr)).orElse(null)
                            : null;
                    Set<CuisineType> cuisinesTypes = findCuisines(restaurantId);
                    Set<String> services = findServices(restaurantId);

                    Restaurant restaurant = new Restaurant(
                            restaurantId,
                            rs.getString("PLACEHOLDER"),
                            rs.getString("PLACEHOLDER"),
                            rs.getString("PLACEHOLDER"),
                            owner,
                            rs.getString("PLACEHOLDER"),
                            location,
                            PriceRange.valueOf(rs.getString("PLACEHOLDER")),
                            rs.getBoolean("PLACEHOLDER"),
                            rs.getBoolean("PLACEHOLDER"),
                            Award.valueOf(rs.getString("PLACEHOLDER")),
                            rs.getBoolean("PLACEHOLDER"),
                            cuisinesTypes,
                            services);
                    restaurants.add(restaurant);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore findByOwner in RestaurantDAO: " + e.getMessage());
        }
        return restaurants;
    }

    public Set<CuisineType> findCuisines(UUID restaurantId) {
        Set<CuisineType> cuisines = new HashSet<>();
        // SELECT cuisine_type FROM restaurant_cuisines WHERE restaurant_id = ?
        String query = "PLACEHOLDER";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, restaurantId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    try {
                        cuisines.add(CuisineType.valueOf(rs.getString("PLACEHOLDER")));
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
        // SELECT service_name FROM restaurant_services WHERE restaurant_id = ?
        String query = "PLACEHOLDER";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, restaurantId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    services.add(rs.getString("PLACEHOLDER"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore findServices in RestaurantDAO: " + e.getMessage());
        }
        return services;
    }

    @Override
    public boolean save(Restaurant restaurant) {
        // INSERT INTO restaurant (id, name, description, website_url, owner_id, phone,
        // location_id, price_range, has_delivery, has_online_booking, award,
        // green_star) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        String query = "PLACEHOLDER";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, restaurant.getId().toString());
            stmt.setString(2, restaurant.getName());
            stmt.setString(3, restaurant.getDescription());
            stmt.setString(4, restaurant.getWebsiteUrl());
            stmt.setString(5, restaurant.getOwner() != null ? restaurant.getOwner().getId().toString() : null);
            stmt.setString(6, restaurant.getPhone());
            stmt.setString(7, restaurant.getLocation() != null ? restaurant.getLocation().getId().toString() : null);
            stmt.setString(8, restaurant.getPriceRange().name());
            stmt.setBoolean(9, restaurant.isHasDelivery());
            stmt.setBoolean(10, restaurant.isHasOnlineBooking());
            stmt.setString(11, restaurant.getAward().name());
            stmt.setBoolean(12, restaurant.isGreenStar());

            if (restaurant.getLocation() != null) {
                locationDAO.save(restaurant.getLocation());
            }

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore save in RestaurantDAO: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(Restaurant restaurant) {
        return save(restaurant);
    }

    @Override
    public boolean delete(UUID id) {
        // DELETE FROM restaurant WHERE id = ?
        String query = "PLACEHOLDER";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, id.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Errore delete in RestaurantDAO: " + e.getMessage());
            return false;
        }
    }
}
