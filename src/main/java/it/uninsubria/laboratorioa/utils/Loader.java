package it.uninsubria.laboratorioa.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.uninsubria.laboratorioa.objects.Location;
import it.uninsubria.laboratorioa.objects.Restaurant;
import it.uninsubria.laboratorioa.objects.Review;
import it.uninsubria.laboratorioa.objects.enums.Award;
import it.uninsubria.laboratorioa.objects.enums.CuisineType;
import it.uninsubria.laboratorioa.objects.enums.Nation;
import it.uninsubria.laboratorioa.objects.enums.PriceRange;
import it.uninsubria.laboratorioa.objects.users.Client;
import it.uninsubria.laboratorioa.objects.users.Owner;
import it.uninsubria.laboratorioa.objects.users.User;
import it.uninsubria.laboratorioa.ui.IO;
import it.uninsubria.laboratorioa.ui.exceptions.AbortOperationException;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Classe di utilità responsabile del caricamento e della gestione dei dati dell'applicazione
 * a partire da file JSON memorizzati nel file system locale.
 * <p>
 * Gestisce la deserializzazione delle entità `User`, `Owner`, `Client` e `Restaurant` e
 * le inserisce in strutture dati statiche per un accesso efficiente tramite ID, nome o proprietario.
 * <p>
 *
 * @author Luca Cavagnari
 * @version 1.0
 */
@UtilityClass
public class Loader {

    /**
     * Cartella radice che contiene tutti i dati del sistema.
     * Definita in {@link Constants#ROOT}
     */
    private static final File ROOT = Constants.ROOT;

    /** Directory contenente i file JSON dei ristoranti. */
    private static final File RESTAURANTS_ROOT = new File(ROOT, "companies");

    /** Directory contenente i file JSON degli utenti. */
    private static final File USERS_ROOT = new File(ROOT, "users");

    private static final File michelinData = new File(Constants.ROOT, "michelin_my_maps.json");
    private static final File oldMichelinData = new File(Constants.ROOT, "michelin_my_maps.old.json");

    /** Mappa dei ristoranti indicizzati per ID. */
    @Getter private final static Map<UUID, Restaurant> restaurantsById = new HashMap<>();

    /** Mappa dei ristoranti indicizzati per nome. */
    @Getter private final static Map<String, Restaurant> restaurantsByName = new HashMap<>();

    /** Mappa degli utenti indicizzati per ID. */
    @Getter private final static Map<UUID, User> usersById = new HashMap<>();

    /** Mappa degli utenti indicizzati per nome utente (username). */
    @Getter private final static Map<String, User> usersByName = new HashMap<>();

    /**
     * Carica tutti i ristoranti dai file specificati e li deserializza in oggetti `Restaurant`.
     * I ristoranti vengono aggiunti alle relative mappe di gestione interna.
     *
     * @param rFiles array di file JSON contenenti i dati dei ristoranti
     */
    private void loadRestaurants(File[] rFiles) {
        if (rFiles == null) return;

        for (File f : rFiles) {
            try {
                JsonNode jsonNode = new ObjectMapper().readTree(f);

                UUID id = UUID.fromString(jsonNode.path("id").asText());
                String name = jsonNode.path("name").asText();
                String description = jsonNode.path("address").asText();
                String websiteUrl = jsonNode.path("websiteUrl").asText("");
                String phone = jsonNode.path("phone").asText();

                Award award = Award.fromInt(jsonNode.path("award").asInt());
                boolean greenStar = jsonNode.path("greenStar").asBoolean();
                boolean hasDelivery = jsonNode.path("hasDelivery").asBoolean(false);
                boolean hasBooking = jsonNode.path("hasOnlineBooking").asBoolean(false);

                UUID ownerId = UUID.fromString(jsonNode.path("owner").asText());
                User owner = usersById.get(ownerId);

                JsonNode locNode = jsonNode.path("location");
                Location loc = new Location(
                        Nation.valueOf(locNode.path("nation").asText().toUpperCase().replace(" ","_")),
                        locNode.path("city").asText(),
                        locNode.path("latitude").asDouble(),
                        locNode.path("longitude").asDouble(),
                        locNode.path("address").asText()
                );

                String price = jsonNode.get("priceRange").asText();
                PriceRange priceRange = PriceRange.byDollarAmount(price.length());

                Set<CuisineType> cuisines = new HashSet<>();
                for (JsonNode node : jsonNode.path("cuisinesTypes")) {
                    try {
                        cuisines.add(CuisineType.valueOf(node.asText().toUpperCase()));
                    } catch (IllegalArgumentException ignored) {}
                }

                Set<String> services = new HashSet<>();
                for (JsonNode node : jsonNode.path("services"))
                    services.add(node.asText());

                Restaurant restaurant = new Restaurant(
                        id,
                        name,
                        description,
                        websiteUrl,
                        (Owner) owner,
                        phone,
                        loc,
                        priceRange,
                        hasDelivery,
                        hasBooking,
                        award,
                        greenStar,
                        cuisines,
                        services);

                for (JsonNode node : jsonNode.path("reviews")) {
                    UUID uId = UUID.fromString(node.path("user").asText());
                    if (!usersById.containsKey(uId)) continue;

                    int value = node.path("velue").asInt();
                    LocalDateTime time = LocalDateTime.parse(node.get("timestamp").asText());
                    String text = node.get("text").asText();
                    String reply = node.get("reply").asText();

                    Review r = new Review(restaurant, usersById.get(uId), value, time, text, reply);
                    restaurant.addReview(r);
                }

                restaurantsById.put(id, restaurant);
                restaurantsByName.put(name, restaurant);

            } catch (IOException e) {
                System.out.println("ERRORE durante il parsing di " + f.getName() + ", causa: " + e.getMessage());
                return;
            } catch (SecurityException e) {
                System.out.println("Accesso negato al file " + f.getName());
                return;
            }
        }

    }

    /**
     * Carica tutti gli utenti dai file specificati e li deserializza in oggetti `User`.
     * Gli utenti possono essere di tipo `Client` o `Owner`.
     *
     * @param uFiles array di file JSON contenenti i dati degli utenti
     */
    private void loadUsers(File[] uFiles) {
        if (uFiles == null) return;

        final ObjectMapper mapper = new ObjectMapper();
        for (File f : uFiles) {
            try {
                JsonNode jsonNode = mapper.readTree(f);
                if (jsonNode == null) continue;

                String id = jsonNode.path("id").asText();
                String username = jsonNode.path("username").asText();
                String name = jsonNode.path("name").asText();
                String lastName = jsonNode.path("lastName").asText();
                LocalDate dateOfBirth = LocalDate.parse(jsonNode.get("dateOfBirth").asText());

                JsonNode location = jsonNode.path("location");
                Location loc = new Location(
                        Nation.valueOf(location.path("nation").asText().toUpperCase().replace(" ","_").replace("_MAINLAND","")),
                        location.path("city").asText(),
                        location.path("latitude").asDouble(),
                        location.path("longitude").asDouble(),
                        location.path("address").asText()
                );

                final JsonNode passwordNode = jsonNode.path("password");
                final String salt = passwordNode.path("salt").asText();
                final String passwordHash = passwordNode.path("password").asText();

                User user;
                if (Objects.equals(jsonNode.get("role").asText(""), "Owner")) {
                    user = new Owner(
                            UUID.fromString(id),
                            username,
                            passwordHash, salt,
                            name, lastName,
                            loc, dateOfBirth
                    );

                } else {
                    Set<UUID> favourites = new HashSet<>();
                    JsonNode favouritesNode = jsonNode.get("favourites");
                    if (favouritesNode != null && favouritesNode.isArray())
                        for (JsonNode fav : favouritesNode)
                            favourites.add(UUID.fromString(fav.asText()));

                    user = new Client(
                            UUID.fromString(id),
                            username,
                            passwordHash, salt,
                            name, lastName,
                            loc, dateOfBirth,
                            favourites
                    );
                }

                usersById.put(UUID.fromString(id), user);
                usersByName.put(username, user);

            } catch (IOException | IllegalArgumentException e) {
                IO.printErrorMessage("ERRORE durante il parsing di " + f.getName() + ", causa: " + e.getMessage());
                return;
            } catch (SecurityException e) {
                IO.printErrorMessage("Accesso negato al file " + f.getName());
                return;
            }

        }

    }

    private void loadMichelin() {
        try {
            // Main output file missing, continuing from
            if (!michelinData.exists() && oldMichelinData.exists()) {
                IO.printErrorMessage("Backup copy of michelin data found. Rolling back to latest version. \n ");
                Files.copy(oldMichelinData.toPath(), michelinData.toPath(), StandardCopyOption.ATOMIC_MOVE);
                return;
            }

            JsonNode jsonNode = new ObjectMapper().readTree(michelinData);
            if (!jsonNode.isArray()) throw new IOException();

            for (JsonNode node : jsonNode) {
                try {
                    UUID id = UUID.fromString(node.path("id").asText());
                    String name = node.path("name").asText();
                    String description = node.path("address").asText();
                    String websiteUrl = node.path("websiteUrl").asText("");
                    String phone = node.path("phone").asText();

                    Award award = Award.fromInt(node.path("award").asInt());
                    boolean greenStar = node.path("greenStar").asBoolean();
                    boolean hasDelivery = node.path("hasDelivery").asBoolean(false);
                    boolean hasBooking = node.path("hasOnlineBooking").asBoolean(false);

                    JsonNode locNode = node.path("location");
                    Location loc = new Location(
                            Nation.valueOf(locNode.path("nation").asText().toUpperCase().replace(" ","_")),
                            locNode.path("city").asText(),
                            locNode.path("latitude").asDouble(),
                            locNode.path("longitude").asDouble(),
                            locNode.path("address").asText()
                    );

                    String price = node.get("priceRange").asText();
                    PriceRange priceRange = PriceRange.byDollarAmount(price.length());

                    Set<CuisineType> cuisines = new HashSet<>();
                    for (JsonNode cuisine : node.path("cuisinesTypes")) {
                        try {
                            cuisines.add(CuisineType.valueOf(cuisine.asText().toUpperCase()));
                        } catch (IllegalArgumentException ignored) {}
                    }

                    Set<String> services = new HashSet<>();
                    for (JsonNode service : node.path("services"))
                        services.add(service.asText());

                    Restaurant restaurant = new Restaurant(
                            id, name, description,
                            websiteUrl, null,
                            phone, loc,
                            priceRange,
                            hasDelivery, hasBooking,
                            award, greenStar,
                            cuisines, services
                    );

                    restaurantsById.put(id,restaurant);
                    restaurantsByName.put(name,restaurant);

                } catch (IllegalArgumentException ignored) {}
            }

        } catch (IOException | IllegalArgumentException e) {
            IO.printErrorMessage("ERRORE durante il parsing di " + michelinData.getName() + ", causa: " + e.getMessage());
        } catch (SecurityException e) {
            IO.printErrorMessage("Accesso negato al file " + michelinData.getName());
        }

    }

    /**
     * Carica tutti i dati degli utenti e dei ristoranti dal file system.
     * I file devono trovarsi nelle directory configurate via `Constants.ROOT`.
     */
    public void loadFromFile() {
        try {
            if (!ROOT.exists()) return;

            File[] restaurants = RESTAURANTS_ROOT.listFiles();
            File[] users = USERS_ROOT.listFiles();

            List<CompletableFuture<Void>> futures = new ArrayList<>();

            // Michelin - completamente indipendente, può andare in parallelo
            if (michelinData.exists()) {
                CompletableFuture<Void> michelinFuture = CompletableFuture
                        .runAsync(Loader::loadMichelin)
                        .exceptionally(ex -> {
                            IO.printErrorMessage("Errore caricamento Michelin: " + ex.getMessage());
                            return null;
                        });
                futures.add(michelinFuture);
            }

            // Users prima, poi restaurants (restaurants dipende da users)
            CompletableFuture<Void> usersAndRestaurantsFuture = CompletableFuture
                    .runAsync(() -> loadUsers(users))
                    .thenRunAsync(() -> loadRestaurants(restaurants))
                    .exceptionally(ex -> {
                        IO.printErrorMessage("Errore caricamento users/restaurants: " + ex.getMessage());
                        return null;
                    });
            
            futures.add(usersAndRestaurantsFuture);

            // Aspetta che tutte le operazioni terminino
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();


            System.out.print("");

        } catch (SecurityException ex) {
            System.out.println("Accesso negato");
            
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

            // input file not found or non in .csv format
            if (!dataset.exists() || !dataset.isFile() || !dataset.getName().endsWith(".csv")) {
                IO.printErrorMessage("File or path " + path + " does not exist or it is not supported by the program, please check and try again.");
                return;

                // dataset parsed at least once
            } else if (michelinData.exists()) {

                // parsed at least more than once, old version present
                if (oldMichelinData.exists()) {
                    IO.printErrorMessage("Backup copy of parsed dataset found. \nWarning: this procedure will overwrite current backup copy, make . \n ");
                    IO.getUserInput("Type 'continue' and hit Enter to proceed");
                }

                Files.copy(michelinData.toPath(), oldMichelinData.toPath(), StandardCopyOption.REPLACE_EXISTING);
                michelinData.delete();
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