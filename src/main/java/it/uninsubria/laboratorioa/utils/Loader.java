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
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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

    /**
     * Directory contenente i file JSON dei ristoranti.
     */
    private static final File RESTAURANTS_ROOT = new File(ROOT, "companies");

    /**
     * Directory contenente i file JSON degli utenti.
     */
    private static final File USERS_ROOT = new File(ROOT, "users");

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
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(f);

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
                    } catch (IllegalArgumentException ignored) {
                    }
                }

                Set<String> services = new HashSet<>();
                for (JsonNode node : jsonNode.path("services")) {
                    services.add(node.asText());
                }

                Restaurant restaurant = new Restaurant(
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
                        services
                );

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
            } catch (SecurityException e) {
                System.out.println("Accesso negato al file " + f.getName());
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
                            username,
                            passwordHash, salt,
                            name, lastName,
                            loc, dateOfBirth
                    );
                } else {
                    Set<UUID> favourites = new HashSet<>();
                    JsonNode favouritesNode = jsonNode.get("favourites");
                    if (favouritesNode != null && favouritesNode.isArray()) {
                        for (JsonNode fav : favouritesNode)
                            favourites.add(UUID.fromString(fav.asText()));
                    }

                    user = new Client(
                            username, passwordHash, salt,
                            name, lastName,
                            loc, dateOfBirth,
                            favourites
                    );
                }

                usersById.put(UUID.fromString(id), user);
                usersByName.put(username, user);

            } catch (IOException | IllegalArgumentException e) {
                System.err.println("ERRORE durante il parsing di " + f.getName() + ", causa: " + e.getMessage());
            } catch (SecurityException e) {
                System.err.println("Accesso negato al file " + f.getName());
            }
        }
    }

    /**
     * Carica tutti i dati degli utenti e dei ristoranti dal file system.
     * I file devono trovarsi nelle directory configurate via `Constants.ROOT`.
     */
    public void loadFromFile() {
        File[] restaurants;
        File[] users;
        try {
            if (!ROOT.exists()) return;

            restaurants = RESTAURANTS_ROOT.listFiles();
            users = USERS_ROOT.listFiles();

            loadUsers(users);
            loadRestaurants(restaurants);

        } catch (SecurityException ex) {
            System.out.println("Accesso negato");
        }
    }
}