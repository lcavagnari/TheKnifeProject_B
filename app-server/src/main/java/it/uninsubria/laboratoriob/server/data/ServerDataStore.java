package it.uninsubria.laboratoriob.server.data;

import it.uninsubria.laboratoriob.server.data.repository.RestaurantRepository;
import it.uninsubria.laboratoriob.server.data.repository.ReviewRepository;
import it.uninsubria.laboratoriob.server.data.repository.UserRepository;

public class ServerDataStore {

    private final RestaurantRepository restaurants;
    private final ReviewRepository reviews;
    private final UserRepository users;

    public ServerDataStore() {
        this.restaurants = new RestaurantRepository();
        this.reviews = new ReviewRepository(restaurants);
        this.users = new UserRepository();
    }

    public RestaurantRepository restaurants() { return restaurants; }
    public ReviewRepository reviews() { return reviews; }
    public UserRepository users() { return users; }
}
