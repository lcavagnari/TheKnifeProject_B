package it.uninsubria.laboratoriob.utils;


import it.uninsubria.laboratoriob.data.ClientDAO;
import it.uninsubria.laboratoriob.data.OwnerDAO;
import it.uninsubria.laboratoriob.data.RestaurantDAO;
import it.uninsubria.laboratoriob.data.ReviewDAO;
import it.uninsubria.laboratoriob.objects.*;
import it.uninsubria.laboratoriob.ui.IO;
import it.uninsubria.laboratoriob.exceptions.AbortOperationException;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Classe di utilità responsabile del caricamento e della gestione dei dati
 * dell'applicazione
 * a partire dal database.
 * <p>
 * Gestisce la lettura delle entità tramite i relativi it.uninsubria.laboratoriob.DAO e le inserisce in
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
    private final static ClientDAO clientDAO = new ClientDAO();
    private final static OwnerDAO ownerDAO = new OwnerDAO();

    private void loadRestaurants() {
        for (Restaurant restaurant : restaurantDAO.findAll()) {
            for (Review review : reviewDAO.findByRestaurant(restaurant.getId())) {
                restaurant.addReview(review);
            }
            restaurantsById.put(restaurant.getId(), restaurant);
            restaurantsByName.put(restaurant.getName(), restaurant);
        }
    }

    private void loadUsers() {
        for (Owner owner : ownerDAO.findAll()) {
            usersById.put(owner.getId(), owner);
            usersByName.put(owner.getUsername(), owner);
        }

        for (Client client : clientDAO.findAll()) {
            usersById.put(client.getId(), client);
            usersByName.put(client.getUsername(), client);
        }
    }

    public void loadFromFile() {
        try {
            CompletableFuture<Void> usersAndRestaurantsFuture = CompletableFuture
                    .runAsync(Loader::loadUsers)
                    .thenRunAsync(Loader::loadRestaurants)
                    .exceptionally(ex -> {
                        IO.printErrorMessage("Errore caricamento users/restaurants: " + ex.getMessage());
                        return null;
                    });

            usersAndRestaurantsFuture.join();

        } catch (Exception ex) {
            IO.printErrorMessage("Errore durante il caricamento: " + ex.getMessage());
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
