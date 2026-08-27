package it.uninsubria.laboratoriob.server.data;

import it.uninsubria.laboratoriob.api.objects.Restaurant;
import it.uninsubria.laboratoriob.api.objects.Review;
import it.uninsubria.laboratoriob.api.objects.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServerDataStore {

    private final RestaurantDAO restaurantDAO = new RestaurantDAO();
    private final ReviewDAO reviewDAO = new ReviewDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final OwnerDAO ownerDAO = new OwnerDAO();

    private final Map<UUID, Restaurant> restaurantsById = new ConcurrentHashMap<>();
    private final Map<String, Restaurant> restaurantsByName = new ConcurrentHashMap<>();
    private final Map<UUID, User> usersById = new ConcurrentHashMap<>();
    private final Map<String, User> usersByName = new ConcurrentHashMap<>();

    // ── DAO accessors ──

    public RestaurantDAO restaurantDAO() { return restaurantDAO; }
    public ReviewDAO reviewDAO() { return reviewDAO; }
    public CustomerDAO customerDAO() { return customerDAO; }
    public OwnerDAO ownerDAO() { return ownerDAO; }

    // ── Write operations ──

    public void addRestaurant(Restaurant r) {
        restaurantsById.put(r.getId(), r);
        restaurantsByName.put(r.getName(), r);
    }

    public void removeRestaurant(UUID id) {
        Restaurant r = restaurantsById.remove(id);
        if (r != null) restaurantsByName.remove(r.getName());
    }

    public void updateRestaurant(Restaurant oldRestaurant, Restaurant newRestaurant) {
        if (oldRestaurant == null || newRestaurant == null) return;
        restaurantsById.remove(oldRestaurant.getId());
        restaurantsByName.remove(oldRestaurant.getName());
        addRestaurant(newRestaurant);
    }

    public void addUser(User u) {
        usersById.put(u.getId(), u);
        usersByName.put(u.getUsername(), u);
    }

    public void removeUser(UUID id) {
        User u = usersById.remove(id);
        if (u != null) usersByName.remove(u.getUsername());
    }

    public void updateUser(User oldUser, User newUser) {
        if (oldUser == null || newUser == null) return;
        usersById.remove(oldUser.getId());
        usersByName.remove(oldUser.getUsername());
        addUser(newUser);
    }

    // ── Read operations (single entity) ──

    public Restaurant findRestaurantById(UUID id) { return restaurantsById.get(id); }

    public Restaurant findRestaurantByName(String name) { return restaurantsByName.get(name); }

    public boolean hasRestaurantByName(String name) { return restaurantsByName.containsKey(name); }

    public User findUserById(UUID id) { return usersById.get(id); }

    public User findUserByName(String name) { return usersByName.get(name); }

    public boolean hasUserByName(String name) { return usersByName.containsKey(name); }

    // ── Read operations (bulk / unmodifiable views) ──

    public Map<UUID, Restaurant> getAllRestaurantsById() { return Collections.unmodifiableMap(restaurantsById); }

    public Map<String, Restaurant> getAllRestaurantsByName() { return Collections.unmodifiableMap(restaurantsByName); }

    public Map<UUID, User> getAllUsersById() { return Collections.unmodifiableMap(usersById); }

    public Map<String, User> getAllUsersByName() { return Collections.unmodifiableMap(usersByName); }

    public Collection<Restaurant> getAllRestaurants() { return Collections.unmodifiableCollection(restaurantsById.values()); }

    public long countRestaurants() { return restaurantsById.size(); }

    public long countUsers() { return usersById.size(); }

    // ── Derived queries (reviews live inside Restaurant objects) ──

    public List<Review> findReviewsByRestaurant(UUID restaurantId) {
        Restaurant r = restaurantsById.get(restaurantId);
        if (r == null) return List.of();
        return List.copyOf(r.getReviews().values());
    }

    public List<Review> findReviewsByUser(UUID userId) {
        List<Review> result = new ArrayList<>();
        for (Restaurant r : restaurantsById.values())
            for (Review review : r.getReviews().values())
                if (review.getUser() != null && review.getUser().getId().equals(userId))
                    result.add(review);



        return result;
    }

    public List<Review> findAllReviews() {
        List<Review> result = new ArrayList<>();
        for (Restaurant r : restaurantsById.values())
            result.addAll(r.getReviews().values());

        return result;
    }
}
