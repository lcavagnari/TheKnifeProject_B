package it.uninsubria.laboratoriob.utils;



import it.uninsubria.laboratoriob.data.CustomerDAO;
import it.uninsubria.laboratoriob.data.OwnerDAO;
import it.uninsubria.laboratoriob.data.RestaurantDAO;
import it.uninsubria.laboratoriob.data.ReviewDAO;
import it.uninsubria.laboratoriob.api.exceptions.AbortOperationException;
import it.uninsubria.laboratoriob.api.objects.*;
import it.uninsubria.laboratoriob.ui.IO;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Classe di utilità responsabile del caricamento e della gestione dei dati
 * dell'applicazione
 * a partire dal database.
 * <p>
 * Gestisce la lettura delle entità tramite i relativi it.uninsubria.laboratoriob.api.data.DAO e le inserisce in
 * strutture dati statiche.
 * <p>
 *
 * @author Luca Cavagnari
 * @version 2.0
 */
@UtilityClass
public class Loader {

    private final static Map<UUID, Restaurant> restaurantsById = new ConcurrentHashMap<>();
    private final static Map<String, Restaurant> restaurantsByName = new ConcurrentHashMap<>();
    private final static Map<UUID, User> usersById = new ConcurrentHashMap<>();
    private final static Map<String, User> usersByName = new ConcurrentHashMap<>();

    private final static RestaurantDAO restaurantDAO = new RestaurantDAO();
    private final static ReviewDAO reviewDAO = new ReviewDAO();

    private final static CustomerDAO CUSTOMER_DAO = new CustomerDAO();
    private final static OwnerDAO OWNER_DAO = new OwnerDAO();

    // ── Write operations ──

    public static void addRestaurant(Restaurant r) {
        restaurantsById.put(r.getId(), r);
        restaurantsByName.put(r.getName(), r);
    }

    public static void removeRestaurant(UUID id) {
        Restaurant r = restaurantsById.remove(id);
        if (r != null) restaurantsByName.remove(r.getName());
    }

    public static void updateRestaurant(Restaurant oldRestaurant, Restaurant newRestaurant) {
        if (oldRestaurant == null || newRestaurant == null) return;

        restaurantsById.remove(oldRestaurant.getId());
        restaurantsByName.remove(oldRestaurant.getName());

        restaurantsById.put(newRestaurant.getId(), newRestaurant);
        restaurantsByName.put(newRestaurant.getName(), newRestaurant);
    }

    public static void addUser(User u) {
        usersById.put(u.getId(), u);
        usersByName.put(u.getUsername(), u);
    }

    public static void removeUser(UUID id) {
        User u = usersById.remove(id);
        if (u != null) usersByName.remove(u.getUsername());
    }

    public static void updateUser(User oldUser, User newUser) {
        if (oldUser == null || newUser == null) return;

        usersById.remove(oldUser.getId());
        usersByName.remove(oldUser.getUsername());

        usersById.put(newUser.getId(), newUser);
        usersByName.put(newUser.getUsername(), newUser);
    }

    // ── Read operations (single entity) ──

    public static Restaurant findRestaurantById(UUID id) {
        return restaurantsById.get(id);
    }

    public static Restaurant findRestaurantByName(String name) {
        return restaurantsByName.get(name);
    }

    public static boolean hasRestaurantByName(String name) {
        return restaurantsByName.containsKey(name);
    }

    public static User findUserById(UUID id) {
        return usersById.get(id);
    }

    public static User findUserByName(String name) {
        return usersByName.get(name);
    }

    public static boolean hasUserByName(String name) {
        return usersByName.containsKey(name);
    }

    // ── Read operations (bulk / unmodifiable views) ──

    public static Map<UUID, Restaurant> getAllRestaurantsById() {
        return Collections.unmodifiableMap(restaurantsById);
    }

    public static Map<String, Restaurant> getAllRestaurantsByName() {
        return Collections.unmodifiableMap(restaurantsByName);
    }

    public static Map<UUID, User> getAllUsersById() {
        return Collections.unmodifiableMap(usersById);
    }

    public static Map<String, User> getAllUsersByName() {
        return Collections.unmodifiableMap(usersByName);
    }

    private void loadRestaurants() throws CompletionException {
        List<Restaurant> restaurants = CompletableFuture
                .supplyAsync(restaurantDAO::findAll)
                .exceptionally(ex -> {
                    IO.printErrorMessage("Errore caricamento restaurants: " + ex.getMessage());
                    return new ArrayList<>();
                })
                .join();

        List<CompletableFuture<List<Review>>> tasks = new ArrayList<>();

        for (Restaurant restaurant : restaurants) {
            tasks.add(
                    CompletableFuture
                            .supplyAsync(() -> reviewDAO.findByRestaurant(restaurant.getId()))
                            .exceptionally(ex -> {
                                IO.printErrorMessage(
                                        "Errore caricamento review for restaurant #"
                                                + restaurant.getId() + ": " + ex.getMessage()
                                );
                                return new ArrayList<>();
                            })
            );
        }

        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();

        for (int i = 0; i < restaurants.size(); i++) {
            Restaurant restaurant = restaurants.get(i);
            List<Review> reviews = tasks.get(i).join();


            for (Review review : reviews) restaurant.addReview(review);

            addRestaurant(restaurant);
        }
    }

    private void loadUsers() throws CompletionException {
        CompletableFuture<List<Owner>> owners = CompletableFuture
                .supplyAsync(OWNER_DAO::findAll)
                .exceptionally(ex -> {
                    IO.printErrorMessage("Errore caricamento clienti: " + ex.getMessage());
                    return null;
                });

        CompletableFuture<List<Customer>> customers = CompletableFuture
                .supplyAsync(CUSTOMER_DAO::findAll)
                .exceptionally(ex -> {
                    IO.printErrorMessage("Errore caricamento proprietari: " + ex.getMessage());
                    return null;
                });

        // join a barriera in attesa del completamento
        CompletableFuture.allOf(owners, customers).join();


        List<Owner> o = owners.join();
        List<Customer> c = customers.join();

        for (Owner owner : o) {
            addUser(owner);
        }

        for (Customer customer : c) {
            addUser(customer);
        }
    }

    public void initialiseMaps() {
        try {
            loadUsers();
            loadRestaurants();


        } catch (CompletionException ex) {
            IO.printErrorMessage("Errore/i durante il caricamento: ");
        } catch (Exception ex) {
            IO.printErrorMessage("Errore/i durante il caricamento: " + ex.getMessage());
        }
    }

    public static void updateMichelinDataset(String path) {
        path = (path != null && !path.isBlank()) ? path : "michelin_my_maps.csv";

        Path inputPath;
        try {
            inputPath = Paths.get(path);
            File dataset = new File(inputPath.toUri());

            if (!dataset.exists() || !dataset.isFile() || !dataset.getName().endsWith(".csv")) {
                IO.printErrorMessage("File or path " + path
                        + " does not exist or it is not supported by the program, please check and try again.");
                return;
            }

        } catch (AbortOperationException ignored) {
            IO.printErrorMessage("Operazione annullata");
            return;

        } catch (Exception ignored) {
            IO.printErrorMessage("File or path " + path + " does not exist, check and try again.");
            return;
        }

        IO.printErrorMessage("Updating michelin data from file...");

        long timestamp = System.currentTimeMillis();
        CsvParser.parseFromDataset(inputPath);

        IO.clearScreen();
        IO.printSuccessMessage("Update completed in " + ((System.currentTimeMillis() - timestamp) + "ms"));
    }
}
