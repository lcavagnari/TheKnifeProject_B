package it.uninsubria.laboratoriob.server.data;

import it.uninsubria.laboratoriob.api.objects.*;
import it.uninsubria.laboratoriob.server.data.repository.RestaurantRepository;
import it.uninsubria.laboratoriob.server.data.repository.ReviewRepository;
import it.uninsubria.laboratoriob.server.data.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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

    /**
     * Bulk-loads users and restaurants (with their reviews) from the database into
     * the repositories' caches. Replaces the old {@code Loader.initialise()}.
     */
    public void initialise() {
        CompletableFuture<List<Owner>> ownersFuture = users.loadAllOwnersFromDb();
        CompletableFuture<List<Customer>> customersFuture = users.loadAllCustomersFromDb();

        // barrier-style synchronisation to ensure all user-relevant data is loaded.
        CompletableFuture.allOf(ownersFuture, customersFuture).join();

        // cache them.
        for (Owner owner : ownersFuture.join()) users.putCache(owner);
        for (Customer customer : customersFuture.join()) users.putCache(customer);

        CompletableFuture<List<Restaurant>> restaurantList = restaurants.loadAllFromDb();

        List<CompletableFuture<Void>> tasks = new ArrayList<>();
        for (Restaurant restaurant : restaurantList.join()) {
            restaurants.putCache(restaurant);
            tasks.add(
                    reviews.loadForRestaurant(restaurant.getId())
                            .thenAccept(reviewList -> {
                                for (Review review : reviewList) restaurant.addReview(review);
                            })
            );
        }

        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();

        resolveOwners();
    }

    private void resolveOwners() {
        for (Restaurant restaurant : restaurants.findAll()) {
            if (restaurant.getOwner() != null) {
                User u = users.findById(restaurant.getOwner().getId());

                if (u instanceof Owner owner) {
                    restaurant.setOwner(owner);
                    owner.addRestaurant(restaurant);
                }
            }
        }
    }
}
