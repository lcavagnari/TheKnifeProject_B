package it.uninsubria.laboratoriob.utils;


import it.uninsubria.laboratoriob.api.data.CustomerDAO;
import it.uninsubria.laboratoriob.api.data.OwnerDAO;
import it.uninsubria.laboratoriob.api.data.RestaurantDAO;
import it.uninsubria.laboratoriob.api.data.ReviewDAO;
import it.uninsubria.laboratoriob.api.exceptions.AbortOperationException;
import it.uninsubria.laboratoriob.api.objects.*;
import it.uninsubria.laboratoriob.ui.IO;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

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

    /**
     * Mappa dei ristoranti indicizzati per ID.
     */
    @Getter
    private final static Map<UUID, Restaurant> restaurantsById = new HashMap<>();

    /**
     * Mappa dei ristoranti indicizzati per nome.
     */
    @Getter
    private final static Map<String, Restaurant> restaurantsByName = new HashMap<>();

    /**
     * Mappa degli utenti indicizzati per ID.
     */
    @Getter
    private final static Map<UUID, User> usersById = new HashMap<>();

    /**
     * Mappa degli utenti indicizzati per nome utente (username).
     */
    @Getter
    private final static Map<String, User> usersByName = new HashMap<>();

    private final static RestaurantDAO restaurantDAO = new RestaurantDAO();
    private final static ReviewDAO reviewDAO = new ReviewDAO();

    private final static CustomerDAO CUSTOMER_DAO = new CustomerDAO();
    private final static OwnerDAO OWNER_DAO = new OwnerDAO();

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

            restaurantsById.put(restaurant.getId(), restaurant);
            restaurantsByName.put(restaurant.getName(), restaurant);
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
            usersById.put(owner.getId(), owner);
            usersByName.put(owner.getUsername(), owner);
        }

        for (Customer customer : c) {
            usersById.put(customer.getId(), customer);
            usersByName.put(customer.getUsername(), customer);
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
